package com.lhf.game.magic.builder;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;

import com.lhf.game.EffectPersistence;
import com.lhf.game.EffectResistance.TargetResistAmount;
import com.lhf.game.TickType;
import com.lhf.game.EffectResistance;
import com.lhf.game.creature.CreatureEffectSource;
import com.lhf.game.creature.vocation.Vocation.VocationName;
import com.lhf.game.dice.DamageDice;
import com.lhf.game.dice.DieType;
import com.lhf.game.enums.Attributes;
import com.lhf.game.enums.DamageFlavor;
import com.lhf.game.enums.ResourceCost;
import com.lhf.game.enums.Stats;
import com.lhf.game.magic.CreatureAOESpellEntry.AutoTargeted;
import com.lhf.game.magic.builder.SpellEntryBuilder.SpellEntryBuilderAdapter;

public class CLIAdapter implements SpellEntryBuilderAdapter {
    private Scanner input;

    public CLIAdapter() {
        this.input = new Scanner(System.in);
    }

    @Override
    public int menuChoice(List<String> choices) {
        String chosen = null;
        do {
            System.out.print("Choose one of:\n");
            StringJoiner sj = new StringJoiner(", ").setEmptyValue("No choices!!");
            for (String choice : choices) {
                sj.add(choice);
            }
            System.out.println(sj.toString());
            chosen = this.input.nextLine().toLowerCase().trim();
        } while (chosen == null || chosen.isBlank());
        for (int i = 0; i < choices.size(); i++) {
            if (chosen.equalsIgnoreCase(choices.get(i))) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void stepSucceeded(boolean succeeded) {
        if (succeeded) {
            System.out.println("Ok, that worked.");
        }
        System.out.println("That didn't work, you may have to try again.");
    }

    @Override
    public Boolean yesOrNo() {
        System.out.println("yes or no?");
        String validation_response = this.input.nextLine().toLowerCase();
        if (validation_response.equals("yes") || validation_response.equals("no")) {
            if (validation_response.equals("yes")) {
                return Boolean.TRUE;
            } else {
                System.out.println("Restarting from last prompt.");
                return Boolean.FALSE;
            }
        } else {
            System.err.println("Invalid response, restarting from last prompt.");
            return Boolean.FALSE;
        }
    }

    @Override
    public void close() {
        this.input.close();
    }

    public String buildString(String attribute, String ofThis, String defaultAttribute) {
        Boolean valid;
        String name;
        do {
            if (defaultAttribute != null && !defaultAttribute.isBlank()) {
                System.out.printf("The default %s is '%s', would you like to have a different %s? ", attribute,
                        defaultAttribute, attribute);
                if (!this.yesOrNo()) {
                    return defaultAttribute;
                }
            }
            System.out.printf("Please type the %s's %s: ", ofThis, attribute);
            name = this.input.nextLine();

            System.out.printf("The %s is: %s is this correct?", attribute, name);

            valid = this.yesOrNo();

        } while (!valid);
        return name;
    }

    @Override
    public String buildName() {
        return this.buildString("name", "spell", null);
    }

    @Override
    public ResourceCost buildLevel() {
        Integer level = null;
        do {
            System.out.println("What is this spell's level as an integer? ");
            level = this.input.nextInt();
            this.input.nextLine();
            System.out.printf("You chose %d, is that correct? ", level);
            if (!this.yesOrNo()) {
                level = null;
            }
        } while (level == null);
        return ResourceCost.fromInt(level);
    }

    @Override
    public String buildInvocation(String name) {
        return this.buildString("invocation", "spell", name);
    }

    @Override
    public String buildDescription(String name) {
        return this.buildString("description", "spell", name);
    }

    @Override
    public Set<VocationName> buildVocations() {
        TreeSet<VocationName> names = new TreeSet<>();
        StringJoiner sj = new StringJoiner(", ");
        for (VocationName vName : VocationName.values()) {
            sj.add(vName.name());
        }
        while (true) {
            System.out.print("Enter one of the vocation names or 'done' if there are no more to add: ");
            System.out.println(sj.toString());
            String chosen = this.input.nextLine();
            if (chosen.equalsIgnoreCase("done")) {
                return names;
            }
            VocationName nextName = VocationName.getVocationName(chosen);
            if (nextName == null) {
                System.err.println(chosen + " is not a valid vocation.  Try " + sj.toString());
                continue;
            }
            names.add(nextName);
        }
    }

    @Override
    public EffectPersistence buildEffectPersistence() {
        boolean valid = false;
        TickType tickType = null;
        int count = 1;
        do {
            System.out.println("What sort of duration should this effect have?");
            StringJoiner sj = new StringJoiner(", ");
            for (TickType tt : TickType.values()) {
                sj.add(tt.name());
            }
            System.out.printf("Choose from: %s \r\n", sj.toString());
            String chosen = null;
            chosen = this.input.nextLine();
            tickType = TickType.getTickType(chosen);
            if (tickType == null) {
                System.err.println(chosen + " is not a valid tick type.  Try " + sj.toString());
                continue;
            }
            if (TickType.INSTANT.equals(tickType)) {
                count = 1;
            } else if (TickType.CONDITIONAL.equals(tickType)) {
                count = -1;
            } else {
                System.out.println("And how many of those? (number) ");
                count = this.input.nextInt();
                this.input.nextLine();
            }
            if (count < -1 || count == 0) {
                System.err.printf("%d is an illegal count of those.\n");
            } else {
                valid = true;
            }
        } while (!valid);
        EffectPersistence effectPersistence = new EffectPersistence(count, tickType);
        System.out.println(effectPersistence.toString());
        return effectPersistence;
    }

    private EnumSet<Attributes> pickAttributes() {
        EnumSet<Attributes> attrSet = EnumSet.noneOf(Attributes.class);
        Attributes attr = null;
        System.out.println("Pick at least one from " + Attributes.values().toString() + " and then enter 'done'");
        do {
            attr = Attributes.getAttribute(this.input.nextLine());
            if (attr != null) {
                attrSet.add(attr);
            }
        } while (attr != null);
        return attrSet.size() > 1 ? attrSet : null;
    }

    @Override
    public EffectResistance buildEffectResistance() {
        EnumSet<Attributes> actorAttrs = null;
        Stats actorStat = null;
        Integer actorDC = null;

        // these are for the target of the effect
        EnumSet<Attributes> targetAttrs = null;
        Stats targetStat = null;
        Integer targetDC = null;
        TargetResistAmount resistAmount = null;

        System.out.println("Should this spell be resistable?");
        if (!this.yesOrNo()) {
            System.out.println("Chosen to have no resistance.");
            return null;
        }
        int menu = -1;
        do {
            System.out.println("What should the actor use?");
            menu = this.menuChoice(List.of("nothing", "attributes", "stat", "dc"));
            switch (menu) {
                case 0:
                    System.out.println("Chosen to have no resistance.");
                    return null;
                case 1:
                    actorAttrs = this.pickAttributes();
                    if (actorAttrs == null) {
                        System.err.println("Unrecognized option...repeating menu.");
                        continue;
                    }
                    menu = 0;
                    break;
                case 2:
                    System.out.println("Pick a stat from " + Stats.values().toString());
                    actorStat = Stats.getStat(this.input.nextLine());
                    if (actorStat == null) {
                        System.err.println("Unrecognized option...repeating menu.");
                        continue;
                    }
                    menu = 0;
                    break;
                case 3:
                    System.out.println("Pick a number to be the DC");
                    try {
                        actorDC = Integer.valueOf(this.input.nextLine());
                    } catch (NumberFormatException e) {
                        actorDC = null;
                        System.err.println("Unrecognized option...repeating menu.");
                        continue;
                    }
                    menu = 0;
                    break;
                default:
                    System.err.println("Unrecognized option...repeating menu.");
                    break;
            }
        } while (menu != 0);
        if (actorAttrs == null && actorStat == null && actorDC == null) {
            System.out.println("Chosen to have no resistance.");
            return null;
        }
        menu = -1;
        do {
            System.out.println("What should the target use?");
            menu = this.menuChoice(List.of("nothing", "attributes", "stat", "dc"));
            switch (menu) {
                case 0:
                    System.out.println("Chosen to have no resistance.");
                    return null;
                case 1:
                    targetAttrs = this.pickAttributes();
                    if (targetAttrs == null) {
                        System.err.println("Unrecognized option...repeating menu.");
                        continue;
                    }
                    menu = 0;
                    break;
                case 2:
                    System.out.println("Pick a stat from " + Stats.values().toString());
                    targetStat = Stats.getStat(this.input.nextLine());
                    if (targetStat == null) {
                        System.err.println("Unrecognized option...repeating menu.");
                        continue;
                    }
                    menu = 0;
                    break;
                case 3:
                    System.out.println("Pick a number to be the DC");
                    try {
                        targetDC = Integer.valueOf(this.input.nextLine());
                    } catch (NumberFormatException e) {
                        targetDC = null;
                        System.err.println("Unrecognized option...repeating menu.");
                        continue;
                    }
                    menu = 0;
                    break;
                default:
                    System.err.println("Unrecognized option...repeating menu.");
                    break;
            }
        } while (menu != 0);
        if (targetAttrs == null && targetStat == null && targetDC == null) {
            System.out.println("Chosen to have no resistance.");
            return null;
        }
        System.out.println("Should the target be able to save for half?");
        if (this.yesOrNo()) {
            resistAmount = TargetResistAmount.HALF;
        } else {
            resistAmount = TargetResistAmount.ALL;
        }

        return new EffectResistance(actorAttrs, actorStat, actorDC, targetAttrs, targetStat, targetDC, resistAmount);
    }

    @Override
    public Set<CreatureEffectSource> buildCreatureEffectSources() {
        HashSet<CreatureEffectSource> sources = new HashSet<>();
        while (true) {
            if (sources.size() > 0) {
                System.out.println("Current sources:");
                for (CreatureEffectSource source : sources) {
                    System.out.println(source.toString());
                }
            }
            System.out.println("Would you like to add a source?");
            if (!this.yesOrNo()) {
                break;
            }
            String effectName = this.buildString("name", "creature effect", null);
            String effectDescription = this.buildString("description", "creature effect", effectName);
            EffectPersistence persistence = this.buildEffectPersistence();
            EffectResistance resistance = this.buildEffectResistance();
            System.out.println("Should it restore the faction?");
            boolean restore = this.yesOrNo();
            CreatureEffectSource source = new CreatureEffectSource(effectName, persistence, resistance,
                    effectDescription, restore);
            System.out.printf("Added %s \n", source.toString());
            sources.add(source);

            int menu = -1;
            do {
                Integer delta;
                Attributes attr;
                System.out.println("What would you like to add to that source?");
                menu = this.menuChoice(List.of("exit", "statchange", "attrscore", "attrbonus", "damagedice"));
                switch (menu) {
                    case 0:
                        break;
                    case 1:
                        System.out.println("Which stat?");
                        Stats stat = Stats.getStat(this.input.nextLine());
                        if (stat == null) {
                            System.err.println("Choose one of " + Stats.values().toString());
                            continue;
                        }
                        System.out.println("Change by how much?");
                        delta = this.input.nextInt();
                        this.input.nextLine();
                        source.addStatChange(stat, delta);
                        break;
                    case 2:
                        System.out.println("Which attribute score?");
                        attr = Attributes.getAttribute(this.input.nextLine());
                        if (attr == null) {
                            System.err.println("Choose one of " + Attributes.values().toString());
                            continue;
                        }
                        System.out.println("Change by how much?");
                        delta = this.input.nextInt();
                        this.input.nextLine();
                        source.addAttributeScoreChange(attr, delta);
                        break;
                    case 3:
                        System.out.println("Which attribute bonus?");
                        attr = Attributes.getAttribute(this.input.nextLine());
                        if (attr == null) {
                            System.err.println("Choose one of " + Attributes.values().toString());
                            continue;
                        }
                        System.out.println("Change by how much?");
                        delta = this.input.nextInt();
                        this.input.nextLine();
                        source.addAttributeBonusChange(attr, delta);
                        break;
                    case 4:
                        System.out.println("Which flavor of damage?");
                        DamageFlavor flavor = DamageFlavor.getDamageFlavor(this.input.nextLine());
                        if (flavor == null) {
                            System.err.println("Choose one of " + DamageFlavor.values().toString());
                            continue;
                        }
                        System.out.println("What type of die?");
                        DieType dieType = DieType.getDieType(this.input.nextLine());
                        if (dieType == null) {
                            System.err.println("Choose one of " + DieType.values().toString());
                            continue;
                        }
                        System.out.println("How many?");
                        delta = this.input.nextInt();
                        this.input.nextLine();
                        source.addDamage(new DamageDice(delta, dieType, flavor));
                        break;
                    default:
                        System.err.println("Unrecognized option...repeating menu.");
                        break;
                }
                System.out.printf("Source: %s \n", source.toString());

            } while (menu != 0);
        }
        return sources;
    }

    @Override
    public AutoTargeted buildAutoSafe() {
        int npc, caster, allies, enemies, renegades;
        System.out.println("Building auto safe, is this spell offensive?");
        boolean isOffensive = this.yesOrNo();
        if (isOffensive) {
            npc = 0; // upcast once to not affect with offensive spell
            caster = 1;
            allies = 2;
            enemies = 3;
            renegades = 4; // upcast four times to not affect with offensive spell
            System.out.printf("How many levels above base are NPCs unaffected? (int) default %d\r\n", npc);
            npc = this.input.nextInt();
            System.out.printf("\r\nHow many levels above base is the caster unaffected? (int) default %d\r\n", caster);
            caster = this.input.nextInt();
            System.out.printf("\r\nHow many levels above base are allies unaffected? (int) default %d\r\n", allies);
            allies = this.input.nextInt();
            System.out.printf("\r\nHow many levels above base are enemies unaffected? (int) default %d\r\n", enemies);
            enemies = this.input.nextInt();
            System.out.printf("\r\nHow many levels above base are renegades unaffected? (int) default %d\r\n",
                    renegades);
            renegades = this.input.nextInt();
        } else {
            npc = -1;
            caster = 0;
            allies = 0;
            enemies = -2;
            renegades = -3; // upcast three times to affect with a beneficial spell
            System.out.printf("How many levels above base are NPCs affected? (int) default %d\r\n", npc);
            npc = this.input.nextInt();
            System.out.printf("\r\nHow many levels above base is the caster affected? (int) default %d\r\n", caster);
            caster = this.input.nextInt();
            System.out.printf("\r\nHow many levels above base are allies affected? (int) default %d\r\n", allies);
            allies = this.input.nextInt();
            System.out.printf("\r\nHow many levels above base are enemies affected? (int) default %d\r\n", enemies);
            enemies = this.input.nextInt();
            System.out.printf("\r\nHow many levels above base are renegades affected? (int) default %d\r\n", renegades);
            renegades = this.input.nextInt();
        }

        this.input.nextLine(); // clear
        return new AutoTargeted(npc, caster, allies, enemies, renegades);
    }

    @Override
    public boolean buildSingleTarget() {
        System.out.println("Does it only target a single creature?");
        return this.yesOrNo();
    }

}
