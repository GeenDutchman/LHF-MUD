package com.lhf.game.magic;

import java.util.List;

import com.lhf.game.creature.Creature;
import com.lhf.game.creature.Player;
import com.lhf.game.magic.concrete.ShockBolt;
import com.lhf.game.magic.concrete.Thaumaturgy;
import com.lhf.game.magic.interfaces.CreatureAffector;
import com.lhf.game.magic.interfaces.RoomAffector;
import com.lhf.game.map.Dungeon;
import com.lhf.game.map.Room;
import com.lhf.server.client.user.UserID;

public class ThirdPower {
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

    private Dungeon dungeon;
    private ShockBolt shockBolt;
    private Thaumaturgy thaumaturgy;

    public ThirdPower(Dungeon dungeon) {
        this.dungeon = dungeon;
        this.shockBolt = new ShockBolt();
        this.thaumaturgy = new Thaumaturgy();
    }

    public ISpell onCast(CubeHolder cubeHolder, String invocation) {
        if (invocation.equals(shockBolt.getInvocation())) {
            return this.shockBolt.setCaster(cubeHolder);
        } else if (invocation.equals(thaumaturgy.getInvocation())) {
            return this.thaumaturgy.setCaster(cubeHolder);
        }
        return this.thaumaturgy.setCaster(cubeHolder);
    }

    public String onCastCommand(UserID userID, String invocation, String target) {
        Room pRoom = this.dungeon.getPlayerRoom(userID);
        Player player = pRoom.getPlayerInRoom(userID);
        ISpell spell = this.onCast(player, invocation);
        if (spell instanceof RoomAffector) {
            RoomAffector roomSpell = (RoomAffector) spell;
            return pRoom.cast(player, roomSpell.setRoom(pRoom));
        }
        if (spell instanceof CreatureAffector && target.length() > 0) {
            Creature targeCreature = null;
            List<Creature> possTargets = pRoom.getCreaturesInRoom(target);
            if (possTargets.size() == 1) {
                targeCreature = possTargets.get(0);
            }
            if (targeCreature == null) {
                StringBuilder sb = new StringBuilder();
                sb.append("You cannot attack '").append(target).append("' ");
                if (possTargets.size() == 0) {
                    sb.append("because it does not exist.");
                } else {
                    sb.append("because it could be any of these:\n");
                    for (Creature c : possTargets) {
                        sb.append(c.getColorTaggedName()).append(" ");
                    }
                }
                sb.append("\r\n");
                return sb.toString();
            }
            CreatureAffector cSpell = (CreatureAffector) spell;
            cSpell.addTarget(targeCreature);
            return pRoom.cast(player, cSpell);
        }
        return "Bad cast?";
    }

}
