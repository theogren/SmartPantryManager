package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.View;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

/**
 * Wires up the shared bottom navigation bar (layout_bottom_nav.xml) using Intents,
 * and keeps content clear of the system navigation bar on newer Android versions.
 */
public final class NavHelper {

    public static final int PANTRY = 0;
    public static final int SUGGESTED = 1;
    public static final int SETTINGS = 2;

    private NavHelper() { }

    /** Highlights the selected tab and makes each tab launch its screen via an Intent. */
    public static void setupBottomNav(final Activity activity, final int selected) {
        TextView navPantry = activity.findViewById(R.id.navPantry);
        TextView navSuggested = activity.findViewById(R.id.navSuggested);
        TextView navSettings = activity.findViewById(R.id.navSettings);

        styleTab(activity, navPantry, selected == PANTRY);
        styleTab(activity, navSuggested, selected == SUGGESTED);
        styleTab(activity, navSettings, selected == SETTINGS);

        navPantry.setOnClickListener(v ->
                navigate(activity, MainActivity.class, selected == PANTRY));
        navSuggested.setOnClickListener(v ->
                navigate(activity, SuggestedRecipesActivity.class, selected == SUGGESTED));
        navSettings.setOnClickListener(v ->
                navigate(activity, SettingsActivity.class, selected == SETTINGS));
    }

    private static void styleTab(Activity activity, TextView tab, boolean active) {
        int colour = ContextCompat.getColor(activity,
                active ? R.color.green_primary : R.color.text_secondary);
        tab.setTextColor(colour);
        tab.setTypeface(null, active ? Typeface.BOLD : Typeface.NORMAL);
    }

    private static void navigate(Activity from, Class<?> target, boolean alreadyHere) {
        if (alreadyHere) return;

        Intent intent = new Intent(from, target);
        if (target == MainActivity.class) {
            // Pantry is the root screen: return to it instead of stacking a new copy.
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        }
        from.startActivity(intent);
        from.overridePendingTransition(0, 0);

        // Only the Pantry screen stays alive underneath; other tabs replace each other.
        if (!(from instanceof MainActivity)) {
            from.finish();
        }
    }

    /** Pads the screen so content is never hidden behind the system navigation bar. */
    public static void applyInsets(Activity activity) {
        View content = activity.findViewById(android.R.id.content);
        ViewCompat.setOnApplyWindowInsetsListener(content, (view, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
            view.setPadding(bars.left, 0, bars.right, bars.bottom);
            return insets;
        });
    }
}
