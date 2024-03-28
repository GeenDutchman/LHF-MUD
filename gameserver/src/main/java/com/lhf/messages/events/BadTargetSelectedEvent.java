package com.lhf.messages.events;

import java.util.Collection;
import java.util.Collections;
import java.util.StringJoiner;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.lhf.Taggable;
import com.lhf.messages.GameEventType;

public class BadTargetSelectedEvent extends GameEvent {
    public enum BadTargetOption {
        SELF, NOTARGET, DNE, UNCLEAR, TOO_MANY, UNTARGETABLE;
    }

    private final BadTargetOption bde;
    private final String badTarget;
    private final Collection<? extends Taggable> possibleTargets;

    public static class Builder extends GameEvent.Builder<Builder> {
        private BadTargetOption bde;
        private String badTarget;
        private Collection<? extends Taggable> possibleTargets = Collections.emptyList();

        protected Builder() {
            super(GameEventType.BAD_TARGET_SELECTED);
        }

        public BadTargetOption getBde() {
            return bde;
        }

        public Builder setBde(BadTargetOption bde) {
            this.bde = bde;
            return this;
        }

        public String getBadTarget() {
            return badTarget;
        }

        public Builder setBadTarget(String badTarget) {
            this.badTarget = badTarget;
            return this;
        }

        public Collection<? extends Taggable> getPossibleTargets() {
            return Collections.unmodifiableCollection(possibleTargets);
        }

        public Builder setPossibleTargets(Collection<? extends Taggable> possibleTargets) {
            this.possibleTargets = possibleTargets != null ? possibleTargets : Collections.emptyList();
            return this;
        }

        @Override
        public BadTargetSelectedEvent Build() {
            return new BadTargetSelectedEvent(this);
        }

        @Override
        public Builder getThis() {
            return this;
        }

    }

    public static Builder getBuilder() {
        return new Builder();
    }

    public BadTargetSelectedEvent(Builder builder) {
        super(builder);
        this.bde = builder.getBde();
        this.badTarget = builder.getBadTarget();
        this.possibleTargets = builder.getPossibleTargets();
    }

    @Override
    public String toString() {
        return this.printString();
    }

    public BadTargetOption getBde() {
        return bde;
    }

    public String getBadTarget() {
        return badTarget;
    }

    public Collection<? extends Taggable> getPossibleTargets() {
        return possibleTargets;
    }

    @Override
    public String printString() {
        StringJoiner sj = new StringJoiner(" ");
        sj.setEmptyValue("");
        if (this.bde == null) {
            sj.add("You have selected a bad or invalid target.");
        } else {
            switch (this.bde) {
            case SELF:
                sj.add("You cannot target yourself!");
                break;
            case NOTARGET:
                sj.add("You did not choose any targets.");
                break;
            case DNE:
                if (this.badTarget != null && this.badTarget.length() > 0) {
                    sj.add(this.badTarget).add("does not exist as a target or is not targetable.");
                } else {
                    sj.add("One of your targets did not exist.");
                }
                break;
            case UNCLEAR:
                if (this.badTarget != null && this.badTarget.length() > 0) {
                    sj.add("You cannot target '").add(this.badTarget).add("' because it is unclear.");
                } else {
                    sj.add("It is unclear what you are targeting.");
                }
                break;
            case TOO_MANY:
                if (this.possibleTargets != null && this.possibleTargets.size() > 0) {
                    sj.add(Integer.toString(this.possibleTargets.size())).add("is too many targets.");
                } else {
                    sj.add("You cannot select that many targets");
                }
                break;
            case UNTARGETABLE:
                if (this.badTarget != null && !this.badTarget.isBlank()) {
                    sj.add(this.badTarget).add("is not targetable.");
                } else {
                    sj.add("You cannot target this.");
                }
                break;
            default:
                sj.add("You have selected a bad or invalid target.");
            }
        }

        if (this.possibleTargets != null && this.possibleTargets.size() > 0) {
            sj.add("Possible targets include:\n");
            for (Taggable tagger : this.possibleTargets) {
                sj.add(tagger.getSimpleContent());
            }
        }

        return sj.toString();
    }

    @Override
    public Element buildXMLElement(Document nodeGenerator) {
        Element myElement = this.produceContentNode(nodeGenerator);
        if (myElement == null) {
            return myElement;
        }

        if (this.bde == null) {
            myElement.appendChild(nodeGenerator.createTextNode("You have selected a bad or invalid target."));
        } else {
            myElement.setAttribute("BadTargetOption", this.bde.toString());
            switch (this.bde) {
            case SELF:
                myElement.appendChild(nodeGenerator.createTextNode("You cannot target yourself!"));
                break;
            case NOTARGET:
                myElement.appendChild(nodeGenerator.createTextNode("You did not choose any targets."));
                break;
            case DNE:
                if (this.badTarget != null && this.badTarget.length() > 0) {
                    myElement.appendChild(nodeGenerator.createTextNode(this.badTarget));
                    myElement.appendChild(
                            nodeGenerator.createTextNode("does not exist as a target or is not targetable."));
                } else {
                    myElement.appendChild(nodeGenerator.createTextNode("One of your targets did not exist."));
                }
                break;
            case UNCLEAR:
                if (this.badTarget != null && this.badTarget.length() > 0) {
                    myElement.appendChild(nodeGenerator.createTextNode("You cannot target '"));
                    myElement.appendChild(nodeGenerator.createTextNode(this.badTarget));
                    myElement.appendChild(nodeGenerator.createTextNode("' because it is unclear."));
                } else {
                    myElement.appendChild(nodeGenerator.createTextNode("It is unclear what you are targeting."));
                }
                break;
            case TOO_MANY:
                if (this.possibleTargets != null && this.possibleTargets.size() > 0) {
                    myElement.appendChild(nodeGenerator.createTextNode(Integer.toString(this.possibleTargets.size())));
                    myElement.appendChild(nodeGenerator.createTextNode("is too many targets."));
                } else {
                    myElement.appendChild(nodeGenerator.createTextNode("You cannot select that many targets"));
                }
                break;
            case UNTARGETABLE:
                if (this.badTarget != null && !this.badTarget.isBlank()) {
                    myElement.appendChild(nodeGenerator.createTextNode(this.badTarget));
                    myElement.appendChild(nodeGenerator.createTextNode("is not targetable."));
                } else {
                    myElement.appendChild(nodeGenerator.createTextNode("You cannot target this."));
                }
                break;
            default:
                myElement.appendChild(nodeGenerator.createTextNode("You have selected a bad or invalid target."));
            }
        }

        if (this.possibleTargets != null && this.possibleTargets.size() > 0) {
            Element available = nodeGenerator.createElement("AvailableTargets");
            available.appendChild(nodeGenerator.createTextNode("Possible targets include:\n"));
            for (Taggable tagger : this.possibleTargets) {
                available.appendChild(tagger.buildXMLElement(nodeGenerator));
            }
            myElement.appendChild(available);
        }

        return myElement;
    }
}
