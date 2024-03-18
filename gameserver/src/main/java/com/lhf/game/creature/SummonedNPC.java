package com.lhf.game.creature;

import java.util.EnumSet;

import com.lhf.game.EffectPersistence.Ticker;
import com.lhf.game.creature.INonPlayerCharacter.INonPlayerCharacterBuildInfo.SummonData;

public class SummonedNPC extends SummonedINonPlayerCharacter<NonPlayerCharacter> {

    public SummonedNPC(NonPlayerCharacter NPC, EnumSet<SummonData> summonData, ICreature summoner, Ticker timeLeft) {
        super(NPC, summonData, summoner, timeLeft);
    }

    public static SummonedNPC fromBuildInfo(INPCBuildInfo builder, CreatureFactory factory, ICreature summoner,
            Ticker timeLeft) {
        if (factory == null) {
            factory = new CreatureFactory();
        }
        return factory.summonNPC(builder, summoner, timeLeft);
    }

    @Override
    public void acceptCreatureVisitor(CreatureVisitor visitor) {
        visitor.visit(this);
    }

}
