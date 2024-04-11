package com.lhf.game.creature.conversation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.function.Function;

import com.lhf.Taggable;
import com.lhf.OutputBuilder.OutputBuilderElement;
import com.lhf.OutputBuilder.OutputSequenceElement;

public interface Transformer extends Function<OutputBuilderElement, OutputBuilderElement> {

    public String describePlainOutput();

    public String getOutputBody();

    public static Transformer ofBuilderElement(OutputBuilderElement toOut) {
        return new Transformer() {

            @Override
            public OutputBuilderElement apply(OutputBuilderElement arg0) {
                return toOut;
            }

            @Override
            public String describePlainOutput() {
                return toOut != null ? toOut.toString() : null;
            }

            @Override
            public String getOutputBody() {
                return toOut != null ? toOut.getCharSequenceAsString() : null;
            }

            @Override
            public String toString() {
                return this.describePlainOutput();
            }

        };
    }

    public static Transformer ofString(String body) {
        return Transformer.ofBuilderElement(OutputSequenceElement.ofCharSequence(body));
    }

    public static Transformer ofTaggable(Taggable taggable) {
        return Transformer.ofBuilderElement(OutputSequenceElement.ofTaggable(taggable));
    }

    public static Transformer ofMapping(Map<String, OutputBuilderElement> mapping) {
        return new Transformer() {

            @Override
            public OutputBuilderElement apply(OutputBuilderElement arg0) {
                if (arg0 == null || mapping == null) {
                    return arg0;
                }
                return mapping.getOrDefault(arg0.getMetaSignal(), arg0);
            }

            @Override
            public String describePlainOutput() {
                return mapping != null ? mapping.toString() : null;
            }

            @Override
            public String getOutputBody() {
                return mapping != null ? mapping.values().toString() : null;
            }

            @Override
            public String toString() {
                return this.describePlainOutput();
            }

        };
    }

    public static Transformer ofTransformerMapping(Map<String, Transformer> mapping) {
        return new Transformer() {

            @Override
            public OutputBuilderElement apply(OutputBuilderElement arg0) {
                if (arg0 == null || mapping == null) {
                    return arg0;
                }
                Transformer located = mapping.getOrDefault(arg0.getMetaSignal(), Transformer.ofBuilderElement(arg0));
                if (located == null) {
                    return arg0;
                }
                return located.apply(arg0);
            }

            @Override
            public String describePlainOutput() {
                return mapping != null ? mapping.toString() : null;
            }

            @Override
            public String getOutputBody() {
                return mapping != null ? mapping.values().toString() : null;
            }

            @Override
            public String toString() {
                return this.describePlainOutput();
            }

        };
    }

    public static class ConversationContext implements Transformer, Map<String, Transformer> {
        public enum ConversationContextKey {
            TALKER_NAME, TALKER_TAGGED_NAME, LISTENER_NAME, LISTENER_TAGGED_NAME;
        }

        private List<UUID> trail;
        private Map<String, Transformer> contextBag;

        public ConversationContext() {
            this.trail = new ArrayList<>();
            this.contextBag = new TreeMap<>();
        }

        public boolean addTrail(UUID nodeID) {
            if (nodeID.equals(this.getTrailEnd())) {
                return false;
            }
            return this.trail.add(nodeID);
        }

        public UUID getTrailEnd() {
            if (this.trail.size() <= 0) {
                return null;
            }
            return this.trail.get(this.trail.size() - 1);
        }

        public UUID backtrack() {
            if (this.trail.size() <= 0) {
                return null;
            }
            return this.trail.remove(this.trail.size() - 1);
        }

        @Override
        public void clear() {
            this.contextBag.clear();
        }

        public boolean containsKey(ConversationContextKey key) {
            return this.containsKey(key.name());
        }

        @Override
        public boolean containsKey(Object key) {
            return this.contextBag.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return this.contextBag.containsValue(value);
        }

        @Override
        public Set<Entry<String, Transformer>> entrySet() {
            return this.contextBag.entrySet();
        }

        public Transformer get(ConversationContextKey key) {
            return this.get(key.name());
        }

        @Override
        public Transformer get(Object key) {
            return this.contextBag.get(key);
        }

        @Override
        public boolean isEmpty() {
            return this.contextBag.isEmpty();
        }

        @Override
        public Set<String> keySet() {
            return this.contextBag.keySet();
        }

        public Transformer put(ConversationContextKey arg0, Transformer arg1) {
            return this.put(arg0.name(), arg1);
        }

        @Override
        public Transformer put(String arg0, Transformer arg1) {
            return this.contextBag.put(arg0, arg1);
        }

        @Override
        public void putAll(Map<? extends String, ? extends Transformer> m) {
            this.contextBag.putAll(m);
        }

        public Transformer remove(ConversationContextKey key) {
            return this.remove(key.name());
        }

        @Override
        public Transformer remove(Object key) {
            return this.contextBag.remove(key);
        }

        @Override
        public int size() {
            return this.contextBag.size();
        }

        @Override
        public Collection<Transformer> values() {
            return this.contextBag.values();
        }

        @Override
        public OutputBuilderElement apply(OutputBuilderElement input) {
            if (input == null) {
                return input;
            }
            String meta = input.getMetaSignal();
            Function<OutputBuilderElement, OutputBuilderElement> function = this.contextBag.getOrDefault(meta, null);
            if (function != null) {
                return function.apply(input);
            }
            return input;
        }

        @Override
        public String describePlainOutput() {
            return this.contextBag != null ? this.contextBag.toString() : null;
        }

        @Override
        public String getOutputBody() {
            return this.contextBag != null ? this.contextBag.values().toString() : null;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ConversationContext [trail=").append(trail).append(", contextBag=").append(contextBag)
                    .append("]");
            return builder.toString();
        }

    }

}
