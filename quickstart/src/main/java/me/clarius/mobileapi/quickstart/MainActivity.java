package me.clarius.mobileapi.quickstart;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Listing installed Clarius packages.\n" +
            "Note: by default, the Quick Start app connects to the Clarius App version from the Play Store.\n" +
            "To connect to another version listed below, change the variable `clariusPackageName` in file `gradle.properties`.");
        List<PackageInfo> packages = getPackageManager().getInstalledPackages(PackageManager.GET_SERVICES);
        if (packages.isEmpty()) {
            Log.e(TAG, "No package found.");
        }
        else {
            int nfound = 0;
            for (PackageInfo p : packages) {
                if (p.packageName.toLowerCase().contains("clarius")) {
                    ++nfound;
                    String log = "Package '" + p.packageName + "' with service(s):";
                    if ((p.services != null) && (p.services.length > 0)) {
                        for (ServiceInfo s : p.services) { log += " " + s.name + ","; }
                    }
                    else {
                        log += " " + "no service";
                    }
                    Log.d(TAG, log);
                }
            }
            Log.d(TAG, "Found " + nfound + " Clarius packages.");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_connect:
                sendBroadcast(new Intent(Intents.CONNECT));
                return true;
            case R.id.action_disconnect:
                sendBroadcast(new Intent(Intents.DISCONNECT));
                return true;
            case R.id.action_ask_scan_area:
                sendBroadcast(new Intent(Intents.ASK_SCAN_AREA));
                return true;
            case R.id.action_ask_probe_info:
                sendBroadcast(new Intent(Intents.ASK_PROBE_INFO));
                return true;
            case R.id.action_ask_depth:
                sendBroadcast(new Intent(Intents.ASK_DEPTH));
                return true;
            case R.id.action_ask_gain:
                sendBroadcast(new Intent(Intents.ASK_GAIN));
                return true;
            case R.id.action_download_raw_data:
                sendBroadcast(new Intent(Intents.DOWNLOAD_RAW_DATA));
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
        }
        if (item.getGroupId() == R.id.group_user_fn) {
            sendBroadcast(new Intent(Intents.USER_FN).putExtra(Intents.KEY_USER_FN, item.getTitle()));
            return true;
        }
        if (item.getGroupId() == R.id.group_user_fn_with_param) {
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_value_title)
                .setView(input)
                .setPositiveButton(R.string.dialog_value_confirm, (dialog, whichButton) -> sendBroadcast(new Intent(Intents.USER_FN)
                    .putExtra(Intents.KEY_USER_FN, item.getTitle())
                    .putExtra(Intents.KEY_USER_PARAM, Double.valueOf(input.getText().toString()))
                )).setNegativeButton(R.string.dialog_value_cancel, (dialog, whichButton) -> {
                    // Do nothing
                }).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
