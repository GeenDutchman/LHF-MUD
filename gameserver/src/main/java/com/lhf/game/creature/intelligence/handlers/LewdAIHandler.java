package com.lhf.game.creature.intelligence.handlers;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Level;

import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.messages.Command;
import com.lhf.messages.CommandBuilder;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.LewdEvent;
import com.lhf.messages.events.LewdEvent.LewdOutMessageType;

public class LewdAIHandler extends AIHandler {
    private Set<String> partners;
    private boolean partnersOnly;
    private boolean stayInAfter;

    public LewdAIHandler() {
        super(GameEventType.LEWD);
        this.partners = new HashSet<>();
        this.partnersOnly = false;
        this.stayInAfter = false;
    }

    public LewdAIHandler(Set<String> partners) {
        super(GameEventType.LEWD);
        this.partners = partners;
        this.partnersOnly = true;
        this.stayInAfter = false;
    }

    public LewdAIHandler setPartnersOnly() {
        this.partnersOnly = true;
        return this;
    }

    public LewdAIHandler setLewdAnyone() {
        this.partnersOnly = false;
        return this;
    }

    public LewdAIHandler setGetUpAfter() {
        this.stayInAfter = false;
        return this;
    }

    public LewdAIHandler setStayInAfter() {
        this.stayInAfter = true;
        return this;
    }

    public LewdAIHandler addPartner(String partnerName) {
        if (partnerName != null) {
            this.partners.add(partnerName);
        }
        return this;
    }

    public void handleProposal(BasicAI bai, LewdEvent lom) {
        if (!lom.getParticipants().containsKey(bai.getNpc())) {
            return; // none of our business
        }
        if (lom.getCreature().equals(bai.getNpc())) {
            return; // we're the one who sent it
        }
        StringJoiner sj = new StringJoiner(", ");
        for (ICreature partyCreature : lom.getParticipants().keySet()) {
            if (this.partnersOnly) {
                if (!this.partners.contains(partyCreature.getName()) // if they aren't our partner
                        && partyCreature != bai.getNpc()) { // or us
                    this.logger.log(Level.WARNING, String.format("%s proposed to lewd %s, but they aren't a parnter!",
                            lom.getCreature().getName(), bai.toString()));
                    Command cmd = CommandBuilder.parse("pass"); // then don't!
                    bai.handleChain(null, cmd);
                    return;
                }
            }
            sj.add(partyCreature.getName());
        }
        this.logger.log(Level.FINEST, String.format("%s agreed to lewd %s", bai.toString(), sj.toString()));
        Command cmd = CommandBuilder.parse("lewd " + sj.toString());
        bai.handleChain(null, cmd);
    }

    public void handleDunnit(BasicAI bai, LewdEvent lom) {
        if (!lom.getParticipants().containsKey(bai.getNpc())) {
            return; // none of our business
        }

        if (!this.stayInAfter) {
            Command cmd = CommandBuilder.parse("GO UP");
            bai.handleChain(null, cmd);
        }
    }

    @Override
    public void handle(BasicAI bai, GameEvent event) {
        if (GameEventType.LEWD.equals(event.getEventType())) {
            LewdEvent lom = (LewdEvent) event;
            this.logger.log(Level.FINEST, () -> String.format("%s: processing \"%s\"", bai.toString(), lom.print()));
            if (lom.getSubType() == LewdOutMessageType.PROPOSED) {
                this.handleProposal(bai, lom);
            }
            if (lom.getSubType() == LewdOutMessageType.DUNNIT) {
                this.handleDunnit(bai, lom);
            }
        }
    }

}
