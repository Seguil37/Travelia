package com.proyecto.travelia.ui;

import android.content.Context;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * ItemDecoration simple para aplicar el mismo espaciado en todos los lados.
 */
public class SpacingItemDecoration extends RecyclerView.ItemDecoration {
    private final int spacingPx;

    public SpacingItemDecoration(Context context, float spacingDp) {
        spacingPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                spacingDp,
                context.getResources().getDisplayMetrics()
        );
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        outRect.left = spacingPx;
        outRect.right = spacingPx;
        outRect.top = spacingPx;
        outRect.bottom = spacingPx;
    }
}
