package com.example.bloombuddy

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.bloombuddy.databinding.ActivityMenuBinding
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import kotlinx.android.synthetic.main.activity_menu.*

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

        initNavigationBar()



    }

    private fun initNavigationBar() {
        btmNaviView = binding.bottomNavigationView
        menu = btmNaviView.menu
        btmNaviView.run {
            setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.tab_map -> {
                        supportFragmentManager.beginTransaction().replace(R.id.home_ly, mapFragment).commit()
                        true
                    }
                    R.id.tab_friend -> {

                        true
                    }
                    R.id.tab_info -> {

                        true
                    }
                }
                false
            }
            selectedItemId = R.id.tab_map
        }
    }

}