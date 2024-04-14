package com.lhf.messages.in;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.lhf.Taggable;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.map.Directions;
import com.lhf.game.serialization.GsonBuilderFactory;
import com.lhf.messages.Command;
import com.lhf.messages.ICommand;
import com.lhf.messages.grammar.PhraseList;
import com.lhf.messages.grammar.PrepositionalPhrases;
import com.lhf.messages.grammar.Prepositions;

public enum AMessageType implements Taggable {
    HELP {

        @Override
        public AttackMessage generateCommand(String whole, Boolean accepted, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new AttackMessage(this, whole, accepted, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            return command != null && this.equals(command.getType());
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.allOf(Prepositions.class);
        }
    },
    SAY {
        @Override
        public SayMessage generateCommand(String whole, Boolean accepted, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new SayMessage(this, whole, accepted, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            if (command == null) {
                return false;
            }
            Boolean validated = true;
            if (command.getIndirects().size() > 0) {
                validated = command.getIndirects().size() == 1 && command.getIndirects().containsKey(Prepositions.TO);
            }
            return command.getDirects().size() >= 1 && validated;
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.of(Prepositions.TO);
        }

    },
    SEE {

        @Override
        public SeeMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new SeeMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            if (command == null) {
                return false;
            }
            return command.getDirects().size() >= 0 && command.getIndirects().size() == 0;
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.noneOf(Prepositions.class);
        }

    },
    GO {
        @Override
        public GoMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new GoMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            return command != null && command.getDirects().size() == 1
                    && Directions.isDirections(command.getDirects().get(0)) && command.getIndirects().isEmpty();
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.noneOf(Prepositions.class);
        }

    },
    ATTACK {
        @Override
        public AttackMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new AttackMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            return command != null && command.getDirects().size() >= 1 && command.getIndirects().size() <= 1;
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.of(Prepositions.WITH);
        }

    },
    CAST {
        @Override
        public CastMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new CastMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            if (command == null) {
                return false;
            }
            Boolean indirectsvalid = true;
            if (command.getIndirects().size() >= 1) {
                indirectsvalid = command.getIndirects().containsKey(Prepositions.AT)
                        || command.getIndirects().containsKey(Prepositions.USE)
                        || command.getIndirects().containsKey(Prepositions.WITH);
            }
            return command.getDirects().size() == 1 && indirectsvalid;
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.of(Prepositions.AT, Prepositions.USE, Prepositions.AS, Prepositions.WITH);
        }

    },
    DROP {
        @Override
        public DropMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new DropMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            if (command == null) {
                return false;
            }
            boolean indirectsvalid = true;
            if (command.getIndirects().size() >= 1) {
                indirectsvalid = command.getIndirects().size() == 1
                        && command.getIndirects().containsKey(Prepositions.IN)
                        && command.getIndirects().getOrDefault(Prepositions.IN, null) != null;
            }
            return command.getDirects().size() >= 1 && indirectsvalid;
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.of(Prepositions.IN);
        }

    },
    EQUIP {
        @Override
        public EquipMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new EquipMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            if (command == null) {
                return false;
            }
            Boolean validated = true;
            if (command.getIndirects().size() > 1) {
                validated = false;
            } else {
                if (command.getIndirects().containsKey(Prepositions.TO)) {
                    final List<String> toList = command.getByPreposition(Prepositions.TO);
                    if (toList == null || toList.size() != 1) {
                        validated = false;
                    } else {
                        validated = EquipmentSlots.isEquipmentSlot(command.getByPrepositionAsString(Prepositions.TO));
                    }
                }
            }
            return command.getDirects().size() >= 1 && validated;
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.of(Prepositions.TO);
        }

    },
    UNEQUIP {
        @Override
        public UnequipMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new UnequipMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            return command != null && command.getDirects().size() == 1 && command.getIndirects().isEmpty();
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.noneOf(Prepositions.class);
        }

    },
    INTERACT {
        @Override
        public InteractMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new InteractMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            return command != null && command.getIndirects().isEmpty() && command.getDirects().size() >= 1;
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.noneOf(Prepositions.class);
        }

    },
    INVENTORY {
        @Override
        public InventoryMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new InventoryMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            return command != null && command.getDirects().isEmpty() && command.getIndirects().isEmpty();
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.noneOf(Prepositions.class);
        }
    },
    TAKE {
        @Override
        public TakeMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new TakeMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            if (command == null) {
                return false;
            }
            boolean indirectsvalid = true;
            if (command.getIndirects().size() >= 1) {
                indirectsvalid = command.getIndirects().size() == 1
                        && command.getIndirects().containsKey(Prepositions.FROM)
                        && command.getIndirects().getOrDefault(Prepositions.FROM, null) != null;
            }
            return command.getDirects().size() >= 1 && indirectsvalid;
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.of(Prepositions.FROM);
        }

    },
    USE {
        @Override
        public UseMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new UseMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            if (command == null) {
                return false;
            }
            Boolean validated = true;
            if (command.getIndirects().size() > 0) {
                validated = command.getIndirects().containsKey(Prepositions.ON) && command.getIndirects().size() == 1;
            }
            return command.getDirects().size() == 1 && validated;
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.of(Prepositions.ON);
        }

    },
    STATUS {
        @Override
        public StatusMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new StatusMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            return command != null && command.getDirects().isEmpty() && command.getIndirects().isEmpty();
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.noneOf(Prepositions.class);
        }

    },
    PLAYERS {
        @Override
        public ListPlayersMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new ListPlayersMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            return command != null && command.getDirects().isEmpty() && command.getIndirects().isEmpty();
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.noneOf(Prepositions.class);
        }

    },
    EXIT {
        @Override
        public ExitMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new ExitMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            return command != null;
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.allOf(Prepositions.class);
        }

    },
    CREATE {
        @Override
        public CreateInMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new CreateInMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            if (command == null) {
                return false;
            }
            if (command.getDirects().size() != 1) {
                return false;
            }
            String name = command.getDirects().get(0);
            if (name == null) {
                return false;
            }
            name = name.trim();
            if (name.isBlank()) {
                return false;
            }
            final Map<Prepositions, String> indirects = command.getIndirectsAsStrings();
            if (indirects == null || indirects.size() < 1 || indirects.size() > 2
                    || !indirects.containsKey(Prepositions.WITH)) {
                return false;
            }
            if (indirects.containsKey(Prepositions.AS)) {
                return VocationName.isVocationName(indirects.getOrDefault(Prepositions.AS, null));
            } else if (indirects.containsKey(Prepositions.JSON)) {
                final String json = indirects.getOrDefault(Prepositions.JSON, null);
                if (!GsonBuilderFactory.checkValidJSON(json)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.of(Prepositions.WITH, Prepositions.AS, Prepositions.JSON);
        }

    },
    SHOUT {
        @Override
        public ShoutMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new ShoutMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            return command != null && command.getDirects().size() >= 1 && command.getIndirects().isEmpty();
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.noneOf(Prepositions.class);
        }

    },
    PASS {
        @Override
        public PassMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new PassMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            return command != null && command.getDirects().isEmpty() && command.getIndirects().isEmpty();
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.noneOf(Prepositions.class);
        }

    },
    LEWD {
        @Override
        public LewdInMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new LewdInMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            if (command == null) {
                return false;
            }
            boolean indirectsvalid = true;
            final Map<Prepositions, List<String>> indirects = command.getIndirects();
            if (indirects == null || indirects.size() == 0) {
                indirectsvalid = true;
            } else if (indirects.containsKey(Prepositions.USE) || indirects.containsKey(Prepositions.AS)) {
                final List<String> used = indirects.getOrDefault(Prepositions.USE, null);
                final List<String> ased = indirects.getOrDefault(Prepositions.AS, null);
                if (used != null && ased != null && indirects.size() == 2) {
                    indirectsvalid = indirectsvalid && !used.isEmpty() && !ased.isEmpty() && used.size() == ased.size();
                } else if (used != null && indirects.size() == 1) {
                    indirectsvalid = indirectsvalid && !used.isEmpty();
                } else if (ased != null && indirects.size() == 1) {
                    indirectsvalid = indirectsvalid && !ased.isEmpty();
                } else {
                    indirectsvalid = false;
                }
                if (indirectsvalid && ased != null) {
                    for (Iterator<String> asIterator = ased.iterator(); indirectsvalid && asIterator.hasNext();) {
                        final String vocationName = asIterator.next();
                        indirectsvalid = indirectsvalid && VocationName.isVocationName(vocationName);
                    }
                }
            } else if (indirects.size() == 1 && indirects.containsKey(Prepositions.JSON)) {
                final List<String> jsonListing = indirects.getOrDefault(Prepositions.JSON, null);
                indirectsvalid = indirectsvalid && jsonListing != null && jsonListing.size() == 1;
                if (indirectsvalid && jsonListing != null) {
                    final String json = jsonListing.get(0);
                    if (json == null) {
                        return false;
                    }
                    if (!GsonBuilderFactory.checkValidJSON(json)) {
                        return false;
                    }
                }
            } else {
                indirectsvalid = false;
            }
            return indirectsvalid && command.getDirects().size() >= 1;
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.of(Prepositions.USE, Prepositions.AS, Prepositions.JSON);
        }
    },
    SPELLBOOK {
        @Override
        public SpellbookMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new SpellbookMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            return command != null && command.getDirects().size() >= 0
                    && (command.getIndirects().size() >= 1
                            ? command.getIndirects().getOrDefault(Prepositions.WITH, null) != null
                            : true);
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.of(Prepositions.WITH);
        }

    },
    STATS {
        @Override
        public StatsInMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new StatsInMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            return command != null;
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.allOf(Prepositions.class);
        }

    },
    REPEAT {
        @Override
        public RepeatInMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new RepeatInMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            return command != null && command.getDirects().isEmpty() && command.getIndirects().isEmpty();
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.noneOf(Prepositions.class);
        }

    },
    FOLLOW {
        @Override
        public FollowMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new FollowMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            if (command == null || command.getDirects().size() != 1) {
                return false;
            }
            if (command.getIndirects().size() > 2) {
                return false;
            } else if (command.getIndirects().size() == 0) {
                return true;
            }
            if (command.getIndirects().size() == 1 && ("override"
                    .equalsIgnoreCase(command.getIndirects().getOrDefault(Prepositions.AS, List.of("invalid")).get(0))
                    || "null".equalsIgnoreCase(
                            command.getIndirects().getOrDefault(Prepositions.USE, List.of("invalid")).get(0)))) {
                return true;
            } else if (command.getIndirects().size() == 2 && ("override"
                    .equalsIgnoreCase(command.getIndirects().getOrDefault(Prepositions.AS, List.of("invalid")).get(0))
                    && "null".equalsIgnoreCase(
                            command.getIndirects().getOrDefault(Prepositions.USE, List.of("invalid")).get(0)))) {
                return true;
            }
            return false;
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.of(Prepositions.USE, Prepositions.AS);
        }

    },
    REST {
        @Override
        public RestMessage generateCommand(String whole, Boolean isValid, PhraseList phrases,
                PrepositionalPhrases prepositional) {
            return new RestMessage(this, whole, isValid, phrases, prepositional);
        }

        @Override
        public boolean checkValidity(ICommand command) {
            return command != null;
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.noneOf(Prepositions.class);
        }

    };

    public static AMessageType getCommandMessage(String value) {
        for (AMessageType v : values()) {
            if (v.toString().equalsIgnoreCase(value)) {
                return v;
            }
        }
        return null;
    }

    public static Boolean isCommandMessage(String value) {
        for (AMessageType v : values()) {
            if (v.toString().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }

    public abstract Command generateCommand(String whole, Boolean isValid, PhraseList phrases,
            PrepositionalPhrases prepositional);

    public abstract boolean checkValidity(ICommand command);

    public abstract EnumSet<Prepositions> getAllowedPrepositions();

    @Override
    public String getTagName() {
        return "command";
    }

    @Override
    public String getSimpleContent() {
        return this.toString();
    }

}
