package com.lhf.game.creature.conversation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.lhf.RichOutput;
import com.lhf.RichOutput.RichOutputBuilder;

public class ConversationTreeNode implements Comparable<ConversationTreeNode>, Serializable {
    public final static String NPC_CONVERSATION_TAG = "NPCConversation";
    public static final String EMPTY = "...";
    private final UUID nodeID;
    private final RichOutput bodySequence;
    private List<RichOutput> prompts;

    public ConversationTreeNode(String someBody) {
        this.nodeID = UUID.randomUUID();
        this.bodySequence = new RichOutputBuilder(NPC_CONVERSATION_TAG).appendString(someBody, null, null).build();
        this.prompts = new ArrayList<>();
    }

    public ConversationTreeNode(RichOutput output) {
        this.nodeID = UUID.randomUUID();
        if (output == null) {
            this.bodySequence = new RichOutputBuilder(NPC_CONVERSATION_TAG).appendString(EMPTY, null, null).build();
        } else if (NPC_CONVERSATION_TAG.equals(output.getBuilderName())) {
            this.bodySequence = output;
        } else {
            this.bodySequence = new RichOutputBuilder(NPC_CONVERSATION_TAG).appendOutputBuilder(output, null, null)
                    .build();
        }
        this.prompts = new ArrayList<>();
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
