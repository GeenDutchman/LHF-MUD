package com.lhf.game.creature.intelligence.handlers;

import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.messages.Command;
import com.lhf.messages.CommandBuilder;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.out.LewdOutMessage;
import com.lhf.messages.out.OutMessage;

public class LewdAIHandler extends AIHandler {
    private Set<Creature> partners;
    private boolean partnersOnly;

    public LewdAIHandler() {
        super(OutMessageType.LEWD);
        this.partners = new HashSet<>();
        this.partnersOnly = false;
    }

    public LewdAIHandler(Set<Creature> partners) {
        super(OutMessageType.LEWD);
        this.partners = partners;
        this.partnersOnly = true;
    }

    public LewdAIHandler setPartnersOnly() {
        this.partnersOnly = true;
        return this;
    }

    public LewdAIHandler setLewdAnyone() {
        this.partnersOnly = false;
        return this;
    }

    public LewdAIHandler addPartner(Creature partner) {
        if (partner != null) {
            this.partners.add(partner);
        }
        return this;
    }

    @Override
    public void handle(BasicAI bai, OutMessage msg) {
        if (OutMessageType.LEWD.equals(msg.getOutType())) {
            LewdOutMessage lom = (LewdOutMessage) msg;
            StringJoiner sj = new StringJoiner(", ");
            for (Creature partyCreature : lom.getParticipants().keySet()) {
                if (this.partnersOnly) {
                    if (!this.partners.contains(partyCreature)) { // if they aren't our partner, then don't
                        Command cmd = CommandBuilder.parse("pass");
                        bai.handleMessage(null, cmd);
                        return;
                    }
                }
                sj.add(partyCreature.getName());
            }
            Command cmd = CommandBuilder.parse("lewd " + sj.toString());
            bai.handleMessage(null, cmd);
        }
    }

}