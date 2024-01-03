# Standard Map

rooms listed by number, adjacent rooms are connected in some way

```
x | x | 7 | x |
x | 6 | x | 8 |
1 | 2 | 3 | 4 |
x | x | x | 5 | Secret room
``````

1. entry hall
2. history room
3. offering room
4. trapped hall
5. statue room
6. armory

or mermaided

```mermaid
flowchart LR
    e3e38e10-70bd-45ad-abb4-6180622244a5[Entry Room]
    da8d2208-a7e1-4e4b-8dd9-cd6b0b8c99e6[History Hall]
    490b8fa6-fb42-4bc2-8c86-42bb07ee8bd6[Offering Room]
    e7a8147f-07e7-4e91-a6d2-e5a443f0681a[Armory]
    ac82b011-3797-410d-9381-339aef704531[Trapped Room]
    815d0325-224b-4c87-af82-17b5232bbc40[Passageway]
    e0e05a7d-7358-4e6d-95c9-ec209caf25d2[Vault]
    d2dfb991-f086-423c-94d4-aee8c21dd424[Statue Room]
    014f7408-e87d-4fec-9fb8-6b6f4959b7ed[Secret Room]

    e3e38e10-70bd-45ad-abb4-6180622244a5-->|east|da8d2208-a7e1-4e4b-8dd9-cd6b0b8c99e6
    da8d2208-a7e1-4e4b-8dd9-cd6b0b8c99e6-->|west|e3e38e10-70bd-45ad-abb4-6180622244a5
    da8d2208-a7e1-4e4b-8dd9-cd6b0b8c99e6-->|east|490b8fa6-fb42-4bc2-8c86-42bb07ee8bd6
    da8d2208-a7e1-4e4b-8dd9-cd6b0b8c99e6-->|north|e7a8147f-07e7-4e91-a6d2-e5a443f0681a
    490b8fa6-fb42-4bc2-8c86-42bb07ee8bd6-->|east|ac82b011-3797-410d-9381-339aef704531
    490b8fa6-fb42-4bc2-8c86-42bb07ee8bd6-->|west|da8d2208-a7e1-4e4b-8dd9-cd6b0b8c99e6
    e7a8147f-07e7-4e91-a6d2-e5a443f0681a-->|south|da8d2208-a7e1-4e4b-8dd9-cd6b0b8c99e6
    e7a8147f-07e7-4e91-a6d2-e5a443f0681a-->|north|815d0325-224b-4c87-af82-17b5232bbc40
    ac82b011-3797-410d-9381-339aef704531-->|west|490b8fa6-fb42-4bc2-8c86-42bb07ee8bd6
    ac82b011-3797-410d-9381-339aef704531-->|south|d2dfb991-f086-423c-94d4-aee8c21dd424
    ac82b011-3797-410d-9381-339aef704531-->|north|e0e05a7d-7358-4e6d-95c9-ec209caf25d2
    815d0325-224b-4c87-af82-17b5232bbc40-->|south|e7a8147f-07e7-4e91-a6d2-e5a443f0681a
    815d0325-224b-4c87-af82-17b5232bbc40-->|east|e0e05a7d-7358-4e6d-95c9-ec209caf25d2
    e0e05a7d-7358-4e6d-95c9-ec209caf25d2-->|south|ac82b011-3797-410d-9381-339aef704531
    e0e05a7d-7358-4e6d-95c9-ec209caf25d2-->|west|815d0325-224b-4c87-af82-17b5232bbc40
    d2dfb991-f086-423c-94d4-aee8c21dd424-->|north|ac82b011-3797-410d-9381-339aef704531
    014f7408-e87d-4fec-9fb8-6b6f4959b7ed-->|west|d2dfb991-f086-423c-94d4-aee8c21dd424
```
