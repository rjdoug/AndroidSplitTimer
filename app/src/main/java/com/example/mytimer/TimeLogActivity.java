package com.example.mytimer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimeLogActivity extends AppCompatActivity {

    ArrayList<PreferenceStopwatch> preferenceStopwatches;
    ArrayList<PreferenceStopwatch> preferenceStopwatchTotalTime;
    ArrayList<PreferenceStopwatch> stopwatches;
    String lastLogReset;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_log);
        loadPreferences();
        updateListView();

        // Set total time
        TextView totalTime = findViewById(R.id.log_tv_total_time);
        totalTime.setText(formatTime(preferenceStopwatchTotalTime.get(0).getTotalTime()));

        TextView tvLastLogReset = findViewById(R.id.log_tv_last_reset);
        tvLastLogReset.setText(lastLogReset);

        if (tvLastLogReset.getText() == "") resetTimerLog();
    }

    protected void onResume() {
        super.onResume();
        // Hide actionbar
        getSupportActionBar().hide();

        // Set last log date

    }

    // Reset accumulating time
    public void onClickTimeLogReset(View view) {
        for (PreferenceStopwatch s : stopwatches) {
            s.resetTotalTime();
        }
        preferenceStopwatchTotalTime.get(0).resetTotalTime();
        // Set total time
        TextView totalTime = findViewById(R.id.log_tv_total_time);
        totalTime.setText(formatTime(preferenceStopwatchTotalTime.get(0).getTotalTime()));

        savePreferences();
        updateListView();
        resetTimerLog();

    }

    // get current time and date and set it to the last reset label/textview. Then save
    public void resetTimerLog() {
        TextView lastLogReset = findViewById(R.id.log_tv_last_reset);
        String timeStamp = new SimpleDateFormat("dd/MM/yyyy E HH:mm").format(Calendar.getInstance().getTime());
        String format = String.format(timeStamp);
        lastLogReset.setText(format);
        Preferences.saveString(Preferences.LOG, this, format, Preferences.LOG_LAST_RESET);
    }

    public void onClickLogBack(View view) {
        finish();
    }

    private void savePreferences() {
        // Convert stand stopwatch to preference friendly stopwatch type
        Preferences.saveArray(stopwatches.toArray
                        (new PreferenceStopwatch[0]), Preferences.TIMER_SAVE_LOCATION,
                this, Preferences.TIMER_ITEMS);

        Preferences.saveArray(preferenceStopwatchTotalTime.toArray(new PreferenceStopwatch[0]),
                Preferences.TIMER_SAVE_LOCATION, this, Preferences.MAIN_TIMER);
    }

    private void loadPreferences() {
        preferenceStopwatches = Preferences.loadArray(Preferences.TIMER_SAVE_LOCATION,
                this, Preferences.TIMER_ITEMS);
        stopwatches = new ArrayList<>();
        stopwatches.addAll(preferenceStopwatches);

        // load total stopwatch timer
        preferenceStopwatchTotalTime = Preferences.loadArray(Preferences.TIMER_SAVE_LOCATION,
                this, Preferences.MAIN_TIMER);

        lastLogReset = Preferences.loadString(Preferences.LOG, this,
                Preferences.LOG_LAST_RESET);




    }

    static class ViewHolder {
        TextView name;
        TextView time;
    }

    public class ListItem {
        public String tv_name;
        public String tv_time;

        public ListItem (String tv_name, String tv_time) {
            this.tv_name = tv_name;
            this.tv_time = tv_time;
        }
    }

    public class CustomAdaptor extends ArrayAdapter<ListItem> {

        int resource;
        private Context context;

        public CustomAdaptor(Context context, int resource, ArrayList<ListItem> listItems) {
            super(context, resource, listItems);
            this.resource = resource;
            this.context = context;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ViewHolder holder;
            // get info
            String name = getItem(position).tv_name;
            String time = getItem(position).tv_time;

            // if not visible
            if(convertView == null){
                // Make visible
                LayoutInflater inflater = LayoutInflater.from(context);
                convertView = inflater.inflate(resource, parent, false);

                holder = new ViewHolder();

                holder.name = (TextView) convertView.findViewById(R.id.log_lv_name);
                holder.time = (TextView) convertView.findViewById(R.id.log_lv_time);

                convertView.setTag(holder);
            } else holder = (ViewHolder) convertView.getTag();

            // Set textviews
            holder.name.setText(name);
            holder.time.setText("" + time);

            return convertView;
        }

    }

    public void updateListView() {
        ArrayList<ListItem> listItems = new ArrayList<>();
        for (int i = 0; i < stopwatches.size(); i++) {
            try {
                listItems.add(new ListItem(stopwatches.get(i).getName(), formatTime(stopwatches.get(i).getTotalTime())));
            } catch (Exception e) { Log.d("testing", "" + e); }
        }

        CustomAdaptor arrayAdapter = new CustomAdaptor(this, R.layout.adapter_time_log_list, listItems);
        ListView transactionListView = (ListView) findViewById(R.id.log_listview);
        transactionListView.setAdapter(arrayAdapter);
    }

    @SuppressLint("DefaultLocale")
    private String formatTime(long millis) {
        int seconds = (int) (millis / 1000) % 60 ;
        int minutes = (int) ((millis / (1000*60)) % 60);
        int hours   = (int) ((millis / (1000*60*60)));
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }


}
