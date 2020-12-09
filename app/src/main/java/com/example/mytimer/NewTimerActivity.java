package com.example.mytimer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class NewTimerActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_timer_overlay);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int) (width * 0.8), (int) (height * 0.5));

        // Center in middle of screen
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -20;

        getWindow().setAttributes(params);

        EditText et = findViewById(R.id.new_timer_et_name);
        et.requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING |
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);



    }

    protected void onResume() {
        super.onResume();

        // Hide actionbar
        getSupportActionBar().hide();



    }

    @Override
    public void onBackPressed() {}

    public void onClickNewTimerSubmit(View view) {
        EditText timerName = findViewById(R.id.new_timer_et_name);

        Intent resultIntent = new Intent();

        resultIntent.putExtra(RequestCode.MAIN_TO_NEWTIMER_STRING, timerName.getText().toString());
        setResult(Activity.RESULT_OK, resultIntent);
        finish();

    }

}
