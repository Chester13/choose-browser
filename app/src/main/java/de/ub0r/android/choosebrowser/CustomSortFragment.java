package de.ub0r.android.choosebrowser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fragment for custom browser sorting with drag-and-drop.
 */
public class CustomSortFragment extends Fragment {

    private PreferencesManager prefsManager;
    private BrowserDiscovery browserDiscovery;
    private CustomSortAdapter adapter;
    private boolean hasChanges = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_custom_sort, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefsManager = new PreferencesManager(requireContext());
        browserDiscovery = new BrowserDiscovery(requireContext());

        RecyclerView recyclerView = view.findViewById(R.id.browsers_list);
        Button btnCancel = view.findViewById(R.id.btn_cancel);
        Button btnSave = view.findViewById(R.id.btn_save);

        // Setup adapter
        adapter = new CustomSortAdapter(
                getString(R.string.selected_browsers),
                getString(R.string.excluded_browsers)
        );
        adapter.setOnDataChangedListener(() -> hasChanges = true);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);

        // Setup drag and drop
        ItemTouchHelper touchHelper = new ItemTouchHelper(new CustomSortAdapter.DragCallback(adapter));
        touchHelper.attachToRecyclerView(recyclerView);
        adapter.setTouchHelper(touchHelper);

        // Load data
        loadBrowsers();

        // Button handlers
        btnCancel.setOnClickListener(v -> goBack());

        btnSave.setOnClickListener(v -> {
            if (saveData()) {
                goBack();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        // Update toolbar
        if (getActivity() instanceof SettingsActivity) {
            SettingsActivity activity = (SettingsActivity) getActivity();
            activity.updateToolbarTitle(getString(R.string.custom_sort));
            activity.setBackButtonEnabled(true);
        }
    }

    private void loadBrowsers() {
        // Get all currently installed browsers
        List<BrowserInfo> allBrowsers = browserDiscovery.discoverAllBrowsers();

        // Get saved lists
        List<BrowserInfo> savedSelected = prefsManager.getCustomSortSelectedBrowsers();
        List<BrowserInfo> savedUnselected = prefsManager.getCustomSortUnselectedBrowsers();

        // Load icons for saved browsers
        for (BrowserInfo browser : savedSelected) {
            browser.loadDetails(requireContext().getPackageManager());
        }
        for (BrowserInfo browser : savedUnselected) {
            browser.loadDetails(requireContext().getPackageManager());
        }

        // Filter out uninstalled browsers
        savedSelected = browserDiscovery.filterInstalledBrowsers(savedSelected);
        savedUnselected = browserDiscovery.filterInstalledBrowsers(savedUnselected);

        // Find any new browsers that aren't in either list
        Set<String> knownKeys = new HashSet<>();
        for (BrowserInfo b : savedSelected) knownKeys.add(b.getUniqueKey());
        for (BrowserInfo b : savedUnselected) knownKeys.add(b.getUniqueKey());

        List<BrowserInfo> newBrowsers = new ArrayList<>();
        for (BrowserInfo browser : allBrowsers) {
            if (!knownKeys.contains(browser.getUniqueKey())) {
                newBrowsers.add(browser);
            }
        }

        // Add new browsers to selected list (as per requirement #9)
        savedSelected.addAll(newBrowsers);

        // If this is the first time (both lists empty), put all browsers in unselected
        if (savedSelected.isEmpty() && savedUnselected.isEmpty()) {
            savedUnselected.addAll(allBrowsers);
        }

        adapter.setData(savedSelected, savedUnselected);
    }

    private boolean saveData() {
        List<BrowserInfo> selected = adapter.getSelectedBrowsers();
        List<BrowserInfo> unselected = adapter.getUnselectedBrowsers();

        // Check if selected list is empty
        if (selected.isEmpty()) {
            Toast.makeText(requireContext(), R.string.custom_sort_empty_warning, Toast.LENGTH_SHORT).show();
            // Auto switch to default sort
            prefsManager.setSortMode(PreferencesManager.SORT_MODE_DEFAULT);
            return true;
        }

        // Save the lists
        prefsManager.saveCustomSortLists(selected, unselected);

        // Update known browsers
        Set<String> allKeys = new HashSet<>();
        for (BrowserInfo b : selected) allKeys.add(b.getUniqueKey());
        for (BrowserInfo b : unselected) allKeys.add(b.getUniqueKey());
        prefsManager.setKnownBrowsers(allKeys);

        hasChanges = false;
        return true;
    }

    private void goBack() {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }
}
