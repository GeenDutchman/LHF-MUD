package com.lhf.game.item;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;

public class ItemDeserializer<T extends Item> implements JsonDeserializer<T> {

    private static final String CLASSNAME = "className";

    @Override
    public T deserialize(final JsonElement jsonElement, final Type type,
            final JsonDeserializationContext deserializationContext) throws JsonParseException {

        final JsonObject jsonObject = jsonElement.getAsJsonObject();
        if (jsonObject.size() == 0) {
            return null;
        }
        final JsonPrimitive prim = (JsonPrimitive) jsonObject.get(CLASSNAME);
        final String className = prim.getAsString();
        final Class<T> clazz = getClassInstance(className);
        return deserializationContext.deserialize(jsonObject, clazz);
    }

    @SuppressWarnings("unchecked")
    public Class<T> getClassInstance(String className) {
        try {
            return (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException cnfe) {
            throw new JsonParseException(cnfe.getMessage());
        }
    }

}