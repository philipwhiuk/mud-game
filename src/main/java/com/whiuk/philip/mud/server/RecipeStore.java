package com.whiuk.philip.mud.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RecipeStore {
    private HashMap<String, Recipe> recipes;
    private final ThingStore thingStore;

    RecipeStore(ThingStore thingStore) throws IOException {
        this.thingStore = thingStore;
        loadRecipes(new BufferedReader(new FileReader("recipes.dat")));
    }

    private void loadRecipes(BufferedReader reader) throws IOException {
        recipes = new HashMap<>();
        int count = Integer.parseInt(reader.readLine());
        for (int i = 0; i < count ; i++) {
            String[] recipeData = reader.readLine().split(",");
            recipes.put(recipeData[0], new Recipe(
                    parseSkillRequirements(reader, Integer.parseInt(recipeData[1])),
                    Integer.parseInt(recipeData[2]),
                    Boolean.parseBoolean(recipeData[3]),
                    Boolean.parseBoolean(recipeData[4]),
                    Boolean.parseBoolean(recipeData[5]),
                    Boolean.parseBoolean(recipeData[6]),
                    parseConsumables(reader, Integer.parseInt(recipeData[7])),
                    parseSuccessItems(reader, Integer.parseInt(recipeData[8])),
                    parseResultTargets(reader, Integer.parseInt(recipeData[9])),
                    parseExpReward(reader, Integer.parseInt(recipeData[10])),
                    reader.readLine(),
                    reader.readLine(),
                    reader.readLine(),
                    reader.readLine()
            ));
        }
    }

    private Map<String, Integer> parseExpReward(BufferedReader reader, int count) throws IOException {
        Map<String, Integer> rewards = new HashMap<>();
        for (int i = 0; i < count; i++) {
            String[] data = reader.readLine().split(",");
            rewards.put(data[0], Integer.valueOf(data[1]));
        }
        return rewards;
    }

    private List<ThingType> parseResultTargets(BufferedReader reader, int count) throws IOException {
        List<ThingType> targets = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String[] data = reader.readLine().split(",");
            ThingType thingType = thingStore.get(data[0]);
            if (thingType == null) {
                throw new IllegalArgumentException(data[0]);
            }
            targets.add(thingType);

        }
        return targets;
    }

    private List<Recipe.RecipeItem> parseSuccessItems(BufferedReader reader, int count) throws IOException {
        List<Recipe.RecipeItem> items = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String[] data = reader.readLine().split(",");
            items.add(new Recipe.RecipeItem(thingStore.getItemType(data[0]), Integer.parseInt(data[1])));
        }
        return items;
    }

    private List<Recipe.RecipeItem> parseConsumables(BufferedReader reader, int count) throws IOException {
        List<Recipe.RecipeItem> rewards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String[] data = reader.readLine().split(",");
            rewards.add(new Recipe.RecipeItem(thingStore.getItemType(data[0]), Integer.parseInt(data[1])));

        }
        return rewards;
    }

    private Map<String,Integer> parseSkillRequirements(BufferedReader reader, int count) throws IOException {
        Map<String, Integer> requirements = new HashMap<>();
        for (int i = 0; i < count; i++) {
            String[] data = reader.readLine().split(",");
            requirements.put(data[0], Integer.valueOf(data[1]));

        }
        return requirements;
    }

    public Recipe get(String recipeId) {
        return recipes.get(recipeId);
    }
}
