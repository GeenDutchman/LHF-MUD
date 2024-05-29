package com.lhf;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.google.common.truth.FailureMetadata;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.Fact;
import com.google.common.truth.Truth;
import com.lhf.RichOutput.OutputBuilderConversionError;
import com.lhf.RichOutput.PrintingInstructions;
import com.lhf.RichOutput.RichOutputElement;

public final class RichOutputSubject extends Subject {

    public static Factory<RichOutputSubject, RichOutput> richOutputs() {
        return RichOutputSubject::new;
    }

    public static RichOutputSubject assertThat(RichOutput actual) {
        return Truth.assertAbout(richOutputs()).that(actual);
    }

    private final RichOutput actual;

    private RichOutputSubject(FailureMetadata metadata, RichOutput actual) {
        super(metadata, actual);
        this.actual = actual;
    }

    public StringSubject asXML() {
        try {
            return check("asXML()").that(RichOutput.printDocument(RichOutput.documentFromOutput(this.actual, null)));
        } catch (OutputBuilderConversionError | TransformerException | ParserConfigurationException
                | IllegalArgumentException e) {
            failWithActual(Fact.fact("Failed the printing to XML", e));
            // need to return a fallback
            return this.printedWithInstructions(EnumSet.allOf(PrintingInstructions.class));
        }
    }

    public StringSubject printed() {
        return this.printedWithInstructions(Set.of());
    }

    public StringSubject printedWithInstructions(Set<PrintingInstructions> instructions) {
        return check("printString(%s)", instructions).that(actual.printString(instructions));
    }

    public StringSubject builderName() {
        return check("builderName()").that(actual.getBuilderName());
    }

    public IterableSubject elements() {
        return check("elements()").that(actual.getElements());
    }

    public IterableSubject flatElements() {
        return check("flatElements()").that(actual.getFlatElements());
    }

    private void drillTest(Consumer<RichOutput.RichOutputElement> elementTester) {
        if (elementTester == null) {
            return;
        }
        final Deque<Iterator<RichOutput.RichOutputElement>> queue = new ArrayDeque<>();
        final Consumer<RichOutput> richOutputConsumer = (output) -> {
            if (output == null) {
                return;
            }
            List<RichOutputElement> elements = output.getElements();
            if (elements == null || elements.isEmpty()) {
                return;
            }
            queue.addLast(elements.iterator());
        };

        final Consumer<Iterator<RichOutputElement>> iteratorConsumer = (iterator) -> {
            if (iterator == null) {
                return;
            }
            while (iterator.hasNext()) {
                RichOutputElement next = iterator.next();
                if (next == null) {
                    continue;
                }
                elementTester.accept(next);
                RichOutput sub = next.getOutput();
                if (sub != null) {
                    richOutputConsumer.accept(sub);
                }
            }
        };
        richOutputConsumer.accept(this.actual);
        while (!queue.isEmpty()) {
            Iterator<RichOutputElement> first = queue.pollFirst();
            iteratorConsumer.accept(first);
        }
    }

    public void containsNoMetadata() {
        this.drillTest(element -> {
            if (element == null) {
                return;
            }
            final String metadata = element.getMetaSignal();
            if (metadata != null) {
                failWithActual(Fact.fact("Contains metadata", metadata));
            }
        });
    }

}
