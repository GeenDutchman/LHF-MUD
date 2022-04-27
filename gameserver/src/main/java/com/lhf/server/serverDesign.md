```mermaid
classDiagram
    class MessageHandler
    <<interface>> MessageHandler
    MessageHandler: +handleMessage(context, Command)
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
    

    Main *-- Server

    Server *-- UserManager
    Server *-- ClientManager
    Server *-- Game

    ClientManager o-- Client

    UserManager o-- User

    Game *-- ThirdPower

    Game *-- Dungeon

    Dungeon o-- Room

    Room o-- Player

    Player <-- Client
    Room <-- Player
    Dungeon <-- Room
    ThirdPower <-- Dungeon
    Game <-- ThirdPower
    Server <-- Game

    MessageHandler <|-- Player
    MessageHandler <|-- Room
    MessageHandler <|-- Dungeon
    MessageHandler <|-- ThirdPower
    MessageHandler <|-- Game
    MessageHandler <|-- Server
    MessageHandler <|-- Client

```


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