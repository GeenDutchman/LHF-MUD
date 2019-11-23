package com.lhf.server.messages.in;


public class AttackMessage extends InMessage {
    private String weaponName = "";
    private String targetName = "";

    static final private String[] prepositionFlags = {"with"};

    AttackMessage(String payload) {
        String[] words = payload.split(" ");
        boolean usedFlags = areFlags(words, prepositionFlags);
        if (usedFlags) { // attack target with weapon
            words = prepositionSeparator(words, prepositionFlags, 2);
            this.targetName += words[0];
            this.weaponName += words[1];
        } else if (words.length >= 2) { // attack weapon target
            this.weaponName += words[0];
            this.targetName += words[1];
        } else {
            this.targetName += words[0];
        }
//        String[] words = prepositionSeparator(payload.split(" "), prepositionFlags, 2);//payload.split(" ");

    }

    public String getWeapon() {
        return weaponName;
    }

    public String getTarget() {
        return targetName;
    }

    @Override
    public String toString() {
        return "Attacking " + this.targetName + " with " + this.weaponName;
    }
}
