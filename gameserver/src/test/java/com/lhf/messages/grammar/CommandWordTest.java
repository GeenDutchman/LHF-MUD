package com.lhf.messages.grammar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;

import com.lhf.messages.CommandMessage;

import org.junit.jupiter.api.Test;

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
        testcases.add(new testcase(null, false).addToken(this.getClass().toString(), false));
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
                    assertEquals(tcase.accepted.get(i), accepted);
                }
            } catch (IllegalArgumentException iae) {
                System.err.println(iae);
                assertFalse(tcase.valid);
            }
            Boolean valid = cw.isValid();
            assertEquals(tcase.valid, valid);
            if (valid) {
                assertEquals(tcase.expected, cw.getCommand());
            }
        }

    }
}
