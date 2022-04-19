package com.lhf.messages.grammar;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;

import com.lhf.messages.grammar.Phrase;

import org.junit.jupiter.api.Test;

public class PhraseTest {
    private class testcase {
        public ArrayList<String> testphrase;
        public ArrayList<Boolean> accepted;
        public String expectedphrase;
        public Boolean expectedvalid;

        public testcase(String expectedphrase, Boolean expectedvalid) {
            this.expectedphrase = expectedphrase;
            this.expectedvalid = expectedvalid;
            this.testphrase = new ArrayList<>();
            this.accepted = new ArrayList<>();
        }

        public testcase addWord(String word, Boolean acc) {
            this.testphrase.add(word);
            this.accepted.add(acc);
            return this;
        }
    }

    @Test
    void testParse() {
        ArrayList<testcase> testcases = new ArrayList<>();
        // testcases.add(new testcase("oneword", true).addWord("oneword", true));
        testcases.add(new testcase("oneword twoword", true).addWord("oneword", true).addWord("twoword", true));
        testcases.add(new testcase("oneword 'midone midtwo' twoword", true).addWord("oneword", true).addWord("'", true)
                .addWord("midone", true).addWord("midtwo", true).addWord("'", true).addWord("twoword", true));
        testcases.add(new testcase("oneword 'openquote", false).addWord("oneword", true).addWord("'", true)
                .addWord("openquote", true));
        testcases.add(new testcase("here we go"/* to there" */, true).addWord("here", true).addWord("we", true)
                .addWord("go", true).addWord("to", false).addWord("there", false));
        testcases.add(new testcase("oneword (midone midtwo) twoword", true).addWord("oneword", true).addWord("(", true)
                .addWord("midone", true).addWord("midtwo", true).addWord(")", true).addWord("twoword", true));

        HashSet<String> preps = new HashSet<>();
        preps.add("to");
        preps.add("at");
        preps.add(",");

        for (testcase tcase : testcases) {
            Phrase p = new Phrase(preps);

            Boolean accepted = true;
            for (int i = 0; i < tcase.testphrase.size() && accepted; i++) {
                accepted = p.parse(tcase.testphrase.get(i));
                assertEquals(tcase.accepted.get(i), accepted);
            }
            assertEquals(tcase.expectedvalid, p.isValid());
            if (p.isValid()) {
                assertEquals(tcase.expectedphrase, p.getResult());
            }
        }
    }
}
