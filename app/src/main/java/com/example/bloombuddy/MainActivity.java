package com.example.bloombuddy;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.kakao.sdk.auth.AuthApiClient;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.kakao.sdk.common.util.Utility;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.Account;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // Google Sign In API와 호출할 구글 로그인 클라이언트
    GoogleSignInClient mGoogleSignInClient;
    private final int RC_SIGN_IN = 123;
    private static final String TAG = "MainActivity";
    private final String logInText = "구글 계정으로 로그인";
    SignInButton signBt;
    Button logoutBt;
//    ImageView profileImage;

    private TextView kakao_nameTv;
    private ImageView kakao_profileImgView;
    private Button kakao_logoutBtn;
    private ImageButton kakao_login_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String keyHash = Utility.INSTANCE.getKeyHash(this);
        Log.d("keyhash", keyHash);

        kakao_login_button = (ImageButton) findViewById(R.id.kakao_login_button);
        kakao_profileImgView = (ImageView) findViewById(R.id.kakao_profileImgView);
        kakao_nameTv = (TextView) findViewById(R.id.kakao_nameTv);
        kakao_logoutBtn = findViewById(R.id.kakao_logoutBtn);

        if (AuthApiClient.getInstance().hasToken()) {
            UserApiClient.getInstance().accessTokenInfo((accessTokenInfo, error) -> {
                if (error != null) {
                    Log.d("token error", "토큰 없음");
                } else if (accessTokenInfo != null) {
                    Log.i("toekn ok", "토큰 정보 보기 성공" +
                            "\n회원번호: ${tokenInfo.id}" +
                            "\n만료시간: ${tokenInfo.expiresIn} 초");
                } else {
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

//        kakao_logoutBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                UserApiClient.getInstance().logout(error -> {
//                    return null;
//                });
//
//
//                kakao_nameTv.setText("logout");
//
//            }
//        });


        signBt = findViewById(R.id.sign_in_button);
        signBt.setOnClickListener(this);
        TextView textView = (TextView) signBt.getChildAt(0);
        textView.setText(logInText);
        // 앱에 필요한 사용자 데이터를 요청하도록 로그인 옵션을 설정한다.
        // DEFAULT_SIGN_IN parameter는 유저의 ID와 기본적인 프로필 정보를 요청하는데 사용된다.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail() // email addresses도 요청함
                .build();

        // 위에서 만든 GoogleSignInOptions을 사용해 GoogleSignInClient 객체를 만듬
        mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);

        // 기존에 로그인 했던 계정을 확인한다.
        GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(MainActivity.this);

        // 로그인 되있는 경우 (토큰으로 로그인 처리)
        if (gsa != null && gsa.getId() != null) {

        }
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.kakao_logoutBtn:
                UserApiClient.getInstance().logout(error -> {
                    return null;
                });
                kakao_nameTv.setText("logout");

                mGoogleSignInClient.signOut()
                        .addOnCompleteListener(this, task -> {
                            Log.d(TAG, "onClick:logout success ");
                            mGoogleSignInClient.revokeAccess()
                                    .addOnCompleteListener(this, task1 -> Log.d(TAG, "onClick:revokeAccess success "));

                        });
                break;
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount acct = completedTask.getResult(ApiException.class);

            if (acct != null) {
                String personName = acct.getDisplayName();
                String personGivenName = acct.getGivenName();
                String personFamilyName = acct.getFamilyName();
                String personEmail = acct.getEmail();
                String personId = acct.getId();
                Uri personPhoto = acct.getPhotoUrl();
                Glide.with(this).load(personPhoto).into(kakao_profileImgView);
                kakao_nameTv.setText(personName);

                Log.d(TAG, "handleSignInResult:personName " + personName);
                Log.d(TAG, "handleSignInResult:personGivenName " + personGivenName);
                Log.d(TAG, "handleSignInResult:personEmail " + personEmail);
                Log.d(TAG, "handleSignInResult:personId " + personId);
                Log.d(TAG, "handleSignInResult:personFamilyName " + personFamilyName);
                Log.d(TAG, "handleSignInResult:personPhoto " + personPhoto);
            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());

        }
    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

}