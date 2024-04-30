package com.lhf;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.messages.events.SeeEvent;

public interface Examinable extends Taggable {
    String getName();

    String getDescription();

    @Override
    public default String getSimpleContent() {
        return this.getName();
    }

    public default void produceExtraDescription(RichOutputBuilder builder) {
        return;
    }

    default SeeEvent produceMessage() {
        return this.produceMessage(SeeEvent.getBuilder().setExaminable(this));
    }

    default SeeEvent produceMessage(SeeEvent.ABuilder<?> seeOutMessage) {
        if (seeOutMessage == null) {
            seeOutMessage = SeeEvent.getBuilder().setExaminable(this);
        }
        return seeOutMessage.Build();
    }

    public default BasicExaminable basicExaminable() {
        return new BasicExaminable(this);
    }

    public static BasicExaminable basicExaminable(Examinable examinable) {
        if (examinable == null) {
            return null;
        }
        return new BasicExaminable(examinable);
    }

    public static final class BasicExaminable implements Examinable, Serializable {
        public final String tagName;
        public final String description;
        public final String name;
        public final String contents;
        public final Map<String, String> tagAttributes;
        public final RichOutput extraDescription;

        private BasicExaminable(String tagName, String description, String name, String contents,
                Map<String, String> tagAttributes, RichOutput extraDescription) {
            this.tagName = tagName;
            this.description = description;
            this.name = name;
            this.contents = contents;
            this.tagAttributes = tagAttributes;
            this.extraDescription = extraDescription;
        }

        private static RichOutput retrieveExtras(final Examinable from) {
            RichOutputBuilder extras = new RichOutputBuilder();
            from.produceExtraDescription(extras);
            return extras.build();
        }

        private BasicExaminable(final Examinable from) {
            this(from.getTagName(), from.getDescription(), from.getName(), from.getSimpleContent(),
                    from.getTagAttributes(), BasicExaminable.retrieveExtras(from));
        }

        @Override
        public String getTagName() {
            return this.tagName;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public String getDescription() {
            return this.description;
        }

        @Override
        public String getSimpleContent() {
            return this.contents;
        }

        @Override
        public Map<String, String> getTagAttributes() {
            return tagAttributes;
        }

        public RichOutput getExtraDescription() {
            return extraDescription;
        }

        @Override
        public void produceExtraDescription(RichOutputBuilder builder) {
            if (builder == null) {
                return;
            }
            builder.appendRichOutput(extraDescription, null, null);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tagName, description, name, contents, tagAttributes, extraDescription);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof BasicExaminable))
                return false;
            BasicExaminable other = (BasicExaminable) obj;
            return Objects.equals(tagName, other.tagName) && Objects.equals(description, other.description)
                    && Objects.equals(name, other.name) && Objects.equals(contents, other.contents)
                    && Objects.equals(tagAttributes, other.tagAttributes)
                    && Objects.equals(extraDescription, other.extraDescription);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("BasicExaminable [tagName=").append(tagName).append(", description=").append(description)
                    .append(", name=").append(name).append(", contents=").append(contents).append(", tagAttributes=")
                    .append(tagAttributes).append(", extraDescription=").append(extraDescription).append("]");
            return builder.toString();
        }

    }
}
