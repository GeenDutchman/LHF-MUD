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
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.Takeable;
import com.lhf.game.item.concrete.Corpse;

public interface IMonster extends INonPlayerCharacter {
    public static final String defaultConvoTreeName = "non_verbal_default";

    @Override
    public default void restoreFaction() {
        this.setFaction(CreatureFaction.MONSTER);
    }

    public abstract long getMonsterNumber();

    public static final class IMonsterBuildInfo implements INonPlayerCharacterBuildInfo {
        private final String className;
        private final INPCBuildInfo iNPCBuilder;
        protected final CreatureBuilderID id;
        private static transient long serialNumber = 0;

        protected IMonsterBuildInfo() {
            this.className = this.getClass().getName();
            this.iNPCBuilder = new INPCBuildInfo().setFaction(CreatureFaction.MONSTER);
            this.id = new CreatureBuilderID();
        }

        public IMonsterBuildInfo(IMonsterBuildInfo other) {
            this.className = other.getClassName();
            this.iNPCBuilder = new INPCBuildInfo(other.iNPCBuilder);
            this.id = new CreatureBuilderID();
        }

        public static IMonsterBuildInfo getInstance() {
            return new IMonsterBuildInfo();
        }

        @Override
        public String getClassName() {
            return this.className;
        }

        public long getSerialNumber() {
            IMonsterBuildInfo.serialNumber++;
            return IMonsterBuildInfo.serialNumber;
        }

        @Override
        public CreatureBuilderID getCreatureBuilderID() {
            return this.id;
        }

        public String getConversationFileName() {
            return iNPCBuilder.getConversationFileName();
        }

        public IMonsterBuildInfo setConversationFileName(String conversationFileName) {
            iNPCBuilder.setConversationFileName(conversationFileName);
            return this;
        }

        public IMonsterBuildInfo setConversationTree(ConversationTree tree) {
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

        public IMonsterBuildInfo useDefaultConversation() {
            this.setConversationFileName(IMonster.defaultConvoTreeName);
            return this;
        }

        public IMonsterBuildInfo addAIHandler(AIHandler handler) {
            iNPCBuilder.addAIHandler(handler);
            return this;
        }

        public List<AIHandler> getAIHandlers() {
            return iNPCBuilder.getAIHandlers();
        }

        public final AIHandler[] getAiHandlersAsArray() {
            return iNPCBuilder.getAiHandlersAsArray();
        }

        public IMonsterBuildInfo clearAIHandlers() {
            iNPCBuilder.clearAIHandlers();
            return this;
        }

        public EnumSet<SummonData> getSummonState() {
            return iNPCBuilder.getSummonState();
        }

        public IMonsterBuildInfo resetSummonState() {
            iNPCBuilder.resetSummonState();
            return this;
        }

        public IMonsterBuildInfo addSummonState(SummonData data) {
            iNPCBuilder.addSummonState(data);
            return this;
        }

        public IMonsterBuildInfo setSummonStates(Set<SummonData> summonData) {
            iNPCBuilder.setSummonStates(summonData);
            return this;
        }

        public String getLeaderName() {
            return iNPCBuilder.getLeaderName();
        }

        public IMonsterBuildInfo setLeaderName(String leaderName) {
            iNPCBuilder.setLeaderName(leaderName);
            return this;
        }

        public IMonsterBuildInfo setCreatureRace(String race) {
            iNPCBuilder.setCreatureRace(race);
            return this;
        }

        public String getCreatureRace() {
            return iNPCBuilder.getCreatureRace();
        }

        public IMonsterBuildInfo defaultStats() {
            iNPCBuilder.defaultStats();
            return this;
        }

        public IMonsterBuildInfo setAttributeBlock(AttributeBlock block) {
            iNPCBuilder.setAttributeBlock(block);
            return this;
        }

        public AttributeBlock getAttributeBlock() {
            return iNPCBuilder.getAttributeBlock();
        }

        public IMonsterBuildInfo setStats(Map<Stats, Integer> newStats) {
            iNPCBuilder.setStats(newStats);
            return this;
        }

        public IMonsterBuildInfo setStat(Stats stat, int value) {
            iNPCBuilder.setStat(stat, value);
            return this;
        }

        public EnumMap<Stats, Integer> getStats() {
            return iNPCBuilder.getStats();
        }

        public IMonsterBuildInfo setProficiencies(EnumSet<EquipmentTypes> types) {
            iNPCBuilder.setProficiencies(types);
            return this;
        }

        public IMonsterBuildInfo addProficiency(EquipmentTypes type) {
            iNPCBuilder.addProficiency(type);
            return this;
        }

        public EnumSet<EquipmentTypes> getProficiencies() {
            return iNPCBuilder.getProficiencies();
        }

        public IMonsterBuildInfo setInventory(Inventory other) {
            iNPCBuilder.setInventory(other);
            return this;
        }

        public IMonsterBuildInfo addItem(Takeable item) {
            iNPCBuilder.addItem(item);
            return this;
        }

        public Inventory getInventory() {
            return iNPCBuilder.getInventory();
        }

        public IMonsterBuildInfo setEquipmentSlots(Map<EquipmentSlots, Equipable> slots) {
            iNPCBuilder.setEquipmentSlots(slots);
            return this;
        }

        public EnumMap<EquipmentSlots, Equipable> getEquipmentSlots() {
            return iNPCBuilder.getEquipmentSlots();
        }

        public IMonsterBuildInfo defaultFlavorReactions() {
            iNPCBuilder.defaultFlavorReactions();
            return this;
        }

        public IMonsterBuildInfo setDamageFlavorReactions(EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> other) {
            iNPCBuilder.setDamageFlavorReactions(other);
            return this;
        }

        public EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> getDamageFlavorReactions() {
            return iNPCBuilder.getDamageFlavorReactions();
        }

        public IMonsterBuildInfo setName(String name) {
            iNPCBuilder.setName(name);
            return this;
        }

        public String getName() {
            return iNPCBuilder.getName();
        }

        public IMonsterBuildInfo setFaction(CreatureFaction faction) {
            iNPCBuilder.setFaction(faction);
            return this;
        }

        public CreatureFaction getFaction() {
            return iNPCBuilder.getFaction();
        }

        public IMonsterBuildInfo setVocation(Vocation vocation) {
            iNPCBuilder.setVocation(vocation);
            return this;
        }

        public IMonsterBuildInfo setVocation(VocationName vocationName) {
            iNPCBuilder.setVocation(vocationName);
            return this;
        }

        public IMonsterBuildInfo setVocationLevel(int level) {
            iNPCBuilder.setVocationLevel(level);
            return this;
        }

        public VocationName getVocation() {
            return iNPCBuilder.getVocation();
        }

        public Integer getVocationLevel() {
            return iNPCBuilder.getVocationLevel();
        }

        public IMonsterBuildInfo setCorpse(Corpse corpse) {
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
            if (!(obj instanceof IMonsterBuildInfo))
                return false;
            IMonsterBuildInfo other = (IMonsterBuildInfo) obj;
            return Objects.equals(id, other.id);
        }

    }

}
