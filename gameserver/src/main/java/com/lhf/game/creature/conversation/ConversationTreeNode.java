package com.lhf.game.creature.conversation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ConversationTreeNode implements Comparable<ConversationTreeNode>, Serializable {
    public static final String EMPTY = "...";
    private final UUID nodeID;
    private String body;
    private List<String> prompts;

    public ConversationTreeNode(String someBody) {
        this.nodeID = UUID.randomUUID();
        this.addBody(someBody);
        this.prompts = new ArrayList<>();
    }

    public void addBody(String bodyText) {
        if (this.body != null) {
            this.body += ' ' + new String(bodyText);
            return;
        }
        this.body = new String(bodyText);
    }

    public boolean addPrompt(String prompt) {
        return this.prompts.add(prompt);
    }

    public UUID getNodeID() {
        return this.nodeID;
    }

    public String getBody() {
        if (this.body == null) {
            return this.getEmptyStatement();
        }
        return this.body;
    }

    public List<String> getPrompts() {
        return this.prompts;
    }

    public ConversationTreeNodeResult getResult() {
        ConversationTreeNodeResult result = new ConversationTreeNodeResult(this.getBody());
        for (String prompt : this.getPrompts()) {
            result.addPrompt(prompt);
        }
        return result;
    }

    public String getEmptyStatement() {
        return ConversationTreeNode.EMPTY;
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
        builder.append("ConversationTreeNode [body=").append(body).append(", nodeID=").append(nodeID)
                .append(", prompts=").append(prompts).append("]");
        return builder.toString();
    }

}
