package com.lhf.server.client;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.lhf.messages.events.GameEvent;

public class ModalSendStrategy implements SendStrategy {
    private final Map<String, SendStrategy> strategies;
    private final LoggerSendStrategy fallbackStrategy = new LoggerSendStrategy(
            Logger.getLogger(String.format("%s.%d", this.getClass().getName(), this.hashCode())), Level.INFO);
    private String current = "default";

    public ModalSendStrategy() {
        this.strategies = new LinkedHashMap<>();
        this.strategies.put("default", fallbackStrategy);
    }

    public ModalSendStrategy(Logger logger, Level level) {
        this.strategies = new LinkedHashMap<>();
        this.strategies.put("default", new LoggerSendStrategy(logger, level));
    }

    public ModalSendStrategy addStrategy(String code, SendStrategy strategy) {
        if (code == null || strategy == null) {
            return this;
        }
        this.strategies.put(code, strategy);
        return this;
    }

    @Override
    public void metaControl(String code) {
        if (code == null || !this.strategies.containsKey(code)) {
            this.current = "default";
        } else {
            this.current = code;
        }
    }

    public SendStrategy getCurrentStrategy() {
        return this.strategies.getOrDefault(current, fallbackStrategy);
    }

    public String getCurrent() {
        return current;
    }

    @Override
    public void send(GameEvent toSend) {
        if (toSend == null) {
            return;
        }
        SendStrategy strategy = this.strategies.getOrDefault(this.current, fallbackStrategy);
        strategy.send(toSend);
    }

}
