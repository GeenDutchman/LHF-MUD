# Basic Structure

```mermaid
classDiagram

  class Examinable
  <<interface>> Examinable
  class Taggable
  <<interface>> Taggable

  class TaggedExaminable
  <<interface>> TaggedExaminable

  Taggable <|-- TaggedExaminable
  Examinable <|-- TaggedExaminable

  note for TaggedExaminable "Not all things inherit from this,\n which is why Examinable and Tagged \nare still separate"

  class ItemContainer
  <<interface>> ItemContainer
  class CreatureContainer
  <<interface>> CreatureContainer
  class Item
  <<abstract>> Item
  class Takeable
  class Usable
  class Equipable
  class Stackable
  class InteractObject
  class Weapon

  ItemContainer o-- Item
  TaggedExaminable <|-- Item
  Item <|.. Takeable
  Item <|.. InteractObject
  Takeable <|-- Usable
  Usable <|-- Equipable
  Usable <|-- Stackable
  Equipable <|-- Weapon

  ItemContainer <|.. Inventory


  class Room
  CreatureContainer <|.. Room
  ItemContainer <|.. Room

  TaggedExaminable <|-- CreatureContainer
  TaggedExaminable <|-- ItemContainer

  class ICreature
  <<interface>> ICreature
  class INonPlayerCharacter
  <<interface>> INonPlayerCharacter
  class IMonster
  <<interface>> IMonster

  TaggedExaminable <|-- ICreature

  ICreature <|-- INonPlayerCharacter
  INonPlayerCharacter <|-- IMonster

  class Creature
  <<abstract>> Creature
  class NonPlayerCharacter
  class Monster
  class DungeonMaster
  class Player

  ICreature <|.. Creature
  CreatureContainer o-- ICreature


  Creature <|.. Player
  Creature <|.. NonPlayerCharacter
  INonPlayerCharacter <|.. NonPlayerCharacter
  NonPlayerCharacter <|-- Monster
  IMonster <|.. Monster
  NonPlayerCharacter <|-- DungeonMaster

  Inventory --* ICreature


```

## More information
* [Dice](gameserver/src/main/java/com/lhf/game/dice/README.md) - one example of something `Taggable` but NOT `Examinable`

# Server Design

## Chain of Command

```mermaid
classDiagram
    class MessageChainHandler
    <<interface>> MessageChainHandler
    MessageChainHandler: +handleMessage(context, Command)
    class Main
    class Server
    class UserManager
    class User
    class ClientManager
    class Client
    class Game
    class ThirdPower
    class Dungeon
    class Room
    class Player
    class BattleManager    

    Main *-- Server
    Server *-- ClientManager
    Server *-- UserManager
    Server *-- Game

    UserManager o-- User
    ClientManager o-- Client


    Game *-- ThirdPower

    Game *-- Dungeon

    Dungeon o-- Room

    Room o-- Player

    User <-- Client
    Player <-- User
    Room <.. Player
    Room <-- BattleManager
    BattleManager <.. Player
    Dungeon <-- Room
    ThirdPower <-- Dungeon
    Game <-- ThirdPower
    Server <-- Game    

    Room o-- BattleManager
    
    MessageChainHandler <|-- BattleManager
    MessageChainHandler <|-- Player
    MessageChainHandler <|-- Room
    MessageChainHandler <|-- Dungeon
    MessageChainHandler <|-- ThirdPower
    MessageChainHandler <|-- Game
    MessageChainHandler <|-- Server
    MessageChainHandler <|-- User
    MessageChainHandler <|-- Client

```

### More information
* [Magic](./game/magic/README.md) - what the `ThirdPower` is all about
* [AI](./game/creature/intelligence/README.md) - The `NonPlayerCharacter`s artificial intelligence

## Sequence Diagram of Commands

```mermaid
sequenceDiagram
    Main ->> Server: Start
    Main ->> Game: Start
    Game --> Dungeon: initialize()
    loop every room
        Dungeon --> Room: initialize()
        Dungeon ->> Room: setSuccessor()
    end
    Game ->> Dungeon: setSuccessor()
    Server --> ClientManager: initialize()
    actor enduser
    enduser --> Server: connect
    activate Server
    Server ->> ClientManager: addClient()
    activate ClientManager
    participant Client
    ClientManager --> Client: initialize
    activate Client
    Client --> Client: handleCommand
    Client ->> enduser: reply
    deactivate ClientManager
    deactivate Server
    deactivate Client

    enduser ->> Client: createCommand
    activate Client
        Client ->> Server: createUser()
        activate Server
            Server ->> Game: newPlayer
            activate Game
                Game --> Player: newPlayer
                Game ->> Dungeon: addPlayer()
                activate Dungeon
                    Dungeon ->> Room: addPlayer()
                    activate Room
                        Room ->> Player: setSuccessor()
                        Player --> Room: done
                    deactivate Room
                    Room --> Dungeon: done
                deactivate Dungeon
                Dungeon --> Game: done
            deactivate Game
            Game --> Server: done
        deactivate Server
        Server --> Client: done
    deactivate Client
    Client --> enduser: done

```

### More Information

* [Commands](./messages/README.md)
* [Out Messages](./messages/out/README.md)