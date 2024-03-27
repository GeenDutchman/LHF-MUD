package com.lhf;

import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public interface Taggable {
    public String getTagName();

    public String getTextContent();

    /**
     * Creates a new XML element and adds it either to the provided `node`, or falls
     * back to adding it to the `document`. If the `document` is null, does nothing.
     * 
     * @param document
     * @param node
     */
    public default Element addToXMLDocument(Document document, Node node) {
        if (document == null) {
            return null;
        }
        Element taggedElement = document.createElement(this.getTagName());
        taggedElement.setTextContent(this.getTextContent());
        if (node != null) {
            node.appendChild(taggedElement);
        } else {
            document.appendChild(taggedElement);
        }
        return taggedElement;
    }

    public static String extract(Taggable taggable) {
        return taggable.getTextContent();
    }

    public default BasicTaggable basicTaggable() {
        return Taggable.basicTaggable(this);
    }

    public static BasicTaggable basicTaggable(Taggable taggable) {
        if (taggable == null) {
            return null;
        }
        return new BasicTaggable(taggable);
    }

    public static final class BasicTaggable implements Taggable {
        public final String tagName;
        public final String contents;

        public static BasicTaggable customTaggable(final String tagName, final String contents) {
            return new BasicTaggable(tagName, contents);
        }

        private BasicTaggable(final Taggable from) {
            this(from.getTagName(), Taggable.extract(from));
        }

        private BasicTaggable(final String tagName, final String contents) {
            this.tagName = tagName;
            this.contents = contents;
        }

        @Override
        public String getTagName() {
            return this.tagName;
        }

        @Override
        public String getTextContent() {
            return this.contents;
        }

        @Override
        public int hashCode() {
            return Objects.hash(tagName, contents);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof BasicTaggable))
                return false;
            BasicTaggable other = (BasicTaggable) obj;
            return Objects.equals(tagName, other.tagName) && Objects.equals(contents, other.contents);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("BasicTaggable [tagName=").append(tagName).append(", contents=").append(contents)
                    .append("]");
            return builder.toString();
        }

    }

}
