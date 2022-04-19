package com.lhf.messages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

public class PrepositionalPhrasesTest {
    private class testcase {
        public ArrayList<String> tokens;
        public ArrayList<Boolean> accepted;
        public String result;
        public Boolean valid;
        public Boolean allowList;

        public testcase(String result, Boolean valid) {
            this.result = result;
            this.valid = valid;
            this.tokens = new ArrayList<>();
            this.accepted = new ArrayList<>();
            this.allowList = false;
        }

        public testcase(String result, Boolean valid, Boolean allowList) {
            this.result = result;
            this.valid = valid;
            this.tokens = new ArrayList<>();
            this.accepted = new ArrayList<>();
            this.allowList = allowList;
        }

        public testcase addWord(String word, Boolean acc) {
            this.tokens.add(word);
            this.accepted.add(acc);
            return this;
        }
    }

    @Test
    void testParse() {
        HashSet<String> preps = new HashSet<>();
        preps.add("to");
        preps.add("at");

        ArrayList<testcase> testcases = new ArrayList<>();
        testcases.add(new testcase("", false).addWord("", false));
        testcases.add(new testcase("blargh", false).addWord("blargh", false));
        testcases.add(new testcase("to", false).addWord("to", true));
        testcases.add(new testcase("to john", true).addWord("to",
                true).addWord("john", true));
        testcases.add(new testcase("to john at third", true).addWord("to",
                true).addWord("john", true)
                .addWord("at", true).addWord("third", true));
        testcases.add(new testcase("to john to mary", false).addWord("to",
                true).addWord("john", true)
                .addWord("to", false).addWord("mary", false));
        testcases.add(new testcase("to john to mary", true, true).addWord("to",
                true).addWord("john", true)
                .addWord("to", true).addWord("mary", true));
        testcases.add(new testcase("to john, mary", true, true).addWord("to", true).addWord("john", true)
                .addWord(",", true).addWord("mary", true));
        testcases.add(new testcase("to john, mary", false, false).addWord("to", true).addWord("john", true)
                .addWord(",", true).addWord("mary", true));
        testcases.add(new testcase("to john 'to mary'", true).addWord("to", true).addWord("john", true)
                .addWord("'", true).addWord("to", true).addWord("mary", true).addWord("'", true));

        for (testcase tcase : testcases) {
            PrepositionalPhrases phrases = new PrepositionalPhrases(preps, tcase.allowList);
            System.out.print("Testing: '" + tcase.result + "' ");
            Boolean accepted = true;

            for (int i = 0; i < tcase.tokens.size() && accepted; i++) {
                accepted = phrases.parse(tcase.tokens.get(i));
                assertEquals(tcase.accepted.get(i), accepted);
            }
            assertEquals(tcase.valid, phrases.isValid());
            if (phrases.isValid()) {
                String resultString = phrases.getResult();
                System.out.println("'" + resultString + "'");
                for (PhraseList p : phrases.phraseMap.values()) {
                    String innerResult = p.getResult();
                    System.out.println("\t-" + innerResult);
                    assertTrue(resultString.contains(innerResult));
                }
            } else {
                System.out.print(" INVALID ");
                System.out.println("'" + phrases.getResult() + "'");
            }
        }

    }
}
