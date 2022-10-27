package me.clarius.sdk.mobileapi.example;

import static me.clarius.sdk.mobileapi.example.BuildConfig.CLARIUS_PACKAGE_NAME;
import static me.clarius.sdk.mobileapi.example.BuildConfig.CLARIUS_SERVICE_NAME;

import android.content.Intent;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import me.clarius.sdk.mobileapi.helper.ApiHelper;

public class MainActivity extends AppCompatActivity {

    private ApiHelper api;
    private final Map<Integer, MenuHandler> menuHandlers = makeMenuHandlers();

    private void sendUserFn(MenuItem item) {
        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
        new AlertDialog.Builder(this)
            .setTitle(R.string.dialog_value_title)
            .setView(input)
            .setPositiveButton(R.string.dialog_value_confirm, (dialog, whichButton) -> sendUserFn(
                String.valueOf(item.getTitle()),
                Double.parseDouble(input.getText().toString())))
            .setNegativeButton(R.string.dialog_value_cancel, null)
            .show();
    }

    private void sendUserFn(String title, double value) {
        try {
            api.userFn(title, value);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        Utils.printClariusPackages(this);
        api = new ApiHelper(this);
        api.setListener(new Listener(this, api));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        api.disconnect();
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
        MenuHandler handler = menuHandlers.get(item.getItemId());
        if (null != handler) {
            try {
                handler.run();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return true;
        }
        if (item.getGroupId() == R.id.group_user_fn) {
            sendUserFn(String.valueOf(item.getTitle()), 0);
            return true;
        }
        if (item.getGroupId() == R.id.group_user_fn_with_param) {
            sendUserFn(item);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private Map<Integer, MenuHandler> makeMenuHandlers() {
        HashMap<Integer, MenuHandler> ret = new HashMap<>();
        ret.put(R.id.action_connect, () -> api.connect(CLARIUS_PACKAGE_NAME, CLARIUS_SERVICE_NAME));
        ret.put(R.id.action_disconnect, () -> api.disconnect());
        ret.put(R.id.action_ask_scan_area, () -> api.askScanArea());
        ret.put(R.id.action_ask_probe_info, () -> api.askProbeInfo());
        ret.put(R.id.action_ask_freeze, () -> api.askFreeze());
        ret.put(R.id.action_ask_depth, () -> api.askDepth());
        ret.put(R.id.action_ask_gain, () -> api.askGain());
        ret.put(R.id.action_ask_patient_info, () -> api.askPatientInfo());
        ret.put(R.id.action_settings, this::showSettings);
        ret.put(R.id.action_start_clarius_app, () -> Utils.startClariusApp(this));
        return ret;
    }

    private void showSettings() {
        startActivity(new Intent(this, SettingsActivity.class));
    }

    private interface MenuHandler {
        void run() throws RemoteException;
    }
}
