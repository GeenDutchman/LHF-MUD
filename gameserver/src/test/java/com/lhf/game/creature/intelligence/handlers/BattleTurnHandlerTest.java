package com.lhf.game.creature.intelligence.handlers;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.truth.Truth8;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.creature.intelligence.GroupAIRunner;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;
import com.lhf.messages.out.BattleTurnMessage;

public class BattleTurnHandlerTest {
    @Spy
    private GroupAIRunner aiRunner = new GroupAIRunner(false, 2, 250, TimeUnit.MILLISECONDS);

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        AIComBundle.setAIRunner(this.aiRunner.start());
    }

    @Test
    void testChooseEnemyTarget() {

    }

    @Test
    void testMeleeAttackTargets() {
        AIComBundle searcher = new AIComBundle();
        searcher.npc.setInBattle(true);

        MessageHandler interceptor = Mockito.mock(MessageHandler.class);
        Mockito.doNothing().when(interceptor).setSuccessor(Mockito.any());
        Mockito.when(interceptor.getSuccessor()).thenReturn(searcher);
        Mockito.doCallRealMethod().when(interceptor).intercept(Mockito.any(MessageHandler.class));
        Mockito.when(interceptor.handleMessage(Mockito.any(CommandContext.class), Mockito.any(Command.class)))
                .thenAnswer(new Answer<CommandContext.Reply>() {

                    @Override
                    public Reply answer(InvocationOnMock invocation) throws Throwable {
                        CommandContext ctx = invocation.getArgument(0);
                        Command cmd = invocation.getArgument(1);
                        if (cmd.getType().equals(CommandMessage.ATTACK) && cmd.getWhole().contains("bloohoo")) {
                            BadTargetSelectedMessage btsm = BadTargetSelectedMessage.getBuilder()
                                    .setBde(BadTargetOption.DNE)
                                    .setBadTarget("bloohoo").setPossibleTargets(new ArrayList<>()).Build();
                            ctx.sendMsg(btsm);
                            return ctx.handled();
                        }
                        if (cmd.getType().equals(CommandMessage.SEE)) {
                            return ctx.handled();
                        }
                        return interceptor.getSuccessor().handleMessage(ctx, cmd);
                    }

                });

        searcher.npc.intercept(interceptor);

        // trigger it
        searcher.npc.sendMsg(BattleTurnMessage.getBuilder().setCurrentCreature(searcher.npc).setYesTurn(true).Build());

        Truth8.assertThat(searcher.brain.getBattleMemories().getLastAttakerName()).isEmpty();
        Mockito.verify(searcher.mockedWrappedHandler, Mockito.timeout(1000)).handleMessage(Mockito.any(),
                Mockito.argThat((command) -> command != null && command.getWhole().contains("PASS")));
        Mockito.verifyNoMoreInteractions(searcher.mockedWrappedHandler);
    }

    @Test
    void testHandle() {

    }
}
