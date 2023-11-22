package com.lhf.game.events.messages.in;

import java.util.ArrayList;

import org.junit.Test;

import com.google.common.truth.Truth;
import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandBuilder;
import com.lhf.game.events.messages.CommandMessage;

public class CastMessageTest {

        private class UnlockedCastMessage extends CastMessage {

                UnlockedCastMessage(String payload) {
                        super(payload);
                }

                public UnlockedCastMessage addADirect(String direct) {
                        this.directs.add(direct);
                        return this;
                }

                public UnlockedCastMessage addAnIndirect(String preposition, String phrase) {
                        this.indirects.put(preposition, phrase);
                        return this;
                }

                public UnlockedCastMessage setValid(boolean valid) {
                        this.isValid = valid;
                        return this;
                }
        }

        @Test
        public void testParseAttack() {
                ArrayList<CastMessage> desired = new ArrayList<>();
                desired.add(new UnlockedCastMessage("cast turnwaster").addADirect("turnwaster"));
                desired.add(new UnlockedCastMessage("cast \"turnwaster\"").addADirect("\"turnwaster\""));
                desired.add(new UnlockedCastMessage("cast \"at locus with nie\"").addADirect("\"at locus with nie\""));

                desired.add(new UnlockedCastMessage("cast turnwaster at boblin").addADirect("turnwaster").addAnIndirect(
                                "at",
                                "boblin"));
                desired.add(new UnlockedCastMessage("cast \"turnwaster\" at boblin").addADirect("\"turnwaster\"")
                                .addAnIndirect("at", "boblin"));
                desired.add(new UnlockedCastMessage("cast \"at locus with nie\" at boblin")
                                .addADirect("\"at locus with nie\"")
                                .addAnIndirect("at", "boblin"));

                desired.add(
                                new UnlockedCastMessage("cast turnwaster with 3").addADirect("turnwaster")
                                                .addAnIndirect("with", "3"));
                desired.add(new UnlockedCastMessage("cast \"turnwaster\" with 3").addADirect("\"turnwaster\"")
                                .addAnIndirect("with", "3"));
                desired.add(new UnlockedCastMessage("cast \"at locus with nie\" with 3")
                                .addADirect("\"at locus with nie\"")
                                .addAnIndirect("with", "3"));

                desired.add(
                                new UnlockedCastMessage("cast turnwaster at boblin with 3").addADirect("turnwaster")
                                                .addAnIndirect("at",
                                                                "boblin")
                                                .addAnIndirect("with", "3"));
                desired.add(new UnlockedCastMessage("cast \"turnwaster\" at boblin with 3").addADirect("\"turnwaster\"")
                                .addAnIndirect("at", "boblin").addAnIndirect("with", "3"));
                desired.add(new UnlockedCastMessage("cast \"at locus with nie\" at boblin with 3")
                                .addADirect("\"at locus with nie\"")
                                .addAnIndirect("at", "boblin").addAnIndirect("with", "3"));

                desired.add(
                                new UnlockedCastMessage("cast turnwaster with 3 at boblin").addADirect("turnwaster")
                                                .addAnIndirect("with", "3").addAnIndirect("at", "boblin"));
                desired.add(new UnlockedCastMessage("cast \"turnwaster\" with 3 at boblin").addADirect("\"turnwaster\"")
                                .addAnIndirect("with", "3").addAnIndirect("at", "boblin"));
                desired.add(new UnlockedCastMessage("cast \"at locus with nie\" with 3 at boblin")
                                .addADirect("\"at locus with nie\"")
                                .addAnIndirect("with", "3").addAnIndirect("at", "boblin"));

                desired.add(new UnlockedCastMessage("cast \"at locus with nie\", \"at locus with nie\"").setValid(false)
                                .addADirect("\"at locus with nie\"").addADirect("\"at locus with nie\""));

                for (CastMessage am : desired) {
                        System.out.println("Testing: " + am.getWhole());
                        Command cmd = CommandBuilder.parse(am.getWhole());
                        Truth.assertThat(cmd.getType()).isEqualTo(CommandMessage.CAST);
                        Truth.assertThat(cmd).isEqualTo(am);
                }
        }
}
