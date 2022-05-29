package com.lhf.game.magic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.Player;
import com.lhf.game.magic.concrete.ShockBolt;
import com.lhf.game.magic.concrete.Thaumaturgy;
import com.lhf.game.magic.interfaces.CreatureAffector;
import com.lhf.game.magic.interfaces.RoomAffector;
import com.lhf.game.map.Dungeon;
import com.lhf.game.map.Room;
import com.lhf.server.client.user.UserID;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.CastMessage;
import com.lhf.messages.out.GameMessage;

public class ThirdPower implements MessageHandler {
    // buff debuff
    // damage heal
    // summon banish

    /*
     * Some spells target creatures
     * Some spells target items
     * Some spells target rooms
     * Some spells target the dungeon
     * 
     * 
     */

    private ShockBolt shockBolt;
    private Thaumaturgy thaumaturgy;
    private MessageHandler successor;
    private HashMap<CommandMessage, String> cmds;

    public ThirdPower(MessageHandler successor) {
        this.successor = successor;
        this.shockBolt = new ShockBolt();
        this.thaumaturgy = new Thaumaturgy();
        this.cmds = this.generateCommands();
    }

    private HashMap<CommandMessage, String> generateCommands() {
        HashMap<CommandMessage, String> toGenerate = new HashMap<>();
        StringJoiner sj = new StringJoiner(" ");
        sj.add("\"cast [invocation]\"").add("Casts the spell that has the matching invocation.").add("\n");
        sj.add("\"cast [invocation] at [target]\"").add("Some spells need you to name a target.").add("\n");
        sj.add("\"cast [invocation] use [level]\"").add(
                "Sometimes you want to put more power into your spell, so put a higher level number for the level.")
                .add("\n");
        toGenerate.put(CommandMessage.CAST, sj.toString()); // TODO: make this help not even show up for non-casters
        return toGenerate;
    }

    public ISpell onCast(CubeHolder cubeHolder, String invocation) {
        if (invocation.equals(shockBolt.getInvocation())) {
            return this.shockBolt.setCaster(cubeHolder);
        } else if (invocation.equals(thaumaturgy.getInvocation())) {
            return this.thaumaturgy.setCaster(cubeHolder);
        }
        return this.thaumaturgy.setCaster(cubeHolder);
    }

    private boolean handleCast(CommandContext ctx, Command msg) {
        CastMessage casting = (CastMessage) msg;
        Player caster = (Player) ctx.getCreature(); // TODO: every caster can cast
        ISpell spell = this.onCast(caster, casting.getInvocation());
        if (spell instanceof RoomAffector) {
            RoomAffector roomSpell = (RoomAffector) spell;
            String castResult = ctx.getRoom().cast(caster, roomSpell.setRoom(ctx.getRoom()));
            ctx.sendMsg(new GameMessage(castResult));
            return true;
        }
        if (spell instanceof CreatureAffector && casting.getTarget().length() > 0) {
            Creature targeCreature = null;
            List<Creature> possTargets = ctx.getRoom().getCreaturesInRoom(casting.getTarget());
            if (possTargets.size() == 1) {
                targeCreature = possTargets.get(0);
            }
            if (targeCreature == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("You cannot attack '").append(casting.getTarget()).append("' ");
                if (possTargets.size() == 0) {
                    sb.append("because it does not exist.");
                } else {
                    sb.append("because it could be any of these:\n");
                    for (Creature c : possTargets) {
                        sb.append(c.getColorTaggedName()).append(" ");
                    }
                }
                sb.append("\r\n");
                ctx.sendMsg(new GameMessage(sb.toString()));
                return true;
            }
            CreatureAffector cSpell = (CreatureAffector) spell;
            cSpell.addTarget(targeCreature);
            String castResult = ctx.getRoom().cast(caster, cSpell);
            ctx.sendMsg(new GameMessage(castResult));
            return true;
        }
        ctx.sendMsg(new GameMessage("Bad cast?"));
        return true;
    }

    @Override
    public void setSuccessor(MessageHandler successor) {
        this.successor = successor;
    }

    @Override
    public MessageHandler getSuccessor() {
        return this.successor;
    }

    @Override
    public Boolean handleMessage(CommandContext ctx, Command msg) {
        if (msg.getType() == CommandMessage.CAST) {
            Creature attempter = ctx.getCreature();
            if (!(attempter instanceof CubeHolder)) {
                ctx.sendMsg(new GameMessage("You are not a caster type, so you cannot cast spells."));
                if (ctx.getRoom() != null) {
                    ctx.getRoom().sendMessageToAllExcept(new GameMessage(
                            attempter.getColorTaggedName() + " mumbles to try and cast a spell...nothing happens."),
                            attempter.getName());
                }
            } else {
                this.handleCast(ctx, msg);
            }
            return true;
        }
        return MessageHandler.super.handleMessage(ctx, msg);
    }

    @Override
    public Map<CommandMessage, String> getCommands() {
        return this.cmds;
    }

}
