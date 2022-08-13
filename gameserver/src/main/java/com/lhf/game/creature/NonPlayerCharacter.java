package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.lhf.game.battle.Attack;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.intelligence.BasicAI;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.item.interfaces.Weapon;
import com.lhf.game.magic.concrete.DMBlessing;
import com.lhf.messages.out.OutMessage;

public class NonPlayerCharacter extends Creature {
    public class BlessedFist extends Creature.Fist {
        private List<DamageDice> blessedDamage;

        BlessedFist() {
            super("Blessed Fist");

            this.blessedDamage = new ArrayList<>();
            for (DamageFlavor df : DamageFlavor.values()) {
                this.blessedDamage.add(new DamageDice(1, DieType.FOUR, df));
            }
            this.blessedDamage.addAll(this.damages);
        }

        @Override
        public List<DamageDice> getDamages() {
            return this.blessedDamage;
        }

        @Override
        public Attack modifyAttack(Attack attack) {
            Attack superDone = super.modifyAttack(attack);
            superDone.addToHitBonus(30);
            return superDone;
        }
    }

    private final Weapon defaultWeapon = new BlessedFist();
    private ConversationTree convoTree = null;
    public static final String defaultConvoTreeName = "verbal_default";

    public NonPlayerCharacter() {
        super();
    }

    public NonPlayerCharacter(String name, Statblock statblock) {
        super(name, statblock);
        this.setFaction(CreatureFaction.NPC);
    }

    @Override
    public OutMessage equipItem(String itemName, EquipmentSlots slot) {
        OutMessage equipMessage = super.equipItem(itemName, slot);
        if (this.getEquipped(EquipmentSlots.ARMOR) != null) {
            this.removeEffectByName(DMBlessing.name);
        }
        return equipMessage;
    }

    @Override
    public void setFaction(CreatureFaction faction) {
        if (!CreatureFaction.NPC.equals(faction)) {
            this.removeEffectByName(DMBlessing.name);
        }
        super.setFaction(faction);
    }

    @Override
    public Weapon getWeapon() {
        if (this.getEquipped(EquipmentSlots.ARMOR) != null) {
            this.removeEffectByName(DMBlessing.name);
        }
        if (this.hasEffect(DMBlessing.name)) {
            return this.defaultWeapon;
        }
        return super.getWeapon();
    }

    public void setConvoTree(ConversationTree tree) {
        this.convoTree = tree;
    }

    public void setConvoTree(ConversationManager manager, String name) {
        if (name != null && manager != null) {
            try {
                this.convoTree = manager.convoTreeFromFile(name);
            } catch (FileNotFoundException e) {
                System.err.println("Cannot load that convo file");
                e.printStackTrace();
            }
        } else {
            this.convoTree = null;
        }
    }

    public ConversationTree getConvoTree() {
        return this.convoTree;
    }

    @Override
    public void sendMsg(OutMessage msg) {
        if (this.getController() == null) {
            BasicAI lizardbrain = new BasicAI(this);
            this.setController(lizardbrain);
        }
        super.sendMsg(msg);
    }

    @Override
    public void restoreFaction() {
        this.setFaction(CreatureFaction.NPC);
    }
}
