package com.lhf.game.magic.concrete;

import com.lhf.game.magic.interfaces.RoomAffector;

public class Thaumaturgy extends RoomAffector {

    protected Thaumaturgy() {
        super(0, "Thaumaturgy", "A way to magical announce your presence");
        this.setInvocation("zarmamoo");
    }

    @Override
    public String Cast() {
        StringBuilder sb = new StringBuilder();
        String casterName = this.getCaster().getName();
        String[] splitname = casterName.split("( |_)");
        int longest = 0;
        for (String split : splitname) {
            if (split.length() > longest) {
                longest = split.length();
            }
        }
        sb.append(this.getCaster().getStartTagName()).append("\\").append("|".repeat(longest)).append("/")
                .append(this.getCaster().getEndTagName()).append("\n");
        for (String split : splitname) {
            sb.append(this.getCaster().getStartTagName()).append("-").append(split)
                    .append(" ".repeat(longest - split.length())).append("-").append(this.getCaster().getEndTagName())
                    .append("\n");
        }
        sb.append(this.getCaster().getStartTagName()).append("/").append("|".repeat(longest)).append("\\")
                .append(this.getCaster().getEndTagName()).append("\n");
        return sb.toString();
    }

}
