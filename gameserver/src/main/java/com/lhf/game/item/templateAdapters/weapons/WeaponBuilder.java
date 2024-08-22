package com.lhf.game.item.templateAdapters.weapons;

import java.util.EnumSet;
import java.util.function.UnaryOperator;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance;
import com.lhf.game.EffectResistance.TargetResistAmount;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.CreatureEffectSource.Deltas;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.EquipableCapability;
import com.lhf.game.item.IItem;
import com.lhf.game.item.Item;
import com.lhf.game.item.WeaponCapability;
import com.lhf.game.item.interfaces.WeaponSubtype;

public final class WeaponBuilder implements IItem.IItemBuilderAdapter {
    private final Item.ItemBuilder inner = new Item.ItemBuilder().setName("Weapon")
            .setDescriptionString("If you choose to use it, most weapons can deal harm to your enemies.")
            .adjustEquipableCapability(WeaponBuilder.basicEquipable(null))
            .adjustWeaponCapability(
                    weaponized -> weaponized != null ? weaponized : new WeaponCapability.Weaponized.WeaponizedBuilder())
            .setTakeable(true);

    private static final UnaryOperator<EquipableCapability.Equipable.EquipableBuilder> basicEquipable(
            EnumSet<EquipmentTypes> types) {
        return equipable -> {
            if (equipable == null) {
                equipable = new EquipableCapability.Equipable.EquipableBuilder(EquipmentSlots.WEAPON);
            }
            equipable.addEquipmentSlot(EquipmentSlots.WEAPON);
            if (types != null) {
                for (final EquipmentTypes equipmentType : types) {
                    equipable.addEquipmentType(equipmentType);
                }
            }
            return equipable;
        };
    }

    public static WeaponBuilder ofRustyDagger() {
        WeaponBuilder builder = new WeaponBuilder();
        builder.inner.setName("Rusty Dagger").setDescriptionString("Rusty Dagger to stab monsters with.");
        builder.inner.adjustWeaponCapability(weaponized -> {
            if (weaponized == null) {
                weaponized = new WeaponCapability.Weaponized.WeaponizedBuilder();
            }
            weaponized.setMainDamageFlavor(DamageFlavor.PIERCING).setWeaponSubtype(WeaponSubtype.PRECISE);
            weaponized.addHitEffectSource(new CreatureEffectSource.Builder("Stab").instantPersistence()
                    .setResistance(new EffectResistance(EnumSet.of(Attributes.DEX), Stats.AC))
                    .setDescription("Daggers stab things")
                    .setOnApplication(new Deltas().addDamage(new DamageDice(1, DieType.FOUR, DamageFlavor.PIERCING))));
            return weaponized;
        });
        builder.inner.adjustEquipableCapability(
                WeaponBuilder.basicEquipable(EnumSet.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.DAGGER)));
        return builder;
    }

    public static WeaponBuilder ofShortsword() {
        WeaponBuilder builder = new WeaponBuilder();
        builder.inner.setName("Shortsword").setDescriptionString(
                "This is a nice, short, shiny sword with a leather grip.  It's a bit simple though...");
        builder.inner.adjustWeaponCapability(weaponized -> {
            if (weaponized == null) {
                weaponized = new WeaponCapability.Weaponized.WeaponizedBuilder();
            }
            weaponized.setMainDamageFlavor(DamageFlavor.SLASHING).setWeaponSubtype(WeaponSubtype.MARTIAL);
            weaponized.addHitEffectSource(new CreatureEffectSource.Builder("Slash").instantPersistence()
                    .setResistance(new EffectResistance(EnumSet.of(Attributes.STR), Stats.AC))
                    .setDescription("Swords cut things")
                    .setOnApplication(new Deltas().addDamage(new DamageDice(1, DieType.SIX, DamageFlavor.SLASHING))));
            return weaponized;
        });
        builder.inner.adjustEquipableCapability(
                WeaponBuilder.basicEquipable(EnumSet.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.SHORTSWORD)));
        return builder;
    }

    public static WeaponBuilder ofLongsword() {
        WeaponBuilder builder = new WeaponBuilder();
        builder.inner.setName("Longsword")
                .setDescriptionString("This is a nice, long, shiny sword.  It's a bit simple though...");
        builder.inner.adjustWeaponCapability(weaponized -> {
            if (weaponized == null) {
                weaponized = new WeaponCapability.Weaponized.WeaponizedBuilder();
            }
            weaponized.setMainDamageFlavor(DamageFlavor.SLASHING).setWeaponSubtype(WeaponSubtype.MARTIAL);
            weaponized.addHitEffectSource(new CreatureEffectSource.Builder("Slash").instantPersistence()
                    .setResistance(new EffectResistance(EnumSet.of(Attributes.STR), Stats.AC))
                    .setDescription("Swords cut things")
                    .setOnApplication(new Deltas().addDamage(new DamageDice(1, DieType.EIGHT, DamageFlavor.SLASHING))));
            return weaponized;
        });
        builder.inner.adjustEquipableCapability(
                WeaponBuilder.basicEquipable(EnumSet.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.LONGSWORD)));
        return builder;
    }

    public static WeaponBuilder ofBossclub() {
        WeaponBuilder builder = new WeaponBuilder();
        builder.inner.setName("Boss Club")
                .setDescriptionString("This is a large club, it seems a bit rusty... wait that is not rust...");
        builder.inner.adjustWeaponCapability(weaponized -> {
            if (weaponized == null) {
                weaponized = new WeaponCapability.Weaponized.WeaponizedBuilder();
            }
            weaponized.setMainDamageFlavor(DamageFlavor.BLUDGEONING).setWeaponSubtype(WeaponSubtype.MARTIAL);
            weaponized.addHitEffectSource(new CreatureEffectSource.Builder("Bash").instantPersistence()
                    .setResistance(new EffectResistance(EnumSet.of(Attributes.STR), Stats.AC))
                    .setDescription("Club it like a boss.").setOnApplication(
                            new Deltas().addDamage(new DamageDice(2, DieType.EIGHT, DamageFlavor.BLUDGEONING))));
            return weaponized;
        });
        builder.inner.adjustEquipableCapability(
                WeaponBuilder.basicEquipable(EnumSet.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.CLUB)));
        return builder;
    }

    public static WeaponBuilder ofWhimsystick() {
        WeaponBuilder builder = new WeaponBuilder();
        builder.inner.setName("Whimsystick")
                .setDescriptionString("This isn't quite a quarterstaff, but also not a club...it is hard to tell. "
                        + "But what you can tell is it seems to have a laughing aura around it, like it doesn't "
                        + "care about what it does to other people...it's a whimsystick.");
        builder.inner.adjustWeaponCapability(weaponized -> {
            if (weaponized == null) {
                weaponized = new WeaponCapability.Weaponized.WeaponizedBuilder();
            }
            weaponized.setMainDamageFlavor(DamageFlavor.SLASHING).setWeaponSubtype(WeaponSubtype.MARTIAL);
            weaponized.addHitEffectSource(new CreatureEffectSource.Builder("Bonk").instantPersistence()
                    .setResistance(new EffectResistance(EnumSet.of(Attributes.STR), Stats.AC))
                    .setDescription("It is bonked.").setOnApplication(
                            new Deltas().addDamage(new DamageDice(1, DieType.SIX, DamageFlavor.MAGICAL_BLUDGEONING))));
            weaponized.setBurstChance(16)
                    .addBurstEffectSource(new CreatureEffectSource.Builder("Whimsy healing").instantPersistence()
                            .setDescription("The whimsystick chose to heal").setOnApplication(
                                    new Deltas().addDamage(new DamageDice(1, DieType.SIX, DamageFlavor.HEALING))));
            return weaponized;
        });
        builder.inner.adjustEquipableCapability(WeaponBuilder
                .basicEquipable(
                        EnumSet.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.QUARTERSTAFF, EquipmentTypes.CLUB))
                .andThen(equipable -> {
                    equipable.addEquipEffect(new CreatureEffectSource.Builder("AC bonus")
                            .setPersistence(new EffectPersistence(TickType.CONDITIONAL))
                            .setDescription("This will magically increase your AC")
                            .setOnApplication(new Deltas().setStatChange(Stats.AC, 1)));
                    return equipable;
                }));
        return builder;
    }

    public static WeaponBuilder ofReaperScythe() {
        WeaponBuilder builder = new WeaponBuilder();
        builder.inner.setName("Reaper Scythe")
                .setDescriptionString("This is a nice, long, shiny scythe.  It's super powerful...");
        builder.inner.adjustWeaponCapability(weaponized -> {
            if (weaponized == null) {
                weaponized = new WeaponCapability.Weaponized.WeaponizedBuilder();
            }
            weaponized.setMainDamageFlavor(DamageFlavor.NECROTIC).setWeaponSubtype(WeaponSubtype.FINESSE);
            weaponized.addHitEffectSource(new CreatureEffectSource.Builder("Scythe").instantPersistence()
                    .setResistance(new EffectResistance(EnumSet.of(Attributes.STR, Attributes.DEX), Stats.AC))
                    .setDescription("Scythes reap things.")
                    .setOnApplication(new Deltas().addDamage(new DamageDice(1, DieType.EIGHT, DamageFlavor.NECROTIC))));
            weaponized.setBurstChance(98)
                    .addBurstEffectSource(new CreatureEffectSource.Builder("Necrotic Damage").instantPersistence()
                            .setResistance(new EffectResistance(null, Stats.MAXHP, null, null, Stats.AC, null,
                                    TargetResistAmount.HALF))
                            .setDescription("This weapon does extra necrotic damage.")
                            .setOnApplication(new Deltas().setStatChange(Stats.CURRENTHP, -100)));
            weaponized.setToHitBonus(2);
            return weaponized;
        });
        builder.inner.adjustEquipableCapability(WeaponBuilder.basicEquipable(EnumSet.of(EquipmentTypes.LONGSWORD,
                EquipmentTypes.SICKLE, EquipmentTypes.QUARTERSTAFF, EquipmentTypes.MARTIALWEAPONS)));
        return builder;
    }

    public WeaponBuilder setMainDamageFlavor(DamageFlavor flavor) {
        if (flavor == null) {
            return this;
        }
        this.inner.adjustWeaponCapability(weaponized -> {
            if (weaponized == null) {
                weaponized = new WeaponCapability.Weaponized.WeaponizedBuilder();
            }
            weaponized.setMainDamageFlavor(flavor);
            return weaponized;
        });
        return this;
    }

    public WeaponBuilder setWeaponSubtype(WeaponSubtype subtype) {
        if (subtype == null) {
            return this;
        }
        this.inner.adjustWeaponCapability(weaponized -> {
            if (weaponized == null) {
                weaponized = new WeaponCapability.Weaponized.WeaponizedBuilder();
            }
            weaponized.setWeaponSubtype(subtype);
            return weaponized;
        });
        return this;
    }

    public WeaponBuilder addHitEffect(CreatureEffectSource.Builder hitBuilder) {
        if (hitBuilder == null) {
            return this;
        }
        this.inner.adjustWeaponCapability(weaponized -> {
            if (weaponized == null) {
                weaponized = new WeaponCapability.Weaponized.WeaponizedBuilder();
            }
            weaponized.addHitEffectSource(hitBuilder);
            return weaponized;
        });
        return this;
    }

    public WeaponBuilder addEquipmentType(EquipmentTypes equipmentType) {
        if (equipmentType == null) {
            return this;
        }
        this.inner.adjustEquipableCapability(equipable -> {
            if (equipable == null) {
                equipable = new EquipableCapability.Equipable.EquipableBuilder(EquipmentSlots.WEAPON);
            }
            equipable.addEquipmentSlot(EquipmentSlots.WEAPON);
            equipable.addEquipmentType(equipmentType);
            return equipable;
        });
        return this;
    }

    @Override
    public WeaponBuilder setName(String name) {
        this.inner.setName(name);
        return this;
    }

    @Override
    public WeaponBuilder reset() {
        this.inner.reset();
        return this;
    }

    @Override
    public Item.ItemBuilder getItemBuilder() {
        return this.inner;
    }

    @Override
    public IItem build() {
        return this.inner.build();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WeaponBuilder [inner=").append(inner).append("]");
        return builder.toString();
    }

}
