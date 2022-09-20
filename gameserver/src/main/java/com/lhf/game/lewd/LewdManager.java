package com.lhf.game.lewd;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lhf.game.creature.Creature;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.map.Room;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.in.LewdInMessage;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;
import com.lhf.messages.out.LewdOutMessage;
import com.lhf.messages.out.LewdOutMessage.LewdOutMessageType;
import com.lhf.server.interfaces.NotNull;

public class LewdManager {
    public enum LewdAnswer {
        DENIED, ASKED, ACCEPTED;

        public static LewdAnswer merge(LewdAnswer a, LewdAnswer b) {
            if (a == null && b == null) {
                return ASKED;
            } else if (a == null && b != null) {
                return b;
            } else if (a != null && b == null) {
                return a;
            } else if (a == b) {
                return a;
            } else if (a == DENIED || b == DENIED) {
                return DENIED;
            } else if (a == ACCEPTED || b == ACCEPTED) {
                return ACCEPTED;
            }
            return ASKED;
        }
    }

    private List<Map<Creature, LewdAnswer>> vrijPartijen;
    private Map<CommandMessage, String> commands;
    private Room room;

    public LewdManager(@NotNull Room room) {
        this.room = room;
        this.vrijPartijen = new ArrayList<>();
        this.commands = new EnumMap<>(CommandMessage.class);
        this.commands.put(CommandMessage.LEWD, "\"lewd [creature]\" lewd another person in the room");
    }

    public void clear() {
        for (Map<Creature, LewdAnswer> party : this.vrijPartijen) {
            messageParticipants(party, new LewdOutMessage(LewdOutMessageType.DENIED, null));
        }
        this.vrijPartijen.clear();
    }

    private void messageParticipants(Map<Creature, LewdAnswer> party, LewdOutMessage lom) {
        if (party != null && lom != null) {
            for (Creature participant : party.keySet()) {
                LewdAnswer answer = party.getOrDefault(participant, LewdAnswer.ASKED);
                if (!LewdAnswer.DENIED.equals(answer)) {
                    participant.sendMsg(lom);
                }
            }
        }
    }

    public boolean handleMessage(CommandContext ctx, Command msg) {
        boolean handled = false;
        if (CommandMessage.LEWD.equals(msg.getType())) {
            handled = this.handleLewd(ctx, msg);
        } else if (this.vrijPartijen.size() > 0 && CommandMessage.PASS.equals(msg.getType())) {
            handled = this.handlePass(ctx, msg);
        }
        return handled;
    }

    protected boolean handlePass(CommandContext ctx, Command msg) {
        if (!CommandMessage.PASS.equals(msg.getType())) {
            return false;
        }
        for (int i = 0; i < this.vrijPartijen.size(); i++) {
            Map<Creature, LewdAnswer> party = this.vrijPartijen.get(i);
            if (party.containsKey(ctx.getCreature())) {
                party.put(ctx.getCreature(), LewdAnswer.DENIED);
                LewdOutMessage lom = new LewdOutMessage(LewdOutMessageType.DENIED, ctx.getCreature(), party);
                ctx.sendMsg(lom);
                for (Creature participant : party.keySet()) {
                    if (!LewdAnswer.DENIED.equals(party.getOrDefault(participant, LewdAnswer.ASKED))) {
                        participant.sendMsg(lom);
                    }
                }
            }
        }
        return true;
    }

    protected boolean handleJoin(CommandContext ctx, int index) {
        if (index < 0 || index >= this.vrijPartijen.size()) {
            ctx.sendMsg(new LewdOutMessage(LewdOutMessageType.ORGY_UNSUPPORTED, ctx.getCreature()));
            return true;
        }
        Map<Creature, LewdAnswer> party = this.vrijPartijen.get(index);
        party.put(ctx.getCreature(), LewdAnswer.ACCEPTED);
        boolean allDone = true;
        for (Creature participant : party.keySet()) {
            LewdAnswer answer = party.getOrDefault(participant, LewdAnswer.ASKED);
            if (!LewdAnswer.DENIED.equals(answer)) {
                participant.sendMsg(new LewdOutMessage(LewdOutMessageType.ACCEPTED, ctx.getCreature(), party));
            }
            if (LewdAnswer.ASKED.equals(answer)) {
                allDone = false;
            }
        }
        if (allDone) {
            this.messageParticipants(party, new LewdOutMessage(LewdOutMessageType.DUNNIT, null, party));
            this.vrijPartijen.remove(index);
        }
        return true;
    }

    private boolean handleEmptyJoin(CommandContext ctx, LewdInMessage lim) {
        int bookmark = -1;
        for (int i = 0; i < this.vrijPartijen.size(); i++) {
            Map<Creature, LewdAnswer> party = this.vrijPartijen.get(i);
            if (party.containsKey(ctx.getCreature())) {
                if (bookmark >= 0) {
                    ctx.sendMsg(new LewdOutMessage(LewdOutMessageType.ORGY_UNSUPPORTED, ctx.getCreature()));
                    return true;
                } else {
                    bookmark = i;
                }
            }
        }
        return this.handleJoin(ctx, bookmark);
    }

    private boolean handlePopulatedJoin(CommandContext ctx, LewdInMessage lim) {
        Map<Creature, LewdAnswer> frijPartij = new HashMap<>();
        frijPartij.putIfAbsent(ctx.getCreature(), LewdAnswer.ACCEPTED);
        for (String possName : lim.getPartners()) {
            List<Creature> possibles = this.room.getCreaturesInRoom(possName);
            if (possibles.size() == 0) {
                ctx.sendMsg(new BadTargetSelectedMessage(BadTargetOption.DNE, possName, possibles));
                return true;
            } else if (possibles.size() > 1) {
                ctx.sendMsg(new BadTargetSelectedMessage(BadTargetOption.UNCLEAR, possName, possibles));
                return true;
            }
            frijPartij.putIfAbsent(possibles.get(0), LewdAnswer.ASKED);
        }
        int index = -1;
        if (this.vrijPartijen.size() == 0) {
            this.vrijPartijen.add(frijPartij);
            index = 0;
        } else {
            for (int i = 0; i < this.vrijPartijen.size(); i++) {
                Map<Creature, LewdAnswer> partij = this.vrijPartijen.get(i);
                if (partij.size() == frijPartij.size() && partij.keySet().containsAll(frijPartij.keySet())) {
                    partij.forEach((key, value) -> frijPartij.merge(key, value, LewdAnswer::merge));
                    this.vrijPartijen.set(i, frijPartij);
                    index = i;
                    break;
                }
            }
            if (index < 0) {
                index = this.vrijPartijen.size();
                this.vrijPartijen.add(frijPartij);
            }
        }
        return this.handleJoin(ctx, index);
    }

    protected boolean handleLewd(CommandContext ctx, Command msg) {
        if (msg.getType() != CommandMessage.LEWD) {
            return false;
        }
        if (ctx.getCreature() == null) {
            return false;
        }

        if (ctx.getCreature().isInBattle()) {
            ctx.sendMsg(new LewdOutMessage(LewdOutMessageType.DENIED, null));
            return true;
        }

        if (ctx.getCreature().getEquipped(EquipmentSlots.ARMOR) != null) {
            ctx.sendMsg(new LewdOutMessage(LewdOutMessageType.NOT_NUDE, ctx.getCreature()));
            return true;
        }

        LewdInMessage lewdInMessage = (LewdInMessage) msg;
        if (lewdInMessage.getPartners().size() > 0) {
            return this.handlePopulatedJoin(ctx, lewdInMessage);
        } else {
            return this.handleEmptyJoin(ctx, lewdInMessage);
        }
    }

    public Map<CommandMessage, String> getCommands() {
        EnumMap<CommandMessage, String> toReturn = new EnumMap<>(this.commands);
        if (this.vrijPartijen.size() > 0) {
            toReturn.put(CommandMessage.PASS, "\"pass\" to decline the lewdness");
        }
        return Map.copyOf(toReturn);
    }

}
