package com.whiuk.philip.mud.server;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ThingStore {
    private HashMap<String, ThingType> thingTypes;

    ThingStore() throws IOException {
        loadThingTypes(new BufferedReader(new FileReader("things.dat")));
    }

    private void loadThingTypes(BufferedReader reader) throws IOException {
        thingTypes = new HashMap<>();
        int count = Integer.parseInt(reader.readLine());
        for (int i = 0; i < count; i++) {
            String[] data = reader.readLine().split(",");
            ThingType thing;
            switch (data[0]) {
                case "Animal":
                    thing = new AnimalType(data[1], reader.readLine(), Integer.parseInt(data[2]), FeedingType.valueOf(data[3]));
                    break;
                case "Equipment":
                    thing = new EquipmentType(data[1], data[2], Slot.valueOf(data[3]));
                    break;
                case "Item":
                    thing = new ItemType(data[1], data[2], parseRecipes(reader, Integer.parseInt(data[3])));
                    break;
                case "Object":
                    thing = new ObjectType(data[1], reader.readLine(), parseRecipes(reader, Integer.parseInt(data[2])));
                    break;
                case "Tree":
                    thing = new TreeType(data[1]);
                    break;
                default:
                    throw new UnsupportedOperationException(data[0]);
            }
            thingTypes.put(data[1], thing);
        }

    }

    private Map<String, Map<String, String>> parseRecipes(BufferedReader reader, int itemTypeCount) throws IOException {
        Map<String, Map<String, String>> recipes = new HashMap<>();
        for (int i = 0; i < itemTypeCount; i++) {
            String[] itemTypeData = reader.readLine().split(",");
            Map<String, String> itemTypeRecipes = new HashMap<>();
            int recipeCount = Integer.parseInt(itemTypeData[1]);
            for (int j = 0; j < recipeCount ; j++) {
                String[] recipeData = reader.readLine().split(",");
                itemTypeRecipes.put(recipeData[0], recipeData[1]);
            }
            recipes.put(itemTypeData[0], itemTypeRecipes);
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
