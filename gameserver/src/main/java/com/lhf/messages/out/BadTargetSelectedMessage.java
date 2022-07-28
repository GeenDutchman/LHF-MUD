package com.lhf.messages.out;

import java.util.List;
import java.util.StringJoiner;

import com.lhf.Taggable;
import com.lhf.messages.OutMessageType;

public class BadTargetSelectedMessage extends OutMessage {
    public enum BadTargetOption {
        SELF, NOTARGET, DNE, UNCLEAR;
    }

    private BadTargetOption bde;
    private String badTarget;
    private List<? extends Taggable> possibleTargets;

    public BadTargetSelectedMessage(BadTargetOption bde, String badTarget) {
        super(OutMessageType.BAD_TARGET_SELECTED);
        this.bde = bde;
        this.badTarget = badTarget;
    }

    public BadTargetSelectedMessage(BadTargetOption bde, String badTarget, List<? extends Taggable> possibleTargets) {
        super(OutMessageType.BAD_TARGET_SELECTED);
        this.bde = bde;
        this.badTarget = badTarget;
        this.possibleTargets = possibleTargets;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.setEmptyValue("");
        switch (this.bde) {
            case SELF:
                sj.add("You cannot target yourself!");
                break;
            case NOTARGET:
                sj.add("You did not choose any targets.");
                break;
            case DNE:
                if (this.badTarget != null && this.badTarget.length() > 0) {
                    sj.add(this.badTarget).add("does not exist as a target or is not targetable.");
                } else {
                    sj.add("One of your targets did not exist.");
                }
                break;
            case UNCLEAR:
                if (this.badTarget != null && this.badTarget.length() > 0) {
                    sj.add("You cannot target '").add(this.badTarget).add("' because it is unclear.");
                } else {
                    sj.add("It is unclear what you are targeting.");
                }
                break;
            default:
                sj.add("You have selected a bad or invalid target.");
        }

        if (this.possibleTargets != null && this.possibleTargets.size() > 0) {
            sj.add("Possible targets include:\n");
            for (Taggable tagger : this.possibleTargets) {
                sj.add(tagger.getColorTaggedName());
            }
        }

        return sj.toString();
    }

    public BadTargetOption getBde() {
        return bde;
    }

    public String getBadTarget() {
        return badTarget;
    }

    public List<? extends Taggable> getPossibleTargets() {
        return possibleTargets;
    }
}
