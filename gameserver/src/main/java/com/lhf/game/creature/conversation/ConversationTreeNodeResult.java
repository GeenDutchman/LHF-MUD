package com.lhf.game.creature.conversation;

import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
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

import com.lhf.RichOutput;
import com.lhf.RichOutput.RichOutputElement;
import com.lhf.RichOutput.RichOutputSequence;
import com.lhf.RichOutput.RichOutputSequenceElement;
import com.lhf.Taggable.BasicTaggable;

public class ConversationTreeNodeResult {
    private final static String BRANCH_TAG = "convo";
    private final RichOutputSequence bodySequence;
    private final List<RichOutputSequence> prompts;

    public static ConversationTreeNodeResult fromString(ConversationTransformer transformer, String body,
            List<String> prompts, SortedSet<ConversationPattern> branchPatterns) {
        if (transformer == null) {
            throw new IllegalArgumentException("Must have a context to create a result!");
        }
        return ConversationTreeNodeResult.create(transformer,
                new RichOutputSequence(ConversationTreeNode.NPC_CONVERSATION_TAG).appendString(body, null, null),
                prompts == null ? null
                        : prompts.stream().filter(p -> p != null)
                                .map(p -> new RichOutputSequence().appendString(p, null, null)).toList(),
                branchPatterns);
    }

    private static List<RichOutputSequence> transformPrompts(ConversationTransformer transformer,
            List<RichOutputSequence> providedPrompts) {
        List<RichOutputSequence> promptResults = List.of();
        if (providedPrompts != null) {
            promptResults = new ArrayList<>();
            for (final RichOutputSequence prompt : providedPrompts) {
                if (prompt == null) {
                    continue;
                }
                RichOutputSequence sequence = new RichOutputSequence(prompt.getBuilderName());
                for (RichOutputElement element : prompt.getElements()) {
                    if (element == null) {
                        continue;
                    }
                    sequence.appendOutputBuilderElement(transformer.apply(element), null, null);
                }
                promptResults.add(sequence);
            }
        }
        return promptResults;
    }

    private static boolean transformForBranches(Deque<RichOutputElement> toProcess, String chars,
            SortedSet<ConversationPattern> branchPatterns) {
        if (branchPatterns != null) {
            for (ConversationPattern pattern : branchPatterns) {
                Matcher matcher = pattern.getRegex().matcher(chars);
                if (matcher.find()) {
                    final int starting = matcher.start();
                    final int ending = matcher.end();
                    if (ending != chars.length()) {
                        toProcess.addFirst(
                                RichOutputSequenceElement.ofCharSequence(chars.subSequence(ending, chars.length())));
                    }
                    toProcess.addFirst(RichOutputSequenceElement
                            .ofTaggable(BasicTaggable.customTaggable(BRANCH_TAG, matcher.group())));
                    if (0 != starting) {
                        toProcess.addFirst(RichOutputSequenceElement.ofCharSequence(chars.subSequence(0, starting)));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static RichOutputSequence transformOutputBuilder(ConversationTransformer transformer,
            RichOutput bodySequence, SortedSet<ConversationPattern> branchPatterns) {
        if (bodySequence == null) {
            return null;
        }
        RichOutputSequence bodyResult = new RichOutputSequence(bodySequence.getBuilderName());
        final List<RichOutputElement> elements = bodySequence.getElements();
        if (elements == null) {
            return bodyResult;
        }
        Deque<RichOutputElement> toProcess = new ArrayDeque<>(elements);
        while (!toProcess.isEmpty()) {
            final RichOutputElement current = toProcess.pop();
            if (current == null) {
                continue;
            }
            RichOutput sub = current.getOutputBuilder();
            if (sub != null) {
                bodyResult.appendOutputBuilder(
                        ConversationTreeNodeResult.transformOutputBuilder(transformer, sub, branchPatterns), null,
                        null); // recursion
                continue;
            }

            String chars = current.getCharSequenceAsString();
            if (chars == null || chars.length() == 0) {
                bodyResult.appendOutputBuilderElement(transformer.apply(current), null, null);
                continue;
            }

            if (branchPatterns != null
                    && ConversationTreeNodeResult.transformForBranches(toProcess, chars, branchPatterns)) {
                continue;
            }

            bodyResult.appendOutputBuilderElement(transformer.apply(current), null, null);
        }
        return bodyResult;
    }

    public static ConversationTreeNodeResult create(ConversationTransformer transformer, RichOutput bodySequence,
            List<RichOutputSequence> prompts, SortedSet<ConversationPattern> branchPatterns) {
        if (transformer == null) {
            throw new IllegalArgumentException("Must have a context to create a result!");
        }
        if (bodySequence == null || bodySequence.getBuilderName() == null) {
            throw new IllegalArgumentException(
                    "Must have an OutputBuilder with a non-null BuilderName to create a result!");
        }
        List<RichOutputSequence> promptResults = ConversationTreeNodeResult.transformPrompts(transformer, prompts);

        RichOutputSequence bodyResult = ConversationTreeNodeResult.transformOutputBuilder(transformer, bodySequence,
                branchPatterns);
        return new ConversationTreeNodeResult(bodyResult, promptResults);
    }

    private ConversationTreeNodeResult(RichOutputSequence bodySequence, List<RichOutputSequence> prompts) {
        this.bodySequence = bodySequence;
        this.prompts = prompts;
    }

    public String printString() {
        return this.bodySequence.printString();
    }

    public RichOutputSequence getBodySequence() {
        return RichOutputSequence.copy(bodySequence);
    }

    public List<String> getPromptsAsStrings() {
        return this.prompts.stream().map(prompt -> prompt.printString()).toList();
    }

    public List<RichOutputSequence> getPrompts() {
        return Collections.unmodifiableList(prompts);
    }

    public final static Document documentFromConversationTreeNodeResult(ConversationTreeNodeResult result)
            throws ParserConfigurationException {
        if (result == null) {
            throw new IllegalArgumentException("Cannot generate document from null result!");
        }

        return RichOutput.documentFromOutputSequence(result.bodySequence, null);
    }

    public final String printXML() throws ParserConfigurationException, TransformerException {
        StringWriter writer = new StringWriter();
        Document myDocument = ConversationTreeNodeResult.documentFromConversationTreeNodeResult(this);
        RichOutput.writeDocument(myDocument, writer,
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
