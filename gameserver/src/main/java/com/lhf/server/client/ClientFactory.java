package com.lhf.server.client;

public interface ClientFactory {
    public class CannotBuildClientException extends Exception {
    }

    public ClientFactory SetSendStrategy(SendStrategyFactory.SendStrategyType outType);

    public Client build() throws CannotBuildClientException;
}
