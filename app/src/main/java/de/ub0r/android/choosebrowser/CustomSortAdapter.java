package de.ub0r.android.choosebrowser;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Adapter for custom browser sorting with drag-and-drop support.
 * Displays two sections: Selected browsers and Other browsers.
 */
public class CustomSortAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_BROWSER = 1;

    private final List<Object> items = new ArrayList<>();
    private final List<BrowserInfo> selectedBrowsers = new ArrayList<>();
    private final List<BrowserInfo> unselectedBrowsers = new ArrayList<>();
    private final String selectedHeader;
    private final String unselectedHeader;

    private ItemTouchHelper touchHelper;
    private OnDataChangedListener dataChangedListener;

    public interface OnDataChangedListener {
        void onDataChanged();
    }

    public CustomSortAdapter(@NonNull String selectedHeader, @NonNull String unselectedHeader) {
        this.selectedHeader = selectedHeader;
        this.unselectedHeader = unselectedHeader;
    }

    public void setTouchHelper(@NonNull ItemTouchHelper touchHelper) {
        this.touchHelper = touchHelper;
    }

    public void setOnDataChangedListener(@Nullable OnDataChangedListener listener) {
        this.dataChangedListener = listener;
    }

    public void setData(@NonNull List<BrowserInfo> selected, @NonNull List<BrowserInfo> unselected) {
        this.selectedBrowsers.clear();
        this.selectedBrowsers.addAll(selected);
        this.unselectedBrowsers.clear();
        this.unselectedBrowsers.addAll(unselected);
        rebuildItems();
    }

    private void rebuildItems() {
        items.clear();
        items.add(selectedHeader);
        items.addAll(selectedBrowsers);
        items.add(unselectedHeader);
        items.addAll(unselectedBrowsers);
        notifyDataSetChanged();
    }

    @NonNull
    public List<BrowserInfo> getSelectedBrowsers() {
        return new ArrayList<>(selectedBrowsers);
    }

    @NonNull
    public List<BrowserInfo> getUnselectedBrowsers() {
        return new ArrayList<>(unselectedBrowsers);
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_BROWSER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View view = inflater.inflate(R.layout.item_section_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_browser_drag, parent, false);
            return new BrowserViewHolder(view);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).bind((String) items.get(position));
        } else if (holder instanceof BrowserViewHolder) {
            BrowserViewHolder browserHolder = (BrowserViewHolder) holder;
            browserHolder.bind((BrowserInfo) items.get(position));

            // Setup drag handle touch listener
            browserHolder.dragHandle.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                    if (touchHelper != null) {
                        touchHelper.startDrag(holder);
                    }
                }
                return false;
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Move an item from one position to another during drag.
     * Uses notifyItemMoved for smooth animation without interrupting the drag.
     */
    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) return;

        Object fromItem = items.get(fromPosition);
        if (!(fromItem instanceof BrowserInfo)) return;

        Object toItem = items.get(toPosition);

        // If target is a header, handle specially
        if (toItem instanceof String) {
            String header = (String) toItem;
            // Remove the item first
            items.remove(fromPosition);

            // Find the header position after removal
            int headerPos = items.indexOf(header);

            int newPosition;
            if (fromPosition < toPosition) {
                // Moving down past a header - insert after the header
                newPosition = headerPos + 1;
            } else {
                // Moving up to a header - insert just before the header
                newPosition = headerPos;
            }

            // Clamp to valid range
            newPosition = Math.max(0, Math.min(newPosition, items.size()));
            items.add(newPosition, fromItem);

            // Notify the move
            notifyItemMoved(fromPosition, newPosition);

            if (dataChangedListener != null) {
                dataChangedListener.onDataChanged();
            }
            return;
        }

        // Normal move between browser items
        items.remove(fromPosition);
        items.add(toPosition, fromItem);
        notifyItemMoved(fromPosition, toPosition);

        if (dataChangedListener != null) {
            dataChangedListener.onDataChanged();
        }
    }

    /**
     * Rebuild the selectedBrowsers and unselectedBrowsers lists from the items list.
     * Should be called when drag ends.
     */
    public void syncListsFromItems() {
        selectedBrowsers.clear();
        unselectedBrowsers.clear();

        boolean inSelectedSection = true;
        for (Object item : items) {
            if (item instanceof String) {
                // This is a header
                if (item.equals(unselectedHeader)) {
                    inSelectedSection = false;
                }
            } else if (item instanceof BrowserInfo) {
                if (inSelectedSection) {
                    selectedBrowsers.add((BrowserInfo) item);
                } else {
                    unselectedBrowsers.add((BrowserInfo) item);
                }
            }
        }
    }

    /**
     * Check if an item at the given position can be moved.
     */
    public boolean canMoveItem(int position) {
        return position >= 0 && position < items.size() && items.get(position) instanceof BrowserInfo;
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.section_title);
        }

        void bind(@NonNull String title) {
            titleView.setText(title);
        }
    }

    static class BrowserViewHolder extends RecyclerView.ViewHolder {
        final ImageView dragHandle;
        private final ImageView iconView;
        private final TextView nameView;

        BrowserViewHolder(@NonNull View itemView) {
            super(itemView);
            dragHandle = itemView.findViewById(R.id.drag_handle);
            iconView = itemView.findViewById(R.id.browser_icon);
            nameView = itemView.findViewById(R.id.browser_name);
        }

        void bind(@NonNull BrowserInfo browser) {
            iconView.setImageDrawable(browser.getIcon());
            nameView.setText(browser.getLabel());
        }
    }

    /**
     * ItemTouchHelper.Callback for drag and drop support.
     */
    public static class DragCallback extends ItemTouchHelper.Callback {
        private final CustomSortAdapter adapter;

        public DragCallback(@NonNull CustomSortAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder) {
            if (!adapter.canMoveItem(viewHolder.getAdapterPosition())) {
                return 0;
            }
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            return makeMovementFlags(dragFlags, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder source,
                              @NonNull RecyclerView.ViewHolder target) {
            adapter.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            // No swipe support
        }

        @Override
        public void onSelectedChanged(@Nullable RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            // When drag ends, sync the internal lists
            if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                adapter.syncListsFromItems();
            }
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true; // Allow long press drag on any part of the item
        }
    }
}
