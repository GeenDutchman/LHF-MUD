package com.lhf.game;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.Taggable;
import com.lhf.TaggedExaminable;
import com.lhf.game.EffectPersistence.Ticker;
import com.lhf.game.creature.ICreature;
import com.lhf.messages.ClientMessenger;
import com.lhf.messages.ITickEvent;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.SeeEvent;
import com.lhf.server.client.ClientID;

public abstract class EntityEffect implements TaggedExaminable, Comparable<EntityEffect>, ClientMessenger {

    protected final EntityEffectSource source;
    private final ClientID clientID;
    private final static Logger logger = Logger.getLogger(EntityEffect.class.getName());
    protected ICreature creatureResponsible;
    protected Taggable generatedBy;
    protected Ticker ticker;

    public EntityEffect(EntityEffectSource source, ICreature creatureResponsible, Taggable generatedBy) {
        this.source = source;
        this.clientID = new ClientID();
        this.creatureResponsible = creatureResponsible;
        this.generatedBy = generatedBy;
        this.ticker = null;
    }

    @Override
    public ClientID getClientID() {
        return this.clientID;
    }

    public ICreature creatureResponsible() {
        return this.creatureResponsible;
    }

    public Taggable getGeneratedBy() {
        return this.generatedBy;
    }

    public boolean isOffensive() {
        return this.source.isOffensive();
    }

    public EffectPersistence getPersistence() {
        return this.source.getPersistence();
    }

    public EffectResistance getResistance() {
        return this.source.getResistance();
    }

    public Ticker getTicker() {
        if (this.ticker == null) {
            this.ticker = this.source.getPersistence().getTicker();
        }
        return this.ticker;
    }

    public int tick(TickType type) {
        return this.getTicker().tick(type);
    }

    @Override
    public Consumer<GameEvent> getAcceptHook() {
        return event -> {
            if (event == null) {
                return;
            }
            if (event instanceof ITickEvent tickEvent) {
                this.tick(tickEvent.getTickType());
            }
        };
    }

    @Override
    public int compareTo(EntityEffect o) {
        if (this.equals(o)) {
            return 0;
        }
        int namecompare = this.getGeneratedBy().getColorTaggedName().compareTo(o.getGeneratedBy().getColorTaggedName());
        if (namecompare != 0) {
            return namecompare;
        }
        int persistenceCompare = this.getPersistence().compareTo(o.getPersistence());
        if (persistenceCompare != 0) {
            return persistenceCompare;
        }
        return this.getClientID().compareTo(o.getClientID());
    }

    @Override
    public String getName() {
        return this.source.getName();
    }

    @Override
    public String printDescription() {
        return this.source.description + "\nIt will last "
                + (this.ticker == null ? this.getPersistence().toString() : this.ticker.toString());
    }

    @Override
    public SeeEvent produceMessage() {
        return this.source.produceMessage();
    }

    @Override
    public String getColorTaggedName() {
        return this.source.getColorTaggedName();
    }

    @Override
    public String getEndTag() {
        return this.source.getEndTag();
    }

    @Override
    public String getStartTag() {
        return this.source.getStartTag();
    }

    @Override
    public String toString() {
        return this.produceMessage().toString();
    }

    @Override
    public void log(Level logLevel, String logMessage) {
        EntityEffect.logger.log(logLevel,
                String.format("%s by %s: %s", this.getName(), this.creatureResponsible, logMessage));
    }

    @Override
    public void log(Level logLevel, Supplier<String> logMessageSupplier) {
        Supplier<String> actualSupplier = () -> {
            return String.format("%s by %s: %s", this.getName(), this.creatureResponsible,
                    logMessageSupplier != null ? logMessageSupplier.get() : "no message");
        };
        EntityEffect.logger.log(logLevel, actualSupplier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, clientID);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof EntityEffect))
            return false;
        EntityEffect other = (EntityEffect) obj;
        return Objects.equals(source, other.source) && Objects.equals(clientID, other.clientID);
    }

}
