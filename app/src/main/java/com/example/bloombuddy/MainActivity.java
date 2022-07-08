package com.example.bloombuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kakao.sdk.auth.AuthApiClient;
import com.kakao.sdk.common.util.Utility;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.Account;

public class MainActivity extends AppCompatActivity {

    private TextView kakao_nameTv;
    private ImageView kakao_profileImgView;
    private Button kakao_logoutBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String keyHash = Utility.INSTANCE.getKeyHash(this);
        Log.d("keyhash", keyHash);

        ImageButton kakao_login_button = (ImageButton) findViewById(R.id.kakao_login_button);
        kakao_profileImgView = (ImageView) findViewById(R.id.kakao_profileImgView);
        kakao_nameTv = (TextView) findViewById(R.id.kakao_nameTv);
        kakao_logoutBtn = findViewById(R.id.kakao_logoutBtn);

        if (AuthApiClient.getInstance().hasToken()) {
            UserApiClient.getInstance().accessTokenInfo((accessTokenInfo, error) -> {
                if(error != null){
                    Log.d("token error", "토큰 없음");
                }
                else if (accessTokenInfo != null) {
                    Log.i("toekn ok", "토큰 정보 보기 성공" +
                            "\n회원번호: ${tokenInfo.id}" +
                            "\n만료시간: ${tokenInfo.expiresIn} 초");
                }
                else{
                    Log.d("token ok", "토큰 있음");
                }
                return null;
            });
        } else {

        }

        kakao_login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(MainActivity.this)) {
                    login();
                } else {
                    accountLogin();
                }
            }
        });

        kakao_logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserApiClient.getInstance().logout(error -> {
                    return null;
                });
                kakao_nameTv.setText("logout");

            }
        });

    }

    public void login() {
        String TAG = "login()";
        UserApiClient.getInstance().loginWithKakaoTalk(MainActivity.this, (oAuthToken, error) -> {
            if (error != null) {
                Log.e(TAG, "로그인 실패", error);
            } else if (oAuthToken != null) {
                Log.i(TAG, "로그인 성공(토큰) : " + oAuthToken.getAccessToken());
                getUserInfo();
            }
            return null;
        });
    }

    public void accountLogin() {
        String TAG = "accountLogin()";
        UserApiClient.getInstance().loginWithKakaoAccount(MainActivity.this, (oAuthToken, error) -> {
            if (error != null) {
                Log.e(TAG, "로그인 실패", error);
            } else if (oAuthToken != null) {
                Log.i(TAG, "로그인 성공(토큰) : " + oAuthToken.getAccessToken());
                getUserInfo();
            }
            return null;
        });
    }

    public void getUserInfo() {
        String TAG = "getUserInfo()";
        UserApiClient.getInstance().me((user, meError) -> {
            if (meError != null) {
                Log.e(TAG, "사용자 정보 요청 실패", meError);
            } else {
                System.out.println("로그인 완료");
                Log.i(TAG, user.toString());
                {
                    Log.i(TAG, "사용자 정보 요청 성공" +
                            "\n회원번호: " + user.getId());
                }
                Account user1 = user.getKakaoAccount();
                {
                    Log.i(TAG, "닉네임: " + user1.getName() +
                            "\n프로필: " + user1.getProfile());
                }
                Glide.with(this)
                        .load(user1.getProfile().getProfileImageUrl())
                        .into(kakao_profileImgView);
                kakao_nameTv.setText(user1.getProfile().getNickname());
            }
            return null;
        });
    }


}