package com.lhf.game.item;

import java.util.Objects;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EntityEffectSource;
import com.lhf.game.TickType;

public class ItemEffectSource extends EntityEffectSource {
    private final GameEventProcessorCapability.Delta gameEventProcessorCapabilityDelta;
    private final InteractableCapability.Delta interactableCapabilityDelta;
    private final RenameCapability.Delta renameCapabilityDelta;
    private final UsableCapability.Delta usableCapabilityDelta;
    private final LockingCapability.Delta lockingCapabilityDelta;

    public static class Builder extends EntityEffectSource.Builder<Builder> {
        private GameEventProcessorCapability.Delta gameEventProcessorCapabilityDelta;
        private InteractableCapability.Delta interactableCapabilityDelta;
        private RenameCapability.Delta renameCapabilityDelta;
        private UsableCapability.Delta usableCapabilityDelta;
        private LockingCapability.Delta lockingCapabilityDelta;

        public Builder(String name) {
            super(name);
            this.gameEventProcessorCapabilityDelta = null;
            this.interactableCapabilityDelta = null;
            this.renameCapabilityDelta = null;
            this.usableCapabilityDelta = null;
            this.lockingCapabilityDelta = null;
            super.setPersistence(new EffectPersistence(TickType.INSTANT)); // instant as long as there's no effect
                                                                           // addition
        }

        @Override
        @Deprecated
        /**
         * @deprecated So far, all item effects are intantaneous, so leave this alone
         *             for now
         */
        public Builder setPersistence(EffectPersistence persistence) {
            return super.setPersistence(persistence);
        }

        public GameEventProcessorCapability.Delta getGameEventProcessorCapabilityDelta() {
            return gameEventProcessorCapabilityDelta;
        }

        public Builder setGameEventProcessorCapabilityDelta(
                GameEventProcessorCapability.Delta gameEventProcessorCapabilityDelta) {
            this.gameEventProcessorCapabilityDelta = gameEventProcessorCapabilityDelta;
            return this;
        }

        public InteractableCapability.Delta getInteractableCapabilityDelta() {
            return interactableCapabilityDelta;
        }

        public Builder setInteractableCapabilityDelta(InteractableCapability.Delta interactableCapabilityDelta) {
            this.interactableCapabilityDelta = interactableCapabilityDelta;
            return this;
        }

        public RenameCapability.Delta getRenameCapabilityDelta() {
            return renameCapabilityDelta;
        }

        public Builder setRenameCapabilityDelta(RenameCapability.Delta renameCapabilityDelta) {
            this.renameCapabilityDelta = renameCapabilityDelta;
            return this;
        }

        public UsableCapability.Delta getUsableCapabilityDelta() {
            return usableCapabilityDelta;
        }

        public Builder setUsableCapabilityDelta(UsableCapability.Delta usableCapabilityDelta) {
            this.usableCapabilityDelta = usableCapabilityDelta;
            return this;
        }

        public LockingCapability.Delta getLockingCapabilityDelta() {
            return lockingCapabilityDelta;
        }

        public Builder setLockingCapabilityDelta(LockingCapability.Delta lockingCapabilityDelta) {
            this.lockingCapabilityDelta = lockingCapabilityDelta;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        public ItemEffectSource build() {
            return new ItemEffectSource(this);
        }

    }

    public static Builder getItemEffectBuilder(String name) {
        return new Builder(name);
    }

    protected ItemEffectSource(Builder builder) {
        super(builder);
        this.gameEventProcessorCapabilityDelta = builder.getGameEventProcessorCapabilityDelta();
        this.interactableCapabilityDelta = builder.getInteractableCapabilityDelta();
        this.renameCapabilityDelta = builder.getRenameCapabilityDelta();
        this.usableCapabilityDelta = builder.getUsableCapabilityDelta();
        this.lockingCapabilityDelta = builder.getLockingCapabilityDelta();
    }

    public GameEventProcessorCapability.Delta getGameEventProcessorCapabilityDelta() {
        return gameEventProcessorCapabilityDelta;
    }

    public InteractableCapability.Delta getInteractableCapabilityDelta() {
        return interactableCapabilityDelta;
    }

    public RenameCapability.Delta getRenameCapabilityDelta() {
        return renameCapabilityDelta;
    }

    public UsableCapability.Delta getUsableCapabilityDelta() {
        return usableCapabilityDelta;
    }

    public LockingCapability.Delta getLockingCapabilityDelta() {
        return lockingCapabilityDelta;
    }

    @Override
    public boolean isOffensive() {
        return false; // until we can enchant items with additional CreatureEffects
    }

    @Override
    public int aiScore() {
        int score = 0;

        if (this.lockingCapabilityDelta != null) {
            score++;
            switch (this.lockingCapabilityDelta) {
            case LOCK:
                break;
            case NOOP:
                break;
            case TOGGLE:
                break;
            case UNLOCK:
                score++;
                break;
            default:
                break;

            }
        }
        if (gameEventProcessorCapabilityDelta != null) {
            score++;
            switch (this.gameEventProcessorCapabilityDelta) {
            case ARM:
                break;
            case DISARM:
                score++;
                break;
            case NOOP:
                break;
            case TOGGLE:
                break;
            default:
                break;

            }
        }
        if (interactableCapabilityDelta != null) {
            score++;
            switch (this.interactableCapabilityDelta) {
            case INCREMENT:
                break;
            case RESET_COUNT:
                score++;
                break;
            default:
                break;

            }
        }
        if (renameCapabilityDelta != null) {
            score++;
            if (renameCapabilityDelta.getNameToAdd() != null) {
                score++;
            }
        }
        if (usableCapabilityDelta != null) {
            score++;
            switch (this.usableCapabilityDelta) {
            case INCREMENT:
                break;
            case RESET_COUNT:
                score++;
                break;
            default:
                break;

            }
        }
        return score;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(gameEventProcessorCapabilityDelta, interactableCapabilityDelta,
                renameCapabilityDelta, usableCapabilityDelta, lockingCapabilityDelta);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof ItemEffectSource))
            return false;
        ItemEffectSource other = (ItemEffectSource) obj;
        return gameEventProcessorCapabilityDelta == other.gameEventProcessorCapabilityDelta
                && interactableCapabilityDelta == other.interactableCapabilityDelta
                && Objects.equals(renameCapabilityDelta, other.renameCapabilityDelta)
                && usableCapabilityDelta == other.usableCapabilityDelta
                && lockingCapabilityDelta == other.lockingCapabilityDelta;
    }

    @Override
    public String toString() {
        StringBuilder builder2 = new StringBuilder();
        builder2.append("ItemEffectSource [gameEventProcessorCapabilityDelta=")
                .append(gameEventProcessorCapabilityDelta).append(", name=").append(name)
                .append(", interactableCapabilityDelta=").append(interactableCapabilityDelta).append(", persistence=")
                .append(persistence).append(", resistance=").append(resistance).append(", renameCapabilityDelta=")
                .append(renameCapabilityDelta).append(", description=").append(description)
                .append(", usableCapabilityDelta=").append(usableCapabilityDelta).append(", lockingCapabilityDelta=")
                .append(lockingCapabilityDelta).append("]");
        return builder2.toString();
    }

}
