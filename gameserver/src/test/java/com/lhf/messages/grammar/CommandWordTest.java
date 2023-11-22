package com.lhf.messages.grammar;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.messages.CommandMessage;

public class CommandWordTest {
    private class testcase extends GrammarTestCase {
        public CommandMessage expected;

        public testcase(CommandMessage exp, Boolean valid) {
            super("null", valid); // just in case
            if (exp != null) {
                this.result = exp.toString();
            }
            this.expected = exp;
        }

        public testcase addToken(String token, Boolean acc) {
            super.addToken(token, acc);
            return this;
        }
    }

    @Test
    void testParse() {
        ArrayList<testcase> testcases = new ArrayList<>();
        testcases.add(new testcase(CommandMessage.GO, true).addToken(CommandMessage.GO.toString(), true));
        testcases.add(new testcase(CommandMessage.ATTACK, true).addToken("ATTACK", true));
        testcases.add(new testcase(CommandMessage.ATTACK, true).addToken("attack", true));
        testcases.add(new testcase(CommandMessage.ATTACK, true).addToken("Attack", true));
        testcases.add(new testcase(null, false).addToken(this.getClass().getName(), false));
        testcases.add(new testcase(CommandMessage.SAY, false).addToken("SAY", true).addToken("SHOUT", false));
        testcases.add(new testcase(CommandMessage.SAY, false).addToken("SAY", true).addToken(this.getClass().getName(),
                false));

        for (testcase tcase : testcases) {
            CommandWord cw = new CommandWord();
            System.out.println("Now testing '" + tcase.result + "'");
            Boolean accepted = true;
            try {
                for (int i = 0; i < tcase.tokens.size() && accepted; i++) {
                    accepted = cw.parse(tcase.tokens.get(i));
                    Truth.assertThat(accepted).isEqualTo(tcase.accepted.get(i));
                }
            } catch (IllegalArgumentException iae) {
                System.err.println(iae);
                Truth.assertThat(tcase.valid).isFalse();
            }
            Boolean valid = cw.isValid();
            Truth.assertThat(valid).isEqualTo(tcase.valid);
            if (valid) {
                Truth.assertThat(cw.getCommand()).isEqualTo(tcase.expected);
            }
        }

    }
}
