package com.whiuk.philip.mud.server;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

class ConversationChoice {
    final String playerText;
    final String npcResponse;
    NPCAction npcAction;
    final Predicate<QuestState> canSee;
    final Map<Integer, ConversationChoiceOption> responseOptions;

    ConversationChoice(String player, String npcResponse, NPCAction npcAction,
                       Predicate<QuestState> canSee, Map<Integer, ConversationChoiceOption> responseOptions) {
        this.playerText = player;
        this.npcResponse = npcResponse;
        this.npcAction = npcAction;
        this.canSee = canSee;
        this.responseOptions = responseOptions;
    }
}