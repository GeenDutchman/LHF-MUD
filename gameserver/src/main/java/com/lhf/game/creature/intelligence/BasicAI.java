package com.lhf.game.creature.intelligence;

import java.util.ArrayList;

import com.lhf.Taggable;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.creature.conversation.ConversationTreeNodeResult;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.CommandBuilder;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.in.AttackMessage;
import com.lhf.messages.in.CastMessage;
import com.lhf.messages.in.SayMessage;
import com.lhf.messages.out.AttackDamageMessage;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.BattleTurnMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.SpeakingMessage;
import com.lhf.server.client.Client;
import com.lhf.server.client.DoNothingSendStrategy;

public class BasicAI extends Client {
    private NonPlayerCharacter npc;
    private Creature lastAttacker;

    public BasicAI(NonPlayerCharacter npc) {
        super();
        this.npc = npc;
        this.setSuccessor(npc);
        this.SetOut(new DoNothingSendStrategy());
    }

    private void useTurn(boolean spell) {
        if (spell || this.lastAttacker == null) {
            CastMessage cMessage = (CastMessage) CommandBuilder.fromCommand(CommandMessage.CAST, "turnwaster!!");
            CommandBuilder.addDirect(cMessage, "turnwaster!!");
            super.handleMessage(null, cMessage);
        } else {
            AttackMessage aMessage = (AttackMessage) CommandBuilder.fromCommand(CommandMessage.ATTACK,
                    this.lastAttacker.getName());
            CommandBuilder.addDirect(aMessage, this.lastAttacker.getName());
            super.handleMessage(null, aMessage);
        }
    }

    private void dumb(OutMessage msg) {
        if (!this.npc.isAlive()) {
            return;
        }
        if (msg instanceof AttackDamageMessage && this.npc.isInBattle()) {
            AttackDamageMessage attackDamageMessage = (AttackDamageMessage) msg;
            if (attackDamageMessage.getVictim() != this.npc) {
                return;
            }
            this.lastAttacker = attackDamageMessage.getAttacker();
            return;
        }
        if (msg instanceof BadTargetSelectedMessage && this.npc.isInBattle()) {
            this.lastAttacker = null; // the message means that this was invalid anyway
            BadTargetSelectedMessage btsm = (BadTargetSelectedMessage) msg;
            ArrayList<Creature> creaturesFound = new ArrayList<>();
            for (Taggable target : btsm.getPossibleTargets()) {
                if (target instanceof Creature) {
                    Creature possTarget = (Creature) target;
                    creaturesFound.add(possTarget);
                    if (possTarget.getFaction() == CreatureFaction.RENEGADE) {
                        this.lastAttacker = possTarget;
                        break;
                    }
                }
            }
            if (this.lastAttacker == null) {
                for (Creature c : creaturesFound) {
                    if (c.getFaction() != this.npc.getFaction()) {
                        this.lastAttacker = c;
                        break;
                    }
                }
            }
            this.useTurn(false);
            return;
        }
        if (msg instanceof BattleTurnMessage) {
            BattleTurnMessage btm = (BattleTurnMessage) msg;
            if (!btm.isWasted() && btm.isAddressTurner() && btm.isYesTurn()) {
                this.useTurn(false);
            }
            return;
        }
        if (msg instanceof SpeakingMessage) {
            SpeakingMessage sm = (SpeakingMessage) msg;
            if (!sm.getShouting() && sm.getHearer() != null && sm.getHearer() instanceof NonPlayerCharacter) {
                if (sm.getSayer() instanceof Creature && this.npc.getConvoTree() != null) {
                    Creature sayer = (Creature) sm.getSayer();
                    ConversationTreeNodeResult result = this.npc.getConvoTree().listen(sayer, sm.getMessage());
                    if (result != null && result.getBody() != null) {
                        SayMessage say = (SayMessage) CommandBuilder.fromCommand(CommandMessage.SAY,
                                "say \"" + result.getBody() + "\" to " + sayer.getName());
                        CommandBuilder.addDirect(say, result.getBody());
                        CommandBuilder.addIndirect(say, "to", sayer.getName());
                        super.handleMessage(null, say);
                    }
                }
            }
            return;
        }
    }

    @Override
    public synchronized void sendMsg(OutMessage msg) {
        super.sendMsg(msg);
        this.dumb(msg);
    }

    public NonPlayerCharacter getNpc() {
        return npc;
    }

    public Creature getLastAttacker() {
        return lastAttacker;
    }

}
