package com.example.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Central SQLite access class for Smart Pantry Manager.
 *  - Creates the tables and seeds 16 recipes on first run.
 *  - Provides CRUD methods for the pantry.
 *  - Contains the Strict-Matching Rule in getSuggestedRecipes().
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "smart_pantry.db";
    private static final int DB_VERSION = 1;

    // Pantry table
    private static final String T_PANTRY = "pantry";
    private static final String P_ID = "id";
    private static final String P_NAME = "name";
    private static final String P_QTY = "quantity";
    private static final String P_UNIT = "unit";

    // Recipes table
    private static final String T_RECIPES = "recipes";
    private static final String R_ID = "id";
    private static final String R_TITLE = "title";
    private static final String R_INSTR = "instructions";

    // Recipe ingredients table
    private static final String T_RI = "recipe_ingredients";
    private static final String RI_ID = "id";
    private static final String RI_RECIPE_ID = "recipe_id";
    private static final String RI_NAME = "ingredient_name";
    private static final String RI_QTY = "required_quantity";
    private static final String RI_UNIT = "unit";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true); // enables ON DELETE CASCADE
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + T_PANTRY + " ("
                + P_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + P_NAME + " TEXT NOT NULL, "
                + P_QTY + " REAL NOT NULL, "
                + P_UNIT + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + T_RECIPES + " ("
                + R_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + R_TITLE + " TEXT NOT NULL, "
                + R_INSTR + " TEXT NOT NULL)");

        db.execSQL("CREATE TABLE " + T_RI + " ("
                + RI_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + RI_RECIPE_ID + " INTEGER NOT NULL, "
                + RI_NAME + " TEXT NOT NULL, "
                + RI_QTY + " REAL NOT NULL, "
                + RI_UNIT + " TEXT NOT NULL, "
                + "FOREIGN KEY(" + RI_RECIPE_ID + ") REFERENCES "
                + T_RECIPES + "(" + R_ID + ") ON DELETE CASCADE)");

        seedRecipes(db); // runs once, when the database file is first created
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + T_RI);
        db.execSQL("DROP TABLE IF EXISTS " + T_RECIPES);
        db.execSQL("DROP TABLE IF EXISTS " + T_PANTRY);
        onCreate(db);
    }

    // =====================================================================
    //  PANTRY CRUD
    // =====================================================================

    /** CREATE: inserts a pantry item and returns its new row id (-1 on failure). */
    public long addPantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(P_NAME, item.getName().trim());
        cv.put(P_QTY, item.getQuantity());
        cv.put(P_UNIT, item.getUnit().trim());
        return db.insert(T_PANTRY, null, cv);
    }

    /** READ: returns every pantry item, sorted alphabetically. */
    public List<PantryItem> getAllPantryItems() {
        List<PantryItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_PANTRY, null, null, null, null, null,
                P_NAME + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                list.add(cursorToPantryItem(c));
            }
        }
        return list;
    }

    /** READ: returns one pantry item by id, or null if not found (used by Edit screen). */
    public PantryItem getPantryItem(long id) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_PANTRY, null, P_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null)) {
            if (c.moveToFirst()) {
                return cursorToPantryItem(c);
            }
        }
        return null;
    }

    /** UPDATE: overwrites an existing pantry item. Returns rows affected. */
    public int updatePantryItem(PantryItem item) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(P_NAME, item.getName().trim());
        cv.put(P_QTY, item.getQuantity());
        cv.put(P_UNIT, item.getUnit().trim());
        return db.update(T_PANTRY, cv, P_ID + "=?",
                new String[]{String.valueOf(item.getId())});
    }

    /** DELETE: removes a pantry item. Returns rows affected. */
    public int deletePantryItem(long id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(T_PANTRY, P_ID + "=?", new String[]{String.valueOf(id)});
    }

    /** DELETE ALL: empties the pantry (used by the Settings screen). Returns rows removed. */
    public int clearPantry() {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(T_PANTRY, null, null);
    }

    private PantryItem cursorToPantryItem(Cursor c) {
        return new PantryItem(
                c.getLong(c.getColumnIndexOrThrow(P_ID)),
                c.getString(c.getColumnIndexOrThrow(P_NAME)),
                c.getDouble(c.getColumnIndexOrThrow(P_QTY)),
                c.getString(c.getColumnIndexOrThrow(P_UNIT)));
    }

    // =====================================================================
    //  RECIPE READ METHODS
    // =====================================================================

    public List<Recipe> getAllRecipes() {
        List<Recipe> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_RECIPES, null, null, null, null, null,
                R_TITLE + " COLLATE NOCASE ASC")) {
            while (c.moveToNext()) {
                list.add(cursorToRecipe(c));
            }
        }
        return list;
    }

    /** Returns one recipe by id, or null (used by the Recipe Detail screen). */
    public Recipe getRecipe(long recipeId) {
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_RECIPES, null, R_ID + "=?",
                new String[]{String.valueOf(recipeId)}, null, null, null)) {
            if (c.moveToFirst()) {
                return cursorToRecipe(c);
            }
        }
        return null;
    }

    /** Returns all ingredient lines a recipe requires. */
    public List<RecipeIngredient> getIngredientsForRecipe(long recipeId) {
        List<RecipeIngredient> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        try (Cursor c = db.query(T_RI, null, RI_RECIPE_ID + "=?",
                new String[]{String.valueOf(recipeId)}, null, null, RI_ID + " ASC")) {
            while (c.moveToNext()) {
                list.add(new RecipeIngredient(
                        c.getLong(c.getColumnIndexOrThrow(RI_RECIPE_ID)),
                        c.getString(c.getColumnIndexOrThrow(RI_NAME)),
                        c.getDouble(c.getColumnIndexOrThrow(RI_QTY)),
                        c.getString(c.getColumnIndexOrThrow(RI_UNIT))));
            }
        }
        return list;
    }

    private Recipe cursorToRecipe(Cursor c) {
        return new Recipe(
                c.getLong(c.getColumnIndexOrThrow(R_ID)),
                c.getString(c.getColumnIndexOrThrow(R_TITLE)),
                c.getString(c.getColumnIndexOrThrow(R_INSTR)));
    }

    // =====================================================================
    //  STRICT-MATCHING RULE (core business logic)
    // =====================================================================

    /**
     * Returns ONLY the recipes for which EVERY required ingredient is in the pantry
     * in at least the required quantity. A recipe missing even one ingredient,
     * or having too little of one, is excluded.
     *
     * Robustness to real-world messiness:
     *  - Case/whitespace/punctuation ignored ("  Tomato " == "tomato").
     *  - Simple singular/plural handling ("tomato" == "tomatoes", "egg" == "eggs").
     *  - Unit conversion within a family: kg<->g, l<->ml, tsp/tbsp/cup<->ml.
     *  - Duplicate pantry rows of the same ingredient are added together.
     */
    public List<Recipe> getSuggestedRecipes() {
        // Step 1: total up the pantry, keyed by "normalisedName|unitFamily", in base units.
        Map<String, Double> pantryTotals = new HashMap<>();
        for (PantryItem item : getAllPantryItems()) {
            String key = matchKey(item.getName(), item.getUnit());
            double baseQty = item.getQuantity() * unitFactor(item.getUnit());
            Double existing = pantryTotals.get(key);
            pantryTotals.put(key, (existing == null ? 0 : existing) + baseQty);
        }

        // Step 2: a recipe qualifies only if ALL of its ingredients pass.
        List<Recipe> suggested = new ArrayList<>();
        for (Recipe recipe : getAllRecipes()) {
            List<RecipeIngredient> needed = getIngredientsForRecipe(recipe.getId());
            if (needed.isEmpty()) {
                continue; // a recipe with no ingredients is never suggested
            }
            boolean canMake = true;
            for (RecipeIngredient ri : needed) {
                String key = matchKey(ri.getIngredientName(), ri.getUnit());
                double requiredBase = ri.getRequiredQuantity() * unitFactor(ri.getUnit());
                Double have = pantryTotals.get(key);
                // missing entirely, or not enough (tiny epsilon avoids floating-point errors)
                if (have == null || have + 1e-9 < requiredBase) {
                    canMake = false;
                    break; // one failure disqualifies the recipe: no partial matches
                }
            }
            if (canMake) {
                suggested.add(recipe);
            }
        }
        return suggested;
    }

    // ---- matching helpers ------------------------------------------------

    private String matchKey(String name, String unit) {
        return normalizeName(name) + "|" + unitFamily(unit);
    }

    /** Lower-cases, strips punctuation, collapses spaces and singularises each word. */
    static String normalizeName(String raw) {
        if (raw == null) return "";
        String cleaned = raw.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z\\s]", " ")
                .trim()
                .replaceAll("\\s+", " ");
        StringBuilder sb = new StringBuilder();
        for (String word : cleaned.split(" ")) {
            if (word.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(singularize(word));
        }
        return sb.toString();
    }

    /** Simple rule-based singulariser (not full NLP, but handles common cases). */
    static String singularize(String w) {
        if (w.length() <= 3) return w;
        if (w.endsWith("ies")) return w.substring(0, w.length() - 3) + "y";   // berries -> berry
        if (w.endsWith("oes")) return w.substring(0, w.length() - 2);          // tomatoes -> tomato
        if (w.endsWith("ss") || w.endsWith("us")) return w;                    // molasses, hummus
        if (w.endsWith("s")) return w.substring(0, w.length() - 1);            // eggs -> egg
        return w;
    }

    /** Returns the unit family: "g" (weight), "ml" (volume), "pcs" (count) or the unit itself. */
    static String unitFamily(String unit) {
        String u = cleanUnit(unit);
        switch (u) {
            case "g": case "gram": case "grams": case "kg": case "kilogram": case "kilograms":
                return "g";
            case "ml": case "millilitre": case "milliliter": case "l": case "litre": case "liter":
            case "litres": case "liters": case "tsp": case "teaspoon": case "teaspoons":
            case "tbsp": case "tablespoon": case "tablespoons": case "cup": case "cups":
                return "ml";
            case "": case "pcs": case "pc": case "piece": case "pieces": case "whole":
            case "item": case "items": case "unit": case "units": case "can": case "cans":
            case "clove": case "cloves": case "slice": case "slices":
                return "pcs";
            default:
                return u; // unknown unit: only matches the identical unit
        }
    }

    /** Multiplier that converts a quantity in this unit to its family's base unit. */
    static double unitFactor(String unit) {
        switch (cleanUnit(unit)) {
            case "kg": case "kilogram": case "kilograms": return 1000.0;
            case "l": case "litre": case "liter": case "litres": case "liters": return 1000.0;
            case "tsp": case "teaspoon": case "teaspoons": return 5.0;
            case "tbsp": case "tablespoon": case "tablespoons": return 15.0;
            case "cup": case "cups": return 240.0;
            default: return 1.0;
        }
    }

    private static String cleanUnit(String unit) {
        return unit == null ? "" : unit.toLowerCase(Locale.ROOT).trim().replace(".", "");
    }

    // =====================================================================
    //  DATABASE SEEDING (16 recipes, inserted on first run only)
    // =====================================================================

    /** Ingredient format: "name|quantity|unit". */
    private void seedRecipes(SQLiteDatabase db) {
        addRecipe(db, "Scrambled Eggs",
                "1. Crack the eggs into a bowl, add salt and whisk.\n2. Melt the butter in a pan on medium heat.\n3. Pour in the eggs and stir gently until just set.\n4. Serve immediately.",
                "egg|3|pcs", "butter|10|g", "salt|1|tsp");

        addRecipe(db, "Tomato Pasta",
                "1. Boil the pasta in salted water until al dente.\n2. Fry the chopped garlic in olive oil for 1 minute.\n3. Add chopped tomatoes and simmer for 10 minutes.\n4. Toss the drained pasta through the sauce.",
                "pasta|200|g", "tomato|4|pcs", "garlic|2|pcs", "olive oil|2|tbsp", "salt|1|tsp");

        addRecipe(db, "Cheese Omelette",
                "1. Whisk the eggs in a bowl.\n2. Melt the butter in a pan and pour in the eggs.\n3. When almost set, sprinkle over the grated cheese.\n4. Fold in half and serve.",
                "egg|3|pcs", "cheese|50|g", "butter|10|g");

        addRecipe(db, "Fluffy Pancakes",
                "1. Mix the flour and sugar in a bowl.\n2. Whisk in the milk and eggs to form a smooth batter.\n3. Melt some butter in a pan and pour in small rounds of batter.\n4. Cook until bubbles appear, flip, and cook until golden.",
                "flour|200|g", "milk|300|ml", "egg|2|pcs", "sugar|2|tbsp", "butter|20|g");

        addRecipe(db, "Egg Fried Rice",
                "1. Cook the rice and let it cool.\n2. Fry the chopped onion in oil until soft.\n3. Push aside, scramble the eggs in the pan.\n4. Add the rice and soy sauce and stir-fry for 3 minutes.",
                "rice|200|g", "egg|2|pcs", "onion|1|pcs", "soy sauce|2|tbsp", "oil|1|tbsp");

        addRecipe(db, "Grilled Cheese Sandwich",
                "1. Butter one side of each bread slice.\n2. Place the cheese between the slices, butter side out.\n3. Grill in a pan on medium heat until golden on both sides.",
                "bread|2|pcs", "cheese|60|g", "butter|10|g");

        addRecipe(db, "Creamy Mashed Potatoes",
                "1. Peel and chop the potatoes, then boil until soft.\n2. Drain and mash.\n3. Stir in the butter, warm milk and salt until smooth.",
                "potato|4|pcs", "butter|30|g", "milk|100|ml", "salt|1|tsp");

        addRecipe(db, "Simple Vegetable Soup",
                "1. Chop the carrots, potatoes and onion.\n2. Add them to a pot with the stock.\n3. Simmer for 25 minutes until tender.\n4. Season with salt and serve.",
                "carrot|2|pcs", "potato|2|pcs", "onion|1|pcs", "vegetable stock|500|ml", "salt|1|tsp");

        addRecipe(db, "Garlic Bread",
                "1. Mash the chopped garlic into the softened butter.\n2. Spread on the bread slices.\n3. Bake at 200 C for 8-10 minutes until crisp.",
                "bread|4|pcs", "butter|40|g", "garlic|3|pcs");

        addRecipe(db, "French Toast",
                "1. Whisk the eggs, milk and sugar together.\n2. Dip each bread slice in the mixture.\n3. Fry in a pan until golden on both sides.",
                "bread|4|pcs", "egg|2|pcs", "milk|100|ml", "sugar|1|tbsp");

        addRecipe(db, "Tuna Sandwich",
                "1. Drain the tuna and mix with the mayonnaise.\n2. Spread onto a slice of bread and top with the second slice.\n3. Cut in half and serve.",
                "bread|2|pcs", "tuna|1|pcs", "mayonnaise|1|tbsp");

        addRecipe(db, "Chicken and Rice",
                "1. Season the chicken with salt and brown it with the chopped onion.\n2. Add the rice and enough water to cover.\n3. Cover and simmer for 20 minutes until the rice is cooked.",
                "chicken|300|g", "rice|200|g", "onion|1|pcs", "salt|1|tsp");

        addRecipe(db, "Honey Oatmeal",
                "1. Combine the oats and milk in a pot.\n2. Cook on low heat for 5 minutes, stirring often.\n3. Drizzle with honey and serve warm.",
                "oats|50|g", "milk|250|ml", "honey|1|tbsp");

        addRecipe(db, "Easy Bean Chilli",
                "1. Fry the chopped onion until soft.\n2. Add the chopped tomatoes, beans and chilli powder.\n3. Simmer for 20 minutes and serve.",
                "bean|400|g", "tomato|3|pcs", "onion|1|pcs", "chilli powder|1|tsp");

        addRecipe(db, "Banana Smoothie",
                "1. Peel the bananas and break into a blender.\n2. Add the milk and honey.\n3. Blend until smooth and serve cold.",
                "banana|2|pcs", "milk|250|ml", "honey|1|tbsp");

        addRecipe(db, "Crispy Potato Wedges",
                "1. Cut the potatoes into wedges.\n2. Toss with olive oil and salt.\n3. Bake at 220 C for 30 minutes, turning once.",
                "potato|3|pcs", "olive oil|2|tbsp", "salt|1|tsp");
    }

    private void addRecipe(SQLiteDatabase db, String title, String instructions,
                           String... ingredients) {
        ContentValues rv = new ContentValues();
        rv.put(R_TITLE, title);
        rv.put(R_INSTR, instructions);
        long recipeId = db.insert(T_RECIPES, null, rv);

        for (String line : ingredients) {
            String[] parts = line.split("\\|");
            ContentValues iv = new ContentValues();
            iv.put(RI_RECIPE_ID, recipeId);
            iv.put(RI_NAME, parts[0]);
            iv.put(RI_QTY, Double.parseDouble(parts[1]));
            iv.put(RI_UNIT, parts[2]);
            db.insert(T_RI, null, iv);
        }
    }
}
