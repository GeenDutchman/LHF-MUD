package com.lhf.messages.in;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.lhf.game.creature.ICreatureBuildInfo;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.serialization.GsonBuilderFactory;
import com.lhf.messages.Command;
import com.lhf.messages.grammar.Prepositions;

public class LewdInMessage extends CommandAdapter {
    private List<ICreatureBuildInfo> cachedBuilders = new ArrayList<>();

    public static class NameVocationPair {
        public final String name;
        public final VocationName vocation;

        public NameVocationPair(String name) {
            this.name = name;
            this.vocation = null;
        }

        public NameVocationPair(String name, String vocation) {
            this.name = name;
            this.vocation = VocationName.getVocationName(vocation);
        }

        public NameVocationPair(String name, VocationName vocation) {
            this.name = name;
            this.vocation = vocation;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if (this.name != null) {
                builder.append("Name: '").append(this.name).append("'' ");
            } else {
                builder.append("No name given ");
            }
            if (this.vocation != null) {
                builder.append("As: ").append(this.vocation);
            } else {
                builder.append("no vocation given");
            }
            return builder.toString();
        }

    }

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

    protected List<String> getNames() {
        return this.getByPreposition(Prepositions.USE);
    }

    protected List<VocationName> getVocationNames() {
        List<String> vNameListing = this.getByPreposition(Prepositions.AS);
        if (vNameListing == null) {
            return null;
        }
        return vNameListing.stream()
                .map(stringName -> VocationName.getVocationName(stringName)).filter(name -> name != null).toList();
    }

    public List<NameVocationPair> getNameVocationPairs() {
        List<NameVocationPair> pairs = new ArrayList<>();
        List<String> names = this.getNames();
        if (names == null) {
            names = List.of();
        }
        List<VocationName> vocations = this.getVocationNames();
        if (vocations == null) {
            vocations = List.of();
        }
        Iterator<String> nameIterator = names.iterator();
        Iterator<VocationName> vocationIterator = vocations.iterator();
        boolean hasNextName = nameIterator.hasNext();
        boolean hasNextVocation = nameIterator.hasNext();
        while (hasNextName || hasNextVocation) {
            pairs.add(new NameVocationPair(hasNextName ? nameIterator.next() : null,
                    hasNextVocation ? vocationIterator.next() : null));
            hasNextName = nameIterator.hasNext();
            hasNextVocation = vocationIterator.hasNext();
        }
        return pairs;
    }

    private String getBuilderJSON() {
        return this.getFirstByPreposition(Prepositions.JSON);
    }

    public synchronized List<ICreatureBuildInfo> getJSONBuildInfos() {
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
        List<String> names = this.getNames();
        if (names != null && names.size() > 0) {
            sj.add("Baby Names:").add(names.toString());
        }
        return sj.toString();
    }
}
