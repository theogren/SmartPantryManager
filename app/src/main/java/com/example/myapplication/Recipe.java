package com.example.myapplication;

/**
 * Model class representing a recipe (title + preparation steps).
 * Its ingredients are stored separately as RecipeIngredient rows.
 */
public class Recipe {
    private long id;
    private String title;
    private String instructions;

    public Recipe() { }

    public Recipe(long id, String title, String instructions) {
        this.id = id;
        this.title = title;
        this.instructions = instructions;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
}
