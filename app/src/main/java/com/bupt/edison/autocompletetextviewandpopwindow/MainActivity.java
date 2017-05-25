package com.bupt.edison.autocompletetextviewandpopwindow;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    @Bind(R.id.typeText)
    FormatAutoCompleteTextView typeText;

    private static final String[] COUNTRIES = new String[]{
            "1234567890AB", "12345678CDEF", "12345678CD90", "1234987690AB", "12345690AB90"
    };
    @Bind(R.id.play)
    Button play;
    @Bind(R.id.stop)
    Button stop;
    @Bind(R.id.oldEdit)
    EditText oldEdit;
    @Bind(R.id.readText)
    Button readText;
    @Bind(R.id.putText)
    Button putText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

//        simpleAutoCompleteTextView();
        powerfulAutoCompleteTextView();
    }

    //AutoCompleteTextView初级用法，https://developer.android.com/training/keyboard-input/style.html
    private void simpleAutoCompleteTextView() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        typeText.setAdapter(adapter);
        typeText.setThreshold(0);
    }

    ArrayList<BankCardModel> models;

    //自定义AutoCompleteTextView - ，功能强大。详见 FormatAutoCompleteTextView
    private void powerfulAutoCompleteTextView() {
        BankCardModel model1 = new BankCardModel();
        model1.setBankCardNo("1234567890AB");
        model1.setBankName("特朗普");
        BankCardModel model2 = new BankCardModel();
        model2.setBankCardNo("12345678CDEF");
        model2.setBankName("普京");
        BankCardModel model3 = new BankCardModel();
        model3.setBankCardNo("12345678CD90");
        model3.setBankName("埃尔多安");
        BankCardModel model4 = new BankCardModel();
        model4.setBankCardNo("1234987690AB");
        model4.setBankName("习大大");
        BankCardModel model5 = new BankCardModel();
        model5.setBankCardNo("12345690AB90");
        model5.setBankName("布朗");

        models = new ArrayList<>();
        models.add(model1);
        models.add(model2);
        models.add(model3);
        models.add(model4);
        models.add(model5);

        BankCardFilterAdapter adapter = new BankCardFilterAdapter(MainActivity.this, models);
        adapter.setFormatAutoCompleteTextView(typeText);
        typeText.setAdapter(adapter); //如果不设置adapter，不会有自动补全功能。
        typeText.setThreshold(1);
        typeText.setOnItemClickListener(this); //设置自动补全列表的点击事件

        //调整自动补全列表的大小和位置
        typeText.setDropDownHorizontalOffset(-dipToPx(60));
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        typeText.setDropDownWidth(displayMetrics.widthPixels - dipToPx(30));
    }

    private int dipToPx(int dip) {
        final DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return (int) (displayMetrics.density * dip + 0.5f);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        typeText.setContentText(models.get(position).getBankCardNo());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }

    MediaPlayer mediaPlayer;

    /**
     * RingtoneManager.TYPE_NOTIFICATION;   通知声音
     * RingtoneManager.TYPE_ALARM;  警告
     * RingtoneManager.TYPE_RINGTONE; 铃声
     *
     * @param context
     * @param position
     */
    public void playSystemRing(Context context, int position) {
        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        RingtoneManager ringtoneManager = new RingtoneManager(context);
        ringtoneManager.setType(RingtoneManager.TYPE_ALARM);
        ringtoneManager.getCursor();
        Uri ringUri = ringtoneManager.getRingtoneUri(position);

        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, 7, AudioManager.ADJUST_SAME);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(context, ringUri);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setLooping(true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopPlay() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    @OnClick({R.id.play, R.id.stop, R.id.readText, R.id.putText})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.play:
                playSystemRing(MainActivity.this, 1);
                break;
            case R.id.stop:
                stopPlay();
                break;
            case R.id.readText:
                Toast.makeText(this, typeText.getRealText(), Toast.LENGTH_SHORT).show();
                break;
            case R.id.putText:
                typeText.setContentText("ABCD1234abcd5678");
                break;
        }
    }
}
