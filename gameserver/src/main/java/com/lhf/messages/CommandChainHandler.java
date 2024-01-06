package com.lhf.messages;

import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;

import com.lhf.messages.in.AttackMessage;
import com.lhf.messages.in.CastMessage;
import com.lhf.messages.in.CreateInMessage;
import com.lhf.messages.in.DropMessage;
import com.lhf.messages.in.EquipMessage;
import com.lhf.messages.in.ExitMessage;
import com.lhf.messages.in.FollowMessage;
import com.lhf.messages.in.GoMessage;
import com.lhf.messages.in.HelpInMessage;
import com.lhf.messages.in.InteractMessage;
import com.lhf.messages.in.InventoryMessage;
import com.lhf.messages.in.LewdInMessage;
import com.lhf.messages.in.ListPlayersMessage;
import com.lhf.messages.in.PassMessage;
import com.lhf.messages.in.RepeatInMessage;
import com.lhf.messages.in.RestMessage;
import com.lhf.messages.in.SayMessage;
import com.lhf.messages.in.SeeMessage;
import com.lhf.messages.in.ShoutMessage;
import com.lhf.messages.in.SpellbookMessage;
import com.lhf.messages.in.StatsInMessage;
import com.lhf.messages.in.StatusMessage;
import com.lhf.messages.in.TakeMessage;
import com.lhf.messages.in.UnequipMessage;
import com.lhf.messages.in.UseMessage;

public interface CommandChainHandler extends GameEventProcessorHub {

    public void setSuccessor(CommandChainHandler successor);

    public CommandChainHandler getSuccessor();

    public default void intercept(CommandChainHandler interceptor) {
        interceptor.setSuccessor(this.getSuccessor());
        this.setSuccessor(interceptor);
    }

    public abstract CommandContext addSelfToContext(CommandContext ctx);

    public static interface ICommandHandlerMetadata {
        public boolean isEnabled();

        public String getHelpString();

        public boolean isEmpoolEnabled();
    }

    public static class CommandHandlerMetadata implements ICommandHandlerMetadata {
        protected boolean enabled;
        protected String helpString;

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public String getHelpString() {
            return helpString;
        }

        @Override
        public boolean isEmpoolEnabled() {
            return false;
        }

    }

    public abstract Map<CommandMessage, ICommandHandlerMetadata> getCommands(CommandContext ctx);

    private static CommandContext addHelps(Map<CommandMessage, ICommandHandlerMetadata> commands, CommandContext ctx) {
        if (ctx == null) {
            ctx = new CommandContext();
        }
        if (commands == null) {
            return ctx;
        }

        for (final Entry<CommandMessage, ICommandHandlerMetadata> entry : commands.entrySet()) {
            final ICommandHandlerMetadata metadata = entry.getValue();
            if (metadata != null && metadata.isEnabled() && metadata.getHelpString() != null) {
                ctx.addHelp(entry.getKey(), metadata.getHelpString());
            }
        }
        return ctx;
    }

    // dispatch
    public default CommandContext.Reply handleInCommand(CommandContext ctx, AttackMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, CastMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, CreateInMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, DropMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, EquipMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, ExitMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, FollowMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, GoMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, HelpInMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, InteractMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, InventoryMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, LewdInMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, ListPlayersMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, PassMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, RepeatInMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, RestMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, SayMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, SeeMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, ShoutMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, SpellbookMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, StatsInMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, StatusMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, TakeMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, UnequipMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

    public default CommandContext.Reply handleInCommand(CommandContext ctx, UseMessage cmd) {
        ctx = this.addSelfToContext(ctx);
        ctx = CommandChainHandler.addHelps(this.getCommands(ctx), ctx);
        CommandChainHandler successor = this.getSuccessor();
        if (successor == null) {
            this.log(Level.WARNING,
                    String.format("No successor handled message: %s\n%s", cmd, ctx.toString()));
            return ctx.failhandle();
        }
        return successor.handleInCommand(ctx, cmd);
    }

}
