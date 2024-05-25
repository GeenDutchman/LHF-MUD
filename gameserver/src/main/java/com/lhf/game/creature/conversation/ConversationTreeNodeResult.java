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
import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.RichOutput.RichOutputElement;

import com.lhf.Taggable.BasicTaggable;

public class ConversationTreeNodeResult {
    private final static String BRANCH_TAG = "convo";
    private final RichOutput body;
    private final List<RichOutput> prompts;

    public static ConversationTreeNodeResult fromString(ConversationTransformer transformer, String body,
            List<String> prompts, SortedSet<ConversationPattern> branchPatterns) {
        if (transformer == null) {
            throw new IllegalArgumentException("Must have a context to create a result!");
        }
        return ConversationTreeNodeResult.create(transformer,
                new RichOutputBuilder(ConversationTreeNode.NPC_CONVERSATION_TAG).appendString(body, null, null).build(),
                prompts == null ? null
                        : prompts.stream().filter(p -> p != null)
                                .map(p -> new RichOutputBuilder().appendString(p, null, null).build()).toList(),
                branchPatterns);
    }

    private static List<RichOutput> transformPrompts(ConversationTransformer transformer,
            List<RichOutput> providedPrompts) {
        if (providedPrompts == null) {
            return List.of();
        }
        List<RichOutput> promptResults = new ArrayList<>();
        for (final RichOutput prompt : providedPrompts) {
            if (prompt == null) {
                continue;
            }
            RichOutputBuilder builder = new RichOutputBuilder(prompt.getBuilderName());
            for (RichOutputElement element : prompt.getElements()) {
                if (element != null) {
                    builder.appendRichOutputElement(transformer.apply(element), null, null);
                }
            }
            promptResults.add(builder.build());
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
                        toProcess.addFirst(RichOutputElement.ofCharSequence(chars.subSequence(ending, chars.length())));
                    }
                    toProcess.addFirst(
                            RichOutputElement.ofTaggable(BasicTaggable.customTaggable(BRANCH_TAG, matcher.group())));
                    if (0 != starting) {
                        toProcess.addFirst(RichOutputElement.ofCharSequence(chars.subSequence(0, starting)));
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private static RichOutput transformOutputBuilder(ConversationTransformer transformer, RichOutput body,
            SortedSet<ConversationPattern> branchPatterns) {
        if (body == null) {
            return null;
        }
        RichOutputBuilder bodyResult = new RichOutputBuilder(body.getBuilderName());
        final List<RichOutputElement> elements = body.getElements();
        if (elements == null) {
            return bodyResult.build();
        }
        Deque<RichOutputElement> toProcess = new ArrayDeque<>(elements);
        while (!toProcess.isEmpty()) {
            final RichOutputElement current = toProcess.pop();
            if (current == null) {
                continue;
            }
            RichOutput sub = current.getOutputBuilder();
            if (sub != null) {
                bodyResult.appendRichOutput(
                        ConversationTreeNodeResult.transformOutputBuilder(transformer, sub, branchPatterns), null,
                        null); // recursion
                continue;
            }

            String chars = current.getCharSequenceAsString();
            if (chars == null || chars.length() == 0) {
                bodyResult.appendRichOutputElement(transformer.apply(current), null, null);
                continue;
            }

            if (branchPatterns != null
                    && ConversationTreeNodeResult.transformForBranches(toProcess, chars, branchPatterns)) {
                continue;
            }

            bodyResult.appendRichOutputElement(transformer.apply(current), null, null);
        }
        return bodyResult.build();
    }

    public static ConversationTreeNodeResult create(ConversationTransformer transformer, RichOutput body,
            List<RichOutput> prompts, SortedSet<ConversationPattern> branchPatterns) {
        if (transformer == null) {
            throw new IllegalArgumentException("Must have a context to create a result!");
        }
        if (body == null || body.getBuilderName() == null) {
            throw new IllegalArgumentException(
                    "Must have an OutputBuilder with a non-null BuilderName to create a result!");
        }
        List<RichOutput> promptResults = ConversationTreeNodeResult.transformPrompts(transformer, prompts);

        RichOutput bodyResult = ConversationTreeNodeResult.transformOutputBuilder(transformer, body, branchPatterns);
        return new ConversationTreeNodeResult(bodyResult, promptResults);
    }

    private ConversationTreeNodeResult(RichOutput body, List<RichOutput> prompts) {
        this.body = body;
        this.prompts = prompts;
    }

    public String printString() {
        return this.body.printString();
    }

    public RichOutput getBody() {
        return this.body != null ? this.body : new RichOutputBuilder().build();
    }

    public List<String> getPromptsAsStrings() {
        return this.prompts.stream().map(prompt -> prompt.printString()).toList();
    }

    public List<RichOutput> getPrompts() {
        return Collections.unmodifiableList(prompts);
    }

    public final static Document documentFromConversationTreeNodeResult(ConversationTreeNodeResult result)
            throws ParserConfigurationException {
        if (result == null) {
            throw new IllegalArgumentException("Cannot generate document from null result!");
        }

        return RichOutput.documentFromOutput(result.body, null);
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
        builder.append("ConversationTreeNodeResult [body=").append(body).append(", prompts=").append(prompts)
                .append("]");
        return builder.toString();
    }

}
