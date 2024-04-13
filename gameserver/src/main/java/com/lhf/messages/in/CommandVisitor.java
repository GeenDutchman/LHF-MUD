package com.lhf.messages.in;

import java.util.function.BiFunction;

import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;

public interface CommandVisitor extends BiFunction<CommandContext, Command, CommandContext.Reply> {

    public static interface CommandVisitorAcceptor {
        public abstract CommandContext.Reply acceptCommandVisitor(CommandContext ctx, CommandVisitor visitor);
    }

    public interface Blank extends CommandVisitor {
        public default CommandContext.Reply visit(CommandContext ctx, AttackMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, CastMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, CreateInMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, DropMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, EquipMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, ExitMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, FollowMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, GoMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, HelpInMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, InteractMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, InventoryMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, LewdInMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, ListPlayersMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, PassMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, RepeatInMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, RestMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, SayMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, SeeMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, ShoutMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, SpellbookMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, StatsInMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, StatusMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, TakeMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, UnequipMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }

        public default CommandContext.Reply visit(CommandContext ctx, UseMessage command) {
            if (ctx == null) {
                ctx = new CommandContext();
            }
            return ctx.failhandle();
        }
    }

    @Override
    default Reply apply(CommandContext ctx, Command command) {
        if (command != null) {
            return command.acceptCommandVisitor(ctx, this);
        } else {
            ctx = new CommandContext();
            return ctx.failhandle();
        }
    }

    public abstract CommandContext.Reply visit(CommandContext ctx, AttackMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, CastMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, CreateInMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, DropMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, EquipMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, ExitMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, FollowMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, GoMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, HelpInMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, InteractMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, InventoryMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, LewdInMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, ListPlayersMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, PassMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, RepeatInMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, RestMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, SayMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, SeeMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, ShoutMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, SpellbookMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, StatsInMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, StatusMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, TakeMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, UnequipMessage command);

    public abstract CommandContext.Reply visit(CommandContext ctx, UseMessage command);

}
