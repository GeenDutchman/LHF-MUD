package com.lhf.game.creature;

import java.util.EnumSet;

import com.lhf.game.EffectPersistence.Ticker;
import com.lhf.game.creature.INonPlayerCharacter.INonPlayerCharacterBuildInfo.SummonData;

public class SummonedMonster extends SummonedINonPlayerCharacter<Monster> implements IMonster {

    public SummonedMonster(Monster monster, EnumSet<SummonData> summonData, ICreature summoner, Ticker timeLeft) {
        super(monster, summonData, summoner, timeLeft);
    }

    public static SummonedMonster fromBuildInfo(MonsterBuildInfo builder, CreatureFactory factory, ICreature summoner,
            Ticker timeLeft) {
        if (factory == null) {
            factory = new CreatureFactory();
        }
        return factory.summonMonster(builder, summoner, timeLeft);
    }

    @Override
    public long getMonsterNumber() {
        return this.wrapped.getMonsterNumber();
    }

    @Override
    public void acceptCreatureVisitor(CreatureVisitor visitor) {
        visitor.visit(this);
    }

}
