package com.lhf;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public interface Taggable {
    public String getTagName();

    public String getSimpleContent();

    public static Map<String, String> produceBasicTagAttributes() {
        Map<String, String> tagAttributes = new TreeMap<>();
        tagAttributes.put("colored", "true");
        return tagAttributes;
    }

    public default Map<String, String> getTagAttributes() {
        return Taggable.produceBasicTagAttributes();
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

    public static final class BasicTaggable implements Taggable, Serializable {
        public final String tagName;
        public final String contents;
        public final Map<String, String> tagAttributes;

        public static BasicTaggable customTaggable(final String tagName, final String contents,
                final Map<String, String> tagAttributes) {
            return new BasicTaggable(tagName, contents, tagAttributes);
        }

        public static BasicTaggable customTaggable(final String tagName, final String contents) {
            return new BasicTaggable(tagName, contents, Taggable.produceBasicTagAttributes());
        }

        private BasicTaggable(final Taggable from) {
            this(from.getTagName(), Taggable.extract(from), from.getTagAttributes());
        }

        private BasicTaggable(final String tagName, final String contents, final Map<String, String> tagAttributes) {
            this.tagName = tagName;
            this.contents = contents;
            this.tagAttributes = tagAttributes != null ? Collections.unmodifiableMap(tagAttributes) : Map.of();
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
        public Map<String, String> getTagAttributes() {
            return this.tagAttributes;
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
