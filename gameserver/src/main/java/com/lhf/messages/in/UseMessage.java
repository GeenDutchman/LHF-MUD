package com.lhf.messages.in;

import java.util.StringJoiner;

import com.lhf.messages.Command;
import com.lhf.messages.CommandMessage;

public class UseMessage extends Command {

    UseMessage(String payload) {
        super(CommandMessage.USE, payload, true);
        this.addPreposition("on");
    }

    public String getUsefulItem() {
        if (this.directs.size() < 1) {
            return null;
        }
        return this.directs.get(0);
    }

    public String getTarget() {
        return this.indirects.getOrDefault("on", null);
    }

    @Override
    public Boolean isValid() {
        Boolean validated = true;
        if (this.indirects.size() > 0) {
            validated = this.indirects.containsKey("on") && this.indirects.size() == 1;
        }
        return super.isValid() && this.directs.size() == 1 && validated;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.toString());
        String item = this.getUsefulItem();
        sj.add("Item:");
        if (item != null) {
            sj.add(item);
        } else {
            sj.add("Not using anything!");
        }
        String target = this.getTarget();
        if (target != null) {
            sj.add("Targeting:").add(target);
        }
        return sj.toString();
    }

}
