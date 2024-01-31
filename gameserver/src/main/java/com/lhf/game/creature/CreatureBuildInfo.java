package com.lhf.game.creature;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import com.lhf.game.ItemContainer;
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
    private EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> damageFlavorReactions;
    protected String name;
    protected CreatureFaction faction;
    protected VocationName vocation;
    protected Integer vocationLevel;
    // protected String statblockName; // TODO: turn this into buildinfoname

    protected CreatureBuildInfo() {
        this.className = this.getClass().getName();
        this.id = new CreatureBuilderID();
        this.creatureRace = "Creature";
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
        this.creatureRace = race != null ? new String(race) : "Creature";
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

    public CreatureBuildInfo setEquipmentSlots(Map<EquipmentSlots, Equipable> slots) {
        this.equipmentSlots = new EnumMap<>(EquipmentSlots.class);
        if (slots != null) {
            for (final Entry<EquipmentSlots, Equipable> entry : slots.entrySet()) {
                final Equipable equipable = entry.getValue();
                if (equipable == null) {
                    continue;
                }
                this.equipmentSlots.put(entry.getKey(), equipable.makeCopy());
            }
        }
        return this;
    }

    @Override
    public EnumMap<EquipmentSlots, Equipable> getEquipmentSlots() {
        return this.equipmentSlots;
    }

    public CreatureBuildInfo defaultFlavorReactions() {
        ICreatureBuildInfo.setDefaultFlavorReactions(this.damageFlavorReactions);
        return this;
    }

    public CreatureBuildInfo setDamageFlavorReactions(EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> other) {
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

    // /**
    // * Will lazily generate a {@link com.lhf.game.creature.statblock.Statblock
    // * Statblock} if none is provided.
    // * <p>
    // * If this has a vocationName set, it'll try to use the provided
    // * {@link com.lhf.game.creature.statblock.StatblockManager StatblockManager}.
    // * Elsewise if this has a {@link com.lhf.game.creature.vocation.Vocation
    // * Vocation} set,
    // * it will use the default for the Vocation.
    // * Otherwise it'll be a plain statblock.
    // *
    // * @return
    // * @throws FileNotFoundException
    // */
    // public Statblock loadStatblock(StatblockManager statblockManager) throws
    // FileNotFoundException {
    // if (this.statblock == null) {
    // String nextname = this.getStatblockName();
    // if (nextname != null) {
    // this.setStatblock(statblockManager.statblockFromfile(nextname));
    // } else if (this.vocation != null) {
    // this.setStatblock(this.vocation.createNewDefaultStatblock("creature").build());
    // } else {
    // this.setStatblock(Statblock.getBuilder().build());
    // }
    // }

    // return this.statblock;
    // }

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
        // TODO: list off additional attributes
        return sb.toString();
    }

}