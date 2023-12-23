package com.lhf.game.creature.intelligence.handlers;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import com.lhf.Taggable;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.INonPlayerCharacter;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.conversation.ConversationTreeNodeResult;
import com.lhf.game.creature.intelligence.AIHandler;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.messages.ClientID;
import com.lhf.messages.Command;
import com.lhf.messages.CommandBuilder;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.GameEventType;
import com.lhf.messages.events.GameEvent;
import com.lhf.messages.events.SpeakingEvent;
import com.lhf.messages.in.SayMessage;
import com.lhf.server.client.user.User;

public class SpokenPromptChunk extends AIHandler {
    private Set<ClientID> prompters;
    private boolean allowUsers;

    public SpokenPromptChunk() {
        super(GameEventType.SPEAKING);
        this.prompters = new HashSet<>();
        this.allowUsers = false;
    }

    public SpokenPromptChunk setAllowUsers() {
        this.allowUsers = true;
        return this;
    }

    public SpokenPromptChunk addPrompter(ClientID id) {
        this.prompters.add(id);
        return this;
    }

    private void basicHandle(BasicAI bai, SpeakingEvent sm) {
        ConversationTree tree = bai.getNpc().getConvoTree();
        if (tree != null) {
            ConversationTreeNodeResult result = tree.listen(sm.getSayer(), sm.getMessage());
            if (result != null && result.getBody() != null) {
                String name = Taggable.extract(sm.getSayer());
                SayMessage say = (SayMessage) CommandBuilder.fromCommand(CommandMessage.SAY,
                        "say \"" + result.getBody() + "\" to " + name);
                CommandBuilder.addDirect(say, result.getBody());
                CommandBuilder.addIndirect(say, "to", name);
                bai.handleChain(null, say);
            }
            if (result != null && result.getPrompts() != null) {
                for (String prompt : result.getPrompts()) {
                    if (prompt.startsWith("STORE")) {
                        this.logger.log(Level.FINE,
                                String.format("Result has storage prompt \"%s\" for %s", prompt, bai.toString()));
                        prompt = prompt.replaceFirst("STORE", "").trim();
                        String[] splits = prompt.split("\\b+", 2);
                        if (splits.length < 2) {
                            continue;
                        }
                        tree.store(sm.getSayer(), splits[0], splits[1]);
                        continue;
                    }
                    if (prompt.startsWith("PROMPT")) {
                        prompt = prompt.replaceFirst("PROMPT", "").trim();
                    }
                    this.logger.log(Level.FINE,
                            String.format("Result has prompt \"%s\" for %s", prompt, bai.toString()));
                    Command cmd = CommandBuilder.parse(prompt);
                    CommandContext.Reply handled = bai.handleChain(null, cmd);
                    this.logger.log(Level.FINER,
                            () -> String.format("%s: prompted command \"%s\" handled: %s", bai.toString(),
                                    cmd.toString(), handled));
                }
            }
        } else {
            this.logger.log(Level.WARNING, () -> String.format("no convo tree found for %s", bai.toString()));
        }
    }

    @Override
    public void handle(BasicAI bai, GameEvent event) {
        if (event.getEventType().equals(GameEventType.SPEAKING)) {
            SpeakingEvent sm = (SpeakingEvent) event;
            if (!sm.getShouting() && sm.getHearer() != null && sm.getHearer() instanceof INonPlayerCharacter) {
                if (sm.getSayer() instanceof ICreature || (this.allowUsers && sm.getSayer() instanceof User)) {
                    if (sm.getMessage().startsWith("PROMPT") &&
                            (this.prompters.contains(sm.getSayer().getClientID())
                                    || sm.getSayer().getClientID().equals(bai.getClientID()))) {
                        String prompt = sm.getMessage().replaceFirst("PROMPT", "").trim();
                        this.logger.log(Level.INFO, String.format("Prompt \"%s\" received from %s for %s", prompt,
                                sm.getSayer().getColorTaggedName(),
                                bai.getNpc() != null ? bai.getNpc().getName() : bai.getColorTaggedName()));
                        Command cmd = CommandBuilder.parse(prompt);
                        bai.handleChain(null, cmd);
                    } else {
                        basicHandle(bai, sm);
                    }
                }
            }
        }

    }

}
