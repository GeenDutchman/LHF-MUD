package com.lhf.messages;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.lhf.messages.grammar.Prepositions;
import com.lhf.messages.in.AMessageType;

public interface ICommand {
    public String getWhole();

    public AMessageType getType();

    public List<String> getDirects();

    public Boolean isValid();

    public default List<String> getByPreposition(Prepositions preposition) {
        final Map<Prepositions, List<String>> indirects = this.getIndirects();
        if (indirects == null || indirects.isEmpty()) {
            return null;
        }
        return indirects.getOrDefault(preposition, null);
    }

    public default String getFirstByPreposition(Prepositions preposition) {
        if (preposition == null) {
            return null;
        }
        final List<String> listing = this.getByPreposition(preposition);
        if (listing == null || listing.isEmpty()) {
            return null;
        }
        return listing.get(0);
    }

    public default String getByPrepositionAsString(Prepositions preposition) {
        if (preposition == null) {
            return null;
        }
        final List<String> listing = this.getByPreposition(preposition);
        if (listing == null || listing.isEmpty()) {
            return null;
        }
        return listing.stream().collect(Collectors.joining(", "));
    }

    public Map<Prepositions, List<String>> getIndirects();

    public default Map<Prepositions, String> getIndirectsAsStrings() {
        final Map<Prepositions, List<String>> indirects = this.getIndirects();
        if (indirects == null || indirects.isEmpty()) {
            return null;
        }
        EnumMap<Prepositions, String> mapping = new EnumMap<>(Prepositions.class);
        for (final Entry<Prepositions, List<String>> entry : indirects.entrySet()) {
            final Prepositions key = entry.getKey();
            final List<String> value = entry.getValue();
            if (key == null || value == null || value.isEmpty()) {
                continue;
            }
            final String valueString = value.stream().collect(Collectors.joining(", "));
            if (valueString == null || valueString.isBlank()) {
                continue;
            }
            mapping.put(key, valueString);
        }
        return mapping;
    }

}
