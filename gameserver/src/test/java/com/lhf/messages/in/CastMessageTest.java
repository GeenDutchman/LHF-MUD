package com.lhf.messages.in;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import org.junit.Test;

import com.google.common.truth.Truth;
import com.lhf.messages.Command;
import com.lhf.messages.grammar.Prepositions;

public class CastMessageTest {

        private class UnlockedCastMessage {
                protected final String whole;
                protected final List<String> directs;
                protected final EnumMap<Prepositions, String> indirects;
                protected boolean isValid;

                UnlockedCastMessage(String payload) {
                        this.whole = payload;
                        this.directs = new ArrayList<>();
                        this.indirects = new EnumMap<>(Prepositions.class);
                        this.isValid = true;
                }

                public UnlockedCastMessage addADirect(String direct) {
                        this.directs.add(direct);
                        return this;
                }

                public UnlockedCastMessage addAnIndirect(String preposition, String phrase) {
                        this.indirects.put(Prepositions.getPreposition(preposition), phrase);
                        return this;
                }

                public UnlockedCastMessage setValid(boolean valid) {
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

                @Override
                public String toString() {
                        StringBuilder builder = new StringBuilder();
                        builder.append("UnlockedCastMessage [whole=").append(whole).append(", directs=").append(directs)
                                        .append(", indirects=").append(indirects).append(", isValid=").append(isValid)
                                        .append("]");
                        return builder.toString();
                }

        }

        @Test
        public void testParseAttack() {
                ArrayList<UnlockedCastMessage> desired = new ArrayList<>();
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

                for (UnlockedCastMessage am : desired) {
                        System.out.println("Testing: " + am.getWhole());
                        Command cmd = Command.parse(am.getWhole());
                        Truth.assertThat(cmd.getType()).isEqualTo(AMessageType.CAST);
                        Truth.assertWithMessage("test case '%s' command '%s'", am, cmd).that(cmd.getWhole())
                                        .isEqualTo(am.getWhole());
                        Truth.assertWithMessage("test case '%s' command '%s'", am, cmd).that(cmd.getIndirects())
                                        .isEqualTo(am.getIndirects());
                        Truth.assertWithMessage("test case '%s' command '%s'", am, cmd).that(cmd.getDirects())
                                        .isEqualTo(am.getDirects());
                        Truth.assertWithMessage("test case '%s' command '%s'", am, cmd).that(cmd.isValid())
                                        .isEqualTo(am.isValid());
                }
        }
}
