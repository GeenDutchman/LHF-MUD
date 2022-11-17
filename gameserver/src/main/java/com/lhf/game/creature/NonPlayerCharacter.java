package com.lhf.game.creature;

import java.io.FileNotFoundException;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectPersistence.TickType;
import com.lhf.game.EffectResistance;
import com.lhf.game.creature.conversation.ConversationManager;
import com.lhf.game.creature.conversation.ConversationTree;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.creature.vocation.Vocation;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.EquipmentSlots;
import com.lhf.game.enums.EquipmentTypes;
import com.lhf.game.enums.Stats;
import com.lhf.game.item.Weapon;
import com.lhf.game.item.interfaces.WeaponSubtype;
import com.lhf.game.magic.concrete.DMBlessing;

public class NonPlayerCharacter extends Creature {
    public static class BlessedFist extends Weapon {
        private final static CreatureEffectSource source = new CreatureEffectSource("Blessed Punch",
                new EffectPersistence(TickType.INSTANT),
                new EffectResistance(EnumSet.allOf(Attributes.class), Stats.AC), "A blessed fist punches harder.",
                false);

        BlessedFist(NonPlayerCharacter owner) {
            super("Blessed Fist", false, Set.of(BlessedFist.source), DamageFlavor.MAGICAL_BLUDGEONING,
                    WeaponSubtype.CREATUREPART);
            if (BlessedFist.source.getDamages().size() == 0) {
                for (DamageFlavor df : DamageFlavor.values()) {
                    BlessedFist.source.addDamage(new DamageDice(1, DieType.FOUR, df));
                }
            }

            this.types = List.of(EquipmentTypes.SIMPLEMELEEWEAPONS, EquipmentTypes.MONSTERPART);
            this.slots = List.of(EquipmentSlots.WEAPON);
            this.descriptionString = "This is a " + getName() + " attached to a " + owner.getName()
                    + "\n";
        }
    }

    private final Weapon defaultWeapon = new BlessedFist(this);
    private ConversationTree convoTree = null;
    public static final String defaultConvoTreeName = "verbal_default";

    public NonPlayerCharacter() {
        super();
    }

    public NonPlayerCharacter(String name, Statblock statblock) {
        super(name, statblock, CreatureFaction.NPC);
    }

    public NonPlayerCharacter(String name, Vocation vocation) {
        super(name, vocation, CreatureFaction.NPC);
    }

    @Override
    public boolean equipItem(String itemName, EquipmentSlots slot) {
        boolean did = super.equipItem(itemName, slot);
        if (this.getEquipped(EquipmentSlots.ARMOR) != null) {
            this.removeEffectByName(DMBlessing.name);
        }
        return did;
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
    public void restoreFaction() {
        this.setFaction(CreatureFaction.NPC);
    }
}
