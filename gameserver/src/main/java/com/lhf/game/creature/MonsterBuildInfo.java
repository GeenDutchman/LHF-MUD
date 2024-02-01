package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.lhf.game.creature.INonPlayerCharacter.INPCBuildInfo;
import com.lhf.game.creature.INonPlayerCharacter.INonPlayerCharacterBuildInfo;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
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

public final class MonsterBuildInfo implements INonPlayerCharacterBuildInfo {
    private final String className;
    private final INPCBuildInfo iNPCBuilder;
    protected final CreatureBuilderID id;
    private static transient long serialNumber = 0;

    protected MonsterBuildInfo() {
        this.className = this.getClass().getName();
        this.iNPCBuilder = new INPCBuildInfo().setFaction(CreatureFaction.MONSTER);
        this.id = new CreatureBuilderID();
    }

    public MonsterBuildInfo(MonsterBuildInfo other) {
        this.className = other.getClassName();
        this.iNPCBuilder = new INPCBuildInfo(other.iNPCBuilder);
        this.id = new CreatureBuilderID();
    }

    public static MonsterBuildInfo getInstance() {
        return new MonsterBuildInfo();
    }

    @Override
    public String getClassName() {
        return this.className;
    }

    public long getSerialNumber() {
        MonsterBuildInfo.serialNumber++;
        return MonsterBuildInfo.serialNumber;
    }

    @Override
    public CreatureBuilderID getCreatureBuilderID() {
        return this.id;
    }

    public String getConversationFileName() {
        return iNPCBuilder.getConversationFileName();
    }

    public MonsterBuildInfo setConversationFileName(String conversationFileName) {
        iNPCBuilder.setConversationFileName(conversationFileName);
        return this;
    }

    public MonsterBuildInfo setConversationTree(ConversationTree tree) {
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

    public MonsterBuildInfo useDefaultConversation() {
        this.setConversationFileName(IMonster.defaultConvoTreeName);
        return this;
    }

    public MonsterBuildInfo addAIHandler(AIHandler handler) {
        iNPCBuilder.addAIHandler(handler);
        return this;
    }

    public List<AIHandler> getAIHandlers() {
        return iNPCBuilder.getAIHandlers();
    }

    public final AIHandler[] getAiHandlersAsArray() {
        return iNPCBuilder.getAiHandlersAsArray();
    }

    public MonsterBuildInfo clearAIHandlers() {
        iNPCBuilder.clearAIHandlers();
        return this;
    }

    public EnumSet<SummonData> getSummonState() {
        return iNPCBuilder.getSummonState();
    }

    public MonsterBuildInfo resetSummonState() {
        iNPCBuilder.resetSummonState();
        return this;
    }

    public MonsterBuildInfo addSummonState(SummonData data) {
        iNPCBuilder.addSummonState(data);
        return this;
    }

    public MonsterBuildInfo setSummonStates(Set<SummonData> summonData) {
        iNPCBuilder.setSummonStates(summonData);
        return this;
    }

    public String getLeaderName() {
        return iNPCBuilder.getLeaderName();
    }

    public MonsterBuildInfo setLeaderName(String leaderName) {
        iNPCBuilder.setLeaderName(leaderName);
        return this;
    }

    public MonsterBuildInfo setCreatureRace(String race) {
        iNPCBuilder.setCreatureRace(race);
        return this;
    }

    public String getCreatureRace() {
        return iNPCBuilder.getCreatureRace();
    }

    public MonsterBuildInfo defaultStats() {
        iNPCBuilder.defaultStats();
        return this;
    }

    public MonsterBuildInfo setAttributeBlock(AttributeBlock block) {
        iNPCBuilder.setAttributeBlock(block);
        return this;
    }

    public MonsterBuildInfo setAttributeBlock(Integer strength, Integer dexterity, Integer constitution,
            Integer intelligence,
            Integer wisdom, Integer charisma) {
        iNPCBuilder.setAttributeBlock(strength, dexterity, constitution, intelligence, wisdom, charisma);
        return this;
    }

    public MonsterBuildInfo resetFlavorReactions() {
        iNPCBuilder.resetFlavorReactions();
        return this;
    }

    public MonsterBuildInfo addFlavorReaction(DamgeFlavorReaction sort, DamageFlavor flavor) {
        iNPCBuilder.addFlavorReaction(sort, flavor);
        return this;
    }

    public AttributeBlock getAttributeBlock() {
        return iNPCBuilder.getAttributeBlock();
    }

    public MonsterBuildInfo setStats(Map<Stats, Integer> newStats) {
        iNPCBuilder.setStats(newStats);
        return this;
    }

    public MonsterBuildInfo setStat(Stats stat, int value) {
        iNPCBuilder.setStat(stat, value);
        return this;
    }

    public EnumMap<Stats, Integer> getStats() {
        return iNPCBuilder.getStats();
    }

    public MonsterBuildInfo setProficiencies(EnumSet<EquipmentTypes> types) {
        iNPCBuilder.setProficiencies(types);
        return this;
    }

    public MonsterBuildInfo addProficiency(EquipmentTypes type) {
        iNPCBuilder.addProficiency(type);
        return this;
    }

    public EnumSet<EquipmentTypes> getProficiencies() {
        return iNPCBuilder.getProficiencies();
    }

    public MonsterBuildInfo setInventory(Inventory other) {
        iNPCBuilder.setInventory(other);
        return this;
    }

    public MonsterBuildInfo addItem(Takeable item) {
        iNPCBuilder.addItem(item);
        return this;
    }

    public Inventory getInventory() {
        return iNPCBuilder.getInventory();
    }

    public MonsterBuildInfo setEquipmentSlots(Map<EquipmentSlots, Equipable> slots) {
        iNPCBuilder.setEquipmentSlots(slots);
        return this;
    }

    public EnumMap<EquipmentSlots, Equipable> getEquipmentSlots() {
        return iNPCBuilder.getEquipmentSlots();
    }

    public MonsterBuildInfo setCreatureEffects(Set<CreatureEffect> others) {
        iNPCBuilder.setCreatureEffects(others);
        return this;
    }

    public Set<CreatureEffect> getCreatureEffects() {
        return iNPCBuilder.getCreatureEffects();
    }

    public MonsterBuildInfo defaultFlavorReactions() {
        iNPCBuilder.defaultFlavorReactions();
        return this;
    }

    public MonsterBuildInfo setDamageFlavorReactions(EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> other) {
        iNPCBuilder.setDamageFlavorReactions(other);
        return this;
    }

    public EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> getDamageFlavorReactions() {
        return iNPCBuilder.getDamageFlavorReactions();
    }

    public MonsterBuildInfo setName(String name) {
        iNPCBuilder.setName(name);
        return this;
    }

    public String getName() {
        return iNPCBuilder.getName();
    }

    public MonsterBuildInfo setFaction(CreatureFaction faction) {
        iNPCBuilder.setFaction(faction);
        return this;
    }

    public CreatureFaction getFaction() {
        return iNPCBuilder.getFaction();
    }

    public MonsterBuildInfo setVocation(Vocation vocation) {
        iNPCBuilder.setVocation(vocation);
        return this;
    }

    public MonsterBuildInfo setVocation(VocationName vocationName) {
        iNPCBuilder.setVocation(vocationName);
        return this;
    }

    public MonsterBuildInfo setVocationLevel(int level) {
        iNPCBuilder.setVocationLevel(level);
        return this;
    }

    public VocationName getVocation() {
        return iNPCBuilder.getVocation();
    }

    public Integer getVocationLevel() {
        return iNPCBuilder.getVocationLevel();
    }

    public MonsterBuildInfo setCorpse(Corpse corpse) {
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
        if (!(obj instanceof MonsterBuildInfo))
            return false;
        MonsterBuildInfo other = (MonsterBuildInfo) obj;
        return Objects.equals(id, other.id);
    }

}