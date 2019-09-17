package com.whiuk.philip.mud.server;

public class Conversation {

    //TODO: Multi-party conversation
    private ConversationChoice choice;
     Talks talkingTo;

    public Conversation(Talks talks) {
        choice = talks.getConversationStart();
    }

    public void start(PlayerCharacter player) {
        processChoice(player);
    }

    private void processChoice(PlayerCharacter player) {
        if (choice.playerText != null) {
            player.sendMessage(player.name + ": " + choice.playerText);
        }
        if (choice.npcResponse != null) {
            player.sendMessage(talkingTo.name() + ": " + choice.playerText);
        }
        if (choice.npcAction != null) {
            choice.npcAction.doAction((NPC) talkingTo);
        } else {
            choice.responseOptions.forEach((key, value) -> player.sendMessage("Option "+key+": " + value.getOption()));
        }
    }

    public void update(PlayerCharacter player, String talkInfo) {
        choice = choice.responseOptions.get(Integer.parseInt(talkInfo)).getChoice();
        processChoice(player);
    }
}
