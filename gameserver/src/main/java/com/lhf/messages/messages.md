## Command Grammar

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
            whatWord -->  whatWord
            whatWord --> [*]
        }
        [*] --> What
        
        What --> Comma
        Comma --> What
        What --> [*]
    }
    Command --> WhatList
    WhatList --> [*]

    state Prepositions{
        state PrepPhrase {
            state IndirectObject {
                direction LR
                ioWord: Word
                [*] --> ioWord
                ioWord --> ioWord
                ioWord --> [*]
            }
            [*] --> Preposition
            Preposition --> IndirectObject
            IndirectObject --> [*]
        }
        [*]  --> PrepPhrase
        PrepPhrase --> PrepPhrase
        PrepPhrase --> [*]
    }
    WhatList --> Prepositions
    Prepositions --> [*]


```


### Full definition


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