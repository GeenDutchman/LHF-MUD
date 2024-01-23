package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.creature.vocation.DMVocation;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.messages.CommandChainHandler;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.interfaces.NotNull;

public class DungeonMaster extends NonPlayerCharacter {

    public static class DungeonMasterBuildInfo implements INonPlayerCharacterBuildInfo {
        private final String className;
        private final INPCBuildInfo iNPCBuilder;
        protected final CreatureBuilderID id;

        protected DungeonMasterBuildInfo() {
            this.className = this.getClass().getName();
            this.iNPCBuilder = new INPCBuildInfo().setFaction(CreatureFaction.NPC).setVocation(new DMVocation());
            this.id = new CreatureBuilderID();
        }

        public DungeonMasterBuildInfo(DungeonMasterBuildInfo other) {
            this.className = other.getClassName();
            this.iNPCBuilder = new INPCBuildInfo(other.iNPCBuilder);
            this.id = new CreatureBuilderID();
        }

        public static DungeonMasterBuildInfo getInstance() {
            return new DungeonMasterBuildInfo();
        }

        @Override
        public String getClassName() {
            return this.className;
        }

        @Override
        public CreatureBuilderID getCreatureBuilderID() {
            return this.id;
        }

        public String getConversationFileName() {
            return iNPCBuilder.getConversationFileName();
        }

        public DungeonMasterBuildInfo setConversationFileName(String conversationFileName) {
            iNPCBuilder.setConversationFileName(conversationFileName);
            return this;
        }

        public DungeonMasterBuildInfo setConversationTree(ConversationTree tree) {
            iNPCBuilder.setConversationTree(tree);
            return this;
        }

        public ConversationTree loadConversationTree(ConversationManager conversationManager)
                throws FileNotFoundException {
            return iNPCBuilder.loadConversationTree(conversationManager);
        }

        public ConversationTree getConversationTree() {
            return iNPCBuilder.getConversationTree();
        }

        public DungeonMasterBuildInfo useDefaultConversation() {
            this.setConversationFileName(IMonster.defaultConvoTreeName);
            return this;
        }

        public DungeonMasterBuildInfo addAIHandler(AIHandler handler) {
            iNPCBuilder.addAIHandler(handler);
            return this;
        }

        public List<AIHandler> getAIHandlers() {
            return iNPCBuilder.getAIHandlers();
        }

        public final AIHandler[] getAiHandlersAsArray() {
            return iNPCBuilder.getAiHandlersAsArray();
        }

        public DungeonMasterBuildInfo clearAIHandlers() {
            iNPCBuilder.clearAIHandlers();
            return this;
        }

        public EnumSet<SummonData> getSummonState() {
            return iNPCBuilder.getSummonState();
        }

        public DungeonMasterBuildInfo resetSummonState() {
            iNPCBuilder.resetSummonState();
            return this;
        }

        public DungeonMasterBuildInfo addSummonState(SummonData data) {
            iNPCBuilder.addSummonState(data);
            return this;
        }

        public DungeonMasterBuildInfo setSummonStates(Set<SummonData> summonData) {
            iNPCBuilder.setSummonStates(summonData);
            return this;
        }

        public String getLeaderName() {
            return iNPCBuilder.getLeaderName();
        }

        public DungeonMasterBuildInfo setLeaderName(String leaderName) {
            iNPCBuilder.setLeaderName(leaderName);
            return this;
        }

        public DungeonMasterBuildInfo setName(String name) {
            iNPCBuilder.setName(name);
            return this;
        }

        public String getName() {
            return iNPCBuilder.getName();
        }

        public DungeonMasterBuildInfo setFaction(CreatureFaction faction) {
            iNPCBuilder.setFaction(faction);
            return this;
        }

        public CreatureFaction getFaction() {
            return iNPCBuilder.getFaction();
        }

        public DungeonMasterBuildInfo setVocation(Vocation vocation) {
            iNPCBuilder.setVocation(vocation);
            return this;
        }

        public CreatureBuildInfo setVocation(VocationName vocationName) {
            return iNPCBuilder.setVocation(vocationName);
        }

        public DungeonMasterBuildInfo setVocationLevel(int level) {
            iNPCBuilder.setVocationLevel(level);
            return this;
        }

        public VocationName getVocation() {
            return iNPCBuilder.getVocation();
        }

        public Integer getVocationLevel() {
            return iNPCBuilder.getVocationLevel();
        }

        public DungeonMasterBuildInfo setStatblock(Statblock statblock) {
            iNPCBuilder.setStatblock(statblock);
            return this;
        }

        public DungeonMasterBuildInfo setStatblockName(String statblockName) {
            iNPCBuilder.setStatblockName(statblockName);
            return this;
        }

        public String getStatblockName() {
            return iNPCBuilder.getStatblockName();
        }

        public Statblock loadStatblock(StatblockManager statblockManager) throws FileNotFoundException {
            return iNPCBuilder.loadStatblock(statblockManager);
        }

        @Override
        public Statblock loadBlankStatblock() {
            return iNPCBuilder.loadBlankStatblock();
        }

        public DungeonMasterBuildInfo useBlankStatblock() {
            iNPCBuilder.useBlankStatblock();
            return this;
        }

        public Statblock getStatblock() {
            return iNPCBuilder.getStatblock();
        }

        public DungeonMasterBuildInfo setCorpse(Corpse corpse) {
            iNPCBuilder.setCorpse(corpse);
            return this;
        }

        public Corpse getCorpse() {
            return iNPCBuilder.getCorpse();
        }

        @Override
        public void acceptBuildInfoVisitor(ICreatureBuildInfoVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("IMonsterBuildInfo [iNPCBuilder=").append(iNPCBuilder).append(", id=").append(id)
                    .append("]");
            return builder.toString();
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof DungeonMasterBuildInfo))
                return false;
            DungeonMasterBuildInfo other = (DungeonMasterBuildInfo) obj;
            return Objects.equals(id, other.id);
        }
    }

    public DungeonMaster(DungeonMasterBuildInfo builder,
            @NotNull CommandInvoker controller, CommandChainHandler successor,
            @NotNull Statblock statblock, ConversationTree conversationTree) {
        super(builder, controller, successor, statblock, conversationTree);
    }

    public static DungeonMasterBuildInfo getDMBuilder() {
        return new DungeonMasterBuildInfo();
    }

    @Override
    public void acceptCreatureVisitor(CreatureVisitor visitor) {
        visitor.visit(this);
    }

}
