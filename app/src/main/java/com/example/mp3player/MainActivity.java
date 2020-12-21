package com.example.mp3player;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private ListView mp3List;
    private String[] mp3Name;
    private Button readBtn;
    private boolean readFromSD;
    private ArrayList<String> mp3Names = new ArrayList<>();
    private ArrayList<String> resId = new ArrayList<>();
    private int positionNow;//歌曲所在目前陣列的位址
    private String TAG = "MainActivity";


    public void findView() {
        mp3List = findViewById(R.id.mp3_list);
        readBtn = findViewById(R.id.read_btn);
        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readFromSD = !readFromSD;
                if (readFromSD) {
                    getPermission();
                } else {
                    readRawFile();
                }
                setData();
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {    //當Activity準備要產生時，先呼叫onCreate方法。
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findView();
        readRawFile();
        setDate();
    }

    //這是RAW的列表,從raw獲取的資料列表化
    public void setDate() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mp3Names);
        //ArrayAdapter是最基本的方法，原則上只在練習ListView時候才會用到它。ArrayAdapter原理非常簡單就是宣告一個陣列把要值塞進去，接著ListView就會依照順序顯示出來
        mp3List.setAdapter(adapter);
        mp3List.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long l) {
                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                Bundle bundle = new Bundle();
                positionNow = i;
                bundle.putStringArrayList("mp3Index", resId);
                bundle.putStringArrayList("mp3Names", mp3Names);
                bundle.putInt("positionNow", positionNow);

                Toast.makeText(MainActivity.this, mp3Names.get(positionNow), Toast.LENGTH_SHORT).show();

                intent.putExtras(bundle);
                startActivity(intent);

            }
        });
    }

    //手機內部歌曲陣列化
    public void readRawFile() {
        mp3Names.clear();
        resId.clear();
        mp3Name = new String[]{"我們不一樣", "體面", "說散就散", "紅蓮華"};
        mp3Names.add("我們不一樣");
        mp3Names.add("體面");
        mp3Names.add("說散就散");
        mp3Names.add("紅蓮華");

        resId.add(String.valueOf(R.raw.mp3_0));
        resId.add(String.valueOf(R.raw.mp3_1));
        resId.add(String.valueOf(R.raw.mp3_2));
        resId.add(String.valueOf(R.raw.mp3_3));

        readBtn.setText(R.string.from_sd);
    }

    //保護協定的設置,無此動作讀取不到手機內部資料
    private void getPermission() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            readSDMp3File();
            Toast.makeText(this, R.string.author_error, Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.author_done, Toast.LENGTH_SHORT).show();
                readSDMp3File();
            } else {
                Toast.makeText(this, R.string.author_fail, Toast.LENGTH_SHORT).show();
                readFromSD = false;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }




    //    讀取SD內部音樂檔
    public void readSDMp3File() {
        FilenameFilter filter = new FilenameFilter() {
            private String[] filter = {
                    "mp3", "ogg", "mp4", "wmv"
            };

            @Override
            public boolean accept(File dir, String filename) {
                for (int i = 0; i < filter.length; i++) {
                    if (filename.indexOf(filter[i]) != -1)
                        return true;
                }
                return false;
            }
        };
//          可獲取歌曲名稱,歌手,專輯名稱,歌曲長度等資訊,未來如有擴充功能用到
//        Cursor cursor = context.getContentResolver().query(
//                MediaStore.Audio.Media.INTERNAL_CONTENT_URI, null, null, null,
//                MediaStore.Audio.Media.DEFAULT_SORT_ORDER);

        //從MUSIC的路徑取的資料
        File root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
        File[] files = root.listFiles(filter);
        Log.e(TAG, "root="+root);

        if (files == null) {
            return;
        }
        mp3Names.clear();
        resId.clear();

        for (File file : files) {
            String fileName = file.getName();
            String absolutePath = file.getAbsolutePath();
            String name = fileName.substring(0, fileName.indexOf("."));
            mp3Names.add(name);
            resId.add(absolutePath);
        }
        readBtn.setText(R.string.from_app);
    }


    //這是SD的列表,從MUSIC取得的資料列表化
    public void setData() {
        //將音樂名稱設置在音樂列表中
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mp3Names);
        mp3List.setAdapter(adapter);

        mp3List.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                if (i >= resId.size()) {
                    Toast.makeText(MainActivity.this, R.string.error, Toast.LENGTH_SHORT).show();
                    return;
                }
                positionNow = i;

                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("mp3Index", resId);
                bundle.putStringArrayList("mp3Names", mp3Names);
                bundle.putInt("positionNow", positionNow);
                bundle.putBoolean("readFromSD", readFromSD);
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });

    }
}




