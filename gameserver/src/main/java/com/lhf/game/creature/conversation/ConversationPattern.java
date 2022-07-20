package com.lhf.game.creature.conversation;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConversationPattern implements Serializable, Comparable<ConversationPattern> {
    private final Pattern regex;
    private final String example;

    public ConversationPattern(String example, String regex) {
        this.regex = Pattern.compile(regex);
        this.example = example;
        this.checkExample();
    }

    public ConversationPattern(String example, String regex, int flags) {
        this.regex = Pattern.compile(regex, flags);
        this.example = example;
        this.checkExample();
    }

    private void checkExample() {
        Matcher matcher = this.regex.matcher(this.example);
        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    "Example " + this.example + " must follow regex " + this.regex.toString());
        }
    }

    public Pattern getRegex() {
        return regex;
    }

    public String getExample() {
        return example;
    }

    public Matcher matcher(CharSequence input) {
        return this.regex.matcher(input);
    }

    public int flags() {
        return this.regex.flags();
    }

    @Override
    public int hashCode() {
        return Objects.hash(example, regex);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ConversationPattern)) {
            return false;
        }
        ConversationPattern other = (ConversationPattern) obj;
        if (!this.regex.pattern().equals(other.regex.pattern())) {
            return false;
        }
        return Objects.equals(example, other.example);
    }

    @Override
    public int compareTo(ConversationPattern arg0) {
        if (arg0 == null) {
            throw new NullPointerException("Cannot compare null ConversationPattern");
        }
        if (this.equals(arg0)) {
            return 0;
        }
        // return longer, more exacting regexes as less than shorter ones
        int lenCompare = arg0.getRegex().toString().length() - this.regex.toString().length();
        if (lenCompare != 0) {
            return lenCompare;
        }
        int patternCompare = this.regex.toString().compareTo(arg0.getRegex().toString());
        if (patternCompare != 0) {
            return patternCompare;
        }
        return this.example.compareTo(arg0.getExample());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConversationPattern [example=").append(example).append(", regex=").append(regex).append("]");
        return builder.toString();
    }

}
