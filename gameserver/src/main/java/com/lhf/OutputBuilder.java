package com.lhf;

import java.util.Collection;
import java.util.List;
import java.util.SortedSet;

public interface OutputBuilder {
    public default OutputBuilder appendString(String toAdd) {
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

    public OutputBuilder appendTaggable(Taggable toAdd, String before, String after);

    public default OutputBuilder appendTaggables(Collection<Taggable> taggables) {
        return this.appendTaggables(taggables, ", ", null, null, null);
    }

    public default OutputBuilder appendTaggables(Collection<Taggable> taggables, String separator, String before,
            String after, String empty) {
        if (before != null) {
            this.appendString(before);
        }
        if (taggables == null || taggables.isEmpty()) {
            if (empty != null) {
                this.appendString(empty);
            }
        } else {
            for (Taggable taggable : taggables) {
                this.appendTaggable(taggable).appendString(separator);
            }
        }
        if (after != null) {
            this.appendString(after);
        }
        return this;
    }

    public default OutputBuilder appendTaggablesAndLast(List<Taggable> taggables) {
        return this.appendTaggablesAndLast(taggables, ",", null, null, null);
    }

    public default OutputBuilder appendTaggablesAndLast(List<Taggable> taggables, String separator, String before,
            String after, String empty) {
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

    public default OutputBuilder appendTaggablesAndLast(SortedSet<Taggable> taggables) {
        return this.appendTaggablesAndLast(taggables, ",", null, null, null);
    }

    public default OutputBuilder appendTaggablesAndLast(SortedSet<Taggable> taggables, String separator, String before,
            String after, String empty) {
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
            final Taggable last = taggables.last();
            final SortedSet<Taggable> remainder = taggables.headSet(last);
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

    public static class StringOutputBuilder implements OutputBuilder {
        private StringBuilder builder = new StringBuilder();

        @Override
        public OutputBuilder appendString(String toAdd, String before, String after) {
            if (toAdd != null) {
                if (before != null) {
                    builder.append(before);
                }
                builder.append(toAdd);
                if (after != null) {
                    builder.append(after);
                }
            }
            return this;
        }

        @Override
        public OutputBuilder appendExaminable(Examinable toAdd, String before, String after) {
            if (toAdd != null) {
                if (before != null) {
                    builder.append(before);
                }
                builder.append(toAdd.getName());
                final String description = toAdd.getDescription();
                if (description != null && !description.isBlank()) {
                    builder.append("\r\ndescription:").append(description);
                }
                toAdd.produceExtraDescription(this);
                if (after != null) {
                    builder.append(after);
                }
            }
            return this;
        }

        @Override
        public OutputBuilder appendTaggable(Taggable toAdd, String before, String after) {
            if (toAdd != null) {
                if (before != null) {
                    builder.append(before);
                }
                builder.append(toAdd.getSimpleContent());
                if (after != null) {
                    builder.append(after);
                }
            }
            return this;
        }

        public String build() {
            return builder.toString();
        }

    }
}
