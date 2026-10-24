package fr.philips.minimal;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends Activity {
    private LinearLayout root;
    private TextView status;
    private int selected = 0;
    private View[] tiles;
    private final String[] labels = {"YouTube", "Netflix", "Prime Video", "Disney+", "Source"};
    private final String[] packages = {
        "com.google.android.youtube.tv",
        "com.netflix.ninja",
        "com.amazon.amazonvideo.livingroom",
        "com.disney.disneyplus",
        ""
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        buildUi();
    }

    private void buildUi() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(48), dp(26), dp(48), dp(24));
        root.setBackgroundColor(Color.BLACK);
        setContentView(root);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView brand = text("PHILIPS MINIMAL", 15, Color.LTGRAY, true);
        brand.setLetterSpacing(0.18f);
        top.addView(brand, new LinearLayout.LayoutParams(0, dp(42), 1));
        TextClock clock = new TextClock(this);
        clock.setFormat24Hour("HH:mm");
        clock.setTextColor(Color.WHITE);
        clock.setTextSize(23);
        clock.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
        top.addView(clock);
        top.addView(spacer(24));
        TextView settings = text("⚙  Paramètres", 15, Color.WHITE, false);
        settings.setFocusable(true); settings.setOnClickListener(v -> openSettings());
        top.addView(settings);
        top.addView(spacer(20));
        TextView home = text("⌂", 27, Color.WHITE, false);
        home.setContentDescription("Accueil Philips"); home.setFocusable(true);
        home.setOnClickListener(v -> goHome());
        top.addView(home);
        root.addView(top);

        TextView heading = text("Que souhaitez-vous regarder ?", 28, Color.WHITE, false);
        heading.setPadding(0, dp(45), 0, dp(28));
        root.addView(heading);

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        root.addView(row, new LinearLayout.LayoutParams(-1, dp(145)));
        tiles = new View[labels.length];
        for (int i=0; i<labels.length; i++) {
            final int index = i;
            TextView tile = text(labels[i], 18, Color.WHITE, true);
            tile.setGravity(Gravity.CENTER);
            tile.setFocusable(true);
            tile.setClickable(true);
            tile.setBackground(background(false));
            tile.setOnFocusChangeListener((v, focused) -> v.setBackground(background(focused)));
            tile.setOnClickListener(v -> launch(index));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, dp(116), 1);
            if (i > 0) lp.leftMargin = dp(12);
            row.addView(tile, lp);
            tiles[i] = tile;
        }
        status = text("Sélectionnez une application avec les flèches et OK.", 14, 0xFF888888, false);
        status.setPadding(0, dp(30), 0, 0);
        root.addView(status);
        tiles[0].requestFocus();
    }

    private GradientDrawable background(boolean focused) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(focused ? 0xFF292929 : 0xFF101010);
        d.setCornerRadius(dp(8));
        d.setStroke(dp(focused ? 2 : 1), focused ? Color.WHITE : 0xFF333333);
        return d;
    }
    private TextView text(String s, int size, int color, boolean bold) {
        TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        t.setTypeface(Typeface.create("sans-serif", bold ? Typeface.BOLD : Typeface.NORMAL));
        return t;
    }
    private View spacer(int width) { View v = new View(this); v.setLayoutParams(new LinearLayout.LayoutParams(dp(width), 1)); return v; }
    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + 0.5f); }

    private void launch(int index) {
        if (index == 4) { openSources(); return; }
        Intent launch = getPackageManager().getLaunchIntentForPackage(packages[index]);
        if (launch == null) {
            status.setText(labels[index] + " n'est pas trouvée sur la télévision.");
            Toast.makeText(this, labels[index] + " n'est pas installée ou n'est pas compatible.", Toast.LENGTH_LONG).show();
            return;
        }
        try { startActivity(launch); }
        catch (ActivityNotFoundException e) { status.setText("Impossible d'ouvrir " + labels[index] + "."); }
    }

    private void openSettings() {
        try { startActivity(new Intent(Settings.ACTION_SETTINGS)); }
        catch (Exception e) { Toast.makeText(this, "Ouvrez les réglages avec la télécommande Philips.", Toast.LENGTH_LONG).show(); }
    }

    private void openSources() {
        // Philips does not document a public Android intent for its proprietary source picker.
        // Try a standard TV input picker; if unsupported, explain the reliable remote shortcut.
        try {
            Intent i = new Intent("android.intent.action.TV_INPUT");
            startActivity(i);
        } catch (Exception e) {
            new AlertDialog.Builder(this).setTitle("Sources HDMI")
                .setMessage("Cette version du système Philips ne donne pas forcément accès à la liste des sources aux applications. Utilisez la touche SOURCES de la télécommande pour choisir HDMI ou TV.")
                .setPositiveButton("OK", null).show();
        }
    }

    private void goHome() {
        try { startActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)); }
        catch (Exception e) { finish(); }
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) { openSettings(); return true; }
        return super.onKeyDown(keyCode, event);
    }
}
