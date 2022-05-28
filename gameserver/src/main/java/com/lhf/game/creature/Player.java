package com.lhf.game.creature;

import com.lhf.game.enums.CreatureType;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.concrete.HealPotion;
import com.lhf.game.item.concrete.equipment.LeatherArmor;
import com.lhf.game.item.concrete.equipment.Longsword;
import com.lhf.game.item.concrete.equipment.Shield;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;

import com.lhf.game.creature.statblock.AttributeBlock;

public class Player extends Creature {
    private User user;

    public Player(User user) {
        this.user = user;
        this.user.setSuccessor(this);
        this.setController(this.user.getClient());
        this.setName(user.getUsername()); // Sets the player name
        this.setCreatureType(CreatureType.PLAYER); // It's a player

        // Set attributes to default values
        AttributeBlock attrBlock = new AttributeBlock(16, 12, 14, 10, 12, 8);
        this.setAttributes(attrBlock);

        // Set default stats
        this.getStats().put(Stats.MAXHP, 12);
        this.getStats().put(Stats.CURRENTHP, 12);
        this.getStats().put(Stats.AC, 11);
        this.getStats().put(Stats.XPWORTH, 500);

        // Set default proficiencies
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
        this.equipItem("Longsword", EquipmentSlots.WEAPON);
        this.equipItem("Shield", EquipmentSlots.SHIELD);
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
        return this.user.getUserID();
    }

    public User getUser() {
        return this.user;
    }

    @Override
    public String getStartTagName() {
        return "<player>";
    }

    @Override
    public String getEndTagName() {
        return "</player>";
    }

}
