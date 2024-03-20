package de.ub0r.android.choosebrowser;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

import de.ub0r.android.logg0r.Log;

public class ChooserFragment extends BottomSheetDialogFragment {

    private static final String TAG = "ChooserFragment";
    private static final String EXTRA_URI = "uri";

    static ChooserFragment newInstance(@NonNull final Uri uri) {
        ChooserFragment f = new ChooserFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_URI, uri.toString());
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, final @Nullable ViewGroup container, final @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chooser, container, false);
    }

    @Override
    public void onCreate(final @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.Theme_Transparent);
    }

    @Override
    public void onViewCreated(final View container, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(container, savedInstanceState);
        showChooser(container);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
        inflater.inflate(R.menu.activity_chooser, menu);
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        finish();
    }

    private void finish() {
        final FragmentActivity activity = getActivity();
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            activity.finish();
        }
    }

//    private void setTitle(@StringRes final int title) {
//        final FragmentActivity activity = getActivity();
//        if (activity != null) {
//            activity.setTitle(title);
//        }
//        final Dialog dialog = getDialog();
//        if (dialog != null) {
//            dialog.setTitle(title);
//        }
//    }
//
//    private void setSubtitle(final String subtitle) {
//        final FragmentActivity activity = getActivity();
//        if (activity != null) {
//            final AppCompatActivity compatActivity = (AppCompatActivity) activity;
//            if (compatActivity.getSupportActionBar() != null) {
//                compatActivity.getSupportActionBar().setSubtitle(subtitle);
//            }
//        }
//        final Dialog dialog = getDialog();
//        if (dialog != null) {
//            dialog.setTitle(String.format("%s - %s", getString(R.string.app_name), subtitle));
//        }
//    }

    private void showChooser(final View container) {
        final Uri uri = Uri.parse(getArguments().getString(EXTRA_URI));
        Log.d(TAG, "showChooser for uri ", uri);

        //setTitle(R.string.app_name);
        //setSubtitle(uri.toString());

        BottomSheetDialog dialog = (BottomSheetDialog)getDialog();
        if (null != dialog) {
            dialog.setDismissWithAnimation(true);
            dialog.setCanceledOnTouchOutside(true);
        }

        final List<ResolveInfo> browsers = listBrowsers(uri);
        final RecyclerView list = container.findViewById(android.R.id.list);
        final ChooserAdapter adapter = new ChooserAdapter(getContext(), new ChooserAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(final ComponentName component) {
                if (component.getPackageName().equals(getString(R.string.copy_link))) {
                    ClipboardManager clipboard = (ClipboardManager)getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("label", uri.toString());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(getContext(), R.string.link_copied, Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    startActivity(uri, component);
                }
            }
        }, browsers);
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private List<ResolveInfo> listBrowsers(Uri uri) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        final PackageManager pm = getContext().getPackageManager();
        return pm.queryIntentActivities(intent, PackageManager.MATCH_ALL);
    }

    private void startActivity(@NonNull final Uri uri, @NonNull final ComponentName component) {
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.setComponent(component);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
