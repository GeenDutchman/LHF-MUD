package com.lhf.messages.events;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.function.Predicate;

import com.lhf.game.TickType;
import com.lhf.messages.GameEventType;

public class GameEventTester implements Predicate<GameEvent>, Comparable<GameEventTester>, Serializable {
    private final String className = this.getClass().getName();
    private final GameEventType type;
    private final SortedSet<String> contained;
    private final SortedSet<String> notContained;
    private final TickType tickType;

    public GameEventTester(GameEventType type, Collection<String> contained, Collection<String> notContained,
            TickType tickType) {
        this.type = type;
        this.contained = contained != null ? Collections.unmodifiableSortedSet(new TreeSet<>(contained)) : null;
        this.notContained = notContained != null ? Collections.unmodifiableSortedSet(new TreeSet<>(notContained))
                : null;
        this.tickType = tickType;
    }

    public GameEventTester(GameEventType type, String containedString) {
        this(type, List.of(containedString), null, null);
    }

    public GameEventTester(String containedString) {
        this(null, List.of(containedString), null, null);
    }

    public GameEventTester(GameEventType type) {
        this(type, null, null, null);
    }

    public GameEventTester(GameEventTester other) {
        this(other.type, other.contained, other.notContained, other.tickType);
    }

    public String getClassName() {
        return className;
    }

    public GameEventType getType() {
        return type;
    }

    public SortedSet<String> getContained() {
        return contained;
    }

    public SortedSet<String> getNotContained() {
        return notContained;
    }

    public TickType getTickType() {
        return tickType;
    }

    protected void failHook(final GameEvent argument, final String reason) {
    }

    protected void successHook(final GameEvent argument, final String reason) {
    }

    @Override
    public boolean test(GameEvent argument) {
        if (argument == null) {
            this.failHook(argument, "argument null, no match");
            return false;
        }

        if (this.type != null && this.type != argument.getEventType()) {
            this.failHook(argument, String.format("Expected type '%s', but got type '%s', no match", this.type,
                    argument.getEventType()));
            return false;
        }

        if (this.tickType != null && this.tickType != argument.getTickType()) {
            this.failHook(argument, String.format("Expected tick type '%s', but got type '%s', no match", this.tickType,
                    argument.getTickType()));
            return false;
        }

        final String argumentAsString = argument.toString();

        if (this.contained != null) {
            for (String words : this.contained) {
                if (!argumentAsString.contains(words)) {
                    this.failHook(argument, String.format("Expected words \"%s\" but not found, no match", words));
                    return false;
                }
            }
        }

        if (this.notContained != null) {
            for (String words : this.notContained) {
                if (argumentAsString.contains(words)) {
                    this.failHook(argument, String.format("not expected words \"%s\", but found, no match", words));
                    return false;
                }
            }
        }

        this.successHook(argument, argumentAsString);
        return true;
    }

    private final int compareType(GameEventType other) {
        if (this.type != null && other != null) {
            return this.type.compareTo(other);
        } else if (this.type == null && other != null) {
            return -1;
        } else if (this.type != null && other == null) {
            return 1;
        }
        return 0; // null and null
    }

    private final int compareTick(TickType other) {
        if (this.tickType != null && other != null) {
            return this.tickType.compareTo(other);
        } else if (this.tickType == null && other != null) {
            return -1;
        } else if (this.tickType != null && other == null) {
            return 1;
        }
        return 0; // null and null
    }

    private final int compareContained(SortedSet<String> other) {
        if (this.contained != null && other != null) {
            int comparison = this.contained.size() - other.size();
            if (comparison != 0) {
                return comparison;
            }
            Iterator<String> myIterator = this.contained.iterator();
            Iterator<String> otherIterator = other.iterator();
            while (myIterator.hasNext() && otherIterator.hasNext()) {
                comparison = myIterator.next().compareTo(otherIterator.next());
                if (comparison != 0) {
                    return comparison;
                }
            }
        } else if (this.contained == null && other == null) {
            return 0;
        } else if (this.contained == null && other != null) {
            return -1;
        } else if (this.contained != null && other == null) {
            return 1;
        }

        return 0;
    }

    private final int compareNotContained(SortedSet<String> other) {
        if (this.notContained != null && other != null) {
            int comparison = this.notContained.size() - other.size();
            if (comparison != 0) {
                return comparison;
            }
            Iterator<String> myIterator = this.notContained.iterator();
            Iterator<String> otherIterator = other.iterator();
            while (myIterator.hasNext() && otherIterator.hasNext()) {
                comparison = myIterator.next().compareTo(otherIterator.next());
                if (comparison != 0) {
                    return comparison;
                }
            }
        } else if (this.notContained == null && other == null) {
            return 0;
        } else if (this.notContained == null && other != null) {
            return -1;
        } else if (this.notContained != null && other == null) {
            return 1;
        }

        return 0;
    }

    @Override
    public int compareTo(GameEventTester other) {
        int comparison = this.compareType(other.type);
        if (comparison != 0) {
            return comparison;
        }
        comparison = this.compareTick(other.tickType);
        if (comparison != 0) {
            return comparison;
        }
        comparison = this.compareContained(other.contained);
        if (comparison != 0) {
            return comparison;
        }
        comparison = this.compareNotContained(other.notContained);
        if (comparison != 0) {
            return comparison;
        }

        return 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, contained, notContained, tickType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!(obj instanceof GameEventTester))
            return false;
        GameEventTester other = (GameEventTester) obj;
        return this.compareTo(other) == 0;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ", "Triggers when an event", "").setEmptyValue("is received");
        if (this.type != null) {
            sj.add(" has the type").add(this.type.toString() + ",");
        }
        if (this.tickType != null) {
            sj.add(" ticks like").add(this.tickType.toString() + ",");
        }
        if (this.contained != null && this.contained.size() > 0) {
            sj.add(" contains any of the phrases:").add(this.contained.toString() + ",");
        }
        if (this.notContained != null && this.notContained.size() > 0) {
            sj.add(" does not contain any of the phrases:").add(this.notContained.toString());
        }
        return sj.toString();
    }

}
