package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.EffectPersistence.Ticker;
import com.lhf.game.creature.DungeonMaster.DungeonMasterBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INPCBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INonPlayerCharacterBuildInfo;
import com.lhf.game.creature.Player.PlayerBuildInfo;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.messages.CommandChainHandler;
import com.lhf.server.client.user.User;

public class CreatureFactory implements ICreatureBuildInfoVisitor {
    private final transient Logger logger = Logger.getLogger(this.getClass().getName());
    private final CreaturePartitionSetVisitor builtCreatures;
    private final transient CommandChainHandler successor;
    private final transient ConversationManager conversationManager;
    private final transient Function<INonPlayerCharacterBuildInfo, BasicAI> brainProducer;
    private final boolean fallbackNoConversation;

    public CreatureFactory() {
        this.builtCreatures = new CreaturePartitionSetVisitor();
        this.successor = null;
        this.conversationManager = null;
        this.brainProducer = (buildInfo) -> NonPlayerCharacter.defaultAIRunner.produceAI();
        this.fallbackNoConversation = true;
    }

    public static CreatureFactory withAIRunner(CommandChainHandler successor, AIRunner aiRunner) {
        final Function<INonPlayerCharacterBuildInfo, BasicAI> brainProducer = aiRunner != null
                ? (buildInfo) -> aiRunner.produceAI()
                : (buildInfo) -> NonPlayerCharacter.defaultAIRunner.produceAI();
        return new CreatureFactory(successor, null, brainProducer, true);
    }

    public static CreatureFactory withBrainProducer(CommandChainHandler successor,
            Function<INonPlayerCharacterBuildInfo, BasicAI> brainProducer) {
        return new CreatureFactory(successor, null, brainProducer, true);
    }

    public static CreatureFactory fromAIRunner(CommandChainHandler successor, ConversationManager conversationManager,
            AIRunner aiRunner, boolean fallbackNoConversation) {
        return new CreatureFactory(successor, conversationManager,
                aiRunner != null ? (buildInfo) -> aiRunner.produceAI() : null, fallbackNoConversation);
    }

    public CreatureFactory(CommandChainHandler successor, ConversationManager conversationManager,
            Function<INonPlayerCharacterBuildInfo, BasicAI> brainProducer, boolean fallbackNoConversation) {
        this.builtCreatures = new CreaturePartitionSetVisitor();
        this.successor = successor;
        this.conversationManager = conversationManager;
        this.brainProducer = brainProducer != null ? brainProducer
                : (buildInfo) -> NonPlayerCharacter.defaultAIRunner.produceAI();
        this.fallbackNoConversation = fallbackNoConversation;
    }

    public CreaturePartitionSetVisitor getBuiltCreatures() {
        return this.builtCreatures;
    }

    public Player buildPlayer(PlayerBuildInfo buildInfo) {
        if (buildInfo == null) {
            this.logger.log(Level.INFO, "Null PlayerBuildInfo provided, skipping...");
            return null;
        }
        final User user = buildInfo.getUser();
        if (user == null) {
            final String error = String.format("%s must declare a User to create a Player!", buildInfo);
            this.logger.log(Level.SEVERE, error);
            throw new IllegalStateException(error);
        }
        Player built = new Player(buildInfo, user, this.successor);
        this.builtCreatures.visit(built);
        return built;
    }

    @Override
    public void visit(PlayerBuildInfo buildInfo) {
        this.buildPlayer(buildInfo);
    }

    private ConversationTree loadConversationTree(INonPlayerCharacterBuildInfo buildInfo) {
        ConversationTree tree = buildInfo.getConversationTree();
        final String treeName = buildInfo.getConversationFileName();
        if (tree == null && treeName != null && this.conversationManager != null) {
            try {
                tree = buildInfo.loadConversationTree(conversationManager);
            } catch (FileNotFoundException e) {
                if (fallbackNoConversation) {
                    this.logger.log(Level.WARNING,
                            String.format(
                                    "Problem loading conversation tree %s for %s, falling back to default statblock",
                                    treeName, buildInfo));
                    tree = null;
                } else {
                    final String errorDescription = String.format("Problem loading conversation tree %s for %s",
                            treeName, buildInfo);
                    this.logger.log(Level.SEVERE, errorDescription + ", raising error");
                    throw new IllegalStateException(errorDescription, e);
                }
            }
        }
        return tree;
    }

    public Monster buildMonster(MonsterBuildInfo buildInfo) {
        if (buildInfo == null) {
            this.logger.log(Level.INFO, "Null MonsterBuildInfo provided, skipping...");
            return null;
        }
        final ConversationTree tree = this.loadConversationTree(buildInfo);
        final BasicAI brain = this.brainProducer.apply(buildInfo);
        Monster monster = new Monster(buildInfo, brain, successor, tree);
        brain.setNPC(monster);
        this.builtCreatures.visit(monster);
        return monster;
    }

    public SummonedMonster summonMonster(MonsterBuildInfo buildInfo, ICreature summoner, Ticker timeLeft) {
        if (buildInfo == null) {
            this.logger.log(Level.INFO, "Null MonsterBuildInfo provided, skipping...");
            return null;
        }
        final ConversationTree tree = this.loadConversationTree(buildInfo);
        final BasicAI brain = this.brainProducer.apply(buildInfo);
        Monster monster = new Monster(buildInfo, brain, successor, tree);
        brain.setNPC(monster);
        SummonedMonster summonedMonster = new SummonedMonster(monster, buildInfo.getSummonState(), summoner, timeLeft);
        this.builtCreatures.visit(summonedMonster);
        return summonedMonster;
    }

    @Override
    public void visit(MonsterBuildInfo buildInfo) {
        this.buildMonster(buildInfo);
    }

    public NonPlayerCharacter buildNPC(INPCBuildInfo buildInfo) {
        if (buildInfo == null) {
            this.logger.log(Level.INFO, "Null INPCBuildInfo provided, skipping...");
            return null;
        }
        final ConversationTree tree = this.loadConversationTree(buildInfo);
        final BasicAI brain = this.brainProducer.apply(buildInfo);
        NonPlayerCharacter npc = new NonPlayerCharacter(buildInfo, brain, successor, tree);
        brain.setNPC(npc);
        this.builtCreatures.visit(npc);
        return npc;
    }

    public SummonedNPC summonNPC(INPCBuildInfo buildInfo, ICreature summoner, Ticker timeLeft) {
        if (buildInfo == null) {
            this.logger.log(Level.INFO, "Null INPCBuildInfo provided, skipping...");
            return null;
        }
        final ConversationTree tree = this.loadConversationTree(buildInfo);
        final BasicAI brain = this.brainProducer.apply(buildInfo);
        NonPlayerCharacter npc = new NonPlayerCharacter(buildInfo, brain, successor, tree);
        brain.setNPC(npc);
        SummonedNPC summonedNPC = new SummonedNPC(npc, buildInfo.getSummonState(), summoner, timeLeft);
        this.builtCreatures.visit(summonedNPC);
        return summonedNPC;
    }

    @Override
    public void visit(INPCBuildInfo buildInfo) {
        this.buildNPC(buildInfo);
    }

    public DungeonMaster buildDungeonMaster(DungeonMasterBuildInfo buildInfo) {
        if (buildInfo == null) {
            this.logger.log(Level.INFO, "Null DungeonMasterBuildInfo provided, skipping...");
            return null;
        }
        final ConversationTree tree = this.loadConversationTree(buildInfo);
        final BasicAI brain = this.brainProducer.apply(buildInfo);
        DungeonMaster dm = new DungeonMaster(buildInfo, brain, successor, tree);
        brain.setNPC(dm);
        this.builtCreatures.visit(dm);
        return dm;
    }

    @Override
    public void visit(DungeonMasterBuildInfo buildInfo) {
        this.buildDungeonMaster(buildInfo);
    }

    @Override
    public void visit(CreatureBuildInfo buildInfo) {
        throw new UnsupportedOperationException(String.format("Cannot build a raw Creature %s", buildInfo));
    }

}
