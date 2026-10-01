package com.example.myapplication;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Add / Edit Ingredient screen.
 * If an item id arrives in the Intent, the form is pre-filled and saving UPDATEs that row;
 * otherwise saving CREATEs a new one. All input is validated before saving.
 */
public class AddIngredientActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "extra_item_id";

    private DatabaseHelper db;
    private EditText etName;
    private EditText etQuantity;
    private Spinner spUnit;
    private PantryItem editingItem; // null when adding a new ingredient

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ingredient);
        NavHelper.applyInsets(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = new DatabaseHelper(this);
        etName = findViewById(R.id.etName);
        etQuantity = findViewById(R.id.etQuantity);
        spUnit = findViewById(R.id.spUnit);
        Button btnSave = findViewById(R.id.btnSave);
        Button btnCancel = findViewById(R.id.btnCancel);

        ArrayAdapter<CharSequence> unitAdapter = ArrayAdapter.createFromResource(
                this, R.array.units, android.R.layout.simple_spinner_item);
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUnit.setAdapter(unitAdapter);

        // Edit mode? Read the id sent by MainActivity.
        long itemId = getIntent().getLongExtra(EXTRA_ITEM_ID, -1);
        if (itemId != -1) {
            editingItem = db.getPantryItem(itemId);
        }

        if (editingItem != null) {
            setTitle("Edit Ingredient");
            etName.setText(editingItem.getName());
            etQuantity.setText(FormatUtil.qty(editingItem.getQuantity()));
            spUnit.setSelection(indexOfUnit(unitAdapter, editingItem.getUnit()));
            btnSave.setText("Update");
        } else {
            setTitle("Add Ingredient");
        }

        btnSave.setOnClickListener(v -> saveIngredient());
        btnCancel.setOnClickListener(v -> finish());
    }

    private int indexOfUnit(ArrayAdapter<CharSequence> adapter, String unit) {
        for (int i = 0; i < adapter.getCount(); i++) {
            CharSequence entry = adapter.getItem(i);
            if (entry != null && entry.toString().equalsIgnoreCase(unit)) {
                return i;
            }
        }
        return 0;
    }

    private void saveIngredient() {
        String name = etName.getText().toString().trim();
        String qtyText = etQuantity.getText().toString().trim().replace(',', '.');
        String unit = spUnit.getSelectedItem().toString();

        // ---- Input validation ----
        boolean valid = true;

        if (name.isEmpty()) {
            etName.setError("Please enter an ingredient name");
            valid = false;
        } else if (!name.matches(".*[A-Za-z].*")) {
            etName.setError("Name must contain letters");
            valid = false;
        } else if (name.length() > 40) {
            etName.setError("Name is too long (max 40 characters)");
            valid = false;
        }

        double quantity = 0;
        if (qtyText.isEmpty()) {
            etQuantity.setError("Please enter a quantity");
            valid = false;
        } else {
            try {
                quantity = Double.parseDouble(qtyText);
                if (quantity <= 0) {
                    etQuantity.setError("Quantity must be greater than 0");
                    valid = false;
                } else if (quantity > 100000) {
                    etQuantity.setError("Quantity is too large");
                    valid = false;
                }
            } catch (NumberFormatException e) {
                etQuantity.setError("Enter a valid number");
                valid = false;
            }
        }

        if (!valid) {
            return; // stay on the form so the user can fix the errors
        }

        // ---- Persist ----
        if (editingItem != null) {
            editingItem.setName(name);
            editingItem.setQuantity(quantity);
            editingItem.setUnit(unit);
            db.updatePantryItem(editingItem);
            Toast.makeText(this, "Ingredient updated", Toast.LENGTH_SHORT).show();
        } else {
            db.addPantryItem(new PantryItem(name, quantity, unit));
            Toast.makeText(this, "Ingredient added", Toast.LENGTH_SHORT).show();
        }
        finish(); // back to the pantry list, which reloads in onResume()
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
