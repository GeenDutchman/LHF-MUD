package com.lhf.messages.grammar;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

public class PrepositionalPhrasesTest {
    private class testcase extends GrammarTestCase {
        public Boolean allowList;

        public testcase(String result, Boolean valid) {
            super(result, valid);
            this.allowList = false;
        }

        public testcase(String result, Boolean valid, Boolean allowList) {
            super(result, valid);
            this.allowList = allowList;
        }

        public testcase addToken(String token, Boolean acc) {
            super.addToken(token, acc);
            return this;
        }
    }

    @Test
    void testParse() {
        HashSet<String> preps = new HashSet<>();
        preps.add("to");
        preps.add("at");

        ArrayList<testcase> testcases = new ArrayList<>();
        testcases.add(new testcase("", false).addToken("", false));
        testcases.add(new testcase("blargh", false).addToken("blargh", false));
        testcases.add(new testcase("to", false).addToken("to", true));
        testcases.add(new testcase("to john", true).addToken("to",
                true).addToken("john", true));
        testcases.add(new testcase("to john at third", true).addToken("to",
                true).addToken("john", true)
                .addToken("at", true).addToken("third", true));
        testcases.add(new testcase("to john to mary", false).addToken("to",
                true).addToken("john", true)
                .addToken("to", false).addToken("mary", false));
        testcases.add(new testcase("to john to mary", true, true).addToken("to",
                true).addToken("john", true)
                .addToken("to", true).addToken("mary", true));
        testcases.add(new testcase("to john, mary", true, true).addToken("to", true).addToken("john", true)
                .addToken(",", true).addToken("mary", true));
        testcases.add(new testcase("to john, mary", false, false).addToken("to", true).addToken("john", true)
                .addToken(",", true).addToken("mary", true));
        testcases.add(new testcase("to john 'to mary'", true).addToken("to", true).addToken("john", true)
                .addToken("'", true).addToken("to", true).addToken("mary", true).addToken("'", true));

        for (testcase tcase : testcases) {
            PrepositionalPhrases phrases = new PrepositionalPhrases(preps, tcase.allowList);
            System.out.print("Testing: '" + tcase.result + "' ");
            Boolean accepted = true;

            for (int i = 0; i < tcase.tokens.size() && accepted; i++) {
                accepted = phrases.parse(tcase.tokens.get(i));
                Truth.assertThat(accepted).isEqualTo(tcase.accepted.get(i));
            }
            Truth.assertThat(phrases.isValid()).isEqualTo(tcase.valid);
            if (phrases.isValid()) {
                String resultString = phrases.getResult();
                System.out.println("'" + resultString + "'");
                for (PhraseList p : phrases.phraseMap.values()) {
                    String innerResult = p.getResult();
                    System.out.println("\t-" + innerResult);
                    Truth.assertThat(resultString).contains(innerResult);
                }
            } else {
                System.out.print(" INVALID ");
                System.out.println("'" + phrases.getResult() + "'");
            }
        }

    }
}
