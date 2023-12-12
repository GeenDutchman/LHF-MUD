package com.lhf.game.creature.intelligence.handlers;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.logging.Level;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.messages.Command;
import com.lhf.messages.CommandBuilder;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.out.LewdOutMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.LewdOutMessage.LewdOutMessageType;

public class LewdAIHandler extends AIHandler {
    private Set<Creature> partners;
    private boolean partnersOnly;
    private boolean stayInAfter;

    public LewdAIHandler() {
        super(OutMessageType.LEWD);
        this.partners = new HashSet<>();
        this.partnersOnly = false;
        this.stayInAfter = false;
    }

    public LewdAIHandler(Set<Creature> partners) {
        super(OutMessageType.LEWD);
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

    public LewdAIHandler addPartner(Creature partner) {
        if (partner != null) {
            this.partners.add(partner);
        }
        return this;
    }

    public void handleProposal(BasicAI bai, LewdOutMessage lom) {
        if (!lom.getParticipants().containsKey(bai.getNpc())) {
            return; // none of our business
        }
        if (lom.getCreature().equals(bai.getNpc())) {
            return; // we're the one who sent it
        }
        StringJoiner sj = new StringJoiner(", ");
        for (Creature partyCreature : lom.getParticipants().keySet()) {
            if (this.partnersOnly) {
                if (!this.partners.contains(partyCreature) // if they aren't our partner
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

    public void handleDunnit(BasicAI bai, LewdOutMessage lom) {
        if (!lom.getParticipants().containsKey(bai.getNpc())) {
            return; // none of our business
        }

        if (!this.stayInAfter) {
            Command cmd = CommandBuilder.parse("GO UP");
            bai.handleChain(null, cmd);
        }
    }

    @Override
    public void handle(BasicAI bai, OutMessage msg) {
        if (OutMessageType.LEWD.equals(msg.getOutType())) {
            LewdOutMessage lom = (LewdOutMessage) msg;
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
