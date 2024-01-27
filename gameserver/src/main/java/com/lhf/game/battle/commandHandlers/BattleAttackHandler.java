package com.lhf.game.battle.commandHandlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.logging.Level;

import com.lhf.game.EffectResistance;
import com.lhf.game.battle.Attack;
import com.lhf.game.battle.BattleManager.PooledBattleManagerCommandHandler;
import com.lhf.game.battle.MultiAttacker;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.dice.MultiRollResult;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.item.ItemNameSearchVisitor;
import com.lhf.game.item.Weapon;
import com.lhf.game.map.SubArea;
import com.lhf.game.map.SubArea.SubAreaSort;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.BadTargetSelectedEvent;
import com.lhf.messages.events.BadTargetSelectedEvent.BadTargetOption;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.ItemNotPossessedEvent;
import com.lhf.messages.events.TargetDefendedEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.AttackMessage;

public class BattleAttackHandler implements PooledBattleManagerCommandHandler {
    private final static String helpString = new StringJoiner(" ")
            .add("\"attack [name]\"").add("Attacks a creature").add("\r\n")
            .add("\"attack [name] with [weapon]\"").add("Attack the named creature with a weapon that you have.")
            .add("In the unlikely event that either the creature or the weapon's name contains 'with', enclose the name in quotation marks.")
            .toString();

    @Override
    public Reply handleCommand(CommandContext ctx, Command cmd) {
        if (cmd == null || cmd.getType() != this.getHandleType()) {
            return ctx.failhandle();
        }
        final AttackMessage attackMessage = new AttackMessage(cmd);
        final SubArea bm = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
        if (!bm.hasRunningThread("AttackHandler.handle()")) {
            ICreature attacker = ctx.getCreature();
            List<ICreature> collected = this.collectTargetsFromRoom(bm, attacker,
                    attackMessage.getTargets());
            if (collected == null || collected.size() == 0) {
                ctx.receive(BadTargetSelectedEvent.getBuilder()
                        .setNotBroadcast().setBde(BadTargetOption.NOTARGET).Build());
                return ctx.handled();
            }
            this.log(Level.FINE, "No current battle detected, starting battle");
            bm.instigate(attacker, collected);
        } else {
            this.log(Level.FINE, "Battle detected, empooling command");
        }
        this.onEmpool(ctx, bm.empool(ctx, cmd));
        return ctx.handled();
    }

    /**
     * Collect the targeted creatures from the room.
     * If it is unclear which target is meant, it will skip that name.
     * If the self is targeted, then it will be skipped.
     * 
     * @param attacker Creature who selected the targets
     * @param names    names of the targets
     * @return Best effort list of Creatures, possibly size 0
     */
    private List<ICreature> collectTargetsFromRoom(final SubArea bm, ICreature attacker, List<String> names) {
        List<ICreature> targets = new ArrayList<>();
        BadTargetSelectedEvent.Builder btMessBuilder = BadTargetSelectedEvent.getBuilder().setNotBroadcast();
        if (names == null || names.size() == 0) {
            return targets;
        }
        for (String targetName : names) {
            btMessBuilder.setBadTarget(targetName);
            List<ICreature> possTargets = new ArrayList<>(bm.getArea().getCreaturesLike(targetName));
            if (possTargets.size() == 1) {
                ICreature targeted = possTargets.get(0);
                if (targeted.equals(attacker)) {
                    ICreature.eventAccepter.accept(attacker, btMessBuilder.setBde(BadTargetOption.SELF).Build());
                    continue; // go to next name
                }
                targets.add(targeted);
            } else {
                btMessBuilder.setPossibleTargets(possTargets);
                if (possTargets.size() == 0) {
                    ICreature.eventAccepter.accept(attacker, btMessBuilder.setBde(BadTargetOption.DNE).Build());
                } else {
                    ICreature.eventAccepter.accept(attacker, btMessBuilder.setBde(BadTargetOption.UNCLEAR).Build());
                }
                continue; // go to next name
            }
        }
        return targets;
    }

    @Override
    public AMessageType getHandleType() {
        return AMessageType.ATTACK;
    }

    @Override
    public Optional<String> getHelp(CommandContext ctx) {
        return Optional.of(BattleAttackHandler.helpString);
    }

    @Override
    public boolean isEnabled(CommandContext ctx) {
        if (!PooledBattleManagerCommandHandler.super.isEnabled(ctx)) {
            return false;
        }
        final SubArea bm = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
        if (bm == null) {
            return false;
        }
        if (bm.getArea() == null) {
            return false;
        }
        return bm.getArea().getCreatures().size() > 1 || bm.getCreatures().size() > 1;
    }

    /**
     * Gets a designated weapon for a creature, either by name, or by default.
     * If a name is provided, but is not found or the item found is not a weapon,
     * then return NULL.
     * 
     * @param attacker   The creature who is attacking
     * @param weaponName The name of a weapon
     * @return a weapon or NULL if the item found is not a weapon or is not found
     */
    private Weapon getDesignatedWeapon(ICreature attacker, String weaponName) {
        if (weaponName != null && weaponName.length() > 0) {
            ItemNameSearchVisitor visitor = new ItemNameSearchVisitor(weaponName);
            attacker.acceptItemVisitor(visitor);
            Optional<Weapon> inventoryItem = visitor.getWeapon();
            ItemNotPossessedEvent.Builder builder = ItemNotPossessedEvent.getBuilder().setNotBroadcast()
                    .setItemName(weaponName).setItemType(Weapon.class.getSimpleName());
            if (inventoryItem.isEmpty()) {
                ICreature.eventAccepter.accept(attacker, builder.Build());
                return null;
            }
            return inventoryItem.get();
        } else {
            return attacker.defaultWeapon();
        }
    }

    private void applyAttacks(final SubArea bm, ICreature attacker, Weapon weapon, Collection<ICreature> targets) {
        for (ICreature target : targets) {
            this.log(Level.FINEST,
                    () -> String.format("Applying attack from %s on %s", attacker.getName(), target.getName()));
            CreatureFaction.checkAndHandleTurnRenegade(attacker, target, bm.getArea());
            if (!bm.hasCreature(target)) {
                bm.addCreature(target);
            }
            Attack a = attacker.attack(weapon);
            Vocation attackerVocation = attacker.getVocation();
            if (attackerVocation != null && attackerVocation instanceof MultiAttacker) {
                a = ((MultiAttacker) attackerVocation).modifyAttack(a, false);
            }

            for (CreatureEffect effect : a) {
                EffectResistance resistance = effect.getResistance();
                MultiRollResult attackerResult = null;
                MultiRollResult targetResult = null;
                if (resistance != null) {
                    attackerResult = resistance.actorEffort(attacker, weapon.getToHitBonus());
                    targetResult = resistance.targetEffort(target, 0);
                }

                if (resistance == null || targetResult == null
                        || (attackerResult != null && (attackerResult.getTotal() > targetResult.getTotal()))) {
                    GameEvent cam = target.applyEffect(effect);
                    bm.announce(cam);
                } else {
                    bm.announce(TargetDefendedEvent.getBuilder().setAttacker(attacker).setTarget(target)
                            .setOffense(attackerResult)
                            .setDefense(targetResult).Build());
                }
            }

        }
    }

    @Override
    public Reply flushHandle(CommandContext ctx, Command cmd) {
        if (cmd != null && cmd.getType() == this.getHandleType()) {
            final AttackMessage aMessage = new AttackMessage(cmd);
            final SubArea bm = ctx.getSubAreaForSort(SubAreaSort.BATTLE);
            bm.log(Level.INFO,
                    ctx.getCreature().getName() + " attempts attacking " + aMessage.getTargets());

            ICreature attacker = ctx.getCreature();

            BadTargetSelectedEvent.Builder btMessBuilder = BadTargetSelectedEvent.getBuilder()
                    .setNotBroadcast();

            if (aMessage.getNumTargets() == 0) {
                ctx.receive(btMessBuilder.setBde(BadTargetOption.NOTARGET).Build());
                return ctx.handled();
            }

            int numAllowedTargets = 1;
            Vocation attackerVocation = attacker.getVocation();
            if (attackerVocation != null && attackerVocation instanceof MultiAttacker) {
                numAllowedTargets = ((MultiAttacker) attackerVocation).maxAttackCount(false);
            }

            if (aMessage.getNumTargets() > numAllowedTargets) {
                String badTarget = aMessage.getTargets().get(numAllowedTargets);
                ctx.receive(btMessBuilder.setBadTarget(badTarget).setBde(BadTargetOption.TOO_MANY).Build());
                return ctx.handled();
            }

            List<ICreature> targets = this.collectTargetsFromRoom(bm, attacker, aMessage.getTargets());
            if (targets == null || targets.size() == 0) {
                ctx.receive(btMessBuilder.setBde(BadTargetOption.NOTARGET).Build());
                return ctx.handled();
            }

            if (attackerVocation != null && attackerVocation instanceof MultiAttacker) {
                ((MultiAttacker) attackerVocation).attackNumberOfTargets(targets.size(), false);
            }

            Weapon weapon = this.getDesignatedWeapon(attacker, aMessage.getWeapon());
            if (weapon == null) {
                this.log(Level.SEVERE, () -> String.format("No weapon found! %s %s", ctx, cmd));
                return ctx.handled();
            }

            this.applyAttacks(bm, attacker, weapon, targets);
            return ctx.handled();
        }
        return ctx.failhandle();
    }
}