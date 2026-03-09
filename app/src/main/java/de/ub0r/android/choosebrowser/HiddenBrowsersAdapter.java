package de.ub0r.android.choosebrowser;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Adapter for displaying browsers with visibility toggle.
 */
public class HiddenBrowsersAdapter extends RecyclerView.Adapter<HiddenBrowsersAdapter.ViewHolder> {

    private final List<BrowserInfo> browsers = new ArrayList<>();
    private final Set<String> hiddenBrowsers;
    private final OnToggleListener listener;

    public interface OnToggleListener {
        void onToggle(BrowserInfo browser, boolean isVisible);
    }

    public HiddenBrowsersAdapter(@NonNull Set<String> hiddenBrowsers, @NonNull OnToggleListener listener) {
        this.hiddenBrowsers = hiddenBrowsers;
        this.listener = listener;
    }

    public void setBrowsers(@NonNull List<BrowserInfo> browsers) {
        this.browsers.clear();
        this.browsers.addAll(browsers);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_browser_toggle, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BrowserInfo browser = browsers.get(position);
        holder.bind(browser);
    }

    @Override
    public int getItemCount() {
        return browsers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconView;
        private final TextView nameView;
        private final SwitchMaterial toggleView;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            iconView = itemView.findViewById(R.id.browser_icon);
            nameView = itemView.findViewById(R.id.browser_name);
            toggleView = itemView.findViewById(R.id.browser_toggle);
        }

        void bind(@NonNull BrowserInfo browser) {
            iconView.setImageDrawable(browser.getIcon());
            nameView.setText(browser.getLabel());

            // Toggle ON = visible, OFF = hidden
            boolean isVisible = !hiddenBrowsers.contains(browser.getUniqueKey());
            toggleView.setOnCheckedChangeListener(null);
            toggleView.setChecked(isVisible);

            toggleView.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    hiddenBrowsers.remove(browser.getUniqueKey());
                } else {
                    hiddenBrowsers.add(browser.getUniqueKey());
                }
                listener.onToggle(browser, isChecked);
            });

            itemView.setOnClickListener(v -> toggleView.toggle());
        }
    }
}
