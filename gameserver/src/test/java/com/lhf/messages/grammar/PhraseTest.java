package com.lhf.messages.grammar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

public class PhraseTest {

    @Test
    void testParse() {
        ArrayList<GrammarTestCase> testcases = new ArrayList<>();
        // testcases.add(new GrammarTestCase("oneword", true).addToken("oneword",
        // true));
        testcases.add(new GrammarTestCase("oneword twoword", true).addToken("oneword", true).addToken("twoword", true));
        testcases.add(new GrammarTestCase("oneword 'midone midtwo' twoword", true).addToken("oneword", true)
                .addToken("'", true)
                .addToken("midone", true).addToken("midtwo", true).addToken("'", true).addToken("twoword", true));
        testcases.add(new GrammarTestCase("oneword 'openquote", false).addToken("oneword", true).addToken("'", true)
                .addToken("openquote", true));
        testcases.add(new GrammarTestCase("here we go"/* to there" */, true).addToken("here", true).addToken("we", true)
                .addToken("go", true).addToken("to", false).addToken("there", false));
        testcases.add(new GrammarTestCase("oneword (midone midtwo) twoword", true).addToken("oneword", true)
                .addToken("(", true)
                .addToken("midone", true).addToken("midtwo", true).addToken(")", true).addToken("twoword", true));

        HashSet<String> preps = new HashSet<>();
        preps.add("to");
        preps.add("at");
        preps.add(",");

        for (GrammarTestCase tcase : testcases) {
            Phrase p = new Phrase(preps);

            Boolean accepted = true;
            for (int i = 0; i < tcase.tokens.size() && accepted; i++) {
                accepted = p.parse(tcase.tokens.get(i));
                assertEquals(tcase.accepted.get(i), accepted);
            }
            assertEquals(tcase.valid, p.isValid());
            if (p.isValid()) {
                assertEquals(tcase.result, p.getResult());
            }
        }
    }
}
