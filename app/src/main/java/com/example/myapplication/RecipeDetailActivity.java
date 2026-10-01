package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/** Recipe Detail screen: full ingredient list and method for the selected recipe. */
public class RecipeDetailActivity extends AppCompatActivity {

    public static final String EXTRA_RECIPE_ID = "extra_recipe_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        NavHelper.applyInsets(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        DatabaseHelper db = new DatabaseHelper(this);
        long recipeId = getIntent().getLongExtra(EXTRA_RECIPE_ID, -1);
        Recipe recipe = db.getRecipe(recipeId);

        if (recipe == null) {
            Toast.makeText(this, "Recipe not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setTitle(recipe.getTitle());
        ((TextView) findViewById(R.id.tvRecipeTitle)).setText(recipe.getTitle());
        ((TextView) findViewById(R.id.tvMethod)).setText(recipe.getInstructions());

        StringBuilder sb = new StringBuilder();
        List<RecipeIngredient> ingredients = db.getIngredientsForRecipe(recipeId);
        for (RecipeIngredient ri : ingredients) {
            sb.append("• ")
                    .append(FormatUtil.capitalize(ri.getIngredientName()))
                    .append(" — ")
                    .append(FormatUtil.qty(ri.getRequiredQuantity()))
                    .append(" ")
                    .append(ri.getUnit())
                    .append("\n");
        }
        ((TextView) findViewById(R.id.tvIngredients)).setText(sb.toString().trim());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
