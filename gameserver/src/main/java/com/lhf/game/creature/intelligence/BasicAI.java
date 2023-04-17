package com.lhf.game.creature.intelligence;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.lhf.Taggable;
import com.lhf.game.creature.Creature;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.creature.intelligence.handlers.ForgetOnOtherExit;
import com.lhf.game.creature.intelligence.handlers.HandleCreatureAffected;
import com.lhf.game.creature.intelligence.handlers.LewdAIHandler;
import com.lhf.game.creature.intelligence.handlers.SpokenPromptChunk;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.messages.CommandBuilder;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.in.AttackMessage;
import com.lhf.messages.in.PassMessage;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.BattleTurnMessage;
import com.lhf.messages.out.CreatureAffectedMessage;
import com.lhf.messages.out.FleeMessage;
import com.lhf.messages.out.MissMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.server.client.Client;
import com.lhf.server.client.DoNothingSendStrategy;
import com.lhf.server.interfaces.NotNull;

public class BasicAI extends Client {
    protected NonPlayerCharacter npc;
    protected BattleMemories battleMemories;
    protected Map<OutMessageType, AIChunk> handlers;
    protected BlockingQueue<OutMessage> queue;
    protected AIRunner runner;

    public class BattleMemories {
        public class BattleStats {
            protected final String targetName;
            private CreatureFaction faction;
            private Vocation vocation;
            private int maxDamage;
            private int aggroDamage;
            private int totalDamage;
            private int numDamgages;
            private int healingPerformed;

            public BattleStats(String targetName, CreatureFaction faction, Vocation vocation) {
                this.targetName = targetName;
                this.faction = faction;
                this.vocation = vocation;
            }

            public String getTargetName() {
                return targetName;
            }

            public CreatureFaction getFaction() {
                return faction;
            }

            public Vocation getVocation() {
                return vocation;
            }

            public int getMaxDamage() {
                return maxDamage;
            }

            public int getAggroDamage() {
                return aggroDamage;
            }

            public int getTotalDamage() {
                return totalDamage;
            }

            public int getAverageDamage() {
                return this.getTotalDamage() / this.getNumDamgages();
            }

            public int getNumDamgages() {
                return numDamgages;
            }

            public int getHealingPerformed() {
                return healingPerformed;
            }

            @Override
            public String toString() {
                StringBuilder builder = new StringBuilder();
                builder.append("BattleStats [targetName=").append(targetName).append(", faction=").append(faction)
                        .append(", vocation=").append(vocation).append(", maxDamage=").append(maxDamage)
                        .append(", aggroDamage=").append(aggroDamage).append(", totalDamage=").append(totalDamage)
                        .append(", numDamgages=").append(numDamgages).append(", healingPerformed=")
                        .append(healingPerformed).append("]");
                return builder.toString();
            }

        }

        protected Creature lastAttaker;
        protected int lastAggroDamage;
        protected Map<String, BattleStats> battleStats;

        public BattleMemories() {
            this.lastAggroDamage = 0;
            this.lastAttaker = null;
            this.battleStats = new TreeMap<>();
        }

        public BattleMemories reset() {
            this.battleStats.clear();
            this.lastAggroDamage = 0;
            this.lastAttaker = null;
            return this;
        }

        public BattleMemories update(CreatureAffectedMessage ca) {
            if (ca.getEffect() == null) {
                return this;
            }
            Creature responsible = ca.getEffect().creatureResponsible();
            if (responsible == null) {
                return this;
            }

            int origRoll = ca.getEffect().getDamageResult().getOrigRoll();
            int roll = ca.getEffect().getDamageResult().getRoll();

            if (!this.battleStats.containsKey(responsible.getName())) {
                this.battleStats.put(responsible.getName(),
                        new BattleStats(responsible.getName(), responsible.getFaction(), responsible.getVocation()));
            }
            if (ca.getAffected() == BasicAI.this.npc && ca.getEffect().isOffensive()) {
                if (origRoll >= this.lastAggroDamage) {
                    this.lastAggroDamage = origRoll;
                    this.lastAttaker = responsible;
                }
            }
            BattleStats found = this.battleStats.get(responsible.getName());
            if (found == null) {
                return this;
            }
            found.numDamgages += roll < 0 ? 1 : 0;
            found.totalDamage += roll;
            found.maxDamage = roll > found.maxDamage ? roll : found.maxDamage;
            found.healingPerformed += ca.getEffect().getDamageResult().getByFlavors(EnumSet.of(DamageFlavor.HEALING),
                    false);
            found.aggroDamage = ca.getEffect().getDamageResult().getByFlavors(EnumSet.of(DamageFlavor.AGGRO), true);

            return this;
        }

        public BattleMemories initialize(Iterable<Creature> creatures) {
            for (Creature creature : creatures) {
                if (creature != null) {
                    if (!this.battleStats.containsKey(creature.getName())) {
                        this.battleStats.put(creature.getName(),
                                new BattleStats(creature.getName(), creature.getFaction(), creature.getVocation()));
                    } else {
                        BattleStats found = this.battleStats.get(creature.getName());
                        found.faction = creature.getFaction(); // update just in case
                    }
                }
            }
            return this;
        }

        public BattleMemories remove(String creatureName) {
            this.battleStats.remove(creatureName);
            return this;
        }

        public Map<String, BattleStats> getBattleStats() {
            return Collections.unmodifiableMap(this.battleStats);
        }

        public Creature getLastAttaker() {
            return lastAttaker;
        }

        public int getLastAggroDamage() {
            return lastAggroDamage;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("BattleMemories [lastAttaker=").append(lastAttaker).append(", lastAggroDamage=")
                    .append(lastAggroDamage).append(", battleStats=").append(battleStats).append("]");
            return builder.toString();
        }

    }

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
        this.battleMemories = new BattleMemories();
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
                this.logger.log(Level.WARNING, () -> String.format("%s: No handler found for %s: %s", this.toString(),
                        msg.getOutType(), msg.print()));
            }
        }
    }

    private void initBasicHandlers() {
        if (this.handlers == null) {
            this.handlers = new TreeMap<>();
        }
        this.handlers.put(OutMessageType.FIGHT_OVER, (BasicAI bai, OutMessage msg) -> {
            if (msg.getOutType().equals(OutMessageType.FIGHT_OVER) && bai.getNpc().isInBattle()) {
                bai.resetBattleMemories();
            }
        });
        this.handlers.put(OutMessageType.SEE, (BasicAI bai, OutMessage msg) -> {
            if (msg.getOutType().equals(OutMessageType.SEE) && bai.getNpc().isInBattle()) {
                SeeOutMessage som = (SeeOutMessage) msg;
                bai.getBattleMemories()
                        .initialize(som.getTaggedCategory("Participants").stream()
                                .filter(possCreature -> possCreature instanceof Creature)
                                .map(toCreature -> (Creature) toCreature).collect(Collectors.toUnmodifiableList()));
            }
        });
        this.handlers.put(OutMessageType.FLEE, (BasicAI bai, OutMessage msg) -> {
            if (msg.getOutType().equals(OutMessageType.FLEE)) {
                FleeMessage flee = (FleeMessage) msg;
                if (flee.isFled() && flee.getRunner() != null) {
                    if (flee.getRunner() == bai.getNpc()) {
                        bai.resetBattleMemories();
                    } else {
                        bai.getBattleMemories().remove(flee.getRunner().getName());
                    }
                }
            }
        });
        this.handlers.put(OutMessageType.BAD_TARGET_SELECTED, (BasicAI bai, OutMessage msg) -> {
            if (msg.getOutType().equals(OutMessageType.BAD_TARGET_SELECTED) && bai.getNpc().isInBattle()) {
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

    public BattleMemories getBattleMemories() {
        return this.battleMemories;
    }

    public BattleMemories resetBattleMemories() {
        return this.battleMemories.reset();
    }

    public BattleMemories updateBattleMemories(CreatureAffectedMessage ca) {
        return this.battleMemories.update(ca);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("BasicAI [npc=").append(npc).append(", queuesize=").append(queue.size()).append("]");
        return builder.toString();
    }

}
