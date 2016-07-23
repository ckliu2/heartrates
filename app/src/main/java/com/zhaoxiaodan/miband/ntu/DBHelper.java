package com.zhaoxiaodan.miband.ntu;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.*;

public class DBHelper extends SQLiteOpenHelper {
    public static String DATABASE_NAME = "heartrate.db";
    public static final int DATABASE_VERSION = 65;
    public static final String CREATE_TABLE_SQL = "CREATE TABLE log_list (_id INTEGER PRIMARY KEY, usrname TEXT, heartrate NUMERIC, created_time TIMESTAMP default CURRENT_TIMESTAMP);";
    public static final String CREATE_TABLE_SQL1 = "CREATE TABLE users_list (_id INTEGER PRIMARY KEY, usrname TEXT, usrmac TEXT,sn NUMERIC,range NUMERIC,range1 NUMERIC,created_time TIMESTAMP default CURRENT_TIMESTAMP); ";
    public static final String CREATE_TABLE_SQL2 = "CREATE TABLE device_list (_id INTEGER PRIMARY KEY, devicename TEXT, usrmac TEXT); ";
    public static final String CREATE_TABLE_SQL3 = "CREATE TABLE setting_list (_id INTEGER PRIMARY KEY, serverURL TEXT,voice NUMERIC); ";

    public static final String DROP_TABLE_SQL = "DROP TABLE IF EXISTS users_list";
    public static final String DROP_TABLE_SQL1 = "DROP TABLE IF EXISTS log_list";
    public static final String DROP_TABLE_SQL2 = "DROP TABLE IF EXISTS device_list";
    public static final String DROP_TABLE_SQL3 = "DROP TABLE IF EXISTS setting_list";


    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(CREATE_TABLE_SQL);
            db.execSQL(CREATE_TABLE_SQL1);
            db.execSQL(CREATE_TABLE_SQL2);
            db.execSQL(CREATE_TABLE_SQL3);

            String sql;

            sql = "insert into users_list(usrname,usrmac,sn,range,range1)values('107Z','C8:0F:10:30:14:8F','1',50,120)";
            db.execSQL(sql);

            sql = "insert into users_list(usrname,usrmac,sn,range,range1)values('106Z','C8:0F:10:36:95:EA','2',50,120)";
            db.execSQL(sql);

            sql = "insert into users_list(usrname,usrmac,sn,range,range1)values('108Z','C8:0F:10:35:FC:44','3',50,120)";
            db.execSQL(sql);


            sql = "insert into setting_list(serverURL,voice)values('http://gitlab.caece.net/api/heartrates/receive',0)";
            db.execSQL(sql);

            //List
            String ls[] = new String[]{
                    ("C8:0F:10:30:14:8F"), ("C8:0F:10:36:95:EA"), ("C8:0F:10:35:FC:44"), ("C8:0F:10:36:CB:12"),("C8:0F:10:36:CD:FE"), ("C8:0F:10:36:CA:D9"),
                    ("C8:0F:10:37:35:86"), ("C8:0F:10:33:37:9E"), ("C8:0F:10:37:35:5E"), ("C8:0F:10:37:2B:8E"), ("C8:0F:10:37:3F:80"), ("C8:0F:10:33:3B:EA"),
                    ("C8:0F:10:37:3C:31"), ("C8:0F:10:37:35:12"), ("C8:0F:10:37:2D:12"), ("C8:0F:10:37:33:A3")
            };
            int k = 0;
            for (int j = 1; j <= 16; j++) {
                sql = "insert into device_list(devicename,usrmac)values('" + String.valueOf(j) + "','" + ls[k] + "')";
                db.execSQL(sql);
                k++;
            }


        } catch (Exception e) {
            Log.d("onCreate", e.toString());
        }
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onDropTable(db);
        onCreate(db);
    }

    public void onDropTable(SQLiteDatabase db) {
        db.execSQL(DROP_TABLE_SQL);
        db.execSQL(DROP_TABLE_SQL1);
        db.execSQL(DROP_TABLE_SQL2);
        db.execSQL(DROP_TABLE_SQL3);
    }


}
