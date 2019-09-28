package com.whiuk.philip.mud.server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeStore {
    private HashMap<String, Recipe> recipes;
    private final ThingStore thingStore;

    RecipeStore(ThingStore thingStore) throws IOException, ParseException {
        this.thingStore = thingStore;
        try (FileReader reader = new FileReader("recipes.json")) {
            JSONParser jsonParser = new JSONParser();
            JSONArray recipesData = (JSONArray) jsonParser.parse(reader);
            loadRecipes(recipesData);
        }
    }

    private void loadRecipes(JSONArray recipesData) {
        recipes = new HashMap<>();
        for (Object aRecipeData : recipesData) {
            JSONObject recipeData = (JSONObject) aRecipeData;
            recipes.put((String) recipeData.get("id"), new Recipe(
                    parseSkillRequirements((JSONArray) recipeData.get("skillRequirements")),
                    ((Long) recipeData.get("percentageSuccessChance")).intValue(),
                    (Boolean) recipeData.get("consumeItem"),
                    (Boolean) recipeData.get("consumeTargetOnSuccess"),
                    (Boolean) recipeData.get("targetMustBeInInventory"),
                    (Boolean) recipeData.get("targetMustNotBeInInventory"),
                    parseConsumables((JSONArray) recipeData.get("consumables")),
                    parseSuccessItems((JSONArray) recipeData.get("successResultItems")),
                    parseResultTargets((JSONArray) recipeData.get("successResultTargets")),
                    parseExpReward((JSONArray) recipeData.get("experienceGained")),
                    (String) recipeData.get("startMessage"),
                    (String) recipeData.get("successMessage"),
                    (String) recipeData.get("failureMessage"),
                    (String) recipeData.get("cancelledMessage")
            ));
        }
    }

    private Map<String, Integer> parseExpReward(JSONArray expRewardsData) {
        Map<String, Integer> rewards = new HashMap<>();
        for (Object expRewardData : expRewardsData) {
            JSONObject data = (JSONObject) expRewardData;
            rewards.put((String) data.get("skill"),
                    ((Long) data.get("xp")).intValue());
        }
        return rewards;
    }

    private List<ThingType> parseResultTargets(JSONArray resultTargetsData) {
        List<ThingType> targets = new ArrayList<>();
        for (Object resultTargetData : resultTargetsData) {
            String data = (String) resultTargetData;
            ThingType thingType = thingStore.get(data);
            if (thingType == null) {
                throw new IllegalArgumentException(data);
            }
            targets.add(thingType);

        }
        return targets;
    }

    private List<Recipe.RecipeItem> parseSuccessItems(JSONArray successItemsData) {
        List<Recipe.RecipeItem> items = new ArrayList<>();
        for (Object successItemData : successItemsData) {
            JSONObject data = (JSONObject) successItemData;
            items.add(new Recipe.RecipeItem(
                    thingStore.getItemType((String) data.get("itemType")),
                    ((Long) data.get("quantity")).intValue()));
        }
        return items;
    }

    private List<Recipe.RecipeItem> parseConsumables(JSONArray consumablesData) {
        List<Recipe.RecipeItem> rewards = new ArrayList<>();
        for (Object consumableData : consumablesData) {
            JSONObject data = (JSONObject) consumableData;
            rewards.add(new Recipe.RecipeItem(
                    thingStore.getItemType((String) data.get("itemType")),
                    ((Long) data.get("quantity")).intValue()));

        }
        return rewards;
    }

    private Map<String,Integer> parseSkillRequirements(JSONArray skillRequirementsData) {
        Map<String, Integer> requirements = new HashMap<>();
        for (Object skillRequirementData : skillRequirementsData) {
            JSONObject data = (JSONObject) skillRequirementData;
            requirements.put((String) data.get("skill"),
                    ((Long) data.get("level")).intValue());

        }
        return requirements;
    }

    public Recipe get(String recipeId) {
        return recipes.get(recipeId);
    }
}
