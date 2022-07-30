package com.lhf;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

public class TaggableTest {
    private class TestTaggable implements Taggable {
        public String contents = "alkjw ionjwef";
        public String tag = "testing";

        @Override
        public String getStartTag() {
            return "<" + tag + ">";
        }

        @Override
        public String getEndTag() {
            return "</" + tag + ">";
        }

        @Override
        public String getColorTaggedName() {
            return this.getStartTag() + this.contents + this.getEndTag();
        }

    }

    @Test
    void testExtract() {
        TestTaggable test = new TestTaggable();
        String extracted = Taggable.extract(test);
        Truth.assertThat(extracted).isEqualTo(test.contents);
    }
}
