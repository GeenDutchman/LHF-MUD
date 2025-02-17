package com.lhf.game.item;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.Consumer;

import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.messages.events.SeeEvent.ABuilder;

public interface RenameCapability extends ItemCapability {
    public String getAlternateName();

    public Deque<String> getNameStack();

    public String popName();

    public RenameCapability addName(String newName);

    public RenameCapability clear();

    @Override
    default boolean isStateful() {
        return true;
    }

    @Override
    default void describe(ABuilder<?> seeEventBuilder) {
        return;
    }

    public static final class Delta implements ICapabilityDelta, Consumer<RenameCapability> {
        private final String nameToAdd;
        private final boolean popName;
        private final boolean clearNames;

        public static Delta ofClearNames() {
            return new Delta(null, false, true);
        }

        public static Delta ofPopName() {
            return new Delta(null, true, false);
        }

        public static Delta ofNextName(String name) {
            if (name == null || !name.matches(IItem.ITEM_NAMES)) {
                return null;
            }
            return new Delta(name, false, false);
        }

        private Delta(String nameToAdd, boolean popName, boolean clearNames) {
            this.nameToAdd = nameToAdd;
            this.popName = popName;
            this.clearNames = clearNames;
        }

        @Override
        public void accept(RenameCapability arg0) {
            if (arg0 != null) {
                if (this.popName) {
                    arg0.popName();
                }
                if (this.clearNames) {
                    arg0.clear();
                }
                if (nameToAdd != null) {
                    arg0.addName(nameToAdd);
                }
            }
        }

        public Delta invert() {
            // can't really invert this, but here's a default
            return new Delta(null, !popName, !clearNames);
        }

        @Override
        public void buildOutput(RichOutputBuilder builder) {
            if (builder == null) {
                return;
            }
            builder.appendString("After application, the renaming capability");
            StringJoiner sj = new StringJoiner(", ").setEmptyValue("will not have changed much at all");
            if (this.popName) {
                sj.add("will have the top temporary name removed");
            }
            if (this.clearNames) {
                sj.add("will have all temporary names removed");
            }
            if (this.nameToAdd != null) {
                sj.add("will have the temporary name '" + this.nameToAdd + "' added");
            }
            builder.appendString(sj.toString() + ".");
        }

        public String getNameToAdd() {
            return nameToAdd;
        }

        public boolean isPopName() {
            return popName;
        }

        public boolean isClearNames() {
            return clearNames;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Delta [nameToAdd=").append(nameToAdd).append(", popName=").append(popName)
                    .append(", clearNames=").append(clearNames).append("]");
            return builder.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(nameToAdd, popName, clearNames);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Delta))
                return false;
            Delta other = (Delta) obj;
            return Objects.equals(nameToAdd, other.nameToAdd) && popName == other.popName
                    && clearNames == other.clearNames;
        }

    }

    public default void acceptDelta(Delta delta) {
        if (delta != null) {
            delta.accept(this);
        }
    }

    @Override
    public default ItemCapabilityNames getCapabilityName() {
        return ItemCapabilityNames.RENAMEABLE;
    }

    public static RenameCapability generateRenameCapability() {
        return new Renameable();
    }

    public static String displayName(IItem myItem) {
        if (myItem == null) {
            return null;
        }
        final RenameCapability capability = myItem.getRenameCapability();
        if (capability != null) {
            final String alternate = capability.getAlternateName();
            if (alternate != null && !alternate.isBlank()) {
                return alternate;
            }
        }
        return myItem.getName();
    }

    public static final class Renameable implements RenameCapability {
        private LinkedList<String> alternateNames;

        @Override
        public String getAlternateName() {
            return this.alternateNames != null ? this.alternateNames.peekFirst() : null;
        }

        @Override
        public Deque<String> getNameStack() {
            return this.alternateNames;
        }

        @Override
        public Renameable addName(String newName) {
            if (newName == null) {
                return this;
            }
            if (!newName.matches(IItem.ITEM_NAMES)) {
                throw new IllegalArgumentException(
                        String.format("Item name must match the regex '%s', but was '%s'", IItem.ITEM_NAMES, newName));
            }
            if (this.alternateNames == null) {
                this.alternateNames = new LinkedList<>();
            }
            this.alternateNames.addFirst(newName);
            return this;
        }

        @Override
        public Renameable clear() {
            if (this.alternateNames != null) {
                this.alternateNames.clear();
            }
            return this;
        }

        @Override
        public String popName() {
            return this.alternateNames != null ? this.alternateNames.removeFirst() : null;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Renameable [alternateNames=").append(alternateNames).append("]");
            return builder.toString();
        }

    }

}
