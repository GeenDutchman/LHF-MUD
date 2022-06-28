package com.lhf.game.creature;

import com.lhf.game.battle.Attack;
import com.lhf.game.battle.BattleAI;
import com.lhf.game.creature.statblock.Statblock;
import com.lhf.game.enums.CreatureFaction;
import com.lhf.game.enums.MonsterAI;
import com.lhf.game.enums.Stats;
import com.lhf.messages.out.AttackDamageMessage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;

public class Monster extends NonPlayerCharacter implements BattleAI {
    private boolean activelyHostile;
    private static long serialNumber = 0;
    private long monsterNumber;

    private MonsterAI aiType;
    private Creature lastAttacker;
    private int lastDamage = 0;

    public Monster() {
        super();
        this.activelyHostile = false;
        this.setSerialNumber();
        this.aiType = MonsterAI.RANDOM;
        this.setFaction(CreatureFaction.MONSTER);
    }

    public Monster(String name, Statblock statblock) {
        super(NameGenerator.GenerateSuffix(name), statblock);
        this.activelyHostile = true;
        this.setSerialNumber();
        this.aiType = MonsterAI.RETALIATORY;
        this.setFaction(CreatureFaction.MONSTER);
    }

    public void setAiType(MonsterAI newType) {
        this.aiType = newType;
    }

    public void setActivelyHostile(boolean setting) {
        this.activelyHostile = setting;
    }

    private void setSerialNumber() {
        this.monsterNumber = Monster.serialNumber;
        Monster.serialNumber++;
    }

    public boolean isActivelyHostile() {
        return this.activelyHostile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        if (!super.equals(o))
            return false;
        Monster monster = (Monster) o;
        return monsterNumber == monster.monsterNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(monsterNumber) + Objects.hash(this.getName()) * 13;
    }

    private Player getRandomPlayer(Collection<Creature> participants) {
        Player[] players = participants.stream().filter(creature -> creature instanceof Player).toArray(Player[]::new);
        int randomIndex = (int) (Math.random() * players.length);
        return players[randomIndex];
    }

    private Optional<Creature> getAttackerCreature(Collection<Creature> participants) {
        if (lastAttacker == null) {
            return Optional.empty();
        }
        return participants.stream().filter(creature -> lastAttacker.getName().equals(creature.getName())).findFirst();
    }

    @Override
    public AttackDamageMessage applyAttack(Attack attack) {
        int prevHealth = this.getStats().get(Stats.CURRENTHP);
        AttackDamageMessage result = super.applyAttack(attack); // perhaps a better way to do this?
        int damageDealt = prevHealth - this.getStats().get(Stats.CURRENTHP);
        switch (aiType) {
            case RETALIATORY:
                if (damageDealt > 0) {
                    lastDamage = damageDealt;
                    lastAttacker = attack.getAttacker();
                }
                break;
            case VENGEFUL:
                if (damageDealt >= lastDamage) {
                    lastDamage = damageDealt;
                    lastAttacker = attack.getAttacker();
                }
                break;
            case RANDOM:
                break;
        }
        return result;
    }

    @Override
    public Collection<Creature> selectAttackTargets(Collection<Creature> participants) {
        HashSet<Creature> targets = new HashSet<>();

        Optional<Creature> maybeTarget = getAttackerCreature(participants);
        if (maybeTarget.isPresent()) {
            targets.add(maybeTarget.get());
        } else { // as a backup
            targets.add(getRandomPlayer(participants));
        }

        return targets;
    }
}
