package com.lhf.messages.out;

import java.util.StringJoiner;

import com.lhf.game.creature.Creature;
import com.lhf.game.dice.Dice.RollResult;
import com.lhf.messages.OutMessageType;

public class AttackDamageMessage extends OutMessage {
    private Creature attacker;
    private Creature victim;
    private StringJoiner builtDamages;
    private boolean killedIt;
    private RollResult total;

    public AttackDamageMessage(Creature attacker, Creature victim) {
        super(OutMessageType.ATTACK_DAMAGE);
        this.attacker = attacker;
        this.victim = victim;
        this.builtDamages = new StringJoiner(" ");
        this.builtDamages.setEmptyValue("");
        this.total = null;
        this.killedIt = false;
    }

    public void addDamage(RollResult damage) {
        builtDamages.add(damage.getColorTaggedName());
        if (this.total == null) {
            this.total = damage;
        } else {
            this.total = this.total.combine(damage);
        }
    }

    public void announceDeath() {
        this.killedIt = true;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        if (builtDamages.length() > 0) {
            sj.add(this.attacker.getColorTaggedName()).add("has dealt");
            sj.add(this.builtDamages.toString());
            sj.add("damage to").add(this.victim.getColorTaggedName());
            sj.add("for a total of");
            sj.add(this.total.getColorTaggedName()).add("\n");
        } else {
            sj.add(this.attacker.getColorTaggedName()).add("attacks").add(this.victim.getColorTaggedName()).add("!");
        }
        if (this.killedIt) {
            sj.add(this.victim.getColorTaggedName()).add("has died.");
        }
        return sj.toString();
    }

    public Creature getAttacker() {
        return attacker;
    }

    public Creature getVictim() {
        return victim;
    }

    public boolean isKilledIt() {
        return killedIt;
    }

    public RollResult getTotal() {
        return total;
    }
}
