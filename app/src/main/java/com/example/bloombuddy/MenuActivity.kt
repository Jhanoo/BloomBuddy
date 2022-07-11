package com.example.bloombuddy

import android.os.Bundle
import android.view.Menu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bloombuddy.databinding.ActivityMenuBinding
import com.example.bloombuddy.menuFragment.FriendFragment
import com.example.bloombuddy.menuFragment.InfoFragment
import com.example.bloombuddy.menuFragment.MapFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MenuActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMenuBinding
    private lateinit var view: View

    private lateinit var btmNaviView: BottomNavigationView
    private lateinit var menu: Menu

    private var userName: String? = null
    private var userProfileUrl: String? = null
    private var userId: String? = null
    private lateinit var platform: String
    lateinit var userData: Array<String>

    private lateinit var mapFragment: MapFragment
    private lateinit var friendFragment: FriendFragment
    private lateinit var infoFragment: InfoFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        view = binding.root
        setContentView(view)


        // 상태바 투명하게
        this.setStatusBarTransparent()
        // 상태바 & 네비게이션바에 버튼이나 텍스트 등 화면구성요소 겹치지 않게 패딩
        view.setPadding(0, 0, 0, this.navigationHeight())

        userData = intent.getStringArrayExtra("userData") as Array<String>
        if (userData != null) {
            platform = userData[0]
            userId = userData[1]
            userName = userData[2]
            userProfileUrl = userData[3]
        }

        mapFragment = MapFragment.newInstance(userData)
        friendFragment = FriendFragment()
        infoFragment = InfoFragment()


        initNavigationBar()


    }

    private fun initNavigationBar() {
        btmNaviView = binding.bottomNavigationView
        menu = btmNaviView.menu

        btmNaviView.run {
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.tab_map -> {
                        changeFragment(mapFragment)
                        item.isChecked = true
                        view.setPadding(0, 0, 0, context.navigationHeight())
                        true
                    }
                    R.id.tab_friend -> {
                        changeFragment(friendFragment)
                        item.isChecked = true
                        view.setPadding(
                            0,
                            context.statusBarHeight(),
                            0,
                            context.navigationHeight()
                        )
                        true
                    }
                    R.id.tab_info -> {
                        changeFragment(infoFragment)
                        item.isChecked = true
                        view.setPadding(
                            0,
                            context.statusBarHeight(),
                            0,
                            context.navigationHeight()
                        )
                        true
                    }
                }
                false
            }
            selectedItemId = R.id.tab_map
        }
    }



    private fun changeFragment(fragment: Fragment) {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.home_ly, fragment)
            .commit()
    }
}