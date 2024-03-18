package com.lhf.messages;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.google.common.truth.Truth;
import com.lhf.messages.grammar.Prepositions;
import com.lhf.messages.in.AMessageType;

public class CommandBuilderTest {
    class ParseTestCase {
        public final String testName;
        public final String input;
        public final Boolean isValid;
        public final AMessageType type;
        public final List<String> directs;
        public final EnumMap<Prepositions, List<String>> indirects;

        public ParseTestCase(String testName, String input, Boolean isValid,
                AMessageType type) {
            this.testName = testName;
            this.input = input;
            this.isValid = isValid;
            this.directs = new ArrayList<>();
            this.indirects = new EnumMap<>(Prepositions.class);
            this.type = type;
        }

        public ParseTestCase addDirect(String direct) {
            this.directs.add(direct);
            return this;
        }

        public ParseTestCase addPrepPhrase(Prepositions preposition, String phrase) {
            this.indirects.compute(preposition, (prep, listy) -> {
                if (listy == null) {
                    listy = new ArrayList<>();
                }
                listy.add(phrase);
                return listy;
            });
            return this;
        }

        public DynamicTest toDynamicTest() {
            return DynamicTest.dynamicTest(this.testName, this::execute);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ParseTestCase [testName=").append(testName).append(", input=").append(input)
                    .append(", isValid=").append(isValid).append(", type=").append(type)
                    .append(", directs=").append(directs).append(", indirects=").append(indirects)
                    .append("]");
            return builder.toString();
        }

        public void execute() {
            System.out.println("Testing: " + this.testName);
            final Command cmd = Command.parse(this.input);
            Truth.assertWithMessage("Expected '%s' to not make null command", this).that(cmd)
                    .isNotNull();
            System.out.println("Recieved: " + cmd.toString());
            Truth.assertWithMessage("Test case '%s' input should match parsed '%s' whole", this, cmd)
                    .that(cmd.getWhole()).isEqualTo(this.input);
            Truth.assertWithMessage("Parsed command '%s' should be just as valid as test case '%s'", cmd,
                    this).that(cmd.isValid()).isEqualTo(this.isValid);
            if (this.isValid) {
                Truth.assertWithMessage("Command types should be the same for test case '%s'",
                        this.testName).that(cmd.getType())
                        .isEqualTo(this.type);
                Truth.assertWithMessage("Directs should be the same for test case '%s'", this.testName)
                        .that(cmd.getDirects()).containsExactlyElementsIn(this.directs)
                        .inOrder();
                Truth.assertWithMessage("Inirects should be the same for test case '%s'", this.testName)
                        .that(cmd.getIndirects()).containsExactlyEntriesIn(this.indirects)
                        .inOrder();
            }
        }

    }

    @Test
    public void testTokenize() {
        String pattern = "\\w+|[^\\s]|\\s+";
        Pattern splitter = Pattern.compile(pattern);
        String toSplit = "I'm not sure \"hello\"";
        Matcher matcher = splitter.matcher(toSplit);
        while (matcher.find()) {
            System.out.println(matcher.group());
        }
    }

    @TestFactory
    Stream<DynamicTest> testParse() {
        ArrayList<ParseTestCase> testCases = new ArrayList<>();
        testCases.add(new ParseTestCase("Command only, CAPS", "SAY", false,
                AMessageType.SAY));
        testCases.add(new ParseTestCase("Command only, lower", "equip", false,
                AMessageType.EQUIP));
        testCases.add(
                new ParseTestCase("Command only, enum", AMessageType.ATTACK.toString(),
                        false, AMessageType.ATTACK));
        testCases.add(new ParseTestCase("Not command", "Zirtech", false, null));
        testCases.add(
                new ParseTestCase("Single direct object", "Say hello", true,
                        AMessageType.SAY).addDirect("hello"));
        testCases.add(new ParseTestCase("Quoted direct object", "Say \"hello\"",
                true, AMessageType.SAY)
                .addDirect("\"hello\""));
        testCases.add(new ParseTestCase("Quoted direct object with preposition",
                "Say\"hello to my little friend\"",
                true, AMessageType.SAY).addDirect("\"hello to my little friend\""));
        testCases.add(new ParseTestCase("Quoted direct object with preposition",
                "Say \"hello to my little friend\" to arnold", true, AMessageType.SAY)
                .addDirect("\"hello to my little friend\"").addPrepPhrase(Prepositions.TO,
                        "arnold"));
        testCases.add(new ParseTestCase("Quoted direct object with preposition and punctuation",
                "Say \"hello there!\" to arnold", true,
                AMessageType.SAY).addDirect("\"hello there!\"")
                .addPrepPhrase(Prepositions.TO, "arnold"));
        testCases.add(
                new ParseTestCase("Quoted comma list", "Say \"one, two, three\" to arnold",
                        true, AMessageType.SAY)
                        .addDirect("\"one, two, three\"")
                        .addPrepPhrase(Prepositions.TO, "arnold"));
        testCases.add(new ParseTestCase("Trailing quoted space", "say \"one \"",
                true, AMessageType.SAY)
                .addDirect("\"one \""));
        testCases.add(new ParseTestCase("Posessive preposition", "Take longsword from John's corpse", false,
                AMessageType.TAKE).addDirect("longsword")
                .addPrepPhrase(Prepositions.FROM, "John's corpse"));
        testCases.add(new ParseTestCase("Quoted Posessive preposition", "Take longsword from \"John's corpse\"",
                true,
                AMessageType.TAKE).addDirect("longsword")
                .addPrepPhrase(Prepositions.FROM, "\"John's corpse\""));

        return testCases.stream().map(testCase -> testCase.toDynamicTest());

    }
}
