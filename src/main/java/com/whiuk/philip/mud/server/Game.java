package com.whiuk.philip.mud.server;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Game {
    static class CommandProcessor {
        final static Set<String> gameCommands = new HashSet<>(Arrays.asList(
                "LOOK", "MOVE", "JUMP", "CLIMB", "FALL",
                "USE", "EAT", "DRINK",
                "EQUIP", "UNEQUIP", "WEAR", "REMOVE",
                "TALK",
                "ATTACK",
                "INVENTORY", "EQUIPMENT",
                "SAY", "SHOUT",
                "OFFER", "ACCEPT", "DECLINE"));

        static void handleMessage(PlayerCharacter character, String message) {
            String[] messageData = message.split(" ");
            switch (messageData[0]) {
                //Character Info
                case "EQUIPMENT":
                    PlayerCharacter.CommandProcessor.handleEquipment(character);
                    break;
                case "INVENTORY":
                    PlayerCharacter.CommandProcessor.handleInventory(character);
                    break;
                case "SKILLS":
                    PlayerCharacter.CommandProcessor.handleSkills(character);
                    break;

                //Chat & Trade
                case "WHO":
                    character.chunk.handleWho(character);
                    break;
                case "SAY":
                    character.chunk.handleSay(character, message.split(" ", 2)[1]);
                    break;
                case "SHOUT":
                    character.chunk.handleShout(character, message.split(" ", 2)[1]);
                    break;
                case "OFFER":
                    character.chunk.handleOffer(character, messageData[1], messageData[2], messageData[3]);
                    break;
                case "ACCEPT":
                    character.handleOfferAccept(messageData[1]);
                    break;
                case "REJECT":
                    character.handleOfferReject(messageData[1]);
                    break;

                //Environment
                case "LOOK":
                    if (messageData.length > 1) {
                        character.chunk.handleLook(character, messageData[1]);
                    } else {
                        character.chunk.handleLook(character);
                    }
                    break;
                case "MOVE":
                    character.chunk.handleMove(character, messageData[1]);
                    break;

                //Actions
                case "USE":
                    character.chunk.handleUse(character, messageData[1], messageData[2],
                            messageData.length > 3 ? messageData[3]: null);
                    break;
                case "EAT":
                    character.handleEat(character, messageData[1]);
                    break;
                case "DRINK":
                    character.handleDrink(character, messageData[1]);
                    break;
                case "GET":
                    character.chunk.handleGet(character, messageData[2]);
                    break;
                case "DROP":
                    character.chunk.handleDrop(character, messageData[2]);
                    break;
                case "EQUIP":
                case "WEAR":
                    character.handleEquip(messageData[2]);
                    break;
                case "REMOVE":
                case "UNEQUIP":
                    character.handleUnequip(messageData[2]);
                    break;
                case "TALK":
                    character.chunk.handleTalk(character, message.split(" ", 2)[1]);

                default:
                    character.sendMessage(String.format("Unknown command: %1$s. Try HELP to see a list of available commands",
                            messageData[0]));
                    break;
            }
        }
    }
}
