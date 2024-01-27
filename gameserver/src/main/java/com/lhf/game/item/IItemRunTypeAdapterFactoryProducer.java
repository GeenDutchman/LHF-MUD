package com.lhf.game.item;

import com.google.gson.typeadapters.RuntimeTypeAdapterFactory;
import com.lhf.game.item.concrete.Chest;
import com.lhf.game.item.concrete.Corpse;
import com.lhf.game.item.concrete.Dispenser;
import com.lhf.game.item.concrete.GuardedChest;
import com.lhf.game.item.concrete.HealPotion;
import com.lhf.game.item.concrete.InteractDoor;
import com.lhf.game.item.concrete.Item;
import com.lhf.game.item.concrete.Lever;
import com.lhf.game.item.concrete.LewdBed;
import com.lhf.game.item.concrete.LockKey;
import com.lhf.game.item.concrete.equipment.BossClub;
import com.lhf.game.item.concrete.equipment.CarnivorousArmor;
import com.lhf.game.item.concrete.equipment.ChainMail;
import com.lhf.game.item.concrete.equipment.LeatherArmor;
import com.lhf.game.item.concrete.equipment.Longsword;
import com.lhf.game.item.concrete.equipment.MantleOfDeath;
import com.lhf.game.item.concrete.equipment.ReaperScythe;
import com.lhf.game.item.concrete.equipment.RingOfSeeing;
import com.lhf.game.item.concrete.equipment.RustyDagger;
import com.lhf.game.item.concrete.equipment.Shield;
import com.lhf.game.item.concrete.equipment.Shortsword;
import com.lhf.game.item.concrete.equipment.Whimsystick;

public class IItemRunTypeAdapterFactoryProducer {
    public static RuntimeTypeAdapterFactory<IItem> produce() {
        RuntimeTypeAdapterFactory<IItem> itemTypeAdapter = RuntimeTypeAdapterFactory
                .of(IItem.class, "className", true)
                .registerSubtype(AItem.class, AItem.class.getName())
                .registerSubtype(Item.class, Item.class.getName())
                .registerSubtype(InteractObject.class, InteractObject.class.getName())
                .registerSubtype(Trap.class, Trap.class.getName())
                .registerSubtype(Lever.class, Lever.class.getName())
                .registerSubtype(InteractDoor.class, InteractDoor.class.getName())
                .registerSubtype(Dispenser.class, Dispenser.class.getName())
                .registerSubtype(Chest.class, Chest.class.getName())
                .registerSubtype(GuardedChest.class, GuardedChest.class.getName())
                .registerSubtype(Corpse.class, Corpse.class.getName())
                .registerSubtype(LewdBed.class, LewdBed.class.getName())
                .registerSubtype(Takeable.class, Takeable.class.getName())
                .registerSubtype(Usable.class, Usable.class.getName())
                .registerSubtype(HealPotion.class, HealPotion.class.getName())
                .registerSubtype(LockKey.class, LockKey.class.getName())
                .registerSubtype(Equipable.class, Equipable.class.getName())
                .registerSubtype(EquipableHiddenEffect.class,
                        EquipableHiddenEffect.class.getName())
                .registerSubtype(CarnivorousArmor.class, CarnivorousArmor.class.getName())
                .registerSubtype(ChainMail.class, ChainMail.class.getName())
                .registerSubtype(LeatherArmor.class, LeatherArmor.class.getName())
                .registerSubtype(MantleOfDeath.class, MantleOfDeath.class.getName())
                .registerSubtype(RingOfSeeing.class, RingOfSeeing.class.getName())
                .registerSubtype(Shield.class, Shield.class.getName())
                .registerSubtype(Weapon.class, Weapon.class.getName())
                .registerSubtype(BossClub.class, BossClub.class.getName())
                .registerSubtype(Longsword.class, Longsword.class.getName())
                .registerSubtype(ReaperScythe.class, ReaperScythe.class.getName())
                .registerSubtype(RustyDagger.class, RustyDagger.class.getName())
                .registerSubtype(Shortsword.class, Shortsword.class.getName())
                .registerSubtype(Whimsystick.class, Whimsystick.class.getName())
                .recognizeSubtypes();
        return itemTypeAdapter;
    }
}
