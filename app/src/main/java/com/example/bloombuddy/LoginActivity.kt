package com.example.bloombuddy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bloombuddy.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.AccessTokenInfo
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse


class LoginActivity : AppCompatActivity(), View.OnClickListener {
    // Google Sign In API와 호출할 구글 로그인 클라이언트
    private var mGoogleSignInClient: GoogleSignInClient? = null
    private val RC_GOOGLE_LOGIN = 123

    private lateinit var naverIdLoginSDK: NaverIdLoginSDK

    private lateinit var googleLoginSIBtn: SignInButton
    private lateinit var kakaoLoginBtn: ImageButton
    private lateinit var naverLoginBtn: ImageButton
    private lateinit var binding: ActivityLoginBinding
    private lateinit var profileImgView: ImageView
    private lateinit var nameTv: TextView
    private lateinit var logoutBtn: Button

    private var userProfileUrl: String? = null
    private var userName: String? = null
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // 상태바 투명하게
        this.setStatusBarTransparent()
        // 상태바 & 네비게이션바에 버튼이나 텍스트 등 화면구성요소 겹치지 않게 패딩
        view.setPadding(
            0,
            applicationContext.statusBarHeight(),
            0,
            applicationContext.navigationHeight()
        )

//        String keyHash = Utility.INSTANCE.getKeyHash(this);
//        Log.d("keyhash", keyHash);
        kakaoLoginBtn = binding.kakaoLoginBtn
        profileImgView = binding.profileImgView
        nameTv = binding.nameTv
        naverLoginBtn = binding.naverLoginBtn
        googleLoginSIBtn = binding.googleLoginBtn
        logoutBtn = binding.logoutBtn

        kakaoLoginBtn.setOnClickListener(this)
        naverLoginBtn.setOnClickListener(this)
        googleLoginSIBtn.setOnClickListener(this)
        logoutBtn.setOnClickListener(this)

        val textView = googleLoginSIBtn.getChildAt(0) as TextView
        textView.text = "구글 계정으로 로그인"
        googleLoginSIBtn.setSize(SignInButton.SIZE_WIDE)


        // kakao
        if (AuthApiClient.instance.hasToken()) {
            UserApiClient.instance.accessTokenInfo { accessTokenInfo: AccessTokenInfo?, error: Throwable? ->
                if (error != null) {
                    Log.d("token error", "토큰 없음")
                } else if (accessTokenInfo != null) {
                    Log.i(
                        "token ok",
                        "토큰 정보 보기 성공 회원번호: ${accessTokenInfo.id} 만료시간: ${accessTokenInfo.expiresIn}초"
                    )
                    kakaoGetUserInfo()
                    startMenuActivity("kakao")
                } else {
                    Log.d("token ok", "토큰 있음 근데 만료됨")
                }
            }
        } else {
            // 토큰 없을 때 -> 로그인 창
        }

        // 앱에 필요한 사용자 데이터를 요청하도록 로그인 옵션을 설정한다.
        // DEFAULT_SIGN_IN parameter는 유저의 ID와 기본적인 프로필 정보를 요청하는데 사용된다.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail() // email addresses도 요청함
            .build()

        // 위에서 만든 GoogleSignInOptions을 사용해 GoogleSignInClient 객체를 만듬
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        // 기존에 로그인 했던 계정을 확인한다.
        val gsa = GoogleSignIn.getLastSignedInAccount(this)

        // 로그인 되있는 경우 (토큰으로 로그인 처리)
        if (gsa != null && gsa.id != null) {
            userName = gsa.displayName
            userId = gsa.id
            userProfileUrl = "" + gsa.photoUrl
            startMenuActivity("google")
        }
        naverIdLoginSDK = NaverIdLoginSDK
        naverIdLoginSDK.initialize(this, "hv_v8qfwCAtL8eMUrfPv", "nDD2xL5l4N", "BloomBuddy")
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.googleLoginBtn -> googleLogin()
            R.id.kakaoLoginBtn ->
                if (UserApiClient.instance.isKakaoTalkLoginAvailable(this@LoginActivity))
                    kakaoLogin()
                else
                    kakaoAccountLogin()
            R.id.naverLoginBtn -> startNaverLogin()
            R.id.logoutBtn -> {
                kakaoLogout()
                naverLogout()
                googleLogout()
                userId = null
                userName = null
                userProfileUrl = null
            }
            else -> {}
        }
    }

    private fun startMenuActivity(platform: String) {
        val intent = Intent(this, MenuActivity::class.java)
        intent.putExtra("userData", arrayOf(platform, userId, userName, userProfileUrl))
        startActivityForResult(intent, REQUEST_CODE_LOGOUT)
    }

    private fun kakaoLogin() {
        val TAG = "login()"
        UserApiClient.instance.loginWithKakaoTalk(this@LoginActivity) { oAuthToken: OAuthToken?, error: Throwable? ->
            if (error != null) {
                Log.e(TAG, "로그인 실패", error)
            } else if (oAuthToken != null) {
                Log.i(TAG, "로그인 성공(토큰) : " + oAuthToken.accessToken)
                kakaoGetUserInfo()
            }
            null
        }
    }

    private fun kakaoLogout() {
        val TAG = "kakaoLogout"
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.e(TAG, "로그아웃 실패. SDK에서 토큰 삭제됨", error)
            } else {
                Log.i(TAG, "로그아웃 성공. SDK에서 토큰 삭제됨")
            }
        }
    }

    private fun kakaoAccountLogin() {
        val TAG = "accountLogin()"
        UserApiClient.instance.loginWithKakaoAccount(this@LoginActivity) { oAuthToken: OAuthToken?, error: Throwable? ->
            if (error != null) {
                Log.e(TAG, "로그인 실패", error)
            } else if (oAuthToken != null) {
                Log.i(TAG, "로그인 성공(토큰) : " + oAuthToken.accessToken)
                kakaoGetUserInfo()
            }
            null
        }
    }

    private fun kakaoGetUserInfo() {
        val TAG = "kakaoGetUserInfo()"
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e(TAG, "사용자 정보 요청 실패", error)
            } else if (user != null) {
                Log.i(
                    TAG, "사용자 정보 요청 성공" +
                            "\n회원번호: ${user.id}" +
                            "\n이메일: ${user.kakaoAccount?.email}" +
                            "\n닉네임: ${user.kakaoAccount?.profile?.nickname}" +
                            "\n프로필사진: ${user.kakaoAccount?.profile?.thumbnailImageUrl}"
                )
                val kakaoUserAccount = user.kakaoAccount
                val kakaoUser = kakaoUserAccount?.profile
                userName = kakaoUser?.nickname
                userProfileUrl = kakaoUser?.profileImageUrl
                userId = "" + user.id
                startMenuActivity("kakao")
            }
        }
    }

    private fun googleHandleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        val TAG = "handleSignInResult"
        try {
            val acct = completedTask.getResult(ApiException::class.java)
            if (acct != null) {
                val googleUserName = acct.displayName
                val googleUserId = acct.id
                val googleUserProfile = acct.photoUrl
                userName = googleUserName
                if (googleUserProfile != null) userProfileUrl = "" + googleUserProfile
                userId = googleUserId
                startMenuActivity("google")
            }
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.e(TAG, "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun googleLogin() {
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, RC_GOOGLE_LOGIN)
    }

    private fun googleLogout() {
        val TAG = "google logout"
        mGoogleSignInClient!!.signOut()
            .addOnCompleteListener(this) { task: Task<Void?>? ->
                Log.d(TAG, "onClick:logout success ")
                mGoogleSignInClient!!.revokeAccess()
                    .addOnCompleteListener(this) { task1: Task<Void?>? ->
                        Log.d(TAG, "onClick:revokeAccess success ")
                    }
            }
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_GOOGLE_LOGIN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            googleHandleSignInResult(task)
        }
        if (requestCode == REQUEST_CODE_LOGOUT) {
            when (resultCode) {
                10 -> kakaoLogout()
                11 -> naverLogout()
                12 -> googleLogout()
            }
            userId = null
            userName = null
            userProfileUrl = null
        }
    }

    private fun startNaverLogin() {
        val profileCallback: NidProfileCallback<NidProfileResponse> =
            object : NidProfileCallback<NidProfileResponse> {
                override fun onSuccess(nidProfileResponse: NidProfileResponse) {
                    val naverUserName = nidProfileResponse.profile!!.name
                    val naverUserId = nidProfileResponse.profile!!.id
                    val naverUserProfile = nidProfileResponse.profile!!.profileImage
                    userName = naverUserName
                    userProfileUrl = naverUserProfile
                    userId = naverUserId
                    startMenuActivity("naver")
                    Log.d("naver login", "naver login success")
                }

                override fun onFailure(i: Int, s: String) {
                    Log.d("naver login", "naver login failed")
                }

                override fun onError(i: Int, s: String) {
                    Log.d("naver login", "naver login error")
                }
            }
        val oauthLoginCallback: OAuthLoginCallback = object : OAuthLoginCallback {
            override fun onSuccess() {
                val nidOAuthLogin = NidOAuthLogin()
                nidOAuthLogin.callProfileApi(profileCallback)
            }

            override fun onFailure(i: Int, s: String) {
                Log.d("naverLogin", "naver login failed222")
            }

            override fun onError(i: Int, s: String) {
                Log.d("naverLogin", "naver login error333")
            }
        }
        naverIdLoginSDK.authenticate(this, oauthLoginCallback)
    }

    private fun naverLogout() {
        naverIdLoginSDK.logout()
    }

    override fun onBackPressed() {
        var tempTime = System.currentTimeMillis()
        var intervalTime = tempTime - backPressedTime

        if (intervalTime in 0..FINISH_INTERVAL_TIME)
            super.onBackPressed()
        else {
            backPressedTime = tempTime
            Toast.makeText(applicationContext, "한번 더 뒤로가기를 누르면 종료됩니다.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val REQUEST_CODE_LOGOUT = 99

        const val FINISH_INTERVAL_TIME: Long = 2000;
        var backPressedTime: Long = 0;
    }


}