package com.example.myapplication;

/**
 * Model class representing one ingredient the user currently has in their pantry.
 */
public class PantryItem {
    private long id;
    private String name;
    private double quantity;
    private String unit;

    public PantryItem() { }

    public PantryItem(String name, double quantity, String unit) {
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
    }

    public PantryItem(long id, String name, double quantity, String unit) {
        this(name, quantity, unit);
        this.id = id;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
