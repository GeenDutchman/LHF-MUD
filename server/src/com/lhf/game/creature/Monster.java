package com.lhf.game.creature;

import com.lhf.game.battle.Attack;
import com.lhf.game.battle.BattleAI;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.interfaces.Equipable;

import java.util.Collection;
import java.util.Objects;

public class Monster extends Creature implements BattleAI {
    private boolean activelyHostile;
    private static long serialNumber = 0;
    private long monsterNumber;

    public Monster() {
        super();
        this.activelyHostile = false;
        this.setName("Monster");
        this.setSerialNumber();
    }

    public Monster(String name, Statblock statblock) {
        super(name, statblock);
        this.activelyHostile = true;
        this.setSerialNumber();
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
        Player[] players =  participants.stream().filter(creature -> creature instanceof Player).toArray(Player[]::new);
        int randomIndex = (int)(Math.random() * players.length);
        return players[randomIndex];
    }

    @Override
    public String performBattleTurn(Collection<Creature> participants) {
        StringBuilder output = new StringBuilder();
        Player target = getRandomPlayer(participants);
        output.append(this.getColorTaggedName());
        output.append(" is attacking ");
        output.append(target.getColorTaggedName());
        output.append('\n');
        Equipable weapon = getEqupped(EquipmentSlots.WEAPON);
        Attack atk = attack(weapon.getName(), target.getName());
        output.append(target.applyAttack(atk));
        return output.toString();
    }
}
