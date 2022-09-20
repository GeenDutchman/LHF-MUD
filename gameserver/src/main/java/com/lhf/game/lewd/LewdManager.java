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
    public interface LewdProduct {
        public void onLewd(Room room, VrijPartij party);
    }

    private List<VrijPartij> vrijPartijen;
    private Map<CommandMessage, String> commands;
    private Room room;
    private LewdProduct lewdProduct;

    public LewdManager(@NotNull Room room) {
        this.room = room;
        this.vrijPartijen = new ArrayList<>();
        this.commands = new EnumMap<>(CommandMessage.class);
        this.commands.put(CommandMessage.LEWD, "\"lewd [creature]\" lewd another person in the room");
        this.lewdProduct = null;
    }

    public LewdManager setLewdProduct(LewdProduct lewdProduct) {
        this.lewdProduct = lewdProduct;
        return this;
    }

    public void clear() {
        for (VrijPartij party : this.vrijPartijen) {
            party.messageParticipants(new LewdOutMessage(LewdOutMessageType.DENIED, null));
        }
        this.vrijPartijen.clear();
    }

    public void removeCreature(Creature creature) {
        for (VrijPartij party : this.vrijPartijen) {
            party.remove(creature);
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
            VrijPartij party = this.vrijPartijen.get(i);
            party.pass(ctx.getCreature());
        }
        return true;
    }

    protected boolean handleJoin(CommandContext ctx, int index) {
        if (index < 0 || index >= this.vrijPartijen.size()) {
            ctx.sendMsg(new LewdOutMessage(LewdOutMessageType.ORGY_UNSUPPORTED, ctx.getCreature()));
            return true;
        }
        VrijPartij party = this.vrijPartijen.get(index);
        if (party.acceptAndCheck(ctx.getCreature())) {
            if (this.lewdProduct != null) {
                this.lewdProduct.onLewd(this.room, party);
            }
            this.vrijPartijen.remove(index);
        }

        return true;
    }

    private boolean handleEmptyJoin(CommandContext ctx, LewdInMessage lim) {
        int bookmark = -1;
        for (int i = 0; i < this.vrijPartijen.size(); i++) {
            VrijPartij party = this.vrijPartijen.get(i);
            if (party.isMember(ctx.getCreature())) {
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
            this.vrijPartijen.add(new VrijPartij(frijPartij));
            index = 0;
        } else {
            for (int i = 0; i < this.vrijPartijen.size(); i++) {
                VrijPartij party = this.vrijPartijen.get(i);
                if (party.match(frijPartij)) {
                    party.merge(frijPartij);
                    index = i;
                    break;
                }
            }
            if (index < 0) {
                index = this.vrijPartijen.size();
                this.vrijPartijen.add(new VrijPartij(frijPartij));
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
