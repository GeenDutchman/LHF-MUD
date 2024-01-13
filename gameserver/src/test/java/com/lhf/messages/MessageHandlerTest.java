package com.lhf.messages;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.truth.Truth;
import com.lhf.messages.in.AMessageType;

@ExtendWith(MockitoExtension.class)
public class MessageHandlerTest {
    @Mock
    private CommandChainHandler leafNodeOne;
    @Mock
    private CommandChainHandler leafNodeTwo;
    @Mock
    private CommandChainHandler branchNode;
    @Mock
    private CommandChainHandler rootNode;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
    }

    public void buildTree() {
        when(this.rootNode.getSuccessor()).thenReturn(null);
        when(this.branchNode.getSuccessor()).thenReturn(this.rootNode);
        when(this.leafNodeOne.getSuccessor()).thenReturn(this.branchNode);
        when(this.leafNodeTwo.getSuccessor()).thenReturn(this.branchNode);

        /**
         * //
         * when(this.rootNode.addSelfToContext(Mockito.isA(CommandContext.class))).thenAnswer(i
         * -> {
         * // CommandContext a = i.getArgument(0);
         * // if (a == null) {
         * // return new CommandContext();
         * // }
         * // return a;
         * // });
         * //
         * when(this.branchNode.addSelfToContext(Mockito.isA(CommandContext.class))).thenAnswer(i
         * -> {
         * // CommandContext a = i.getArgument(0);
         * // if (a == null) {
         * // return new CommandContext();
         * // }
         * // return a;
         * // });
         * //
         * when(this.leafNodeOne.addSelfToContext(Mockito.isA(CommandContext.class))).thenAnswer(i
         * -> {
         * // CommandContext a = i.getArgument(0);
         * // if (a == null) {
         * // return new CommandContext();
         * // }
         * // return a;
         * // });
         * //
         * when(this.leafNodeTwo.addSelfToContext(Mockito.isA(CommandContext.class))).thenAnswer(i
         * -> {
         * // CommandContext a = i.getArgument(0);
         * // if (a == null) {
         * // return new CommandContext();
         * // }
         * // return a;
         * // });
         */

        Map<AMessageType, String> leafNodeOneHelps = new HashMap<>();
        leafNodeOneHelps.put(AMessageType.HELP, "When you need help");
        leafNodeOneHelps.put(AMessageType.INVENTORY, "When you need to know what you have");
        when(this.leafNodeOne.getCommands(Mockito.isA(CommandContext.class))).thenAnswer(i -> {
            CommandContext cc = i.getArgument(0);
            if (cc != null) {
                cc.addHelps(leafNodeOneHelps);
            }
            return leafNodeOneHelps;
        });

        Map<AMessageType, String> leafNodeTwoHelps = new HashMap<>();
        leafNodeTwoHelps.put(AMessageType.CAST, "If you are a caster");
        when(this.leafNodeTwo.getCommands(Mockito.isA(CommandContext.class))).thenAnswer(i -> {
            CommandContext cc = i.getArgument(0);
            if (cc != null) {
                cc.addHelps(leafNodeTwoHelps);
            }
            return leafNodeTwoHelps;
        });

        Map<AMessageType, String> branchNodeHelps = new HashMap<>();
        branchNodeHelps.put(AMessageType.HELP, "When you get higher help");
        branchNodeHelps.put(AMessageType.SEE, "I added something!");
        when(this.branchNode.getCommands(Mockito.isA(CommandContext.class))).thenAnswer(i -> {
            CommandContext cc = i.getArgument(0);
            if (cc != null) {
                cc.addHelps(branchNodeHelps);
            }
            return branchNodeHelps;
        });

        // root node does nothing
        when(this.rootNode.getCommands(any())).thenReturn(null);

    }

    private CommandContext follow(CommandChainHandler mh, CommandContext ctx) {
        if (ctx == null) {
            ctx = new CommandContext();
        }
        CommandChainHandler following = mh;
        while (following != null) {
            following.getCommands(ctx);
            following = following.getSuccessor();
        }
        return ctx;
    }

    @Test
    void testGetCommands() {
        buildTree();
        Map<AMessageType, String> receivedNodeOne = this.follow(this.leafNodeOne, null).getHelps();
        System.out.println(receivedNodeOne);
        Truth.assertThat(receivedNodeOne).containsKey(AMessageType.INVENTORY);
        Truth.assertThat(receivedNodeOne).containsKey(AMessageType.HELP);
        Truth.assertThat(receivedNodeOne).doesNotContainKey(AMessageType.CAST);
        Truth.assertThat(receivedNodeOne).containsKey(AMessageType.SEE);
        Truth.assertThat(receivedNodeOne).containsKey(AMessageType.HELP);
        Truth.assertThat(receivedNodeOne.get(AMessageType.HELP)).doesNotContain("higher");

        Map<AMessageType, String> rec = this.follow(this.leafNodeTwo, null).getHelps();
        Truth.assertThat(rec).doesNotContainKey(AMessageType.INVENTORY);
        Truth.assertThat(rec).containsKey(AMessageType.HELP);
        Truth.assertThat(rec).containsKey(AMessageType.CAST);
        Truth.assertThat(rec).containsKey(AMessageType.SEE);
        Truth.assertThat(rec).containsKey(AMessageType.HELP);
        Truth.assertThat(rec.get(AMessageType.HELP)).contains("higher");
    }

    // @Test
    // void testHandleMessage() {

    // }
}
