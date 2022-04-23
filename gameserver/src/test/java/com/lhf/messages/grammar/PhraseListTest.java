package com.lhf.messages.grammar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

public class PhraseListTest {

    @Test
    void testParse() {
        ArrayList<GrammarTestCase> testcases = new ArrayList<>();
        testcases.add(new GrammarTestCase("oneword", true).addToken("oneword", true));
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
        testcases.add(
                new GrammarTestCase("hello, there", true).addToken("hello", true).addToken(",", true).addToken("there",
                        true));
        testcases.add(new GrammarTestCase("", false).addToken(",", true));
        testcases.add(new GrammarTestCase("", false).addToken(",", true).addToken(",", true).addToken(",", true));
        testcases.add(new GrammarTestCase("I, 'have no', bananas for you", true).addToken("I", true).addToken(",", true)
                .addToken("'", true)
                .addToken("have", true).addToken("no", true).addToken("'", true).addToken(",", true)
                .addToken("bananas", true).addToken("for", true).addToken("you", true));

        HashSet<String> preps = new HashSet<>();
        preps.add("to");
        preps.add("at");

        for (GrammarTestCase tcase : testcases) {
            PhraseList phList = new PhraseList(preps);

            Boolean accepted = true;
            for (int i = 0; i < tcase.tokens.size() && accepted; i++) {
                accepted = phList.parse(tcase.tokens.get(i));
                assertEquals(tcase.accepted.get(i), accepted);
            }
            assertEquals(tcase.valid, phList.isValid());
            if (phList.isValid()) {
                assertEquals(tcase.result, phList.getResult());
            }
        }
    }
}
