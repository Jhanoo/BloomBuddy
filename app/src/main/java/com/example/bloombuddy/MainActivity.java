package com.example.bloombuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.kakao.sdk.common.util.Utility;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String keyHash = Utility.INSTANCE.getKeyHash(this);
        Log.d("keyhash", keyHash);
    }
}