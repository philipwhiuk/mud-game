package com.whiuk.philip.mud.server;

public class Experience {
    private int experience;
    private int level;

    public static Experience NoExperience() {
        return new Experience(0);
    }

    private Experience(int experience) {
        this.level = ExperienceTable.getLevel(experience);
        this.experience = experience;
    }

    void gainExperience(int xpGain) {
        experience += xpGain;
        while (ExperienceTable.toLevel(level, experience) < 0) {
            level++;
        }
    }

    public int level() {
        return level;
    }
}
