package com.lhf.server.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;

import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.BadFatalEvent;
import com.lhf.messages.in.AMessageType;
import com.lhf.server.interfaces.ConnectionListener;

public class ClientHandle extends Client implements Runnable {
    private Socket socket;

    private boolean connected;
    private boolean killIt;

    private ConnectionListener connectionListener;
    private BufferedReader in;

    private String repeatCommand;
    private RepeatHandler repeatHandler = new RepeatHandler();

    protected class RepeatHandler implements CommandHandler {
        private static final Set<String> noPrefix = Set.of(AMessageType.REPEAT.name(), AMessageType.CREATE.name());

        public static final boolean isValidRepeatCommand(String prospective) {
            if (prospective == null || prospective.isBlank()) {
                return false;
            }
            return RepeatHandler.noPrefix.stream()
                    .noneMatch(prefix -> prospective.regionMatches(true, 0, prefix, 0, prefix.length()));
        }

        @Override
        public AMessageType getHandleType() {
            return AMessageType.REPEAT;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            Client client = ctx.getClient();
            if (client != null && client instanceof ClientHandle cHandle) {
                String repeater = cHandle.getRepeatCommand();
                if (repeater != null && !repeater.isBlank()) {
                    return Optional.of(String.format("Will send the following command once again: \"%s\"", repeater));
                } else {
                    return Optional.of(String.format(
                            "Will re-send a command as long as that command that does not begin with any of the following: %s",
                            RepeatHandler.noPrefix));
                }
            }
            return Optional.empty();
        }

        @Override
        public boolean isEnabled(CommandContext ctx) {
            Client client = ctx.getClient();
            if (client != null && client instanceof ClientHandle cHandle) {
                return RepeatHandler.isValidRepeatCommand(cHandle.getRepeatCommand());
            }
            return false;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            Client client = ctx.getClient();
            if (client != null && client instanceof ClientHandle cHandle) {
                String repeater = cHandle.getRepeatCommand();
                if (repeater != null && !repeater.isBlank()) {
                    return cHandle.ProcessString(repeater);
                }
            }
            return ctx.failhandle();
        }

        @Override
        public CommandChainHandler getChainHandler(CommandContext ctx) {
            return ClientHandle.this;
        }

    }

    protected ClientHandle(Socket socket, ConnectionListener cl) throws IOException {
        super();
        this.socket = socket;
        this.out = new PrintWriterSendStrategy(socket.getOutputStream());
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        connected = true;
        killIt = false;
        this.connectionListener = cl;
        this.log(Level.FINEST, "ClientHandle created");
    }

    @Override
    public void run() {
        this.log(Level.FINER, "Running ClientHandle");
        String value;
        try {
            while (!this.killIt && ((value = in.readLine()) != null)) {
                this.ProcessString(value);
                this.setRepeatCommand(value);
            }
        } catch (IOException e) {
            final BadFatalEvent fatal = BadFatalEvent.getBuilder().setException(e).setExtraInfo("recoverable").Build();
            this.log(Level.SEVERE, fatal.toString(), e);
            Client.eventAccepter.accept(this, fatal);
        } catch (Exception e) {
            final BadFatalEvent fatal = BadFatalEvent.getBuilder().setException(e).setExtraInfo("irrecoverable")
                    .Build();
            this.log(Level.SEVERE, fatal.toString(), e);
            Client.eventAccepter.accept(this, fatal);
            throw e;
        } finally {
            connectionListener.clientConnectionTerminated(id); // let connectionListener know that it
                                                               // is over
            this.kill();
        }
    }

    public void kill() {
        this.log(Level.INFO, "Disconnecting ClientHandler");
        this.killIt = true;
        if (connected && socket.isConnected()) {
            try {
                socket.close();
                connected = false;
            } catch (IOException e) {
                this.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    void disconnect() {
        this.log(Level.INFO, "Requesting ClientHandler to stop");
        this.killIt = true;
    }

    public String getRepeatCommand() {
        return repeatCommand;
    }

    public void setRepeatCommand(String prospective) {
        if (RepeatHandler.isValidRepeatCommand(prospective)) {
            this.repeatCommand = prospective;
        }
    }

    @Override
    public Map<AMessageType, CommandHandler> getCommands(CommandContext ctx) {
        Map<AMessageType, CommandHandler> cmdMap = super.getCommands(ctx);
        cmdMap.put(AMessageType.REPEAT, this.repeatHandler);
        return cmdMap;
    }

    @Override
    public synchronized void log(Level logLevel, String logMessage) {
        super.log(logLevel, logMessage);
    }

    @Override
    public synchronized void log(Level logLevel, Supplier<String> logMessageSupplier) {
        super.log(logLevel, logMessageSupplier);
    }

    @Override
    public synchronized void log(Level level, String msg, Throwable thrown) {
        super.log(level, msg, thrown);
    }

    @Override
    public synchronized void log(Level level, Throwable thrown, Supplier<String> msgSupplier) {
        super.log(level, thrown, msgSupplier);
    }

}
