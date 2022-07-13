package com.lhf.game.creature.conversation;

import java.util.Map;
import java.util.StringJoiner;
import java.util.TreeMap;

import com.lhf.server.interfaces.NotNull;

public class ConversationTreeNode {
    private ConversationTreeNodeID nodeID;
    private StringJoiner body;
    private Map<String, ConversationTreeNodeID> forwardMap;
    // TODO: tag keywords optionally

    public ConversationTreeNode(@NotNull ConversationTreeNodeID nodeID) {
        this.nodeID = nodeID;
        this.body = new StringJoiner(" ");
        this.body.setEmptyValue("I have nothing to say to you right now!");
        this.forwardMap = new TreeMap<>();
    }

    public void addBody(String bodyText) {
        this.body.add(bodyText);
    }

    public void addBodyWithForwardRef(String bodyText, String keyword, ConversationTreeNodeID nodeID) {
        this.addBody(bodyText);
        this.addForwardRef(keyword, nodeID);
    }

    public void addForwardRef(String keyword, ConversationTreeNodeID nodeID) {
        this.forwardMap.put(keyword.toLowerCase(), nodeID);
    }

    public ConversationTreeNodeID getNodeID() {
        return this.nodeID;
    }

    public String getBody() {
        return this.body.toString();
    }

    public Map<String, ConversationTreeNodeID> getForwardMap() {
        return this.forwardMap;
    }

    public ConversationTreeNodeID getNextNodeID(String message) {
        String lowerMessage = message.toLowerCase();
        for (String keyword : this.forwardMap.keySet()) {
            if (lowerMessage.matches(".*\\b" + keyword + "\\b.*")) {
                return this.forwardMap.get(keyword);
            }
        }
        return null;
    }
}
