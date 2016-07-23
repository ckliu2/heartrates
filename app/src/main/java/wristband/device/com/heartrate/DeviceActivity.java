package wristband.device.com.heartrate;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.*;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.*;
import android.util.Log;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.zhaoxiaodan.miband.*;
import com.zhaoxiaodan.miband.model.VibrationMode;
import com.zhaoxiaodan.miband.ntu.*;

import android.view.*;

import java.util.*;

import android.app.*;
import android.bluetooth.*;
import android.app.AlertDialog.*;

import com.zhaoxiaodan.miband.*;


public class DeviceActivity extends AppCompatActivity {
    String TAG = "Device";
    Toast toast;
    Button findBtn, stopScan;
    private Context context;
    ArrayList<String> deviceMacItem;
    BluetoothAdapter mBluetoothAdapter;
    MiBand miband;
    ListView lv;
    ArrayAdapter adapter;
    DBHelper dbHelper;
    SQLiteDatabase db;
    Cursor findEntry;
    int itemId;
    String sql, bedno;
    List<String> devices = new ArrayList<String>();
    Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);
        context = this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        adapter = new ArrayAdapter<String>(context, R.layout.item, new ArrayList<String>());
        lv = (ListView) findViewById(R.id.listView);
        Log.d(TAG, "DeviceActivity onCreate");
        Bundle extras = getIntent().getExtras();
        itemId = extras.getInt("itemId");
        bedno = extras.getString("bedno");
        MiBand.startScan(scanCallback); //手環尋找
        toast = Toast.makeText(context, "Looking wristband, please wait...\nTo remind you that your wristband near the phone!", Toast.LENGTH_LONG);
        toast.show();

        ((Button) findViewById(R.id.findBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "開始尋找手環...");
                toast = Toast.makeText(context, "Looking wristband, please wait...", Toast.LENGTH_LONG);
                toast.show();
                MiBand.startScan(scanCallback);
            }
        });

        ((Button) findViewById(R.id.stopScan)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "停止尋找手環...");
                toast = Toast.makeText(context, "Stop looking wristband，To re-find press「Start Find」", Toast.LENGTH_LONG);
                toast.show();
                MiBand.stopScan(scanCallback);
            }
        });

        //返回基本設定
        ((Button) findViewById(R.id.returnBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "DeviceActivity returnBtn");
                Intent intent = new Intent(DeviceActivity.this, SettingActivity.class);
                intent.putExtra("itemId", itemId);
                startActivity(intent);
                finish();
            }
        });

        //返回基本設定
        ((Button) findViewById(R.id.returnBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "DeviceActivity returnBtn");
                Intent intent = new Intent(DeviceActivity.this, SettingActivity.class);
                intent.putExtra("itemId", itemId);
                startActivity(intent);
                finish();
            }
        });

    }

    public void onDestroy() {
        System.out.println("ondestroy() called");
        super.onDestroy();
        MiBand.stopScan(scanCallback);
    }


    final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            Log.d(TAG,
                    "find devices name===" + device.getName() + ",uuid:"
                            + device.getUuids() + ",add:"
                            + device.getAddress() + ",type:"
                            + device.getType() + ",bondState:"
                            + device.getBondState() + ",rssi:" + result.getRssi());
            String item = device.getAddress();
            Log.d(TAG, "device detection isexist===" + deviceCheck(device.getAddress()) + "---mac=" + device.getAddress() + "--deviceName=" + device.getName());



            if (device.getName() != null) {
                if (device.getName().equals("MI1S")) {
                    if (deviceCheck(device.getAddress()) == false && devices.contains(item) == false) {
                        devices.add(item);
                        adapter.add(item);
                    }
                }
            }

            lv.setAdapter(adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String item = ((TextView) view).getText().toString();
                    Log.d(TAG, "select mac=" + item);
                    dialog(item);
                }
            });

        }
    };


    private void dialog(final String mac) {
        Log.d(TAG, "dialog mac=" + mac);
        MiBand.stopScan(scanCallback);
        vibrated(mac);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Please input the name of wristband");
        alertDialog.setMessage("Please enter the name of vibration wristband");

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String no = input.getText().toString();
                        Log.d(TAG, "no=" + no);
                        if (no != "") {
                            saveDevice(no, mac);
                            intent.setClass(DeviceActivity.this, DeviceListActivity.class);
                            Bundle bundle = new Bundle();
                            bundle.putInt("itemId",itemId );
                            bundle.putString("bedno",bedno);
                            intent.putExtras(bundle);
                            startActivity(intent);
                            finish();
                        } else {
                            toast = Toast.makeText(context, "Please enter the name of vibration wristband", Toast.LENGTH_LONG);
                            toast.show();
                            dialog.cancel();
                        }
                    }
                });

        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();

    }

    public void saveDevice(String no, String mac) {
        db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("devicename", no);
        cv.put("usrmac", mac);
        long id = db.insert("device_list", null, cv);
        db.close();
        Log.d(TAG, "save saveDevice no=" + no + "--mac=" + mac + "---id=" + id);
    }

    private void vibrated(String mac) {
        Log.d(TAG, "vibrated mac=" + mac);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mac);
        miband = new MiBand(this);
        miband.connect(device, new ActionCallback() {
            @Override
            public void onSuccess(Object data) {
                Log.d(TAG, "connect success vibrated");
                miband.startVibration(VibrationMode.VIBRATION_10_TIMES_WITH_LED);
            }

            @Override
            public void onFail(int errorCode, String msg) {
                Log.d(TAG, "connect fail vibrated, code:" + errorCode + ",mgs:" + msg);
            }
        });
    }

    public boolean deviceCheck(String mac) {
        boolean r;
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
        sql = " SELECT count(*) AS count  FROM device_list where usrmac='" + mac + "'  ";
        findEntry = db.rawQuery(sql, null);
        int c = 0;
        while (findEntry.moveToNext()) {
            c = findEntry.getInt(0);
        }
        findEntry.close();
        db.close();

        if (c != 0) {
            r = true;
        } else {
            r = false;
        }
        return r;
    }


}
