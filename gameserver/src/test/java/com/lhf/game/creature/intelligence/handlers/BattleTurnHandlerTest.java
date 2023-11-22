package com.lhf.game.creature.intelligence.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.truth.Truth;
import com.google.common.truth.Truth8;
import com.lhf.game.battle.BattleStats;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.creature.intelligence.GroupAIRunner;
import com.lhf.game.creature.intelligence.handlers.BattleTurnHandler.TargetLists;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.events.GameEventContext;
import com.lhf.game.events.GameEventHandler;
import com.lhf.game.events.GameEventContext.Reply;
import com.lhf.game.events.messages.Command;
import com.lhf.game.events.messages.CommandMessage;
import com.lhf.game.events.messages.out.BadTargetSelectedMessage;
import com.lhf.game.events.messages.out.BattleTurnMessage;
import com.lhf.game.events.messages.out.StatsOutMessage;
import com.lhf.game.events.messages.out.BadTargetSelectedMessage.BadTargetOption;

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
        AIComBundle finder = new AIComBundle();
        finder.npc.setFaction(CreatureFaction.RENEGADE);
        AIComBundle attacker = new AIComBundle();
        AIComBundle subAttacker = new AIComBundle();

        BattleTurnHandler handler = new BattleTurnHandler();
        finder.brain.addHandler(handler);
        BattleStats battleStats = new BattleStats()
                .initialize(List.of(finder.npc, attacker.npc, subAttacker.npc));

        TargetLists targets = handler.chooseTargets(
                Optional.of(StatsOutMessage.getBuilder().addRecords(battleStats.getBattleStatSet())
                        .Build()),
                finder.npc.getHarmMemories(),
                finder.npc.getFaction());
        Truth.assertThat(targets.enemies()).hasSize(2);
        Truth.assertThat(targets.enemies().get(0).getValue()).isAtLeast(targets.enemies().get(1).getValue());

    }

    @Test
    void testMeleeAttackTargets() {
        AIComBundle searcher = new AIComBundle();
        searcher.npc.setInBattle(true);

        GameEventHandler interceptor = Mockito.mock(GameEventHandler.class);
        Mockito.doNothing().when(interceptor).setSuccessor(Mockito.any());
        Mockito.when(interceptor.getSuccessor()).thenReturn(searcher);
        Mockito.doCallRealMethod().when(interceptor).intercept(Mockito.any(GameEventHandler.class));
        Mockito.when(interceptor.handleMessage(Mockito.any(GameEventContext.class), Mockito.any(Command.class)))
                .thenAnswer(new Answer<GameEventContext.Reply>() {

                    @Override
                    public Reply answer(InvocationOnMock invocation) throws Throwable {
                        GameEventContext ctx = invocation.getArgument(0);
                        Command cmd = invocation.getArgument(1);
                        if (cmd.getGameEventType().equals(CommandMessage.ATTACK)
                                && cmd.getWhole().contains("bloohoo")) {
                            BadTargetSelectedMessage btsm = BadTargetSelectedMessage
                                    .getBuilder()
                                    .setBde(BadTargetOption.DNE)
                                    .setBadTarget("bloohoo")
                                    .setPossibleTargets(new ArrayList<>()).Build();
                            ctx.sendMsg(btsm);
                            return ctx.handled();
                        }
                        if (cmd.getGameEventType().equals(CommandMessage.SEE)) {
                            return ctx.handled();
                        }
                        return interceptor.getSuccessor().handleMessage(ctx, cmd);
                    }

                });

        searcher.npc.intercept(interceptor);

        // trigger it
        searcher.npc.sendMsg(BattleTurnMessage.getBuilder().setCurrentCreature(searcher.npc).setYesTurn(true)
                .Build());

        Truth8.assertThat(searcher.npc.getHarmMemories().getLastAttackerName()).isEmpty();
        Mockito.verify(searcher.mockedWrappedHandler, Mockito.timeout(1000)).handleMessage(Mockito.any(),
                Mockito.argThat((command) -> command != null && command.getWhole().contains("PASS")));
    }

}
