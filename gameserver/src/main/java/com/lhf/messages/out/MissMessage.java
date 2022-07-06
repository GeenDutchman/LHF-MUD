package com.lhf.messages.out;

import com.lhf.game.battle.Attack;
import com.lhf.game.creature.Creature;
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.DiceD4;

public class MissMessage extends OutMessage {
    private Creature attacker;
    private Creature target;
    private Attack attack;

    private String staticOutput;

    public MissMessage(Creature attacker, Creature target, Attack attack) {
        this.attacker = attacker;
        this.target = target;
        this.attack = attack;
        this.staticOutput = null;
    }

    @Override
    public String toString() {
        if (this.staticOutput == null) {
            StringBuilder output = new StringBuilder();
            Dice chooser = new DiceD4(1);
            int which = chooser.rollDice().getTotal();
            switch (which) {
                case 1:
                    output.append(attacker.getColorTaggedName()).append(' ').append(attack.getToHit())
                            .append(" misses ")
                            .append(target.getColorTaggedName());
                    break;
                case 2:
                    output.append(target.getColorTaggedName()).append(" dodged the attack ").append(attack.getToHit())
                            .append(" from ")
                            .append(attacker.getColorTaggedName());
                    break;
                case 3:
                    output.append(attacker.getColorTaggedName()).append(" whiffed ").append(attack.getToHit())
                            .append(" their attack on ")
                            .append(target.getColorTaggedName());
                    break;
                default:
                    output.append("The attack ").append(attack.getToHit()).append(" by ")
                            .append(attacker.getColorTaggedName())
                            .append(" on ")
                            .append(target.getColorTaggedName()).append(" does not land");
                    break;

            }
            output.append('\n');
            this.staticOutput = output.toString();
        }
        return this.staticOutput;
    }

    public Creature getAttacker() {
        return attacker;
    }

    public Creature getTarget() {
        return target;
    }

    public Attack getAttack() {
        return attack;
    }

}
