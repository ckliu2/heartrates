package wristband.device.com.heartrate;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.graphics.Color;

import android.os.Handler;
import android.os.Message;

import com.zhaoxiaodan.miband.ntu.User;

public class UserAdapter extends BaseAdapter {

    // 定義 LayoutInflater
    private LayoutInflater myInflater;
    // 定義 Adapter 內藴藏的資料容器
    private ArrayList<User> list;

    String TAG="UserAdapter";


    public UserAdapter(Context context, ArrayList<User> list) {
        //預先取得 LayoutInflater 物件實體
        myInflater = LayoutInflater.from(context);
        this.list = list;
    }

    @Override
    public int getCount() { // 公定寫法(取得List資料筆數)
        return list.size();
    }

    @Override
    public Object getItem(int position) { // 公定寫法(取得該筆資料)
        return list.get(position);
    }

    @Override
    public long getItemId(int position) { // 公定寫法(取得該筆資料的position)
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        try{



        ViewHolder holder;

        if (convertView == null) {
            // 1:將 R.layout.row 實例化
            convertView = myInflater.inflate(R.layout.row, null);

            //預設斷線
            //convertView.setBackgroundColor(Color.parseColor("#CCCCCC"));
           
            // 2:建立 UI 標籤結構並存放到 holder
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.heartRate = (TextView) convertView.findViewById(R.id.heartRate);
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            holder.responseTime = (TextView) convertView.findViewById(R.id.responseTime);
            holder.battery = (TextView) convertView.findViewById(R.id.battery);
            holder.heartRateAVG = (TextView) convertView.findViewById(R.id.heartRateAVG);
            holder.deviceName = (TextView) convertView.findViewById(R.id.deviceName);

            // 3:注入 UI 標籤結構 --> convertView
            convertView.setTag(holder);

        } else {
            // 取得  UI 標籤結構
            holder = (ViewHolder) convertView.getTag();
        }

        // 4:取得Fastfood物件資料
        User user = list.get(position);

        // 5:設定顯示資料
        holder.name.setText("Room no : " + user.getName());
        holder.heartRate.setText("Heart Rate(bpm) : " + user.getMyHeartRate());
        holder.image.setImageResource(user.getImageId());
        holder.responseTime.setText(user.getResponseTime());
        holder.battery.setText("Battery(%) : " + user.getBatteryCapacity());
        holder.heartRateAVG.setText("AVG(bpm) : " + user.getHeartRateAVG());
        String device = user.getDeviceName();
        holder.deviceName.setText(user.getDeviceName());

        //顯示色塊
        try {
            Log.d("color", "color avg=" + user.getHeartRateAVG() + "--range=" + user.getRange() + "---range1=" + user.getRange1());

            //斷線
            if (user.getHeartRateAVG().equals("--")) {
                convertView.setBackgroundColor(Color.parseColor("#CCCCCC"));
                user.setAvgStatus(0);
                holder.image.setImageResource(R.drawable.discon);
            } else {
                if (Integer.parseInt(user.getHeartRateAVG()) >= user.getRange() && Integer.parseInt(user.getHeartRateAVG()) <= user.getRange1()) {
                    user.setAvgStatus(1);
                    Log.d("color", "OK");
                    convertView.setBackgroundColor(Color.WHITE);
                } else {
                    Log.d("color", "fail");
                    user.setAvgStatus(2);
                    convertView.setBackgroundColor(Color.parseColor("#FF9999"));
                }
            }
        } catch (Exception e) {
            Log.d("setBackgroundColor", "setBackgroundColor error=" + e.toString());
        }

        }catch (Exception e){
            Log.d(TAG,"UserAdapter getView error="+e.toString());
        }

        return convertView;
    }

    // UI 標籤結構
    static class ViewHolder {
        TextView name;
        TextView heartRate;
        ImageView image;
        TextView responseTime;
        TextView battery;
        TextView heartRateAVG;
        TextView deviceName;
    }
}