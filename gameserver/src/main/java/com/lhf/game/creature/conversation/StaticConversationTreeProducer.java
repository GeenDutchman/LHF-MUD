package com.lhf.game.creature.conversation;

import java.util.regex.Pattern;

import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.game.creature.conversation.ConversationTransformer.ConversationContextKey;

public final class StaticConversationTreeProducer {

    public final static ConversationTree.Builder produceTestBuilder() {
        ConversationTreeNode.Builder start = ConversationTreeNode.Builder.ofString("Start")
                .addPrompt(new RichOutputBuilder().appendString("PROMPT ATTACK")
                        .appendMetadata(ConversationContextKey.TALKER_NAME.name()));
        ConversationTreeNode.Builder second = ConversationTreeNode.Builder.ofString("Second")
                .addPrompt(new RichOutputBuilder().appendString("PROMPT ATTACK")
                        .appendMetadata(ConversationContextKey.TALKER_NAME.name()));
        ConversationTreeNode.Builder mid = ConversationTreeNode.Builder.ofString("Mid");
        ConversationPattern anything = new ConversationPattern("anything", ".+", Pattern.CASE_INSENSITIVE);
        ConversationTree.Builder builder = new ConversationTree.Builder(start).addDefaultGreetings()
                .addGreeting(anything).addDefaultRepeatWords().setTreeName("aggravated");
        builder.addNode(start.getNodeID(), anything, second);
        builder.addNode(second.getNodeID(), anything, start);
        builder.addNode(second.getNodeID(), new ConversationPattern("doowhop", "doowhop"), "Random branch dooXwhop");
        builder.addNode(start.getNodeID(), new ConversationPattern("tomid", "tomid"), mid);
        builder.addNode(mid.getNodeID(), anything, second);
        return builder;
    }

    public final static ConversationTree.Builder produceNonVerbalDefault() {
        ConversationTreeNode.Builder Grr = ConversationTreeNode.Builder.ofString("Grr");
        ConversationTreeNode.Builder Hsss = ConversationTreeNode.Builder.ofString("Hsss");
        ConversationTreeNode.Builder Growl = ConversationTreeNode.Builder.ofString("Growl");
        ConversationTreeNode.Builder Snarl = ConversationTreeNode.Builder.ofString("Snarl");
        ConversationPattern pattern = new ConversationPattern("Grr", ".*", Pattern.CASE_INSENSITIVE);
        ConversationTree.Builder builder = new ConversationTree.Builder(Grr).setTreeName("non_verbal_default")
                .addNode(Grr.getNodeID(), pattern, Hsss).addNode(Hsss.getNodeID(), pattern, Growl)
                .addNode(Growl.getNodeID(), pattern, Snarl).addDefaultGreetings();
        return builder;
    }

    public final static ConversationTree.Builder produceVerbalDefault() {
        ConversationTreeNode.Builder start = ConversationTreeNode.Builder.ofRichOutputBuilder(new RichOutputBuilder()
                .appendChild("Hello").appendMetadata(ConversationContextKey.TALKER_TAGGED_NAME.name()));
        ConversationTreeNode.Builder secrets = ConversationTreeNode.Builder
                .ofString("This dungeon has secrets, if you look.");
        ConversationTreeNode.Builder smile = ConversationTreeNode.Builder
                .ofString("*Mysterious Smile* I have said enough.");
        ConversationTreeNode.Builder greetings = ConversationTreeNode.Builder.ofString("Greetings");
        ConversationTreeNode.Builder forgiveness = ConversationTreeNode.Builder
                .ofString("May the Dungeon Mistress and Dungeon Master watch over and forgive you.");

        ConversationTree.Builder builder = new ConversationTree.Builder(start).addDefaultGreetings()
                .addDefaultRepeatWords().setTreeName("verbal_default");
        builder.addNode(start.getNodeID(), new ConversationPattern("Hi", ".*", Pattern.CASE_INSENSITIVE), greetings);
        builder.addNode(greetings.getNodeID(), new ConversationPattern("I must go.", ".*", Pattern.CASE_INSENSITIVE),
                forgiveness);
        builder.addNode(greetings.getNodeID(),
                new ConversationPattern("What can you tell me?", "what|tell me", Pattern.CASE_INSENSITIVE), secrets);
        builder.addNode(secrets.getNodeID(), new ConversationPattern("Like what?", "what"), smile);
        return builder;
    }

    public final static ConversationTree.Builder produceAggravated() {
        ConversationTreeNode.Builder start = ConversationTreeNode.Builder.ofString(" ")
                .addPrompt(new RichOutputBuilder().appendString("PROMPT ATTACK")
                        .appendMetadata(ConversationContextKey.TALKER_NAME.name()));
        ConversationTreeNode.Builder second = ConversationTreeNode.Builder.ofString(" ")
                .addPrompt(new RichOutputBuilder().appendString("PROMPT ATTACK")
                        .appendMetadata(ConversationContextKey.TALKER_NAME.name()));
        ConversationPattern anything = new ConversationPattern("anything", ".+", Pattern.CASE_INSENSITIVE);
        ConversationTree.Builder builder = new ConversationTree.Builder(start).addDefaultGreetings()
                .addGreeting(anything).addDefaultRepeatWords().setTreeName("aggravated");
        builder.addNode(start.getNodeID(), anything, second);
        builder.addNode(second.getNodeID(), anything, start);
        return builder;
    }

    public final static ConversationTree.Builder produceGary() {
        ConversationTreeNode.Builder start = ConversationTreeNode.Builder
                .ofString("Intro lore placeholder here. Are you ok to start?");
        ConversationTreeNode.Builder selection = ConversationTreeNode.Builder
                .ofString("Do you want to be a FIGHTER, MAGE, or HEALER?");
        ConversationTreeNode.Builder fighter = ConversationTreeNode.Builder.ofString(
                "You have selected FIGHTER. Are you unsure about that, or are you ready to go into the dungeon?")
                .addPrompt("STORE CREATE_VOCATION FIGHTER");
        ConversationTreeNode.Builder mage = ConversationTreeNode.Builder
                .ofString("You have selected MAGE. Are you unsure about that, or are you ready to go into the dungeon?")
                .addPrompt("STORE CREATE_VOCATION MAGE");
        ConversationTreeNode.Builder healer = ConversationTreeNode.Builder
                .ofString(
                        "You have selected HEALER. Are you unsure about that, or are you ready to go into the dungeon?")
                .addPrompt("STORE CREATE_VOCATION HEALER");
        ConversationTreeNode.Builder done = ConversationTreeNode.Builder.ofString("It is done!")
                .addPrompt(new RichOutputBuilder().appendChild("PROMPT LEWD Ada Lovejax use")
                        .appendMetadata(ConversationContextKey.TALKER_NAME.name()).appendString("as", " ", " ")
                        .appendMetadata("CREATE_VOCATION"));

        ConversationTree.Builder builder = new ConversationTree.Builder(start).setTreeName("gary").clearGreetings()
                .addGreeting(new ConversationPattern(
                        "This is some lore, but to make a character you need to say \"hi\" to me!", "\\bhi|hi to me\\b",
                        Pattern.CASE_INSENSITIVE))
                .addDefaultRepeatWords();
        builder.addNode(start.getNodeID(), new ConversationPattern("ok", "\\b(ok|okay)\\b", Pattern.CASE_INSENSITIVE),
                selection);
        builder.addNode(selection.getNodeID(), new ConversationPattern("FIGHTER", "fighter", Pattern.CASE_INSENSITIVE),
                fighter);
        builder.addNode(selection.getNodeID(), new ConversationPattern("MAGE", "mage", Pattern.CASE_INSENSITIVE), mage);
        builder.addNode(selection.getNodeID(), new ConversationPattern("HEALER", "healer", Pattern.CASE_INSENSITIVE),
                healer);
        ConversationPattern unsure = new ConversationPattern("I'm unsure", "\\bunsure\\b", Pattern.CASE_INSENSITIVE);
        builder.addNode(fighter.getNodeID(), unsure, selection);
        builder.addNode(mage.getNodeID(), unsure, selection);
        builder.addNode(healer.getNodeID(), unsure, selection);
        ConversationPattern ready = new ConversationPattern("I'm ready", "\\bready\\b", Pattern.CASE_INSENSITIVE);
        builder.addNode(fighter.getNodeID(), ready, done);
        builder.addNode(mage.getNodeID(), ready, done);
        builder.addNode(healer.getNodeID(), ready, done);

        return builder;

    }
}
