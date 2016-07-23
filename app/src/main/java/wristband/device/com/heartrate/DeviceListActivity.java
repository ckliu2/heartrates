package wristband.device.com.heartrate;

import android.content.*;
import android.database.*;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.*;
import android.util.*;
import android.widget.AdapterView.OnItemClickListener;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.app.*;
import android.app.AlertDialog.*;

import com.zhaoxiaodan.miband.ntu.DBHelper;

import java.util.*;


public class DeviceListActivity extends AppCompatActivity {
    ListView lv;
    TextView title;
    String sql, bedno;
    int itemId;
    DBHelper dbHelper;
    SQLiteDatabase db;
    Cursor findEntry;
    Context context;
    ArrayAdapter adapter;
    Toast toast;
    String TAG = "DeviceListActivity";
    Intent intent = new Intent();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        context = this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        lv = (ListView) findViewById(R.id.listView);
        title = (TextView) findViewById(R.id.title);
        Bundle extras = getIntent().getExtras();
        itemId = extras.getInt("itemId");
        bedno = extras.getString("bedno");
        title.setText("Room No. " + bedno + " (Replacement Battery)");
        adapter = new ArrayAdapter<String>(context, R.layout.item, new ArrayList<String>());

        Log.d(TAG, "DeviceListActivity onCreate itemId=" + itemId);

        //手環清單
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
        String sql1 = "select _id,devicename,usrmac from device_list order by devicename asc";
        findEntry = db.rawQuery(sql1, null);
        while (findEntry.moveToNext()) {
            String devicename = findEntry.getString(1);
            String usrmac = findEntry.getString(2);
            String item = devicename + "  |  " + usrmac;
            Log.d(TAG, "item=" + item);
            adapter.add(item);
        }
        findEntry.close();
        db.close();

        lv.setAdapter(adapter);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = ((TextView) view).getText().toString();
                String mac = item.substring(item.length() - 17, item.length());
                String devicename = item.substring(0, item.length() - 22);
                Log.d(TAG, "select item=" + item + "---mac=" + mac);
                dialog(item, devicename, mac);
            }
        });

        //基本設定
        ((Button) findViewById(R.id.returnBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "基本設定");
                Intent intent = new Intent();
                intent.setClass(DeviceListActivity.this, SettingActivity.class);
                intent.putExtra("itemId", itemId);
                Log.d(TAG, "基本設定 itemId=" + itemId);
                startActivity(intent);
                finish();
            }
        });
    }

    private void dialog(final String no, final String devicename, final String mac) {
        AlertDialog.Builder builder = new Builder(context);
        builder.setTitle("Wristband Name (" + devicename + ")");
        builder.setIcon(android.R.drawable.ic_dialog_info);

        final String TAG = "dialog";
        builder.setItems(R.array.device_option, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<String> list = Arrays.asList((getResources().getStringArray(R.array.device_option)));
                if (list.get(which).equals("Select")) {
                    Log.d(TAG, "dialog 更換電池");
                    updateDevice(mac);
                }

                if (list.get(which).equals("Rename")) {
                    Log.d(TAG, "dialog 更名");
                    deviceRename(mac, devicename);
                }

                if (list.get(which).equals("Delete")) {
                    Log.d(TAG, "dialog 刪除");
                    removeDeviceList(mac);
                    intent.setClass(DeviceListActivity.this, DeviceListActivity.class);
                    intent.putExtra("itemId", itemId);
                    startActivity(intent);
                }
                dialog.dismiss();
            }

        });

        builder.setNegativeButton(R.string.cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void deviceRename(final String mac, final String oldName) {
        Log.d(TAG, "dialog deviceRename=" + oldName);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("更名(" + oldName + ")");
        alertDialog.setMessage("請輸入新的手環名稱");

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("確認",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String newName = input.getText().toString();
                        Log.d(TAG, "deviceRename=" + newName);
                        if (newName != "") {
                            sql = " update device_list set devicename='" + newName + "' where usrmac='" + mac + "' ";
                            dbHelper = new DBHelper(context);
                            db = dbHelper.getWritableDatabase();
                            db.execSQL(sql);
                            db.close();
                            intent.setClass(DeviceListActivity.this, DeviceListActivity.class);
                            intent.putExtra("itemId", itemId);
                            intent.putExtra("bedno", bedno);
                            startActivity(intent);
                            finish();
                        } else {
                            toast = Toast.makeText(context, "請輸入新的手環名稱", Toast.LENGTH_LONG);
                            toast.show();
                            dialog.cancel();
                        }
                    }
                });

        alertDialog.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

        alertDialog.show();

    }

    public void updateDevice(String mac) {
        sql = " update users_list set usrmac='" + mac + "' where sn='" + itemId + "' ";
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
        db.execSQL(sql);
        db.close();
        intent.setClass(DeviceListActivity.this, HomeActivity.class);
        startActivity(intent);
        finish();
        Log.d(TAG, "removeDeviceList=" + sql);
    }

    public void removeDeviceList(String mac) {
        sql = " delete from device_list where usrmac='" + mac + "' ";
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
        db.execSQL(sql);
        db.close();
        Log.d(TAG, "removeDeviceList=" + sql);
    }
}
