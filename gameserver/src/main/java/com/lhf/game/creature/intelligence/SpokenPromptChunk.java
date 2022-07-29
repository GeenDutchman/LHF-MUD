package com.lhf.game.creature.intelligence;

import java.util.HashSet;
import java.util.Set;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.game.creature.conversation.ConversationTreeNodeResult;
import com.lhf.messages.Command;
import com.lhf.messages.CommandBuilder;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.OutMessageType;
import com.lhf.messages.in.SayMessage;
import com.lhf.messages.out.OutMessage;
import com.lhf.messages.out.SpeakingMessage;
import com.lhf.server.client.ClientID;

public class SpokenPromptChunk implements AIChunk {
    private Set<ClientID> prompters;

    public SpokenPromptChunk() {
        this.prompters = new HashSet<>();
    }

    public void addPrompter(ClientID id) {
        this.prompters.add(id);
    }

    private void basicHandle(BasicAI bai, SpeakingMessage sm, Creature sayer) {
        if (bai.getNpc().getConvoTree() != null) {
            ConversationTreeNodeResult result = bai.getNpc().getConvoTree().listen(sayer, sm.getMessage());
            if (result != null && result.getBody() != null) {
                SayMessage say = (SayMessage) CommandBuilder.fromCommand(CommandMessage.SAY,
                        "say \"" + result.getBody() + "\" to " + sayer.getName());
                CommandBuilder.addDirect(say, result.getBody());
                CommandBuilder.addIndirect(say, "to", sayer.getName());
                bai.handleMessage(null, say);
            }
            if (result != null && result.getPrompts() != null) {
                for (String prompt : result.getPrompts()) {
                    if (prompt.startsWith("PROMPT")) {
                        prompt = prompt.replaceFirst("PROMPT", "").trim();
                    }
                    Command cmd = CommandBuilder.parse(prompt);
                    bai.handleMessage(null, cmd);
                }
            }
        }
    }

    @Override
    public void handle(BasicAI bai, OutMessage msg) {
        if (msg.getOutType().equals(OutMessageType.SPEAKING)) {
            SpeakingMessage sm = (SpeakingMessage) msg;
            if (!sm.getShouting() && sm.getHearer() != null && sm.getHearer() instanceof NonPlayerCharacter) {
                if (sm.getSayer() instanceof Creature) {
                    Creature sayer = (Creature) sm.getSayer();
                    if (sm.getMessage().startsWith("PROMPT") &&
                            (this.prompters.contains(sayer.getClientID())
                                    || sayer.getClientID().equals(bai.getClientID()))) {
                        String prompt = sm.getMessage().replaceFirst("PROMPT", "").trim();
                        Command cmd = CommandBuilder.parse(prompt);
                        bai.handleMessage(null, cmd);
                    } else {
                        basicHandle(bai, sm, sayer);
                    }
                }
            }
        }

    }

}
