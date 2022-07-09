package com.example.bloombuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

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
import com.google.android.gms.tasks.Task;

import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.Account;
import com.kakao.sdk.user.model.Profile;
import com.navercorp.nid.NaverIdLoginSDK;
import com.navercorp.nid.oauth.NidOAuthLogin;
import com.navercorp.nid.oauth.OAuthLoginCallback;
import com.navercorp.nid.profile.NidProfileCallback;
import com.navercorp.nid.profile.data.NidProfileResponse;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    // Google Sign In API와 호출할 구글 로그인 클라이언트
    GoogleSignInClient mGoogleSignInClient;
    private final int RC_GOOGLE_LOGIN = 123;
    SignInButton googleLoginSIBtn;

    private TextView nameTv;
    private ImageView profileImgView;
    private Button logoutBtn;
    private ImageButton kakaoLoginBtn;
    private ImageButton naverLoginBtn;
    private NaverIdLoginSDK naverIdLoginSDK;

    private String userName;
    private String userProfileUrl;
    private String userId;

    private static int REQUEST_CODE_LOGOUT = 99;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // 상태바 투명하게
        StatusBarKt.setStatusBarTransparent(this);
        // 상태바 & 네비게이션바에 버튼이나 텍스트 등 화면구성요소 겹치지 않게 패딩
        ConstraintLayout container = findViewById(R.id.login_constraintLayout);
        container.setPadding(0, StatusBarKt.statusBarHeight(getApplicationContext()), 0, StatusBarKt.navigationHeight(getApplicationContext()));

//        String keyHash = Utility.INSTANCE.getKeyHash(this);
//        Log.d("keyhash", keyHash);

        kakaoLoginBtn = findViewById(R.id.kakaoLoginBtn);
        profileImgView = findViewById(R.id.profileImgView);
        nameTv = findViewById(R.id.nameTv);
        naverLoginBtn = findViewById(R.id.naverLoginBtn);

        googleLoginSIBtn = findViewById(R.id.googleLoginBtn);
        logoutBtn = findViewById(R.id.logoutBtn);

        kakaoLoginBtn.setOnClickListener(this);
        googleLoginSIBtn.setOnClickListener(this);
        logoutBtn.setOnClickListener(this);
        naverLoginBtn.setOnClickListener(this);

        TextView textView = (TextView) googleLoginSIBtn.getChildAt(0);
        textView.setText("구글 계정으로 로그인");
        googleLoginSIBtn.setSize(SignInButton.SIZE_WIDE);


        // kakao
        if (AuthApiClient.getInstance().hasToken()) {
            UserApiClient.getInstance().accessTokenInfo((accessTokenInfo, error) -> {
                if (error != null) {
                    Log.d("token error", "토큰 없음");
                } else if (accessTokenInfo != null) {
                    Log.i("token ok", "토큰 정보 보기 성공" +
                            "\n회원번호: " + accessTokenInfo.getId() +
                            "\n만료시간: " + accessTokenInfo.getExpiresIn() + "초");
                    kakaoGetUserInfo();
                    startMapActivity("kakao");

                } else {
                    Log.d("token ok", "토큰 있음 근데 만료됨");
                }
                return null;
            });
        } else {
            // 토큰 없을 때 -> 로그인 창
        }

        // 앱에 필요한 사용자 데이터를 요청하도록 로그인 옵션을 설정한다.
        // DEFAULT_SIGN_IN parameter는 유저의 ID와 기본적인 프로필 정보를 요청하는데 사용된다.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail() // email addresses도 요청함
                .build();

        // 위에서 만든 GoogleSignInOptions을 사용해 GoogleSignInClient 객체를 만듬
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // 기존에 로그인 했던 계정을 확인한다.
        GoogleSignInAccount gsa = GoogleSignIn.getLastSignedInAccount(this);

        // 로그인 되있는 경우 (토큰으로 로그인 처리)
        if (gsa != null && gsa.getId() != null) {
            userName = gsa.getDisplayName();
            userId = gsa.getId();
            userProfileUrl = "" + gsa.getPhotoUrl();
            setUserProfile();

            startMapActivity("google");
        }

        naverIdLoginSDK = NaverIdLoginSDK.INSTANCE;
        naverIdLoginSDK.initialize(this, "hv_v8qfwCAtL8eMUrfPv", "nDD2xL5l4N", "BloomBuddy");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.googleLoginBtn:
                googleLogin();
                break;

            case R.id.kakaoLoginBtn:
                if (UserApiClient.getInstance().isKakaoTalkLoginAvailable(LoginActivity.this))
                    kakaoLogin();
                else
                    kakaoAccountLogin();
                break;

            case R.id.naverLoginBtn:
                startNaverLogin();
                break;

            case R.id.logoutBtn:
                kakaoLogout();
                naverLogout();
                googleLogout();
                userId = null;
                userName = null;
                userProfileUrl = null;
                setUserProfile();
                break;


            default:
                break;
        }
    }

    private void startMapActivity(String platform) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("userData", new String[]{platform, userId, userName, userProfileUrl});

        startActivityForResult(intent, REQUEST_CODE_LOGOUT);
    }

    private void setUserProfile() {
        if (userId != null) {
            if (userName != null)
                nameTv.setText(userName);
            if (userProfileUrl != null && !userProfileUrl.equals(""))
                Glide.with(this)
                        .load(userProfileUrl)
                        .override(300, 300)
                        .fitCenter()
                        .into(profileImgView);
        } else {
            nameTv.setText("default");
            profileImgView.setImageResource(com.kakao.common.R.drawable.kakao_default_profile_image);
        }
    }

    public String[] getUserProfile() {
        return new String[]{userId, userName, userProfileUrl};
    }

    public void kakaoLogin() {
        String TAG = "login()";
        UserApiClient.getInstance().loginWithKakaoTalk(LoginActivity.this, (oAuthToken, error) -> {
            if (error != null) {
                Log.e(TAG, "로그인 실패", error);
            } else if (oAuthToken != null) {
                Log.i(TAG, "로그인 성공(토큰) : " + oAuthToken.getAccessToken());
                kakaoGetUserInfo();
            }
            return null;
        });
    }

    private void kakaoLogout() {
        UserApiClient.getInstance().logout(error -> null);
    }

    public void kakaoAccountLogin() {
        String TAG = "accountLogin()";
        UserApiClient.getInstance().loginWithKakaoAccount(LoginActivity.this, (oAuthToken, error) -> {
            if (error != null) {
                Log.e(TAG, "로그인 실패", error);
            } else if (oAuthToken != null) {
                Log.i(TAG, "로그인 성공(토큰) : " + oAuthToken.getAccessToken());
                kakaoGetUserInfo();
            }
            return null;
        });
    }

    public void kakaoGetUserInfo() {
        String TAG = "kakaoGetUserInfo()";
        UserApiClient.getInstance().me((user, meError) -> {
            if (meError != null) {
                Log.e(TAG, "사용자 정보 요청 실패", meError);
            } else {
                Log.d(TAG, "로그인 완료");
                Log.i(TAG, user.toString());
                {
                    Log.i(TAG, "사용자 정보 요청 성공" +
                            "\n회원번호: " + user.getId());
                }
                Account kakaoUserAccount = user.getKakaoAccount();
                Profile kakaoUser = kakaoUserAccount.getProfile();
                userName = kakaoUser.getNickname();
                userProfileUrl = kakaoUser.getProfileImageUrl();
                userId = "" + user.getId();
                setUserProfile();
                startMapActivity("kakao");
            }
            return null;
        });
    }


    private void googleHandleSignInResult(Task<GoogleSignInAccount> completedTask) {
        String TAG = "handleSignInResult";
        try {
            GoogleSignInAccount acct = completedTask.getResult(ApiException.class);

            if (acct != null) {
                String googleUserName = acct.getDisplayName();
                String googleUserId = acct.getId();
                Uri googleUserProfile = acct.getPhotoUrl();
                userName = googleUserName;
                if (googleUserProfile != null)
                    userProfileUrl = "" + googleUserProfile;
                userId = googleUserId;

                setUserProfile();
                startMapActivity("google");

            }
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(TAG, "signInResult:failed code=" + e.getStatusCode());

        }
    }


    private void googleLogin() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_LOGIN);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_GOOGLE_LOGIN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            googleHandleSignInResult(task);
        }

        if (requestCode == REQUEST_CODE_LOGOUT) {
            switch (resultCode) {
                case 10:
                    kakaoLogout();
                    break;
                case 11:
                    naverLogout();
                    break;
                case 12:
                    googleLogout();
                    break;
            }
            userId = null;
            userName = null;
            userProfileUrl = null;
            setUserProfile();
        }
    }

    private void startNaverLogin() {
        NidProfileCallback<NidProfileResponse> profileCallback = new NidProfileCallback<NidProfileResponse>() {
            @Override
            public void onSuccess(NidProfileResponse nidProfileResponse) {
                String naverUserName = nidProfileResponse.getProfile().getName();
                String naverUserId = nidProfileResponse.getProfile().getId();
                String naverUserProfile = nidProfileResponse.getProfile().getProfileImage();

                userName = naverUserName;
                userProfileUrl = naverUserProfile;
                userId = naverUserId;

                setUserProfile();
                startMapActivity("naver");
                Log.d("naver login", "naver login success");
            }

            @Override
            public void onFailure(int i, @NonNull String s) {
                Log.d("naver login", "naver login failed");
            }

            @Override
            public void onError(int i, @NonNull String s) {
                Log.d("naver login", "naver login error");

            }
        };

        OAuthLoginCallback oauthLoginCallback = new OAuthLoginCallback() {
            @Override
            public void onSuccess() {
//                String naverToken = naverIdLoginSDK.getAccessToken();
//                Log.d("naver token", naverToken);
                NidOAuthLogin nidOAuthLogin = new NidOAuthLogin();
                nidOAuthLogin.callProfileApi(profileCallback);
            }

            @Override
            public void onFailure(int i, @NonNull String s) {

                Log.d("naverlogin", "naver login failed222");
            }

            @Override
            public void onError(int i, @NonNull String s) {

                Log.d("naverlogin", "naver login error333");
            }
        };
        naverIdLoginSDK.authenticate(this, oauthLoginCallback);
    }

    private void naverLogout() {
        naverIdLoginSDK.logout();
    }


}