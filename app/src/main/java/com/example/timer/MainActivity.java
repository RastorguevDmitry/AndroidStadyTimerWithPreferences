package com.example.timer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.UnicodeSetSpanner;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    SeekBar seekBar;
    TextView textView;
    private boolean isTimerOn;
    private Button button;
    private CountDownTimer countDownTimer;
    private int defaultInterval;
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        seekBar = findViewById(R.id.seekBar);
        textView = findViewById(R.id.textView);
        button = findViewById(R.id.button);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        seekBar.setMax(600);
        setIntervalFromSharedPreferences(sharedPreferences);
        isTimerOn = false;

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                updateTimer(i * 1000);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

    }

    public void start(View view) {

        if (!isTimerOn) {
            button.setText("stop");
            seekBar.setEnabled(false);
            isTimerOn = true;

            countDownTimer = new CountDownTimer(seekBar.getProgress() * 1000, 1000) {
                @Override
                public void onTick(long milesUnpilFinish) {
                    updateTimer(milesUnpilFinish);
                }

                @Override
                public void onFinish() {
                    SharedPreferences sharedPreferences =
                            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    if (sharedPreferences.getBoolean("enable_sound", true)) {
                        MediaPlayer mediaPlayer = null;
                        String melodyName = sharedPreferences.getString("timer_melody", "bell");
                        if (melodyName.equals("bell")) {
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bell_sound);
                        } else if (melodyName.equals("alarm_siren")) {
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.alarm_siren_sound);
                        } else if (melodyName.equals("bip")) {
                            mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.bip_sound);
                        }
                        mediaPlayer.start();
                    }
                    resetTimer();
                }
            };
            countDownTimer.start();
        } else {
            resetTimer();
        }
    }

    private void updateTimer(long milesUnpilFinish) {
        int minuts = (int) milesUnpilFinish / 1000 / 60;
        int seconds = (int) (milesUnpilFinish / 1000 - (minuts * 60));
        String minutesString = "";
        String secondsString = "";

        if (minuts < 10) {
            minutesString = "0" + minuts;
        } else minutesString = "" + minuts;

        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else secondsString = "" + seconds;

        textView.setText(minutesString + ":" + secondsString);

    }

    private void resetTimer() {
        countDownTimer.cancel();
        button.setText("start");
        seekBar.setEnabled(true);
        seekBar.setMax(600);
        isTimerOn = false;
        setIntervalFromSharedPreferences(sharedPreferences);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.timer_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent openSettings = new Intent(this, SettingsActivity.class);
            startActivity(openSettings);
            return true;
        } else if (id == R.id.about) {
            Intent openSAbout = new Intent(this, AboutActivity.class);
            startActivity(openSAbout);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setIntervalFromSharedPreferences(SharedPreferences sharedPreferences) {
        defaultInterval = Integer.valueOf(sharedPreferences.getString("timer_default_interval", "30"));
        updateTimer(defaultInterval * 1000);
        seekBar.setProgress(defaultInterval);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals("timer_default_interval")) {
            setIntervalFromSharedPreferences(sharedPreferences);
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }
}
