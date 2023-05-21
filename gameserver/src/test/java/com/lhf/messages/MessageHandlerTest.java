package com.lhf.messages;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.truth.Truth;

@ExtendWith(MockitoExtension.class)
public class MessageHandlerTest {
    @Mock
    private MessageHandler leafNodeOne;
    @Mock
    private MessageHandler leafNodeTwo;
    @Mock
    private MessageHandler branchNode;
    @Mock
    private MessageHandler rootNode;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    public void buildTree() {
        when(this.rootNode.getSuccessor()).thenReturn(null);
        when(this.branchNode.getSuccessor()).thenReturn(this.rootNode);
        when(this.leafNodeOne.getSuccessor()).thenReturn(this.branchNode);
        when(this.leafNodeTwo.getSuccessor()).thenReturn(this.branchNode);

        when(this.rootNode.addSelfToContext(any())).thenReturn(new CommandContext());
        when(this.branchNode.addSelfToContext(any())).thenReturn(new CommandContext());
        when(this.leafNodeOne.addSelfToContext(any())).thenReturn(new CommandContext());
        when(this.leafNodeTwo.addSelfToContext(any())).thenReturn(new CommandContext());

        Map<CommandMessage, String> leafNodeOneHelps = new HashMap<>();
        leafNodeOneHelps.put(CommandMessage.HELP, "When you need help");
        leafNodeOneHelps.put(CommandMessage.INVENTORY, "When you need to know what you have");
        when(this.leafNodeOne.getCommands(any())).thenReturn(leafNodeOneHelps);

        Map<CommandMessage, String> leafNodeTwoHelps = new HashMap<>();
        leafNodeTwoHelps.put(CommandMessage.CAST, "If you are a caster");
        when(this.leafNodeTwo.getCommands(any())).thenReturn(leafNodeTwoHelps);

        Map<CommandMessage, String> branchNodeHelps = new HashMap<>();
        branchNodeHelps.put(CommandMessage.HELP, "When you get higher help");
        branchNodeHelps.put(CommandMessage.SEE, "I added something!");
        when(this.branchNode.getCommands(any())).thenReturn(branchNodeHelps);

        // root node does nothing
        when(this.rootNode.getCommands(any())).thenReturn(null);

    }

    @Test
    void testGatherHelp() {
        buildTree();
        Map<CommandMessage, String> receivedNodeOne = this.leafNodeOne.getCommands(null);
        System.out.println(receivedNodeOne);
        Truth.assertThat(receivedNodeOne).containsKey(CommandMessage.INVENTORY);
        Truth.assertThat(receivedNodeOne).containsKey(CommandMessage.HELP);
        Truth.assertThat(receivedNodeOne).doesNotContainKey(CommandMessage.CAST);
        Truth.assertThat(receivedNodeOne).containsKey(CommandMessage.SEE);
        Truth.assertThat(receivedNodeOne).containsKey(CommandMessage.HELP);
        Truth.assertThat(receivedNodeOne.get(CommandMessage.HELP)).doesNotContain("higher");

        Map<CommandMessage, String> rec = this.leafNodeTwo.getCommands(null);
        Truth.assertThat(rec).doesNotContainKey(CommandMessage.INVENTORY);
        Truth.assertThat(rec).containsKey(CommandMessage.HELP);
        Truth.assertThat(rec).containsKey(CommandMessage.CAST);
        Truth.assertThat(rec).containsKey(CommandMessage.SEE);
        Truth.assertThat(rec).containsKey(CommandMessage.HELP);
        Truth.assertThat(rec.get(CommandMessage.HELP)).contains("higher");
    }

    // @Test
    // void testHandleMessage() {

    // }
}
