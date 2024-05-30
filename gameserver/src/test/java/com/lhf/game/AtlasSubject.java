package com.lhf.game;

import java.util.function.Function;

import com.google.common.truth.ComparableSubject;
import com.google.common.truth.CustomSubjectBuilder;
import com.google.common.truth.FailureMetadata;
import com.google.common.truth.IntegerSubject;
import com.google.common.truth.IterableSubject;
import com.google.common.truth.MapSubject;
import com.google.common.truth.StringSubject;
import com.google.common.truth.Subject;
import com.google.common.truth.Truth;

public class AtlasSubject<AtlasMemberType, AtlasMemberID extends Comparable<AtlasMemberID>, AtlasLinkType extends Comparable<AtlasLinkType>, AtlasTraversalTestType extends Comparable<AtlasTraversalTestType>>
        extends ComparableSubject<Atlas<AtlasMemberType, AtlasMemberID, AtlasLinkType, AtlasTraversalTestType>> {

    public final static class AtlasSubjectBuilder extends CustomSubjectBuilder {

        AtlasSubjectBuilder(FailureMetadata metadata) {
            super(metadata);
        }

        public <Member, ID extends Comparable<ID>, Link extends Comparable<Link>, Traversal extends Comparable<Traversal>> AtlasSubject<Member, ID, Link, Traversal> that(
                Atlas<Member, ID, Link, Traversal> actual) {
            return new AtlasSubject<>(metadata(), actual);
        }

    }

    public static CustomSubjectBuilder.Factory<AtlasSubjectBuilder> atlases() {
        return AtlasSubjectBuilder::new;
    }

    public static <Member, ID extends Comparable<ID>, Link extends Comparable<Link>, Traversal extends Comparable<Traversal>> AtlasSubject<Member, ID, Link, Traversal> assertThat(
            Atlas<Member, ID, Link, Traversal> actual) {
        return Truth.assertAbout(atlases()).that(actual);
    }

    private final Atlas<AtlasMemberType, AtlasMemberID, AtlasLinkType, AtlasTraversalTestType> actual;

    protected AtlasSubject(FailureMetadata metadata,
            Atlas<AtlasMemberType, AtlasMemberID, AtlasLinkType, AtlasTraversalTestType> actual) {
        super(metadata, actual);
        this.actual = actual;
    }

    public IntegerSubject size() {
        return check("size()").that(actual.size());
    }

    public void hasSize(Integer size) {
        this.size().isEqualTo(size);
    }

    public IterableSubject members() {
        return check("getAtlasMembers()").that(actual.getAtlasMembers());
    }

    public MapSubject memberMap() {
        return check("getAtlasMap()").that(actual.getAtlasMap());
    }

    public Subject firstMember() {
        return check("getFirstMember()").that(actual.getFirstMember());
    }

    public Subject memberOrNull(AtlasMemberID id) {
        return check("getAtlasMemberOrNull(%s)", id).that(actual.getAtlasMemberOrNull(id));
    }

    public IterableSubject linksBetween(AtlasMemberID here, AtlasMemberID there) {
        return check("getLinksBetween(%s, %s)", here, there).that(actual.getLinksBetween(here, there));
    }

    public void hasLinkBetween(AtlasMemberID here, AtlasMemberID there) {
        this.linksBetween(here, there).isNotEmpty();
    }

    public StringSubject asMermaid() {
        final String indent = "    ";
        final boolean fence = false;
        final boolean includeStart = true;
        final Function<AtlasMemberID, String> idDisplay = id -> id != null ? id.toString() : "null";
        final Function<AtlasLinkType, String> linkDisplay = link -> link != null ? link.toString() : "null";
        final Function<AtlasTraversalTestType, String> traversalDisplay = test -> test != null ? test.toString()
                : "null";
        final Function<AtlasMemberType, String> memberDisplay = member -> member != null ? member.toString() : "null";
        return check("toStateDiagramMermaid(%s, %s, %s, toString, toString, toString, toString)", indent, fence,
                includeStart)
                        .that(actual.toStateDiagramMermaid(indent, fence, includeStart, idDisplay, linkDisplay,
                                traversalDisplay, memberDisplay));
    }

    public StringSubject asString() {
        return check("toString()").that(actual.toString());
    }

}
