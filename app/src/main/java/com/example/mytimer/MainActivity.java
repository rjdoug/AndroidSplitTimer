package com.example.mytimer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    /*
       TODO: BUG - What if it is the first time launching the app and you don't check log for days
        Needs to update as soon as first launch
       TODO: BUG - We have a fix, but figure out the real reason why on resume loadPreferences is
        an issue for totalTime chronometer but not the other one. More info in onActivityResult
        method. Currently feels like a bit of a cheap way out, and I don't want it turning into a
        band-aid problem
       TODO: BUG - Seems to be a clock drift of sorts. Will resync itself when onPause is called
       TODO: On the delete button add an are you sure boxK
     */


    public boolean running = false;

    // holds the index of the current chronometer in use
    private int currentStopwatchIndex = 0;
    // holds the number of stopwatches not including totalTimer
    int stopwatchCount;
    // Holds total accumulate time of all the stopwatches
    Stopwatch stopwatchTotalTime;
    // holds each individual stopwatch
    ArrayList<Stopwatch> stopwatches = new ArrayList<>();
    // holds stopwatches that are preference friendly. Only used to store and load preferences
    ArrayList<PreferenceStopwatch> preferenceStopwatches;
    ArrayList<PreferenceStopwatch> preferenceStopwatchTotalTime;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadPreferences();
        // if first time install ie. no preferences to load from
        if (stopwatchTotalTime == null) {
            stopwatchTotalTime = new Stopwatch(findViewById(R.id.chronometer_total));
        }

        // Just for look - creates a single stopwatch if none are already there
        if (stopwatches.isEmpty()) {
            stopwatches.add(new Stopwatch(findViewById(R.id.chronometer)));
            renameTimer();
        }
        updateStopwatchInfo();
        // -1 from stopwatchCount because the totalTimer counts as one, but we don't want it counted
        // in this instance
        stopwatchCount = stopwatches.size();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //loadPreferences();

        // Hide actionbar
        getSupportActionBar().hide();
    }

    /* if not running, savePreferences then create an intent that will return a result. The return
     * result is to indicate to the app to loadPreferences again */
    public void onClickTimeLog(View view) {
        if (!running) {
            savePreferences();
            Intent i = new Intent(this, TimeLogActivity.class);
            startActivityForResult(i, RequestCode.MAIN_TO_TIME_LOG_INT);
        } else {
            Toast.makeText(getApplicationContext(),"Please stop current stopwatch first",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    /* Where the returning result are sorted */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            /* When new timer is created, there is a prompt that asks for a name. On the finish()
            * of said activity, the input will return to here */
            case (RequestCode.MAIN_TO_NEWTIMER_INT) : {
                if (resultCode == Activity.RESULT_OK) {
                    String newText = data.getStringExtra(RequestCode.MAIN_TO_NEWTIMER_STRING);
                    stopwatches.get(currentStopwatchIndex).setName(newText);
                    updateStopwatchInfo();
                }
                break;
            }
            /* We couldn't load preferences on Resume as it would cause the totalTime to not count
            when outside of app (on pause). If we removed it, it fixed that problem, but then we
            need loadPreferences to be called when we return from TimeLog incase of log reset.
            If we receive the request code on returning from an activity then load preferences
             */
            case (RequestCode.MAIN_TO_TIME_LOG_INT) : {
                loadPreferences();
            }

        }
    }
    // start timer logic
    public void onClickStartButton(View view) {
        if (!running) startAndUpdate();
        else stopAndUpdate();
    }
    // reset timer logic
    public void onClickResetButton(View view) {
        stopwatches.get(currentStopwatchIndex).reset();
        savePreferences();
    }
    // reset total time logic
    public void onClickResetTotalButton(View view) {
        stopwatchTotalTime.reset();
    }

    // Button to right of top timer
    public void onClickNextTimer(View view) {
        // Make sure stopwatch has stopped before moving on
        if (running) {
            Toast.makeText(getApplicationContext(),"Please stop current stopwatch first", Toast.LENGTH_SHORT).show();
            return;
        }
        // if at end of stopwatch list add a new one
        if (currentStopwatchIndex == stopwatchCount - 1) {
            // Capture actions preformed by sub activity
            renameTimer();
            createNewStopwatch();
            // save newly created stopwatch
            savePreferences();
            stopwatchCount++;
        }
        // Shift to newly created stopwatch
        currentStopwatchIndex++;
        updateStopwatchInfo();

        /* If still on the first timer we do not want to see a back button, afterwards turn on.
         Only want to call it once hence the greater than 1. Don't need to call it every event
         call */
        // TODO: Make this conditional better
        if (!(currentStopwatchIndex > 1)) {
            ImageButton backButton = findViewById(R.id.button_previous_timer);
            backButton.setVisibility(View.VISIBLE);
        }
        // If coming back from zero stopwatches to one, then make chonometer visible
        // TODO: Test whether this is actually used
        if (currentStopwatchIndex == 0) {
            Chronometer chronometer = findViewById(R.id.chronometer);
            chronometer.setVisibility(View.VISIBLE);
        }
    }
    /* Check the stopwatch is stopped before switching. Then switch accordingly */
    public void onClickPreviousTimer(View view) {
        // Cannot change timer while chronometer is running
        if (running) {
            Toast.makeText(getApplicationContext(),"Please stop current stopwatch first", Toast.LENGTH_SHORT).show();
            return;
        }
        currentStopwatchIndex--;
        updateStopwatchInfo();
    }

    public void onClickMainTextViewLabel(View view) {

         if(!running) renameTimer();
         else {
             Toast.makeText(getApplicationContext(),"Please stop current stopwatch before " +
                     "renaming", Toast.LENGTH_SHORT).show();
         }
    }



    // Changes chronometer to previous stopwatch. Updates text
    public void onClickRemove(View view) {
        if (stopwatchCount == 1) {
            Toast.makeText(getApplicationContext(),"You must have a minimum of one stopwatch", Toast.LENGTH_SHORT).show();
            return;
        }
        if (running) stopAndUpdate();

        stopwatches.remove(stopwatches.get(currentStopwatchIndex));
        // currentStopwatchIndex of removal. Typically shifts down on removal ie there are 6
        // items, delete index 3, now index is 2.
        if (currentStopwatchIndex == stopwatches.size()) {
            currentStopwatchIndex--;

        }
        stopwatchCount--;
        updateStopwatchInfo();
    }



    // ############### PRIVATE METHODS ###############

    private void createNewStopwatch() {
        stopwatches.add(new Stopwatch(findViewById(R.id.chronometer)));
    }

    /* Updates label above stopwatch
     *  Displays current time of stowpatch - all stopwatches share the same chronometer */
    private void updateStopwatchInfo() {
        TextView stopwatchName = findViewById(R.id.tv_timer_name);
        // if there are stopwatches then update chronometer to match current stopwatch
        if (currentStopwatchIndex != -1) {
            stopwatchName.setText(stopwatches.get(currentStopwatchIndex).getName());
            stopwatches.get(currentStopwatchIndex).restoreStopwatch();
        } else {
            stopwatchName.setText("");
            Chronometer chronometer = findViewById(R.id.chronometer);
            chronometer.setVisibility(View.INVISIBLE);
        }
        previousButtonUpdateUI();
        nextButtonUpdateUI();
        savePreferences();
    }

    private void loadPreferences() {
        preferenceStopwatches = Preferences.loadArray(Preferences.TIMER_SAVE_LOCATION,
                this, Preferences.TIMER_ITEMS);
        stopwatches = new ArrayList<>();
        for (PreferenceStopwatch s : preferenceStopwatches) {
            stopwatches.add(s.reloadStopwatch(findViewById(R.id.chronometer)));
        }

        // load total stopwatch timer
        preferenceStopwatchTotalTime = Preferences.loadArray(Preferences.TIMER_SAVE_LOCATION,
                this, Preferences.MAIN_TIMER);
        // if array is empty ie first time install then do nothing - try catch ignore
        try {
            stopwatchTotalTime = preferenceStopwatchTotalTime.get(0).reloadStopwatch(
                    findViewById(R.id.chronometer_total));
            stopwatchTotalTime.restoreStopwatch();
        } catch (Exception e) {}



    }

    private void savePreferences() {
        // Convert stand stopwatch to preference friendly stopwatch type
        preferenceStopwatches = new ArrayList<>();
        for (Stopwatch s : stopwatches) {
            preferenceStopwatches.add(new PreferenceStopwatch(s));
        }
        Preferences.saveArray(preferenceStopwatches.toArray
                        (new PreferenceStopwatch[0]), Preferences.TIMER_SAVE_LOCATION,
                this, Preferences.TIMER_ITEMS);

        preferenceStopwatchTotalTime = new ArrayList<>();
        preferenceStopwatchTotalTime.add(new PreferenceStopwatch(stopwatchTotalTime));
        Preferences.saveArray(preferenceStopwatchTotalTime.toArray(new PreferenceStopwatch[0]),
                Preferences.TIMER_SAVE_LOCATION, this, Preferences.MAIN_TIMER);
    }

    // Updates the next button depending on whether there are timers after current
    private void nextButtonUpdateUI() {
        ImageButton button = findViewById(R.id.button_next_timer);
        if (currentStopwatchIndex == stopwatches.size() - 1)
            button.setImageResource(R.drawable.ic_baseline_add_24);
        else button.setImageResource(R.drawable.ic_baseline_arrow_forward_24);
    }

    private void previousButtonUpdateUI() {
        ImageButton button = findViewById(R.id.button_previous_timer);
        if (currentStopwatchIndex < 1) {
            button.setVisibility(View.INVISIBLE);
        }
        else button.setVisibility(View.VISIBLE);
    }

    private void renameTimer() {
        Intent i = new Intent(this, NewTimerActivity.class);
        startActivityForResult(i, RequestCode.MAIN_TO_NEWTIMER_INT);
    }

    private void stopAndUpdate() {
        TextView startStopButton = (TextView) findViewById(R.id.button_start);
        stopwatchTotalTime.stop();
        stopwatches.get(currentStopwatchIndex).stop();
        startStopButton.setText(R.string.start_button_label);
        savePreferences();
        running = false;
    }

    private void startAndUpdate() {
        TextView startStopButton = (TextView) findViewById(R.id.button_start);
        stopwatches.get(currentStopwatchIndex).start();
        stopwatchTotalTime.start();
        startStopButton.setText(R.string.stop_button_label);
        running = true;
    }
}