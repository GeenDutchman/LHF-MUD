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
import java.util.stream.Collector;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.lhf.Examinable.BasicExaminable;
import com.lhf.Taggable.BasicTaggable;

public interface OutputBuilder {
    public String getBuilderName();

    public List<OutputBuilderElement> getElements();

    public default OutputBuilder appendOutputBuilderElement(OutputBuilderElement toAdd) {
        return this.appendOutputBuilderElement(toAdd, " ", null);
    }

    public OutputBuilder appendOutputBuilderElement(OutputBuilderElement toAdd, String before, String after);

    public default OutputBuilder appendOutputBuilder(OutputBuilder toAdd) {
        return this.appendOutputBuilder(toAdd, " ", null);
    }

    public OutputBuilder appendOutputBuilder(OutputBuilder toAdd, String before, String after);

    public default OutputBuilder appendString(String toAdd) {
        return this.appendString(toAdd, " ", null);
    }

    public default OutputBuilder appendChild(String toAdd) {
        return this.appendString(toAdd, " ", null);
    }

    public OutputBuilder appendString(String toAdd, String before, String after);

    public default OutputBuilder appendExaminable(Examinable toAdd) {
        return this.appendExaminable(toAdd, " ", null);
    }

    public OutputBuilder appendExaminable(Examinable toAdd, String before, String after);

    public default OutputBuilder appendTaggable(Taggable toAdd) {
        return this.appendTaggable(toAdd, " ", null);
    }

    public default OutputBuilder appendChild(Taggable toAdd) {
        return this.appendTaggable(toAdd);
    }

    public OutputBuilder appendTaggable(Taggable toAdd, String before, String after);

    public default <Tgg extends Taggable> OutputBuilder appendTaggables(Collection<Tgg> taggables) {
        return this.appendTaggables(taggables, ", ", null, null, null);
    }

    public default <Tgg extends Taggable> OutputBuilder appendTaggables(Collection<Tgg> taggables, String separator,
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

    public default <Tgg extends Taggable> OutputBuilder appendTaggablesAndLast(List<Tgg> taggables) {
        return this.appendTaggablesAndLast(taggables, ",", null, null, null);
    }

    public default <Tgg extends Taggable> OutputBuilder appendTaggablesAndLast(List<Tgg> taggables, String separator,
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

    public default <Tgg extends Taggable> OutputBuilder appendTaggablesAndLast(SortedSet<Tgg> taggables) {
        return this.appendTaggablesAndLast(taggables, ",", null, null, null);
    }

    public default <Tgg extends Taggable> OutputBuilder appendTaggablesAndLast(SortedSet<Tgg> taggables,
            String separator, String before, String after, String empty) {
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

    public abstract OutputBuilder produceSubBuilder(String subName);

    public static interface OutputBuilderElement {
        @Deprecated(forRemoval = false)
        public CharSequence getCharSequence();

        public String getCharSequenceAsString();

        public Taggable getTaggable();

        public Examinable getExaminable();

        public OutputBuilder getOutputBuilder();

        public String getMetaSignal();
    }

    public final static class OutputSequenceElement implements OutputBuilderElement, Serializable {
        private final CharSequence charSequence;
        private final BasicTaggable taggable;
        private final BasicExaminable examinable;
        private final OutputSequence outputSequence;
        private final String metaSignal;

        private OutputSequenceElement(CharSequence charSequence, Taggable taggable, Examinable examinable,
                OutputSequence outputSequence, String metaSignal) {
            this.charSequence = charSequence;
            this.taggable = Taggable.basicTaggable(taggable);
            this.examinable = Examinable.basicExaminable(examinable);
            this.outputSequence = outputSequence;
            this.metaSignal = metaSignal != null ? new String(metaSignal) : null;
        }

        public OutputSequenceElement(OutputBuilderElement other) {
            if (other != null) {
                this.charSequence = other.getCharSequenceAsString();
                this.taggable = Taggable.basicTaggable(other.getTaggable());
                this.examinable = Examinable.basicExaminable(other.getExaminable());
                this.outputSequence = new OutputSequence(other.getOutputBuilder());
                this.metaSignal = other.getMetaSignal();
            } else {
                this.charSequence = "";
                this.taggable = null;
                this.examinable = null;
                this.outputSequence = null;
                this.metaSignal = null;
            }
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

        public static OutputSequenceElement ofOutputBuilder(OutputBuilder builder) {
            return new OutputSequenceElement(null, null, null, new OutputSequence(builder), null);
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

        public String printString() {
            if (this.charSequence != null) {
                return this.charSequence.toString();
            } else if (this.taggable != null) {
                return this.taggable.getSimpleContent();
            } else if (this.examinable != null) {
                StringBuilder sb = new StringBuilder();
                sb.append("**").append(this.examinable.getName()).append("**");
                final String description = this.examinable.getDescription();
                if (description != null && !description.isBlank()) {
                    sb.append(" Description: ").append(description).append(" ");
                }
                return sb.toString();
            } else if (this.outputSequence != null) {
                return this.outputSequence.printString();
            } else {
                return "";
            }
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

    public static final class OutputSequence implements OutputBuilder, Iterable<OutputSequenceElement>, Serializable {

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

        public OutputSequence(OutputBuilder sequence) {
            if (sequence != null) {
                this.sequenceName = sequence.getBuilderName();
                this.elements = new ArrayList<>();
                sequence.getElements().forEach(other -> new OutputSequenceElement(other));
            } else {
                this.sequenceName = null;
                this.elements = new ArrayList<>();
            }
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
                this.elements.add(new OutputSequenceElement(toAdd));
                if (after != null) {
                    this.elements.add(OutputSequenceElement.ofCharSequence(after));
                }
            }
            return this;
        }

        @Override
        public OutputSequence appendOutputBuilder(OutputBuilder toAdd, String before, String after) {
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

        public String printString() {
            StringBuilder sb = new StringBuilder();
            if (this.sequenceName != null) {
                sb.append("\r\n").append(this.sequenceName).append(":\r\n");
            }
            for (final OutputSequenceElement outputSequenceElement : elements) {
                if (outputSequenceElement != null) {
                    sb.append(outputSequenceElement.printString());
                }
            }
            return sb.toString();
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

    public static void writeDocument(Document document, Writer writer) throws TransformerException {
        if (document == null || writer == null) {
            throw new IllegalArgumentException("Cannot write null document or to null writer");
        }
        Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
        transformer.transform(new DOMSource(document), new StreamResult(writer));
    }

    public static String printDocument(Document document) throws TransformerException {
        if (document == null) {
            throw new IllegalArgumentException("Cannot make string from null document!");
        }
        StringWriter writer = new StringWriter();
        OutputBuilder.writeDocument(document, writer);
        return writer.toString();
    }

    public static Document documentFromOutputSequence(OutputSequence sequence, Map<String, String> tagAttributes)
            throws ParserConfigurationException {
        if (sequence == null) {
            throw new IllegalArgumentException("Cannot generate document from null sequence");
        }
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newDefaultInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();
        Element root = document.createElement(sequence.getBuilderName());
        if (tagAttributes != null) {
            for (final Entry<String, String> entry : tagAttributes.entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                if (key != null && value != null) {
                    root.setAttribute(key, value);
                }
            }
        }

        OutputBuilder.acceptOutputBuilder(document, root, sequence);

        return document;
    }

    private static void acceptTaggable(Document document, Element element, Taggable toAdd) {
        if (document == null || element == null || toAdd == null) {
            return;
        }
        final String tagName = toAdd.getTagName();
        Element myElement = document
                .createElement(tagName != null && !tagName.isEmpty() && !tagName.isBlank() ? tagName : "Taggable");
        element.appendChild(myElement);
        myElement.appendChild(document.createTextNode(toAdd.getSimpleContent()));
        final Map<String, String> tagAttributes = toAdd.getTagAttributes();
        if (tagAttributes != null) {
            for (final Entry<String, String> entry : tagAttributes.entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                if (key != null && value != null) {
                    myElement.setAttribute(key, value);
                }
            }
        }
    }

    public final static String XML_DESCRIPTION = "description";

    private static void acceptExaminable(Document document, Element element, Examinable toAdd) {
        if (document == null || element == null || toAdd == null) {
            return;
        }
        final String tagName = toAdd.getTagName();
        Element myElement = document
                .createElement(tagName != null && !tagName.isEmpty() && !tagName.isBlank() ? tagName : "Examinable");
        element.appendChild(myElement);
        final String name = toAdd.getName();
        final String simpleContent = toAdd.getSimpleContent();
        if (name != null && !name.equals(simpleContent)) {
            OutputBuilder.acceptTaggable(document, myElement, BasicTaggable.customTaggable("name", name, Map.of()));
        }
        if (simpleContent != null && !simpleContent.isEmpty() && !simpleContent.isBlank()) {
            myElement.appendChild(document.createTextNode(simpleContent));
        }
        final Map<String, String> tagAttributes = toAdd.getTagAttributes();
        if (tagAttributes != null) {
            for (final Entry<String, String> entry : tagAttributes.entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                if (key != null && value != null) {
                    myElement.setAttribute(key, value);
                }
            }
        }

        final String description = toAdd.getDescription();
        if (description != null && !description.isEmpty() && !description.isBlank()) {
            Element descriptionElement = document.createElement(XML_DESCRIPTION);
            descriptionElement.setTextContent(description.trim());
            myElement.appendChild(descriptionElement);
        }
    }

    private static void acceptOutputBuilder(Document document, Element element, OutputBuilder outputBuilder) {
        if (document == null || element == null || outputBuilder == null) {
            return;
        }
        Element myElement = element;
        final String sequenceName = outputBuilder.getBuilderName();
        if (sequenceName != null) {
            myElement = document.createElement(sequenceName);
            element.appendChild(myElement);
        }
        final List<OutputBuilderElement> elementList = outputBuilder.getElements();
        for (final OutputBuilderElement sequenceMember : elementList) {
            if (sequenceMember == null) {
                continue;
            } else if (sequenceMember.getCharSequence() != null) {
                myElement.appendChild(document.createTextNode(sequenceMember.getCharSequence().toString()));
            } else if (sequenceMember.getTaggable() != null) {
                OutputBuilder.acceptTaggable(document, myElement, sequenceMember.getTaggable());
            } else if (sequenceMember.getExaminable() != null) {
                OutputBuilder.acceptExaminable(document, myElement, sequenceMember.getExaminable());
            } else if (sequenceMember.getOutputBuilder() != null) {
                OutputBuilder.acceptOutputBuilder(document, myElement, sequenceMember.getOutputBuilder());
            }
        }
    }

    public static final class OutputSequenceElementCollector
            implements Collector<OutputSequenceElement, OutputSequence, OutputSequence> {

        @Override
        public BiConsumer<OutputSequence, OutputSequenceElement> accumulator() {
            return new BiConsumer<OutputBuilder.OutputSequence, OutputBuilder.OutputSequenceElement>() {

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
            return new BinaryOperator<OutputBuilder.OutputSequence>() {

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
