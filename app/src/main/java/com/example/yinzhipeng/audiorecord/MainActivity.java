package com.example.yinzhipeng.audiorecord;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.drm.DrmStore;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity{
    private final String TAG ="MainActivity";
    private Context context=this;
    private MediaPlayer mMediaPlayer;
    private AudioAttributes mPlaybackAttributes;
    private AudioManager mAudiomanage;
    private AudioFocusRequest mFocusRequest;
    private Button button;

    private Handler mHandler = new Handler();
    final Object mFocusLock = new Object();
    boolean mPlaybackDelayed = false;
    boolean mPlaybackNowAuthorized = false;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private void Media_init() {
        //初始化AudioManager对象
        //Audiomanage = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //申请焦点
        //  mAudioManager.requestAudioFocus(mAudioFocusChange, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        try {
            //获取音频文件
           // FileOutputStream os= new FileOutputStream("/sdcard/semple.mp3");
            //this.getAssets().openFd("littlelucky.mp3");
            AssetFileDescriptor fileDescriptor =  this.getAssets().openFd("semple.mp3");
            // 保持每次打开在文件头部
            fileDescriptor.close();
            fileDescriptor =  this.getAssets().openFd("semple.mp3");
            //实例化MediaPlayer对象
            mMediaPlayer = new MediaPlayer();
            //设置播放流类型
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            //设置播放源，有多个参数可以选择，具体参考相关文档，本文旨在介绍音频焦点
            mMediaPlayer.setDataSource(fileDescriptor.getFileDescriptor(),
                    fileDescriptor.getStartOffset(),
                    fileDescriptor.getLength());
            //设置循环播放
            mMediaPlayer.setLooping(true);
            //准备监听
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    //准备完成后自动播放
                    //mMediaPlayer.start();
                }
            });
            //异步准备
            mMediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void init_AudioFocus(){
        // 获得AudioManager对象  该对象提供访问音量和铃声模式的操作
        mAudiomanage= (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);

        //
        mPlaybackAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build();

        //创建AudioFocusRequest 对象
        mFocusRequest = new AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(mPlaybackAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(mMyFocusListener)
                .build();


    }

    @Override
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Media_init();
        button= (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onClick(View v) {
                if(!mMediaPlayer.isPlaying())
                {
                    start();
                }else{
                    stop();
                }
            }
        });
        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
        init_AudioFocus();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    private AudioManager.OnAudioFocusChangeListener mMyFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch(focusChange){
                case AudioManager.AUDIOFOCUS_GAIN:
                    //焦点回调  开启
                    Log.d(TAG, "onAudioFocusChange: AUDIO_GAIN");
                    start();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    //暂时失去焦点
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    //长时间失去焦点
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    //短暂丢失焦点
                    break;
            }
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void start(){
        int res =mAudiomanage.requestAudioFocus(mFocusRequest);
        button.setText("stop");
        mMediaPlayer.start();
        Log.d(TAG,"media playing!");
    }
    private void stop(){
        button.setText("start");
        mMediaPlayer.pause();
        Log.d(TAG,"media pause!");

    }


}
