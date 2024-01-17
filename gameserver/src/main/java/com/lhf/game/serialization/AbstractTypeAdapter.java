/**
 * Sourced from https://github.com/albertattard/gson-typeadapterfactory-example/blob/da9c17cc0a91e5affb1ce2b2b5959a9286d73af7/src/main/java/com/javacreed/examples/gson/part4/AbstractTypeAdapter.java
 * With the following licence.
 * 
 * I'm the one who added the `rawString` methods
 */

/*
 * #%L
 * Gson TypeAdapterFactory Example
 * %%
 * Copyright (C) 2012 - 2015 Java Creed
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package com.lhf.game.serialization;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

public abstract class AbstractTypeAdapter<T> extends TypeAdapter<T> {

    private Gson gson;

    protected <E> void delegateWrite(final JsonWriter out, final E object) throws IOException {
        @SuppressWarnings("unchecked")
        final Class<E> type = (Class<E>) object.getClass();
        delegateWrite(out, object, type);
    }

    protected <E> void delegateWrite(final JsonWriter out, final E object, final Class<E> type) throws IOException {
        final TypeAdapter<E> typeAdapter = gson.getAdapter(type);
        typeAdapter.write(out, object);
    }

    protected <E> String rawString(final E object) {
        @SuppressWarnings("unchecked")
        final Class<E> type = (Class<E>) object.getClass();
        return rawString(object, type);
    }

    protected <E> String rawString(final E object, final Class<E> type) {
        return gson.toJson(object, type);
    }

    @Override
    public T read(final JsonReader in) throws IOException {
        throw new UnsupportedOperationException("Method not implemented");
    }

    public void setGson(final Gson gson) {
        this.gson = gson;
    }

    @Override
    public void write(final JsonWriter out, final T value) throws IOException {
        throw new UnsupportedOperationException("Method not implemented");
    }
}
