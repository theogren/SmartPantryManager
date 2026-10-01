package com.example.myapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

/**
 * Settings screen.
 *  - Toggle whether deleting a pantry item asks for confirmation (saved in SharedPreferences).
 *  - Clear the whole pantry (with confirmation).
 */
public class SettingsActivity extends AppCompatActivity {

    public static final String PREFS_NAME = "smart_pantry_prefs";
    public static final String KEY_CONFIRM_DELETE = "confirm_delete";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        NavHelper.applyInsets(this);

        final DatabaseHelper db = new DatabaseHelper(this);
        final SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        SwitchCompat switchConfirm = findViewById(R.id.switchConfirmDelete);
        switchConfirm.setChecked(prefs.getBoolean(KEY_CONFIRM_DELETE, true));
        switchConfirm.setOnCheckedChangeListener((button, isChecked) ->
                prefs.edit().putBoolean(KEY_CONFIRM_DELETE, isChecked).apply());

        findViewById(R.id.btnClearPantry).setOnClickListener(v ->
                new AlertDialog.Builder(SettingsActivity.this)
                        .setTitle("Clear pantry")
                        .setMessage("This removes ALL ingredients from your pantry. Continue?")
                        .setPositiveButton("Clear all", (dialog, which) -> {
                            db.clearPantry();
                            Toast.makeText(SettingsActivity.this,
                                    "Pantry cleared", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show());

        NavHelper.setupBottomNav(this, NavHelper.SETTINGS);
    }
}
