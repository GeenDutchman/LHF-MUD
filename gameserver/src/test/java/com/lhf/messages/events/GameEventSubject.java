package com.lhf.messages.events;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.google.common.truth.Fact;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.StringSubject;
import com.google.common.truth.StringSubject.CaseInsensitiveStringComparison;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;
import com.lhf.RichOutputSubject;
import com.lhf.game.TickType;
import com.lhf.messages.GameEventType;

public final class GameEventSubject extends Subject {
    public static Subject.Factory<GameEventSubject, GameEvent> gameEvents() {
        return GameEventSubject::new;
    }

    public static GameEventSubject assertThat(GameEvent actual) {
        return Truth.assertAbout(gameEvents()).that(actual);
    }

    private final GameEvent actual;

    private GameEventSubject(FailureMetadata metadata, GameEvent actual) {
        super(metadata, actual);
        this.actual = actual;
    }

    public void hasType(GameEventType type) {
        check("getXmlEventType()").that(this.actual.getXmlEventType()).isEqualTo(type);
    }

    public void isBroadcast() {
        check("isBroadcast()").that(this.actual.isBroadcast()).isTrue();
    }

    public void isNotBroadcast() {
        check("isBroadcast()").that(this.actual.isBroadcast()).isFalse();
    }

    public void hasTickType(TickType type) {
        check("getTickType()").that(this.actual.getTickType()).isEqualTo(type);
    }

    public StringSubject asString() {
        return check("printString()").that(this.actual.printString());
    }

    public RichOutputSubject asRichOutput() {
        return check("getRichOutput()").about(RichOutputSubject.richOutputs()).that(actual.getRichOutput());
    }

    public StringSubject asXML() {
        try {
            return check("printXML()").that(actual.printXML());
        } catch (ParserConfigurationException | TransformerException | IllegalArgumentException e) {
            failWithActual(Fact.fact("Failed to translate to XML with exception", e));
            // must return fallback
            return this.asString();
        }
    }

    /**
     * Provided as a shortcut for a `asString().ignoringCase()`
     * 
     * @return
     */
    public CaseInsensitiveStringComparison ignoringCase() {
        return this.asString().ignoringCase();
    }

    /**
     * Provided as a shortcut for `asString().contains(string)`
     * 
     * @param string
     */
    public void contains(CharSequence string) {
        this.asString().contains(string);
    }

    /**
     * Provided as a shortcut for `asString().doesNotContain(string)`
     * 
     * @param string
     */
    public void doesNotContain(CharSequence string) {
        this.asString().doesNotContain(string);
    }
}
