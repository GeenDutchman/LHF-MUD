package com.lhf.game.creature;

import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.concrete.HealPotion;
import com.lhf.game.item.concrete.equipment.LeatherArmor;
import com.lhf.game.item.concrete.equipment.Longsword;
import com.lhf.game.item.concrete.equipment.Shield;
import com.lhf.server.client.user.User;
import com.lhf.server.client.user.UserID;

import com.lhf.game.creature.statblock.AttributeBlock;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.vocation.Fighter;
import com.lhf.game.creature.vocation.Vocation;

public class Player extends Creature {
    private User user;

    public Player(User user) {
        this.user = user;
        this.user.setSuccessor(this);
        this.setController(this.user.getClient());
        this.setName(user.getUsername()); // Sets the player name
        this.setFaction(CreatureFaction.PLAYER); // It's a player
        this.setVocation(new Fighter());

        // Set attributes to default values
        AttributeBlock attrBlock = new AttributeBlock(16, 12, 14, 10, 12, 8);
        this.setAttributes(attrBlock);

        // Set default stats
        this.getStats().put(Stats.MAXHP, 12);
        this.getStats().put(Stats.CURRENTHP, 12);
        this.getStats().put(Stats.AC, 11);
        this.getStats().put(Stats.XPWORTH, 500);

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

    public Player(User user, Statblock statblock, Vocation vocation) {
        super(user.getUsername(), statblock);
        this.user = user;
        this.user = user;
        this.user.setSuccessor(this);
        this.setController(this.user.getClient());
        this.setVocation(vocation);
        this.setFaction(CreatureFaction.PLAYER);

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

}
