package com.lhf.messages.in;


public class AttackMessage extends InMessage {
    private String weaponName;
    private String targetName;

    static final private String prepositionFlag = "with";

    public AttackMessage(String payload) {
        String[] words = payload.split(" ");
        if (words.length > 2 && prepositionFlag.equals(words[1])) { // attack monster with weapon
            this.targetName = words[0];
            this.weaponName = words[2];
        } else if (words.length == 1) { // attack monster
            this.targetName = words[0];
            this.weaponName = "";
        } else { // attack weapon monster <blah blah blah ignored>
            this.weaponName = words[0];
            this.targetName = words[1];
        }
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
