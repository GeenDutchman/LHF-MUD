package com.lhf.game.magic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.lhf.game.creature.Creature;
import com.lhf.game.magic.concrete.ShockBolt;
import com.lhf.game.magic.concrete.Thaumaturgy;
import com.lhf.game.magic.interfaces.CreatureTargetingSpell;
import com.lhf.game.magic.interfaces.RoomAffector;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.CastMessage;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.SpellFizzleMessage;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;
import com.lhf.messages.out.SpellFizzleMessage.SpellFizzleType;

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

    public ISpell onCast(Creature cubeHolder, String invocation) {
        if (invocation.equals(shockBolt.getInvocation())) {
            return this.shockBolt.setCaster(cubeHolder);
        } else if (invocation.equals(thaumaturgy.getInvocation())) {
            return this.thaumaturgy.setCaster(cubeHolder);
        }
        return this.thaumaturgy.setCaster(cubeHolder);
    }

    private boolean handleCast(CommandContext ctx, Command msg) {
        CastMessage casting = (CastMessage) msg;
        Creature caster = ctx.getCreature();
        ISpell spell = this.onCast(caster, casting.getInvocation());
        if (spell instanceof RoomAffector) {
            RoomAffector roomSpell = (RoomAffector) spell;
            ctx.getRoom().cast(caster, roomSpell.setRoom(ctx.getRoom()));
            return true;
        }
        if (spell instanceof CreatureTargetingSpell && casting.getTarget().length() > 0) {
            Creature targetCreature = null;
            List<Creature> possTargets = ctx.getRoom().getCreaturesInRoom(casting.getTarget());
            if (possTargets.size() == 1) {
                targetCreature = possTargets.get(0);
            }
            if (targetCreature == null) {
                if (possTargets.size() == 0) {
                    ctx.sendMsg(new BadTargetSelectedMessage(BadTargetOption.DNE, casting.getTarget(), possTargets));
                } else {
                    ctx.sendMsg(
                            new BadTargetSelectedMessage(BadTargetOption.UNCLEAR, casting.getTarget(), possTargets));
                }
                return true;
            }
            CreatureTargetingSpell cSpell = (CreatureTargetingSpell) spell;
            cSpell.addTarget(targetCreature);
            ctx.getRoom().cast(caster, cSpell);
            return true;
        }
        ctx.sendMsg(new BadTargetSelectedMessage(BadTargetOption.NOTARGET, null));
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
            if (attempter.getVocation().isEmpty() || !(attempter.getVocation().get() instanceof CubeHolder)) {
                ctx.sendMsg(new SpellFizzleMessage(SpellFizzleType.NOT_CASTER, attempter, true));
                if (ctx.getRoom() != null) {
                    ctx.getRoom().sendMessageToAllExcept(
                            new SpellFizzleMessage(SpellFizzleType.NOT_CASTER, attempter, false),
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
