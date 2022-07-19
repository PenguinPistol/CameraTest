package me.penguinpistol.cameratest.common;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ListSpacingDecoration extends RecyclerView.ItemDecoration {
    private final float itemSpacing;
    private final float firstSpacing;
    private final float lastSpacing;

    /**
     * @param spacing DP
     */
    public ListSpacingDecoration(float spacing) {
        this(spacing, 0, 0);
    }

    /**
     * @param spacing DP
     * @param firstSpacing DP
     * @param lastSpacing DP
     */
    public ListSpacingDecoration(float spacing, float firstSpacing, float lastSpacing) {
        this.itemSpacing = spacing;
        this.firstSpacing = firstSpacing;
        this.lastSpacing = lastSpacing;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        float density = parent.getResources().getDisplayMetrics().density;
        float spacing = parent.getChildAdapterPosition(view) == 0 ? firstSpacing : itemSpacing;
        LinearLayoutManager lm = (LinearLayoutManager)parent.getLayoutManager();
        RecyclerView.Adapter adapter = parent.getAdapter();

        if(lm != null && adapter != null) {
            if (lm.getOrientation() == LinearLayoutManager.VERTICAL) {
                outRect.top += spacing * density;
                if (parent.getChildAdapterPosition(view) == adapter.getItemCount() - 1) {
                    outRect.bottom += lastSpacing * density;
                }
            } else {
                outRect.left += spacing * density;
                if (parent.getChildAdapterPosition(view) == adapter.getItemCount() - 1) {
                    outRect.right += lastSpacing * density;
                }
            }
        }
    }
}
