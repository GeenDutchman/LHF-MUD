package com.lhf.game.item;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.lhf.game.battle.Attack;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.ICreature;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DiceD100;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.item.interfaces.WeaponSubtype;
import com.lhf.messages.events.SeeEvent.ABuilder;
import com.lhf.messages.events.SeeEvent.SeeCategory;

public interface WeaponCapability extends ItemCapability {
    public DamageFlavor getMainDamageFlavor();

    public WeaponSubtype getWeaponSubtype();

    public default int getToHitBonus() {
        return 0;
    }

    public Set<CreatureEffectSource> getHitEffectSources();

    public default Set<CreatureEffectSource> getBurstEffectSources() {
        return Set.of();
    }

    public default int getBurstChance() {
        return 0;
    }

    @Override
    default boolean isStateful() {
        return false;
    }

    @Override
    default void describe(ABuilder<?> seeEventBuilder) {
        if (seeEventBuilder == null) {
            return;
        }
        final Set<CreatureEffectSource> sources = this.getHitEffectSources();
        if (sources != null) {
            for (CreatureEffectSource source : sources) {
                if (source.getOnApplication() == null) {
                    continue;
                }
                for (DamageDice dd : source.getOnApplication().getDamages()) {
                    seeEventBuilder.addSeen(SeeCategory.DAMAGES, dd);
                }
            }
        }
    }

    @Override
    default ItemCapabilityNames getCapabilityName() {
        return ItemCapabilityNames.WEAPON;
    }

    public static Attack generateAttack(ICreature attacker, IItem item) {
        if (attacker == null || item == null) {
            return null;
        }
        final WeaponCapability capability = item.getWeaponCapability();
        if (capability == null) {
            return null;
        }
        Set<CreatureEffect> effects = new LinkedHashSet<>();
        final Set<CreatureEffectSource> hitEffects = capability.getHitEffectSources();
        if (hitEffects != null) {
            for (final CreatureEffectSource creatureEffectSource : hitEffects) {
                effects.add(new CreatureEffect(creatureEffectSource, attacker, item));
            }
        }
        final Set<CreatureEffectSource> burstEffects = capability.getBurstEffectSources();
        if (burstEffects != null) {
            DiceD100 hundred = new DiceD100(1);
            if (hundred.rollDice().getRoll() <= capability.getBurstChance()) {
                for (CreatureEffectSource creatureEffectSource : burstEffects) {
                    effects.add(new CreatureEffect(creatureEffectSource, attacker, item));
                }
            }
        }
        return new Attack(attacker, null, effects); // change this to take the item
    }

    public static WeaponCapability generateWeaponCapability() {
        return new Weaponized();
    }

    public static final class Weaponized implements WeaponCapability, Serializable {
        private final DamageFlavor mainDamageFlavor;
        private final WeaponSubtype weaponSubtype;
        private final int toHitBonus;
        private final Set<CreatureEffectSource> hitEffectSources;
        private final Set<CreatureEffectSource> burstEffectSources;
        private final int burstChance;

        public static final class WeaponizedBuilder implements Serializable {
            private DamageFlavor mainDamageFlavor;
            private WeaponSubtype weaponSubtype;
            private int toHitBonus = 0;
            private Set<CreatureEffectSource.Builder> hitEffectSources;
            private Set<CreatureEffectSource.Builder> burstEffectSources;
            private int burstChance = 0;

            public DamageFlavor getMainDamageFlavor() {
                return mainDamageFlavor;
            }

            public WeaponizedBuilder setMainDamageFlavor(DamageFlavor mainDamageFlavor) {
                this.mainDamageFlavor = mainDamageFlavor;
                return this;
            }

            public WeaponSubtype getWeaponSubtype() {
                return weaponSubtype;
            }

            public WeaponizedBuilder setWeaponSubtype(WeaponSubtype weaponSubtype) {
                this.weaponSubtype = weaponSubtype;
                return this;
            }

            public int getToHitBonus() {
                return toHitBonus;
            }

            public WeaponizedBuilder setToHitBonus(int toHitBonus) {
                this.toHitBonus = toHitBonus;
                return this;
            }

            public Set<CreatureEffectSource> getHitEffectSources() {
                return hitEffectSources == null ? null
                        : hitEffectSources.stream().filter(builder -> builder != null).map(builder -> builder.build())
                                .collect(Collectors.toCollection(LinkedHashSet::new));
            }

            public WeaponizedBuilder setHitEffectSources(Set<CreatureEffectSource.Builder> hitEffectSources) {
                this.hitEffectSources = hitEffectSources;
                return this;
            }

            public WeaponizedBuilder addHitEffectSource(CreatureEffectSource.Builder builder) {
                if (builder != null) {
                    if (this.hitEffectSources == null) {
                        this.hitEffectSources = new LinkedHashSet<>();
                    }
                    this.hitEffectSources.add(builder);
                }
                return this;
            }

            public WeaponizedBuilder clearHitEffectSources() {
                if (this.hitEffectSources != null) {
                    this.hitEffectSources.clear();
                }
                return this;
            }

            public Set<CreatureEffectSource> getBurstEffectSources() {
                return burstEffectSources == null ? null
                        : burstEffectSources.stream().filter(builder -> builder != null).map(builder -> builder.build())
                                .collect(Collectors.toCollection(LinkedHashSet::new));
            }

            public WeaponizedBuilder setBurstEffectSources(Set<CreatureEffectSource.Builder> burstEffectSources) {
                this.burstEffectSources = burstEffectSources;
                return this;
            }

            public WeaponizedBuilder addBurstEffectSource(CreatureEffectSource.Builder builder) {
                if (builder != null) {
                    if (this.burstEffectSources == null) {
                        this.burstEffectSources = new LinkedHashSet<>();
                    }
                    this.burstEffectSources.add(builder);
                }
                return this;
            }

            public WeaponizedBuilder clearBurstEffectSources() {
                if (this.burstEffectSources != null) {
                    this.burstEffectSources.clear();
                }
                return this;
            }

            public int getBurstChance() {
                return burstChance;
            }

            public WeaponizedBuilder setBurstChance(int burstChance) {
                this.burstChance = burstChance;
                return this;
            }

            public Weaponized build() {
                return new Weaponized(this);
            }

        }

        private Weaponized() {
            this.mainDamageFlavor = DamageFlavor.BLUDGEONING;
            this.weaponSubtype = WeaponSubtype.CREATUREPART;
            this.toHitBonus = 0;
            this.hitEffectSources = Set.of();
            this.burstEffectSources = Set.of();
            this.burstChance = 0;
        }

        protected Weaponized(WeaponizedBuilder builder) {
            if (builder == null) {
                this.mainDamageFlavor = DamageFlavor.BLUDGEONING;
                this.weaponSubtype = WeaponSubtype.CREATUREPART;
                this.toHitBonus = 0;
                this.hitEffectSources = Set.of();
                this.burstEffectSources = Set.of();
                this.burstChance = 0;
            } else {
                this.mainDamageFlavor = builder.getMainDamageFlavor();
                this.weaponSubtype = builder.getWeaponSubtype();
                this.toHitBonus = builder.getToHitBonus();
                this.hitEffectSources = builder.getHitEffectSources();
                this.burstEffectSources = builder.getBurstEffectSources();
                this.burstChance = builder.getBurstChance();
            }
        }

        @Override
        public DamageFlavor getMainDamageFlavor() {
            return this.mainDamageFlavor;
        }

        @Override
        public WeaponSubtype getWeaponSubtype() {
            return this.weaponSubtype;
        }

        @Override
        public int getToHitBonus() {
            return this.toHitBonus;
        }

        @Override
        public Set<CreatureEffectSource> getHitEffectSources() {
            return this.hitEffectSources == null ? null : Collections.unmodifiableSet(this.hitEffectSources);
        }

        @Override
        public Set<CreatureEffectSource> getBurstEffectSources() {
            return this.burstEffectSources == null ? null : Collections.unmodifiableSet(this.burstEffectSources);
        }

        @Override
        public int getBurstChance() {
            return this.burstChance;
        }

    }

}
