package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.game.enums.EquipmentSlots;
import com.lhf.messages.Command;
import com.lhf.messages.grammar.PhraseList;
import com.lhf.messages.grammar.PrepositionalPhrases;

public class UnequipMessage extends Command {

    public UnequipMessage(AMessageType command, String whole, Boolean isValid, PhraseList phrases,
            PrepositionalPhrases prepositional) {
        super(command, whole, isValid, phrases, prepositional);
    }

    public String getUnequipWhat() {
        if (this.getDirects().size() < 1) {
            return null;
        }
        return this.getDirects().get(0);
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.toString());
        sj.add("UnequipWhat:");
        String unequip = this.getUnequipWhat();
        if (unequip != null) {
            sj.add(unequip);
        } else {
            sj.add("Nothing to unequip!");
        }
        sj.add("IsSlot:").add(EquipmentSlots.isEquipmentSlot(unequip).toString());
        return sj.toString();
    }

}
