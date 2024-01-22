package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.game.creature.DungeonMaster.DungeonMasterBuildInfo;
import com.lhf.game.creature.IMonster.IMonsterBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INPCBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INonPlayerCharacterBuildInfo;
import com.lhf.game.creature.Player.PlayerBuildInfo;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.intelligence.AIRunner;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.messages.CommandChainHandler;
import com.lhf.server.client.user.User;

public class CreatureFactory implements ICreatureBuildInfoVisitor {
    private final transient Logger logger = Logger.getLogger(this.getClass().getName());
    private final CreaturePartitionSetVisitor builtCreatures;
    private final transient CommandChainHandler successor;
    private final transient StatblockManager statblockManager;
    private final transient ConversationManager conversationManager;
    private final transient Function<INonPlayerCharacterBuildInfo, BasicAI> brainProducer;
    private final boolean fallbackNoConversation;
    private final boolean fallbackDefaultStatblock;

    public CreatureFactory() {
        this.builtCreatures = new CreaturePartitionSetVisitor();
        this.successor = null;
        this.statblockManager = null;
        this.conversationManager = null;
        this.brainProducer = (buildInfo) -> NonPlayerCharacter.defaultAIRunner
                .produceAI(buildInfo.getAiHandlersAsArray());
        this.fallbackDefaultStatblock = true;
        this.fallbackNoConversation = true;
    }

    public static CreatureFactory withAIRunner(CommandChainHandler successor, AIRunner aiRunner) {
        final Function<INonPlayerCharacterBuildInfo, BasicAI> brainProducer = aiRunner != null
                ? (buildInfo) -> aiRunner.produceAI(buildInfo.getAiHandlersAsArray())
                : (buildInfo) -> NonPlayerCharacter.defaultAIRunner.produceAI(buildInfo.getAiHandlersAsArray());
        return new CreatureFactory(successor, null, null, brainProducer, true, true);
    }

    public static CreatureFactory withBrainProducer(CommandChainHandler successor,
            Function<INonPlayerCharacterBuildInfo, BasicAI> brainProducer) {
        return new CreatureFactory(successor, null, null, brainProducer, true, true);
    }

    public static CreatureFactory fromAIRunner(CommandChainHandler successor, StatblockManager statblockManager,
            ConversationManager conversationManager, AIRunner aiRunner, boolean fallbackNoConversation,
            boolean fallbackDefaultStatblock) {
        return new CreatureFactory(successor, statblockManager, conversationManager,
                aiRunner != null ? (buildInfo) -> aiRunner.produceAI(buildInfo.getAiHandlersAsArray()) : null,
                fallbackNoConversation, fallbackDefaultStatblock);
    }

    public CreatureFactory(CommandChainHandler successor, StatblockManager statblockManager,
            ConversationManager conversationManager, Function<INonPlayerCharacterBuildInfo, BasicAI> brainProducer,
            boolean fallbackNoConversation, boolean fallbackDefaultStatblock) {
        this.builtCreatures = new CreaturePartitionSetVisitor();
        this.successor = successor;
        this.statblockManager = statblockManager;
        this.conversationManager = conversationManager;
        this.brainProducer = brainProducer != null ? brainProducer
                : (buildInfo) -> NonPlayerCharacter.defaultAIRunner.produceAI(buildInfo.getAiHandlersAsArray());
        this.fallbackNoConversation = fallbackNoConversation;
        this.fallbackDefaultStatblock = fallbackDefaultStatblock;
    }

    public CreaturePartitionSetVisitor getBuiltCreatures() {
        return this.builtCreatures;
    }

    @Override
    public void visit(PlayerBuildInfo buildInfo) {
        final User user = buildInfo.getUser();
        if (user == null) {
            final String error = String.format("%s must declare a User to create a Player!", buildInfo);
            this.logger.log(Level.SEVERE, error);
            throw new IllegalStateException(error);
        }
        Statblock currStatBlock = buildInfo.getStatblock();
        if (currStatBlock == null) {
            VocationName currVocation = buildInfo.getVocation();
            if (currVocation == null) {
                throw new IllegalStateException(String.format(
                        "'%s' Must have a statblock or a Vocation from which to define the statblock!", buildInfo));
            }
            currStatBlock = currVocation.createNewDefaultStatblock("Player").build();
            buildInfo.setStatblock(currStatBlock);
        }
        Player built = new Player(buildInfo, user, this.successor, buildInfo.getStatblock());
        this.builtCreatures.visit(built);
    }

    private Statblock loadStatblock(INonPlayerCharacterBuildInfo buildInfo) {
        Statblock block = buildInfo.getStatblock();
        final String blockName = buildInfo.getStatblockName();
        if (block == null && blockName != null && this.statblockManager != null) {
            try {
                block = buildInfo.loadStatblock(statblockManager);
            } catch (FileNotFoundException e) {
                if (fallbackDefaultStatblock) {
                    this.logger.log(Level.WARNING,
                            String.format("Problem loading statblock %s for %s, falling back to default statblock",
                                    blockName, buildInfo));
                    block = buildInfo.loadBlankStatblock();
                } else {
                    final String errorDescription = String.format("Problem loading statblock %s for %s", blockName,
                            buildInfo);
                    this.logger.log(Level.SEVERE, errorDescription + ", raising error");
                    throw new IllegalStateException(errorDescription, e);
                }
            }
        } else if (block == null) {
            block = buildInfo.loadBlankStatblock();
        }
        return block;
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

    @Override
    public void visit(IMonsterBuildInfo buildInfo) {
        final Statblock block = this.loadStatblock(buildInfo);
        final ConversationTree tree = this.loadConversationTree(buildInfo);
        Monster monster = new Monster(buildInfo, this.brainProducer.apply(buildInfo), successor,
                block, tree);
        this.builtCreatures.visit(monster);
    }

    @Override
    public void visit(INPCBuildInfo buildInfo) {
        final Statblock block = this.loadStatblock(buildInfo);
        final ConversationTree tree = this.loadConversationTree(buildInfo);
        NonPlayerCharacter npc = new NonPlayerCharacter(buildInfo,
                this.brainProducer.apply(buildInfo), successor, block, tree);
        this.builtCreatures.visit(npc);
    }

    @Override
    public void visit(DungeonMasterBuildInfo buildInfo) {
        final Statblock block = this.loadStatblock(buildInfo);
        final ConversationTree tree = this.loadConversationTree(buildInfo);
        DungeonMaster dm = new DungeonMaster(buildInfo, this.brainProducer.apply(buildInfo),
                successor, block, tree);
        this.builtCreatures.visit(dm);
    }

    @Override
    public void visit(CreatureBuildInfo buildInfo) {
        throw new UnsupportedOperationException(String.format("Cannot build a raw Creature %s", buildInfo));
    }

}
