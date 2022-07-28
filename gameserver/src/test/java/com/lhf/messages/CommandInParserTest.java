package com.lhf.messages;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.messages.in.InMessage;

public class CommandInParserTest {
    class ParseTestCase {
        public String testName;
        public String input;
        public Command command;
        public Boolean isValid;

        public ParseTestCase(String testName, String input, Boolean isValid,
                CommandMessage type) {
            this.testName = testName;
            this.input = input;
            this.isValid = isValid;
            this.command = InMessage.fromCommand(type, input);
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

    }

    @Test
    void testParse() {
        ArrayList<ParseTestCase> testCases = new ArrayList<>();
        testCases.add(new ParseTestCase("Command only, CAPS", "SAY", true, CommandMessage.SAY));
        testCases.add(new ParseTestCase("Command only, lower", "equip", true, CommandMessage.EQUIP));
        testCases.add(
                new ParseTestCase("Command only, enum", CommandMessage.ATTACK.toString(), true, CommandMessage.ATTACK));
        testCases.add(new ParseTestCase("Not command", "Zirtech", false, null));
        testCases.add(
                new ParseTestCase("Single direct object", "Say hello", true, CommandMessage.SAY).addDirect("hello"));
        testCases.add(new ParseTestCase("Quoted direct object", "Say \"hello\"", true, CommandMessage.SAY)
                .addDirect("\"hello\""));
        testCases.add(new ParseTestCase("Quoted direct object with preposition", "Say\"hello to my little friend\"",
                true, CommandMessage.SAY).addDirect("\"hello to my little friend\""));
        testCases.add(new ParseTestCase("Quoted direct object with preposition",
                "Say \"hello to my little friend\" to arnold", true, CommandMessage.SAY)
                .addDirect("\"hello to my little friend\"").addPrepPhrase("to", "arnold"));
        // TODO: we should take better care of punctuation
        // testCases.add(new ParseTestCase("Quoted direct object with preposition and
        // punctuation",
        // "Say \"hello there!\" to arnold", true,
        // CommandMessage.SAY).addDirect("\"hello there!\"")
        // .addPrepPhrase("to", "arnold"));
        // testCases.add(
        // new ParseTestCase("Quoted comma list", "Say \"one, two, three\" to arnold",
        // true, CommandMessage.SAY)
        // .addDirect("\"one, two, three\"").addPrepPhrase("to", "arnold"));

        for (ParseTestCase tc : testCases) {
            System.out.println("Testing: " + tc.testName);
            Command cmd = CommandBuilder.parse(tc.input);
            Truth.assertThat(cmd).isNotNull();
            if (tc.command != null) {
                Truth.assertThat(cmd.isValid).isEqualTo(tc.command.isValid);
            } else {
                Truth.assertThat(cmd.isValid).isEqualTo(tc.isValid);
            }
            if (cmd.isValid) {
                Truth.assertThat(cmd).isEqualTo(tc.command);
            }
        }

    }
}
