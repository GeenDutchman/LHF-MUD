package com.lhf.messages.grammar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;

import com.lhf.messages.CommandMessage;

import org.junit.jupiter.api.Test;

public class GrammaredCommandPhraseTest {

    @Test
    void testParse() {
        ArrayList<GrammarTestCase> testcases = new ArrayList<>();
        testcases.add(new GrammarTestCase("GO east", true).addToken(CommandMessage.GO.toString(), true).addToken("east",
                true));
        testcases.add(new GrammarTestCase("GO east", true).addToken("go", true).addToken("east", true));
        testcases.add(new GrammarTestCase("SEE", true).addToken("see", true));
        testcases.add(new GrammarTestCase("blarch SEE", false).addToken("blarch", false).addToken("see", true));
        testcases.add(new GrammarTestCase("SAY 'say hello to my', joe to joe", true).addToken("say", true)
                .addToken("'", true).addToken("say", true).addToken("hello", true).addToken("to", true)
                .addToken("my", true).addToken("'", true).addToken(",", true).addToken("joe", true).addToken("to", true)
                .addToken("joe", true));

        for (GrammarTestCase testcase : testcases) {
            System.out.println("Testing: " + testcase.result);
            GrammaredCommandPhrase gcp = new GrammaredCommandPhrase();

            Boolean accepted = true;
            try {
                for (int i = 0; i < testcase.tokens.size() && accepted; i++) {
                    accepted = gcp.parse(testcase.tokens.get(i));
                    assertEquals(testcase.accepted.get(i), accepted);
                }
            } catch (IllegalArgumentException iae) {
                System.err.println(iae);
                assertFalse(testcase.valid);
            }
            accepted = gcp.isValid();
            assertEquals(testcase.valid, accepted);
            if (accepted) {
                assertEquals(testcase.result, gcp.getResult());
            }
        }

    }
}
