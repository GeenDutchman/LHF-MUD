package com.lhf.messages.in;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.junit.Test;

import com.google.common.truth.Truth;
import com.lhf.messages.Command;
import com.lhf.messages.grammar.Prepositions;

public class AttackMessageTest {

        private class UnlockedAttackMessage {
                protected final String whole;
                protected final List<String> directs;
                protected final EnumMap<Prepositions, String> indirects;
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
                        this.indirects.put(Prepositions.valueOf(phrase), phrase);
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

                public EnumMap<Prepositions, String> getIndirects() {
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
                        Truth.assertThat(cmd.getType()).isEqualTo(AMessageType.ATTACK);
                        Truth.assertThat(cmd.getWhole()).isEqualTo(am.getWhole());
                        Truth.assertThat(cmd.getIndirects()).isEqualTo(am.getIndirects());
                        Truth.assertThat(cmd.getDirects()).isEqualTo(am.getDirects());
                        Truth.assertThat(cmd.isValid()).isEqualTo(am.isValid());
                }
        }
}
