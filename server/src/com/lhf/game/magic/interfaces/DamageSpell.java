package com.lhf.game.magic.interfaces;

import java.util.List;
import java.util.StringJoiner;

import com.lhf.game.dice.DamageDice;
import com.lhf.game.enums.DamageFlavor;

public interface DamageSpell {
    public abstract List<DamageDice> getDamages();

    public default String printWhichDamages() {
        StringJoiner sj = new StringJoiner(", ");
        sj.setEmptyValue("no damage!");
        for (DamageDice dd : this.getDamages()) {
            sj.add(dd.getColorTaggedName());
        }
        return sj.toString();
    }

    public abstract DamageFlavor getMainFlavor();

    public default String printStats() {
        StringBuilder sb = new StringBuilder();
        if (this.getDamages().size() > 0) {
            sb.append("This spell deals damage like ").append(this.printWhichDamages()).append("\n");
        }
        return sb.toString();
    }
}
