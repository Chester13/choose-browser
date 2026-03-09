package de.ub0r.android.choosebrowser;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;


public class ChooserActivity extends AppCompatActivity {

    private final IntentParser mParser = new IntentParser();

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        // Check if launched from launcher (no URL data)
        if (isLauncherIntent(intent)) {
            // Navigate to settings
            startActivity(new Intent(this, SettingsActivity.class));
            finish();
            return;
        }

        final Uri uri = mParser.parseIntent(intent);
        if (uri == null) {
            Toast.makeText(this, R.string.empty_link, Toast.LENGTH_LONG).show();
            finish();
        } else {
            showChooser(uri);
        }
    }

    /**
     * Check if the intent is from launcher (no URL data to process).
     */
    private boolean isLauncherIntent(Intent intent) {
        if (intent == null) {
            return true;
        }

        // Check for explicit launcher intent
        if (Intent.ACTION_MAIN.equals(intent.getAction())
                && intent.hasCategory(Intent.CATEGORY_LAUNCHER)) {
            return true;
        }

        // Check if there's no data to process
        return intent.getData() == null
                && !intent.hasExtra(Intent.EXTRA_TEXT)
                && intent.getClipData() == null;
    }

    private void showChooser(@NonNull Uri uri) {
        showChooserAsDialog(uri);
    }

    private void showChooserAsDialog(@NonNull final Uri uri) {
        ChooserFragment.newInstance(uri).show(getSupportFragmentManager(), "dialog");
    }
}
