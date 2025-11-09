package com.proyecto.travelia.ui;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.proyecto.travelia.ComprarActivity;
import com.proyecto.travelia.ExplorarActivity;
import com.proyecto.travelia.InicioActivity;
import com.proyecto.travelia.R;
import com.proyecto.travelia.favoritos.FavoritosActivity;

public class BottomNavView extends CardView {

    public enum Tab { HOME, EXPLORAR, ADD, FAVORITES, RESERVE }

    private View navHome, navExplorar, navAdd, navFavorites, navReserve;
    private boolean finishOnNavigate = true;

    public BottomNavView(Context context) { super(context); init(context, null); }
    public BottomNavView(Context context, @Nullable AttributeSet attrs) { super(context, attrs); init(context, attrs); }
    public BottomNavView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) { super(context, attrs, defStyleAttr); init(context, attrs); }

    private void init(Context ctx, @Nullable AttributeSet attrs) {
        LayoutInflater.from(ctx).inflate(R.layout.view_bottom_nav, this, true);

        navHome      = findViewById(R.id.nav_home);
        navExplorar  = findViewById(R.id.nav_explorar);
        navAdd       = findViewById(R.id.nav_add);
        navFavorites = findViewById(R.id.nav_favorites);
        navReserve   = findViewById(R.id.nav_reserve);

        // Leer attrs
        if (attrs != null) {
            final int[] set = { R.attr.currentTab, R.attr.finishOnNavigate };
            final android.content.res.TypedArray a =
                    ctx.obtainStyledAttributes(attrs, R.styleable.BottomNavView);
            int tabIndex = a.getInt(R.styleable.BottomNavView_currentTab, 0);
            finishOnNavigate = a.getBoolean(R.styleable.BottomNavView_finishOnNavigate, true);
            a.recycle();
            highlight(Tab.values()[tabIndex]);
        }

        // Inset bottom (IME + system bars)
        ViewCompat.setOnApplyWindowInsetsListener(this, (v, insets) -> {
            Insets sys = insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.ime());
            ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            if (lp != null) {
                lp.bottomMargin = dp(10) + sys.bottom;
                v.setLayoutParams(lp);
            }
            return insets;
        });

        wireDefaultNavigation(ctx);
    }

    private void wireDefaultNavigation(Context ctx) {
        navHome.setOnClickListener(v -> navigate(ctx, InicioActivity.class));
        navExplorar.setOnClickListener(v -> navigate(ctx, ExplorarActivity.class));
        navAdd.setOnClickListener(v -> { /* acción pendiente */ });
        navFavorites.setOnClickListener(v -> navigate(ctx, com.proyecto.travelia.favoritos.FavoritosActivity.class));
        navReserve.setOnClickListener(v -> navigate(ctx, ComprarActivity.class));
    }

    private void navigate(Context ctx, Class<?> target) {
        if (ctx.getClass() == target) return; // ya estás en esa Activity
        ctx.startActivity(new Intent(ctx, target));
        if (finishOnNavigate && ctx instanceof android.app.Activity) {
            ((android.app.Activity) ctx).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            ((android.app.Activity) ctx).finish();
        }
    }

    public void highlight(Tab tab) {
        // Marca visual de la pestaña activa (por simplicidad: cambia alpha de los demás)
        float active = 1f, inactive = 0.6f;
        navHome.setAlpha(inactive);
        navExplorar.setAlpha(inactive);
        navAdd.setAlpha(inactive);
        navFavorites.setAlpha(inactive);
        navReserve.setAlpha(inactive);
        switch (tab) {
            case HOME:      navHome.setAlpha(active); break;
            case EXPLORAR:  navExplorar.setAlpha(active); break;
            case ADD:       navAdd.setAlpha(active); break;
            case FAVORITES: navFavorites.setAlpha(active); break;
            case RESERVE:   navReserve.setAlpha(active); break;
        }
    }

    public void setFinishOnNavigate(boolean finish) { this.finishOnNavigate = finish; }

    // Permite sobrescribir la acción del botón Add si luego quieres algo especial
    public void setOnAddClickListener(@Nullable OnClickListener l) {
        navAdd.setOnClickListener(l != null ? l : v -> { /* default vacía */ });
    }

    private int dp(int value) {
        float d = getResources().getDisplayMetrics().density;
        return Math.round(value * d);
    }
}
