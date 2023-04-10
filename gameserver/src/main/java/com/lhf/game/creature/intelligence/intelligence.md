# Intelligence

Creatures that are not Players should be able to take actions as well.  This package will let a basic ai control their actions.  Most of these actions are reactive to messages that come in.

```mermaid
sequenceDiagram
    actor AIRunner
    activate AIRunner
    par receive message
        ClientMessenger ->>+ BasicAI : sendMsg(OutMessage)
        BasicAI ->>- BasicAI : enqueue message
    and process message
        loop when message found in queue
            AIRunner ->>+ BasicAI : process(msg)
            opt handler found for type
                BasicAI ->>+ AIChunk : handle(msg)
                loop while processing
                    opt send message
                        AIChunk ->>+ BasicAI: CommandMessage
                        BasicAI ->> MessageHandler: handleMessage(CommandMessage)
                        deactivate BasicAI
                    end
                end
                AIChunk ->>- BasicAI: return void
            end
            BasicAI ->>- ClientMessenger: Null
        end
    end

```

## AIRunner

The AIRunner interface allows for a separate thread to handle the processing of messages, rather than the main thread.  You can stop it and check if it is stopped.  You can queue up attention and register an NPC with it.

### GroupAIRunner

A queued implementation of an AIRunner.

