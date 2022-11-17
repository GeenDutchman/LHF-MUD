package com.lhf.server.client;

public interface ClientFactory {
    public class CannotBuildClientException extends Exception {
    }

    public ClientFactory SetSendStrategy(SendStrategy out);

    public Client build() throws CannotBuildClientException;
}
