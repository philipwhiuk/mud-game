package com.whiuk.philip.mud.server;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Recipe {

    private final Map<String, Integer> skillRequirements;
    private final int percentageSuccessChance;
    private final boolean consumeItem;
    private final boolean consumeTargetOnSuccess;
    private final boolean targetMustBeInInventory;
    private final boolean targetMustNotBeInInventory;
    private final String startMessage;
    private final String failureMessage;
    private final String successMessage;
    private final String cancelledMessage;
    private List<RecipeItem> consumables;
    private List<RecipeItem> successResultItems;
    private List<ThingType> successResultTargets;
    private Map<String, Integer> experienceGained;

    Recipe(
            Map<String, Integer> skillRequirements, int percentageSuccessChance,
            boolean consumeItem, boolean consumeTargetOnSuccess,
            boolean targetMustBeInInventory, boolean targetMustNotBeInInventory,
            List<RecipeItem> consumables,
            List<RecipeItem> successResultItems, List<ThingType> successResultTargets,
            Map<String, Integer> experienceGained,
            String startMessage, String successMessage, String failureMessage, String cancelledMessage) {
        this.skillRequirements = skillRequirements;
        this.targetMustBeInInventory = targetMustBeInInventory;
        this.targetMustNotBeInInventory = targetMustNotBeInInventory;
        this.startMessage = startMessage;
        this.consumeItem = consumeItem;
        this.percentageSuccessChance = percentageSuccessChance;
        this.successMessage = successMessage;
        this.consumeTargetOnSuccess = consumeTargetOnSuccess;
        this.consumables = consumables;
        this.successResultItems = successResultItems;
        this.successResultTargets = successResultTargets;
        this.experienceGained = experienceGained;
        this.failureMessage = failureMessage;
        this.cancelledMessage = cancelledMessage;
    }

    static class RecipeItem {
        final ItemType itemType;
        final int quantity;

        RecipeItem(ItemType itemType, int quantity) {
            this.itemType = itemType;
            this.quantity = quantity;
        }
    }

    public void scheduleTask(
            @SuppressWarnings("unused") Chunk chunk,
            ScheduledExecutorService taskQueue,
            PlayerCharacter player,
            Item item, Thing target)  {
        //TODO: Schedule in chunk
        if (canBeDone(player)) {
            player.sendMessage(startMessage);
            player.startTask(taskQueue, () -> {
                boolean targetInInventory;
                targetInInventory = targetMustBeInInventory || !targetMustNotBeInInventory && player.hasItem(target.id());
                for (RecipeItem recipeItem : consumables) {
                    for (int i = 0; i < recipeItem.quantity; i++) {
                        player.removeItem(recipeItem.itemType.id);
                    }
                }
                if (consumeItem) {
                    player.removeItem(item.id());
                }
                boolean success = isSuccess();
                if (success) {
                    player.sendMessage(successMessage);
                    if (consumeTargetOnSuccess) {
                        if (targetInInventory) {
                            player.removeItem(target.id());
                        } else {
                            player.location.remove(target);
                        }
                    }
                    for (Map.Entry<String, Integer> experience : experienceGained.entrySet()) {
                        String skill = experience.getKey();
                        int expGain = experience.getValue();
                        Experience skillXp = player.skills.getOrDefault(skill, Experience.NoExperience());
                        skillXp.gainExperience(expGain);
                        player.skills.put(skill, skillXp);
                    }
                    for (RecipeItem recipeItem : successResultItems) {
                        for (int i = 0; i < recipeItem.quantity; i++) {
                            player.receiveItem(recipeItem.itemType.create());
                        }
                    }
                    for (ThingType thing : successResultTargets) {
                        player.location.add(thing.create());
                    }
                } else {
                    player.sendMessage(failureMessage);
                }
            }, 600, cancelledMessage);
        } else {
            player.sendMessage("You don't have everything you need for that");
        }
    }

    private boolean isSuccess() {
        return new Random().nextInt(100) < percentageSuccessChance;
    }

    private boolean canBeDone(PlayerCharacter player) {
        for (RecipeItem i : consumables) {
            if (!player.hasItem(i.itemType.id, i.quantity)) {
                return false;
            }
        }
        for (Map.Entry<String, Integer> skillReq : skillRequirements.entrySet()) {
            if (player.skills.getOrDefault(skillReq.getKey(), Experience.NoExperience()).level() < skillReq.getValue
                    ()) {
                return false;
            }
        }
        int totalCount = successResultItems.stream().map(r -> r.quantity).mapToInt(x -> x).sum();
        return player.hasSpaceForItems(totalCount);
    }
}