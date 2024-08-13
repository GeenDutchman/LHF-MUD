package com.lhf.messages.events;

import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.game.item.IItem;
import com.lhf.game.item.ItemEffect;
import com.lhf.messages.GameEventType;

public class ItemAffectedEvent extends GameEvent {
    private final IItem itemEffected;
    private final ItemEffect effect;

    public static class Builder extends GameEvent.Builder<Builder> {
        private IItem itemEffected;
        private ItemEffect effect;

        protected Builder() {
            super(GameEventType.ITEM_AFFECTED);
        }

        public IItem getItemEffected() {
            return itemEffected;
        }

        public Builder setItemEffected(IItem itemEffected) {
            this.itemEffected = itemEffected;
            return this;
        }

        public ItemEffect getEffect() {
            return effect;
        }

        public Builder setEffect(ItemEffect effect) {
            this.effect = effect;
            return this;
        }

        @Override
        public Builder getThis() {
            return this;
        }

        @Override
        public ItemAffectedEvent Build() {
            return new ItemAffectedEvent(this);
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public ItemAffectedEvent(Builder builder) {
        super(builder);
        this.itemEffected = builder.getItemEffected();
        this.effect = builder.getEffect();
    }

    public IItem getItemEffected() {
        return itemEffected;
    }

    public ItemEffect getEffect() {
        return effect;
    }

    @Override
    public void buildOutput(RichOutputBuilder builder) {
        if (builder == null) {
            return;
        }
        if (this.effect.creatureResponsible() != null) {
            builder.appendTaggable(this.effect.creatureResponsible()).appendString("used");
            builder.appendTaggable(this.effect.getGeneratedBy()).appendString("on");
        } else {
            builder.appendTaggable(this.effect.getGeneratedBy()).appendString("affected");
        }

        if (this.itemEffected != null) {
            builder.appendString("the item").appendTaggable(this.itemEffected, " ", "!");
        } else {
            builder.appendString("an item!");
        }

    }
}
