package com.example.bloombuddy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.bloombuddy.form.LoginData;
import com.example.bloombuddy.form.LoginResponse;
import com.example.bloombuddy.network.RetrofitClient;
import com.example.bloombuddy.network.ServiceApi;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private Button createNewBtn;
    private AutoCompleteTextView mIDView;
    private EditText mPasswordView;
    private Button mLoginButton;
    private ServiceApi service;
    private ProgressBar mProgressView;
    private String userName;
    private String userProfileUrl;
    private String userId;
    private String joinId;
    private String api_token;

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
        createNewBtn = findViewById(R.id.create_account_button);
        mIDView = (AutoCompleteTextView) findViewById(R.id.login_id);
        mPasswordView = (EditText) findViewById(R.id.login_password);
        mLoginButton = (Button) findViewById(R.id.login_button);
        mProgressView = (ProgressBar) findViewById(R.id.login_progress);

        kakaoLoginBtn.setOnClickListener(this);
        googleLoginSIBtn.setOnClickListener(this);
        logoutBtn.setOnClickListener(this);
        naverLoginBtn.setOnClickListener(this);
        createNewBtn.setOnClickListener(this);
        mLoginButton.setOnClickListener(this);

        service = RetrofitClient.getClient().create(ServiceApi.class);
        TextView textView = (TextView) googleLoginSIBtn.getChildAt(0);
        textView.setText("구글 계정으로 로그인");
        googleLoginSIBtn.setSize(SignInButton.SIZE_WIDE);

        Button btn = findViewById(R.id.btn0);
        btn.setOnClickListener(v -> {
            Intent intent = new Intent(this, MapActivity.class);
            intent.putExtra("profile", userProfileUrl);
            startActivity(intent);
        });


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
                .requestIdToken(getString(R.string.default_web_client_id))
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
            joinId = "G" + userName;
            setUserProfile();

            startMapActivity("google");
        }

        naverIdLoginSDK = NaverIdLoginSDK.INSTANCE;
        naverIdLoginSDK.initialize(this, "hv_v8qfwCAtL8eMUrfPv", "nDD2xL5l4N", "BloomBuddy");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.create_account_button:
                Intent intent = new Intent(LoginActivity.this, JoinActivity.class);
                startActivity(intent);
                break;

            case R.id.login_button:
                attemptLogin();
                break;

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
        Intent intent = new Intent(this, MainActivity.class);
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

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    public void attemptLogin(){
        mIDView.setError(null);
        mPasswordView.setError(null);

        String userid = mIDView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // 패스워드의 유효성 검사
        if (password.isEmpty()) {
            mPasswordView.setError("비밀번호를 입력해주세요.");
            focusView = mPasswordView;
            cancel = true;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError("6자 이상의 비밀번호를 입력해주세요.");
            focusView = mPasswordView;
            cancel = true;
        }

        // id의 유효성 검사
        if (userid.isEmpty()) {
            mIDView.setError("ID를 입력해주세요.");
            focusView = mIDView;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            startLogin(new LoginData(userid, password, null,"BLOOM", null));
            showProgress(true);
        }
    }

    private void startLogin(LoginData data){
        service.userLogin(data).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                LoginResponse result = response.body();
                Toast.makeText(LoginActivity.this, result.getMessage(), Toast.LENGTH_SHORT).show();
                if(result.getCode()==327){
                    sendProfileImage();
                }
                showProgress(false);
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "로그인 에러 발생", Toast.LENGTH_SHORT).show();
                Log.e("로그인 에러 발생", t.getMessage());
                showProgress(false);
            }
        });
    }

    private void sendProfileImage(){
        //service.sendImg(data).enqueue(new Callback<>())
    }

    private void showProgress(boolean show) {
        mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
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
                api_token = oAuthToken.getAccessToken();
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
                api_token = oAuthToken.getAccessToken();
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
                joinId = "K" + userName;
                startLogin(new LoginData(joinId,null,userName,"KAKAO",api_token));
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
                String Idtoken = "";
                //String Idtoken = acct.getIdToken();
                userName = googleUserName;
                if (googleUserProfile != null)
                    userProfileUrl = "" + googleUserProfile;
                userId = googleUserId;
                joinId = "G"+ userName;
                startLogin(new LoginData(joinId,null,userName,"GOOGLE",Idtoken));
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
            joinId = null;
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
                String naverToken = naverIdLoginSDK.getAccessToken();
                userName = naverUserName;
                userProfileUrl = naverUserProfile;
                userId = naverUserId;
                joinId = "N" + userName;
                startLogin(new LoginData(joinId,null,userName,"NAVER",naverToken));
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