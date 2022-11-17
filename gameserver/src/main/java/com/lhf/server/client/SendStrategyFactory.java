package com.lhf.server.client;

import java.io.OutputStream;

public class SendStrategyFactory {
    public enum SendStrategyType {
        DO_NOTHING, STRING_BUFFER, LIST_BUFFER, PRINT_WRITER;
    }

    protected class CannotBuildSendStrategyException extends Exception {
    }

    public SendStrategy build(SendStrategyType type, OutputStream outputStream)
            throws CannotBuildSendStrategyException {
        switch (type) {
            case LIST_BUFFER:
                return new ListBufferSendStrategy();
            case PRINT_WRITER:
                if (outputStream == null) {
                    throw new CannotBuildSendStrategyException();
                }
                return new PrintWriterSendStrategy(outputStream);
            case STRING_BUFFER:
                return new StringBufferSendStrategy();
            case DO_NOTHING:
                // fallthrough
            default:
                return new DoNothingSendStrategy();

        }
    }
}
