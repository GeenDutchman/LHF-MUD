package com.lhf.game.creature.conversation;

import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.UUID;

public class ConversationTreeNode {
    private final UUID nodeID;
    private StringJoiner body;
    private Map<String, UUID> forwardMap;
    // TODO: tag keywords optionally

    public ConversationTreeNode() {
        this.nodeID = UUID.randomUUID();
        this.body = new StringJoiner(" ");
        this.body.setEmptyValue("I have nothing to say to you right now!");
        this.forwardMap = new TreeMap<>();
    }

    public ConversationTreeNode(String emptyStatement) {
        this.nodeID = UUID.randomUUID();
        this.body = new StringJoiner(" ");
        if (emptyStatement == null) {
            this.body.setEmptyValue("");
        } else {
            this.body.setEmptyValue(emptyStatement);
        }
        this.forwardMap = new TreeMap<>();
    }

    public void addBody(String bodyText) {
        this.body.add(bodyText);
    }

    public void addBodyWithForwardRef(String bodyText, String keyword, UUID nodeID) {
        this.addBody(bodyText);
        this.addForwardRef(keyword, nodeID);
    }

    public void addForwardRef(String keyword, UUID nodeID) {
        this.forwardMap.put(keyword.toLowerCase(), nodeID);
    }

    public UUID getNodeID() {
        return this.nodeID;
    }

    public String getBody() {
        return this.body.toString();
    }

    public Map<String, UUID> getForwardMap() {
        return this.forwardMap;
    }

    public UUID getNextNodeID(String message) {
        String lowerMessage = message.toLowerCase();
        for (String keyword : this.forwardMap.keySet()) {
            if (lowerMessage.matches(".*\\b" + keyword + "\\b.*")) {
                return this.forwardMap.get(keyword);
            }
        }
        return null;
    }
}
