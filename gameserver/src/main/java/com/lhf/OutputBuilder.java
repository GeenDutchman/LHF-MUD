package com.lhf;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

public interface OutputBuilder {
    public default OutputBuilder appendString(String toAdd) {
        return this.appendString(toAdd, " ", null);
    }

    public default OutputBuilder appendChild(String toAdd) {
        return this.appendString(toAdd, " ", null);
    }

    public OutputBuilder appendString(String toAdd, String before, String after);

    public default OutputBuilder appendExaminable(Examinable toAdd) {
        return this.appendExaminable(toAdd, " ", null);
    }

    public OutputBuilder appendExaminable(Examinable toAdd, String before, String after);

    public default OutputBuilder appendTaggable(Taggable toAdd) {
        return this.appendTaggable(toAdd, " ", null);
    }

    public default OutputBuilder appendChild(Taggable toAdd) {
        return this.appendTaggable(toAdd);
    }

    public OutputBuilder appendTaggable(Taggable toAdd, String before, String after);

    public default <Tgg extends Taggable> OutputBuilder appendTaggables(Collection<Tgg> taggables) {
        return this.appendTaggables(taggables, ", ", null, null, null);
    }

    public default <Tgg extends Taggable> OutputBuilder appendTaggables(Collection<Tgg> taggables, String separator,
            String before, String after, String empty) {
        if (before != null) {
            this.appendString(before);
        }
        if (taggables == null || taggables.isEmpty()) {
            if (empty != null) {
                this.appendString(empty);
            }
        } else if (taggables.size() == 1) {
            this.appendTaggable(taggables.stream().findAny().get());
        } else {
            boolean first = true;
            for (Taggable taggable : taggables) {
                this.appendTaggable(taggable, first ? " " : separator, null);
                first = false;
            }
        }
        if (after != null) {
            this.appendString(after);
        }
        return this;
    }

    public default <Tgg extends Taggable> OutputBuilder appendTaggablesAndLast(List<Tgg> taggables) {
        return this.appendTaggablesAndLast(taggables, ",", null, null, null);
    }

    public default <Tgg extends Taggable> OutputBuilder appendTaggablesAndLast(List<Tgg> taggables, String separator,
            String before, String after, String empty) {
        if (before != null) {
            this.appendString(before);
        }
        if (taggables == null || taggables.isEmpty()) {
            if (empty != null) {
                this.appendString(empty);
            }
        } else if (taggables.size() == 1) {
            this.appendTaggable(taggables.get(0));
        } else {
            final int lastIndex = taggables.size() - 1;
            for (int i = 0; i < lastIndex; i++) {
                this.appendTaggable(taggables.get(i), " ", separator);
            }
            this.appendString("and", " ", null).appendTaggable(taggables.get(lastIndex), " ", null);
        }
        if (after != null) {
            this.appendString(after);
        }
        return this;
    }

    public default <Tgg extends Taggable> OutputBuilder appendTaggablesAndLast(SortedSet<Tgg> taggables) {
        return this.appendTaggablesAndLast(taggables, ",", null, null, null);
    }

    public default <Tgg extends Taggable> OutputBuilder appendTaggablesAndLast(SortedSet<Tgg> taggables,
            String separator, String before, String after, String empty) {
        if (before != null) {
            this.appendString(before);
        }
        if (taggables == null || taggables.isEmpty()) {
            if (empty != null) {
                this.appendString(empty);
            }
        } else if (taggables.size() == 1) {
            this.appendTaggable(taggables.first());
        } else {
            final Tgg last = taggables.last();
            final SortedSet<Tgg> remainder = taggables.headSet(last);
            for (final Taggable taggable : remainder) {
                this.appendTaggable(taggable, " ", separator);
            }
            this.appendString("and", " ", null).appendTaggable(last, " ", null);
        }
        if (after != null) {
            this.appendString(after);
        }
        return this;
    }

    public abstract OutputBuilder produceSubBuilder(String subName);

    public static class StringOutputBuilder implements OutputBuilder, CharSequence {
        private final String builderName;
        private final List<CharSequence> sequences;

        public StringOutputBuilder() {
            this.builderName = null;
            this.sequences = new ArrayList<>();
        }

        public StringOutputBuilder(String name) {
            this.builderName = name;
            this.sequences = new ArrayList<>();
            if (name != null) {
                StringBuilder builder = new StringBuilder();
                builder.append("\r\n").append(name).append(":\r\n");
                this.sequences.add(builder.toString());
            }
        }

        public String getBuilderName() {
            return builderName;
        }

        @Override
        public OutputBuilder appendString(String toAdd, String before, String after) {
            if (toAdd != null) {
                StringBuilder builder = new StringBuilder();
                if (before != null) {
                    builder.append(before);
                }
                builder.append(toAdd);
                if (after != null) {
                    builder.append(after);
                }
                this.sequences.add(builder.toString());
            }
            return this;
        }

        @Override
        public OutputBuilder appendExaminable(Examinable toAdd, String before, String after) {
            if (toAdd != null) {
                StringBuilder builder = new StringBuilder();
                if (before != null) {
                    builder.append(before);
                }
                builder.append(toAdd.getName());
                final String description = toAdd.getDescription();
                if (description != null && !description.isBlank()) {
                    OutputBuilder descriptBuilder = this.produceSubBuilder("Description");
                    descriptBuilder.appendString(description);
                }
                toAdd.produceExtraDescription(this);
                if (after != null) {
                    builder.append(after);
                }
                this.sequences.add(builder.toString());
            }
            return this;
        }

        @Override
        public OutputBuilder appendTaggable(Taggable toAdd, String before, String after) {
            if (toAdd != null) {
                StringBuilder builder = new StringBuilder();
                if (before != null) {
                    builder.append(before);
                }
                builder.append(toAdd.getSimpleContent());
                if (after != null) {
                    builder.append(after);
                }
                this.sequences.add(builder.toString());
            }
            return this;
        }

        @Override
        public StringOutputBuilder produceSubBuilder(String subName) {
            StringOutputBuilder sub = new StringOutputBuilder(subName);
            this.sequences.add(sub);
            return sub;
        }

        public String printString() {
            return this.toString();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            for (final CharSequence charSequence : sequences) {
                if (charSequence != null) {
                    sb.append(charSequence.toString());
                }
            }
            return sb.toString();
        }

        @Override
        public char charAt(int arg0) {
            return this.toString().charAt(arg0);
        }

        @Override
        public int length() {
            return this.toString().length();
        }

        @Override
        public CharSequence subSequence(int arg0, int arg1) {
            return this.toString().subSequence(arg0, arg1);
        }

    }
}
