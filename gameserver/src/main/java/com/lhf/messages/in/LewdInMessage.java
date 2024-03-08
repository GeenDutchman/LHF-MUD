package com.lhf.messages.in;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.lhf.game.creature.ICreatureBuildInfo;
import com.lhf.game.serialization.GsonBuilderFactory;
import com.lhf.messages.Command;
import com.lhf.messages.grammar.Prepositions;

public class LewdInMessage extends CommandAdapter {
    private List<ICreatureBuildInfo> cachedBuilders = new ArrayList<>();

    public LewdInMessage(Command command) {
        super(command);
    }

    public Set<String> getPartners() {
        if (this.getDirects().size() < 1) {
            return new TreeSet<>();
        }
        Set<String> partners = new TreeSet<>();
        partners.addAll(this.getDirects());
        return partners;
    }

    private final String[] split() {
        String[] splitten = this.getIndirects().getOrDefault(Prepositions.USE, "").split(Pattern.quote(", "));
        return splitten;
    }

    private final Set<String> makeSet(String[] splitten) {
        Set<String> babies = new TreeSet<>();
        if (splitten == null) {
            return babies;
        }
        for (String baby : splitten) {
            babies.add(baby);
        }
        return babies;
    }

    public Set<String> getNames() {
        if (this.getIndirects().size() < 1) {
            return new TreeSet<>();
        }
        String[] splitten = this.split();
        return this.makeSet(splitten);
    }

    private String getBuilderJSON() {
        return this.getIndirects().getOrDefault(Prepositions.JSON, null);
    }

    public synchronized List<ICreatureBuildInfo> getBuildInfos() {
        if (this.cachedBuilders != null && !this.cachedBuilders.isEmpty()) {
            return this.cachedBuilders;
        }
        final String json = this.getBuilderJSON();
        if (json != null) {
            final Type listType = new TypeToken<List<ICreatureBuildInfo>>() {
            }.getType();
            final Gson gson = new GsonBuilderFactory().creatureInfoBuilders().build();
            this.cachedBuilders = gson.fromJson(json, listType);
        }
        return this.cachedBuilders;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add(super.toString());
        sj.add("Partners:");
        Set<String> partners = this.getPartners();
        if (partners != null && partners.size() > 0) {
            sj.add(partners.toString());
        } else {
            sj.add("No partner specified");
        }
        Set<String> names = this.getNames();
        if (names != null && names.size() > 0) {
            sj.add("Baby Names:").add(names.toString());
        }
        return sj.toString();
    }
}
