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

import com.zhaoxiaodan.miband.ntu.*;

import android.view.*;

import java.util.*;

import android.app.*;
import android.bluetooth.*;

import com.zhaoxiaodan.miband.MiBand;

import android.widget.AdapterView.OnItemClickListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;


public class SettingActivity extends AppCompatActivity {
    String TAG = "SettingActivity";
    private BluetoothAdapter btAdapter;
    DBHelper dbHelper;
    SQLiteDatabase db;
    Cursor findEntry;
    Context context;
    EditText userId, usrname, usrmac, srange, erange, alisName;
    Button saveBtn, findBtn, stopScan, cancelBinding;
    String sql;
    int itemId;
    private MiBand miband;
    ArrayList<String> deviceMacItem;
    Toast toast;
    ArrayAdapter adapter;
    ArrayList maclist;
    Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        context = this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        adapter = new ArrayAdapter<String>(context, R.layout.item, new ArrayList<String>());

        userId = (EditText) findViewById(R.id.userId);
        usrname = (EditText) findViewById(R.id.usrname);
        usrmac = (EditText) findViewById(R.id.usrmac);
        alisName = (EditText) findViewById(R.id.alisName);
        srange = (EditText) findViewById(R.id.srange);
        erange = (EditText) findViewById(R.id.erange);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        cancelBinding = (Button) findViewById(R.id.cancelBinding);

        Bundle extras = getIntent().getExtras();
        itemId = extras.getInt("itemId");
        Log.d(TAG, "SettingActivity itemId=" + itemId);

        String uid = "", name = "", mac = "", range = "", range1 = "", devicename = "";
        sql = " SELECT _id, usrname, usrmac,range,range1 FROM users_list where _id='" + itemId + "' ";
        Log.d(TAG, "SettingActivity sql=" + sql);
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
        findEntry = db.rawQuery(sql, null);
        while (findEntry.moveToNext()) {
            uid = findEntry.getString(0);
            name = findEntry.getString(1);
            mac = findEntry.getString(2);
            range = findEntry.getString(3);
            range1 = findEntry.getString(4);
        }
        findEntry.close();
        db.close();

        //手環名稱
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
        sql = "select devicename from device_list where usrmac='" + mac + "'";
        findEntry = db.rawQuery(sql, null);
        while (findEntry.moveToNext()) {
            devicename = findEntry.getString(0);
        }
        findEntry.close();
        db.close();

        //display
        userId.setText(uid);
        usrname.setText(name);
        usrmac.setText(mac);
        alisName.setText(devicename);
        srange.setText(range);
        erange.setText(range1);

        Log.d(TAG, "uid=" + uid + "--name=" + name + "---mac=" + mac + "--range=" + range + "--range1=" + range1);

        ((Button) findViewById(R.id.saveBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = usrname.getText().toString();
                String mac = usrmac.getText().toString();
                String range = srange.getText().toString();
                String range1 = erange.getText().toString();
                String sql = "update users_list set usrname='" + name + "',usrmac='" + mac + "',range='" + range + "' ,range1='" + range1 + "' where _id='" + itemId + "'";
                dbHelper = new DBHelper(context);
                db = dbHelper.getWritableDatabase();
                db.execSQL(sql);
                db.close();
                Log.d(TAG, "saveBtn sql=" + sql);
                dialog();
            }
        });

        ((Button) findViewById(R.id.devicelist)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "手環清單");
                intent.setClass(SettingActivity.this, DeviceActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("itemId",itemId );
                bundle.putString("bedno",usrname.getText().toString());
                intent.putExtras(bundle);
                startActivity(intent);
                finish();
            }
        });

        ((Button) findViewById(R.id.switchBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "更換電池");
                intent.setClass(SettingActivity.this, DeviceListActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt("itemId",itemId );
                bundle.putString("bedno",usrname.getText().toString());
                intent.putExtras(bundle);
                Log.d(TAG,"更換電池 bedno="+usrname.getText());
                startActivity(intent);
                finish();
            }
        });

        ((Button) findViewById(R.id.cancelBinding)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "cancelBinding...");
                toast = Toast.makeText(context, "取消綁定...", Toast.LENGTH_LONG);
                toast.show();
                usrmac.setText("");
                saveBtn.callOnClick();
            }
        });
    }


    private void dialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.save_ok);
        builder.setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setClass(SettingActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
        builder.create().show();
    }


}
