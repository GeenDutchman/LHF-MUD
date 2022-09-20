package com.lhf.game.lewd;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

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

    protected class VrijPartij {
        protected Map<Creature, LewdAnswer> party;
        protected StringJoiner sb;

        public VrijPartij(Map<Creature, LewdAnswer> frijPartij) {
            this.party = frijPartij;
            this.sb = new StringJoiner(" ").setEmptyValue("");
        }

        public Set<Creature> getParticipants(LewdAnswer answer) {
            if (answer == null) {
                answer = LewdAnswer.ACCEPTED;
            }
            HashSet<Creature> doers = new HashSet<>();
            for (Map.Entry<Creature, LewdAnswer> entry : this.party.entrySet()) {
                if (answer.equals(entry.getValue())) {
                    doers.add(entry.getKey());
                }
            }
            return doers;
        }

        public Set<Creature> getParticipants() {
            return this.getParticipants(LewdAnswer.ACCEPTED);
        }

        public VrijPartij addName(String name) {
            this.sb.add(name);
            return this;
        }

        public void messageParticipants(LewdOutMessage lom) {
            if (lom != null) {
                for (Creature participant : party.keySet()) {
                    LewdAnswer answer = party.getOrDefault(participant, LewdAnswer.ASKED);
                    if (!LewdAnswer.DENIED.equals(answer)) {
                        participant.sendMsg(lom);
                    }
                }
            }
        }

        public VrijPartij merge(Map<Creature, LewdAnswer> partij) {
            partij.forEach((key, value) -> this.party.merge(key, value, LewdAnswer::merge));
            return this;
        }

        public boolean match(Map<Creature, LewdAnswer> partij) {
            if (partij == null) {
                return false;
            }
            if (partij.size() == this.party.size() && partij.keySet().containsAll(this.party.keySet())) {
                return true;
            }
            return false;
        }

        public boolean isMember(Creature creature) {
            return this.party.containsKey(creature);
        }

        protected VrijPartij accept(Creature creature) {
            if (this.party.containsKey(creature)) {
                this.party.put(creature, LewdAnswer.ACCEPTED);
            }
            return this;
        }

        public boolean acceptAndCheck(Creature creature) {
            this.accept(creature);
            boolean allDone = true;
            for (Creature participant : party.keySet()) {
                LewdAnswer answer = party.getOrDefault(participant, LewdAnswer.ASKED);
                if (!LewdAnswer.DENIED.equals(answer)) {
                    participant.sendMsg(new LewdOutMessage(LewdOutMessageType.ACCEPTED, creature, party));
                }
                if (LewdAnswer.ASKED.equals(answer)) {
                    allDone = false;
                }
            }
            if (allDone) {
                LewdOutMessage lom = new LewdOutMessage(LewdOutMessageType.DUNNIT, null, party);
                this.messageParticipants(lom);
            }
            return allDone;
        }

        public VrijPartij pass(Creature creature) {
            if (party.containsKey(creature)) {
                party.put(creature, LewdAnswer.DENIED);
                LewdOutMessage lom = new LewdOutMessage(LewdOutMessageType.DENIED, creature, party);
                creature.sendMsg(lom);
                this.messageParticipants(lom);
            }
            return this;
        }

        protected void remove(Creature creature) {
            this.party.remove(creature);
        }
    }

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
