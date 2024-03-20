package de.ub0r.android.choosebrowser;

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

        final Uri uri = mParser.parseIntent(getIntent());
        if (uri == null) {
            Toast.makeText(this, R.string.empty_link, Toast.LENGTH_LONG).show();
            finish();
        } else {
            showChooser(uri);
        }
    }

    private void showChooser(@NonNull Uri uri) {
        showChooserAsDialog(uri);
    }

    private void showChooserAsDialog(@NonNull final Uri uri) {
        ChooserFragment.newInstance(uri).show(getSupportFragmentManager(), "dialog");
    }
}
