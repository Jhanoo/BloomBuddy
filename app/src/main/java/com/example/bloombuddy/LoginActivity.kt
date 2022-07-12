package com.example.bloombuddy

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bloombuddy.databinding.ActivityLoginBinding
import com.example.bloombuddy.form.LoginData
import com.example.bloombuddy.form.LoginResponse
import com.example.bloombuddy.network.RetrofitClient
import com.example.bloombuddy.network.ServiceApi
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.navercorp.nid.NaverIdLoginSDK
import com.navercorp.nid.oauth.NidOAuthLogin
import com.navercorp.nid.oauth.OAuthLoginCallback
import com.navercorp.nid.profile.NidProfileCallback
import com.navercorp.nid.profile.data.NidProfileResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity(), View.OnClickListener {
    // Google Sign In API와 호출할 구글 로그인 클라이언트
    private var mGoogleSignInClient: GoogleSignInClient? = null

    private lateinit var naverIdLoginSDK: NaverIdLoginSDK

    private lateinit var googleLoginSIBtn: SignInButton
    private lateinit var kakaoLoginBtn: ImageButton
    private lateinit var naverLoginBtn: ImageButton
    private lateinit var binding: ActivityLoginBinding

    private var userProfileUrl: String? = null
    private var userName: String? = null
    private var userId: String? = null
    private var joinId: String? = null
    private lateinit var service: ServiceApi
    private lateinit var mProgressView: ProgressBar
    private lateinit var createNewBtn: Button
    private lateinit var mIDView: AutoCompleteTextView
    private lateinit var mPasswordView: EditText
    private lateinit var mLoginButton: Button
    private lateinit var api_token: String

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
        naverLoginBtn = binding.naverLoginBtn
        googleLoginSIBtn = binding.googleLoginBtn
        createNewBtn = binding.createAccountButton
        mIDView = binding.loginId
        mPasswordView = binding.loginPassword
        mLoginButton = binding.loginButton
        mProgressView = binding.loginProgress

        kakaoLoginBtn.setOnClickListener(this)
        naverLoginBtn.setOnClickListener(this)
        googleLoginSIBtn.setOnClickListener(this)
        createNewBtn.setOnClickListener(this)
        mLoginButton.setOnClickListener(this)
        service = RetrofitClient.getClient().create(ServiceApi::class.java)

        val textView = googleLoginSIBtn.getChildAt(0) as TextView
        textView.text = "구글 계정으로 로그인"
        googleLoginSIBtn.setSize(SignInButton.SIZE_WIDE)

        // 앱에 필요한 사용자 데이터를 요청하도록 로그인 옵션을 설정한다.
        // DEFAULT_SIGN_IN parameter는 유저의 ID와 기본적인 프로필 정보를 요청하는데 사용된다.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail() // email addresses도 요청함
            .build()

        // 위에서 만든 GoogleSignInOptions을 사용해 GoogleSignInClient 객체를 만듬
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        naverIdLoginSDK = NaverIdLoginSDK
        naverIdLoginSDK.initialize(this, "hv_v8qfwCAtL8eMUrfPv", "nDD2xL5l4N", "BloomBuddy")
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.create_account_button-> {
                val intent = Intent(this@LoginActivity, JoinActivity::class.java)
                startActivity(intent)
            }
            R.id.login_button -> attemptLogin()
            R.id.googleLoginBtn -> googleLogin()
            R.id.kakaoLoginBtn ->
                if (UserApiClient.instance.isKakaoTalkLoginAvailable(this@LoginActivity))
                    kakaoLogin()
                else
                    kakaoAccountLogin()
            R.id.naverLoginBtn -> startNaverLogin()
//            R.id.logoutBtn -> {
//                kakaoLogout()
//                naverLogout()
//                googleLogout()
//                userId = null
//                userName = null
//                userProfileUrl = null
//            }
            else -> {}
        }
    }

    private fun startMenuActivity(platform: String) {
        val intent = Intent(this, MenuActivity::class.java)
        intent.putExtra("userData", arrayOf(platform, userId, userName, userProfileUrl))
        startActivityForResult(intent, REQUEST_CODE_LOGOUT)
        finish()
    }

    private fun kakaoLogin() {
        val TAG = "login()"
        UserApiClient.instance.loginWithKakaoTalk(this@LoginActivity) { oAuthToken: OAuthToken?, error: Throwable? ->
            if (error != null) {
                Log.e(TAG, "로그인 실패", error)
            } else if (oAuthToken != null) {
                Log.i(TAG, "로그인 성공(토큰) : " + oAuthToken.accessToken)
                kakaoGetUserInfo()
                api_token = oAuthToken.accessToken
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
                api_token = oAuthToken.accessToken
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
                joinId = "K$userName"
                startLogin(LoginData(joinId, null, userName, "KAKAO"))
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
                joinId = "G$userName"
                startLogin(LoginData(joinId, null, userName, "GOOGLE"))
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
                    val naverToken = naverIdLoginSDK.getAccessToken()
                    userName = naverUserName
                    userProfileUrl = naverUserProfile
                    userId = naverUserId
                    joinId = "N$userName"
                    startLogin(LoginData(joinId, null, userName, "NAVER"))
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

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }

    private fun attemptLogin() {
        mIDView.error = null
        mPasswordView.error = null
        val userid = mIDView.text.toString()
        val password = mPasswordView.text.toString()
        var cancel = false
        var focusView: View? = null

        // 패스워드의 유효성 검사
        if (password.isEmpty()) {
            mPasswordView.error = "비밀번호를 입력해주세요."
            focusView = mPasswordView
            cancel = true
        } else if (!isPasswordValid(password)) {
            mPasswordView.error = "6자 이상의 비밀번호를 입력해주세요."
            focusView = mPasswordView
            cancel = true
        }

        // id의 유효성 검사
        if (userid.isEmpty()) {
            mIDView.error = "ID를 입력해주세요."
            focusView = mIDView
            cancel = true
        }
        if (cancel) {
            focusView!!.requestFocus()
        } else {
            startLogin(LoginData(userid, password, null, "BLOOM"))
            showProgress(true)
        }
    }

    private fun startLogin(data: LoginData) {
        service.userLogin(data).enqueue(object : Callback<LoginResponse?> {
            override fun onResponse(call: Call<LoginResponse?>, response: Response<LoginResponse?>) {
                val result: LoginResponse? = response.body()
                Toast.makeText(this@LoginActivity, result!!.message, Toast.LENGTH_SHORT).show()
                if (result.code == 327) {
                    //sendProfileImage()
                }
                showProgress(false)
            }

            override fun onFailure(call: Call<LoginResponse?>?, t: Throwable) {
                Toast.makeText(this@LoginActivity, "로그인 에러 발생", Toast.LENGTH_SHORT).show()
                Log.e("로그인 에러 발생", t.message!!)
                showProgress(false)
            }

        })
    }

//    private fun sendProfileImage() {
//        service.sendImg(data).enqueue(new Callback<>())
//    }

    private fun showProgress(show: Boolean) {
        mProgressView.visibility = if (show) View.VISIBLE else View.GONE
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
        private const val RC_GOOGLE_LOGIN = 123

        const val FINISH_INTERVAL_TIME: Long = 2000
        var backPressedTime: Long = 0
    }


}