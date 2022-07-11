package com.example.bloombuddy;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.kakao.sdk.user.UserApiClient;
import com.navercorp.nid.NaverIdLoginSDK;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private GoogleSignInClient mGoogleSignInClient;
    private NaverIdLoginSDK naverIdLoginSDK;

    private TextView nameTv;
    private ImageView profileImgView;
    private Button logoutBtn;


    private String userName;
    private String userProfileUrl;
    private String userId;
    private String platform;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 상태바 투명하게
        StatusBarKt.setStatusBarTransparent(this);
        // 상태바 & 네비게이션바에 버튼이나 텍스트 등 화면구성요소 겹치지 않게 패딩
        ConstraintLayout container = findViewById(R.id.map_constraintLayout);
        container.setPadding(0, StatusBarKt.statusBarHeight(getApplicationContext()), 0, StatusBarKt.navigationHeight(getApplicationContext()));


        profileImgView = findViewById(R.id.profileImgView2);
        nameTv = findViewById(R.id.nameTv2);
        logoutBtn = findViewById(R.id.logoutBtn2);

        logoutBtn.setOnClickListener(this);

        Intent intent = getIntent();
        String[] userData = intent.getStringArrayExtra("userData");

        platform = userData[0];
        userId = userData[1];
        userName = userData[2];
        userProfileUrl = userData[3];

        setUserProfile();

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.logoutBtn2:
                switch (platform){
                    case "kakao":
                        setResult(10);
                        finish();
                        break;
                    case "google":
                        setResult(11);
                        finish();
                        break;
                    case "naver":
                        setResult(12);
                        finish();
                        break;
                    default:
                        Log.d("logout error", "logout error!");
                        break;
                }

                break;


            default:
                break;
        }
    }

    private void setUserProfile(){
        if(userId != null){
            if(userName != null)
                nameTv.setText(userName);
            if(userProfileUrl != null && !userProfileUrl.equals(""))
                Glide.with(this)
                        .load(userProfileUrl)
                        .override(300, 300)
                        .centerCrop()
                        .into(profileImgView);
        }
        else{
            nameTv.setText("default");
            profileImgView.setImageResource(com.kakao.common.R.drawable.kakao_default_profile_image);
        }
    }

    private void kakaoLogout() {
        UserApiClient.getInstance().logout(error -> null);
    }


    private void googleLogout() {
        String TAG = "google logout";
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, task -> {
                    Log.d(TAG, "onClick:logout success ");
                    mGoogleSignInClient.revokeAccess()
                            .addOnCompleteListener(this, task1 -> Log.d(TAG, "onClick:revokeAccess success "));
                });
    }

    private void naverLogout() {
        naverIdLoginSDK.logout();
    }
}