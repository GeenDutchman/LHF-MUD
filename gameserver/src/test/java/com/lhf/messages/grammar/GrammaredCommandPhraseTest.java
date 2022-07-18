package com.lhf.messages.grammar;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.messages.CommandMessage;

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
                    Truth.assertThat(accepted).isEqualTo(testcase.accepted.get(i));
                }
            } catch (IllegalArgumentException iae) {
                System.err.println(iae);
                Truth.assertThat(testcase.valid).isFalse();
            }
            accepted = gcp.isValid();
            Truth.assertThat(accepted).isEqualTo(testcase.valid);
            if (accepted) {
                Truth.assertThat(gcp.getResult()).isEqualTo(testcase.result);
            }
        }

    }
}
