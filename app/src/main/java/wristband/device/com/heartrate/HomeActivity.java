package wristband.device.com.heartrate;

import android.support.v7.app.*;

import java.util.*;

import com.common.*;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;

import android.bluetooth.*;
import android.content.*;
import android.os.*;
import android.text.InputType;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.zhaoxiaodan.miband.*;
import com.zhaoxiaodan.miband.ntu.*;

import java.text.*;

import com.zhaoxiaodan.miband.ActionCallback;
import com.zhaoxiaodan.miband.listeners.*;
import com.zhaoxiaodan.miband.model.*;

import android.database.sqlite.*;
import android.database.*;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.*;

import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.media.*;


public class HomeActivity extends AppCompatActivity {
    Context context;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothDevice device;
    MiBand miband1, miband2, miband3;
    User user1, user2, user3;
    ListView lv;
    ArrayList<User> listItem;
    UserAdapter adapter;
    Intent intent = new Intent();
    DBHelper dbHelper;
    SQLiteDatabase db;
    Cursor cursor;
    String sql;
    Toast toast;
    String TAG = "HomeActivity";
    String serverURL = "";
    UserInfo userInfo = new UserInfo(20111111, 1, 32, 160, 40, "User1", 0);
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    List<String> devices = new ArrayList<String>();

    //Settting
    int voice = 0;
    int sleepSec = 1000; //間隔時間
    int scanSec = 15000; //心率掃秒時間
    int betterySec = 180000; //電池讀取時間
    int deviceCheckSec = 180000; //斷線檢查時間
    int scanSleepSec = 60000; //scan sleep
    int bleResetSec = 3600000; //BLE重新開關時間
    int bleSleepSec = 5000; //BLE重新開關等待時間
    int bleCount = 0;

    //Thread
    HandlerThread deviceThread = new HandlerThread("deviceThread");
    HandlerThread myThread = new HandlerThread("myThread");
    HandlerThread handlerThread1 = new HandlerThread("heartrate1");
    HandlerThread handlerThread2 = new HandlerThread("heartrate2");
    HandlerThread handlerThread3 = new HandlerThread("heartrate3");

    //Handler
    Handler heartHandler1 = new Handler();
    Handler heartHandler2 = new Handler();
    Handler heartHandler3 = new Handler();
    Handler myHandler = new Handler();
    Handler deviceHandler = new Handler();
    Handler uiHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        context = this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        lv = (ListView) findViewById(R.id.listView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        Log.d(TAG, "appversion[20160719----2]");

        //開啟藍芽設備
        openBle();

        //DB
        initDB();

        //connect to miband
        threadstart();

        //ui handler
        uiHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                listItem = (ArrayList<User>) msg.obj;
                try {
                    for (int i = 0; i < listItem.size(); i++) {
                        User u = listItem.get(i);
                        Log.d(TAG, "uiHandler user" + u.getFlag() + "---heartrate=" + u.getMyHeartRate());
                    }
                    listItem.clear();
                    listItem.add(user1);
                    listItem.add(user2);
                    listItem.add(user3);
                    adapter.notifyDataSetChanged();
                } catch (Exception e) {
                    Log.d(TAG, "uiHandler error=" + e.toString());
                }
            }
        };
    }

    private void threadstart() {
        try {
            //Thread
            handlerThread1.start();
            handlerThread2.start();
            handlerThread3.start();
            myThread.start();
            deviceThread.start();

            //Handler
            heartHandler1 = new Handler(handlerThread1.getLooper());
            heartHandler2 = new Handler(handlerThread2.getLooper());
            heartHandler3 = new Handler(handlerThread3.getLooper());
            myHandler = new Handler(myThread.getLooper());
            deviceHandler = new Handler(deviceThread.getLooper());

            //Post
            myHandler.post(avgCheck);
            myHandler.post(display);
            myHandler.post(clearLog);
            deviceHandler.post(deviceCheck);
            deviceHandler.post(bleReset);

        } catch (Exception e) {
            Log.d(TAG, "threadstart error=" + e.toString());
        }
    }


    private void threadSleep1() {
        try {
            handlerThread1.sleep(sleepSec);
        } catch (InterruptedException e) {
            Log.d(TAG, "threadSleep1 error=" + e.toString());
        }
    }

    private void threadSleep2() {
        try {
            handlerThread2.sleep(sleepSec);
        } catch (InterruptedException e) {
            Log.d(TAG, "threadSleep2 error=" + e.toString());
        }
    }

    private void threadSleep3() {
        try {
            handlerThread3.sleep(sleepSec);
        } catch (InterruptedException e) {
            Log.d(TAG, "threadSleep3 error=" + e.toString());
        }
    }


    private void deviceThreadSleep(int ms) {
        try {
            deviceThread.sleep(ms);
        } catch (InterruptedException e) {
            Log.d(TAG, "deviceThreadSleep error=" + e.toString());
        }
    }


    private Runnable listener1 = new Runnable() {
        public void run() {
            try {
                Log.d(TAG, "connect1 listener1");

                boolean mylistener = miband1.setHeartRateScanListener1(new HeartRateNotifyListener() {
                    public void onNotify(int heartRate) {
                        int rateFilter = heartRateFilter(user1, heartRate);
                        //alert(user1, rateFilter);
                        Log.d(TAG, "heart rate(1): " + heartRate + "---rateFilter=" + rateFilter);
                        if (rateFilter >= user1.getRange() && rateFilter <= user1.getRange1()) {
                            user1.setImageId(R.drawable.normal);
                            user1.setDpmStatus(1);
                        } else {
                            user1.setImageId(R.drawable.over);
                            user1.setDpmStatus(2);
                        }
                        user1.setResponseTime(dateFormat.format(Tools.getCurrentTimestamp()));
                        user1.setMyHeartRate(String.valueOf(rateFilter));
                        saveHeart(user1);
                    }
                });
                Log.d(TAG, "connect1 mylistener=" + mylistener);

                if (mylistener == true) {
                    threadSleep1();
                    heartHandler1.post(HeartRateScan1);
                    Log.d(TAG, "connect1 success Scan");

                    //battery
                    threadSleep1();
                    myHandler.post(battery1);
                } else {
                    closeGatt1();
                }

            } catch (Exception e) {
                Log.d(TAG, "listener1 error=" + e.toString());
            }
        }
    };

    private Runnable listener2 = new Runnable() {
        public void run() {
            try {
                Log.d(TAG, "connect2 listener2");
                boolean mylistener = miband2.setHeartRateScanListener1(new HeartRateNotifyListener() {
                    public void onNotify(int heartRate) {
                        int rateFilter = heartRateFilter(user2, heartRate);
                        //alert(user2, rateFilter);
                        Log.d(TAG, "heart rate(2): " + heartRate + "---rateFilter=" + rateFilter);
                        if (rateFilter >= user2.getRange() && rateFilter <= user2.getRange1()) {
                            user2.setImageId(R.drawable.normal);
                            user2.setDpmStatus(1);
                        } else {
                            user2.setImageId(R.drawable.over);
                            user2.setDpmStatus(2);
                        }
                        user2.setResponseTime(dateFormat.format(Tools.getCurrentTimestamp()));
                        user2.setMyHeartRate(String.valueOf(rateFilter));
                        saveHeart(user2);
                    }
                });
                Log.d(TAG, "connect2 mylistener=" + mylistener);

                if (mylistener == true) {
                    threadSleep2();
                    heartHandler2.post(HeartRateScan2);
                    Log.d(TAG, "connect2 success Scan");

                    //battery
                    threadSleep2();
                    myHandler.post(battery2);
                } else {
                    closeGatt2();
                }
            } catch (Exception e) {
                Log.d(TAG, "listener2 error=" + e.toString());
            }
        }
    };

    private Runnable listener3 = new Runnable() {
        public void run() {
            try {
                Log.d(TAG, "connect3 listener3");
                boolean mylistener = miband3.setHeartRateScanListener1(new HeartRateNotifyListener() {
                    public void onNotify(int heartRate) {
                        int rateFilter = heartRateFilter(user3, heartRate);
                        //alert(user3, rateFilter);
                        Log.d(TAG, "heart rate(3): " + heartRate + "---rateFilter=" + rateFilter);
                        if (rateFilter >= user3.getRange() && rateFilter <= user3.getRange1()) {
                            user3.setImageId(R.drawable.normal);
                            user3.setDpmStatus(1);
                        } else {
                            user3.setImageId(R.drawable.over);
                            user3.setDpmStatus(2);
                        }
                        user3.setResponseTime(dateFormat.format(Tools.getCurrentTimestamp()));
                        user3.setMyHeartRate(String.valueOf(rateFilter));
                        saveHeart(user3);
                    }
                });
                Log.d(TAG, "connect3 mylistener=" + mylistener);

                if (mylistener == true) {
                    threadSleep3();
                    heartHandler3.post(HeartRateScan3);
                    Log.d(TAG, "connect3 success Scan");

                    //battery
                    threadSleep3();
                    myHandler.post(battery3);
                } else {
                    closeGatt3();
                }

            } catch (Exception e) {
                Log.d(TAG, "listener3 error=" + e.toString());
            }
        }
    };

    private Runnable heartRateStart1 = new Runnable() {
        public void run() {
            try {
                if (!user1.getMac().equals("")) {
                    int sleep = (int) (Math.random() * sleepSec);
                    handlerThread1.sleep(sleep);

                    Log.d(TAG, "connect1........sleep=" + sleep);
                    //remove tasks
                    heartHandler1.removeCallbacks(HeartRateScan1);
                    heartHandler1.removeCallbacks(battery1);
                    handlerThread1.sleep(sleep);

                    miband1 = new MiBand(context);
                    device = mBluetoothAdapter.getRemoteDevice(user1.getMac());
                    miband1.connect(device, new ActionCallback() {
                        public void onSuccess(Object data) {
                            Log.d(TAG, "connect1 success miband1");
                            boolean b = miband1.setUserInfo1(userInfo);
                            Log.d(TAG, "connect1 userinfo=" + b);
                            if (b) {
                                threadSleep1();
                                heartHandler1.post(listener1);
                            } else {
                                closeGatt1();
                            }
                        }

                        public void onFail(int errorCode, String msg) {
                            Log.d(TAG, "connect1 fail, code:" + errorCode + ",mgs:" + msg + "--- call disconnect");
                        }
                    });

                    miband1.setDisconnectedListener(new NotifyListener() {
                        public void onNotify(byte[] data) {
                            Log.d(TAG, "connect1 disconnect");
                            myHandler.removeCallbacks(battery1);
                            heartHandler1.removeCallbacks(HeartRateScan1);
                            clearUser(1);
                        }
                    });
                }
            } catch (Exception e) {
                Log.d(TAG, "heartRateStart1 error=" + e.toString());
            }
        }
    };

    private Runnable heartRateStart2 = new Runnable() {
        public void run() {
            try {
                if (!user2.getMac().equals("")) {
                    int sleep = (int) (Math.random() * sleepSec);

                    Log.d(TAG, "connect2......sleep=" + sleep);
                    //remove tasks
                    heartHandler2.removeCallbacks(HeartRateScan2);
                    heartHandler2.removeCallbacks(battery2);
                    handlerThread2.sleep(sleep);

                    miband2 = new MiBand(context);
                    device = mBluetoothAdapter.getRemoteDevice(user2.getMac());
                    miband2.connect(device, new ActionCallback() {
                        public void onSuccess(Object data) {
                            Log.d(TAG, "connect2 success miband2");
                            boolean b = miband2.setUserInfo1(userInfo);
                            Log.d(TAG, "connect2 userinfo=" + b);
                            if (b) {
                                threadSleep2();
                                heartHandler2.post(listener2);
                            } else {
                                closeGatt2();
                            }
                        }

                        public void onFail(int errorCode, String msg) {
                            Log.d(TAG, "connect2 fail, code:" + errorCode + ",mgs:" + msg + "--- call disconnect");
                        }
                    });

                    miband2.setDisconnectedListener(new NotifyListener() {
                        public void onNotify(byte[] data) {
                            Log.d(TAG, "connect2 disconnect");
                            myHandler.removeCallbacks(battery2);
                            heartHandler2.removeCallbacks(HeartRateScan2);
                            clearUser(2);
                        }
                    });
                }
            } catch (Exception e) {
                Log.d(TAG, "heartRateStart2 error=" + e.toString());
            }
        }
    };


    private Runnable heartRateStart3 = new Runnable() {
        public void run() {
            try {
                if (!user3.getMac().equals("")) {
                    int sleep = (int) (Math.random() * sleepSec);

                    Log.d(TAG, "connect3......sleep=" + sleep);
                    //remove tasks
                    heartHandler3.removeCallbacks(HeartRateScan3);
                    heartHandler3.removeCallbacks(battery3);
                    handlerThread3.sleep(sleep);

                    miband3 = new MiBand(context);
                    device = mBluetoothAdapter.getRemoteDevice(user3.getMac());
                    miband3.connect(device, new ActionCallback() {
                        public void onSuccess(Object data) {
                            Log.d(TAG, "connect3 success miband3");
                            boolean b = miband3.setUserInfo1(userInfo);
                            Log.d(TAG, "connect3 userinfo=" + b);
                            if (b) {
                                threadSleep3();
                                heartHandler3.post(listener3);
                            } else {
                                closeGatt3();
                            }
                        }

                        public void onFail(int errorCode, String msg) {
                            Log.d(TAG, "connect3 fail, code:" + errorCode + ",mgs:" + msg + "--- call disconnect");
                        }
                    });

                    miband3.setDisconnectedListener(new NotifyListener() {
                        public void onNotify(byte[] data) {
                            Log.d(TAG, "connect3 disconnect");
                            myHandler.removeCallbacks(battery3);
                            heartHandler3.removeCallbacks(HeartRateScan3);
                            clearUser(3);
                        }
                    });
                }
            } catch (Exception e) {
                Log.d(TAG, "heartRateStart3 error=" + e.toString());
            }
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Log.d(TAG, "MenuItem action_settings");
            settingServerURL();
            return true;
        }

        if (id == R.id.alertvoice) {
            Log.d(TAG, "alertvoice");
            adjectVoice();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openBle() {
        try {
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null) {
                toast = Toast.makeText(this, "Your phone does not have Bluetooth devices!", Toast.LENGTH_LONG);
                toast.show();
            }
            if (!mBluetoothAdapter.isEnabled()) {
                mBluetoothAdapter.enable();
            }
        } catch (Exception e) {
            Log.d(TAG, "openBle error=" + e.toString());
        }
    }


    private void initDB() {
        try {
            toast = Toast.makeText(this, "Wristband connection in about 30 seconds, please wait....", Toast.LENGTH_LONG);
            toast.show();

            dbHelper = new DBHelper(context);
            db = dbHelper.getWritableDatabase();

            //清空紀錄
            sql = "delete from log_list";
            db.execSQL(sql);

            sql = "select _id,usrname,usrmac,sn,range,range1 from users_list";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                String _id = cursor.getString(0);
                String usrname = cursor.getString(1);
                String usrmac = cursor.getString(2);
                int sn = cursor.getInt(3);
                int range = cursor.getInt(4);
                int range1 = cursor.getInt(5);
                switch (sn) {
                    case 1:
                        user1 = new User(1, usrname, "", "", usrmac, dateFormat.format(Tools.getCurrentTimestamp()), R.drawable.discon, 1, range, range1, getDeviceName(usrmac), 0, 0);
                        break;
                    case 2:
                        user2 = new User(2, usrname, "", "", usrmac, dateFormat.format(Tools.getCurrentTimestamp()), R.drawable.discon, 2, range, range1, getDeviceName(usrmac), 0, 0);
                        break;
                    case 3:
                        user3 = new User(3, usrname, "", "", usrmac, dateFormat.format(Tools.getCurrentTimestamp()), R.drawable.discon, 3, range, range1, getDeviceName(usrmac), 0, 0);
                        break;
                }
                Log.d("DB", "user _id=" + _id + "--usrname=" + usrname + "--usrmac=" + usrmac + "--sn=" + sn + "--range=" + range + "--range1=" + range1 + "---deviceName=" + getDeviceName(usrmac));
            }
            cursor.close();
            db.close();

            //serverURL
            dbHelper = new DBHelper(context);
            db = dbHelper.getWritableDatabase();
            sql = "select serverURL,voice from setting_list";
            cursor = db.rawQuery(sql, null);
            while (cursor.moveToNext()) {
                serverURL = cursor.getString(0);
                voice = cursor.getInt(1);
            }
            cursor.close();
            db.close();

            //display
            listItem = new ArrayList<User>();
            listItem.add(user1);
            listItem.add(user2);
            listItem.add(user3);
            adapter = new UserAdapter(context, listItem);
            lv.setAdapter(adapter);
            lv.setOnItemClickListener(new MyOnItemClickListener());
        } catch (Exception e) {
            Log.d(TAG, "initDB error=" + e.toString());
        }
    }

    public String getDeviceName(String mac) {
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
        sql = "select devicename from device_list where usrmac='" + mac + "'";
        Cursor findEntry = db.rawQuery(sql, null);
        String devicename = "";
        while (findEntry.moveToNext()) {
            devicename = findEntry.getString(0);
        }
        findEntry.close();
        db.close();
        if (devicename == "") {
            devicename = "Unpaired Wristband";
        }
        return devicename;
    }

    private class MyOnItemClickListener implements OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            String TAG = "OnItemClick";
            try {
                dialog(listItem.get(position).getId());
            } catch (Exception e) {
                Log.d(TAG, "OnItemClick error=" + e.toString());
            }
        }
    }


    public void onStop() {

        super.onStop();
    }


    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        //remove Hander
        try {
            myHandler.removeCallbacks(avgCheck);
            myHandler.removeCallbacks(display);
            myHandler.removeCallbacks(clearLog);
            myHandler.removeCallbacks(deviceCheck);
            myHandler.removeCallbacks(battery1);
            myHandler.removeCallbacks(battery2);
            myHandler.removeCallbacks(battery3);
            heartHandler1.removeCallbacks(heartRateStart1);
            heartHandler2.removeCallbacks(heartRateStart2);
            heartHandler3.removeCallbacks(heartRateStart3);
            deviceHandler.removeCallbacks(deviceCheck);
            deviceHandler.removeCallbacks(bleReset);
            Log.d(TAG, "onDestroy remove Hander");
        } catch (Exception e) {
            Log.d(TAG, "onDestroy remove Hander error=" + e.toString());
        }

        //interrupt thread
        try {
            handlerThread1.getLooper().quit();
            handlerThread2.getLooper().quit();
            handlerThread3.getLooper().quit();
            myThread.getLooper().quit();
            deviceThread.getLooper().quit();
            Log.d(TAG, "onDestroy interrupt thread");
        } catch (Exception e) {
            Log.d(TAG, "onDestroy interrupt thread error=" + e.toString());
        }

        //gattClose
        try {
            miband1.gattClose();
            miband2.gattClose();
            miband3.gattClose();
        } catch (Exception e) {
            Log.d(TAG, "onDestroy close gatt error=" + e.toString());
        }

        super.onDestroy();
    }

    private void dialog(final int itemId) {
        AlertDialog.Builder builder = new Builder(context);
        builder.setTitle(R.string.setting);
        builder.setIcon(android.R.drawable.ic_dialog_info);
        final String TAG = "dialog";
        Log.d(TAG, "dialog itemId=" + itemId);
        builder.setItems(R.array.setting_list, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                List<String> list = Arrays.asList((getResources().getStringArray(R.array.setting_list)));
                Intent intent = new Intent();
                if (list.get(which).equals("Setting")) {
                    intent.setClass(context, SettingActivity.class);
                    intent.putExtra("itemId", itemId);
                    startActivity(intent);
                    finish();
                }
                if (list.get(which).equals("Record")) {
                    intent.setClass(context, HistoryActivity.class);
                    intent.putExtra("itemId", itemId);
                    startActivity(intent);
                    finish();
                }


                if (list.get(which).equals("stop1")) {
                    try {
                        miband1.gattClose();
                        heartHandler1.removeCallbacks(HeartRateScan1);
                        myHandler.removeCallbacks(battery1);
                    } catch (Exception e) {
                        Log.d(TAG, "miband1 disconnect error=" + e.toString());
                    }
                }

                if (list.get(which).equals("restart1")) {
                    heartRateStart1.run();
                }

                if (list.get(which).equals("battery1")) {
                    battery1.run();
                }


                if (list.get(which).equals("stop2")) {
                    try {
                        miband2.gattClose();
                        heartHandler2.removeCallbacks(HeartRateScan2);
                        myHandler.removeCallbacks(battery2);
                    } catch (Exception e) {
                        Log.d(TAG, "miband2 disconnect error=" + e.toString());
                    }
                }

                if (list.get(which).equals("restart2")) {
                    heartRateStart2.run();
                }

                if (list.get(which).equals("battery2")) {
                    battery2.run();
                }

                if (list.get(which).equals("stop3")) {
                    try {
                        miband3.gattClose();
                        heartHandler3.removeCallbacks(HeartRateScan3);
                        myHandler.removeCallbacks(battery3);
                    } catch (Exception e) {
                        Log.d(TAG, "miband3 disconnect error=" + e.toString());
                    }
                }

                if (list.get(which).equals("restart3")) {
                    heartRateStart3.run();
                }

                if (list.get(which).equals("battery3")) {
                    battery3.run();
                }

                if (list.get(which).equals("dbcheck")) {
                    deviceCheck.run();
                }

                if (list.get(which).equals("bleReset")) {
                    bleReset.run();
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


    private void sendJSON(User user) {
        String TAG = "sendJSON";
        try {
            Date now = new Date();
            JSONObject jo = new JSONObject();
            jo.put("room", user.getName());
            jo.put("responseDate", dateFormat.format(now));
            jo.put("rate", user.getMyHeartRate());
            jo.put("avg", user.getHeartRateAVG());
            jo.put("battery", user.getBatteryCapacity());
            jo.put("deviceName", user.getDeviceName());
            jo.put("dpmStatus", user.getDpmStatus());
            jo.put("avgStatus", user.getAvgStatus());
            ArrayList<NameValuePair> paris = new ArrayList<NameValuePair>();
            paris.add(new BasicNameValuePair("remoteJSON", jo.toString()));
            Log.d(TAG, jo.toString());

            final HttpParams httpParams = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpParams, 3000);
            HttpConnectionParams.setSoTimeout(httpParams, 3000);
            HttpClient httpClient = new DefaultHttpClient(httpParams);

            //serverURL="http://192.168.0.102:3000/api/heartrates/receive";
            //serverURL = "http://10.12.5.80:3000/api/heartrates/receive";

            Log.d(TAG, "url=" + serverURL);
            HttpPost httpPost = new HttpPost(serverURL);
            httpPost.setEntity(new UrlEncodedFormEntity(paris));
            httpClient.execute(httpPost);

        } catch (Exception e) {
            Log.d(TAG, "sendJSON error=" + e.toString());
        }
    }

    private void saveHeart(User user) {
        try {
            dbHelper = new DBHelper(context);
            db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("usrname", user.getId());
            cv.put("heartrate", user.getMyHeartRate());
            long id = db.insert("log_list", null, cv);
            db.close();
            Log.d("DB", "saveHeart success id=" + id + "---heartrate=" + user.getMyHeartRate() + "--usrname=" + user.getId());
            //connect to Server
            sendJSON(user);

        } catch (Exception e) {
            Log.d("DB", "saveHeart fail=" + e.toString() + "---heartrate=" + user.getMyHeartRate() + "--usrname=" + user.getId());
        }
    }


    /*******************
     * avg
     *******************/

    void avg() {
        for (int i = 1; i <= 3; i++) {
            try {
                dbHelper = new DBHelper(context);
                db = dbHelper.getWritableDatabase();
                sql = "select heartrate from log_list where usrname='" + i + "' and created_time >= datetime('now','-3 minutes')";
                int sum = 0;
                int count = 0;
                cursor = db.rawQuery(sql, null);
                while (cursor.moveToNext()) {
                    sum += cursor.getInt(0);
                    count++;
                }
                cursor.close();
                db.close();

                int avg = (int) (sum / count);
                Log.d(TAG, "avg user" + i + "---avg=" + avg + "----count=" + count);

                switch (i) {
                    case 1:
                        if (count == 0) {
                            user1.setHeartRateAVG("--");
                        } else {
                            user1.setHeartRateAVG(String.valueOf(avg));
                        }
                        break;

                    case 2:
                        if (count == 0) {
                            user2.setHeartRateAVG("--");
                        } else {
                            user2.setHeartRateAVG(String.valueOf(avg));
                        }
                        break;

                    case 3:
                        if (count == 0) {
                            user3.setHeartRateAVG("--");
                        } else {
                            user3.setHeartRateAVG(String.valueOf(avg));
                        }
                        break;
                }
            } catch (Exception e) {
                Log.d(TAG, "avg error=" + e.toString());
            }
        }
    }


    void adjectVoice() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Setting");
        alertDialog.setMessage("Please enter the alert volume");

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setText(String.valueOf(voice));
        alertDialog.setView(input);

        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String text = input.getText().toString();
                        Log.d(TAG, "text=" + text);
                        if (!text.equals("")) {
                            sql = " update setting_list set voice='" + Integer.parseInt(text) + "' ";
                            dbHelper = new DBHelper(context);
                            db = dbHelper.getWritableDatabase();
                            db.execSQL(sql);
                            db.close();
                            voice = Integer.parseInt(text);
                            toast = Toast.makeText(context, "Saved successfully", Toast.LENGTH_LONG);
                            toast.show();
                        } else {
                            toast = Toast.makeText(context, "Please enter the alert volume", Toast.LENGTH_LONG);
                            toast.show();
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

    void settingServerURL() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Setting");
        alertDialog.setMessage("Please enter a host receives a URL");

        final EditText input = new EditText(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setText(serverURL);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String url = input.getText().toString();
                        Log.d(TAG, "url=" + url);
                        if (!url.equals("")) {
                            sql = " update setting_list set serverURL='" + url + "' ";
                            dbHelper = new DBHelper(context);
                            db = dbHelper.getWritableDatabase();
                            db.execSQL(sql);
                            db.close();
                            serverURL = url;
                            toast = Toast.makeText(context, "Saved successfully", Toast.LENGTH_LONG);
                            toast.show();
                        } else {
                            toast = Toast.makeText(context, "Please enter a host receives a URL", Toast.LENGTH_LONG);
                            toast.show();
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


    /*******************
     * avg
     *******************/

    private Runnable avgCheck = new Runnable() {
        public void run() {
            avg();
            myHandler.postDelayed(avgCheck, 30000);
        }
    };


    /*******************
     * deviceCheck
     *******************/

    private Runnable deviceCheck = new Runnable() {
        public void run() {
            try {
                Log.d(TAG, "deviceCheck start");
                int a1 = 0, a2 = 0, a3 = 0;
                for (int i = 1; i <= 3; i++) {
                    sql = "select heartrate from log_list where usrname='" + i + "' and created_time >= datetime('now','-2 minutes')";
                    DBHelper dbHelper = new DBHelper(context);
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    Cursor cursor = db.rawQuery(sql, null);
                    while (cursor.moveToNext()) {
                        switch (i) {
                            case 1:
                                a1++;
                                break;
                            case 2:
                                a2++;
                                break;
                            case 3:
                                a3++;
                                break;
                        }
                    }
                    cursor.close();
                    db.close();
                }

                //close gatt
                if (a1 == 0) {
                    closeGatt1();
                }

                if (a2 == 0) {
                    closeGatt2();
                }

                if (a3 == 0) {
                    closeGatt3();
                }

                Log.d(TAG, "deviceCheck a1=" + a1 + "--a2=" + a2 + "--a3=" + a3);

                if (a1 == 0 || a2 == 0 || a3 == 0) {
                    devices.clear();
                    MiBand.startScan(scanCallback);
                    Log.d(TAG, "MiBand startScan");
                    deviceThreadSleep(scanSleepSec);
                    MiBand.stopScan(scanCallback);
                    Log.d(TAG, "MiBand stopScan");
                    deviceThreadSleep(1000);
                }

                boolean b1 = macIsExist(user1.getMac());
                boolean b2 = macIsExist(user2.getMac());
                boolean b3 = macIsExist(user3.getMac());

                Log.d(TAG, "deviceCheck b1=" + b1 + "--b2=" + b2 + "--b3=" + b3);

                if (b1 == true && a1 == 0) {
                    heartHandler1.post(heartRateStart1);
                }

                if (b2 == true && a2 == 0) {
                    heartHandler2.post(heartRateStart2);
                }

                if (b3 == true && a3 == 0) {
                    heartHandler3.post(heartRateStart3);
                }
            } catch (Exception e) {
                Log.d(TAG, "deviceCheck error=" + e.toString());
            }
            deviceHandler.postDelayed(deviceCheck, deviceCheckSec);
        }
    };


    /*******************
     * bleReset
     *******************/

    private Runnable bleReset = new Runnable() {
        public void run() {
            try {
                if (bleCount != 0) {

                    Log.d(TAG, "bleReset");

                    //close all gatt
                    closeGatt1();
                    closeGatt2();
                    closeGatt3();

                    //close ble
                    if (mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.disable();
                    }

                    //sleep
                    deviceThreadSleep(bleSleepSec);

                    //open ble
                    if (!mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.enable();
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "bleReset error=" + e.toString());
            }
            bleCount++;
            deviceHandler.postDelayed(bleReset, bleResetSec);
        }
    };


    /*******************
     * alert
     *******************/

    public void alert(User user, int heartrate) {
        if (heartrate < user.getRange() && user.getMac() != "") {
            ToneGenerator toneG = new ToneGenerator(AudioManager.STREAM_ALARM, voice);
            toneG.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 200);
            Log.d(TAG, "alert voice=" + voice);
        }
    }

    /*******************
     * clearLog
     *******************/

    private Runnable clearLog = new Runnable() {
        public void run() {
            try {
                sql = "delete from log_list where created_time < datetime('now','-15 minutes')";
                dbHelper = new DBHelper(context);
                db = dbHelper.getWritableDatabase();
                db.execSQL(sql);
            } catch (Exception e) {
                Log.d(TAG, "clearLog error=" + e.toString());
            }
            myHandler.postDelayed(clearLog, 30000);
        }
    };


    private Runnable display = new Runnable() {
        public void run() {
            try {
                ArrayList<User> list = new ArrayList<User>();
                list.add(user1);
                list.add(user2);
                list.add(user3);

                Message message = new Message();
                message.obj = list;
                uiHandler.sendMessage(message);
                Log.d(TAG, "display =" + Tools.getCurrentTimestamp());
            } catch (Exception e) {
                Log.d(TAG, "display error=" + e.toString());
            }
            myHandler.postDelayed(display, 3000);
        }
    };


    /*******************
     * Battery
     *******************/

    private Runnable battery1 = new Runnable() {
        public void run() {
            try {
                if (user1.getMac() != "") {
                    miband1.getBatteryInfo(new ActionCallback() {
                        public void onSuccess(Object data) {
                            BatteryInfo info = (BatteryInfo) data;
                            user1.setBatteryCapacity(String.valueOf(info.getLevel()));
                            Log.d(TAG, "Battery[1]=" + info.getLevel());
                        }

                        public void onFail(int errorCode, String msg) {
                            Log.d(TAG, "Battery[1] fail msg=" + msg);
                        }
                    });
                }
            } catch (Exception e) {
                Log.d(TAG, "Battery[1] error=" + e.toString());
            }
            Log.d(TAG, "Battery1...");
            myHandler.postDelayed(battery1, betterySec);
        }
    };

    private Runnable battery2 = new Runnable() {
        public void run() {
            try {
                if (user2.getMac() != "") {
                    miband2.getBatteryInfo(new ActionCallback() {
                        public void onSuccess(Object data) {
                            BatteryInfo info = (BatteryInfo) data;
                            user2.setBatteryCapacity(String.valueOf(info.getLevel()));
                            Log.d(TAG, "Battery[2]=" + info.getLevel());
                        }

                        public void onFail(int errorCode, String msg) {
                            Log.d(TAG, "Battery[2] fail msg=" + msg);
                        }
                    });
                }
            } catch (Exception e) {
                Log.d(TAG, "Battery[2] error=" + e.toString());
            }
            Log.d(TAG, "Battery2...");
            myHandler.postDelayed(battery2, betterySec);
        }
    };

    private Runnable battery3 = new Runnable() {
        public void run() {
            try {
                if (user3.getMac() != "") {
                    miband3.getBatteryInfo(new ActionCallback() {
                        public void onSuccess(Object data) {
                            BatteryInfo info = (BatteryInfo) data;
                            user3.setBatteryCapacity(String.valueOf(info.getLevel()));
                            Log.d(TAG, "Battery[3]=" + info.getLevel());
                        }

                        public void onFail(int errorCode, String msg) {
                            Log.d(TAG, "Battery[3] fail msg=" + msg);
                        }
                    });
                }
            } catch (Exception e) {
                Log.d(TAG, "Battery[3] error=" + e.toString());
            }
            Log.d(TAG, "Battery3...");
            myHandler.postDelayed(battery3, betterySec);
        }
    };


    /*******************
     * heartRateFilter
     *******************/

    public int heartRateFilter(User user, int rate) {
        try {
            if (rate == 0) {
                return rate;
            } else {
                sql = " SELECT heartrate FROM log_list where usrname='" + user.getFlag() + "' and created_time >= datetime('now','-3 minutes') and heartrate between " + user.getRange() + " and " + user.getRange1();
                int count = 0, sum = 0;
                DBHelper dbHelper1 = new DBHelper(context);
                SQLiteDatabase db1 = dbHelper1.getWritableDatabase();
                Cursor findEntry1 = db1.rawQuery(sql, null);
                while (findEntry1.moveToNext()) {
                    sum += findEntry1.getInt(0);
                    count++;
                    Log.d(TAG, "heartRateFilter user" + user.getFlag() + "----heartrate=" + findEntry1.getInt(0) + "---count=" + count);
                }
                findEntry1.close();
                db1.close();

                int newrate = rate;

                //正常值
                if (rate >= user.getRange() && rate <= user.getRange1()) {
                    if (count > 0) {
                        newrate = (int) (sum + rate) / (count + 1);
                        Log.d(TAG, "heartRateFilter user" + user.getFlag() + "----sum=" + sum + "----rate=" + rate + "---count=" + count + "---newrate=" + newrate);
                    }
                } else {
                    newrate = (int) sum / count;
                }

                if (newrate > user.getRange1()) {
                    newrate = user.getRange1();
                }

                if (newrate < user.getRange()) {
                    newrate = user.getRange();
                }

                return newrate;
            }
        } catch (Exception e) {

            if (rate > user.getRange1()) {
                rate = user.getRange1();
            }

            if (rate < user.getRange()) {
                rate = user.getRange();
            }

            return rate;
        }
    }


    /*******************
     * HeartRate
     *******************/

    private Runnable HeartRateScan1 = new Runnable() {
        public void run() {
            try {
                miband1.startHeartRateScan();
            } catch (Exception e) {
                Log.d(TAG, "HeartRateScan1 error=" + e.toString());
            }
            Log.d(TAG, "HeartRateScan1...");
            heartHandler1.postDelayed(HeartRateScan1, scanSec);
        }
    };

    private Runnable HeartRateScan2 = new Runnable() {
        public void run() {
            try {
                miband2.startHeartRateScan();
            } catch (Exception e) {
                Log.d(TAG, "HeartRateScan2 error=" + e.toString());
            }
            Log.d(TAG, "HeartRateScan2...");
            heartHandler2.postDelayed(HeartRateScan2, scanSec);
        }
    };

    private Runnable HeartRateScan3 = new Runnable() {
        public void run() {
            try {
                miband3.startHeartRateScan();
            } catch (Exception e) {
                Log.d(TAG, "HeartRateScan3 error=" + e.toString());
            }
            Log.d(TAG, "HeartRateScan3...");
            heartHandler3.postDelayed(HeartRateScan3, scanSec);
        }
    };


    /*******************
     * startScan
     *******************/
    public boolean macIsExist(String mac) {
        boolean r = false;
        try {
            for (int i = 0; i < devices.size(); i++) {
                Log.d(TAG, "macIsExist=" + devices.get(i));
            }

            if (devices.contains(mac)) {
                r = true;
            }
            return r;
        } catch (Exception e) {
            Log.d(TAG, "macIsExist error=" + e.toString());
            return false;
        }
    }


    final ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String mac = device.getAddress();
            if (device.getName() != null) {
                if (device.getName().equals("MI1S")) {
                    if (devices.contains(mac) == false) {
                        Log.d(TAG, "scanCallback mac==" + mac);
                        devices.add(mac);
                    }
                }
            }

        }
    };


    //closeGatt1
    private void closeGatt1() {
        try {
            heartHandler1.removeCallbacks(HeartRateScan1);
            heartHandler1.removeCallbacks(battery1);
            miband1.gattClose();
            clearUser(1);
            Log.d(TAG, "closeGatt miband1 close");
        } catch (Exception e) {
            Log.d(TAG, "closeGatt miband1 close error=" + e.toString());
        }
    }

    //closeGatt2
    private void closeGatt2() {
        try {
            heartHandler2.removeCallbacks(HeartRateScan2);
            heartHandler2.removeCallbacks(battery2);
            miband2.gattClose();
            clearUser(2);
            Log.d(TAG, "closeGatt miband2 close");
        } catch (Exception e) {
            Log.d(TAG, "closeGatt miband2 close error=" + e.toString());
        }
    }

    //closeGatt3
    private void closeGatt3() {
        try {
            heartHandler3.removeCallbacks(HeartRateScan3);
            heartHandler3.removeCallbacks(battery3);
            miband3.gattClose();
            clearUser(3);
            Log.d(TAG, "closeGatt miband3 close");
        } catch (Exception e) {
            Log.d(TAG, "closeGatt miband3 close error=" + e.toString());
        }
    }

    //clearUser
    private void clearUser(int i) {
        try {
            Log.d(TAG, "clearUser=" + i);
            sql = "delete from log_list where usrname='" + i + "'";
            dbHelper = new DBHelper(context);
            db = dbHelper.getWritableDatabase();
            db.execSQL(sql);
        } catch (Exception e) {
            Log.d(TAG, "clearUser error=" + e.toString());
        }
    }


}
