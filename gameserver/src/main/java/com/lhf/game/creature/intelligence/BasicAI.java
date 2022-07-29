package com.lhf.game.creature.intelligence;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import com.lhf.Taggable;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.creature.conversation.ConversationTreeNodeResult;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.CommandBuilder;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.OutMessageType;
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
    private Map<OutMessageType, AIChunk> handlers;

    public BasicAI(NonPlayerCharacter npc) {
        super();
        this.npc = npc;
        this.setSuccessor(npc);
        this.SetOut(new DoNothingSendStrategy());
        this.handlers = new TreeMap<>();
        this.initBasicHandlers();
    }

    private void initBasicHandlers() {
        if (this.handlers == null) {
            this.handlers = new TreeMap<>();
        }
        this.handlers.put(OutMessageType.ATTACK_DAMAGE, (BasicAI bai, OutMessage msg) -> {
            if (msg.getOutType().equals(OutMessageType.ATTACK_DAMAGE) && bai.getNpc().isInBattle()) {
                AttackDamageMessage attackDamageMessage = (AttackDamageMessage) msg;
                if (attackDamageMessage.getVictim() != bai.getNpc()) {
                    return;
                }
                bai.setLastAttacker(attackDamageMessage.getAttacker());
            }
        });
        this.handlers.put(OutMessageType.BAD_TARGET_SELECTED, (BasicAI bai, OutMessage msg) -> {
            if (msg.getOutType().equals(OutMessageType.BAD_TARGET_SELECTED)) {
                bai.setLastAttacker(null); // the message means that this was invalid anyway
                BadTargetSelectedMessage btsm = (BadTargetSelectedMessage) msg;
                ArrayList<Creature> creaturesFound = new ArrayList<>();
                for (Taggable target : btsm.getPossibleTargets()) {
                    if (target instanceof Creature) {
                        Creature possTarget = (Creature) target;
                        creaturesFound.add(possTarget);
                        if (possTarget.getFaction() == CreatureFaction.RENEGADE) {
                            bai.setLastAttacker(possTarget);
                            break;
                        }
                    }
                }
                if (bai.getLastAttacker() == null) {
                    for (Creature c : creaturesFound) {
                        if (c.getFaction() != bai.getNpc().getFaction()) {
                            bai.setLastAttacker(c);
                            break;
                        }
                    }
                }
                bai.useTurn(false);
            }
        });
        this.handlers.put(OutMessageType.BATTLE_TURN, (BasicAI bai, OutMessage msg) -> {
            if (msg.getOutType().equals(OutMessageType.BATTLE_TURN)) {
                BattleTurnMessage btm = (BattleTurnMessage) msg;
                if (!btm.isWasted() && btm.isAddressTurner() && btm.isYesTurn()) {
                    bai.useTurn(false);
                }
                return;
            }
        });
        this.handlers.put(OutMessageType.SPEAKING, (BasicAI bai, OutMessage msg) -> {
            if (msg.getOutType().equals(OutMessageType.SPEAKING)) {
                SpeakingMessage sm = (SpeakingMessage) msg;
                if (!sm.getShouting() && sm.getHearer() != null && sm.getHearer() instanceof NonPlayerCharacter) {
                    if (sm.getSayer() instanceof Creature && bai.getNpc().getConvoTree() != null) {
                        Creature sayer = (Creature) sm.getSayer();
                        ConversationTreeNodeResult result = bai.getNpc().getConvoTree().listen(sayer, sm.getMessage());
                        if (result != null && result.getBody() != null) {
                            SayMessage say = (SayMessage) CommandBuilder.fromCommand(CommandMessage.SAY,
                                    "say \"" + result.getBody() + "\" to " + sayer.getName());
                            CommandBuilder.addDirect(say, result.getBody());
                            CommandBuilder.addIndirect(say, "to", sayer.getName());
                            bai.handleMessage(null, say);
                        }
                    }
                }
            }
        });
    }

    public void addHandler(OutMessageType type, AIChunk chunk) {
        this.handlers.put(type, chunk);
    }

    protected void useTurn(boolean spell) {
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

    @Override
    public synchronized void sendMsg(OutMessage msg) {
        super.sendMsg(msg);
        AIChunk ai = this.handlers.get(msg.getOutType());
        if (ai != null) {
            ai.handle(this, msg);
        }
    }

    public NonPlayerCharacter getNpc() {
        return npc;
    }

    public Creature getLastAttacker() {
        return lastAttacker;
    }

    public void setLastAttacker(Creature lastAttacker) {
        this.lastAttacker = lastAttacker;
    }

}
