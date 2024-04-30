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

public final class RichOutput implements Serializable {

    public final static class RichOutputElement implements Serializable {
        private final String charSequence;
        private final BasicTaggable taggable;
        private final BasicExaminable examinable;
        private final RichOutput Output;
        private final String metaSignal;

        private RichOutputElement(CharSequence charSequence, Taggable taggable, Examinable examinable,
                RichOutput Output, String metaSignal) {
            this.charSequence = charSequence != null ? charSequence.toString() : null;
            this.taggable = Taggable.basicTaggable(taggable);
            this.examinable = Examinable.basicExaminable(examinable);
            this.Output = Output;
            this.metaSignal = metaSignal != null ? new String(metaSignal) : null;
        }

        public static RichOutputElement ofCharSequence(CharSequence charSequence) {
            return new RichOutputElement(charSequence, null, null, null, null);
        }

        public static RichOutputElement ofTaggable(Taggable taggable) {
            return new RichOutputElement(null, taggable, null, null, null);
        }

        public static RichOutputElement ofExaminable(Examinable examinable) {
            return new RichOutputElement(null, null, examinable, null, null);
        }

        public static RichOutputElement ofOutput(RichOutput sequence) {
            return new RichOutputElement(null, null, null, sequence, null);
        }

        public static RichOutputElement ofMetaSignal(String metaSignal) {
            return new RichOutputElement(null, null, null, null, metaSignal);
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

        public RichOutput getOutputBuilder() {
            return Output;
        }

        public String getMetaSignal() {
            return metaSignal;
        }

        @Override
        public int hashCode() {
            return Objects.hash(charSequence, taggable, examinable, Output, metaSignal);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof RichOutputElement))
                return false;
            RichOutputElement other = (RichOutputElement) obj;
            return Objects.equals(charSequence, other.charSequence) && Objects.equals(taggable, other.taggable)
                    && Objects.equals(examinable, other.examinable) && Objects.equals(Output, other.Output)
                    && Objects.equals(metaSignal, other.metaSignal);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("OutputElement [charSequence=").append(charSequence).append(", taggable=").append(taggable)
                    .append(", examinable=").append(examinable).append(", Output=").append(Output)
                    .append(", metaSignal=").append(metaSignal).append("]");
            return builder.toString();
        }

        public String printString() {
            return this.printString(Set.of());
        }

        public String printString(Set<PrintingInstructions> instructions) {
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

    private final String sequenceName;
    private final List<RichOutputElement> elements;

    private RichOutput(RichOutputBuilder builder) {
        if (builder != null) {
            this.sequenceName = builder.getBuilderName();
            List<com.lhf.RichOutput.RichOutputBuilder.BuilderElement> tempElements = builder.getElements();
            if (tempElements != null) {
                this.elements = tempElements.stream().filter(element -> element != null).map(element -> element.build())
                        .toList();
            } else {
                this.elements = List.of();
            }
        } else {
            this.sequenceName = null;
            this.elements = new ArrayList<>();
        }
    }

    public static RichOutputBuilder getBuilder() {
        return new RichOutputBuilder();
    }

    public static RichOutputBuilder getBuilder(String name) {
        return new RichOutputBuilder(name);
    }

    public final String getBuilderName() {
        return sequenceName;
    }

    public final List<RichOutputElement> getElements() {
        return Collections.unmodifiableList(elements);
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
        if (!(obj instanceof RichOutput))
            return false;
        RichOutput other = (RichOutput) obj;
        return Objects.equals(sequenceName, other.sequenceName);
    }

    public enum PrintingInstructions {
        TAGS, BUILDER_NAME, META_SIGNAL;
    }

    public String printString() {
        return this.printString(Set.of());
    }

    public String printString(Set<PrintingInstructions> instructions) {
        StringBuilder sb = new StringBuilder();
        final String builderName = this.getBuilderName();
        if (instructions != null && instructions.contains(PrintingInstructions.BUILDER_NAME) && builderName != null) {
            sb.append(builderName).append("- ");
        }

        final List<RichOutputElement> elements = this.getElements();
        if (elements != null) {
            for (final RichOutputElement OutputElement : elements) {
                if (OutputElement != null) {
                    sb.append(OutputElement.printString(instructions));
                }
            }
        }
        return sb.toString();
    }

    public static class RichOutputBuilder implements Serializable {

        private static class BuilderElement implements Serializable {
            private RichOutputBuilder builder;
            private RichOutputElement element;

            public BuilderElement(RichOutputBuilder builder) {
                this.builder = builder;
            }

            public BuilderElement(RichOutputElement element) {
                this.element = element;
            }

            // public void set(RichOutputBuilder builder) {
            // this.builder = builder;
            // if (builder != null) {
            // this.element = null;
            // }
            // }

            public void set(RichOutputElement element) {
                this.element = element;
                if (element != null) {
                    this.builder = null;
                }
            }

            public RichOutputElement build() {
                if (builder != null) {
                    return RichOutputElement.ofOutput(builder.build());
                } else if (element != null) {
                    return element;
                } else {
                    return new RichOutputElement(null, null, null, null, null);
                }
            }

            @Override
            public String toString() {
                StringBuilder builder2 = new StringBuilder();
                builder2.append("BuilderElement [builder=").append(builder).append(", element=").append(element)
                        .append("]");
                return builder2.toString();
            }

            // public String printString() {
            // return this.printString(Set.of());
            // }

            public String printString(Set<PrintingInstructions> instructions) {
                if (builder != null) {
                    return builder.printString(instructions);
                } else if (element != null) {
                    return element.printString(instructions);
                } else {
                    return "";
                }
            }

            @Override
            public int hashCode() {
                return Objects.hash(builder, element);
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj)
                    return true;
                if (!(obj instanceof BuilderElement))
                    return false;
                BuilderElement other = (BuilderElement) obj;
                return Objects.equals(builder, other.builder) && Objects.equals(element, other.element);
            }

            public boolean hasElement(RichOutputElement toFind) {
                if (toFind == null || this.element == null) {
                    return false;
                }
                return this.element.equals(toFind);
            }

            // public boolean hasElement(BuilderElement toFind) {
            // if (toFind == null || toFind.element == null || this.element == null) {
            // return false;
            // }
            // return this.element.equals(toFind.element);
            // }

        }

        private String sequenceName;
        private List<BuilderElement> elements;

        public RichOutputBuilder() {
            this(null);
        }

        public RichOutputBuilder(String sequenceName) {
            this.sequenceName = sequenceName;
            this.elements = new ArrayList<>();
        }

        public List<BuilderElement> getElements() {
            return this.elements;
        }

        public String getBuilderName() {
            return sequenceName;
        }

        public RichOutput build() {
            return new RichOutput(this);
        }

        public RichOutputBuilder appendOutputBuilderElement(RichOutputElement toAdd) {
            return this.appendOutputBuilderElement(toAdd, " ", null);
        }

        public RichOutputBuilder appendOutputBuilderElement(RichOutputElement toAdd, String before, String after) {
            if (toAdd != null) {
                if (before != null) {
                    this.elements.add(new BuilderElement(RichOutputElement.ofCharSequence(before)));
                }
                this.elements.add(new BuilderElement(toAdd));
                if (after != null) {
                    this.elements.add(new BuilderElement(RichOutputElement.ofCharSequence(after)));
                }
            }
            return this;
        }

        public RichOutputBuilder appendOutputBuilder(RichOutput toAdd) {
            return this.appendOutputBuilder(toAdd, " ", null);
        }

        public RichOutputBuilder appendOutputBuilder(RichOutput toAdd, String before, String after) {
            if (toAdd != null) {
                if (before != null) {
                    this.elements.add(new BuilderElement(RichOutputElement.ofCharSequence(before)));
                }
                this.elements.add(new BuilderElement(RichOutputElement.ofOutput(toAdd)));
                if (after != null) {
                    this.elements.add(new BuilderElement(RichOutputElement.ofCharSequence(after)));
                }
            }
            return this;
        }

        public RichOutputBuilder appendString(String toAdd) {
            return this.appendString(toAdd, " ", null);
        }

        public RichOutputBuilder appendChild(String toAdd) {
            return this.appendString(toAdd, " ", null);
        }

        public RichOutputBuilder appendString(String toAdd, String before, String after) {
            if (toAdd != null) {
                if (before != null) {
                    this.elements.add(new BuilderElement(RichOutputElement.ofCharSequence(before)));
                }
                this.elements.add(new BuilderElement(RichOutputElement.ofCharSequence(toAdd)));
                if (after != null) {
                    this.elements.add(new BuilderElement(RichOutputElement.ofCharSequence(after)));
                }
            }
            return this;
        }

        public RichOutputBuilder appendExaminable(Examinable toAdd) {
            return this.appendExaminable(toAdd, " ", null);
        }

        public RichOutputBuilder appendExaminable(Examinable toAdd, String before, String after) {
            if (toAdd != null) {
                if (before != null) {
                    this.elements.add(new BuilderElement(RichOutputElement.ofCharSequence(before)));
                }
                this.elements.add(new BuilderElement(RichOutputElement.ofExaminable(toAdd)));
                toAdd.produceExtraDescription(this);
                if (after != null) {
                    this.elements.add(new BuilderElement(RichOutputElement.ofCharSequence(after)));
                }
            }
            return this;
        }

        public RichOutputBuilder appendTaggable(Taggable toAdd) {
            return this.appendTaggable(toAdd, " ", null);
        }

        public RichOutputBuilder appendChild(Taggable toAdd) {
            return this.appendTaggable(toAdd);
        }

        public RichOutputBuilder appendTaggable(Taggable toAdd, String before, String after) {
            if (toAdd != null) {
                if (before != null) {
                    this.elements.add(new BuilderElement(RichOutputElement.ofCharSequence(before)));
                }
                this.elements.add(new BuilderElement(RichOutputElement.ofTaggable(toAdd)));
                if (after != null) {
                    this.elements.add(new BuilderElement(RichOutputElement.ofCharSequence(after)));
                }
            }
            return this;
        }

        public <Tgg extends Taggable> RichOutputBuilder appendTaggables(Collection<Tgg> taggables) {
            return this.appendTaggables(taggables, ", ", null, null, null);
        }

        public <Tgg extends Taggable> RichOutputBuilder appendTaggables(Collection<Tgg> taggables, String separator,
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

        public <Tgg extends Taggable> RichOutputBuilder appendTaggablesAndLast(List<Tgg> taggables) {
            return this.appendTaggablesAndLast(taggables, ",", null, null, null);
        }

        public <Tgg extends Taggable> RichOutputBuilder appendTaggablesAndLast(List<Tgg> taggables, String separator,
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

        public <Tgg extends Taggable> RichOutputBuilder appendTaggablesAndLast(SortedSet<Tgg> taggables) {
            return this.appendTaggablesAndLast(taggables, ",", null, null, null);
        }

        public <Tgg extends Taggable> RichOutputBuilder appendTaggablesAndLast(SortedSet<Tgg> taggables,
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

        public RichOutputBuilder produceSubBuilder(String subName) {
            RichOutputBuilder sub = new RichOutputBuilder(subName);
            this.elements.add(new BuilderElement(sub));
            return sub;
        }

        public RichOutputBuilder replaceElement(RichOutputElement toFind, RichOutputElement replacement) {
            Iterator<BuilderElement> iterator = this.elements.iterator();
            while (iterator.hasNext()) {
                BuilderElement element = iterator.next();
                if (element == null) {
                    iterator.remove();
                    continue;
                }
                if (element.hasElement(toFind)) {
                    if (replacement != null) {
                        element.set(replacement);
                    } else {
                        iterator.remove();
                    }
                    return this;
                }
            }
            return this;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("RichOutputBuilder [sequenceName=").append(sequenceName).append(", elements=")
                    .append(elements).append("]");
            return builder.toString();
        }

        public String printString() {
            return this.printString(Set.of());
        }

        public String printString(Set<PrintingInstructions> instructions) {
            StringBuilder sb = new StringBuilder();
            final String builderName = this.getBuilderName();
            if (instructions != null && instructions.contains(PrintingInstructions.BUILDER_NAME)
                    && builderName != null) {
                sb.append(builderName).append("- ");
            }

            final List<BuilderElement> elements = this.getElements();
            if (elements != null) {
                for (final BuilderElement OutputElement : elements) {
                    if (OutputElement != null) {
                        sb.append(OutputElement.printString(instructions));
                    }
                }
            }
            return sb.toString();
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

    public static Document documentFromOutput(RichOutput sequence, Map<String, String> tagAttributes)
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
                    "Error either creating root element (with the Output name of '%s') or appending it to the document",
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
            final List<RichOutputElement> elementList) throws OutputBuilderConversionError {
        if (document == null || node == null || elementList == null) {
            return;
        }
        for (int i = 0; i < elementList.size(); i++) {
            final RichOutputElement sequenceMember = elementList.get(i);
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
        final List<RichOutputElement> elementList = outputBuilder.getElements();
        try {
            RichOutput.acceptOutputBuilderElements(document, myNode, elementList);
        } catch (OutputBuilderConversionError e) {
            throw new OutputBuilderConversionError(String.format("Error for OutputBuilder '%s'", sequenceName), e);
        }

    }

    public static final class OutputElementCollector
            implements Collector<RichOutputElement, RichOutputBuilder, RichOutput> {

        @Override
        public Set<Characteristics> characteristics() {
            return Set.of(Characteristics.IDENTITY_FINISH);
        }

        @Override
        public BiConsumer<RichOutputBuilder, RichOutputElement> accumulator() {

            return (builder, element) -> {
                if (builder == null || element == null) {
                    return;
                }
                builder.appendOutputBuilderElement(element);
            };
        }

        @Override
        public BinaryOperator<RichOutputBuilder> combiner() {
            return (builderOne, builderTwo) -> {
                if (builderOne != null && builderTwo != null) {
                    return builderOne.appendOutputBuilder(builderTwo.build());
                } else if (builderOne != null && builderTwo == null) {
                    return builderOne;
                } else if (builderOne == null && builderTwo != null) {
                    return builderTwo;
                } else {
                    return new RichOutputBuilder();
                }
            };
        }

        @Override
        public Function<RichOutputBuilder, RichOutput> finisher() {
            return (builder) -> {
                if (builder == null) {
                    return new RichOutput(null);
                }
                return new RichOutput(builder);
            };
        }

        @Override
        public Supplier<RichOutputBuilder> supplier() {
            return () -> {
                return new RichOutputBuilder();
            };
        }

    }

    public static Collector<RichOutputElement, RichOutputBuilder, RichOutput> collector() {
        return new OutputElementCollector();
    }

}
