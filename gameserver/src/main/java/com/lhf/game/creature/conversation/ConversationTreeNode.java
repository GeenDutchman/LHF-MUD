package com.lhf.game.creature.conversation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.lhf.RichOutput.RichOutputSequence;
import com.lhf.RichOutput.RichOutputSequenceElement;
import com.lhf.game.creature.conversation.ConversationTransformer.ConversationContextKey;

public class ConversationTreeNode implements Comparable<ConversationTreeNode>, Serializable {
    public final static String NPC_CONVERSATION_TAG = "NPCConversation";
    public static final String EMPTY = "...";
    private final UUID nodeID;
    private final RichOutputSequence bodySequence;
    private List<RichOutputSequence> prompts;

    public ConversationTreeNode(String someBody) {
        this.nodeID = UUID.randomUUID();
        this.bodySequence = new RichOutputSequence(NPC_CONVERSATION_TAG);
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
            this.bodySequence.appendOutputBuilderElement(RichOutputSequenceElement.ofMetaSignal(meta));
        }
        return this;
    }

    public RichOutputSequence getBodySequence() {
        if (bodySequence == null || bodySequence.getElements().size() == 0) {
            return new RichOutputSequence().appendString(EMPTY, null, null);
        }
        return RichOutputSequence.copy(bodySequence);
    }

    public String getBodyAsString() {
        return this.getBodySequence().printString();
    }

    public boolean addPrompt(String promptBody) {
        return this.addPrompt(new RichOutputSequence().appendString(promptBody, null, null));
    }

    public boolean addPrompt(RichOutputSequence prompt) {
        return this.prompts.add(prompt);
    }

    public UUID getNodeID() {
        return this.nodeID;
    }

    public List<RichOutputSequence> getPrompts() {
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
