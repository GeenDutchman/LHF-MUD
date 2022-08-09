package com.lhf.game.magic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import com.lhf.game.creature.Creature;
import com.lhf.game.magic.concrete.ShockBolt;
import com.lhf.game.magic.concrete.Thaumaturgy;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.MessageHandler;
import com.lhf.messages.in.CastMessage;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.CreatureAffectedMessage;
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
    private Map<String, SpellEntry> entries;
    private MessageHandler successor;
    private HashMap<CommandMessage, String> cmds;

    public ThirdPower(MessageHandler successor) {
        this.successor = successor;
        this.entries = new HashMap<>();
        SpellEntry shockBolt = new ShockBolt();
        this.entries.put(shockBolt.getInvocation(), shockBolt);
        SpellEntry thaumaturgy = new Thaumaturgy();
        this.entries.put(thaumaturgy.getInvocation(), thaumaturgy);
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

    private boolean handleCast(CommandContext ctx, Command msg) {
        CastMessage casting = (CastMessage) msg;
        Creature caster = ctx.getCreature();
        SpellEntry entry = this.entries.get(casting.getInvocation());
        if (entry == null) {
            ctx.sendMsg(new SpellFizzleMessage(SpellFizzleType.MISPRONOUNCE, caster, true));
            if (ctx.getRoom() != null) {
                ctx.getRoom().sendMessageToAll(new SpellFizzleMessage(SpellFizzleType.MISPRONOUNCE, caster, false));
            }
            return true;
        }
        if (entry instanceof CreatureTargetingSpellEntry) {
            CreatureTargetingSpell spell = new CreatureTargetingSpell((CreatureTargetingSpellEntry) entry);
            spell.setCaster(caster);
            // TODO: duration should be a thing
            if (ctx.getRoom() == null) {
                ctx.sendMsg(new SpellFizzleMessage(SpellFizzleType.OTHER, caster, true));
                return true;
            }
            List<Creature> possTargets = new ArrayList<>();
            for (String targetName : casting.getTargets()) {
                List<Creature> found = ctx.getRoom().getCreaturesInRoom(targetName);
                if (found.size() > 1 || found.size() == 0) {
                    ctx.sendMsg(new BadTargetSelectedMessage(
                            found.size() > 1 ? BadTargetOption.UNCLEAR : BadTargetOption.NOTARGET, targetName, found));
                    return true;
                }
                possTargets.add(found.get(0));
            }
            ctx.getRoom().sendMessageToAll(entry.Cast(caster, casting.getLevel(), possTargets));
            for (Creature target : possTargets) {
                CreatureAffectedMessage cam = target.applyAffects(spell);
                if (ctx.getBattleManager() != null) {
                    ctx.getBattleManager().sendMessageToAllParticipants(cam);
                } else if (ctx.getRoom() != null) {
                    ctx.getRoom().sendMessageToAll(cam);
                } else {
                    caster.sendMsg(cam);
                    target.sendMsg(cam);
                }
            }
            return true;
        } // TODO: other cases

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
