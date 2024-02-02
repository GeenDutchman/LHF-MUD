package com.lhf.game.creature;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import com.lhf.game.ItemContainer;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.creature.inventory.Inventory;
import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.DamgeFlavorReaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Equipable;
import com.lhf.game.item.Takeable;
import com.lhf.game.item.concrete.Corpse;

/**
 * Builder pattern root for Creature
 */
public final class CreatureBuildInfo implements ICreatureBuildInfo {

    private final String className;
    protected final CreatureBuilderID id;
    private String creatureRace;
    private AttributeBlock attributeBlock;
    private EnumMap<Stats, Integer> stats;
    private EnumSet<EquipmentTypes> proficiencies;
    private Inventory inventory;
    private EnumMap<EquipmentSlots, Equipable> equipmentSlots;
    protected TreeSet<CreatureEffect> effects;
    private EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> damageFlavorReactions;
    protected String name;
    protected CreatureFaction faction;
    protected VocationName vocation;
    protected Integer vocationLevel;
    // protected String statblockName; // TODO: turn this into buildinfoname

    protected CreatureBuildInfo() {
        this.className = this.getClass().getName();
        this.id = new CreatureBuilderID();
        this.creatureRace = defaultRaceName;
        this.attributeBlock = new AttributeBlock();
        this.stats = new EnumMap<>(Stats.class);
        this.defaultStats();
        this.proficiencies = EnumSet.noneOf(EquipmentTypes.class);
        this.inventory = new Inventory();
        this.equipmentSlots = new EnumMap<>(EquipmentSlots.class);
        this.damageFlavorReactions = new EnumMap<>(DamgeFlavorReaction.class);
        this.defaultFlavorReactions();
        this.name = null;
        this.faction = null;
        this.vocation = null;
        this.vocationLevel = null;
        this.effects = new TreeSet<>();
    }

    public CreatureBuildInfo(CreatureBuildInfo other) {
        this.className = other.getClassName();
        this.id = new CreatureBuilderID();
        this.setCreatureRace(other.creatureRace);
        this.setAttributeBlock(other.attributeBlock);
        this.setStats(other.stats);
        this.setProficiencies(other.proficiencies);
        this.setInventory(other.inventory);
        this.setEquipmentSlots(other.equipmentSlots);
        this.setCreatureEffects(other.effects);
        this.setDamageFlavorReactions(other.damageFlavorReactions);
        this.name = other.name != null ? new String(other.name) : null;
        this.faction = other.faction;
        this.vocation = other.vocation;
        this.vocationLevel = other.vocationLevel != null ? other.vocationLevel.intValue() : null;
    }

    @Override
    public final String getClassName() {
        return this.className;
    }

    public final CreatureBuilderID getCreatureBuilderID() {
        return this.id;
    }

    public CreatureBuildInfo setCreatureRace(String race) {
        this.creatureRace = race != null ? new String(race) : defaultRaceName;
        return this;
    }

    @Override
    public String getCreatureRace() {
        return this.creatureRace;
    }

    public CreatureBuildInfo defaultStats() {
        ICreatureBuildInfo.setDefaultStats(this.stats);
        return this;
    }

    public CreatureBuildInfo setAttributeBlock(AttributeBlock block) {
        this.attributeBlock = block != null ? new AttributeBlock(block) : new AttributeBlock();
        return this;
    }

    public CreatureBuildInfo setAttributeBlock(Integer strength, Integer dexterity, Integer constitution,
            Integer intelligence,
            Integer wisdom, Integer charisma) {
        this.attributeBlock = new AttributeBlock(strength, dexterity, constitution, intelligence, wisdom, charisma);
        return this;
    }

    public CreatureBuildInfo resetFlavorReactions() {
        this.damageFlavorReactions = new EnumMap<>(DamgeFlavorReaction.class);
        for (DamgeFlavorReaction reaction : DamgeFlavorReaction.values()) {
            this.damageFlavorReactions.computeIfAbsent(reaction, key -> EnumSet.noneOf(DamageFlavor.class));
        }
        return this;
    }

    public CreatureBuildInfo addFlavorReaction(DamgeFlavorReaction sort, DamageFlavor flavor) {
        if (sort != null && flavor != null) {
            this.damageFlavorReactions.computeIfAbsent(sort, key -> EnumSet.of(flavor)).add(flavor);
        }
        return this;
    }

    @Override
    public AttributeBlock getAttributeBlock() {
        return this.attributeBlock;
    }

    public CreatureBuildInfo setStats(Map<Stats, Integer> newStats) {
        if (newStats == null) {
            this.stats = new EnumMap<>(Stats.class);
            this.defaultStats();
        } else {
            this.stats = new EnumMap<>(newStats);
        }
        return this;
    }

    public CreatureBuildInfo setStat(Stats stat, int value) {
        if (stat != null) {
            this.stats.put(stat, value);
        }
        return this;
    }

    @Override
    public EnumMap<Stats, Integer> getStats() {
        return this.stats;
    }

    public CreatureBuildInfo setProficiencies(EnumSet<EquipmentTypes> types) {
        this.proficiencies = types != null ? EnumSet.copyOf(types)
                : EnumSet.noneOf(EquipmentTypes.class);
        return this;
    }

    public CreatureBuildInfo addProficiency(EquipmentTypes type) {
        if (type != null) {
            this.proficiencies.add(type);
        }
        return this;
    }

    @Override
    public EnumSet<EquipmentTypes> getProficiencies() {
        return this.proficiencies;
    }

    public CreatureBuildInfo setInventory(Inventory other) {
        this.inventory = new Inventory(other); // this is null-safe
        return this;
    }

    public CreatureBuildInfo addItem(Takeable item) {
        if (item != null) {
            this.inventory.addItem(item);
        }
        return this;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    private void onEquipmentChange(Equipable equipable, boolean putOn) {
        List<CreatureEffectSource> sources = equipable.getEquippingEffects();
        if (sources != null) {
            for (final CreatureEffectSource source : sources) {
                if (source == null) {
                    continue;
                }
                Deltas deltas = putOn ? source.getOnApplication() : source.getOnRemoval();
                if (deltas == null) {
                    continue;
                }
                // effects match based on their source
                final CreatureEffect composed = new CreatureEffect(source, null, equipable);
                if ((putOn && this.effects.add(composed)) || (!putOn && this.effects.remove(composed))) {
                    for (Stats stat : deltas.getStatChanges().keySet()) {
                        int amount = deltas.getStatChanges().getOrDefault(stat, 0);
                        this.stats.put(stat, this.stats.getOrDefault(stat, 0) + amount);
                    }
                    for (Attributes delta : deltas.getAttributeScoreChanges().keySet()) {
                        int amount = deltas.getAttributeScoreChanges().getOrDefault(delta, 0);
                        this.attributeBlock.setScoreBonus(delta, this.attributeBlock.getScoreBonus(delta) + amount);
                    }
                    for (Attributes delta : deltas.getAttributeBonusChanges().keySet()) {
                        int amount = deltas.getAttributeBonusChanges().getOrDefault(delta, 0);
                        this.attributeBlock.setModBonus(delta, this.attributeBlock.getModBonus(delta) + amount);
                    }
                }
            }
        }
    }

    public CreatureBuildInfo addEquipment(EquipmentSlots slot, Equipable equipable) {
        if (slot == null || equipable == null) {
            return this;
        }
        final Equipable retrieved = this.equipmentSlots.getOrDefault(slot, null);
        if (retrieved != null) {
            this.onEquipmentChange(retrieved, false);
            this.inventory.addItem(retrieved);
        }
        this.equipmentSlots.put(slot, equipable);
        this.onEquipmentChange(equipable, true);
        return this;
    }

    public CreatureBuildInfo setEquipmentSlots(Map<EquipmentSlots, Equipable> slots) {
        this.equipmentSlots = new EnumMap<>(EquipmentSlots.class);
        if (slots != null) {
            for (final Entry<EquipmentSlots, Equipable> entry : slots.entrySet()) {
                final Equipable equipable = entry.getValue();
                if (equipable == null) {
                    continue;
                }
                Equipable copied = equipable.makeCopy();
                this.equipmentSlots.put(entry.getKey(), copied);
                this.onEquipmentChange(copied, true);
            }
        }
        return this;
    }

    @Override
    public EnumMap<EquipmentSlots, Equipable> getEquipmentSlots() {
        return this.equipmentSlots;
    }

    public CreatureBuildInfo setCreatureEffects(Set<CreatureEffect> others) {
        this.effects = new TreeSet<>();
        if (others != null) {
            for (final CreatureEffect effect : others) {
                this.effects.add(
                        new CreatureEffect(effect.getSource(), effect.creatureResponsible(), effect.getGeneratedBy()));
            }
        }
        return this;
    }

    public Set<CreatureEffect> getCreatureEffects() {
        return this.effects;
    }

    public CreatureBuildInfo defaultFlavorReactions() {
        ICreatureBuildInfo.setDefaultFlavorReactions(this.damageFlavorReactions);
        return this;
    }

    public CreatureBuildInfo setDamageFlavorReactions(
            EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> other) {
        if (other == null) {
            this.damageFlavorReactions = new EnumMap<>(DamgeFlavorReaction.class);
            this.defaultFlavorReactions();
        } else {
            this.damageFlavorReactions = new EnumMap<>(other);
        }
        return this;
    }

    @Override
    public EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> getDamageFlavorReactions() {
        return this.damageFlavorReactions;
    }

    public CreatureBuildInfo setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Will lazily generate a name if none is already set
     * 
     * @return
     */
    public synchronized String getName() {
        if (this.name == null || name.isBlank()) {
            return NameGenerator.Generate(null);
        }
        return this.name;
    }

    @Override
    public String getRawName() {
        return this.name;
    }

    public CreatureBuildInfo setFaction(CreatureFaction faction) {
        this.faction = faction;
        return this;
    }

    /**
     * Will lazily generate a faction (default to
     * {@link com.lhf.game.enums.CreatureFaction#RENEGADE RENEGADE}) if none is
     * already set
     * 
     * @return
     */
    public CreatureFaction getFaction() {
        if (this.faction == null) {
            return CreatureFaction.RENEGADE;
        }
        return this.faction;
    }

    public CreatureBuildInfo setVocation(Vocation vocation) {
        if (vocation == null) {
            this.vocation = null;
            this.vocationLevel = null;
        } else {
            this.vocation = vocation.getVocationName();
            this.vocationLevel = vocation.getLevel();
            this.proficiencies.addAll(this.vocation.defaultProficiencies());
            for (final Takeable item : this.vocation.defaultInventory()) {
                this.inventory.addItem(item);
            }
            this.stats = new EnumMap<>(this.vocation.defaultStats());
            this.attributeBlock = this.vocation.defaultAttributes();
        }
        return this;
    }

    public CreatureBuildInfo setVocation(VocationName vocationName) {
        this.vocation = vocationName;
        return this;
    }

    public CreatureBuildInfo setVocationLevel(int level) {
        this.vocationLevel = level;
        return this;
    }

    public VocationName getVocation() {
        return this.vocation;
    }

    public Integer getVocationLevel() {
        return this.vocationLevel;
    }

    public CreatureBuildInfo setCorpse(Corpse corpse) {
        if (corpse != null) {
            ItemContainer.transfer(corpse, this.inventory, null, true);
        }
        return this;
    }

    @Override
    public void acceptBuildInfoVisitor(ICreatureBuildInfoVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof CreatureBuildInfo))
            return false;
        CreatureBuildInfo other = (CreatureBuildInfo) obj;
        return Objects.equals(id, other.id);
    }

    protected String equipmentSlotsToString() {
        // EquipmentSlots[] slotValues = EquipmentSlots.values();
        StringBuilder stringBuilder = new StringBuilder("{");
        for (EquipmentSlots key : equipmentSlots.keySet()) {
            String item_name = equipmentSlots.get(key).getName();
            if (item_name == null) {
                item_name = "empty";
            }
            stringBuilder.append(key).append("=").append(item_name).append(",");
        }
        stringBuilder.append("}");
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(this.id.toString());
        sb.append(this.getClass().getSimpleName()).append(" with the following characteristics: \r\n");
        sb.append("With race: ").append(this.creatureRace).append("\r\n");
        if (this.name == null) {
            sb.append("Name will be generated.\r\n");
        } else {
            sb.append("Name is:").append(this.name).append(".\r\n");
        }
        if (this.vocation != null) {
            sb.append("Vocation of ").append(this.vocation);
            if (this.vocationLevel != null) {
                sb.append("with level of").append(this.vocationLevel);
            }
            sb.append(".\r\n");
        }
        if (this.attributeBlock != null) {
            sb.append("With attributes like:").append(this.attributeBlock.toString()).append("\r\n");
        }
        if (this.stats != null && !this.stats.isEmpty()) {
            sb.append("And stats like:").append(this.stats).append("\r\n");
        }
        if (this.inventory != null && !this.inventory.isEmpty()) {
            sb.append("Inventory contains:").append(this.inventory);
        }
        sb.append("And equipment:").append(this.equipmentSlotsToString()).append("\r\n");
        return sb.toString();
    }

}