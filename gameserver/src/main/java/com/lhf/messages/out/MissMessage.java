package com.lhf.messages.out;

import java.util.StringJoiner;

import com.lhf.game.battle.Attack;
import com.lhf.game.creature.Creature;
import com.lhf.game.dice.Dice;
import com.lhf.game.dice.DiceD4;
import com.lhf.game.dice.Dice.RollResult;

public class MissMessage extends OutMessage {
    private Creature attacker;
    private Creature target;
    private RollResult offense;
    private RollResult defense;

    private String staticOutput;

    public MissMessage(Creature attacker, Creature target, RollResult offense, RollResult defense) {
        this.attacker = attacker;
        this.target = target;
        this.offense = offense;
        this.defense = defense;
        this.staticOutput = null;
    }

    @Override
    public String toString() {
        if (this.staticOutput == null) {
            StringJoiner output = new StringJoiner(" ");
            Dice chooser = new DiceD4(1);
            int which = chooser.rollDice().getTotal();
            switch (which) {
                case 1:
                    output.add(attacker.getColorTaggedName());
                    if (this.offense != null) {
                        output.add(this.offense.getColorTaggedName());
                    }
                    output.add("misses").add(target.getColorTaggedName());
                    if (this.defense != null) {
                        output.add(this.defense.getColorTaggedName());
                    }
                    break;
                case 2:
                    output.add(target.getColorTaggedName()).add("dodged");
                    if (this.defense != null) {
                        output.add(this.defense.getColorTaggedName());
                    }
                    output.add("the attack");
                    if (this.offense != null) {
                        output.add(this.offense.getColorTaggedName());
                    }
                    output.add("from").add(attacker.getColorTaggedName());
                    break;
                case 3:
                    output.add(attacker.getColorTaggedName()).add("whiffed");
                    if (this.offense != null) {
                        output.add(this.offense.getColorTaggedName());
                    }
                    output.add("their attack on").add(target.getColorTaggedName());
                    if (this.defense != null) {
                        output.add(this.defense.getColorTaggedName());
                    }
                    break;
                default:
                    output.add("The attack");
                    if (this.offense != null) {
                        output.add(this.offense.getColorTaggedName());
                    }
                    output.add("by").add(attacker.getColorTaggedName());
                    output.add("on").add(target.getColorTaggedName());
                    output.add("does not land");
                    if (this.defense != null) {
                        output.add(this.defense.getColorTaggedName());
                    }
                    break;

            }
            output.add("\n");
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

    public RollResult getOffense() {
        return offense;
    }

    public RollResult getDefense() {
        return defense;
    }

}
