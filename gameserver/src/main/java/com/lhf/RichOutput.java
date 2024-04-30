package com.lhf;

import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.lhf.Examinable.BasicExaminable;
import com.lhf.Taggable.BasicTaggable;

public interface RichOutput {
    public String getBuilderName();

    public List<OutputBuilderElement> getElements();

    public enum PrintingInstructions {
        TAGS, BUILDER_NAME, META_SIGNAL;
    }

    public default String printString() {
        return this.printString(Set.of());
    }

    public default String printString(Set<PrintingInstructions> instructions) {
        StringBuilder sb = new StringBuilder();
        final String builderName = this.getBuilderName();
        if (instructions != null && instructions.contains(PrintingInstructions.BUILDER_NAME) && builderName != null) {
            sb.append(builderName).append("- ");
        }

        final List<OutputBuilderElement> elements = this.getElements();
        if (elements != null) {
            for (final OutputBuilderElement outputSequenceElement : elements) {
                if (outputSequenceElement != null) {
                    sb.append(outputSequenceElement.printString(instructions));
                }
            }
        }
        return sb.toString();
    }

    public default RichOutput appendOutputBuilderElement(OutputBuilderElement toAdd) {
        return this.appendOutputBuilderElement(toAdd, " ", null);
    }

    public RichOutput appendOutputBuilderElement(OutputBuilderElement toAdd, String before, String after);

    public default RichOutput appendOutputBuilder(RichOutput toAdd) {
        return this.appendOutputBuilder(toAdd, " ", null);
    }

    public RichOutput appendOutputBuilder(RichOutput toAdd, String before, String after);

    public default RichOutput appendString(String toAdd) {
        return this.appendString(toAdd, " ", null);
    }

    public default RichOutput appendChild(String toAdd) {
        return this.appendString(toAdd, " ", null);
    }

    public RichOutput appendString(String toAdd, String before, String after);

    public default RichOutput appendExaminable(Examinable toAdd) {
        return this.appendExaminable(toAdd, " ", null);
    }

    public RichOutput appendExaminable(Examinable toAdd, String before, String after);

    public default RichOutput appendTaggable(Taggable toAdd) {
        return this.appendTaggable(toAdd, " ", null);
    }

    public default RichOutput appendChild(Taggable toAdd) {
        return this.appendTaggable(toAdd);
    }

    public RichOutput appendTaggable(Taggable toAdd, String before, String after);

    public default <Tgg extends Taggable> RichOutput appendTaggables(Collection<Tgg> taggables) {
        return this.appendTaggables(taggables, ", ", null, null, null);
    }

    public default <Tgg extends Taggable> RichOutput appendTaggables(Collection<Tgg> taggables, String separator,
            String before, String after, String empty) {
        if (before != null) {
            this.appendString(before);
        }
        if (taggables == null || taggables.isEmpty()) {
            if (empty != null) {
                this.appendString(empty);
            }
        } else if (taggables.size() == 1) {
            this.appendTaggable(taggables.stream().findAny().get());
        } else {
            boolean first = true;
            for (Taggable taggable : taggables) {
                this.appendTaggable(taggable, first ? " " : separator, null);
                first = false;
            }
        }
        if (after != null) {
            this.appendString(after);
        }
        return this;
    }

    public default <Tgg extends Taggable> RichOutput appendTaggablesAndLast(List<Tgg> taggables) {
        return this.appendTaggablesAndLast(taggables, ",", null, null, null);
    }

    public default <Tgg extends Taggable> RichOutput appendTaggablesAndLast(List<Tgg> taggables, String separator,
            String before, String after, String empty) {
        if (before != null) {
            this.appendString(before);
        }
        if (taggables == null || taggables.isEmpty()) {
            if (empty != null) {
                this.appendString(empty);
            }
        } else if (taggables.size() == 1) {
            this.appendTaggable(taggables.get(0));
        } else {
            final int lastIndex = taggables.size() - 1;
            for (int i = 0; i < lastIndex; i++) {
                this.appendTaggable(taggables.get(i), " ", separator);
            }
            this.appendString("and", " ", null).appendTaggable(taggables.get(lastIndex), " ", null);
        }
        if (after != null) {
            this.appendString(after);
        }
        return this;
    }

    public default <Tgg extends Taggable> RichOutput appendTaggablesAndLast(SortedSet<Tgg> taggables) {
        return this.appendTaggablesAndLast(taggables, ",", null, null, null);
    }

    public default <Tgg extends Taggable> RichOutput appendTaggablesAndLast(SortedSet<Tgg> taggables, String separator,
            String before, String after, String empty) {
        if (before != null) {
            this.appendString(before);
        }
        if (taggables == null || taggables.isEmpty()) {
            if (empty != null) {
                this.appendString(empty);
            }
        } else if (taggables.size() == 1) {
            this.appendTaggable(taggables.first());
        } else {
            final Tgg last = taggables.last();
            final SortedSet<Tgg> remainder = taggables.headSet(last);
            for (final Taggable taggable : remainder) {
                this.appendTaggable(taggable, " ", separator);
            }
            this.appendString("and", " ", null).appendTaggable(last, " ", null);
        }
        if (after != null) {
            this.appendString(after);
        }
        return this;
    }

    public abstract RichOutput produceSubBuilder(String subName);

    public static interface OutputBuilderElement {
        @Deprecated(forRemoval = false)
        public CharSequence getCharSequence();

        public String getCharSequenceAsString();

        public Taggable getTaggable();

        public Examinable getExaminable();

        public RichOutput getOutputBuilder();

        public String getMetaSignal();

        public default String printString() {
            return this.printString(Set.of());
        }

        public default String printString(Set<PrintingInstructions> instructions) {
            final String charSequence = this.getCharSequenceAsString();
            final Taggable taggable = this.getTaggable();
            final Examinable examinable = this.getExaminable();
            final RichOutput builder = this.getOutputBuilder();
            final String meta = this.getMetaSignal();

            if (charSequence != null) {
                return charSequence;
            } else if (taggable != null) {
                StringBuilder sb = new StringBuilder().append("**");
                if (instructions != null && instructions.contains(PrintingInstructions.TAGS)) {
                    sb.append(taggable.getTagName()).append("-");
                }
                return sb.append(taggable.getSimpleContent()).append("**").toString();
            } else if (examinable != null) {
                StringBuilder sb = new StringBuilder().append("**");
                if (instructions != null && instructions.contains(PrintingInstructions.TAGS)) {
                    sb.append(examinable.getTagName()).append("-");
                }
                sb.append(examinable.getName()).append("**");
                final String description = examinable.getDescription();
                if (description != null && !description.isBlank()) {
                    sb.append(" Description - ").append(description).append(" ");
                }
                return sb.toString();
            } else if (builder != null) {
                return builder.printString(instructions);
            } else if (meta != null && instructions != null
                    && instructions.contains(PrintingInstructions.META_SIGNAL)) {
                return new StringBuilder(" ").append(meta).append(" ").toString();
            } else {
                return "";
            }
        }
    }

    public final static class OutputSequenceElement implements OutputBuilderElement, Serializable {
        private final String charSequence;
        private final BasicTaggable taggable;
        private final BasicExaminable examinable;
        private final OutputSequence outputSequence;
        private final String metaSignal;

        private OutputSequenceElement(CharSequence charSequence, Taggable taggable, Examinable examinable,
                OutputSequence outputSequence, String metaSignal) {
            this.charSequence = charSequence != null ? charSequence.toString() : null;
            this.taggable = Taggable.basicTaggable(taggable);
            this.examinable = Examinable.basicExaminable(examinable);
            this.outputSequence = outputSequence;
            this.metaSignal = metaSignal != null ? new String(metaSignal) : null;
        }

        public static final OutputSequenceElement copy(OutputBuilderElement other) {
            if (other == null) {
                return null;
            }
            return new OutputSequenceElement(other.getCharSequenceAsString(), other.getTaggable(),
                    other.getExaminable(), OutputSequence.copy(other.getOutputBuilder()), other.getMetaSignal());
        }

        public static OutputSequenceElement ofCharSequence(CharSequence charSequence) {
            return new OutputSequenceElement(charSequence, null, null, null, null);
        }

        public static OutputSequenceElement ofTaggable(Taggable taggable) {
            return new OutputSequenceElement(null, taggable, null, null, null);
        }

        public static OutputSequenceElement ofExaminable(Examinable examinable) {
            return new OutputSequenceElement(null, null, examinable, null, null);
        }

        public static OutputSequenceElement ofOutputSequence(OutputSequence sequence) {
            return new OutputSequenceElement(null, null, null, sequence, null);
        }

        public static OutputSequenceElement ofOutputBuilder(RichOutput builder) {
            return new OutputSequenceElement(null, null, null, OutputSequence.copy(builder), null);
        }

        public static OutputSequenceElement ofMetaSignal(String metaSignal) {
            return new OutputSequenceElement(null, null, null, null, metaSignal);
        }

        @Deprecated(forRemoval = false)
        public CharSequence getCharSequence() {
            return charSequence;
        }

        public String getCharSequenceAsString() {
            return charSequence != null ? charSequence.toString() : null;
        }

        public Taggable getTaggable() {
            return taggable;
        }

        public Examinable getExaminable() {
            return examinable;
        }

        public OutputSequence getOutputBuilder() {
            return outputSequence;
        }

        @Override
        public String getMetaSignal() {
            return metaSignal;
        }

        @Override
        public int hashCode() {
            return Objects.hash(charSequence, taggable, examinable, outputSequence, metaSignal);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof OutputSequenceElement))
                return false;
            OutputSequenceElement other = (OutputSequenceElement) obj;
            return Objects.equals(charSequence, other.charSequence) && Objects.equals(taggable, other.taggable)
                    && Objects.equals(examinable, other.examinable)
                    && Objects.equals(outputSequence, other.outputSequence)
                    && Objects.equals(metaSignal, other.metaSignal);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("OutputSequenceElement [charSequence=").append(charSequence).append(", taggable=")
                    .append(taggable).append(", examinable=").append(examinable).append(", outputSequence=")
                    .append(outputSequence).append(", metaSignal=").append(metaSignal).append("]");
            return builder.toString();
        }

    }

    public static final class OutputSequence implements RichOutput, Iterable<OutputSequenceElement>, Serializable {

        private final String sequenceName;
        private final List<OutputSequenceElement> elements;

        public OutputSequence() {
            this.sequenceName = null;
            this.elements = new ArrayList<>();
        }

        public OutputSequence(String sequenceName) {
            this.sequenceName = sequenceName;
            this.elements = new ArrayList<>();
        }

        public static final OutputSequence copy(RichOutput sequence) {
            if (sequence == null) {
                return null;
            }
            OutputSequence next = new OutputSequence(sequence.getBuilderName());
            List<OutputBuilderElement> oldElements = sequence.getElements();
            if (oldElements != null) {
                oldElements.stream().filter(element -> element != null)
                        .forEach(element -> next.elements.add(OutputSequenceElement.copy(element)));
            }
            return next;
        }

        public final String getBuilderName() {
            return sequenceName;
        }

        public final List<OutputBuilderElement> getElements() {
            return Collections.unmodifiableList(elements);
        }

        @Override
        public OutputSequence appendOutputBuilderElement(OutputBuilderElement toAdd, String before, String after) {
            if (toAdd != null) {
                if (before != null) {
                    this.elements.add(OutputSequenceElement.ofCharSequence(before));
                }
                this.elements.add(OutputSequenceElement.copy(toAdd));
                if (after != null) {
                    this.elements.add(OutputSequenceElement.ofCharSequence(after));
                }
            }
            return this;
        }

        @Override
        public OutputSequence appendOutputBuilder(RichOutput toAdd, String before, String after) {
            if (toAdd != null) {
                if (before != null) {
                    this.elements.add(OutputSequenceElement.ofCharSequence(before));
                }
                this.elements.add(OutputSequenceElement.ofOutputBuilder(toAdd));
                if (after != null) {
                    this.elements.add(OutputSequenceElement.ofCharSequence(after));
                }
            }
            return this;
        }

        @Override
        public OutputSequence appendString(String toAdd, String before, String after) {
            if (toAdd != null) {
                if (before != null) {
                    this.elements.add(OutputSequenceElement.ofCharSequence(before));
                }
                this.elements.add(OutputSequenceElement.ofCharSequence(toAdd));
                if (after != null) {
                    this.elements.add(OutputSequenceElement.ofCharSequence(after));
                }
            }
            return this;
        }

        @Override
        public OutputSequence appendExaminable(Examinable toAdd, String before, String after) {
            if (toAdd != null) {
                if (before != null) {
                    this.elements.add(OutputSequenceElement.ofCharSequence(before));
                }
                this.elements.add(OutputSequenceElement.ofExaminable(toAdd));
                toAdd.produceExtraDescription(this);
                if (after != null) {
                    this.elements.add(OutputSequenceElement.ofCharSequence(after));
                }
            }
            return this;
        }

        @Override
        public OutputSequence appendTaggable(Taggable toAdd, String before, String after) {
            if (toAdd != null) {
                if (before != null) {
                    this.elements.add(OutputSequenceElement.ofCharSequence(before));
                }
                this.elements.add(OutputSequenceElement.ofTaggable(toAdd));
                if (after != null) {
                    this.elements.add(OutputSequenceElement.ofCharSequence(after));
                }
            }
            return this;
        }

        @Override
        public OutputSequence produceSubBuilder(String subName) {
            OutputSequence sub = new OutputSequence(subName);
            this.elements.add(OutputSequenceElement.ofOutputSequence(sub));
            return sub;
        }

        public OutputSequence replaceElement(OutputSequenceElement toFind, OutputSequenceElement replacement) {
            final int index = this.elements.indexOf(toFind);
            if (index >= 0) {
                if (replacement != null) {
                    this.elements.set(index, replacement);
                } else {
                    this.elements.remove(index);
                }
            }
            return this;
        }

        @Override
        public int hashCode() {
            if (sequenceName == null) {
                return super.hashCode();
            }
            return Objects.hash(sequenceName);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof OutputSequence))
                return false;
            OutputSequence other = (OutputSequence) obj;
            return Objects.equals(sequenceName, other.sequenceName);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("OutputSequence [sequenceName=").append(sequenceName).append(", elements=").append(elements)
                    .append("]");
            return builder.toString();
        }

        @Override
        public Iterator<OutputSequenceElement> iterator() {
            return this.elements.iterator();
        }

    }

    public static void writeDocument(Document document, Writer writer, Map<String, String> transformerProperties)
            throws TransformerException {
        if (document == null || writer == null) {
            throw new IllegalArgumentException("Cannot write null document or to null writer");
        }
        Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
        if (transformerProperties != null) {
            for (Map.Entry<String, String> entry : transformerProperties.entrySet()) {
                if (entry == null) {
                    continue;
                }
                final String key = entry.getKey();
                final String value = entry.getValue();
                if (key == null || value == null) {
                    continue;
                }
                transformer.setOutputProperty(key, value);
            }
        }
        transformer.transform(new DOMSource(document), new StreamResult(writer));
    }

    public static void writeDocument(Document document, Writer writer) throws TransformerException {
        if (document == null || writer == null) {
            throw new IllegalArgumentException("Cannot write null document or to null writer");
        }
        RichOutput.writeDocument(document, writer, Map.of());
    }

    public static String printDocument(Document document) throws TransformerException {
        if (document == null) {
            throw new IllegalArgumentException("Cannot make string from null document!");
        }
        StringWriter writer = new StringWriter();
        RichOutput.writeDocument(document, writer);
        return writer.toString();
    }

    public final static class OutputBuilderConversionError extends RuntimeException {
        public OutputBuilderConversionError(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static Document documentFromOutputSequence(OutputSequence sequence, Map<String, String> tagAttributes)
            throws ParserConfigurationException, OutputBuilderConversionError {
        if (sequence == null) {
            throw new IllegalArgumentException("Cannot generate document from null sequence");
        }
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newDefaultInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element root = null;
        try {
            root = document.createElement(sequence.getBuilderName());
            document.appendChild(root);
        } catch (DOMException e) {
            throw new OutputBuilderConversionError(String.format(
                    "Error either creating root element (with the OutputSequence name of '%s') or appending it to the document",
                    sequence.getBuilderName()), e);
        }

        if (tagAttributes != null) {
            for (final Entry<String, String> entry : tagAttributes.entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                if (key != null && value != null) {
                    root.setAttribute(key, value);
                }
            }
        }

        try {
            RichOutput.acceptOutputBuilderElements(document, root, sequence.getElements());
        } catch (OutputBuilderConversionError e) {
            throw new OutputBuilderConversionError(
                    String.format("Error for OutputBuilder '%s'", sequence.getBuilderName()), e);
        }

        return document;
    }

    private static void acceptTaggable(Document document, Node node, Taggable toAdd)
            throws OutputBuilderConversionError {
        if (document == null || node == null || toAdd == null) {
            return;
        }
        final String tagName = toAdd.getTagName();
        Element myElement = null;
        try {
            myElement = document
                    .createElement(tagName != null && !tagName.isEmpty() && !tagName.isBlank() ? tagName : "Taggable");
            node.appendChild(myElement);
        } catch (DOMException e) {
            throw new OutputBuilderConversionError(String.format(
                    "Error either creating element (with the Taggable TagName of '%s') or appending it to the current node",
                    tagName), e);
        }

        try {
            myElement.appendChild(document.createTextNode(toAdd.getSimpleContent()));
        } catch (DOMException e) {
            throw new OutputBuilderConversionError(
                    String.format("Error appending child text node for Taggable %s", toAdd.getSimpleContent()), e);
        }
        final Map<String, String> tagAttributes = toAdd.getTagAttributes();
        if (tagAttributes != null) {
            for (final Entry<String, String> entry : tagAttributes.entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                if (key != null && value != null) {
                    try {
                        myElement.setAttribute(key, value);
                    } catch (DOMException e) {
                        throw new OutputBuilderConversionError(
                                String.format("Error setting attribute '%s=%s' for Taggable %s", key, value,
                                        toAdd.getSimpleContent()),
                                e);
                    }
                }
            }
        }
    }

    public final static String XML_DESCRIPTION = "description";

    private static void acceptExaminable(Document document, Node node, Examinable toAdd)
            throws OutputBuilderConversionError {
        if (document == null || node == null || toAdd == null) {
            return;
        }
        final String tagName = toAdd.getTagName();
        Element myElement = null;
        try {
            myElement = document.createElement(
                    tagName != null && !tagName.isEmpty() && !tagName.isBlank() ? tagName : "Examinable");
            node.appendChild(myElement);
        } catch (DOMException e) {
            throw new OutputBuilderConversionError(String.format(
                    "Error either creating element (with the Examinable TagName of '%s') or appending it to the current node",
                    tagName), e);
        }
        final String name = toAdd.getName();
        final String simpleContent = toAdd.getSimpleContent();
        if (name != null && !name.equals(simpleContent)) {
            final BasicTaggable nameTaggable = BasicTaggable.customTaggable("name", name, Map.of());
            try {
                RichOutput.acceptTaggable(document, myElement, nameTaggable);
            } catch (OutputBuilderConversionError e) {
                throw new OutputBuilderConversionError(
                        String.format("Error accepting name Taggable '%s' for the Examinable", nameTaggable), e);
            }
        }
        if (simpleContent != null && !simpleContent.isEmpty() && !simpleContent.isBlank()) {
            try {
                myElement.appendChild(document.createTextNode(simpleContent));
            } catch (DOMException e) {
                throw new OutputBuilderConversionError(
                        String.format("Error appending simplecontent '%s' for the Examinable %s", simpleContent, name),
                        e);
            }
        }
        final Map<String, String> tagAttributes = toAdd.getTagAttributes();
        if (tagAttributes != null) {
            for (final Entry<String, String> entry : tagAttributes.entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                if (key != null && value != null) {
                    try {
                        myElement.setAttribute(key, value);
                    } catch (DOMException e) {
                        throw new OutputBuilderConversionError(
                                String.format("Error setting attribute '%s=%s' for Examinable %s", key, value, name),
                                e);
                    }
                }
            }
        }

        final String description = toAdd.getDescription();
        if (description != null && !description.isEmpty() && !description.isBlank()) {
            Element descriptionElement = document.createElement(XML_DESCRIPTION);
            try {
                descriptionElement.setTextContent(description.trim());
                myElement.appendChild(descriptionElement);
            } catch (DOMException e) {
                throw new OutputBuilderConversionError(String
                        .format("Error creating or appending Examinable '%s' description: '%s'", name, description), e);
            }
        }
    }

    private static void acceptOutputBuilderElements(final Document document, final Node node,
            final List<OutputBuilderElement> elementList) throws OutputBuilderConversionError {
        if (document == null || node == null || elementList == null) {
            return;
        }
        for (int i = 0; i < elementList.size(); i++) {
            final OutputBuilderElement sequenceMember = elementList.get(i);
            if (sequenceMember == null) {
                continue;
            } else if (sequenceMember.getCharSequence() != null) {
                try {
                    node.appendChild(document.createTextNode(sequenceMember.getCharSequence().toString()));
                } catch (DOMException e) {
                    throw new OutputBuilderConversionError(
                            String.format("Error for element %d while appending text '%s'", i,
                                    sequenceMember.getCharSequenceAsString()),
                            e);
                }
            } else if (sequenceMember.getTaggable() != null) {
                try {
                    RichOutput.acceptTaggable(document, node, sequenceMember.getTaggable());
                } catch (OutputBuilderConversionError e) {
                    throw new OutputBuilderConversionError(
                            String.format("Error for element %d while appending Taggable", i), e);
                }
            } else if (sequenceMember.getExaminable() != null) {
                try {
                    RichOutput.acceptExaminable(document, node, sequenceMember.getExaminable());
                } catch (OutputBuilderConversionError e) {
                    throw new OutputBuilderConversionError(
                            String.format("Error for element %d while appending Examinable", i), e);
                }
            } else if (sequenceMember.getOutputBuilder() != null) {
                try {
                    RichOutput.acceptOutputBuilder(document, node, sequenceMember.getOutputBuilder());
                } catch (OutputBuilderConversionError e) {
                    throw new OutputBuilderConversionError(
                            String.format("Error for element %d while appending OutputBuilder", i), e);
                } catch (DOMException e) {
                    throw new OutputBuilderConversionError(
                            String.format("DOM Error for element %d while appending OutputBuilder", i), e);
                }
            }
        }
    }

    private static void acceptOutputBuilder(Document document, Node node, RichOutput outputBuilder)
            throws OutputBuilderConversionError {
        if (document == null || node == null || outputBuilder == null) {
            return;
        }
        Node myNode = node;
        final String sequenceName = outputBuilder.getBuilderName();
        if (sequenceName != null) {
            try {
                myNode = document.createElement(sequenceName);
                node.appendChild(myNode);
            } catch (DOMException e) {
                throw new OutputBuilderConversionError(String.format(
                        "Error either creating element (with the OutputBuilder name of '%s') or appending it to the current node",
                        sequenceName), e);
            }
        }
        final List<OutputBuilderElement> elementList = outputBuilder.getElements();
        try {
            RichOutput.acceptOutputBuilderElements(document, myNode, elementList);
        } catch (OutputBuilderConversionError e) {
            throw new OutputBuilderConversionError(String.format("Error for OutputBuilder '%s'", sequenceName), e);
        }

    }

    public static final class OutputSequenceElementCollector
            implements Collector<OutputSequenceElement, OutputSequence, OutputSequence> {

        @Override
        public BiConsumer<OutputSequence, OutputSequenceElement> accumulator() {
            return new BiConsumer<RichOutput.OutputSequence, RichOutput.OutputSequenceElement>() {

                @Override
                public void accept(OutputSequence arg0, OutputSequenceElement arg1) {
                    if (arg0 == null || arg1 == null) {
                        return;
                    }
                    arg0.appendOutputBuilderElement(arg1);
                }

            };
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of(Characteristics.IDENTITY_FINISH);
        }

        @Override
        public BinaryOperator<OutputSequence> combiner() {
            return new BinaryOperator<RichOutput.OutputSequence>() {

                @Override
                public OutputSequence apply(OutputSequence arg0, OutputSequence arg1) {
                    OutputSequence sequence = new OutputSequence();
                    sequence.appendOutputBuilder(arg0).appendOutputBuilder(arg1);
                    return sequence;
                }

            };
        }

        @Override
        public Function<OutputSequence, OutputSequence> finisher() {
            return Function.identity();
        }

        @Override
        public Supplier<OutputSequence> supplier() {
            return () -> new OutputSequence();
        }

    }

    public static Collector<OutputSequenceElement, OutputSequence, OutputSequence> collector() {
        return new OutputSequenceElementCollector();
    }

}
