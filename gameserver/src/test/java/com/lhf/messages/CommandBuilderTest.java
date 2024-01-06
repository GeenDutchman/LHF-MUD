package com.lhf.messages;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import com.google.common.truth.Truth;
import com.lhf.messages.in.CommandAdapter;
import com.lhf.messages.in.AMessageType;

public class CommandBuilderTest {
        class ParseTestCase {
                public String testName;
                public String input;
                public Command command;
                public Boolean isValid;

                public ParseTestCase(String testName, String input, Boolean isValid,
                                AMessageType type) {
                        this.testName = testName;
                        this.input = input;
                        this.isValid = isValid;
                        this.command = CommandAdapter.fromCommand(type, input);
                        if (this.command != null) {
                                this.command = this.command.setValid(isValid);
                        }
                }

                public ParseTestCase addDirect(String direct) {
                        this.command.addDirect(direct);
                        return this;
                }

                public ParseTestCase addPrepPhrase(String preposition, String phrase) {
                        this.command.addIndirect(preposition, phrase);
                        return this;
                }

                public DynamicTest toDynamicTest() {
                        return DynamicTest.dynamicTest(this.testName, this::execute);
                }

                public void execute() {
                        System.out.println("Testing: " + this.testName);
                        Command cmd = CommandBuilder.parse(this.input);
                        Truth.assertWithMessage("Expected '%s' to not make null command", this.input).that(cmd)
                                        .isNotNull();
                        System.out.println("Recieved: " + cmd.toString());
                        if (this.command != null) {
                                System.out.println("Expected: " + this.command.toString());
                                Truth.assertWithMessage(
                                                "Expected validation of command '%s' -> '%s' to be %s, but was not.",
                                                this.input, this.command.toString(), this.command.isValid())
                                                .that(cmd.isValid())
                                                .isEqualTo(this.command.isValid());
                        } else {
                                Truth.assertWithMessage("Expected validation of command '%s' to be %s, but it was not.",
                                                this.input,
                                                this.isValid)
                                                .that(cmd.isValid()).isEqualTo(this.isValid);
                        }
                        if (cmd.isValid) {
                                Truth.assertWithMessage("Expected commands to match, but they were not.").that(cmd)
                                                .isEqualTo(this.command);
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
                testCases.add(new ParseTestCase("Command only, CAPS", "SAY", true,
                                AMessageType.SAY));
                testCases.add(new ParseTestCase("Command only, lower", "equip", true,
                                AMessageType.EQUIP));
                testCases.add(
                                new ParseTestCase("Command only, enum", AMessageType.ATTACK.toString(),
                                                true, AMessageType.ATTACK));
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
                                .addDirect("\"hello to my little friend\"").addPrepPhrase("to", "arnold"));
                testCases.add(new ParseTestCase("Quoted direct object with preposition and punctuation",
                                "Say \"hello there!\" to arnold", true,
                                AMessageType.SAY).addDirect("\"hello there!\"")
                                .addPrepPhrase("to", "arnold"));
                testCases.add(
                                new ParseTestCase("Quoted comma list", "Say \"one, two, three\" to arnold",
                                                true, AMessageType.SAY)
                                                .addDirect("\"one, two, three\"").addPrepPhrase("to", "arnold"));
                testCases.add(new ParseTestCase("Trailing quoted space", "say \"one \"", true, AMessageType.SAY)
                                .addDirect("\"one \""));
                testCases.add(new ParseTestCase("Posessive preposition", "Take longsword from John's corpse", false,
                                AMessageType.TAKE).addDirect("longsword").addPrepPhrase("from", "John's corpse"));
                testCases.add(new ParseTestCase("Quoted Posessive preposition", "Take longsword from \"John's corpse\"",
                                true,
                                AMessageType.TAKE).addDirect("longsword").addPrepPhrase("from", "\"John's corpse\""));

                return testCases.stream().map(testCase -> testCase.toDynamicTest());

        }
}
