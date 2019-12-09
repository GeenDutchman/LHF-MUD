package com.lhf.game.creature;

import com.lhf.game.enums.CreatureType;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.concrete.HealPotion;
import com.lhf.game.item.concrete.LeatherArmor;
import com.lhf.game.item.concrete.Longsword;
import com.lhf.game.item.concrete.Shield;
import com.lhf.game.map.objects.sharedinterfaces.Taggable;
import com.lhf.server.client.user.UserID;

import static com.lhf.game.enums.Attributes.*;

public class Player extends Creature implements Taggable {
    private UserID id;

    public Player(UserID id, String name) {
        this.id = id;
        this.setName(name); //Sets the player name
        this.setCreatureType(CreatureType.PLAYER); //It's a player

        //Set attributes to default values
        this.getAttributes().put(STR, 16);
        this.getAttributes().put(DEX, 12);
        this.getAttributes().put(CON, 14);
        this.getAttributes().put(INT, 10);
        this.getAttributes().put(WIS, 12);
        this.getAttributes().put(CHA, 8);

        //Set modifiers to default values
        this.getModifiers().put(STR, 3);
        this.getModifiers().put(DEX, 1);
        this.getModifiers().put(CON, 2);
        this.getModifiers().put(INT, 0);
        this.getModifiers().put(WIS, 1);
        this.getModifiers().put(CHA, -1);

        //Set default stats
        this.getStats().put(Stats.MAXHP, 12);
        this.getStats().put(Stats.CURRENTHP, 12);
        this.getStats().put(Stats.AC, 11);
        this.getStats().put(Stats.XPWORTH, 500);

        //Set default proficiencies
        this.getProficiencies().add(EquipmentTypes.LIGHTARMOR);
        this.getProficiencies().add(EquipmentTypes.MEDIUMARMOR);
        this.getProficiencies().add(EquipmentTypes.HEAVYARMOR);
        this.getProficiencies().add(EquipmentTypes.SHIELD);
        this.getProficiencies().add(EquipmentTypes.SIMPLEMELEEWEAPONS);
        this.getProficiencies().add(EquipmentTypes.SIMPLERANGEDWEAPONS);
        this.getProficiencies().add(EquipmentTypes.MARTIALWEAPONS);
        this.getProficiencies().add(EquipmentTypes.RANGEDWEAPONS);

        Longsword longsword = new Longsword(true);
        LeatherArmor leatherArmor = new LeatherArmor(true);
        HealPotion potion = new HealPotion(true);
        Shield shield = new Shield(true);

        this.getInventory().addItem(longsword);
        this.getInventory().addItem(leatherArmor);
        this.getInventory().addItem(shield);
        this.getInventory().addItem(potion);
        this.equipItem("Leather Armor", EquipmentSlots.ARMOR);
        this.equipItem("Longsword",EquipmentSlots.WEAPON);
        this.equipItem("Shield",EquipmentSlots.SHIELD);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Player)) {
            return false;
        }
        Player p = (Player) obj;
        return p.getId().equals(getId());
    }

    public UserID getId() {
        return id;
    }

    @Override
    public String getStartTagName() {
        return "<player>";
    }

    @Override
    public String getEndTagName() {
        return "</player>";
    }

    public String getStatus() {
        return "You have " +
                getStats().get(Stats.CURRENTHP) +
                "/" +
                getStats().get(Stats.MAXHP) +
                " HP\r\n";
    }
}
