package com.lhf.game.creature;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

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
import com.lhf.messages.CommandChainHandler;
import com.lhf.server.client.CommandInvoker;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;
import com.lhf.server.interfaces.NotNull;

public class Player extends Creature {
    private User user;

    public static class PlayerBuildInfo implements ICreatureBuildInfo {
        private final String className;
        private transient User user;
        private final CreatureBuildInfo creatureBuilder;
        protected final CreatureBuilderID id = new CreatureBuilderID();

        public PlayerBuildInfo() {
            this.className = this.getClass().getName();
            this.creatureBuilder = new CreatureBuildInfo().setFaction(CreatureFaction.PLAYER);
            this.user = null;
        }

        private PlayerBuildInfo(User user) {
            this();
            this.setUser(user);
        }

        public PlayerBuildInfo(ICreatureBuildInfo buildInfo) {
            this();
            this.copyFromICreatureBuildInfo(buildInfo);
        }

        public static PlayerBuildInfo fromOther(PlayerBuildInfo buildInfo) {
            return new PlayerBuildInfo(buildInfo);
        }

        public static PlayerBuildInfo getInstance(User user) {
            if (user == null) {
                return new PlayerBuildInfo();
            }
            return new PlayerBuildInfo(user);
        }

        public PlayerBuildInfo copyFromICreatureBuildInfo(ICreatureBuildInfo buildInfo) {
            if (buildInfo != null) {
                this.creatureBuilder.copyFrom(buildInfo).setFaction(CreatureFaction.PLAYER);
                if (this.user != null) {
                    this.setName(this.user.getUsername());
                }
            }
            return this;
        }

        public PlayerBuildInfo copyFromPlayerBuildInfo(PlayerBuildInfo buildInfo) {
            if (buildInfo != null) {
                this.copyFromICreatureBuildInfo(buildInfo);
            }
            return this;
        }

        public User getUser() {
            return user;
        }

        @Override
        public String getClassName() {
            return this.className;
        }

        @Override
        public CreatureBuilderID getCreatureBuilderID() {
            return this.id;
        }

        public PlayerBuildInfo setCreatureRace(String race) {
            creatureBuilder.setCreatureRace(race);
            return this;
        }

        public String getCreatureRace() {
            return creatureBuilder.getCreatureRace();
        }

        public PlayerBuildInfo defaultStats() {
            creatureBuilder.defaultStats();
            return this;
        }

        public PlayerBuildInfo setAttributeBlock(AttributeBlock block) {
            creatureBuilder.setAttributeBlock(block);
            return this;
        }

        public PlayerBuildInfo setAttributeBlock(Integer strength, Integer dexterity, Integer constitution,
                Integer intelligence,
                Integer wisdom, Integer charisma) {
            creatureBuilder.setAttributeBlock(strength, dexterity, constitution, intelligence, wisdom, charisma);
            return this;
        }

        public PlayerBuildInfo resetFlavorReactions() {
            creatureBuilder.resetFlavorReactions();
            return this;
        }

        public PlayerBuildInfo addFlavorReaction(DamgeFlavorReaction sort, DamageFlavor flavor) {
            creatureBuilder.addFlavorReaction(sort, flavor);
            return this;
        }

        public AttributeBlock getAttributeBlock() {
            return creatureBuilder.getAttributeBlock();
        }

        public PlayerBuildInfo setStats(Map<Stats, Integer> newStats) {
            creatureBuilder.setStats(newStats);
            return this;
        }

        public PlayerBuildInfo setStat(Stats stat, int value) {
            creatureBuilder.setStat(stat, value);
            return this;
        }

        public EnumMap<Stats, Integer> getStats() {
            return creatureBuilder.getStats();
        }

        public PlayerBuildInfo setProficiencies(EnumSet<EquipmentTypes> types) {
            creatureBuilder.setProficiencies(types);
            return this;
        }

        public PlayerBuildInfo addProficiency(EquipmentTypes type) {
            creatureBuilder.addProficiency(type);
            return this;
        }

        public EnumSet<EquipmentTypes> getProficiencies() {
            return creatureBuilder.getProficiencies();
        }

        public PlayerBuildInfo setInventory(Inventory other) {
            creatureBuilder.setInventory(other);
            return this;
        }

        public PlayerBuildInfo addItem(Takeable item) {
            creatureBuilder.addItem(item);
            return this;
        }

        public Inventory getInventory() {
            return creatureBuilder.getInventory();
        }

        public PlayerBuildInfo addEquipment(EquipmentSlots slot, Equipable equipable) {
            creatureBuilder.addEquipment(slot, equipable);
            return this;
        }

        public PlayerBuildInfo setEquipmentSlots(Map<EquipmentSlots, Equipable> slots) {
            creatureBuilder.setEquipmentSlots(slots);
            return this;
        }

        public EnumMap<EquipmentSlots, Equipable> getEquipmentSlots() {
            return creatureBuilder.getEquipmentSlots();
        }

        public PlayerBuildInfo setCreatureEffects(Set<CreatureEffect> others) {
            creatureBuilder.setCreatureEffects(others);
            return this;
        }

        public Set<CreatureEffect> getCreatureEffects() {
            return creatureBuilder.getCreatureEffects();
        }

        public PlayerBuildInfo applyEffect(CreatureEffect effect) {
            creatureBuilder.applyEffect(effect);
            return this;
        }

        public PlayerBuildInfo repealEffect(String name) {
            creatureBuilder.repealEffect(name);
            return this;
        }

        public PlayerBuildInfo repealEffect(CreatureEffect toRepeal) {
            creatureBuilder.repealEffect(toRepeal);
            return this;
        }

        public PlayerBuildInfo defaultFlavorReactions() {
            creatureBuilder.defaultFlavorReactions();
            return this;
        }

        public PlayerBuildInfo setDamageFlavorReactions(EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> other) {
            creatureBuilder.setDamageFlavorReactions(other);
            return this;
        }

        public EnumMap<DamgeFlavorReaction, EnumSet<DamageFlavor>> getDamageFlavorReactions() {
            return creatureBuilder.getDamageFlavorReactions();
        }

        public PlayerBuildInfo setUser(User user) {
            if (user == null) {
                throw new IllegalArgumentException("Cannot set a null user!");
            }
            this.user = user;
            this.setName(this.user.getUsername());
            return this;
        }

        public PlayerBuildInfo setName(String name) {
            creatureBuilder.setName(name);
            return this;
        }

        public String getName() {
            return creatureBuilder.getName();
        }

        @Override
        public String getRawName() {
            return creatureBuilder.getRawName();
        }

        public PlayerBuildInfo setFaction(CreatureFaction faction) {
            creatureBuilder.setFaction(faction);
            return this;
        }

        public CreatureFaction getFaction() {
            return creatureBuilder.getFaction();
        }

        public PlayerBuildInfo setVocation(Vocation vocation) {
            creatureBuilder.setVocation(vocation);
            return this;
        }

        public PlayerBuildInfo setVocation(VocationName vocationName) {
            creatureBuilder.setVocation(vocationName);
            return this;
        }

        public PlayerBuildInfo setVocationLevel(int level) {
            creatureBuilder.setVocationLevel(level);
            return this;
        }

        public VocationName getVocation() {
            return creatureBuilder.getVocation();
        }

        public Integer getVocationLevel() {
            return creatureBuilder.getVocationLevel();
        }

        public PlayerBuildInfo setCorpse(Corpse corpse) {
            creatureBuilder.setCorpse(corpse);
            return this;
        }

        @Override
        public void acceptBuildInfoVisitor(ICreatureBuildInfoVisitor visitor) {
            visitor.visit(this);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("PlayerBuildInfo [user=").append(user).append(", creatureBuilder=").append(creatureBuilder)
                    .append(", id=").append(id).append("]");
            return builder.toString();
        }

    }

    public Player(PlayerBuildInfo builder,
            @NotNull CommandInvoker controller, CommandChainHandler successor) {
        super(builder, controller, successor);
        this.user = builder.getUser();
        this.user.setSuccessor(this);
    }

    public static PlayerBuildInfo getPlayerBuilder(User user) {
        return new PlayerBuildInfo(user);
    }

    @Override
    public void acceptCreatureVisitor(CreatureVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public void restoreFaction() {
        this.setFaction(CreatureFaction.PLAYER);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Player)) {
            return false;
        }
        Player p = (Player) obj;
        return p.getId().equals(getId());
    }

    public UserID getId() {
        return this.user.getUserID();
    }

    public User getUser() {
        return this.user;
    }

    public CommandInvoker disconnectController() {
        CommandInvoker stored = this.getController();
        this.log(Level.WARNING, () -> String.format("Disconnecting controller %s", stored));
        this.setController(null);
        return stored;
    }

}
