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

import com.lhf.Taggable.BasicTaggable;

public interface OutputBuilder {
    public default OutputBuilder appendOutputSequenceElement(OutputSequenceElement toAdd) {
        return this.appendOutputSequenceElement(toAdd, " ", null);
    }

    public OutputBuilder appendOutputSequenceElement(OutputSequenceElement toAdd, String before, String after);

    public default OutputBuilder appendOutputSequence(OutputSequence toAdd) {
        return this.appendOutputSequence(toAdd, " ", null);
    }

    public OutputBuilder appendOutputSequence(OutputSequence toAdd, String before, String after);

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

    public final static class OutputSequenceElement implements Serializable {
        private final CharSequence charSequence;
        private final Taggable taggable;
        private final Examinable examinable;
        private final OutputSequence outputSequence;

        private OutputSequenceElement(CharSequence charSequence, Taggable taggable, Examinable examinable,
                OutputSequence outputSequence) {
            this.charSequence = charSequence;
            this.taggable = taggable;
            this.examinable = examinable;
            this.outputSequence = outputSequence;
        }

        public OutputSequenceElement(OutputSequenceElement other) {
            if (other != null) {
                if (other.charSequence != null) {
                    this.charSequence = other.charSequence.toString();
                } else {
                    this.charSequence = null;
                }
                this.taggable = other.taggable;
                this.examinable = other.examinable;
                this.outputSequence = new OutputSequence(other.outputSequence);
            } else {
                this.charSequence = "";
                this.taggable = null;
                this.examinable = null;
                this.outputSequence = null;
            }
        }

        public static OutputSequenceElement ofCharSequence(CharSequence charSequence) {
            return new OutputSequenceElement(charSequence, null, null, null);
        }

        public static OutputSequenceElement ofTaggable(Taggable taggable) {
            return new OutputSequenceElement(null, taggable, null, null);
        }

        public static OutputSequenceElement ofExaminable(Examinable examinable) {
            return new OutputSequenceElement(null, null, examinable, null);
        }

        public static OutputSequenceElement ofOutputSequence(OutputSequence sequence) {
            return new OutputSequenceElement(null, null, null, sequence);
        }

        public CharSequence getCharSequence() {
            return charSequence;
        }

        public Taggable getTaggable() {
            return taggable;
        }

        public Examinable getExaminable() {
            return examinable;
        }

        public OutputSequence getOutputSequence() {
            return outputSequence;
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
            return Objects.hash(charSequence, taggable, examinable, outputSequence);
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
                    && Objects.equals(outputSequence, other.outputSequence);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("OutputSequenceElement [charSequence=").append(charSequence).append(", taggable=")
                    .append(taggable).append(", examinable=").append(examinable).append(", outputSequence=")
                    .append(outputSequence).append("]");
            return builder.toString();
        }

    }

    public static final class OutputSequence implements OutputBuilder, Iterable<OutputSequenceElement>, Serializable {

        private final String sequenceName;
        public final List<OutputSequenceElement> elements;

        public OutputSequence() {
            this.sequenceName = null;
            this.elements = new ArrayList<>();
        }

        public OutputSequence(String sequenceName) {
            this.sequenceName = sequenceName;
            this.elements = new ArrayList<>();
        }

        public OutputSequence(OutputSequence sequence) {
            if (sequence != null) {
                this.sequenceName = sequence.getSequenceName();
                this.elements = new ArrayList<>();
                sequence.getElements().forEach(other -> new OutputSequenceElement(other));
            } else {
                this.sequenceName = null;
                this.elements = new ArrayList<>();
            }
        }

        public final String getSequenceName() {
            return sequenceName;
        }

        public final List<OutputSequenceElement> getElements() {
            return Collections.unmodifiableList(elements);
        }

        @Override
        public OutputSequence appendOutputSequenceElement(OutputSequenceElement toAdd, String before, String after) {
            if (toAdd != null) {
                if (before != null) {
                    this.elements.add(OutputSequenceElement.ofCharSequence(before));
                }
                this.elements.add(toAdd);
                if (after != null) {
                    this.elements.add(OutputSequenceElement.ofCharSequence(after));
                }
            }
            return this;
        }

        @Override
        public OutputSequence appendOutputSequence(OutputSequence toAdd, String before, String after) {
            if (toAdd != null) {
                if (before != null) {
                    this.elements.add(OutputSequenceElement.ofCharSequence(before));
                }
                this.elements.add(OutputSequenceElement.ofOutputSequence(toAdd));
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
        Element root = document.createElement(sequence.getSequenceName());
        if (tagAttributes != null) {
            for (final Entry<String, String> entry : tagAttributes.entrySet()) {
                final String key = entry.getKey();
                final String value = entry.getValue();
                if (key != null && value != null) {
                    root.setAttribute(key, value);
                }
            }
        }

        OutputBuilder.acceptOutputSequence(document, root, sequence);

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

    private static void acceptOutputSequence(Document document, Element element, OutputSequence toAdd) {
        if (document == null || element == null || toAdd == null) {
            return;
        }
        Element myElement = element;
        final String sequenceName = toAdd.getSequenceName();
        if (sequenceName != null) {
            myElement = document.createElement(sequenceName);
            element.appendChild(myElement);
        }
        final List<OutputSequenceElement> elementList = toAdd.getElements();
        for (final OutputSequenceElement sequenceMember : elementList) {
            if (sequenceMember == null) {
                continue;
            } else if (sequenceMember.getCharSequence() != null) {
                myElement.appendChild(document.createTextNode(sequenceMember.getCharSequence().toString()));
            } else if (sequenceMember.getTaggable() != null) {
                OutputBuilder.acceptTaggable(document, myElement, sequenceMember.getTaggable());
            } else if (sequenceMember.getExaminable() != null) {
                OutputBuilder.acceptExaminable(document, myElement, sequenceMember.getExaminable());
            } else if (sequenceMember.getOutputSequence() != null) {
                OutputBuilder.acceptOutputSequence(document, myElement, sequenceMember.getOutputSequence());
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
                    arg0.appendOutputSequenceElement(arg1);
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
                    sequence.appendOutputSequence(arg0).appendOutputSequence(arg1);
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
