package com.lhf.game.item.concrete;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.lhf.game.creature.Creature;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.InteractObject;
import com.lhf.game.lewd.VrijPartij;
import com.lhf.game.map.Room;
import com.lhf.messages.Command;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandMessage;
import com.lhf.messages.in.LewdInMessage;
import com.lhf.messages.out.BadTargetSelectedMessage;
import com.lhf.messages.out.BadTargetSelectedMessage.BadTargetOption;
import com.lhf.messages.out.InteractOutMessage;
import com.lhf.messages.out.InteractOutMessage.InteractOutMessageType;
import com.lhf.messages.out.LewdOutMessage;
import com.lhf.messages.out.LewdOutMessage.LewdOutMessageType;
import com.lhf.messages.out.OutMessage;

public class LewdBed extends Bed {

    public interface LewdProduct {
        public void onLewd(Room room, VrijPartij party);
    }

    protected List<VrijPartij> vrijPartijen;
    protected LewdProduct lewdProduct;

    public LewdBed(Room room, int capacity, int sleepSeconds) {
        super(room, Integer.max(capacity, 2), sleepSeconds);
        this.vrijPartijen = new ArrayList<>();
        this.lewdProduct = null;
    }

    public LewdBed setLewdProduct(LewdProduct lewdProduct) {
        this.lewdProduct = lewdProduct;
        return this;
    }

    public void clear() {
        for (VrijPartij party : this.vrijPartijen) {
            party.messageParticipants(new LewdOutMessage(LewdOutMessageType.DENIED, null));
        }
        this.vrijPartijen.clear();
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

    protected boolean handleJoin(Creature joiner, int index) {
        if (!this.isInBed(joiner)) {
            joiner.sendMsg(new LewdOutMessage(LewdOutMessageType.NOT_READY, joiner));
            return true;
        }
        if (index < 0 || index >= this.vrijPartijen.size()) {
            joiner.sendMsg(new LewdOutMessage(LewdOutMessageType.ORGY_UNSUPPORTED, joiner));
            return true;
        }
        VrijPartij party = this.vrijPartijen.get(index);
        if (party.acceptAndCheck(joiner)) {
            if (this.lewdProduct != null) {
                this.lewdProduct.onLewd(this.room, party);
            }
            this.vrijPartijen.remove(index);
        }

        return true;
    }

    protected boolean handleEmptyJoin(Creature joiner) {
        if (!this.isInBed(joiner)) {
            joiner.sendMsg(new LewdOutMessage(LewdOutMessageType.NOT_READY, joiner));
            return true;
        }
        for (int i = 0; i < this.vrijPartijen.size(); i++) {
            VrijPartij party = this.vrijPartijen.get(i);
            if (party.isMember(joiner)) {
                return this.handleJoin(joiner, i);
            }
        }
        joiner.sendMsg(new LewdOutMessage(LewdOutMessageType.SOLO_UNSUPPORTED, joiner));
        return true;
    }

    protected boolean handlePopulatedJoin(Creature joiner, Set<String> possPartners) {
        if (!this.isInBed(joiner)) {
            joiner.sendMsg(new LewdOutMessage(LewdOutMessageType.NOT_READY, joiner));
            return true;
        }
        Set<Creature> invited = new HashSet<>();
        if (possPartners != null) {
            for (String possName : possPartners) {
                List<Creature> possibles = this.getCreaturesInBed(possName);
                if (possibles.size() == 0) {
                    joiner.sendMsg(new BadTargetSelectedMessage(BadTargetOption.DNE, possName, possibles));
                    return true;
                } else if (possibles.size() > 1) {
                    joiner.sendMsg(new BadTargetSelectedMessage(BadTargetOption.UNCLEAR, possName, possibles));
                    return true;
                }
                invited.add(possibles.get(0));
            }
        }

        if (possPartners == null || invited.size() == 0) {
            return this.handleEmptyJoin(joiner);
        }

        if (this.vrijPartijen.size() == 0 && invited.size() > 0) {
            VrijPartij party = new VrijPartij(joiner, invited);
            this.vrijPartijen.add(party);
            party.propose(joiner);
            return this.handleJoin(joiner, 0);
        } else {
            invited.add(joiner);
            for (int i = 0; i < this.vrijPartijen.size(); i++) {
                VrijPartij party = this.vrijPartijen.get(i);
                if (party.match(invited)) {
                    return this.handleJoin(joiner, i);
                }
            }

            int index = this.vrijPartijen.size();
            VrijPartij party = new VrijPartij(joiner, invited);
            this.vrijPartijen.add(party);
            party.propose(joiner);
            return this.handleJoin(joiner, index);
        }
    }

    protected boolean handleLewd(CommandContext ctx, Command msg) {
        if (msg.getType() != CommandMessage.LEWD) {
            return false;
        }
        if (ctx.getCreature() == null) {
            return false;
        }

        if (!this.isInBed(ctx.getCreature())) {
            ctx.sendMsg(new LewdOutMessage(LewdOutMessageType.NOT_READY, null));
            return true;
        }

        if (ctx.getCreature().isInBattle()) {
            ctx.sendMsg(new LewdOutMessage(LewdOutMessageType.NOT_READY, null));
            return true;
        }

        if (ctx.getCreature().getEquipped(EquipmentSlots.ARMOR) != null) {
            ctx.sendMsg(new LewdOutMessage(LewdOutMessageType.NOT_READY, ctx.getCreature()));
            return true;
        }

        LewdInMessage lewdInMessage = (LewdInMessage) msg;
        if (lewdInMessage.getPartners().size() > 0) {
            return this.handlePopulatedJoin(ctx.getCreature(), lewdInMessage.getPartners());
        } else {
            return this.handleEmptyJoin(ctx.getCreature());
        }
    }

    @Override
    public boolean handleMessage(CommandContext ctx, Command msg) {
        boolean handled = super.handleMessage(ctx, msg);
        if (handled) {
            return handled;
        }
        if (CommandMessage.LEWD.equals(msg.getType())) {
            handled = this.handleLewd(ctx, msg);
        } else if (this.vrijPartijen.size() > 0 && CommandMessage.PASS.equals(msg.getType())) {
            handled = this.handlePass(ctx, msg);
        }
        return handled;
    }

    @Override
    public boolean remove(Creature doneSleeping) {
        if (super.remove(doneSleeping)) {
            for (VrijPartij party : this.vrijPartijen) {
                party.remove(doneSleeping);
            }
            return true;
        }
        return false;
    }

    @Override
    public Map<CommandMessage, String> getCommands() {
        Map<CommandMessage, String> bedCommands = super.getCommands();
        bedCommands.put(CommandMessage.LEWD, "\"lewd [creature]\" lewd another person in the bed");
        if (this.vrijPartijen.size() > 0) {
            bedCommands.put(CommandMessage.PASS, "\"pass\" to decline the lewdness");
        }
        return Map.copyOf(bedCommands);
    }

    @Override
    protected OutMessage bedAction(Creature creature, InteractObject triggerObject, Map<String, Object> args) {
        if (creature == null) {
            return new InteractOutMessage(triggerObject, InteractOutMessageType.CANNOT);
        }
        if (this.getOccupancy() >= this.getCapacity()) {
            return new InteractOutMessage(triggerObject, InteractOutMessageType.CANNOT, "The bed is full!");
        }

        if (creature.getEquipped(EquipmentSlots.ARMOR) != null) {
            return new LewdOutMessage(LewdOutMessageType.NOT_READY, creature);
        }

        if (this.addCreature(creature)) {
            return new InteractOutMessage(triggerObject, "You are now in the bed!");
        }
        return new InteractOutMessage(triggerObject, InteractOutMessageType.ERROR, "You are already in the bed!");
    }

}
