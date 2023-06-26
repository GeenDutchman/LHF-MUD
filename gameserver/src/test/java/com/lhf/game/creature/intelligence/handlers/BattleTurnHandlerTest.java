package com.lhf.game.creature.intelligence.handlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
import com.lhf.game.EffectPersistence;
import com.lhf.game.TickType;
import com.lhf.game.creature.CreatureEffect;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.intelligence.AIComBundle;
import com.lhf.game.creature.intelligence.BasicAI.BattleMemories;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.creature.intelligence.GroupAIRunner;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;
import com.lhf.messages.out.BattleTurnMessage;
import com.lhf.messages.out.CreatureAffectedMessage;

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

        List<Map.Entry<String, Float>> targets = handler.chooseEnemyTarget(finder.brain.getBattleMemories(),
                finder.npc.getFaction());
        Truth.assertThat(targets).isEmpty();

        CreatureEffectSource source = new CreatureEffectSource("test", new EffectPersistence(TickType.INSTANT), null,
                "For a test", false).addDamage(new DamageDice(1, DieType.HUNDRED, DamageFlavor.BLUDGEONING));

        finder.brain.getBattleMemories().update(CreatureAffectedMessage.getBuilder().setAffected(finder.npc)
                .setEffect(new CreatureEffect(source, attacker.npc, attacker.npc)).Build());

        targets = handler.chooseEnemyTarget(finder.brain.getBattleMemories(), finder.npc.getFaction());
        Truth.assertThat(targets).isNotEmpty();
        Truth.assertThat(targets).hasSize(1);

        finder.brain.getBattleMemories().update(CreatureAffectedMessage.getBuilder().setAffected(finder.npc)
                .setEffect(new CreatureEffect(source, subAttacker.npc, subAttacker.npc)).Build());

        targets = handler.chooseEnemyTarget(finder.brain.getBattleMemories(), finder.npc.getFaction());
        Truth.assertThat(targets).isNotEmpty();
        Truth.assertThat(targets).hasSize(2);
        Truth.assertThat(targets.get(0).getValue()).isAtLeast(targets.get(1).getValue());
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

}
