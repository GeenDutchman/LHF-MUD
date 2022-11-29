package com.lhf.game.creature.intelligence;

import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.google.common.truth.Truth;
import com.lhf.game.creature.NonPlayerCharacter;
import com.lhf.messages.out.FatalMessage;
import com.lhf.messages.out.SeeOutMessage;
import com.lhf.messages.out.WelcomeMessage;
import com.lhf.server.client.ClientID;

@ExtendWith(MockitoExtension.class)
public class GroupAIRunnerTest {
    @Test
    void testProcessLeavesSome() throws InterruptedException {
        GroupAIRunner runner = Mockito.spy(new GroupAIRunner(false, 2));
        BasicAI qAi = runner.register(new NonPlayerCharacter());
        qAi.sendMsg(new SeeOutMessage("denied once"));
        qAi.sendMsg(new WelcomeMessage());
        qAi.sendMsg(new FatalMessage());

        Truth.assertThat(qAi.size()).isEqualTo(3);
        Truth.assertThat(runner.size()).isEqualTo(1);
        ClientID next = runner.getNext(1, TimeUnit.SECONDS);
        runner.process(next);
        Truth.assertThat(qAi.size()).isEqualTo(1);
        Truth.assertThat(runner.size()).isEqualTo(1);
        next = runner.getNext(1, TimeUnit.SECONDS);
        runner.process(next);
        Truth.assertThat(qAi.size()).isEqualTo(0);
        Truth.assertThat(runner.size()).isEqualTo(0);
        next = runner.getNext(1, TimeUnit.SECONDS);
        Truth.assertThat(next).isNull();

        verify(runner).getAttention(qAi.getClientID());
    }
}
