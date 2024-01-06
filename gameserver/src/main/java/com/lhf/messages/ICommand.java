package com.lhf.messages;

import java.util.List;
import java.util.Map;

import com.lhf.messages.grammar.Prepositions;

public interface ICommand {
    public String getWhole();

    public CommandMessage getType();

    public List<String> getDirects();

    public Boolean isValid();

    public String getByPreposition(Prepositions preposition);

    public Map<Prepositions, String> getIndirects();

}
