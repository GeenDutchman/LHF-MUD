package com.lhf.game.creature.conversation;

import java.lang.reflect.Type;

import com.google.gson.InstanceCreator;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ConversationPatternSerializer
        implements JsonDeserializer<ConversationPattern>, JsonSerializer<ConversationPattern> {

    @Override
    public JsonElement serialize(ConversationPattern src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("regex", src.getRegex().pattern());
        object.addProperty("flags", src.flags());
        object.addProperty("example", src.getExample());
        return object;
    }

    @Override
    public ConversationPattern deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        String regex = object.get("regex").getAsString();
        int flags = object.get("flags").getAsInt();
        String example = object.get("example").getAsString();

        return new ConversationPattern(example, regex, flags);
    }

}
