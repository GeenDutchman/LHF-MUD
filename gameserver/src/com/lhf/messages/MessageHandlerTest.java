package com.lhf.messages;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        this.leafNodeOne.setSuccessor(branchNode);
        this.leafNodeTwo.setSuccessor(branchNode);
        this.branchNode.setSuccessor(rootNode);

        Map<CommandMessage, String> leafNodeOneHelps = new HashMap<>();
        leafNodeOneHelps.put(CommandMessage.HELP, "When you need help");
        leafNodeOneHelps.put(CommandMessage.INVENTORY, "When you need to know what you have");
        when(this.leafNodeOne.getCommands()).thenReturn(leafNodeOneHelps);

        Map<CommandMessage, String> leafNodeTwoHelps = new HashMap<>();
        leafNodeTwoHelps.put(CommandMessage.CAST, "If you are a caster");
        when(this.leafNodeTwo.getCommands()).thenReturn(leafNodeTwoHelps);

        Map<CommandMessage, String> branchNodeHelps = new HashMap<>();
        branchNodeHelps.put(CommandMessage.HELP, "When you get higher help");
        when(this.branchNode.getCommands()).thenReturn(branchNodeHelps);

        // root node does nothing
        when(this.rootNode.getCommands()).thenReturn(null);
    }

    @Test
    void testGatherHelp() {
        Map<CommandMessage, String> receivedNodeOne = this.leafNodeOne.gatherHelp();
        assertTrue(receivedNodeOne.containsKey(CommandMessage.INVENTORY));
        assertTrue(receivedNodeOne.containsKey(CommandMessage.HELP));
        assertFalse(receivedNodeOne.containsKey(CommandMessage.CAST));
        assertFalse(receivedNodeOne.get(CommandMessage.HELP).contains("higher"));

        Map<CommandMessage, String> rec = this.leafNodeTwo.gatherHelp();
        assertFalse(rec.containsKey(CommandMessage.INVENTORY));
        assertTrue(rec.containsKey(CommandMessage.HELP));
        assertTrue(rec.containsKey(CommandMessage.CAST));
        assertTrue(rec.get(CommandMessage.HELP).contains("higher"));

    }

    @Test
    void testHandleMessage() {

    }
}
