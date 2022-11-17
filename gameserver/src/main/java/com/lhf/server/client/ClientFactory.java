package com.lhf.server.client;

import com.lhf.server.client.SendStrategyFactory.SendStrategyType;

public abstract class ClientFactory {
    public class CannotBuildClientException extends Exception {
        protected CannotBuildClientException(String reason) {
            super(reason);
        }

        protected CannotBuildClientException(String reason, Throwable throwable) {
            super(reason, throwable);
        }

        protected CannotBuildClientException(Throwable throwable) {
            super(throwable);
        }
    }

    protected ClientManager clientManager;
    protected SendStrategyType sendStrategyFactoryType;

    public ClientFactory(ClientManager clientManager) {
        if (clientManager == null) {
            this.clientManager = new ClientManager();
        } else {
            this.clientManager = clientManager;
        }
    }

    public ClientManager getClientManager() {
        return this.clientManager;
    }

    public ClientFactory SetSendStrategyType(SendStrategyType outType) {
        this.sendStrategyFactoryType = outType;
        return this;
    }

    public abstract Client build() throws CannotBuildClientException;
}
