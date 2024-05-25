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
import com.lhf.RichOutput.RichOutputElement;

public interface ConversationTransformer extends Function<RichOutputElement, RichOutputElement> {

    public String describePlainOutput();

    public String getOutputBody();

    public static ConversationTransformer ofBuilderElement(RichOutputElement toOut) {
        return new ConversationTransformer() {

            @Override
            public RichOutputElement apply(RichOutputElement arg0) {
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

    public static ConversationTransformer ofString(String body) {
        return ConversationTransformer.ofBuilderElement(RichOutputElement.ofCharSequence(body));
    }

    public static ConversationTransformer ofTaggable(Taggable taggable) {
        return ConversationTransformer.ofBuilderElement(RichOutputElement.ofTaggable(taggable));
    }

    public static ConversationTransformer ofMapping(Map<String, RichOutputElement> mapping) {
        return new ConversationTransformer() {

            @Override
            public RichOutputElement apply(RichOutputElement arg0) {
                if (arg0 == null || mapping == null) {
                    return arg0;
                }
                String meta = arg0.getMetaSignal();
                if (meta == null) {
                    return arg0;
                }
                return mapping.getOrDefault(meta, arg0);
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

    public static ConversationTransformer ofTransformerMapping(Map<String, ConversationTransformer> mapping) {
        return new ConversationTransformer() {

            @Override
            public RichOutputElement apply(RichOutputElement arg0) {
                if (arg0 == null || mapping == null) {
                    return arg0;
                }
                String meta = arg0.getMetaSignal();
                if (meta == null) {
                    return arg0;
                }
                ConversationTransformer located = mapping.getOrDefault(meta,
                        ConversationTransformer.ofBuilderElement(arg0));
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

    public static ConversationTransformer ofTalkerAndListener(Taggable talker, Taggable listener) {
        return ConversationTransformer.ofTransformerMapping(Map.of(ConversationContextKey.TALKER_NAME.name(),
                ConversationTransformer.ofString(talker.getSimpleContent()),
                ConversationContextKey.TALKER_TAGGED_NAME.name(), ConversationTransformer.ofTaggable(talker),
                ConversationContextKey.LISTENER_NAME.name(),
                ConversationTransformer.ofString(listener.getSimpleContent()),
                ConversationContextKey.LISTENER_TAGGED_NAME.name(), ConversationTransformer.ofTaggable(listener)));
    }

    public static ConversationTransformer ofTalkerAndListener(String talker, String listener) {
        return ConversationTransformer.ofTalkerAndListener(Taggable.BasicTaggable.customTaggable("Creature", talker),
                Taggable.BasicTaggable.customTaggable("Creature", listener));
    }

    public enum ConversationContextKey {
        TALKER_NAME, TALKER_TAGGED_NAME, LISTENER_NAME, LISTENER_TAGGED_NAME;
    }

    public static class ConversationContext implements ConversationTransformer, Map<String, ConversationTransformer> {

        private List<UUID> trail;
        private Map<String, ConversationTransformer> contextBag;

        public ConversationContext() {
            this.trail = new ArrayList<>();
            this.contextBag = new TreeMap<>();
            this.contextBag.put(ConversationContextKey.TALKER_NAME.name(), ConversationTransformer.ofString("Someone"));
            this.contextBag.put(ConversationContextKey.TALKER_TAGGED_NAME.name(),
                    ConversationTransformer.ofTaggable(Taggable.BasicTaggable.customTaggable("Creature", "Someone")));
            this.contextBag.put(ConversationContextKey.LISTENER_NAME.name(),
                    ConversationTransformer.ofString("Somebody"));
            this.contextBag.put(ConversationContextKey.LISTENER_TAGGED_NAME.name(),
                    ConversationTransformer.ofTaggable(Taggable.BasicTaggable.customTaggable("Creature", "Somebody")));
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
        public Set<Entry<String, ConversationTransformer>> entrySet() {
            return this.contextBag.entrySet();
        }

        public ConversationTransformer get(ConversationContextKey key) {
            return this.get(key.name());
        }

        @Override
        public ConversationTransformer get(Object key) {
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

        public ConversationTransformer put(ConversationContextKey arg0, ConversationTransformer arg1) {
            return this.put(arg0.name(), arg1);
        }

        @Override
        public ConversationTransformer put(String arg0, ConversationTransformer arg1) {
            return this.contextBag.put(arg0, arg1);
        }

        @Override
        public void putAll(Map<? extends String, ? extends ConversationTransformer> m) {
            this.contextBag.putAll(m);
        }

        public ConversationTransformer remove(ConversationContextKey key) {
            return this.remove(key.name());
        }

        @Override
        public ConversationTransformer remove(Object key) {
            return this.contextBag.remove(key);
        }

        @Override
        public int size() {
            return this.contextBag.size();
        }

        @Override
        public Collection<ConversationTransformer> values() {
            return this.contextBag.values();
        }

        @Override
        public RichOutputElement apply(RichOutputElement input) {
            if (input == null) {
                return input;
            }
            String meta = input.getMetaSignal();
            if (meta == null) {
                return input;
            }
            Function<RichOutputElement, RichOutputElement> function = this.contextBag.getOrDefault(meta, null);
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
