package com.lhf.game.creature.intelligence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.lhf.Taggable;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.creature.intelligence.handlers.ForgetOnOtherExit;
import com.lhf.game.creature.intelligence.handlers.HandleCreatureAffected;
import com.lhf.game.creature.intelligence.handlers.LewdAIHandler;
import com.lhf.game.creature.intelligence.handlers.SpokenPromptChunk;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.CommandBuilder;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.in.AttackMessage;
import com.lhf.messages.in.PassMessage;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.CreatureAffectedMessage;
import com.lhf.messages.out.BattleTurnMessage;
import com.lhf.messages.out.MissMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.server.client.Client;
import com.lhf.server.client.DoNothingSendStrategy;
import com.lhf.server.interfaces.NotNull;

public class BasicAI extends Client {
    protected NonPlayerCharacter npc;
    protected Creature lastAttacker;
    protected Map<OutMessageType, AIChunk> handlers;
    protected BlockingQueue<OutMessage> queue;
    protected AIRunner runner;

    protected BasicAI(NonPlayerCharacter npc, AIRunner runner) {
        super();
        this.npc = npc;
        this.npc.setController(this);
        this.setSuccessor(npc);
        this.SetOut(new DoNothingSendStrategy());
        this.handlers = new TreeMap<>();
        this.initBasicHandlers();
        this.runner = runner;
        this.queue = new ArrayBlockingQueue<>(32, true);
    }

    public OutMessage peek() {
        return this.queue.peek();
    }

    public OutMessage poll() {
        return this.queue.poll();
    }

    public int size() {
        return this.queue.size();
    }

    public void process(OutMessage msg) {
        if (msg != null) {
            AIChunk ai = this.handlers.get(msg.getOutType());
            if (ai != null) {
                ai.handle(this, msg);
            } else {
                this.logger.warning(() -> String.format("No handler found for %s", msg.getOutType()));
            }
        }
    }

    private void initBasicHandlers() {
        if (this.handlers == null) {
            this.handlers = new TreeMap<>();
        }
        this.handlers.put(OutMessageType.MISS, (BasicAI bai, OutMessage msg) -> {
            if (msg.getOutType().equals(OutMessageType.MISS) && bai.getNpc().isInBattle()) {
                MissMessage missMessage = (MissMessage) msg;
                if (missMessage.getTarget() != bai.getNpc()) {
                    return;
                }
                if (bai.getLastAttacker() == null) {
                    bai.setLastAttacker(missMessage.getAttacker());
                }
            }
        });
        this.handlers.put(OutMessageType.FIGHT_OVER, (BasicAI bai, OutMessage msg) -> {
            if (msg.getOutType().equals(OutMessageType.FIGHT_OVER) && bai.getNpc().isInBattle()) {
                bai.setLastAttacker(null);
            }
        });
        this.handlers.put(OutMessageType.BAD_TARGET_SELECTED, (BasicAI bai, OutMessage msg) -> {
            if (msg.getOutType().equals(OutMessageType.BAD_TARGET_SELECTED)) {
                bai.setLastAttacker(null); // the message means that this was invalid anyway
                BadTargetSelectedMessage btsm = (BadTargetSelectedMessage) msg;
                ArrayList<Creature> creaturesFound = new ArrayList<>();
                if (btsm.getPossibleTargets() != null) {
                    for (Taggable target : btsm.getPossibleTargets()) {
                        if (target instanceof Creature) {
                            creaturesFound.add((Creature) target);
                        }
                    }
                }
                if (bai.getNpc() != null && bai.getNpc().isInBattle()) {
                    bai.selectNextTarget(creaturesFound);
                    bai.basicAttack();
                }
            }
        });
        this.handlers.put(OutMessageType.BATTLE_TURN, (BasicAI bai, OutMessage msg) -> {
            if (msg.getOutType().equals(OutMessageType.BATTLE_TURN)) {
                BattleTurnMessage btm = (BattleTurnMessage) msg;
                if (bai.getNpc() != null && bai.getNpc().equals(btm.getMyTurn()) && !btm.isBroadcast()
                        && btm.isYesTurn()) {
                    bai.basicAttack();
                }
                return;
            }
        });
        this.handlers.put(OutMessageType.CREATURE_AFFECTED, (BasicAI bai, OutMessage msg) -> {
            if (msg.getOutType().equals(OutMessageType.CREATURE_AFFECTED) && bai.getNpc().isInBattle()) {
                CreatureAffectedMessage caMessage = (CreatureAffectedMessage) msg;
                if (caMessage.getAffected() != bai.getNpc()) {
                    return;
                }
                if (caMessage.getEffect().isOffensive()) {
                    bai.setLastAttacker(caMessage.getEffect().creatureResponsible());
                }
            }
        });
        this.addHandler(new SpokenPromptChunk());
        this.addHandler(new ForgetOnOtherExit());
        this.addHandler(new HandleCreatureAffected());
        this.addHandler(new LewdAIHandler().setPartnersOnly());
    }

    protected void selectNextTarget(Collection<Creature> possTargets) {
        if (this.getLastAttacker() != null) {
            return; // no need to reselect if known
        }
        for (Creature creature : possTargets) {
            if (creature == this.getNpc()) {
                continue;
            }
            if (creature.getFaction() == null || CreatureFaction.RENEGADE.equals(creature.getFaction())) {
                this.setLastAttacker(creature);
            }
            if (this.getLastAttacker() == null) {
                if (!CreatureFaction.NPC.equals(creature.getFaction())
                        && this.npc.getFaction().competing(creature.getFaction())) {
                    this.setLastAttacker(creature);
                }
            }
        }
    }

    protected void basicAttack() {
        if (this.getLastAttacker() == null) {
            PassMessage passCommand = (PassMessage) CommandBuilder.fromCommand(CommandMessage.PASS, "pass");
            this.handleMessage(null, passCommand);
            return;
        }
        AttackMessage aMessage = (AttackMessage) CommandBuilder.fromCommand(CommandMessage.ATTACK,
                this.getLastAttacker().getName());
        CommandBuilder.addDirect(aMessage, this.getLastAttacker().getName());
        super.handleMessage(null, aMessage);
    }

    public BasicAI addHandler(OutMessageType type, AIChunk chunk) {
        this.handlers.put(type, chunk);
        return this;
    }

    public BasicAI addHandler(@NotNull AIHandler aiHandler) {
        this.handlers.put(aiHandler.getOutMessageType(), aiHandler);
        return this;
    }

    @Override
    public synchronized void sendMsg(OutMessage msg) {
        super.sendMsg(msg);
        try {
            if (this.runner == null) {
                this.process(msg);
                return;
            }
            if (this.queue.offer(msg, 30, TimeUnit.SECONDS)) {
                this.runner.getAttention(this);
            } else {
                System.err.println("Unable to queue: " + msg.toString());
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public NonPlayerCharacter getNpc() {
        return npc;
    }

    public Creature getLastAttacker() {
        return lastAttacker;
    }

    public void setLastAttacker(Creature lastAttacker) {
        if (lastAttacker != this.npc) {
            this.lastAttacker = lastAttacker;
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BasicAI [npc=").append(npc.getName()).append(", queuesize=").append(queue.size()).append("]");
        return builder.toString();
    }

}
