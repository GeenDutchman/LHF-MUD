package com.lhf.game.creature.conversation;

import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Document;

import com.lhf.OutputBuilder;
import com.lhf.OutputBuilder.OutputBuilderElement;
import com.lhf.OutputBuilder.OutputSequence;
import com.lhf.OutputBuilder.OutputSequenceElement;
import com.lhf.Taggable.BasicTaggable;

public class ConversationTreeNodeResult {
    private final OutputSequence bodySequence;
    private final List<OutputSequence> prompts;

    public static ConversationTreeNodeResult fromString(ConversationContext ctx, String body, List<String> prompts,
            SortedSet<ConversationPattern> branchPatterns) {
        if (ctx == null) {
            throw new IllegalArgumentException("Must have a context to create a result!");
        }
        return ConversationTreeNodeResult
                .create(ctx, new OutputSequence().appendChild(body),
                        prompts == null ? null
                                : prompts.stream().filter(p -> p != null)
                                        .map(p -> new OutputSequence().appendString(p, null, null)).toList(),
                        branchPatterns);
    }

    public static ConversationTreeNodeResult create(ConversationContext ctx, OutputBuilder bodySequence,
            List<OutputSequence> prompts, SortedSet<ConversationPattern> branchPatterns) {
        if (ctx == null) {
            throw new IllegalArgumentException("Must have a context to create a result!");
        }
        List<OutputSequence> promptResults = List.of();
        if (prompts != null) {
            promptResults = new ArrayList<>();
            for (final OutputSequence prompt : prompts) {
                if (prompt == null) {
                    continue;
                }
                OutputSequence sequence = new OutputSequence(prompt.getBuilderName());
                for (OutputBuilderElement element : prompt.getElements()) {
                    if (element == null) {
                        continue;
                    }
                    sequence.appendOutputBuilderElement(ctx.mapping(element), null, null);
                }
                promptResults.add(sequence);
            }
        }

        OutputSequence bodyResult = new OutputSequence();
        Deque<OutputSequenceElement> toProcess = new ArrayDeque<>();
        toProcess.add(OutputSequenceElement.ofOutputBuilder(bodySequence));
        if (branchPatterns != null) {
            processNext: while (!toProcess.isEmpty()) {
                OutputSequenceElement current = toProcess.pop();
                if (current == null) {
                    continue;
                }

                CharSequence chars = current.getCharSequenceAsString();
                if (chars == null || chars.length() == 0) {
                    bodyResult.appendOutputBuilderElement(ctx.mapping(current), null, null);
                    continue processNext;
                }

                for (ConversationPattern pattern : branchPatterns) {
                    Matcher matcher = pattern.getRegex().matcher(chars);
                    if (matcher.find()) {
                        final int starting = matcher.start();
                        final int ending = matcher.end();
                        if (ending != chars.length()) {
                            toProcess.addFirst(
                                    OutputSequenceElement.ofCharSequence(chars.subSequence(ending, chars.length())));
                        }
                        toProcess.addFirst(OutputSequenceElement
                                .ofTaggable(BasicTaggable.customTaggable("convo", matcher.group())));
                        if (0 != starting) {
                            toProcess.addFirst(OutputSequenceElement.ofCharSequence(chars.subSequence(0, starting)));
                        }
                        continue processNext; // ********** NOTE THE LABEL JUMP!! **********
                    }
                }
                bodyResult.appendOutputBuilderElement(ctx.mapping(current), null, null);
            }
        }
        while (!toProcess.isEmpty()) {
            bodyResult.appendOutputBuilderElement(ctx.mapping(toProcess.pop()), null, null);
        }
        return new ConversationTreeNodeResult(bodyResult, promptResults);
    }

    private ConversationTreeNodeResult(OutputSequence bodySequence, List<OutputSequence> prompts) {
        this.bodySequence = bodySequence;
        this.prompts = prompts;
    }

    public String printString() {
        return this.bodySequence.printString();
    }

    public List<String> getPrompts() {
        return this.prompts.stream().map(prompt -> prompt.printString()).toList();
    }

    public final static Document documentFromConversationTreeNodeResult(ConversationTreeNodeResult result)
            throws ParserConfigurationException {
        if (result == null) {
            throw new IllegalArgumentException("Cannot generate document from null result!");
        }
        OutputSequence sequence = new OutputSequence("Conversation");
        sequence.appendOutputBuilder(result.bodySequence, null, null);

        return OutputBuilder.documentFromOutputSequence(sequence, null);
    }

    public final String printXML() throws ParserConfigurationException, TransformerException {
        StringWriter writer = new StringWriter();
        Document myDocument = ConversationTreeNodeResult.documentFromConversationTreeNodeResult(this);
        OutputBuilder.writeDocument(myDocument, writer,
                Map.of(OutputKeys.INDENT, "no", OutputKeys.OMIT_XML_DECLARATION, "yes"));
        return writer.toString();
    }

    public final String print() {
        try {
            return this.printXML();
        } catch (ParserConfigurationException | TransformerException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.WARNING, e, () -> {
                StringJoiner sj = new StringJoiner("\r\n");
                sj.add("Falling back to Stringify: Error occurred when printing XML for conversation:")
                        .add(this.toString());
                return sj.toString();
            });
            return this.printString();
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConversationTreeNodeResult [bodySequence=").append(bodySequence).append(", prompts=")
                .append(prompts).append("]");
        return builder.toString();
    }

}
