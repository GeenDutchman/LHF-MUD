package com.lhf.server.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;

import com.lhf.messages.ClientMessenger;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageChainHandler;
import com.lhf.messages.out.FatalMessage;
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
        private static final Set<String> noPrefix = Set.of(CommandMessage.REPEAT.name(), CommandMessage.CREATE.name());

        public static final boolean isValidRepeatCommand(String prospective) {
            if (prospective == null || prospective.isBlank()) {
                return false;
            }
            return RepeatHandler.noPrefix.stream()
                    .noneMatch(prefix -> prospective.regionMatches(true, 0, prefix, 0, prefix.length()));
        }

        private static final Predicate<CommandContext> enabledPredicate = RepeatHandler.defaultPredicate.and(ctx -> {
            ClientMessenger client = ctx.getClientMessenger();
            if (client != null && client instanceof ClientHandle cHandle) {
                return RepeatHandler.isValidRepeatCommand(cHandle.getRepeatCommand());
            }
            return false;
        });

        @Override
        public CommandMessage getHandleType() {
            return CommandMessage.REPEAT;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            ClientMessenger client = ctx.getClientMessenger();
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
        public Predicate<CommandContext> getEnabledPredicate() {
            return RepeatHandler.enabledPredicate;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            ClientMessenger client = ctx.getClientMessenger();
            if (client != null && client instanceof ClientHandle cHandle) {
                String repeater = cHandle.getRepeatCommand();
                if (repeater != null && !repeater.isBlank()) {
                    return cHandle.ProcessString(repeater);
                }
            }
            return ctx.failhandle();
        }

        @Override
        public MessageChainHandler getChainHandler() {
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
        this.logger.log(Level.FINEST, "ClientHandle created");
    }

    @Override
    public void run() {
        this.logger.log(Level.FINER, "Running ClientHandle");
        String value;
        try {
            while (!this.killIt && ((value = in.readLine()) != null)) {
                this.ProcessString(value);
                this.setRepeatCommand(value);
            }
        } catch (IOException e) {
            final FatalMessage fatal = FatalMessage.getBuilder().setException(e).setExtraInfo("recoverable").Build();
            this.logger.log(Level.SEVERE, fatal.toString(), e);
            sendMsg(fatal);
        } catch (Exception e) {
            final FatalMessage fatal = FatalMessage.getBuilder().setException(e).setExtraInfo("irrecoverable").Build();
            this.logger.log(Level.SEVERE, fatal.toString(), e);
            sendMsg(fatal);
            throw e;
        } finally {
            connectionListener.clientConnectionTerminated(id); // let connectionListener know that it is over
            this.kill();
        }
    }

    public void kill() {
        this.logger.log(Level.INFO, "Disconnecting ClientHandler");
        this.killIt = true;
        if (connected && socket.isConnected()) {
            try {
                socket.close();
                connected = false;
            } catch (IOException e) {
                this.logger.log(Level.WARNING, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    void disconnect() {
        this.logger.log(Level.INFO, "Requesting ClientHandler to stop");
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
    public Map<CommandMessage, CommandHandler> getCommands(CommandContext ctx) {
        Map<CommandMessage, CommandHandler> cmdMap = super.getCommands(ctx);
        cmdMap.put(CommandMessage.REPEAT, this.repeatHandler);
        return cmdMap;
    }

}
