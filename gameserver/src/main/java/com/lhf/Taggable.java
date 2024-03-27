package com.lhf;

import java.util.Objects;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface Taggable {
    public String getTagName();

    public String getSimpleContent();

    @Deprecated
    public default String print() {
        final String tagName = this.getTagName();
        final String contents = this.getSimpleContent();
        return new StringBuilder().append('<').append(tagName).append('>').append(contents).append("</").append(tagName)
                .append('>').toString();
    }

    public static Element buildXMLElementFromTaggable(Document nodeGenerator, Taggable taggable) {
        if (nodeGenerator == null || taggable == null) {
            return null;
        }
        Element myElement = nodeGenerator.createElement(taggable.getTagName());
        myElement.setAttribute("colored", "true");
        final String simpleContent = taggable.getSimpleContent();
        if (simpleContent != null && !simpleContent.isEmpty() && !simpleContent.isBlank()) {
            myElement.appendChild(nodeGenerator.createTextNode(simpleContent));
        }
        return myElement;
    }

    public default Element buildXMLElement(Document nodeGenerator) {
        if (nodeGenerator == null) {
            return null;
        }
        return Taggable.buildXMLElementFromTaggable(nodeGenerator, this);
    }

    public static String extract(Taggable taggable) {
        return taggable.getSimpleContent();
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
        public String getSimpleContent() {
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
