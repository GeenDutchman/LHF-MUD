package com.lhf;

import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.google.common.truth.Truth;
import com.lhf.RichOutput.PrintingInstructions;
import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.RichOutput.RichOutputElement;
import com.lhf.Taggable.BasicTaggable;

public class RichOutputTest {
    @Test
    void testPrintString() {
        final String sequenceName = "Sequence Name";
        final String body1 = "I have no";
        final String body2 = "time to dilly dally!";
        final BasicTaggable taggable = BasicTaggable.customTaggable("VIP", "Jonny");
        final String body3 = "is";
        final String metadata = "text=blue";
        final String body4 = "coming!";
        final String subSequenceName = "Todos";
        final String subBody = "Roll out the carpet!";
        RichOutputBuilder builder = new RichOutputBuilder(sequenceName);
        builder.appendString(body1);
        builder.appendString(body2);
        builder.appendTaggable(taggable);
        builder.appendString(body3);
        builder.appendMetadata(metadata);
        builder.appendString(body4);
        builder.appendRichOutputBuilder(new RichOutputBuilder(subSequenceName).appendString(subBody));

        final RichOutput built = builder.build();

        final String noInstructions = built.printString(Set.of());
        Truth.assertThat(noInstructions).doesNotContain(sequenceName);
        Truth.assertThat(noInstructions).doesNotContain(taggable.getTagName());
        Truth.assertThat(noInstructions).doesNotContain(metadata);

        RichOutputSubject.assertThat(built).flatElements().contains(RichOutputElement.ofMetaSignal(metadata));
        RichOutputSubject.assertThat(built).flatElements().hasSize(6); // test the string collapsing

        final String allInstructions = built.printString();
        System.out.println(allInstructions);
        StringBuilder expected = new StringBuilder("\r\n").append(sequenceName).append(" - \r\n")
                .append(PrintingInstructions.TAB_STRING);
        expected.append(body1).append(" ").append(body2);
        expected.append(" **").append(taggable.getTagName()).append("-").append(taggable.getSimpleContent())
                .append("** ").append(body3).append(" ").append(metadata).append(" ").append(body4);
        expected.append(" \r\n").append(PrintingInstructions.TAB_STRING).append(PrintingInstructions.TAB_STRING)
                .append(subSequenceName).append(" - \r\n");
        expected.append(PrintingInstructions.TAB_STRING).append(PrintingInstructions.TAB_STRING)
                .append(PrintingInstructions.TAB_STRING).append(subBody).append("\r\n")
                .append(PrintingInstructions.TAB_STRING);
        RichOutputSubject.assertThat(built).printedWithInstructions(EnumSet.allOf(PrintingInstructions.class))
                .isEqualTo(expected.toString());
    }
}
