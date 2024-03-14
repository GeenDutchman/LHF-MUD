package com.lhf.messages.in;

import java.util.StringJoiner;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.lhf.game.creature.Player;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.serialization.GsonBuilderFactory;
import com.lhf.messages.Command;
import com.lhf.messages.grammar.Prepositions;

public class CreateInMessage extends CommandAdapter {
    private Player.PlayerBuildInfo cachedBuild = null;

    public CreateInMessage(Command command) {
        super(command);
    }

    public String getUsername() {
        if (this.getDirects().size() < 1) {
            return null;
        }
        return this.getDirects().get(0).trim();
    }

    public String getPassword() {
        return this.getFirstByPreposition(Prepositions.WITH);
    }

    private String vocationRequest() {
        return this.getFirstByPreposition(Prepositions.AS);
    }

    private String getBuilderJSON() {
        return this.getFirstByPreposition(Prepositions.JSON);
    }

    public synchronized Player.PlayerBuildInfo getBuildInfo() throws JsonParseException {
        if (cachedBuild != null) {
            return cachedBuild;
        }
        final String json = this.getBuilderJSON();
        if (json != null) {
            final Gson gson = new GsonBuilderFactory().creatureInfoBuilders().build();
            this.cachedBuild = gson.fromJson(json, Player.PlayerBuildInfo.class);
        } else if (this.vocationRequest() != null) {
            this.cachedBuild = new Player.PlayerBuildInfo(null).setName(this.getUsername())
                    .setVocation(VocationName.getVocationName(this.vocationRequest()));
        }
        return this.cachedBuild;
    }

    @Override
    public String toString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.add("Message:").add(this.getType().toString());
        sj.add("Valid:").add(this.isValid().toString());
        sj.add("Username:");
        if (this.getUsername() != null) {
            sj.add(this.getUsername());
        } else {
            sj.add("not provided");
        }
        sj.add("Password:");
        if (this.getPassword() != null) {
            sj.add("provided");
        } else {
            sj.add("not provided");
        }
        if (this.vocationRequest() != null) {
            sj.add("Requested to be: " + this.vocationRequest());
        } else if (this.getBuilderJSON() != null) {
            sj.add("Requested to build as:").add(this.getBuilderJSON());
        }
        return sj.toString();
    }

}
