package com.lhf;

import java.util.Objects;

public interface Taggable {
    String getStartTag();

    String getEndTag();

    String getColorTaggedName();

    public static String extract(Taggable taggable) {
        int lenStart = taggable.getStartTag().length();
        int lenEnd = taggable.getEndTag().length();
        String extracted = taggable.getColorTaggedName();
        int extractSize = extracted.length() - lenEnd;
        extracted = extracted.substring(lenStart, extractSize);
        return extracted;
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
        public final String startTag;
        public final String endTag;
        public final String contents;

        public static BasicTaggable customTaggable(final String startTag, final String contents, final String endTag) {
            return new BasicTaggable(startTag, contents, endTag);
        }

        private BasicTaggable(final Taggable from) {
            this(from.getStartTag(), Taggable.extract(from), from.getEndTag());
        }

        private BasicTaggable(final String startTag, final String contents, final String endTag) {
            this.startTag = startTag;
            this.contents = contents;
            this.endTag = endTag;
        }

        @Override
        public String getStartTag() {
            return this.startTag;
        }

        @Override
        public String getEndTag() {
            return this.endTag;
        }

        @Override
        public String getColorTaggedName() {
            return this.getStartTag() + this.contents + this.getEndTag();
        }

        @Override
        public int hashCode() {
            return Objects.hash(startTag, endTag, contents);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof BasicTaggable))
                return false;
            BasicTaggable other = (BasicTaggable) obj;
            return Objects.equals(startTag, other.startTag) && Objects.equals(endTag, other.endTag)
                    && Objects.equals(contents, other.contents);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("BasicTaggable [startTag=").append(startTag).append(", contents=").append(contents)
                    .append(", endTag=").append(endTag)
                    .append("]");
            return builder.toString();
        }

    }

}
