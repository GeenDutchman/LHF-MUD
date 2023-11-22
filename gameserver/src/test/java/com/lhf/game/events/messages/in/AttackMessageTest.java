package com.lhf.game.events.messages.in;

import java.util.ArrayList;

import org.junit.Test;

import com.google.common.truth.Truth;
import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandBuilder;
import com.lhf.game.events.messages.CommandMessage;

public class AttackMessageTest {

        private class UnlockedAttackMessage extends AttackMessage {

                UnlockedAttackMessage(String payload) {
                        super(payload);
                }

                public UnlockedAttackMessage addADirect(String direct) {
                        this.directs.add(direct);
                        return this;
                }

                public UnlockedAttackMessage addAnIndirect(String preposition, String phrase) {
                        this.indirects.put(preposition, phrase);
                        return this;
                }

                public UnlockedAttackMessage setValid(boolean valid) {
                        this.isValid = valid;
                        return this;
                }
        }

        @Test
        public void testParseAttack() {
                ArrayList<AttackMessage> desired = new ArrayList<>();
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

                for (AttackMessage am : desired) {
                        System.out.println("Testing: " + am.getWhole());
                        Command cmd = CommandBuilder.parse(am.getWhole());
                        Truth.assertThat(cmd.getGameEventType()).isEqualTo(CommandMessage.ATTACK);
                        Truth.assertThat(cmd).isEqualTo(am);
                }
        }
}
