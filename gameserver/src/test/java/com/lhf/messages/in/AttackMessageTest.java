package com.lhf.messages.in;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.truth.Truth;
import com.lhf.messages.Command;
import com.lhf.messages.grammar.Prepositions;

public class AttackMessageTest {

    private class UnlockedAttackMessage {
        protected final String whole;
        protected final List<String> directs;
        protected final EnumMap<Prepositions, List<String>> indirects;
        protected boolean isValid;

        UnlockedAttackMessage(String payload) {
            this.whole = payload;
            this.directs = new ArrayList<>();
            this.indirects = new EnumMap<>(Prepositions.class);
            this.isValid = true;
        }

        public UnlockedAttackMessage addADirect(String direct) {
            this.directs.add(direct);
            return this;
        }

        public UnlockedAttackMessage addAnIndirect(String preposition, String phrase) {
            this.indirects.compute(Prepositions.getPreposition(preposition), (prep, phraseList) -> {
                if (phraseList == null) {
                    List<String> next = new ArrayList<>();
                    if (!(phrase == null || phrase.isEmpty() || phrase.isBlank())) {
                        next.add(phrase);
                    }
                    return next;
                }
                if (!(phrase == null || phrase.isEmpty() || phrase.isBlank())) {
                    phraseList.add(phrase);
                }
                return phraseList;
            });
            return this;
        }

        public UnlockedAttackMessage setValid(boolean valid) {
            this.isValid = valid;
            return this;
        }

        public String getWhole() {
            return whole;
        }

        public List<String> getDirects() {
            return directs;
        }

        public Map<Prepositions, List<String>> getIndirects() {
            return indirects;
        }

        public boolean isValid() {
            return isValid;
        }

    }

    @Test
    public void testParseAttack() {
        ArrayList<UnlockedAttackMessage> desired = new ArrayList<>();
        desired.add(new UnlockedAttackMessage("attack goblin").addADirect("goblin"));
        desired.add(new UnlockedAttackMessage("attack goblin with sword")
                .addADirect("goblin").addAnIndirect("with", "sword"));
        desired.add(new UnlockedAttackMessage("attack \"boblin the goblin\"")
                .addADirect("\"boblin the goblin\""));
        desired.add(new UnlockedAttackMessage("attack \"boblin the goblin\" with sword")
                .addADirect("\"boblin the goblin\"").addAnIndirect("with", "sword"));
        desired.add(new UnlockedAttackMessage("attack \"boblin the goblin\", \"morc the orc\"")
                .addADirect("\"boblin the goblin\"").addADirect("\"morc the orc\""));
        desired.add(new UnlockedAttackMessage("attack \"boblin the goblin\", \"morc the orc\" with sword")
                .addADirect("\"boblin the goblin\"").addADirect("\"morc the orc\"")
                .addAnIndirect("with", "sword"));
        desired.add(new UnlockedAttackMessage("attack \"boblin the goblin\", \"boblin the goblin\"")
                .addADirect("\"boblin the goblin\"").addADirect("\"boblin the goblin\""));
        desired.add(new UnlockedAttackMessage("attack \"boblin the goblin\", \"boblin the goblin\" with sword")
                .addADirect("\"boblin the goblin\"").addADirect("\"boblin the goblin\"")
                .addAnIndirect("with", "sword"));
        desired.add(new UnlockedAttackMessage("attack boblin the goblin with sword")
                .addADirect("boblin the goblin").addAnIndirect("with", "sword"));
        desired.add(new UnlockedAttackMessage("attack boblin the goblin, morc the orc with sword")
                .addADirect("boblin the goblin").addADirect("morc the orc")
                .addAnIndirect("with", "sword"));
        desired.add(new UnlockedAttackMessage("attack boblin with eyes with sword").setValid(false)
                .addADirect("boblin")
                .addAnIndirect("with", "eyes"));
        desired.add(new UnlockedAttackMessage("attack goblin with with sword").setValid(false)
                .addADirect("goblin").addAnIndirect("with", ""));
        desired.add(new UnlockedAttackMessage("attack goblin with sword with bow").setValid(false)
                .addADirect("goblin").addAnIndirect("with", "sword"));
        desired.add(new UnlockedAttackMessage("attack \"goblin with eyes\"")
                .addADirect("\"goblin with eyes\""));
        desired.add(new UnlockedAttackMessage("attack \"goblin with eyes\" with sword")
                .addADirect("\"goblin with eyes\"").addAnIndirect("with", "sword"));

        for (UnlockedAttackMessage am : desired) {
            System.out.println("Testing: " + am.getWhole());
            Command cmd = Command.parse(am.getWhole());
            Truth.assertWithMessage(am.getWhole()).that(cmd.getType()).isEqualTo(AMessageType.ATTACK);
            Truth.assertWithMessage(am.getWhole()).that(cmd.getWhole()).isEqualTo(am.getWhole());
            Truth.assertWithMessage(am.getWhole()).that(cmd.getIndirects()).containsExactlyEntriesIn(am.getIndirects());
            Truth.assertWithMessage(am.getWhole()).that(cmd.getDirects()).isEqualTo(am.getDirects());
            Truth.assertWithMessage(am.getWhole()).that(cmd.isValid()).isEqualTo(am.isValid());
        }
    }
}
