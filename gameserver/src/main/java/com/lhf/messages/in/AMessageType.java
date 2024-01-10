package com.lhf.messages.in;

import java.util.EnumSet;

import com.lhf.Taggable;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.map.Directions;
import com.lhf.messages.ICommand;
import com.lhf.messages.grammar.Prepositions;

public enum AMessageType implements Taggable {
    HELP {
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
        public boolean checkValidity(ICommand command) {
            if (command == null) {
                return false;
            }
            Boolean indirectsvalid = true;
            if (command.getIndirects().size() >= 1) {
                indirectsvalid = command.getIndirects().containsKey(Prepositions.AT)
                        || command.getIndirects().containsKey(Prepositions.USE);
            }
            return command.getDirects().size() == 1 && indirectsvalid;
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.of(Prepositions.AT, Prepositions.USE, Prepositions.AS);
        }

    },
    DROP {
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
        public boolean checkValidity(ICommand command) {
            if (command == null) {
                return false;
            }
            Boolean validated = true;
            if (command.getIndirects().size() > 1) {
                validated = false;
            } else {
                if (command.getIndirects().containsKey(Prepositions.TO)) {
                    validated = command.getDirects().size() == 1
                            && EquipmentSlots.isEquipmentSlot(command.getByPreposition(Prepositions.TO));
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
        public boolean checkValidity(ICommand command) {
            return command != null && command.getDirects().size() == 1 && !command.getDirects().get(0).trim().isBlank()
                    && command.getIndirects().size() >= 1 && command.getIndirects().containsKey(Prepositions.WITH);
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.of(Prepositions.WITH, Prepositions.AS);
        }

    },
    SHOUT {
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
        public boolean checkValidity(ICommand command) {
            if (command == null) {
                return false;
            }
            boolean indirectsvalid = true;
            if (command.getIndirects().size() > 0) {
                final String indirect = command.getIndirects().getOrDefault(Prepositions.USE, null);
                indirectsvalid = indirect != null && !indirect.isBlank() && command.getDirects().size() >= 1;
            }
            return indirectsvalid;
        }

        @Override
        public EnumSet<Prepositions> getAllowedPrepositions() {
            return EnumSet.of(Prepositions.USE);
        }
    },
    SPELLBOOK {
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
        public boolean checkValidity(ICommand command) {
            if (command == null || command.getDirects().size() != 1) {
                return false;
            }
            if (command.getIndirects().size() > 2) {
                return false;
            } else if (command.getIndirects().size() == 0) {
                return true;
            }
            if (command.getIndirects().size() == 1
                    && ("override".equalsIgnoreCase(command.getIndirects().get(Prepositions.AS))
                            || "null".equalsIgnoreCase(command.getIndirects().get(Prepositions.USE)))) {
                return true;
            } else if (command.getIndirects().size() == 2
                    && ("override".equalsIgnoreCase(command.getIndirects().get(Prepositions.AS))
                            && "null".equalsIgnoreCase(command.getIndirects().get(Prepositions.USE)))) {
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

    public abstract boolean checkValidity(ICommand command);

    public abstract EnumSet<Prepositions> getAllowedPrepositions();

    @Override
    public String getStartTag() {
        return "<command>";
    }

    @Override
    public String getEndTag() {
        return "</command>";
    }

    @Override
    public String getColorTaggedName() {
        return this.getStartTag() + this.toString() + this.getEndTag();
    }

}
