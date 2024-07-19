package com.lhf.game;

import java.io.Serializable;
import java.util.function.BiFunction;

public interface IExternalReference<T> extends Serializable, Comparable<IExternalReference<T>> {
    public T getReference();

    public IExternalReference<T> setReference(T target);

    public String getLocality();

    public String getReferenceName();

    public default void populateExternalReference(BiFunction<String, String, T> populator) {
        if (populator != null && this.getReferenceName() != null) {
            final T potentialTarget = populator.apply(getLocality(), getReferenceName());
            if (potentialTarget != null) {
                this.setReference(potentialTarget);
            }
        }
    }

    @Override
    default int compareTo(IExternalReference<T> arg0) {
        final String myLocality = this.getLocality();
        final String theirLocality = arg0.getLocality();
        if (myLocality != null && theirLocality == null) {
            return -1; // external references come first
        } else if (myLocality == null && theirLocality != null) {
            return 1;
        } else if (myLocality != null && theirLocality != null) {
            int comparison = myLocality.compareTo(theirLocality);
            if (comparison != 0) {
                return comparison;
            }
        }

        final String myName = this.getReferenceName();
        final String theirName = arg0.getReferenceName();
        if (myName != null && theirName == null) {
            return -1;
        } else if (myName == null && theirName != null) {
            return 1;
        } else if (myName != null && theirName != null) {
            return myName.compareTo(theirName);
        }
        return 0;
    }

    public static abstract class ExternalReference<R> implements IExternalReference<R> {
        private final String locality;
        private final String referenceName;
        private transient R reference;

        public ExternalReference(String locality, String referenceName) {
            if (referenceName != null && locality == null) {
                throw new IllegalArgumentException(String
                        .format("If a reference name is provided ('%s') then the locality must also be provided!"));
            }
            this.locality = locality;
            this.referenceName = referenceName;
        }

        public ExternalReference(IExternalReference<R> other) {
            this.locality = other.getLocality();
            this.referenceName = other.getReferenceName();
            this.reference = other.getReference();
        }

        @Override
        public String getLocality() {
            return locality;
        }

        @Override
        public String getReferenceName() {
            return referenceName;
        }

        @Override
        public R getReference() {
            return reference;
        }

        @Override
        public ExternalReference<R> setReference(R reference) {
            this.reference = reference;
            return this;
        }

    }
}
