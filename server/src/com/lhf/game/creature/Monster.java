package com.lhf.game.creature;

import com.lhf.game.battle.Attack;
import com.lhf.game.battle.BattleAI;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.enums.MonsterAI;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.interfaces.Equipable;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;

public class Monster extends Creature implements BattleAI {
    private boolean activelyHostile;
    private static long serialNumber = 0;
    private long monsterNumber;

    private MonsterAI aiType;
    private String lastAttacker;
    private int lastDamage = 0;

    public Monster() {
        super();
        this.activelyHostile = false;
        this.setName("Monster");
        this.setSerialNumber();
        this.aiType = MonsterAI.RANDOM;
    }

    public Monster(String name, Statblock statblock) {
        super(name, statblock);
        this.activelyHostile = true;
        this.setSerialNumber();
        this.aiType = MonsterAI.RETALIATORY;
    }

    public void setAiType(MonsterAI newType) {
        this.aiType = newType;
    }

    public void setActivelyHostile(boolean setting) {
        this.activelyHostile = setting;
    }

    private void setSerialNumber() {
        this.monsterNumber = Monster.serialNumber;
        Monster.serialNumber++;
    }

    public boolean isActivelyHostile() {
        return this.activelyHostile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Monster monster = (Monster) o;
        return monsterNumber == monster.monsterNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(monsterNumber) + Objects.hash(this.getName()) * 13;
    }

    private Player getRandomPlayer(Collection<Creature> participants) {
        Player[] players = participants.stream().filter(creature -> creature instanceof Player).toArray(Player[]::new);
        int randomIndex = (int)(Math.random() * players.length);
        return players[randomIndex];
    }

    private Optional<Creature> getAttackerCreature(Collection<Creature> participants) {
        if (lastAttacker == null || lastAttacker.isEmpty()) {
            return Optional.empty();
        }
        return participants.stream().filter(creature -> lastAttacker.equals(creature.getName())).findFirst();
    }

    @Override
    public String applyAttack(Attack attack) {
        int prevHealth = this.getStats().get(Stats.CURRENTHP);
        String result = super.applyAttack(attack);
        int damageDealt = prevHealth - this.getStats().get(Stats.CURRENTHP);
        switch (aiType) {
            case RETALIATORY:
                if (damageDealt > 0) {
                    lastDamage = damageDealt;
                    lastAttacker = attack.getAttacker();
                }
                break;
            case VENGEFUL:
                if (damageDealt >= lastDamage) {
                    lastDamage = damageDealt;
                    lastAttacker = attack.getAttacker();
                }
                break;
        }
        return result;
    }

    @Override
    public String performBattleTurn(Collection<Creature> participants) {
        StringBuilder output = new StringBuilder();

        Optional<Creature> maybeTarget = getAttackerCreature(participants);
        Creature target;
        if (maybeTarget.isPresent()) {
            target = maybeTarget.get();
        } else {
            target = getRandomPlayer(participants);
        }

        output.append(getColorTaggedName());
        output.append(" is attacking ");
        output.append(target.getColorTaggedName());
        output.append('\n');
        Equipable weapon = getWeapon();
        Attack atk = attack(weapon.getName(), target.getName());
        output.append(target.applyAttack(atk));
        return output.toString();
    }
}
