package com.lhf;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;

public class TaggableTest {
    private class TestTaggable implements Taggable {
        public String contents = "alkjw ionjwef";
        public String tag = "testing";

        @Override
        public String getTagName() {
            return tag;
        }

        @Override
        public String getSimpleContent() {
            return contents;
        }

    }

    @Test
    void testExtract() {
        TestTaggable test = new TestTaggable();
        String extracted = Taggable.extract(test);
        Truth.assertThat(extracted).isEqualTo(test.contents);
    }
}
