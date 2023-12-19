# Conversation

## Conceptualization

The first thing is that we need a sort of tree structure, potentially a DAG.

```mermaid
flowchart TB
    start -->|greeting|first([hello])
    first -->|keywordOne|this([this branch])
    first -->|keywordTwo|that([that branch])
    that -.->|keywordThree|first
```

When the conversation goes off an end, then we can set up a mechanism to go back to the start node (so thus not a DAG).

So at the very least, each `node` of conversation should have some sort of `body` of text.  That `body` should contain or hint at (tag?) key words that can direct the conversation to a new `node`.

The `tree` of `node`s should maintain some sort of bookmark indicating which `node` of the conversation a `creature` is currently at.  The `tree` should maintain what greeting is used to start the conversation.  It should also maintain a specific keyword to repeat the current `node`'s `body`.

Optionally, the `NPC` could "forget" about where a `creature` is in the conversation when the `creature` leaves the `room`.  And the `NPC` can potentially broadcast the greeting when a `creature` enters the room.

## Implementation

```mermaid
classDiagram
    direction TD

    class Node {
        -UUID nodeID
        -String body
        +UUID getNodeID()
        +String getBody()
    }

    class Branch {
        -Pattern regex
        -UUID nodeID
        +Pattern getRegex()
        +UUID getNodeID()
    }

    class Tree {
        -Node start
        -Map[UUID, Node] nodes
        -Map[UUID, Branch] branches
        -Map[Creature, UUID] bookmarks
        -Set[Branch] greetings
        -Set[String] repeatWords 
        -String repeat(Creature c)
        -String greet(Creature c)
        +String listen(Creature c, String message)
        +void forget(Creature c)
        +Node addNode(UUID nodeID, Pattern regex, Node nextNode)
        +String broadcast()
    }

    Node --o Tree
    Branch --o Tree
    Branch --> Node
```
