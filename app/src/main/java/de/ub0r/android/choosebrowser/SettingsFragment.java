package de.ub0r.android.choosebrowser;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

/**
 * Main settings fragment with sort mode selection.
 */
public class SettingsFragment extends Fragment {

    private PreferencesManager prefsManager;
    private RadioButton radioDefaultSort;
    private RadioButton radioCustomSort;
    private Button btnHiddenList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefsManager = new PreferencesManager(requireContext());

        radioDefaultSort = view.findViewById(R.id.radio_default_sort);
        radioCustomSort = view.findViewById(R.id.radio_custom_sort);
        btnHiddenList = view.findViewById(R.id.btn_hidden_list);

        // Set initial state based on saved preference
        updateRadioSelection();

        // Handle sort mode changes manually since RadioButtons are nested in LinearLayouts
        radioDefaultSort.setOnClickListener(v -> {
            radioDefaultSort.setChecked(true);
            radioCustomSort.setChecked(false);
            prefsManager.setSortMode(PreferencesManager.SORT_MODE_DEFAULT);
            updateHiddenListButtonVisibility();
        });

        radioCustomSort.setOnClickListener(v -> {
            radioCustomSort.setChecked(true);
            radioDefaultSort.setChecked(false);
            prefsManager.setSortMode(PreferencesManager.SORT_MODE_CUSTOM);
            updateHiddenListButtonVisibility();
            // Navigate to custom sort fragment
            openCustomSortFragment();
        });

        // Hidden list button
        btnHiddenList.setOnClickListener(v -> openHiddenBrowsersFragment());

        updateHiddenListButtonVisibility();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Update UI in case preferences changed
        updateRadioSelection();
        updateHiddenListButtonVisibility();

        // Update toolbar
        if (getActivity() instanceof SettingsActivity) {
            SettingsActivity activity = (SettingsActivity) getActivity();
            activity.updateToolbarTitle(getString(R.string.settings));
            activity.setBackButtonEnabled(false);
        }
    }

    private void updateRadioSelection() {
        // Check if custom sort list is empty, auto-switch to default
        if (prefsManager.isCustomSortMode()) {
            if (prefsManager.getCustomSortSelectedBrowsers().isEmpty()) {
                prefsManager.setSortMode(PreferencesManager.SORT_MODE_DEFAULT);
            }
        }

        if (prefsManager.isDefaultSortMode()) {
            radioDefaultSort.setChecked(true);
            radioCustomSort.setChecked(false);
        } else {
            radioCustomSort.setChecked(true);
            radioDefaultSort.setChecked(false);
        }
    }

    private void updateHiddenListButtonVisibility() {
        btnHiddenList.setVisibility(prefsManager.isDefaultSortMode() ? View.VISIBLE : View.GONE);
    }

    private void openHiddenBrowsersFragment() {
        if (getActivity() instanceof SettingsActivity) {
            ((SettingsActivity) getActivity()).showFragment(new HiddenBrowsersFragment(), true);
        }
    }

    private void openCustomSortFragment() {
        if (getActivity() instanceof SettingsActivity) {
            ((SettingsActivity) getActivity()).showFragment(new CustomSortFragment(), true);
        }
    }
}
