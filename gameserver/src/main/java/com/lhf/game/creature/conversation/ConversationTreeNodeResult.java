package com.lhf.game.creature.conversation;

import java.util.ArrayList;
import java.util.List;

public class ConversationTreeNodeResult {
    private String body;
    private List<String> prompts;

    public ConversationTreeNodeResult(String body) {
        this.setBody(body);
        this.prompts = new ArrayList<>();
    }

    public void setBody(String body) {
        this.body = new String(body);
    }

    public boolean addPrompt(String prompt) {
        return this.prompts.add(prompt);
    }

    public String getBody() {
        return body;
    }

    public List<String> getPrompts() {
        return prompts;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConversationTreeNodeResult [body=").append(body).append(", prompts=").append(prompts)
                .append("]");
        return builder.toString();
    }
}
