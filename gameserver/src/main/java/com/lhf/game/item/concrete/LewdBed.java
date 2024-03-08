package com.lhf.game.item.concrete;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.logging.Level;

import com.google.gson.JsonParseException;
import com.lhf.game.creature.ICreature;
import com.lhf.game.creature.ICreatureBuildInfo;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.lewd.LewdProduct;
import com.lhf.game.lewd.VrijPartij;
import com.lhf.game.map.Area;
import com.lhf.messages.Command;
import com.lhf.messages.CommandChainHandler;
import com.lhf.messages.CommandContext;
import com.lhf.messages.CommandContext.Reply;
import com.lhf.messages.events.BadMessageEvent;
import com.lhf.messages.events.BadMessageEvent.BadMessageType;
import com.lhf.messages.events.BadTargetSelectedEvent;
import com.lhf.messages.events.BadTargetSelectedEvent.BadTargetOption;
import com.lhf.messages.events.LewdEvent;
import com.lhf.messages.events.LewdEvent.LewdOutMessageType;
import com.lhf.messages.in.AMessageType;
import com.lhf.messages.in.LewdInMessage;

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

        public Builder addOccupant(ICreature occupant) {
            this.subBuilder = subBuilder.addOccupant(occupant);
            return this;
        }

        public Builder setLewdProduct(LewdProduct product) {
            this.lewdProduct = product;
            return this;
        }

        public LewdBed build(Area room) {
            return new LewdBed(this, room);
        }
    }

    public LewdBed(Builder builder, Area room) {
        super(builder.subBuilder, room);
        this.vrijPartijen = Collections.synchronizedNavigableMap(new TreeMap<>());
        this.lewdProduct = builder.lewdProduct;
        this.commands.put(AMessageType.LEWD, new LewdHandler());
        this.commands.put(AMessageType.PASS, new PassHandler());
    }

    public LewdBed setLewdProduct(LewdProduct lewdProduct) {
        this.lewdProduct = lewdProduct;
        return this;
    }

    public void clear() {
        LewdEvent.Builder deniedMessage = LewdEvent.getBuilder().setSubType(LewdOutMessageType.DENIED)
                .setBroacast();
        for (VrijPartij party : this.vrijPartijen.values()) {
            party.messageParticipants(deniedMessage.setParty(party.getParty()).Build());
        }
        this.vrijPartijen.clear();
    }

    protected class PassHandler implements BedCommandHandler {
        private final static String helpString = "\"pass\" to decline all the lewdness";

        @Override
        public AMessageType getHandleType() {
            return AMessageType.LEWD;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(PassHandler.helpString);
        }

        @Override
        public boolean isEnabled(CommandContext ctx) {
            return BedCommandHandler.super.isEnabled(ctx) && LewdBed.this.vrijPartijen.size() > 0;
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            if (cmd == null || !AMessageType.PASS.equals(cmd.getType())) {
                return ctx.failhandle();
            }
            Iterator<VrijPartij> it = LewdBed.this.vrijPartijen.values().iterator();
            while (it.hasNext()) {
                VrijPartij party = it.next();
                if (party.pass(ctx.getCreature()).check()) {
                    it.remove();
                }
            }
            return ctx.handled();
        }

        @Override
        public CommandChainHandler getChainHandler(CommandContext ctx) {
            return LewdBed.this;
        }

    }

    protected class LewdHandler implements BedCommandHandler {
        private final static String helpString = "\"lewd [creature]\" lewd another person in the bed";

        @Override
        public AMessageType getHandleType() {
            return AMessageType.LEWD;
        }

        @Override
        public Optional<String> getHelp(CommandContext ctx) {
            return Optional.of(helpString);
        }

        @Override
        public Reply handleCommand(CommandContext ctx, Command cmd) {
            LewdEvent.Builder lewdOutMessage = LewdEvent.getBuilder();
            if (cmd == null || cmd.getType() != this.getHandleType()) {
                return ctx.failhandle();
            }
            if (ctx.getCreature() == null) {
                return ctx.failhandle();
            }

            if (!LewdBed.this.isInBed(ctx.getCreature())) {
                ctx.receive(lewdOutMessage.setSubType(LewdOutMessageType.NOT_READY).setNotBroadcast().Build());
                return ctx.failhandle();
            }
            lewdOutMessage.setCreature(ctx.getCreature());

            if (ctx.getCreature().isInBattle()) {
                ctx.receive(lewdOutMessage.setSubType(LewdOutMessageType.NOT_READY).setNotBroadcast().Build());
                return ctx.failhandle();
            }

            if (ctx.getCreature().getEquipped(EquipmentSlots.ARMOR) != null) {
                LewdBed.this.logger.log(Level.WARNING,
                        String.format("%s is still wearing armor, but wants to lewd!", ctx.getCreature().getName()));
                ctx.receive(lewdOutMessage.setSubType(LewdOutMessageType.NOT_READY).setNotBroadcast().Build());
                return ctx.failhandle();
            }

            final LewdInMessage lewdInMessage = new LewdInMessage(cmd);

            if (lewdInMessage.getPartners().size() > 0) {
                try {
                    return LewdBed.this.handlePopulatedJoin(ctx.getCreature(), lewdInMessage.getPartners(),
                            lewdInMessage.getNames(), lewdInMessage.getBuildInfos())
                                    ? ctx.handled()
                                    : ctx.failhandle();
                } catch (JsonParseException e) {
                    LewdBed.this.logger.log(Level.WARNING, e.toString());
                    ctx.receive(BadMessageEvent.getBuilder().setBadMessageType(BadMessageType.OTHER).setNotBroadcast()
                            .setNotBroadcast().setCommand(cmd));
                    return ctx.failhandle();
                }
            } else {
                return LewdBed.this.handleEmptyJoin(ctx.getCreature()) ? ctx.handled() : ctx.failhandle();
            }
        }

        @Override
        public CommandChainHandler getChainHandler(CommandContext ctx) {
            return LewdBed.this;
        }

    }

    protected boolean handleJoin(ICreature joiner, int index) {
        LewdEvent.Builder lewdOutMessage = LewdEvent.getBuilder().setCreature(joiner).setNotBroadcast();
        if (!this.isInBed(joiner)) {
            ICreature.eventAccepter.accept(joiner, lewdOutMessage.setSubType(LewdOutMessageType.NOT_READY).Build());
            return true;
        }

        VrijPartij party = this.vrijPartijen.getOrDefault(index, null);

        if (party == null) {
            ICreature.eventAccepter.accept(joiner, lewdOutMessage.setSubType(LewdOutMessageType.MISSED).Build());
            return true;
        }
        lewdOutMessage.setParty(party.getParty());
        if (party.accept(joiner).check()) {
            if (this.lewdProduct != null) {
                final Consumer<Area> onLewd = this.lewdProduct.onLewdAreaChanges(party);
                if (onLewd != null) {
                    onLewd.accept(area);
                }
            }
            this.vrijPartijen.remove(index);
        }

        return true;
    }

    protected boolean handleEmptyJoin(ICreature joiner) {
        LewdEvent.Builder lewdOutMessage = LewdEvent.getBuilder().setCreature(joiner).setNotBroadcast();
        if (!this.isInBed(joiner)) {
            ICreature.eventAccepter.accept(joiner, lewdOutMessage.setSubType(LewdOutMessageType.NOT_READY).Build());
            return true;
        }
        for (int i : this.vrijPartijen.keySet()) {
            VrijPartij party = this.vrijPartijen.get(i);
            if (party.isMember(joiner)) {
                return this.handleJoin(joiner, i);
            }
        }
        this.logger.log(Level.WARNING, String.format("%s wanted to do solo play", joiner.getName()));
        ICreature.eventAccepter.accept(joiner, lewdOutMessage.setSubType(LewdOutMessageType.SOLO_UNSUPPORTED).Build());
        return true;
    }

    protected boolean handlePopulatedJoin(ICreature joiner, Set<String> possPartners, Set<String> babyNames,
            Collection<ICreatureBuildInfo> buildInfos) {
        LewdEvent.Builder lewdOutMessage = LewdEvent.getBuilder().setCreature(joiner);
        if (!this.isInBed(joiner)) {
            ICreature.eventAccepter.accept(joiner,
                    lewdOutMessage.setSubType(LewdOutMessageType.NOT_READY).setNotBroadcast().Build());
            return true;
        }
        Set<ICreature> invited = new HashSet<>();
        if (possPartners != null) {
            for (String possName : possPartners) {
                List<ICreature> possibles = this.getCreaturesLike(possName);
                if (possibles.size() == 0) {
                    this.logger.log(Level.WARNING,
                            String.format("%s wanted to lewd someone named %s, but DNE", joiner.getName(), possName));
                    ICreature.eventAccepter.accept(joiner,
                            BadTargetSelectedEvent.getBuilder().setBde(BadTargetOption.DNE)
                                    .setBadTarget(possName).setPossibleTargets(possibles).Build());
                    return true;
                } else if (possibles.size() > 1) {
                    this.logger.log(Level.WARNING,
                            String.format("%s wanted to lewd someone named %s, but UNCLEAR", joiner.getName(),
                                    possName));
                    ICreature.eventAccepter.accept(joiner,
                            BadTargetSelectedEvent.getBuilder().setBde(BadTargetOption.UNCLEAR)
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
            VrijPartij party = new VrijPartij(joiner, invited).addNames(babyNames).addBuildInfos(buildInfos);
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

            VrijPartij party = new VrijPartij(joiner, invited).addNames(babyNames).addBuildInfos(buildInfos);
            this.vrijPartijen.put(party.hashCode(), party);
            party.propose();
            return this.handleJoin(joiner, party.hashCode());
        }
    }

    @Override
    public boolean removeCreature(ICreature doneSleeping) {
        if (super.removeCreature(doneSleeping)) {
            for (VrijPartij party : this.vrijPartijen.values()) {
                party.remove(doneSleeping);
            }
            return true;
        }
        return false;
    }

    @Override
    public void doAction(CommandContext ctx) {
        super.doAction(ctx);
        if (ctx == null) {
            return;
        }
        final ICreature creature = ctx.getCreature();
        if (creature == null) {
            return;
        }
        if (creature.getEquipped(EquipmentSlots.ARMOR) != null) {
            this.logger.log(Level.WARNING, () -> String.format("%s is still wearing armor!", creature.getName()));
            ICreature.eventAccepter.accept(creature,
                    LewdEvent.getBuilder().setSubType(LewdOutMessageType.NOT_READY).setCreature(creature).Build());
        }
    }

}
