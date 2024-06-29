package com.lhf.game;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class Atlas<AtlasMemberType, AtlasMemberID extends Comparable<AtlasMemberID>, AtlasLinkType extends Comparable<AtlasLinkType>, AtlasTraversalTestType extends Comparable<AtlasTraversalTestType>>
        implements Comparable<Atlas<AtlasMemberType, AtlasMemberID, AtlasLinkType, AtlasTraversalTestType>> {

    public static sealed class AtlasException extends Exception
            permits AtlasMemberException, AtlasLinkException, AtlasTraversalException {
        public AtlasException(String message) {
            super(message);
        }

        public AtlasException(String message, Throwable cause) {
            super(message, cause);
        }

        public AtlasException(Throwable cause) {
            super(cause);
        }
    }

    public final static class AtlasMemberException extends AtlasException {
        final String memberID;

        public AtlasMemberException(String id) {
            super(String.format("No atlas member found with id '%s'"));
            this.memberID = id;
        }

        public AtlasMemberException(String id, Throwable cause) {
            super(String.format("No atlas member found with id '%s'"), cause);
            this.memberID = id;
        }

        public String getMemberID() {
            return memberID;
        }
    }

    public final static class AtlasLinkException extends AtlasException {
        final String source;
        final String through;
        final String destination;

        private static String composeDescription(String source, String through, String destination) {
            if (source != null && through != null && destination != null) {
                return String.format("No directed link found from source '%s' through '%s' to destination '%s'");
            } else if (source != null && through != null) {
                return String.format("No directed link found from source '%s' through '%s'", source, through);
            } else if (source != null && destination != null) {
                return String.format("No directed link found from source '%s' to destination '%s'", source,
                        destination);
            }
            return String.format("No directed link found from source '%s' through '%s' to destination '%s'");
        }

        public AtlasLinkException(String source, String through, String destination) {
            super(AtlasLinkException.composeDescription(source, through, destination));
            this.source = source;
            this.through = null;
            this.destination = destination;
        }

        public AtlasLinkException(String source, String through, String destination, Throwable cause) {
            super(AtlasLinkException.composeDescription(source, through, destination), cause);
            this.source = source;
            this.through = null;
            this.destination = destination;
        }

        public AtlasLinkException(String message, String source, String through, String destination, Throwable cause) {
            super(String.format("Error '%s' found with link from source '%s' through '%s' to destination '%s'", message,
                    source, through, destination), cause);
            this.source = source;
            this.through = null;
            this.destination = destination;
        }

        public String getSource() {
            return source;
        }

        public String getThrough() {
            return through;
        }

        public String getDestination() {
            return destination;
        }

    }

    public final static class AtlasTraversalException extends AtlasException {
        public enum TraversalExceptionType {
            NO_SOURCE, NO_DESTINATION, NO_LINK, FAIL_TRAVERSAL_TEST, OTHER;
        }

        private static String composeDescription(TraversalExceptionType exceptionType) {
            if (exceptionType == null) {
                return "Some other error occurred during traversal. ";
            }
            switch (exceptionType) {
            case FAIL_TRAVERSAL_TEST:
                return "The traversal test failed. ";
            case NO_DESTINATION:
                return "The destination member was not found. ";
            case NO_LINK:
                return "No link was found";
            case NO_SOURCE:
                return "The source member was not found. ";
            case OTHER:
                // fallthrough
            default:
                return "Some other error occurred during traversal. ";

            }
        }

        final TraversalExceptionType type;

        public static AtlasTraversalException failTraversalTest(AtlasException atlasException) {
            return new AtlasTraversalException(TraversalExceptionType.FAIL_TRAVERSAL_TEST, atlasException);
        }

        public static AtlasTraversalException noDestination(AtlasException atlasException) {
            return new AtlasTraversalException(TraversalExceptionType.NO_DESTINATION, atlasException);
        }

        public static AtlasTraversalException noLink(AtlasException atlasException) {
            return new AtlasTraversalException(TraversalExceptionType.NO_LINK, atlasException);
        }

        public static AtlasTraversalException noSource(AtlasException atlasException) {
            return new AtlasTraversalException(TraversalExceptionType.NO_SOURCE, atlasException);
        }

        protected AtlasTraversalException(TraversalExceptionType exceptionType) {
            super(AtlasTraversalException.composeDescription(exceptionType));
            this.type = exceptionType != null ? exceptionType : TraversalExceptionType.OTHER;
        }

        protected AtlasTraversalException(TraversalExceptionType exceptionType, AtlasException atlasException) {
            super(AtlasTraversalException.composeDescription(exceptionType), atlasException);
            this.type = exceptionType != null ? exceptionType : TraversalExceptionType.OTHER;
        }

        public TraversalExceptionType getType() {
            return type;
        }

    }

    private static final class TargetedTester<TargetLinkType extends Comparable<TargetLinkType>, TargetIDType extends Comparable<TargetIDType>, TargetTraversalTestType extends Comparable<TargetTraversalTestType>, TargetMemberType>
            implements
            Comparable<TargetedTester<TargetLinkType, TargetIDType, TargetTraversalTestType, TargetMemberType>> {
        private final TargetLinkType link;
        private final TargetIDType targetId;
        private final TargetTraversalTestType predicate;
        private final String externalReferenceLocality;
        private final String externalTargetName;
        private transient TargetMemberType externalTarget;

        protected TargetedTester(TargetLinkType link, TargetIDType targetId, TargetTraversalTestType predicate) {
            this.link = link;
            this.targetId = targetId;
            this.predicate = predicate;
            this.externalReferenceLocality = null;
            this.externalTargetName = null;
        }

        protected TargetedTester(TargetLinkType link, TargetIDType targetId, TargetTraversalTestType predicate,
                String externalReferenceLocality, String externalTargetName) {
            if (externalTargetName != null && externalReferenceLocality == null) {
                throw new IllegalArgumentException(String.format(
                        "If an external target name is provided ('%s') then the external reference locality must also be provided!",
                        externalTargetName));
            }
            this.link = link;
            this.targetId = targetId;
            this.predicate = predicate;
            this.externalReferenceLocality = externalReferenceLocality;
            this.externalTargetName = externalTargetName;
        }

        public TargetLinkType getLink() {
            return link;
        }

        public TargetIDType getTargetId() {
            return targetId;
        }

        public TargetTraversalTestType getPredicate() {
            return predicate;
        }

        public TargetMemberType getExternalTarget() {
            return externalTarget;
        }

        protected void setExternalTarget(TargetMemberType externalTarget) {
            this.externalTarget = externalTarget;
        }

        // FIXME: the external shouldn't be able to control this
        protected void populateExternalReference(BiFunction<String, String, TargetMemberType> populator) {
            if (populator != null && this.externalReferenceLocality != null) {
                this.externalTarget = populator.apply(externalReferenceLocality, externalTargetName);
            }
        }

        @Override
        public int compareTo(
                TargetedTester<TargetLinkType, TargetIDType, TargetTraversalTestType, TargetMemberType> other) {
            int comparison = this.link.compareTo(other.link);
            if (comparison != 0) {
                return comparison;
            }
            comparison = this.predicate.compareTo(other.predicate);
            if (comparison != 0) {
                return comparison;
            }

            if (this.externalReferenceLocality != null && other.externalReferenceLocality == null) {
                return -1; // external references come first
            } else if (this.externalReferenceLocality == null && other.externalReferenceLocality != null) {
                return 1;
            } else if (this.externalReferenceLocality != null && other.externalReferenceLocality != null) {
                comparison = this.externalReferenceLocality.compareTo(other.externalReferenceLocality);
                if (comparison != 0) {
                    return comparison;
                }
                if (this.externalTargetName != null && other.externalReferenceLocality == null) {
                    return -1; // specific references come first
                } else if (this.externalTargetName == null && other.externalTargetName != null) {
                    return 1;
                } else if (this.externalTargetName != null && other.externalTargetName != null) {
                    comparison = this.externalTargetName.compareTo(other.externalTargetName);
                    if (comparison != 0) {
                        return comparison;
                    }
                } // if both are null it doesn't matter
            } // if both are null it doesn't matter

            return this.targetId.compareTo(other.targetId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(link, targetId, predicate, externalReferenceLocality, externalTargetName);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof TargetedTester))
                return false;
            TargetedTester<?, ?, ?, ?> other = (TargetedTester<?, ?, ?, ?>) obj;
            return Objects.equals(link, other.link) && Objects.equals(targetId, other.targetId)
                    && Objects.equals(predicate, other.predicate)
                    && Objects.equals(externalReferenceLocality, other.externalReferenceLocality)
                    && Objects.equals(externalTargetName, other.externalTargetName);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("TargetedTester [link=").append(link).append(", targetId=").append(targetId)
                    .append(", predicate=").append(predicate).append(", externalReferenceLocality=")
                    .append(externalReferenceLocality).append(", externalTargetName=").append(externalTargetName)
                    .append("]");
            return builder.toString();
        }

    }

    private final static class AtlasMappingItem<MappingMember, MappingLinkType extends Comparable<MappingLinkType>, MappingTargetID extends Comparable<MappingTargetID>, MappingTraversalTestType extends Comparable<MappingTraversalTestType>>
            implements Serializable {
        private final MappingMember atlasMember;
        private final TreeSet<TargetedTester<MappingLinkType, MappingTargetID, MappingTraversalTestType, MappingMember>> links;
        private LinkedHashSet<MappingTargetID> backLinks;

        protected AtlasMappingItem(MappingMember atlasMember) {
            this.atlasMember = atlasMember;
            this.links = new TreeSet<>();
            this.backLinks = new LinkedHashSet<>();
        }

        public MappingMember getAtlasMember() {
            return atlasMember;
        }

        protected SortedSet<TargetedTester<MappingLinkType, MappingTargetID, MappingTraversalTestType, MappingMember>> getFilteredTargetedTesters(
                Predicate<MappingLinkType> linkTester) {
            final TreeSet<TargetedTester<MappingLinkType, MappingTargetID, MappingTraversalTestType, MappingMember>> testers = new TreeSet<>();
            this.links.stream().filter(tester -> tester != null).forEach(element -> testers.add(element));
            if (linkTester != null) {
                testers.removeIf(element -> !linkTester.test(element.link));
            }
            return Collections.unmodifiableSortedSet(testers);
        }

        protected SortedSet<TargetedTester<MappingLinkType, MappingTargetID, MappingTraversalTestType, MappingMember>> getTargetedTesters(
                MappingLinkType link) {
            if (link == null) {
                return Collections.unmodifiableSortedSet(new TreeSet<>());
            }
            return this.getFilteredTargetedTesters(tryLink -> link.equals(tryLink));
        }

        private synchronized void removeLinksTo(MappingTargetID forwardID) {
            if (forwardID == null || this.links == null) {
                return;
            }
            Iterator<TargetedTester<MappingLinkType, MappingTargetID, MappingTraversalTestType, MappingMember>> testerIter = this.links
                    .iterator();
            while (testerIter.hasNext()) {
                TargetedTester<MappingLinkType, MappingTargetID, MappingTraversalTestType, MappingMember> tester = testerIter
                        .next();
                if (tester == null) {
                    testerIter.remove();
                } else if (forwardID.equals(tester.targetId)) {
                    tester.setExternalTarget(null); // just in case
                    testerIter.remove();
                }
            }
        }

        private synchronized void addLink(
                TargetedTester<MappingLinkType, MappingTargetID, MappingTraversalTestType, MappingMember> tester) {
            if (tester == null) {
                return;
            }
            this.links.add(tester);
        }

        protected Map<MappingLinkType, Set<TargetedTester<MappingLinkType, MappingTargetID, MappingTraversalTestType, MappingMember>>> getLinksAsMap() {
            return this.links.stream().filter(entry -> entry != null)
                    .collect(Collectors.groupingBy(entry -> entry.getLink(), () -> new LinkedHashMap<>(),
                            Collectors.collectingAndThen(Collectors.toCollection(() -> new LinkedHashSet<>()),
                                    set -> Collections.unmodifiableSet(set))));
        }

        public Set<MappingLinkType> getAvailableLinks() {
            return Collections.unmodifiableSet(this.getLinksAsMap().keySet());
        }

        // FIXME: the external shouldn't be able to control this
        protected void populateExternalReferences(BiFunction<String, String, MappingMember> populator) {
            if (populator == null) {
                return;
            }
            for (final TargetedTester<MappingLinkType, MappingTargetID, MappingTraversalTestType, MappingMember> link : this.links) {
                if (link != null) {
                    link.populateExternalReference(populator);
                }
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(atlasMember);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof AtlasMappingItem))
                return false;
            AtlasMappingItem<?, ?, ?, ?> other = (AtlasMappingItem<?, ?, ?, ?>) obj;
            return Objects.equals(atlasMember, other.atlasMember);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("AtlasMappingItem [atlasMember=").append(atlasMember).append(", links=").append(links)
                    .append(", backLinks=").append(backLinks).append("]");
            return builder.toString();
        }

    }

    protected final UUID uuid = UUID.randomUUID();
    // TODO: wait for `SequencedCollection` from Java21
    private final LinkedHashMap<AtlasMemberID, AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType>> mapping;

    public abstract AtlasMemberID getIDForMemberType(AtlasMemberType member);

    public abstract String getNameForMemberType(AtlasMemberType member);

    protected Atlas() {
        this.mapping = new LinkedHashMap<>(); // keep insertion order
    }

    public AtlasTrawlerBuilder<AtlasMemberType, AtlasMemberID, AtlasLinkType, AtlasTraversalTestType> getTrawlerBuilder() {
        return AtlasTrawlerBuilder.trawl(this);
    }

    public synchronized int size() {
        return this.mapping.size();
    }

    public final synchronized void addMember(final AtlasMemberType nextMember) {
        this.emplaceMember(nextMember);
    }

    private final synchronized AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> emplaceMember(
            final AtlasMemberType nextMember) {
        synchronized (this.mapping) {
            if (nextMember == null) {
                throw new IllegalArgumentException("Cannot add null member!");
            }
            final AtlasMemberID nextMemberID = this.getIDForMemberType(nextMember);
            return this.mapping.computeIfAbsent(nextMemberID, key -> new AtlasMappingItem<>(nextMember));
        }
    }

    public final synchronized void connectOneWay(final AtlasMemberType first, final AtlasLinkType toSecond,
            final AtlasMemberType second, AtlasTraversalTestType predicate) throws AtlasMemberException {
        synchronized (this.mapping) {
            if (first == null) {
                throw new IllegalArgumentException("The 'first' argument must not be null!");
            } else if (second == null) {
                throw new IllegalArgumentException("the 'second' argument must not be null!");
            }

            if (toSecond == null) {
                throw new IllegalArgumentException("The provided link to the second must not be null!");
            }

            final AtlasMemberID firstID = this.getIDForMemberType(first);
            final AtlasMemberID secondID = this.getIDForMemberType(second);
            if (firstID == null) {
                throw new AtlasMemberException(null,
                        new IllegalStateException(String.format("Cannot retrieve member ID for %s", first)));
            } else if (secondID == null) {
                throw new AtlasMemberException(null,
                        new IllegalStateException(String.format("Cannot retrieve member ID for %s", second)));
            }

            AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> firstMappingItem = this
                    .getAtlasMappingItem(firstID);
            AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> secondMappingItem = this
                    .getAtlasMappingItem(secondID);
            if (firstMappingItem == null && secondMappingItem == null) {
                throw new AtlasMemberException(firstID.toString(),
                        new AtlasMemberException(secondID.toString(),
                                new IllegalStateException(String.format(
                                        "One or both of the following MUST already exist in the Atlas! %s %s", first,
                                        second))));
            } else if (firstMappingItem == null) {
                firstMappingItem = this.emplaceMember(first);
            } else if (secondMappingItem == null) {
                secondMappingItem = this.emplaceMember(second);
            }

            firstMappingItem.addLink(new TargetedTester<>(toSecond, secondID, predicate));
            secondMappingItem.backLinks.add(firstID);
        }
    }

    public final synchronized void connectOneWayExternally(final AtlasMemberType first, final AtlasLinkType toSecond,
            final AtlasTraversalTestType predicate, final String externalLocality, final String externalTargetName)
            throws AtlasMemberException {
        this.connectOneWayExternallyInternalFallback(first, toSecond, predicate, externalLocality, externalTargetName,
                null);
    }

    public final synchronized void connectOneWayExternallyInternalFallback(final AtlasMemberType first,
            final AtlasLinkType toSecond, final AtlasTraversalTestType predicate, final String externalLocality,
            final String externalTargetName, final AtlasMemberType fallback) throws AtlasMemberException {
        synchronized (this.mapping) {
            if (first == null) {
                throw new IllegalArgumentException("The 'first' argument must not be null!");
            } else if (externalTargetName != null && externalLocality == null) {
                throw new IllegalArgumentException(String.format(
                        "If the external target name is provided ('%s'), the external locality must be provided as well!",
                        externalTargetName));
            } else if (externalLocality == null) {
                throw new IllegalArgumentException(
                        "The external locality must be provided and the external target name is optional");
            }

            if (toSecond == null) {
                throw new IllegalArgumentException("The provided link to the second must not be null!");
            }

            final AtlasMemberID firstID = this.getIDForMemberType(first);
            if (firstID == null) {
                throw new AtlasMemberException(null,
                        new IllegalStateException(String.format("Cannot retrieve member ID for 'first' %s", first)));
            }

            AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> firstMappingItem = this
                    .getAtlasMappingItem(firstID);
            if (firstMappingItem == null) {
                throw new AtlasMemberException(firstID.toString());
            }

            AtlasMemberID fallbackID = null;
            if (fallback != null) {
                fallbackID = this.getIDForMemberType(fallback);
                if (fallbackID != null) {
                    AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> fallbackMappingItem = this
                            .getAtlasMappingItem(fallbackID);
                    if (fallbackMappingItem == null) {
                        fallbackMappingItem = this.emplaceMember(fallback);
                    }
                    fallbackMappingItem.backLinks.add(firstID);
                }
            }

            firstMappingItem.addLink(
                    new TargetedTester<>(toSecond, fallbackID, predicate, externalLocality, externalTargetName));
        }
    }

    public abstract AtlasLinkType translateLinkToOpposite(AtlasLinkType link);

    /**
     * Meant to be overridden. This is what is called when no predicate reverser is
     * given to connectTwoWay.
     * 
     * By default returns the input (identity function).
     * 
     * @param predicate
     * @return
     */
    public AtlasTraversalTestType reverseTraversalTest(AtlasTraversalTestType predicate) {
        return predicate;
    }

    public final synchronized void connectTwoWay(final AtlasMemberType first, final AtlasLinkType firstToSecond,
            final AtlasMemberType second, final AtlasTraversalTestType predicate,
            AtlasFunction<AtlasLinkType, AtlasLinkType> linkReverser,
            AtlasFunction<AtlasTraversalTestType, AtlasTraversalTestType> predicateReverser) throws AtlasException {
        if (firstToSecond == null) {
            throw new IllegalArgumentException("The provided link to the second must not be null!");
        }
        final AtlasLinkType secondToFirst = linkReverser != null ? linkReverser.apply(firstToSecond)
                : this.translateLinkToOpposite(firstToSecond);
        if (secondToFirst == null) {
            throw new IllegalArgumentException(String.format("The reversal of '%s' must not be null!", firstToSecond));
        }
        AtlasTraversalTestType reversedPredicate = predicateReverser != null ? predicateReverser.apply(predicate)
                : this.reverseTraversalTest(predicate);
        synchronized (this.mapping) {
            this.connectOneWay(first, firstToSecond, second, predicate);
            this.connectOneWay(second, secondToFirst, first, reversedPredicate);
        }
    }

    public final synchronized void connect(AtlasMemberType first, AtlasLinkType toSecond, AtlasMemberType second,
            AtlasTraversalTestType predicate) throws AtlasException {
        synchronized (this.mapping) {
            this.connectTwoWay(first, toSecond, second, predicate, this::translateLinkToOpposite,
                    this::reverseTraversalTest);
        }
    }

    // FIXME: the external shouldn't be able to control this
    public final synchronized void populateExternalReferences(BiFunction<String, String, AtlasMemberType> populator) {
        if (populator == null) {
            return;
        }
        synchronized (this.mapping) {
            for (final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> mapMember : this.mapping
                    .values()) {
                if (mapMember != null) {
                    mapMember.populateExternalReferences(populator);
                }
            }
        }
    }

    public final synchronized void removeMemberAndChildren(AtlasMemberType rootToRemove) {
        if (rootToRemove == null) {
            return;
        }
        final AtlasMemberID rootMemberID = this.getIDForMemberType(rootToRemove);
        this.removeMemberAndChildren(rootMemberID);
    }

    public final synchronized void removeMemberAndChildren(AtlasMemberID rootMemberIDToRemove) {
        if (rootMemberIDToRemove == null) {
            return;
        }
        Atlas<AtlasMemberType, AtlasMemberID, AtlasLinkType, AtlasTraversalTestType>.DepthFirstMappingItemIterator iter = new DepthFirstMappingItemIterator(
                rootMemberIDToRemove);
        while (iter.hasNext()) {
            iter.remove();
        }
    }

    private final Set<AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType>> getAtlasMappingItems() {
        synchronized (this.mapping) {
            LinkedHashSet<AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType>> mappingSet = new LinkedHashSet<>();
            for (final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> mappingItem : this.mapping
                    .values()) {
                if (mappingItem == null) {
                    continue;
                }
                mappingSet.add(mappingItem);
            }
            return Collections.unmodifiableSet(mappingSet);
        }
    }

    private final Optional<AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType>> getFirstMappingItem() {
        synchronized (this.mapping) {
            return this.mapping.values().stream().filter(item -> item != null).findFirst();
        }
    }

    public final Map<AtlasMemberID, AtlasMemberType> getAtlasMap() {
        synchronized (this.mapping) {
            LinkedHashMap<AtlasMemberID, AtlasMemberType> mappingMap = new LinkedHashMap<>();
            for (final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> mappingItem : this.mapping
                    .values()) {
                if (mappingItem == null) {
                    continue;
                }
                final AtlasMemberType member = mappingItem.getAtlasMember();
                if (member == null) {
                    continue;
                }
                final AtlasMemberID id = this.getIDForMemberType(member);
                if (id == null) {
                    continue;
                }
                mappingMap.put(id, member);
            }
            return Collections.unmodifiableMap(mappingMap);
        }
    }

    public final Set<AtlasMemberType> getAtlasMembers() {
        synchronized (this.mapping) {
            LinkedHashSet<AtlasMemberType> mappingSet = new LinkedHashSet<>();
            for (final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> mappingItem : this.mapping
                    .values()) {
                if (mappingItem == null) {
                    continue;
                }
                final AtlasMemberType member = mappingItem.getAtlasMember();
                if (member == null) {
                    continue;
                }
                mappingSet.add(member);
            }
            return Collections.unmodifiableSet(mappingSet);
        }
    }

    public final AtlasMemberType getFirstMember() {
        synchronized (this.mapping) {
            Optional<AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType>> firstItem = this
                    .getFirstMappingItem();
            if (firstItem == null || firstItem.isEmpty()) {
                return null;
            }
            return firstItem.get().getAtlasMember();
        }
    }

    private final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> getAtlasMappingItem(
            AtlasMemberID memberID) {
        synchronized (this.mapping) {
            if (memberID == null) {
                return null;
            }
            return this.mapping.get(memberID);
        }
    }

    public final AtlasMemberType getAtlasMemberOrNull(AtlasMemberID memberId) {
        synchronized (this.mapping) {
            AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> item = this
                    .getAtlasMappingItem(memberId);
            if (item == null) {
                return null;
            }
            return item.getAtlasMember();
        }
    }

    public final AtlasMemberType getAtlasMemberOrThrow(AtlasMemberID memberId) throws AtlasMemberException {
        synchronized (this.mapping) {
            AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> item = this
                    .getAtlasMappingItem(memberId);
            if (item == null) {
                throw new AtlasMemberException(memberId.toString(),
                        new NoSuchElementException(String.format("No element found for id '%s'", memberId)));
            }
            AtlasMemberType retrieved = item.getAtlasMember();
            if (retrieved == null) {
                throw new AtlasMemberException(memberId.toString(),
                        new NoSuchElementException(String.format("Null entry found for id '%s'", memberId)));
            }
            return retrieved;
        }
    }

    public final Set<AtlasLinkType> getLinksForMember(AtlasMemberID memberID) {
        final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> member = this
                .getAtlasMappingItem(memberID);
        return member != null ? member.getAvailableLinks() : Set.of();
    }

    public final Set<AtlasMemberID> getTargetsFromMember(AtlasMemberID from, Predicate<AtlasLinkType> through) {
        final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> member = this
                .getAtlasMappingItem(from);
        if (member == null) {
            return Set.of();
        }
        SortedSet<TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType, AtlasMemberType>> testers = member
                .getFilteredTargetedTesters(through);
        if (testers == null) {
            return Set.of();
        }
        return testers.stream().map(tester -> tester.targetId).collect(Collectors.toSet());
    }

    public final AtlasMemberID getTargetFromMemberOrNull(AtlasMemberID from, AtlasLinkType through) {
        final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> member = this
                .getAtlasMappingItem(from);
        if (member == null) {
            return null;
        }
        final SortedSet<TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType, AtlasMemberType>> targeted = member
                .getTargetedTesters(through);
        if (targeted == null) {
            return null;
        }
        final TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType, AtlasMemberType> first = targeted
                .first();
        if (first == null) {
            return null;
        }
        return first.targetId;
    }

    public final AtlasMemberID getTargetFromMemberOrThrow(AtlasMemberID from, AtlasLinkType through)
            throws AtlasMemberException, AtlasLinkException {
        final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> member = this
                .getAtlasMappingItem(from);
        if (member == null) {
            throw new AtlasMemberException(from.toString(),
                    new NoSuchElementException(String.format("No element found for id '%s'", from)));
        }
        final SortedSet<TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType, AtlasMemberType>> targeted = member
                .getTargetedTesters(through);
        if (targeted == null) {
            throw new AtlasLinkException(from.toString(), through.toString(), null, new IllegalStateException(
                    String.format("Null set of links found for '%s' through '%s'", from, through)));
        }
        final TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType, AtlasMemberType> first = targeted
                .first();
        if (first == null || first.targetId == null) {
            throw new AtlasLinkException(from.toString(), through.toString(), null);
        }
        return first.targetId;
    }

    public final SortedSet<AtlasTraversalTestType> getTraversalTestsFromMember(AtlasMemberID from,
            AtlasLinkType through) {
        SortedSet<AtlasTraversalTestType> collected = new TreeSet<>();
        final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> member = this
                .getAtlasMappingItem(from);
        if (member == null) {
            return Collections.unmodifiableSortedSet(collected);
        }
        final SortedSet<TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType, AtlasMemberType>> targeted = member
                .getTargetedTesters(through);
        if (targeted == null) {
            return Collections.unmodifiableSortedSet(collected);
        }
        for (TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType, AtlasMemberType> tester : targeted) {
            if (tester != null && tester.predicate != null) {
                collected.add(tester.predicate);
            }
        }
        return Collections.unmodifiableSortedSet(collected);
    }

    public final AtlasTraversalTestType getTraversalTestFromMemberOrNull(AtlasMemberID from, AtlasLinkType through) {
        final SortedSet<AtlasTraversalTestType> testSet = this.getTraversalTestsFromMember(from, through);
        if (testSet == null || testSet.isEmpty()) {
            return null;
        }
        return testSet.first();
    }

    public final AtlasTraversalTestType getTraversalTestFromMemberOrThrow(AtlasMemberID from, AtlasLinkType through)
            throws AtlasMemberException, AtlasLinkException {
        final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> member = this
                .getAtlasMappingItem(from);
        if (member == null) {
            throw new AtlasMemberException(from.toString(),
                    new NoSuchElementException(String.format("No element found for id '%s'", from)));
        }
        final SortedSet<TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType, AtlasMemberType>> targeted = member
                .getTargetedTesters(through);
        if (targeted == null) {
            throw new AtlasLinkException(from.toString(), through.toString(), null, new IllegalStateException(
                    String.format("Null set of links found for '%s' through '%s'", from, through)));
        }
        final TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType, AtlasMemberType> first = targeted
                .first();
        if (first == null || first.predicate == null) {
            throw new AtlasLinkException("No traversal test", from.toString(), through.toString(),
                    first != null && first.targetId != null ? first.targetId.toString() : null, null);
        }
        return first.predicate;
    }

    public final SortedSet<AtlasLinkType> getLinksBetween(AtlasMemberID here, AtlasMemberID there) {
        TreeSet<AtlasLinkType> collected = new TreeSet<>();
        if (here == null || there == null) {
            return Collections.unmodifiableSortedSet(collected);
        }
        final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> member = this
                .getAtlasMappingItem(here);
        if (member == null || member.links == null) {
            return Collections.unmodifiableSortedSet(collected);
        }
        for (TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType, AtlasMemberType> link : member.links) {
            if (link == null) {
                continue;
            }
            if (there.equals(link.targetId)) {
                collected.add(link.link);
            }
        }
        return Collections.unmodifiableSortedSet(collected);
    }

    public final AtlasLinkType getLinkTypeBetweenOrNull(AtlasMemberID here, AtlasMemberID there) {
        final SortedSet<AtlasLinkType> linksSet = this.getLinksBetween(here, there);
        if (linksSet == null) {
            return null;
        }
        return linksSet.first();
    }

    public final AtlasLinkType getLinkTypeBetweenOrThrow(AtlasMemberID here, AtlasMemberID there)
            throws AtlasMemberException, AtlasLinkException {
        final SortedSet<AtlasLinkType> linksSet = this.getLinksBetween(here, there);
        if (linksSet == null) {
            throw new AtlasLinkException(here.toString(), null, there.toString(),
                    new IllegalStateException(String.format("Null set of links")));
        } else if (linksSet.isEmpty()) {
            throw new AtlasLinkException(here.toString(), null, there.toString());
        }
        final AtlasLinkType theLink = linksSet.first();
        if (theLink == null) {
            throw new AtlasLinkException(here.toString(), null, there.toString(),
                    new NullPointerException("First link in set is null"));
        }
        return theLink;
    }

    public final AtlasTraversalTestType getTraversalBetweenOrNull(AtlasMemberID here, AtlasMemberID there) {
        if (here == null || there == null) {
            return null;
        }
        final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> member = this
                .getAtlasMappingItem(here);
        if (member == null || member.links == null) {
            return null;
        }
        for (TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType, AtlasMemberType> link : member.links) {
            if (link == null) {
                continue;
            }
            if (there.equals(link.targetId)) {
                return link.getPredicate();
            }
        }
        return null;
    }

    @FunctionalInterface
    public static interface ContextualTraversalPredicate<ContextualMemberType, ContextualLinkType extends Comparable<ContextualLinkType>, ContextualTraversalType extends Comparable<ContextualTraversalType>> {
        public boolean test(ContextualTraversalType traversalTester, ContextualMemberType source,
                ContextualLinkType link, ContextualMemberType destination);
    }

    public final AtlasMemberType attemptTraversal(AtlasMemberID from, AtlasLinkType through,
            ContextualTraversalPredicate<AtlasMemberType, AtlasLinkType, AtlasTraversalTestType> traversalPredicate)
            throws AtlasTraversalException {
        if (traversalPredicate == null || through == null) {
            throw new IllegalArgumentException("TravelPredicate and Through should not be null!");
        }
        return this.attemptAllTraversals(from, link -> through.equals(link), traversalPredicate, false);
    }

    public final AtlasMemberType attemptAllTraversals(AtlasMemberID from, Predicate<AtlasLinkType> throughPredicate,
            ContextualTraversalPredicate<AtlasMemberType, AtlasLinkType, AtlasTraversalTestType> traversalPredicate,
            boolean ignoreLinkFailure) throws AtlasTraversalException {
        if (throughPredicate == null || traversalPredicate == null) {
            throw new IllegalArgumentException("TravelPredicate and Through should not be null!");
        }
        final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> member = this
                .getAtlasMappingItem(from);
        if (member == null) {
            throw AtlasTraversalException.noSource(new AtlasMemberException(from.toString()));
        } else if (member.links == null) {
            throw AtlasTraversalException.noLink(new AtlasLinkException(from.toString(), null, null,
                    new IllegalStateException("Atlas member has null associated links")));
        }
        final SortedSet<TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType, AtlasMemberType>> testers = member
                .getFilteredTargetedTesters(throughPredicate);
        if (testers == null || testers.isEmpty()) {
            throw AtlasTraversalException
                    .noLink(new AtlasLinkException(from.toString(), "provided predicate", null, null));
        }
        for (TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType, AtlasMemberType> targetedTester : testers) {
            if (targetedTester == null || targetedTester.targetId == null) {
                if (ignoreLinkFailure) {
                    continue;
                }
                throw AtlasTraversalException.noLink(new AtlasLinkException(from.toString(), "provided predicate", null,
                        new IllegalStateException("Atlas link is null")));
            }
            AtlasMemberType destination = targetedTester.getExternalTarget();
            if (destination != null && traversalPredicate.test(targetedTester.predicate, member.atlasMember,
                    targetedTester.link, destination)) {
                return destination;
            }
            try {
                destination = this.getAtlasMemberOrThrow(targetedTester.targetId);
            } catch (AtlasMemberException e) {
                if (ignoreLinkFailure) {
                    continue;
                }
                throw AtlasTraversalException.noDestination(e);
            }
            if (traversalPredicate.test(targetedTester.predicate, member.atlasMember, targetedTester.link,
                    destination)) {
                return destination;
            } else if (!ignoreLinkFailure) {
                throw AtlasTraversalException.failTraversalTest(new AtlasLinkException("failed traversal test",
                        from.toString(), "provided predicate", targetedTester.link.toString(), null));
            }
        }
        throw AtlasTraversalException.noLink(new AtlasLinkException(from.toString(), "provided predicate", null));
    }

    public final UUID getUuid() {
        return uuid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof Atlas))
            return false;
        Atlas<?, ?, ?, ?> other = (Atlas<?, ?, ?, ?>) obj;
        return Objects.equals(uuid, other.uuid) && Objects.equals(mapping, other.mapping);
    }

    @Override
    public int compareTo(Atlas<AtlasMemberType, AtlasMemberID, AtlasLinkType, AtlasTraversalTestType> other) {
        return this.uuid.compareTo(other.uuid);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Atlas [uuid=").append(uuid).append(", mapping=").append(mapping).append("]");
        return builder.toString();
    }

    protected class DepthFirstMappingItemIterator implements
            Iterator<AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType>> {
        protected Deque<AtlasMemberID> stack;
        protected LinkedHashSet<AtlasMemberID> visited;
        protected AtlasMemberID cursor;

        public DepthFirstMappingItemIterator(AtlasMemberID startID) {
            this.stack = new ArrayDeque<>();
            this.visited = new LinkedHashSet<>();
            if (startID != null) {
                this.stack.push(startID);
            }
            this.cursor = null;
        }

        @Override
        public void remove() {
            if (this.cursor == null) {
                throw new IllegalStateException("Not in state to remove the current item");
            }
            synchronized (Atlas.this.mapping) {
                final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> mappingItem = Atlas.this.mapping
                        .remove(this.cursor);
                this.cursor = null;
                if (mappingItem == null || mappingItem.backLinks == null) {
                    return;
                }
                for (final AtlasMemberID back : mappingItem.backLinks) {
                    final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> parentItem = Atlas.this.mapping
                            .getOrDefault(back, null);
                    if (parentItem == null) {
                        continue;
                    }
                    parentItem.removeLinksTo(back);
                }
            }
        }

        @Override
        public boolean hasNext() {
            return !this.stack.isEmpty();
        }

        @Override
        public AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }

            while (!this.stack.isEmpty()) {
                final AtlasMemberID current = this.stack.pop();
                final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> mappingItem = getAtlasMappingItem(
                        current);
                if (mappingItem == null) {
                    continue;
                }
                if (!this.visited.contains(current)) {
                    final Collection<TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType, AtlasMemberType>> targetedTesters = mappingItem
                            .getFilteredTargetedTesters(null);
                    for (final TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType, AtlasMemberType> tester : targetedTesters) {
                        final AtlasMemberID targetID = tester.getTargetId();
                        if (targetID != null && !this.visited.contains(targetID)) {
                            this.stack.push(targetID);
                        }
                    }
                    this.visited.add(current);
                    this.stack.push(current);
                    this.cursor = current;
                    return mappingItem;
                }
            }

            this.cursor = null;
            throw new NoSuchElementException("End of Line");
        }

    }

    protected class CompleteDepthFirstIterator extends DepthFirstMappingItemIterator {
        private Iterator<AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType>> innerIterator = Atlas.this
                .getAtlasMappingItems().iterator(); // inner iterator is off of a snapshot, so no
                                                    // concurrentmofificationexception here!

        public CompleteDepthFirstIterator() {
            super(Atlas.this.getIDForMemberType(Atlas.this.getFirstMember()));
        }

        @Override
        public boolean hasNext() {
            return (!this.stack.isEmpty() || this.innerIterator.hasNext()) && this.visited.size() < Atlas.this.size();
        }

        @Override
        public AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }

            if (this.stack.isEmpty()) {
                while (this.innerIterator.hasNext()) { // for each subtree
                    final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> member = this.innerIterator
                            .next();
                    final AtlasMemberID checkId = Atlas.this.getIDForMemberType(member.getAtlasMember());
                    if (checkId == null || this.visited.contains(checkId)) {
                        continue;
                    }
                    this.stack.push(checkId);
                    break;
                }
            }

            return super.next();
        }

    }

    public class DepthFirstIterator implements Iterator<AtlasMemberType> {
        private DepthFirstMappingItemIterator inner;

        public DepthFirstIterator() {
            this.inner = new CompleteDepthFirstIterator();
        }

        public DepthFirstIterator(AtlasMemberID startID) {
            this.inner = new DepthFirstMappingItemIterator(startID);
        }

        @Override
        public boolean hasNext() {
            return this.inner.hasNext();
        }

        @Override
        public AtlasMemberType next() {
            return this.inner.next().getAtlasMember();
        }

        @Override
        public void remove() {
            this.inner.remove();
        }
    }

    public DepthFirstIterator depthFirstIterator() {
        return new DepthFirstIterator();
    }

    @FunctionalInterface
    public interface AtlasFunction<T, R> {
        R apply(T var) throws AtlasException;

        default <V> AtlasFunction<V, R> compose(AtlasFunction<? super V, ? extends T> before) {
            Objects.requireNonNull(before);
            return (v) -> {
                return this.apply(before.apply(v));
            };
        }

        default <V> AtlasFunction<T, V> andThen(AtlasFunction<? super R, ? extends V> after) {
            Objects.requireNonNull(after);
            return (t) -> {
                return after.apply(this.apply(t));
            };
        }

        static <T> AtlasFunction<T, T> identity() {
            return (t) -> {
                return t;
            };
        }
    }

    public final <TranslateMemberType, TranslateID extends Comparable<TranslateID>, TranslateLinkType extends Comparable<TranslateLinkType>, TranslateTraversalTestType extends Comparable<TranslateTraversalTestType>> Map<AtlasMemberID, TranslateID> translate(
            Atlas<TranslateMemberType, TranslateID, TranslateLinkType, TranslateTraversalTestType> translation,
            AtlasFunction<AtlasMemberType, TranslateMemberType> memberTransformer,
            AtlasFunction<AtlasLinkType, TranslateLinkType> linkTransformer,
            AtlasFunction<AtlasTraversalTestType, TranslateTraversalTestType> traversalTestTransformer)
            throws AtlasException {

        if (translation == null) {
            throw new NullPointerException("Cannot translate to a null atlas!");
        }
        final Map<AtlasMemberID, TranslateID> visited = new LinkedHashMap<>();
        if (this.size() == 0) {
            return visited;
        }
        if (memberTransformer == null) {
            throw new IllegalArgumentException("Must provide a member transformer function");
        } else if (linkTransformer == null) {
            throw new IllegalArgumentException("Must provide a link transformer function");
        } else if (traversalTestTransformer == null) {
            throw new IllegalArgumentException("Must provide a traversal transformer function");
        }

        for (final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> mappingItem : this
                .getAtlasMappingItems()) {
            final AtlasMemberType member = mappingItem.getAtlasMember();
            if (member == null) {
                throw new NullPointerException(String.format("Mapping item '%s' must have a member!", mappingItem));
            }
            final AtlasMemberID mappingItemId = this.getIDForMemberType(mappingItem.getAtlasMember());
            final TranslateMemberType translatedMember = memberTransformer.apply(member);
            final TranslateID translatedID = translation.getIDForMemberType(translatedMember);
            translation.addMember(translatedMember);
            visited.put(mappingItemId, translatedID);
        }

        for (final Entry<AtlasMemberID, TranslateID> vistedEntry : visited.entrySet()) {
            final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> member = this
                    .getAtlasMappingItem(vistedEntry.getKey());
            final AtlasMappingItem<TranslateMemberType, TranslateLinkType, TranslateID, TranslateTraversalTestType> translatedMember = translation
                    .getAtlasMappingItem(vistedEntry.getValue());
            for (final TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType, AtlasMemberType> tester : member.links) {
                final AtlasMemberID targetMemberID = tester.getTargetId();
                final AtlasMappingItem<TranslateMemberType, TranslateLinkType, TranslateID, TranslateTraversalTestType> translatedTarget = translation
                        .getAtlasMappingItem(visited.get(targetMemberID));
                final AtlasLinkType link = tester.getLink();
                final TranslateMemberType accrossTranslation = translatedTarget.getAtlasMember();
                final AtlasTraversalTestType predicate = tester.getPredicate();
                try {
                    translation.connectOneWay(translatedMember.getAtlasMember(), linkTransformer.apply(link),
                            accrossTranslation, traversalTestTransformer.apply(predicate));
                } catch (AtlasMemberException e) {
                    throw new AtlasException(String.format(
                            "Cannot complete the translation linking of this Atlas '%s', especially between this member '%s' and the translation '%s'",
                            this, member, translatedMember), e);
                }
            }
        }

        return visited;
    }

    public final <TranslateMemberType, TranslateID extends Comparable<TranslateID>, TranslateLinkType extends Comparable<TranslateLinkType>, TranslateTraversalTestType extends Comparable<TranslateTraversalTestType>> Map<AtlasMemberID, TranslateID> translateToSuppliedAtlas(
            Supplier<Atlas<TranslateMemberType, TranslateID, TranslateLinkType, TranslateTraversalTestType>> starter,
            AtlasFunction<AtlasMemberType, TranslateMemberType> memberTransformer,
            AtlasFunction<AtlasLinkType, TranslateLinkType> linkTransformer,
            AtlasFunction<AtlasTraversalTestType, TranslateTraversalTestType> traversalTestTransformer)
            throws AtlasException {
        if (starter == null) {
            throw new IllegalArgumentException("Must provide an Atlas supplier for translation!");
        }
        final Atlas<TranslateMemberType, TranslateID, TranslateLinkType, TranslateTraversalTestType> translation = starter
                .get();
        return this.translate(translation, memberTransformer, linkTransformer, traversalTestTransformer);
    }

    @Deprecated
    public final String toMermaidFlowchart(boolean fence) {
        StringBuilder sb = new StringBuilder();
        StringBuilder edges = new StringBuilder();
        if (fence) {
            sb.append("```mermaid").append("\r\n");
        }
        sb.append("flowchart LR").append("\r\n");
        for (final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> mapItem : this
                .getAtlasMappingItems()) {
            final AtlasMemberType member = mapItem.getAtlasMember();
            final String uuid = this.getIDForMemberType(member).toString();
            sb.append("    ").append(uuid).append("[").append(this.getNameForMemberType(member)).append("]\r\n");
            for (final TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType, AtlasMemberType> dir : mapItem.links) {
                String otherUUID = dir.getTargetId().toString();
                edges.append("    ").append(uuid).append("-->|").append(dir.getLink()).append("|").append(otherUUID)
                        .append("\r\n");
            }
        }
        sb.append("\r\n");
        sb.append(edges.toString());
        if (fence) {
            sb.append("```").append("\r\n");
        }
        return sb.toString();
    }

    public abstract class AtlasToMermaidWriter {
        public final String indent;

        protected AtlasToMermaidWriter(String indent) {
            this.indent = indent != null && indent.length() > 0 ? indent : "    ";
        }

        protected abstract String displayID(AtlasMemberID id);

        protected abstract String displayLink(AtlasLinkType link);

        protected abstract String displayTraversalTest(AtlasTraversalTestType traversal);

        protected abstract String displayMemberNote(AtlasMemberType member);

        protected String startsWith(AtlasMemberType starter) {
            StringBuilder sb = new StringBuilder();
            if (starter != null) {
                sb.append(this.indent).append("[*] --> ");
                sb.append(this.displayID(Atlas.this.getIDForMemberType(starter)).replace("-", ""));
                sb.append("\r\n");
            }
            return sb.toString();
        }

        private final String forTesters(String id,
                Collection<TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType, AtlasMemberType>> set) {
            StringBuilder sb = new StringBuilder();
            if (set == null || id == null) {
                return sb.toString();
            }

            for (final TargetedTester<AtlasLinkType, AtlasMemberID, AtlasTraversalTestType, AtlasMemberType> targeted : set) {
                if (targeted == null) {
                    continue;
                }
                sb.append(this.indent).append(id).append(" --> ")
                        .append(this.displayID(targeted.targetId).replace("-", "")).append(" : ");
                if (targeted.link != null) {
                    sb.append(this.displayLink(targeted.link)).append(" ");
                }
                if (targeted.predicate != null) {
                    sb.append(this.displayTraversalTest(targeted.predicate));
                }
                sb.append("\r\n");
            }
            return sb.toString();
        }

        public final String printStateDiagram(boolean fence, boolean includeStart) {
            StringBuilder sb = new StringBuilder();
            StringBuilder linkBuilder = new StringBuilder();
            if (fence) {
                sb.append("```mermaid\r\n");
            }
            sb.append("stateDiagram-v2\r\n");

            if (includeStart) {
                linkBuilder.append(this.startsWith(getFirstMember()));
            }

            for (final AtlasMappingItem<AtlasMemberType, AtlasLinkType, AtlasMemberID, AtlasTraversalTestType> mappingItem : Atlas.this.mapping
                    .values()) {
                final AtlasMemberType member = mappingItem.getAtlasMember();
                if (member == null) {
                    continue;
                }
                final String id = this.displayID(Atlas.this.getIDForMemberType(member)).replace("-", "");
                sb.append(indent).append(id).append(":").append(Atlas.this.getNameForMemberType(member)).append("\r\n");
                final String note = this.displayMemberNote(member);
                if (note != null && !note.isBlank()) {
                    if (note != null && !note.isBlank()) {
                        sb.append(indent).append("note right of ").append(id).append("\r\n");
                        for (String part : note.split("\\r?\\n")) {
                            sb.append(indent + indent).append(part).append("\r\n");
                        }
                        sb.append(indent).append("end note\r\n");
                    }
                }

                linkBuilder.append(this.forTesters(id, mappingItem.links));
            }

            sb.append(linkBuilder.toString()).append("\r\n");

            if (fence) {
                sb.append("```\r\n");
            }

            return sb.toString();
        }

        @Override
        public String toString() {
            return this.printStateDiagram(false, true);
        }

    }

    public abstract AtlasToMermaidWriter generateMermaidWriter(String indent);

}
