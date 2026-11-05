package com.relax.tvlauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Pantalla de inicio: muestra directamente todas las apps (sin cajón de apps).
 * Pensado para equipos con poca memoria: una sola actividad, sin librerías de
 * imágenes, carga de iconos en un hilo de fondo y fondo animado muy ligero.
 */
public class MainActivity extends Activity implements AppAdapter.Listener {

    private RecyclerView grid;
    private AppAdapter adapter;
    private TextView greeting;
    private AnimationDrawable background;
    private boolean firstLoad = true;

    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private final BroadcastReceiver packageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadApps();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        View root = findViewById(R.id.root);
        root.setBackgroundResource(R.drawable.bg_anim);
        background = (AnimationDrawable) root.getBackground();
        background.setEnterFadeDuration(6000);
        background.setExitFadeDuration(6000);

        greeting = findViewById(R.id.greeting);
        ((TextClock) findViewById(R.id.date)).setFormat24Hour("EEEE, d 'de' MMMM");
        ((TextClock) findViewById(R.id.date)).setFormat12Hour("EEEE, d 'de' MMMM");

        findViewById(R.id.btn_settings).setOnClickListener(v -> openSettings());

        DisplayMetrics dm = getResources().getDisplayMetrics();
        float widthDp = dm.widthPixels / dm.density;
        int span = Math.max(4, Math.min(8, (int) ((widthDp - 120) / 140)));

        adapter = new AppAdapter(this);
        grid = findViewById(R.id.grid);
        grid.setLayoutManager(new GridLayoutManager(this, span));
        grid.setHasFixedSize(true);
        grid.setItemAnimator(null);
        grid.setItemViewCacheSize(12);
        grid.setAdapter(adapter);

        loadApps();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter f = new IntentFilter();
        f.addAction(Intent.ACTION_PACKAGE_ADDED);
        f.addAction(Intent.ACTION_PACKAGE_REMOVED);
        f.addAction(Intent.ACTION_PACKAGE_REPLACED);
        f.addDataScheme("package");
        registerReceiver(packageReceiver, f);
        if (background != null) background.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGreeting();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(packageReceiver);
        if (background != null) background.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        io.shutdownNow();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Botón Inicio pulsado: volver al principio de la lista
        if (grid != null) grid.scrollToPosition(0);
    }

    @Override
    public void onBackPressed() {
        // Un launcher no debe cerrarse con "Atrás".
    }

    private void updateGreeting() {
        int h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int res;
        if (h < 6) res = R.string.greeting_night;
        else if (h < 12) res = R.string.greeting_morning;
        else if (h < 20) res = R.string.greeting_afternoon;
        else res = R.string.greeting_night;
        greeting.setText(res);
    }

    private void loadApps() {
        io.execute(() -> {
            PackageManager pm = getPackageManager();
            Map<String, AppEntry> map = new LinkedHashMap<>();
            String[] categories = {Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER};
            for (String cat : categories) {
                Intent query = new Intent(Intent.ACTION_MAIN).addCategory(cat);
                for (ResolveInfo ri : pm.queryIntentActivities(query, 0)) {
                    String pkg = ri.activityInfo.packageName;
                    if (pkg.equals(getPackageName()) || map.containsKey(pkg)) continue;
                    AppEntry e = new AppEntry();
                    e.packageName = pkg;
                    e.label = String.valueOf(ri.loadLabel(pm));
                    e.component = new android.content.ComponentName(pkg, ri.activityInfo.name);
                    e.icon = ri.loadIcon(pm);
                    map.put(pkg, e);
                }
            }
            List<AppEntry> list = new ArrayList<>(map.values());
            Collections.sort(list, new Comparator<AppEntry>() {
                @Override
                public int compare(AppEntry a, AppEntry b) {
                    return a.label.compareToIgnoreCase(b.label);
                }
            });
            main.post(() -> {
                if (isDestroyed()) return;
                adapter.setItems(list);
                if (firstLoad) {
                    firstLoad = false;
                    grid.postDelayed(() -> {
                        View first = grid.getChildAt(0);
                        if (first != null) first.requestFocus();
                    }, 200);
                }
            });
        });
    }

    private void openSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(this, R.string.cannot_open, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAppClick(AppEntry entry) {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setComponent(entry.component);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        try {
            startActivity(i);
        } catch (Exception ex) {
            Toast.makeText(this, R.string.cannot_open, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAppLongClick(AppEntry entry) {
        final String[] options = {
                getString(R.string.action_open),
                getString(R.string.action_info),
                getString(R.string.action_uninstall)
        };
        new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                .setTitle(entry.label)
                .setItems(options, (dialog, which) -> {
                    Uri uri = Uri.parse("package:" + entry.packageName);
                    try {
                        if (which == 0) {
                            onAppClick(entry);
                        } else if (which == 1) {
                            startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri));
                        } else {
                            startActivity(new Intent(Intent.ACTION_DELETE, uri));
                        }
                    } catch (Exception ex) {
                        Toast.makeText(this, R.string.cannot_open, Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }
}
