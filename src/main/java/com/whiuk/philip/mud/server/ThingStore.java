package com.whiuk.philip.mud.server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ThingStore {
    private HashMap<String, ThingType> thingTypes;

    ThingStore() throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader("things.json")) {
            JSONArray thingTypesData = (JSONArray) jsonParser.parse(reader);
            loadThingTypes(thingTypesData);
        }
    }

    private void loadThingTypes(JSONArray thingTypesData) {
        thingTypes = new HashMap<>();
        for (Object aThingTypesData : thingTypesData) {
            JSONObject thingTypeData = (JSONObject) aThingTypesData;
            ThingType thing;
            String type = (String) thingTypeData.get("type");
            String id = (String) thingTypeData.get("id");
            try {
                switch (type) {
                    case "Animal":
                        thing = new AnimalType(
                                id,
                                (String) thingTypeData.get("description"),
                                ((Long) thingTypeData.get("maxHealth")).intValue(),
                                FeedingType.valueOf((String) thingTypeData.get("feedingType")));
                        break;
                    case "Equipment":
                        thing = new EquipmentType(id,
                                (String) thingTypeData.get("name"),
                                parseRecipes((JSONArray) thingTypeData.getOrDefault("recipes", new JSONArray())),
                                Slot.valueOf((String) thingTypeData.get("slot")));
                        break;
                    case "Item":
                        thing = new ItemType(id,
                                (String) thingTypeData.get("name"),
                                parseRecipes((JSONArray) thingTypeData.getOrDefault("recipes", new JSONArray())));
                        break;
                    case "Object":
                        thing = new ObjectType(id, (String) thingTypeData.get("description"),
                                parseRecipes((JSONArray) thingTypeData.getOrDefault("recipes", new JSONArray())));
                        break;
                    case "Tree":
                        thing = new TreeType(id);
                        break;
                    default:
                        throw new UnsupportedOperationException(type);
                }
                thingTypes.put(id, thing);
            } catch (Exception e) {
                throw new RuntimeException("Failed to parse thing: " + id, e);
            }
        }

    }

    private Map<String, Map<String, String>> parseRecipes(JSONArray recipesData) {
        Map<String, Map<String, String>> recipes = new HashMap<>();
        for (Object aRecipesData : recipesData) {
            JSONObject itemTypeData = (JSONObject) aRecipesData;
            Map<String, String> itemTypeRecipes = new HashMap<>();
            JSONArray itemTypeRecipesData = (JSONArray) itemTypeData.getOrDefault("recipes",
                    new JSONArray());
            for (Object anItemTypeRecipesData : itemTypeRecipesData) {
                JSONObject recipeData = (JSONObject) anItemTypeRecipesData;
                itemTypeRecipes.put(
                        (String) recipeData.get("id"),
                        (String) recipeData.get("recipeId"));
            }
            recipes.put((String) itemTypeData.get("itemType"), itemTypeRecipes);
        }
        return recipes;
    }

    public ThingType get(String id) {
        return thingTypes.get(id);
    }

    public ItemType getItemType(String id) {
        return (ItemType) thingTypes.get(id);
    }
}
