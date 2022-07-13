package com.example.bloombuddy.menuFragment

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.bloombuddy.R
import com.example.bloombuddy.form.MarkerData
import com.google.android.gms.location.*
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.normal.TedPermission
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.util.FusedLocationSource
import kotlinx.android.synthetic.main.fragment_map.view.*
import kotlinx.android.synthetic.main.item_point.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.MalformedURLException
import java.net.URL


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"

class MapFragment : Fragment(), OnMapReadyCallback {
    // TODO: Rename and change types of parameters
    private var userData: Array<String>? = null

    private lateinit var mapView: MapView
    private lateinit var locationSource: FusedLocationSource
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var naverMap: NaverMap
    private lateinit var uiSettings: UiSettings
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private var myMarker: Marker? = null
    private var bitmap: Bitmap? = null
    private var hasPermission = false

    private var userName: String? = null
    private var userProfileUrl: String? = null
    private var userId: String? = null
    private lateinit var platform: String
    private var mContainer: ViewGroup? = null

    private var markerMap: MutableMap<String, MarkerData> = mutableMapOf()
    private var markerArr: ArrayList<MarkerData> = ArrayList()
    private val defaultImage = OverlayImage.fromResource(R.drawable.user)
    private lateinit var listener: Overlay.OnClickListener
    private lateinit var infoWindow: InfoWindow

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userData = it.getStringArray(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mContainer = container

        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView = view.mapView
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        if (userData != null) {
            platform = userData!![0]
            userId = userData!![1]
            userName = userData!![2]
            userProfileUrl = userData!![3]
        }
        if (userData != null)
            Log.d(
                "userData", "\n${userData!![0]} \n" +
                        "${userData!![1]}\n" +
                        "${userData!![2]}\n" +
                        "${userData!![3]}"
            )

        locationSource =
            FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        locationRequest = LocationRequest.create()
        locationRequest.run {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY //높은 정확도
            interval = 1000 //1초에 한번씩 GPS 요청
        }

        if (userProfileUrl != null) {
            CoroutineScope(Dispatchers.Main).launch {
                bitmap = withContext(Dispatchers.IO) {
                    ImageLoader.loadImage(userProfileUrl!!)
                }
                bitmap = getRoundedCornerBitmap(bitmap!!)
            }
        }

        checkPermissions()
        markerArr.add(
            MarkerData(
                "김태훈",
                "http://cdn.pixabay.com/photo/2022/07/07/14/00/otter-7307280_1280.jpg",
                36.3743953,
                127.3655277
            )
        )
        markerArr.add(
            MarkerData(
                "정찬우",
                "http://t1.daumcdn.net/cfile/tistory/24283C3858F778CA2E",
                36.3740953,
                127.3656277
            )
        )
        markerArr.add(
            MarkerData(
                "이지현",
                "https://lh3.googleusercontent.com/a-/AFdZucr8w61s9nKov8uLQ9p8qJoEL_lYw2XkHmFBM_s",
                36.3741953,
                127.3658277
            )
        )
        Log.d("tag", "" + (userData?.get(3)))
//        markerArr.add(MarkerData("김사은", null, 36.3744953, 127.3659277))
//        markerArr.add(MarkerData("김재민", null, 36.3749953, 127.3661277))
        for (i in 0 until markerArr.size) {
            markerMap[markerArr[i].userId] = markerArr[i]
        }
    }

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        if (hasPermission) {
            setUpdateLocationListener()
        }
        mapView.onResume()

    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (hasPermission) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    @UiThread
    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        uiSettings = naverMap.uiSettings
        naverMap.locationSource = locationSource
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        setOverlayOnclickListener()
        uiSettings.isLocationButtonEnabled = true
        uiSettings.isLogoClickEnabled = false
        uiSettings.isCompassEnabled = true
        uiSettings.isCompassEnabled = false

        naverMap.mapType = NaverMap.MapType.Terrain
        setMarkers()

        val cameraPosition = CameraPosition(LatLng(36.3740953, 127.3656277), 17.0)
        naverMap.cameraPosition = cameraPosition
        infoWindow = InfoWindow()

        infoWindow.adapter = object : InfoWindow.DefaultViewAdapter(requireContext()) {
            override fun getContentView(iw: InfoWindow): View {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_point, mContainer, false)
                var marker = iw.marker
                view.infoNameTv.text = markerMap[marker!!.tag]!!.userId
                view.infoProfileImgView.setImageBitmap(marker.icon.getBitmap(context))


                return view
            }
        }
    }

    // 위치 권한 확인 및 요청
    private fun checkPermissions() {
        TedPermission.create()
            .setPermissionListener(object : PermissionListener {
                override fun onPermissionGranted() {
                    fusedLocationProviderClient =
                        LocationServices.getFusedLocationProviderClient(context!!)
//                    setUpdateLocationListener()
                    //location 요청 함수 호출 (locationRequest, locationCallback)
                    locationCallback = object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult?) {
                            locationResult ?: return
                            for ((i, location) in locationResult.locations.withIndex()) {
                                Log.d("location: ", "${location.latitude}, ${location.longitude}")
                                setLastLocation(location)
                            }
                        }
                    }

                    hasPermission = true
                }

                override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                    Toast.makeText(
                        requireContext(),
                        "위치 권한 허용을 하지 않으면 서비스를 이용할 수 없습니다.",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                    hasPermission = false
                }

            })
            .setRationaleTitle("위치 권한 요청")
            .setRationaleMessage("현재 위치로 이동하기 위해 위치 권한이 필요합니다")
            .setDeniedMessage("[설정] -> [권한] 에서 위치 권한을 허용해주세요.")
            .setPermissions(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).check()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) { // 권한 거부됨
                naverMap.locationTrackingMode = LocationTrackingMode.None

                checkPermissions()
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setUpdateLocationListener() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    private fun setLastLocation(location: Location) {
        val myLocation = LatLng(location.latitude, location.longitude)
        if (myMarker != null) {
            myMarker!!.map = null
        }

        val marker = Marker()
        marker.position = myLocation
        marker.width = 150
        marker.height = 150

        if (bitmap == null) {
            marker.icon = defaultImage
        } else {
            marker.icon = OverlayImage.fromBitmap(bitmap!!)
            Log.d("userData", "bitmap set")
        }

        marker.map = naverMap

        myMarker = marker
    }

    object ImageLoader {
        fun loadImage(imageUrl: String): Bitmap? {
            var bitmap: Bitmap? = null
            try {
                val url = URL(imageUrl)
                val stream = url.openStream()
                bitmap = BitmapFactory.decodeStream(stream)
                Log.d("tagtag", "a " + (bitmap == null))
                return bitmap
            } catch (e: MalformedURLException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return bitmap
        }

    }

    private fun getRoundedCornerBitmap(bitmap: Bitmap): Bitmap? {
        var output: Bitmap = if (bitmap.width > bitmap.height) {
            Bitmap.createBitmap(
                bitmap,
                (bitmap.width - bitmap.height) / 2,
                0,
                bitmap.height,
                bitmap.height
            )
        } else if (bitmap.width < bitmap.height) {
            var bit: Bitmap = Bitmap.createBitmap(
                bitmap,
                0,
                (bitmap.height - bitmap.width) / 2,
                bitmap.width,
                bitmap.width
            )
            Bitmap.createBitmap(
                bit.width,
                bit.height, Bitmap.Config.ARGB_8888
            )
        } else {
            Bitmap.createBitmap(
                bitmap.width,
                bitmap.height, Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        val rect = Rect(0, 0, output.width, output.height)
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawCircle(output.width / 2f, output.height / 2f, output.width / 2f, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        return output
    }

    private fun setMarkers() {
        for (i in 0 until markerArr.size) {
            CoroutineScope(Dispatchers.Main).launch {
                var marker: Marker
                if (markerArr[i].profileURL != null) {

                    val loc = LatLng(markerArr[i].latitude, markerArr[i].longitude)
                    marker = Marker()
                    marker.position = loc
                    marker.width = 150
                    marker.height = 150
                    marker.tag = markerArr[i].userId

                    var profileBitmap: Bitmap? = null
                    profileBitmap = withContext(Dispatchers.IO) {
                        ImageLoader.loadImage(markerArr[i].profileURL)
                    }
                    Log.d("tag", "b = " + (profileBitmap == null))
                    if (profileBitmap != null)
                        profileBitmap = getRoundedCornerBitmap(profileBitmap!!)

                    if (profileBitmap == null) {
                        marker.icon = defaultImage
                    } else {
                        marker.icon = OverlayImage.fromBitmap(profileBitmap!!)
                    }
                    marker.map = naverMap
                    marker.onClickListener = listener
                }
            }


        }
    }

    private fun setOverlayOnclickListener() {
        listener = Overlay.OnClickListener {
            var marker = it as Marker
            if (marker.infoWindow != null) {
                infoWindow.close()
                true
            } else {
                infoWindow.open(marker)
                true
            }

            false
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000


        @JvmStatic
        fun newInstance(userData: Array<String>) =
            MapFragment().apply {
                arguments = Bundle().apply {
                    putStringArray(ARG_PARAM1, userData)
                }
            }
    }
}