package com.lhf.game.creature.conversation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.lhf.OutputBuilder.OutputSequence;
import com.lhf.OutputBuilder.OutputSequenceElement;
import com.lhf.game.creature.conversation.ConversationTransformer.ConversationContextKey;

public class ConversationTreeNode implements Comparable<ConversationTreeNode>, Serializable {
    public static final String EMPTY = "...";
    private final UUID nodeID;
    private final OutputSequence bodySequence;
    private List<OutputSequence> prompts;

    public ConversationTreeNode(String someBody) {
        this.nodeID = UUID.randomUUID();
        this.bodySequence = new OutputSequence();
        this.bodySequence.appendString(someBody, null, null);
        this.prompts = new ArrayList<>();
    }

    public ConversationTreeNode addBody(String moreBody) {
        this.bodySequence.appendString(moreBody);
        return this;
    }

    public ConversationTreeNode addMetaSignal(ConversationContextKey meta) {
        if (meta != null) {
            return this.addMetaSignal(meta.name());
        }
        return this;
    }

    public ConversationTreeNode addMetaSignal(String meta) {
        if (meta != null) {
            this.bodySequence.appendOutputBuilderElement(OutputSequenceElement.ofMetaSignal(meta));
        }
        return this;
    }

    public OutputSequence getBodySequence() {
        if (bodySequence == null || bodySequence.getElements().size() == 0) {
            return new OutputSequence().appendString(EMPTY, null, null);
        }
        return OutputSequence.copy(bodySequence);
    }

    public String getBodyAsString() {
        return this.getBodySequence().printString();
    }

    public boolean addPrompt(String promptBody) {
        return this.addPrompt(new OutputSequence().appendString(promptBody, null, null));
    }

    public boolean addPrompt(OutputSequence prompt) {
        return this.prompts.add(prompt);
    }

    public UUID getNodeID() {
        return this.nodeID;
    }

    public List<OutputSequence> getPrompts() {
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
