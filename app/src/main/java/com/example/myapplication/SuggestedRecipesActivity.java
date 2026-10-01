package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Suggested Recipes screen.
 * Shows ONLY recipes that pass the Strict-Matching Rule
 * (DatabaseHelper.getSuggestedRecipes()), or a friendly message if none do.
 */
public class SuggestedRecipesActivity extends AppCompatActivity
        implements RecipeAdapter.OnRecipeClickListener {

    private DatabaseHelper db;
    private RecipeAdapter adapter;
    private RecyclerView rvRecipes;
    private TextView tvNoRecipes;
    private TextView tvHeader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggested_recipes);
        NavHelper.applyInsets(this);

        db = new DatabaseHelper(this);
        rvRecipes = findViewById(R.id.rvRecipes);
        tvNoRecipes = findViewById(R.id.tvNoRecipes);
        tvHeader = findViewById(R.id.tvSuggestedHeader);

        rvRecipes.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RecipeAdapter(new ArrayList<>(), this);
        rvRecipes.setAdapter(adapter);

        NavHelper.setupBottomNav(this, NavHelper.SUGGESTED);
    }

    /** Re-run the matching every time the screen appears, so it reflects the current pantry. */
    @Override
    protected void onResume() {
        super.onResume();
        List<Recipe> matches = db.getSuggestedRecipes();
        adapter.setRecipes(matches);

        if (matches.isEmpty()) {
            tvHeader.setText("Recipes you can make now");
            tvNoRecipes.setVisibility(View.VISIBLE);
            rvRecipes.setVisibility(View.GONE);
        } else {
            tvHeader.setText("You can make " + matches.size()
                    + (matches.size() == 1 ? " recipe" : " recipes") + " right now");
            tvNoRecipes.setVisibility(View.GONE);
            rvRecipes.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRecipeClick(Recipe recipe) {
        Intent intent = new Intent(this, RecipeDetailActivity.class);
        intent.putExtra(RecipeDetailActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
