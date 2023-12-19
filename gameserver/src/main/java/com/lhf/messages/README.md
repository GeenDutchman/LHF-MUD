## Full definition


```mermaid
stateDiagram-v2
    direction LR
    [*] --> Command
    Command --> [*]

    state WhatList {
        direction TB
        state What {
            direction LR
            whatWord: Word
            [*] --> whatWord
            betweenWordSpace: Space
            whatWord --> betweenWordSpace
            betweenWordSpace --> whatWord
            whatWord --> [*]
        }
        interWhatSpace: Space
        [*] --> interWhatSpace
        interWhatSpace --> What
        preCommaSpace: Space
        What --> preCommaSpace
        preCommaSpace --> Comma
        
        What --> Comma
        Comma --> interWhatSpace
        Comma --> What
        What --> [*]
    }
    Command --> WhatList
    WhatList --> [*]

    state Prepositions{
        state PrepPhrase {
            [*] --> Preposition
            postPrepSpace: Space
            Preposition --> postPrepSpace
            state IndirectObject {
                direction LR
                ioWord: Word
                [*] --> ioWord
                betweenIoSpace: Space
                ioWord --> betweenIoSpace
                betweenIoSpace --> ioWord
                ioWord --> [*]
            }
            postPrepSpace --> IndirectObject
            IndirectObject --> [*]
        }
        interPhraseSpace: Space
        [*] --> interPhraseSpace
        interPhraseSpace --> PrepPhrase
        PrepPhrase --> interPhraseSpace
        PrepPhrase --> [*]
    }
    WhatList --> Prepositions
    Prepositions --> [*]


```

## Tokens

- any word
- any character that's not a space
- any space

## Examples

### Attack

- attack goblin
- attack goblin with sword
- attack "boblin the goblin"
- attack "boblin the goblin" with sword
- attack "boblin the goblin", "morc the orc"
- attack "boblin the goblin", "morc the orc" with sword
- attack "boblin the goblin", "boblin the goblin"
- attack "boblin the goblin", "boblin the goblin" with sword
- attack boblin the goblin with sword
- attack boblin the goblin, morc the orc with sword
- ~~attack boblin with eyes with sword~~
- ~~attack goblin with with sword~~
- ~~attack goblin with sword with bow~~
- attack "goblin with eyes"
- attack "goblin with eyes" with sword