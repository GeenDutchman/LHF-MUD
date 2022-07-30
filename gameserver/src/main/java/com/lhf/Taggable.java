package com.lhf;

public interface Taggable {
    String getStartTag();

    String getEndTag();

    String getColorTaggedName();

    public static String extract(Taggable taggable) {
        int lenStart = taggable.getStartTag().length();
        int lenEnd = taggable.getEndTag().length();
        String extracted = taggable.getColorTaggedName();
        int extractSize = extracted.length() - lenEnd;
        extracted = extracted.substring(lenStart, extractSize);
        return extracted;
    }
}
