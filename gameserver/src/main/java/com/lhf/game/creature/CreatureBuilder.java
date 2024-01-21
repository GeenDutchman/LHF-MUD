package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.Objects;

import com.lhf.game.ItemContainer;
import com.lhf.game.creature.ICreature.ICreatureBuilder;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.statblock.StatblockManager;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.item.concrete.Corpse;

/**
 * Builder pattern root for Creature
 */
public final class CreatureBuilder implements ICreatureBuilder {

    private final String className;
    protected final transient CreatureBuilderID id;
    protected String name;
    protected CreatureFaction faction;
    protected VocationName vocation;
    protected Integer vocationLevel;
    protected String statblockName;
    protected Statblock statblock;
    protected Corpse corpse;

    protected CreatureBuilder() {
        this.className = this.getClass().getName();
        this.id = new CreatureBuilderID();
        this.name = null;
        this.faction = null;
        this.vocation = null;
        this.vocationLevel = null;
        this.statblockName = null;
        this.statblock = null;
        this.corpse = null;
    }

    public CreatureBuilder(CreatureBuilder other) {
        this.className = other.getClassName();
        this.id = new CreatureBuilderID();
        this.name = new String(other.name);
        this.faction = other.faction;
        this.vocation = other.vocation;
        this.vocationLevel = other.vocationLevel != null ? other.vocationLevel.intValue() : null;
        this.statblockName = new String(other.statblockName);
        this.statblock = this.statblock != null ? new Statblock(other.statblock) : null;
        this.corpse = null;
        if (other.corpse != null) {
            this.corpse = other.corpse.makeCopy();
            ItemContainer.transfer(other.corpse, this.corpse, null, true);
        }
    }

    @Override
    public final String getClassName() {
        return this.className;
    }

    public final CreatureBuilderID getCreatureBuilderID() {
        return this.id;
    }

    public CreatureBuilder setName(String name) {
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
            this.name = NameGenerator.Generate(null);
        }
        return this.name;
    }

    public CreatureBuilder setFaction(CreatureFaction faction) {
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
            this.faction = CreatureFaction.RENEGADE;
        }
        return this.faction;
    }

    public CreatureBuilder setVocation(Vocation vocation) {
        if (vocation == null) {
            this.vocation = null;
            this.vocationLevel = null;
        } else {
            this.vocation = vocation.getVocationName();
            this.vocationLevel = vocation.getLevel();
        }
        return this;
    }

    public CreatureBuilder setVocation(VocationName vocationName) {
        this.vocation = vocationName;
        return this;
    }

    public CreatureBuilder setVocationLevel(int level) {
        this.vocationLevel = level;
        return this;
    }

    public VocationName getVocation() {
        return this.vocation;
    }

    public Integer getVocationLevel() {
        return this.vocationLevel;
    }

    public CreatureBuilder setStatblock(Statblock statblock) {
        this.statblock = statblock;
        if (this.statblock != null) {
            this.statblockName = this.statblock.getCreatureRace();
        }
        return this;
    }

    public CreatureBuilder setStatblockName(String statblockName) {
        this.statblockName = statblockName;
        if (this.statblock != null && !this.statblock.getCreatureRace().equals(statblockName)) {
            this.statblock = null;
        }
        return this;
    }

    public String getStatblockName() {
        return statblockName;
    }

    /**
     * Will lazily generate a {@link com.lhf.game.creature.statblock.Statblock
     * Statblock} if none is provided.
     * <p>
     * If this has a vocationName set, it'll try to use the provided
     * {@link com.lhf.game.creature.statblock.StatblockManager StatblockManager}.
     * Elsewise if this has a {@link com.lhf.game.creature.vocation.Vocation
     * Vocation} set,
     * it will use the default for the Vocation.
     * Otherwise it'll be a plain statblock.
     * 
     * @return
     * @throws FileNotFoundException
     */
    public Statblock loadStatblock(StatblockManager statblockManager) throws FileNotFoundException {
        if (this.statblock == null) {
            String nextname = this.getStatblockName();
            if (nextname != null) {
                this.setStatblock(statblockManager.statblockFromfile(nextname));
            } else if (this.vocation != null) {
                this.setStatblock(this.vocation.createNewDefaultStatblock("creature").build());
            } else {
                this.setStatblock(Statblock.getBuilder().build());
            }
        }

        return this.statblock;
    }

    public CreatureBuilder useBlankStatblock() {
        this.setStatblock(Statblock.getBuilder().build());
        return this;
    }

    public Statblock getStatblock() {
        return this.statblock;
    }

    public CreatureBuilder setCorpse(Corpse corpse) {
        this.corpse = corpse;
        return this;
    }

    public Corpse getCorpse() {
        return this.corpse;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof CreatureBuilder))
            return false;
        CreatureBuilder other = (CreatureBuilder) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
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
        if (this.statblockName != null) {
            sb.append("Statblock similar to: ").append(this.statblockName);
            if (this.statblock != null) {
                sb.append(" (concrete statblock present)");
            }
            sb.append(".\r\n");
        }
        return sb.toString();
    }

}