package com.lhf.messages.grammar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import com.lhf.messages.CommandMessage;

import org.junit.jupiter.api.Test;

public class CommandWordTest {
    private class testcase {
        public ArrayList<String> tokens;
        public ArrayList<Boolean> accepted;
        public Boolean valid;
        public CommandMessage expected;

        public testcase(CommandMessage exp, Boolean valid) {
            this.valid = valid;
            this.expected = exp;
            this.tokens = new ArrayList<>();
            this.accepted = new ArrayList<>();
        }

        public testcase addToken(String token, Boolean acc) {
            this.tokens.add(token);
            this.accepted.add(acc);
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
        testcases.add(new testcase(CommandMessage.DROP, true).addToken(this.getClass().toString(), false)
                .addToken(CommandMessage.DROP.toString(), true));

        for (testcase tcase : testcases) {
            CommandWord cw = new CommandWord();
            Boolean accepted = true;
            for (int i = 0; i < tcase.tokens.size() && accepted; i++) {
                accepted = cw.parse(tcase.tokens.get(i));
                assertEquals(tcase.accepted.get(i), accepted);
            }
            Boolean valid = cw.isValid();
            assertEquals(tcase.valid, valid);
            if (valid) {
                assertEquals(tcase.expected, cw.getCommand());
            }
        }

    }
}
