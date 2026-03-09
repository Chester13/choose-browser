package de.ub0r.android.choosebrowser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Set;

/**
 * Fragment for managing hidden browsers list.
 */
public class HiddenBrowsersFragment extends Fragment implements HiddenBrowsersAdapter.OnToggleListener {

    private PreferencesManager prefsManager;
    private BrowserDiscovery browserDiscovery;
    private HiddenBrowsersAdapter adapter;
    private RecyclerView recyclerView;
    private TextView emptyText;
    private Set<String> hiddenBrowsers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_hidden_browsers, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefsManager = new PreferencesManager(requireContext());
        browserDiscovery = new BrowserDiscovery(requireContext());

        recyclerView = view.findViewById(R.id.browsers_list);
        emptyText = view.findViewById(R.id.empty_text);

        hiddenBrowsers = prefsManager.getHiddenBrowsers();
        adapter = new HiddenBrowsersAdapter(hiddenBrowsers, this);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        loadBrowsers();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Update toolbar
        if (getActivity() instanceof SettingsActivity) {
            SettingsActivity activity = (SettingsActivity) getActivity();
            activity.updateToolbarTitle(getString(R.string.hidden_browsers));
            activity.setBackButtonEnabled(true);
        }
    }

    private void loadBrowsers() {
        List<BrowserInfo> browsers = browserDiscovery.discoverAllBrowsers();

        if (browsers.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyText.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyText.setVisibility(View.GONE);
            adapter.setBrowsers(browsers);
        }
    }

    @Override
    public void onToggle(BrowserInfo browser, boolean isVisible) {
        // Save immediately when toggled
        prefsManager.setHiddenBrowsers(hiddenBrowsers);
    }
}
