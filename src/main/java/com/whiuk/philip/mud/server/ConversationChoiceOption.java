package com.whiuk.philip.mud.server;

public class ConversationChoiceOption {
    private final String option;
    private final ConversationChoice choice;

    public ConversationChoiceOption(String option, ConversationChoice choice) {
        this.option = option;
        this.choice = choice;
    }

    public String getOption() {
        return option;
    }

    public ConversationChoice getChoice() {
        return choice;
    }
}
