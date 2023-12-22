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
import com.lhf.game.battle.BattleStats.BattleStatsQuery;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.creature.intelligence.GroupAIRunner;
import com.lhf.game.creature.intelligence.handlers.BattleTurnHandler.TargetLists;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageChainHandler;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;
import com.lhf.messages.out.BattleRoundMessage;
import com.lhf.messages.out.StatsOutMessage;

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
                Optional.of(StatsOutMessage.getBuilder()
                        .addRecords(battleStats.getBattleStatSet(BattleStatsQuery.ONLY_LIVING))
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

        MessageChainHandler interceptor = Mockito.mock(MessageChainHandler.class);
        Mockito.doNothing().when(interceptor).setSuccessor(Mockito.any());
        Mockito.when(interceptor.getSuccessor()).thenReturn(searcher);
        Mockito.doCallRealMethod().when(interceptor).intercept(Mockito.any(MessageChainHandler.class));
        Mockito.when(interceptor.handle(Mockito.any(CommandContext.class), Mockito.any(Command.class)))
                .thenAnswer(new Answer<CommandContext.Reply>() {

                    @Override
                    public Reply answer(InvocationOnMock invocation) throws Throwable {
                        CommandContext ctx = invocation.getArgument(0);
                        Command cmd = invocation.getArgument(1);
                        if (cmd.getType().equals(CommandMessage.ATTACK)
                                && cmd.getWhole().contains("bloohoo")) {
                            BadTargetSelectedMessage btsm = BadTargetSelectedMessage
                                    .getBuilder()
                                    .setBde(BadTargetOption.DNE)
                                    .setBadTarget("bloohoo")
                                    .setPossibleTargets(new ArrayList<>()).Build();
                            ctx.receive(btsm);
                            return ctx.handled();
                        }
                        if (cmd.getType().equals(CommandMessage.SEE)) {
                            return ctx.handled();
                        }
                        return interceptor.getSuccessor().handleChain(ctx, cmd);
                    }

                });

        searcher.npc.intercept(interceptor);

        // trigger it
        AIComBundle.eventAccepter.accept(searcher.npc,
                BattleRoundMessage.getBuilder().setAboutCreature(searcher.npc).setNeeded()
                        .Build());

        Truth8.assertThat(searcher.npc.getHarmMemories().getLastAttackerName()).isEmpty();
        Mockito.verify(searcher.mockedWrappedHandler, Mockito.timeout(1000)).handle(Mockito.any(),
                Mockito.argThat((command) -> command != null && command.getWhole().contains("PASS")));
    }

}
