package com.lhf.game.creature.conversation;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

public class ConversationTreeNode implements Comparable<ConversationTreeNode> {
    private final UUID nodeID;
    private StringJoiner body;
    private String emptyStatement;

    public ConversationTreeNode() {
        this.nodeID = UUID.randomUUID();
        this.body = new StringJoiner(" ");
        this.emptyStatement = "I have nothing to say to you right now!";
        this.body.setEmptyValue(this.emptyStatement);
    }

    public ConversationTreeNode(String emptyStatement) {
        this.nodeID = UUID.randomUUID();
        this.body = new StringJoiner(" ");
        this.setEmptyStatement(emptyStatement);
    }

    public void addBody(String bodyText) {
        this.body.add(bodyText);
    }

    public UUID getNodeID() {
        return this.nodeID;
    }

    public String getBody() {
        return this.body.toString();
    }

    public String getEmptyStatement() {
        return emptyStatement;
    }

    public void setEmptyStatement(String emptyStatement) {
        this.emptyStatement = emptyStatement;
        if (emptyStatement == null) {
            this.emptyStatement = "";
        }
        this.body.setEmptyValue(emptyStatement);
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
        builder.append("ConversationTreeNode [body=").append(body).append(", nodeID=").append(nodeID).append("]");
        return builder.toString();
    }

}