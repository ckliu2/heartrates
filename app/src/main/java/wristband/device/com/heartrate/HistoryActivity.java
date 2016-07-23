package wristband.device.com.heartrate;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.*;
import com.zhaoxiaodan.miband.ntu.*;
import java.util.*;
import android.view.*;


public class HistoryActivity extends AppCompatActivity {

    String TAG="HistoryActivity";
    private DBHelper dbHelper;
    private SQLiteDatabase db;
    private Cursor maincursor;
    private Context context;
    ListView listView;
    TextView usrname,avg,usercount;
    String name="";
    Vector heartrate1=new Vector();
    Vector times1=new Vector();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        context = this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        Bundle extras=getIntent().getExtras();
        int itemId=extras.getInt("itemId");
        Log.d(TAG,"HistoryActivity itemId="+itemId);

        usrname=(TextView)findViewById(R.id.userId);
        avg=(TextView)findViewById(R.id.avg);
        usercount=(TextView)findViewById(R.id.usercount);


        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();

        String sql="SELECT _id, usrname, usrmac FROM users_list where _id='"+itemId+"'";
        Log.d(TAG,"HistoryActivity sql="+sql);
        Cursor findEntry = db.rawQuery(sql, null);
        while (findEntry.moveToNext()) {
            name=findEntry.getString(1);
            usrname.setText(name+" (bpm/3mins)");
        }
        findEntry.close();



        String sql1="SELECT usrname,heartrate,datetime(created_time,'localtime')  FROM log_list where usrname='"+itemId+"' and created_time >= datetime('now','-3 minutes') order by _id desc ";

        Log.d(TAG, "sql1=" + sql1);

        int sum=0;
        int count=0;
        Cursor findEntry1 = db.rawQuery(sql1, null);
        while (findEntry1.moveToNext()) {
            heartrate1.addElement(findEntry1.getString(1));
            times1.addElement(findEntry1.getString(2));
            sum+=findEntry1.getInt(1);
            Log.d(TAG, "heartrate1=" + findEntry1.getString(1));
            Log.d(TAG,"times1="+findEntry1.getString(2));
            count++;
        }
        findEntry1.close();
        db.close();


        if(count==0){
            avg.setText("Average : -- (bpm/3mins)");
        }else{
            avg.setText("Average : " + (int) sum / count + "(dpm)");
        }
        usercount.setText("Items : " +  count );



        listView = (ListView) findViewById(R.id.listView);
        List<HashMap<String , String>> list = new ArrayList<>();

        for(int i = 0 ; i < times1.size() ; i++){
            HashMap<String , String> hashMap = new HashMap<>();
            hashMap.put("title" , String.valueOf(heartrate1.get(i)));
            hashMap.put("text" , String.valueOf(times1.get(i)));
            list.add(hashMap);
        }

        ListAdapter listAdapter = new SimpleAdapter(
                this,
                list,
                android.R.layout.simple_list_item_2 ,
                new String[]{"title" , "text"} ,
                new int[]{android.R.id.text1 , android.R.id.text2});

        listView.setAdapter(listAdapter);


        //返回主畫面
        ((Button) findViewById(R.id.returnBtn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(context, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }
}
