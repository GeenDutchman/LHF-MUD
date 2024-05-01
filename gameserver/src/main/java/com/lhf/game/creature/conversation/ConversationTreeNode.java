package com.lhf.game.creature.conversation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.lhf.RichOutput;
import com.lhf.RichOutput.RichOutputBuilder;
import com.lhf.RichOutput.RichOutputElement;

public class ConversationTreeNode implements Comparable<ConversationTreeNode>, Serializable {
    public final static String NPC_CONVERSATION_TAG = "NPCConversation";
    public static final String EMPTY = "...";
    private final UUID nodeID;
    private final RichOutput bodySequence;
    private List<RichOutput> prompts;

    private ConversationTreeNode(Builder builder) {
        if (builder == null) {
            this.nodeID = UUID.randomUUID();
            this.bodySequence = new RichOutputBuilder(NPC_CONVERSATION_TAG).appendString(EMPTY, null, null).build();
            this.prompts = new ArrayList<>();
        } else {
            this.nodeID = builder.getNodeID();
            this.bodySequence = builder.getBodySequence().build();
            this.prompts = builder.getPrompts().stream().filter(rob -> rob != null).map(rob -> rob.build()).toList();
        }
    }

    public static class Builder implements Serializable {
        private final UUID nodeID;
        private RichOutputBuilder bodySequence;
        private List<RichOutputBuilder> prompts;

        public Builder() {
            this.nodeID = UUID.randomUUID();
            this.bodySequence = new RichOutputBuilder(NPC_CONVERSATION_TAG);
            this.prompts = new ArrayList<>();
        }

        public Builder(ConversationTreeNode node) {
            this.bodySequence = new RichOutputBuilder(NPC_CONVERSATION_TAG);
            this.prompts = new ArrayList<>();
            if (node != null) {
                UUID retrieved = node.getNodeID();
                if (retrieved == null) {
                    this.nodeID = UUID.randomUUID();
                } else {
                    this.nodeID = node.getNodeID();
                }
                RichOutput body = node.getBodySequence();
                RichOutputBuilder sub = this.bodySequence;
                if (!NPC_CONVERSATION_TAG.equals(body.getBuilderName())) {
                    sub = this.bodySequence.produceSubBuilder(body.getBuilderName());
                }
                for (RichOutputElement element : body.getElements()) {
                    sub.appendRichOutputElement(element, null, null);
                }
            } else {
                this.nodeID = UUID.randomUUID();
            }
        }

        public static Builder ofString(String body) {
            Builder builder = new Builder();
            builder.bodySequence.appendString(body, null, null);
            return builder;
        }

        public static Builder ofRichOutputBuilder(RichOutputBuilder output) {
            Builder builder = new Builder();
            builder.setBodySequence(output);
            return builder;
        }

        public synchronized UUID getNodeID() {
            return nodeID;
        }

        public synchronized RichOutputBuilder getBodySequence() {
            if (bodySequence == null) {
                this.bodySequence = new RichOutputBuilder(NPC_CONVERSATION_TAG);
            }
            return bodySequence;
        }

        public Builder setBodySequence(RichOutputBuilder body) {
            if (body == null) {
                this.bodySequence = new RichOutputBuilder(NPC_CONVERSATION_TAG);
            } else if (NPC_CONVERSATION_TAG.equals(body.getBuilderName())) {
                this.bodySequence = body;
            } else {
                this.bodySequence = new RichOutputBuilder(NPC_CONVERSATION_TAG).appendRichOutputBuilder(body, null,
                        null);
            }
            return this;
        }

        public synchronized List<RichOutputBuilder> getPrompts() {
            if (prompts == null) {
                this.prompts = new ArrayList<>();
            }
            return prompts;
        }

        public Builder setPrompts(List<RichOutputBuilder> prompts) {
            this.prompts = prompts != null ? prompts : new ArrayList<>();
            return this;
        }

        public Builder addPrompt(RichOutputBuilder nextPrompt) {
            if (this.prompts == null) {
                this.prompts = new ArrayList<>();
            }
            if (nextPrompt != null) {
                this.prompts.add(nextPrompt);
            }
            return this;
        }

        public Builder addPrompt(String nextPrompt) {
            if (nextPrompt == null) {
                return this;
            }
            return this.addPrompt(new RichOutputBuilder().appendString(nextPrompt, null, null));
        }

        @Override
        public int hashCode() {
            return Objects.hash(nodeID);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (!(obj instanceof Builder))
                return false;
            Builder other = (Builder) obj;
            return Objects.equals(nodeID, other.nodeID);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("Builder [nodeID=").append(nodeID).append(", bodyBuilder=").append(bodySequence)
                    .append(", prompts=").append(prompts).append("]");
            return builder.toString();
        }

        public ConversationTreeNode build() {
            return new ConversationTreeNode(this);
        }

    }

    public RichOutput getBodySequence() {
        if (bodySequence == null || bodySequence.getElements().size() == 0) {
            return new RichOutputBuilder(NPC_CONVERSATION_TAG).appendString(EMPTY, null, null).build();
        }
        return bodySequence;
    }

    public String getBodyAsString() {
        return this.getBodySequence().printString();
    }

    public boolean addPrompt(String promptBody) {
        return this.addPrompt(new RichOutputBuilder().appendString(promptBody, null, null).build());
    }

    public boolean addPrompt(RichOutput prompt) {
        return this.prompts.add(prompt);
    }

    public UUID getNodeID() {
        return this.nodeID;
    }

    public List<RichOutput> getPrompts() {
        return this.prompts;
    }

    @Override
    public int compareTo(ConversationTreeNode o) {
        if (o == null) {
            throw new NullPointerException("Cannot compare to a null Conversation Node");
        }
        return this.nodeID.compareTo(o.getNodeID());
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodeID);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ConversationTreeNode)) {
            return false;
        }
        ConversationTreeNode other = (ConversationTreeNode) obj;
        return Objects.equals(nodeID, other.nodeID);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConversationTreeNode [nodeID=").append(nodeID).append(", bodySequence=").append(bodySequence)
                .append(", prompts=").append(prompts).append("]");
        return builder.toString();
    }

}
