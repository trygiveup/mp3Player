package com.example.mp3player;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PlayerActivity extends AppCompatActivity implements View.OnClickListener {
    private String TAG = "PlayerActivity";
    private TextView titleText;
    private ImageView titleImg;
    private ImageButton playBtn;
    private ImageButton stopBtn;
    private ImageButton nextsongBtn;
    private ImageButton previoussongBtn;
    private ArrayList<String> mp3Index;
    private boolean readFromSD;
    private boolean pause;
    private MediaPlayer mediaPlayer;
    private boolean isComplete;
    ExecutorService es = Executors.newSingleThreadExecutor();
    private int positionNow;
    private int currentPosition;  //紀錄目前撥放歌曲的位址
    private ArrayList<String> mp3Names;
    private int position;
    private TextView startTimeText;
    private TextView finalTimeText;
    private SeekBar timeSeek;
    private Handler timeHandler;


    private GestureDetector gestureDetector;
    final int Right = 1;
    final int Left = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);
        Bundle bundle = getIntent().getExtras();
        mp3Index = bundle.getStringArrayList("mp3Index");
        readFromSD = bundle.getBoolean("readFromSD");
        positionNow = bundle.getInt("positionNow");
        mp3Names = bundle.getStringArrayList("mp3Names");
        position = positionNow;
        Log.d(TAG, mp3Names + " " + mp3Index);
        findViews();
        startMusic();
        timeHandler = new Handler(Looper.getMainLooper());
        gestureDetector = new GestureDetector(PlayerActivity.this, onGestureListener);
    }


    public void findViews() {
        titleText = findViewById(R.id.title_text);
        titleImg = findViewById(R.id.title_img);
        playBtn = findViewById(R.id.play_btn);
        stopBtn = findViewById(R.id.stop_btn);
        nextsongBtn = findViewById(R.id.nextsong_btn);
        previoussongBtn = findViewById(R.id.previousong_btn);
        startTimeText = findViewById(R.id.start_text);
        finalTimeText = findViewById(R.id.end_text);
        timeSeek = findViewById(R.id.time_seek);
        playBtn.setEnabled(false);
        stopBtn.setEnabled(false);
        titleText.setText(mp3Names.get(positionNow));
        playBtn.setOnClickListener(this);
        stopBtn.setOnClickListener(this);
        nextsongBtn.setOnClickListener(this);
        previoussongBtn.setOnClickListener(this);
        timeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if (mediaPlayer == null) {
                    seekBar.setProgress(0);
                    return;
                }

                mediaPlayer.seekTo(seekBar.getProgress());

                String startTime = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getCurrentPosition()),
                        TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getCurrentPosition()));
                startTimeText.setText(startTime);
            }
        });
    }

    public void startMusic() {
        Bitmap image = getBitmapFromSDCard(mp3Names.get(position));
        if (image != null) {
            titleImg.setImageBitmap(image);
        } else {
            titleImg.setImageResource(R.drawable.title);
        }
        imgChange(titleImg);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        playBtn.setEnabled(true);
                        stopBtn.setEnabled(true);
                        playMusic(mp3Index.get(position));
                        setSeekTime();
                    }
                });
            }
        }).start();
    }


    private static Bitmap getBitmapFromSDCard(String file) {
        try {
            String sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).toString();

            return BitmapFactory.decodeFile(sd + "/" + file + ".jpg");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void playMusic(String[] index) {
        if (readFromSD) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(String.valueOf(index));
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
                mediaPlayer.stop();
            }
        } else {
            mediaPlayer = MediaPlayer.create(this, Integer.parseInt(String.valueOf(index)));
        }

//        mediaPlayer.setLooping(true);   是否要連續撥放

        mediaPlayer.start();
        playBtn.setImageResource(R.drawable.music_pause);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                nextSong();
            }
        });
    }


    public void playMusic(String index) {
        if (readFromSD) {
            mediaPlayer = new MediaPlayer();
            try {
                mediaPlayer.setDataSource(index);  //setDateSource 直接呼叫create函式例項化一個MediaPlayer物件,播放位於res/raw/test.mp3檔案
                mediaPlayer.prepare();             //或用路徑 此種方法是通過res轉換成uri然後呼叫setDataSource()方法,需要注意格式Uri.parse
            } catch (IOException e) {
                e.printStackTrace();
                mediaPlayer.stop();
            }
        } else {
            mediaPlayer = MediaPlayer.create(this, Integer.parseInt(index));
        }

//        mediaPlayer.setLooping(true);是否要連續撥放

        mediaPlayer.start();
        playBtn.setImageResource(R.drawable.music_pause);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                playBtn.setImageResource(R.drawable.music_play);
//                Toast.makeText(PlayerActivity.this, R.string.complete, Toast.LENGTH_SHORT).show();
                nextSong();
            }//两个静态变量，分别对应Toast.LENGTH_LONG（3.5秒）和Toast.LENGTH_SHORT（2秒）的值
        });
    }

    public void stopMusic() {

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        startTimeText.setText("00:00");
        timeSeek.setProgress(0);
        playBtn.setImageResource(R.drawable.music_play);
    }

    public void nextSong() {

        stopMusic();
        position++;
        this.position = position;

        try {
            playMusic(mp3Index.get(position));
        } catch (Exception e) {
            position = 0;
            playMusic(mp3Index.get(position));
        }
        setSeekTime();
        Bitmap image = getBitmapFromSDCard(mp3Names.get(position));
        if (image != null) {
            titleImg.setImageBitmap(image);
        } else {
            titleImg.setImageResource(R.drawable.title);
        }
        imgTest(titleImg);
        titleText.setText(mp3Names.get(position));
    }

    public void previouSong(int position) {
        stopMusic();
        position--;
        this.position = position;
        try {
            playMusic(mp3Index.get(position));
        } catch (Exception e) {
            position = mp3Names.size() - 1;
            playMusic(mp3Index.get(position));
        }
        setSeekTime();
        Bitmap image = getBitmapFromSDCard(mp3Names.get(position));
        if (image != null) {
            titleImg.setImageBitmap(image);
        } else {
            titleImg.setImageResource(R.drawable.title);
        }
        imgTest1(titleImg);
        titleText.setText(mp3Names.get(position));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.play_btn:
                if (mediaPlayer == null) {
                    playMusic(mp3Names.get(positionNow));
                    setSeekTime();
                    return;
                }

                if (!mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                    playBtn.setImageResource(R.drawable.music_pause);

                } else {
                    mediaPlayer.pause();
                    playBtn.setImageResource(R.drawable.music_play);
                    return;
                }
                break;
            case R.id.stop_btn:
                stopMusic();
                finish();
                break;
            case R.id.nextsong_btn:
                nextSong();
                break;
            case R.id.previousong_btn:
                previouSong(position);
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {//案返回鍵後執行的程序
            timeHandler.removeCallbacksAndMessages(null);
            stopMusic();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void imgChange(ImageView iv) {
        ScaleAnimation animation;
        animation = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f);
        //(fromX,toX)X軸從fromX的倍率放大/縮小至toX的倍率
        //(fromY,toY)X軸從fromY的倍率放大/縮小至toX的倍率
        animation.setDuration(1000);//設定動畫開始到結束的執行時間
        animation.setRepeatCount(0); //設定重複次數 -1為無限次數 0
        animation.setFillAfter(true);
        iv.startAnimation(animation);
    }

    private void imgTest(ImageView v) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, "translationX", -500, 600, -400, 0);
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(v, "rotationY", 0.0f, 360.0f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animator, animator1);
        set.setDuration(3000);
        set.start();   //alpha 0,1,0 半透明  translationX  0,200,-200,0 位移 rotationY  0.0f, 360.0f  旋轉
    }

    private void imgTest1(ImageView v) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(v, "translationX", 500, -600, 400, 0);
        ObjectAnimator animator1 = ObjectAnimator.ofFloat(v, "rotationY", 0.0f, -360.0f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animator, animator1);
        set.setDuration(3000);
        set.start();   //alpha 0,1,0 半透明  translationX  0,200,-200,0 位移 rotationY  0.0f, 360.0f  旋轉
    }

    public void setSeekTime() {
        timeSeek.setMax(mediaPlayer.getDuration());
        String startTime = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getCurrentPosition()),
                TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getCurrentPosition()));

        startTimeText.setText(startTime);

        String finalTime = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes((long) mediaPlayer.getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getDuration()));

        finalTimeText.setText(finalTime);

        timeSeek.setProgress(mediaPlayer.getCurrentPosition());


        //timeHandler.post(updateTime);
        timeHandler.postDelayed(updateTime, 50);
    }

    private Runnable updateTime = new Runnable() {
        @Override
        public void run() {

            if (isComplete || mediaPlayer == null) {
                return;
            }

            String startTime = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(mediaPlayer.getCurrentPosition()),
                    TimeUnit.MILLISECONDS.toSeconds(mediaPlayer.getCurrentPosition()));

            startTimeText.setText(startTime);

            timeSeek.setProgress(mediaPlayer.getCurrentPosition());
            timeHandler.postDelayed(this, 50);
        }
    };


//    將MotionEvent事件處理交給gestureDetector物件
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                System.out.println(" ACTION_DOWN");//手指在螢幕上按下
                break;
            case MotionEvent.ACTION_MOVE:
                System.out.println(" ACTION_MOVE");//手指正在螢幕上滑動
                break;
            case MotionEvent.ACTION_UP:
                System.out.println(" ACTION_UP");//手指從螢幕擡起了
                break;
            default:
                break;
        }
        return gestureDetector.onTouchEvent(event);
    }

    private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            //e1就是初始狀態的MotionEvent物件，e2就是滑動了過後的MotionEvent物件
            //velocityX和velocityY就是滑動的速率
            float x = e2.getX() - e1.getX();
            //滑動後的x值減去滑動前的x值 就是滑動的橫向水平距離(x)
            float y = e2.getY() - e1.getY();
            //滑動後的y值減去滑動前的y值 就是滑動的縱向垂直距離(y)
            if (x > 100) {
                doResult(Right);
                Log.w("tag", "RIGHT>" + x);
            }
            if (x < -100) {
                doResult(Left);
                Log.w("tag", "LEFT>" + x);
            }
            return true;
        }
    };

    public void doResult(int action) {
        switch (action) {
            case Right:
                stopMusic();
                previouSong(position - 1);
            case Left:
                stopMusic();
                nextSong();
        }
    }
}
    /**
     * 手勢滑動測試demo
     * 步驟，
     * 1、例項化GestureDetector物件
     * 2、例項化 GestureDetector.OnGestureListener 手勢監聽物件
     * 3、覆寫onTouchEvent方法，在onTouchEvent方法中將event物件傳給gestureDetector.onTouchEvent(event);處理。
     */
    /**
     * 要實現手指在螢幕上左右滑動的事件需要例項化物件GestureDetector，new GestureDetector(MainActivity.this,onGestureListener);
     * 首先實現監聽物件GestureDetector.OnGestureListener，根據x或y軸前後變化座標來判斷是左滑動還是右滑動
     * 並根據不同手勢滑動做出事件處理doResult（int action），
     然後覆寫onTouchEvent方法，在onTouchEvent方法中將event物件傳給gestureDetector.onTouchEvent(event);處理。
     */


