package com.lhf.messages.in;

import java.util.EnumSet;

import com.lhf.Taggable;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.map.Directions;
import com.lhf.messages.Command;
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

        @Override
        public HelpInMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new HelpInMessage(command);
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

        @Override
        public SayMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new SayMessage(command);
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

        @Override
        public SeeMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new SeeMessage(command);
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

        @Override
        public GoMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new GoMessage(command);
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

        @Override
        public AttackMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new AttackMessage(command);
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
            return EnumSet.of(Prepositions.AT, Prepositions.USE);
        }

        @Override
        public CastMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new CastMessage(command);
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

        @Override
        public DropMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new DropMessage(command);
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

        @Override
        public EquipMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new EquipMessage(command);
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

        @Override
        public UnequipMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new UnequipMessage(command);
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

        @Override
        public InteractMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new InteractMessage(command);
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

        @Override
        public InventoryMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new InventoryMessage(command);
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

        @Override
        public TakeMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new TakeMessage(command);
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

        @Override
        public UseMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new UseMessage(command);
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

        @Override
        public StatusMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new StatusMessage(command);
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

        @Override
        public ListPlayersMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new ListPlayersMessage(command);
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

        @Override
        public ExitMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new ExitMessage(command);
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

        @Override
        public CreateInMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new CreateInMessage(command);
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

        @Override
        public ShoutMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new ShoutMessage(command);
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

        @Override
        public PassMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new PassMessage(command);
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

        @Override
        public LewdInMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new LewdInMessage(command);
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

        @Override
        public SpellbookMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new SpellbookMessage(command);
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

        @Override
        public StatsInMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new StatsInMessage(command);
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

        @Override
        public RepeatInMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new RepeatInMessage(command);
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

        @Override
        public FollowMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new FollowMessage(command);
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

        @Override
        public RestMessage adaptCommand(Command command) {
            if (command == null || this != command.getType()) {
                throw new IllegalArgumentException(String.format("%s cannot adapt a command like '%s'", this, command));
            }
            return new RestMessage(command);
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

    public abstract CommandAdapter adaptCommand(Command command);

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
