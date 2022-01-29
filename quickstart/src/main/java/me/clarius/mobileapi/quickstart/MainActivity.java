package me.clarius.mobileapi.quickstart;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MobileApi/Main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
    }

    @Override
    protected void onStart() {
        super.onStart();
        ClariusPackages.print(this);
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
        MenuHandler handler = mMenuHandlers.get(item.getItemId());
        if (null != handler) {
            handler.run();
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

    interface MenuHandler {
        void run();
    }

    private Map<Integer, MenuHandler> makeMenuHandlers() {
        HashMap<Integer, MenuHandler> ret = new HashMap<>();
        ret.put(R.id.action_connect, () -> sendBroadcast(new Intent(Intents.CONNECT)));
        ret.put(R.id.action_disconnect, () -> sendBroadcast(new Intent(Intents.DISCONNECT)));
        ret.put(R.id.action_ask_scan_area, () -> sendBroadcast(new Intent(Intents.ASK_SCAN_AREA)));
        ret.put(R.id.action_ask_probe_info, () -> sendBroadcast(new Intent(Intents.ASK_PROBE_INFO)));
        ret.put(R.id.action_ask_freeze, () -> sendBroadcast(new Intent(Intents.ASK_FREEZE)));
        ret.put(R.id.action_ask_depth, () -> sendBroadcast(new Intent(Intents.ASK_DEPTH)));
        ret.put(R.id.action_ask_gain, () -> sendBroadcast(new Intent(Intents.ASK_GAIN)));
        ret.put(R.id.action_ask_patient_info, () -> sendBroadcast(new Intent(Intents.ASK_PATIENT_INFO)));
        ret.put(R.id.action_download_raw_data, () -> sendBroadcast(new Intent(Intents.DOWNLOAD_RAW_DATA)));
        ret.put(R.id.action_settings, () -> startActivity(new Intent(this, SettingsActivity.class)));
        ret.put(R.id.action_start_clarius_app, this::startClariusApp);
        return ret;
    }

    private final Map<Integer, MenuHandler> mMenuHandlers = makeMenuHandlers();

    private void startClariusApp() {
        Log.i(TAG, "Starting Clarius App...");
        Intent intent = getPackageManager().getLaunchIntentForPackage(BuildConfig.CLARIUS_PACKAGE_NAME);
        if (null == intent) {
            Toast.makeText(this, "Could not find the Clarius App package, verify it is installed.", Toast.LENGTH_SHORT).show();
        }
        else {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT);
            startActivity(intent);
        }
    }
}
