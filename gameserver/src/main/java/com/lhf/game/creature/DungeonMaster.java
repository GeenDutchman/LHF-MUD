package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.vocation.DMVocation;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.DamgeFlavorReaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.Takeable;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.messages.CommandChainHandler;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.interfaces.NotNull;

public class DungeonMaster extends NonPlayerCharacter {

    public static class DungeonMasterBuildInfo implements INonPlayerCharacterBuildInfo {
        private final String className;
        private final INPCBuildInfo iNPCBuilder;
        protected final CreatureBuilderID id = new CreatureBuilderID();

        protected DungeonMasterBuildInfo() {
            this.className = this.getClass().getName();
            this.iNPCBuilder = new INPCBuildInfo().setFaction(CreatureFaction.NPC).setVocation(new DMVocation());
        }

        public DungeonMasterBuildInfo(INonPlayerCharacterBuildInfo other) {
            this();
            this.copyFromINonPlayerCharacterBuildInfo(other);
        }

        public DungeonMasterBuildInfo copyFromICreatureBuildInfo(ICreatureBuildInfo buildInfo) {
            if (buildInfo != null) {
                this.iNPCBuilder.copyFromICreatureBuildInfo(buildInfo).setFaction(CreatureFaction.NPC);
            }
            return this;
        }

        public DungeonMasterBuildInfo copyFromINonPlayerCharacterBuildInfo(INonPlayerCharacterBuildInfo buildInfo) {
            if (buildInfo != null) {
                this.iNPCBuilder.copyFromINonPlayerCharacterBuildInfo(buildInfo).setFaction(CreatureFaction.NPC);
            }
            return this;
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

        public DungeonMasterBuildInfo setCreatureRace(String race) {
            iNPCBuilder.setCreatureRace(race);
            return this;
        }

        public String getCreatureRace() {
            return iNPCBuilder.getCreatureRace();
        }

        public DungeonMasterBuildInfo defaultStats() {
            iNPCBuilder.defaultStats();
            return this;
        }

        public DungeonMasterBuildInfo setAttributeBlock(AttributeBlock block) {
            iNPCBuilder.setAttributeBlock(block);
            return this;
        }

        public DungeonMasterBuildInfo setAttributeBlock(Integer strength, Integer dexterity, Integer constitution,
                Integer intelligence,
                Integer wisdom, Integer charisma) {
            iNPCBuilder.setAttributeBlock(strength, dexterity, constitution, intelligence, wisdom, charisma);
            return this;
        }

        public DungeonMasterBuildInfo resetFlavorReactions() {
            iNPCBuilder.resetFlavorReactions();
            return this;
        }

        public DungeonMasterBuildInfo addFlavorReaction(DamgeFlavorReaction sort, DamageFlavor flavor) {
            iNPCBuilder.addFlavorReaction(sort, flavor);
            return this;
        }

        public AttributeBlock getAttributeBlock() {
            return iNPCBuilder.getAttributeBlock();
        }

        public DungeonMasterBuildInfo setStats(Map<Stats, Integer> newStats) {
            iNPCBuilder.setStats(newStats);
            return this;
        }

        public DungeonMasterBuildInfo setStat(Stats stat, int value) {
            iNPCBuilder.setStat(stat, value);
            return this;
        }

        public EnumMap<Stats, Integer> getStats() {
            return iNPCBuilder.getStats();
        }

        public DungeonMasterBuildInfo setProficiencies(EnumSet<EquipmentTypes> types) {
            iNPCBuilder.setProficiencies(types);
            return this;
        }

        public DungeonMasterBuildInfo addProficiency(EquipmentTypes type) {
            iNPCBuilder.addProficiency(type);
            return this;
        }

        public EnumSet<EquipmentTypes> getProficiencies() {
            return iNPCBuilder.getProficiencies();
        }

        public DungeonMasterBuildInfo setInventory(Inventory other) {
            iNPCBuilder.setInventory(other);
            return this;
        }

        public DungeonMasterBuildInfo addItem(Takeable item) {
            iNPCBuilder.addItem(item);
            return this;
        }

        public Inventory getInventory() {
            return iNPCBuilder.getInventory();
        }

        public DungeonMasterBuildInfo addEquipment(EquipmentSlots slot, Equipable equipable, boolean withoutEffects) {
            iNPCBuilder.addEquipment(slot, equipable, withoutEffects);
            return this;
        }

        public DungeonMasterBuildInfo setEquipmentSlots(Map<EquipmentSlots, Equipable> slots, boolean withoutEffects) {
            iNPCBuilder.setEquipmentSlots(slots, withoutEffects);
            return this;
        }

        public EnumMap<EquipmentSlots, Equipable> getEquipmentSlots() {
            return iNPCBuilder.getEquipmentSlots();
        }

        public DungeonMasterBuildInfo setCreatureEffects(Set<CreatureEffect> others) {
            iNPCBuilder.setCreatureEffects(others);
            return this;
        }

        public Set<CreatureEffect> getCreatureEffects() {
            return iNPCBuilder.getCreatureEffects();
        }

        public DungeonMasterBuildInfo defaultFlavorReactions() {
            iNPCBuilder.defaultFlavorReactions();
            return this;
        }

        public DungeonMasterBuildInfo setDamageFlavorReactions(
                EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> other) {
            iNPCBuilder.setDamageFlavorReactions(other);
            return this;
        }

        public EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> getDamageFlavorReactions() {
            return iNPCBuilder.getDamageFlavorReactions();
        }

        public DungeonMasterBuildInfo setName(String name) {
            iNPCBuilder.setName(name);
            return this;
        }

        public String getName() {
            return iNPCBuilder.getName();
        }

        @Override
        public String getRawName() {
            return iNPCBuilder.getRawName();
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

        public DungeonMasterBuildInfo setVocation(VocationName vocationName) {
            iNPCBuilder.setVocation(vocationName);
            return this;
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

        public DungeonMasterBuildInfo setCorpse(Corpse corpse) {
            iNPCBuilder.setCorpse(corpse);
            return this;
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
            ConversationTree conversationTree) {
        super(builder, controller, successor, conversationTree);
    }

    public static DungeonMasterBuildInfo getDMBuilder() {
        return new DungeonMasterBuildInfo();
    }

    @Override
    public void acceptCreatureVisitor(CreatureVisitor visitor) {
        visitor.visit(this);
    }

}
