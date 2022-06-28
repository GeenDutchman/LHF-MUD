package com.lhf.messages.out;

public class BadTargetSelectedMessage extends OutMessage {
    public enum BadTargetOption {
        SELF, NOTARGET, DNE;
    }

    private BadTargetOption bde;

    public BadTargetSelectedMessage(BadTargetOption bde) {
        this.bde = bde;
    }

    @Override
    public String toString() {
        switch (this.bde) {
            case SELF:
                return "You cannot target yourself!";
            case NOTARGET:
                return "You did not choose any targets.";
            case DNE:
                return "One of your targets did not exist.";
            default:
                return "You have selected a bad or invalid target.";
        }
    }

    public BadTargetOption getBde() {
        return bde;
    }
}
