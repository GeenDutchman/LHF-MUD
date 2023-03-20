package com.lhf.game.item.concrete;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lhf.game.creature.Creature;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.InteractObject;
import com.lhf.game.lewd.LewdProduct;
import com.lhf.game.lewd.VrijPartij;
import com.lhf.game.map.Area;
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

    protected SortedMap<Integer, VrijPartij> vrijPartijen;
    protected LewdProduct lewdProduct;

    public static class Builder {
        private Bed.Builder subBuilder;
        private LewdProduct lewdProduct;

        private Builder() {
            this.subBuilder = Bed.Builder.getInstance();
            this.lewdProduct = null;
        }

        public static Builder getInstance() {
            return new Builder();
        }

        public Builder setName(String name) {
            this.subBuilder = subBuilder.setName(name);
            return this;
        }

        public Builder setSleepSeconds(int sleepSecs) {
            this.subBuilder = subBuilder.setSleepSeconds(sleepSecs);
            return this;
        }

        public Builder setCapacity(int cap) {
            this.subBuilder = subBuilder.setCapacity(cap);
            return this;
        }

        public Builder addOccupant(Creature occupant) {
            this.subBuilder = subBuilder.addOccupant(occupant);
            return this;
        }

        public Builder setLewdProduct(LewdProduct product) {
            this.lewdProduct = product;
            return this;
        }

        public LewdBed build(Area room) {
            return new LewdBed(room, this);
        }
    }

    public LewdBed(Area room, Builder builder) {
        super(room, builder.subBuilder);
        this.vrijPartijen = Collections.synchronizedNavigableMap(new TreeMap<>());
        this.lewdProduct = builder.lewdProduct;
    }

    public LewdBed setLewdProduct(LewdProduct lewdProduct) {
        this.lewdProduct = lewdProduct;
        return this;
    }

    public void clear() {
        LewdOutMessage.Builder deniedMessage = LewdOutMessage.getBuilder().setSubType(LewdOutMessageType.DENIED)
                .setBroacast();
        for (VrijPartij party : this.vrijPartijen.values()) {
            party.messageParticipants(deniedMessage.setParty(party.getParty()).Build());
        }
        this.vrijPartijen.clear();
    }

    protected boolean handlePass(CommandContext ctx, Command msg) {
        if (!CommandMessage.PASS.equals(msg.getType())) {
            return false;
        }
        Iterator<VrijPartij> it = this.vrijPartijen.values().iterator();
        while (it.hasNext()) {
            VrijPartij party = it.next();
            if (party.passAndCheck(ctx.getCreature())) {
                it.remove();
            }
        }
        return true;
    }

    protected boolean handleJoin(Creature joiner, int index) {
        LewdOutMessage.Builder lewdOutMessage = LewdOutMessage.getBuilder().setCreature(joiner).setNotBroadcast();
        if (!this.isInBed(joiner)) {
            joiner.sendMsg(lewdOutMessage.setSubType(LewdOutMessageType.NOT_READY).Build());
            return true;
        }

        VrijPartij party = this.vrijPartijen.getOrDefault(index, null);

        if (party == null) {
            joiner.sendMsg(lewdOutMessage.setSubType(LewdOutMessageType.MISSED).Build());
            return true;
        }
        lewdOutMessage.setParty(party.getParty());
        if (party.acceptAndCheck(joiner)) {
            if (this.lewdProduct != null) {
                this.lewdProduct.onLewd(this.room, party);
            }
            this.vrijPartijen.remove(index);
        }

        return true;
    }

    protected boolean handleEmptyJoin(Creature joiner) {
        LewdOutMessage.Builder lewdOutMessage = LewdOutMessage.getBuilder().setCreature(joiner).setNotBroadcast();
        if (!this.isInBed(joiner)) {
            joiner.sendMsg(lewdOutMessage.setSubType(LewdOutMessageType.NOT_READY).Build());
            return true;
        }
        for (int i : this.vrijPartijen.keySet()) {
            VrijPartij party = this.vrijPartijen.get(i);
            if (party.isMember(joiner)) {
                return this.handleJoin(joiner, i);
            }
        }
        joiner.sendMsg(lewdOutMessage.setSubType(LewdOutMessageType.SOLO_UNSUPPORTED).Build());
        return true;
    }

    protected boolean handlePopulatedJoin(Creature joiner, Set<String> possPartners, Set<String> babyNames) {
        LewdOutMessage.Builder lewdOutMessage = LewdOutMessage.getBuilder().setCreature(joiner);
        if (!this.isInBed(joiner)) {
            joiner.sendMsg(lewdOutMessage.setSubType(LewdOutMessageType.NOT_READY).setNotBroadcast().Build());
            return true;
        }
        Set<Creature> invited = new HashSet<>();
        if (possPartners != null) {
            for (String possName : possPartners) {
                List<Creature> possibles = this.getCreaturesLike(possName);
                if (possibles.size() == 0) {
                    joiner.sendMsg(BadTargetSelectedMessage.getBuilder().setBde(BadTargetOption.DNE)
                            .setBadTarget(possName).setPossibleTargets(possibles).Build());
                    return true;
                } else if (possibles.size() > 1) {
                    joiner.sendMsg(BadTargetSelectedMessage.getBuilder().setBde(BadTargetOption.UNCLEAR)
                            .setBadTarget(possName).setPossibleTargets(possibles).Build());
                    return true;
                }
                invited.add(possibles.get(0));
            }
        }

        if (possPartners == null || invited.size() == 0) {
            return this.handleEmptyJoin(joiner);
        }

        if (this.vrijPartijen.size() == 0 && invited.size() > 0) {
            VrijPartij party = new VrijPartij(joiner, invited).addNames(babyNames);
            this.vrijPartijen.put(party.hashCode(), party);
            party.propose();
            return this.handleJoin(joiner, party.hashCode());
        } else {
            invited.add(joiner);
            for (int i : this.vrijPartijen.keySet()) {
                VrijPartij party = this.vrijPartijen.get(i);
                if (party.match(invited)) {
                    return this.handleJoin(joiner, i);
                }
            }

            VrijPartij party = new VrijPartij(joiner, invited).addNames(babyNames);
            this.vrijPartijen.put(party.hashCode(), party);
            party.propose();
            return this.handleJoin(joiner, party.hashCode());
        }
    }

    protected boolean handleLewd(CommandContext ctx, Command msg) {
        LewdOutMessage.Builder lewdOutMessage = LewdOutMessage.getBuilder();
        if (msg.getType() != CommandMessage.LEWD) {
            return false;
        }
        if (ctx.getCreature() == null) {
            return false;
        }

        if (!this.isInBed(ctx.getCreature())) {
            ctx.sendMsg(lewdOutMessage.setSubType(LewdOutMessageType.NOT_READY).setNotBroadcast().Build());
            return true;
        }
        lewdOutMessage.setCreature(ctx.getCreature());

        if (ctx.getCreature().isInBattle()) {
            ctx.sendMsg(lewdOutMessage.setSubType(LewdOutMessageType.NOT_READY).setNotBroadcast().Build());
            return true;
        }

        if (ctx.getCreature().getEquipped(EquipmentSlots.ARMOR) != null) {
            ctx.sendMsg(lewdOutMessage.setSubType(LewdOutMessageType.NOT_READY).setNotBroadcast().Build());
            return true;
        }

        LewdInMessage lewdInMessage = (LewdInMessage) msg;
        if (lewdInMessage.getPartners().size() > 0) {
            return this.handlePopulatedJoin(ctx.getCreature(), lewdInMessage.getPartners(), lewdInMessage.getNames());
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
    public boolean removeCreature(Creature doneSleeping) {
        if (super.removeCreature(doneSleeping)) {
            for (VrijPartij party : this.vrijPartijen.values()) {
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
            bedCommands.put(CommandMessage.PASS, "\"pass\" to decline all the lewdness");
        }
        return Map.copyOf(bedCommands);
    }

    @Override
    protected OutMessage bedAction(Creature creature, InteractObject triggerObject, Map<String, Object> args) {
        InteractOutMessage.Builder interactOutMessage = InteractOutMessage.getBuilder().setTaggable(triggerObject);
        if (creature == null) {
            return interactOutMessage.setSubType(InteractOutMessageType.CANNOT).Build();
        }
        if (this.getOccupancy() >= this.getCapacity()) {
            return interactOutMessage.setSubType(InteractOutMessageType.CANNOT).setDescription("The bed is full!")
                    .Build();
        }

        if (creature.getEquipped(EquipmentSlots.ARMOR) != null) {
            return LewdOutMessage.getBuilder().setSubType(LewdOutMessageType.NOT_READY).setCreature(creature).Build();
        }

        if (this.addCreature(creature)) {
            return interactOutMessage.setSubType(InteractOutMessageType.PERFORMED)
                    .setDescription("You are now in the bed!").Build();
        }
        return interactOutMessage.setSubType(InteractOutMessageType.ERROR).setDescription("You are already in the bed!")
                .Build();
    }

}
