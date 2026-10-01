package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Pantry List screen (launcher Activity).
 * READs all pantry items into a RecyclerView, and launches the Add/Edit screen
 * (CREATE / UPDATE) or deletes items (DELETE).
 */
public class MainActivity extends AppCompatActivity
        implements PantryAdapter.OnPantryItemActionListener {

    private DatabaseHelper db;
    private PantryAdapter adapter;
    private RecyclerView rvPantry;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NavHelper.applyInsets(this);

        db = new DatabaseHelper(this);

        rvPantry = findViewById(R.id.rvPantry);
        tvEmpty = findViewById(R.id.tvEmpty);

        rvPantry.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PantryAdapter(new ArrayList<>(), this);
        rvPantry.setAdapter(adapter);

        // Explicit Intent: open the form in "add" mode (no item id passed).
        findViewById(R.id.btnAddIngredient).setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AddIngredientActivity.class)));

        NavHelper.setupBottomNav(this, NavHelper.PANTRY);
    }

    /** Reload every time we return, so adds/edits/deletes show immediately. */
    @Override
    protected void onResume() {
        super.onResume();
        loadPantry();
    }

    private void loadPantry() {
        List<PantryItem> items = db.getAllPantryItems();
        adapter.setItems(items);

        boolean empty = items.isEmpty();
        tvEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvPantry.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    /** Row tapped: open the same form in "edit" mode, passing the item id via an Intent extra. */
    @Override
    public void onEditItem(PantryItem item) {
        Intent intent = new Intent(this, AddIngredientActivity.class);
        intent.putExtra(AddIngredientActivity.EXTRA_ITEM_ID, item.getId());
        startActivity(intent);
    }

    @Override
    public void onDeleteItem(final PantryItem item) {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        boolean confirm = prefs.getBoolean(SettingsActivity.KEY_CONFIRM_DELETE, true);

        if (confirm) {
            new AlertDialog.Builder(this)
                    .setTitle("Delete ingredient")
                    .setMessage("Remove " + FormatUtil.capitalize(item.getName()) + " from your pantry?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteItem(item))
                    .setNegativeButton("Cancel", null)
                    .show();
        } else {
            deleteItem(item);
        }
    }

    private void deleteItem(PantryItem item) {
        db.deletePantryItem(item.getId());
        Toast.makeText(this, "Ingredient removed", Toast.LENGTH_SHORT).show();
        loadPantry();
    }
}
