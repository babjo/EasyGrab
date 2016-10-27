package babjo.org.taxidata.view

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import babjo.org.taxidata.Const
import babjo.org.taxidata.R
import babjo.org.taxidata.api.GetTaxiData
import babjo.org.taxidata.api.Point
import babjo.org.taxidata.presenter.TaxiPresenter
import babjo.org.taxidata.usecase.GetTaxiDataNearMeUseCase
import net.daum.mf.map.api.*

class MainActivity : AppCompatActivity(),
        MapView.MapViewEventListener, MapView.CurrentLocationEventListener, MainView {

    private val TAG = MainActivity::class.simpleName
    private var mMapView: MapView? = null
    private var mProgressBar: ProgressBar? = null
    private var mFloatingBtn: FloatingActionButton? = null

    private var isTracking: Boolean = false
    private var mTaxiPresenter: TaxiPresenter? = null
    private val REQUEST_TRACKING = 0
    private val REQUEST_SEARCHING = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapLayout = MapLayout(this)
        mMapView = mapLayout.mapView

        mMapView?.setDaumMapApiKey(Const.DAUM_MAPS_ANDROID_APP_API_KEY)
        mMapView?.setOpenAPIKeyAuthenticationResultListener(DefaultOpenAPIKeyAuthenticationResultListener())
        mMapView?.setMapViewEventListener(this)
        mMapView?.setCurrentLocationEventListener(this)
        mMapView?.mapType = MapView.MapType.Standard

        val mapViewContainer = findViewById(R.id.map_view) as ViewGroup
        mapViewContainer.addView(mapLayout)

        mProgressBar = findViewById(R.id.progress) as ProgressBar
        mFloatingBtn = findViewById(R.id.fab) as FloatingActionButton

        mFloatingBtn?.setOnClickListener {
            checkGPSPermission(this, REQUEST_TRACKING, {
                switchTracking()
            })
        }

        mTaxiPresenter = TaxiPresenter(GetTaxiDataNearMeUseCase(), this)

        val searchCntOnView = findViewById(R.id.searchTaxiNearMe) as RelativeLayout
        searchCntOnView.setOnClickListener {
            checkGPSPermission(this, REQUEST_SEARCHING, {mTaxiPresenter?.searchTaxiNearMe(mMapView?.mapCenterPoint!!.mapPointGeoCoord.longitude, mMapView?.mapCenterPoint!!.mapPointGeoCoord.latitude)})
        }

    }

    private fun switchTracking() {
        if (isTracking) {
            trackingModeOff()
            mFloatingBtn?.setImageDrawable(resources.getDrawable(R.drawable.ic_gps_fixed_black_36dp))
            mFloatingBtn?.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            isTracking = false
            mProgressBar?.visibility = View.INVISIBLE
        } else {
            trackingModeOn()
            mFloatingBtn?.setImageDrawable(resources.getDrawable(R.drawable.ic_gps_fixed_white_36dp))
            mFloatingBtn?.backgroundTintList = ColorStateList.valueOf(Color.parseColor(resources.getString(R.color.colorAccent)))
            isTracking = true
            mProgressBar?.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        trackingModeOff()
        mTaxiPresenter?.destroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_TRACKING -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    switchTracking()
                } else {
                    Toast.makeText(this, "GPS 권한을 획득하지 못했습니다. 기능 사용을 위해 권한을 설정해주세요.", Toast.LENGTH_LONG).show();
                }
                return
            }
            REQUEST_SEARCHING ->{
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mTaxiPresenter?.searchTaxiNearMe(mMapView?.mapCenterPoint!!.mapPointGeoCoord.longitude, mMapView?.mapCenterPoint!!.mapPointGeoCoord.latitude)
                } else {
                    Toast.makeText(this, "GPS 권한을 획득하지 못했습니다. 기능 사용을 위해 권한을 설정해주세요.",Toast.LENGTH_LONG).show();
                }
                return
            }
        }
    }


    private fun trackingModeOff() {
        mMapView?.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
    }

    private fun trackingModeOn() {
        mMapView?.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading
    }

    override fun onMapViewInitialized(mapView: MapView?) {
        Log.i(TAG, "MapView had loaded. Now, MapView APIs could be called safely")
        removeAllAndAddCircle(mapView?.mapCenterPoint)
    }

    override fun onMapViewDragStarted(p0: MapView?, p1: MapPoint?) {
    }
    override fun onMapViewMoveFinished(p0: MapView?, p1: MapPoint?) {
    }
    override fun onMapViewDragEnded(p0: MapView?, p1: MapPoint?) {
    }
    override fun onMapViewSingleTapped(p0: MapView?, p1: MapPoint?) {
    }
    override fun onMapViewZoomLevelChanged(p0: MapView?, p1: Int) {
    }
    override fun onMapViewDoubleTapped(p0: MapView?, p1: MapPoint?) {
    }
    override fun onMapViewCenterPointMoved(mapView: MapView?, mapPoint: MapPoint?) {
        removeAllAndAddCircle(mapPoint)
    }

    private fun removeAllAndAddCircle(mapPoint: MapPoint?) {
        mMapView?.removeAllCircles()
        mMapView?.addCircle(MapCircle(
                mapPoint, // center
                10, // radius
                Color.argb(128, 0, 255, 0), // strokeColor
                Color.argb(128, 0, 255, 0) // fillColor
        ))
    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {
    }


    override fun onCurrentLocationUpdateFailed(mapView: MapView?) {
    }

    override fun onCurrentLocationDeviceHeadingUpdate(mapView: MapView?, p1: Float) {
    }

    override fun onCurrentLocationUpdateCancelled(mapView: MapView?) {
    }

    override fun onCurrentLocationUpdate(mapView: MapView?, currentLocation: MapPoint?, accuracyInMeters: Float) {
        val mapPointGeo = currentLocation!!.mapPointGeoCoord
        Log.i(TAG, "MapView onCurrentLocationUpdate (${mapPointGeo.latitude},${mapPointGeo.longitude}) accuracy ($accuracyInMeters)")
        mProgressBar?.visibility = View.INVISIBLE
        removeAllAndAddCircle(currentLocation)
    }

    override fun onPreSearchTaxiOn() {
        mMapView?.removeAllPolylines()
    }

    override fun onPostSearchTaxiOn(resultList: Array<GetTaxiData>) {
        resultList.sortByDescending { it.cntOn + it.cntOff }
        Log.d(TAG, "Searched TaxiData is a total ${resultList.size}")
        resultList.take(24).forEachIndexed { i, taxiData ->
            mMapView?.addPolyline(createPolyline(taxiData.points, i))
        }

    }

    override fun onErrorSearchTaxiOn(e: Throwable) {
        Toast.makeText(this, "서버와 통신에서 오류가 발생했습니다. 잠시후 이용해주세요.", Toast.LENGTH_LONG).show()
    }

    private fun createPolyline(points : Array<Point>, num: Int) : MapPolyline {
        val polyline = MapPolyline()
        when(num){
            in 0..12 -> polyline.lineColor = Color.rgb(num * 20, 255, 0)
            in 13..24 -> polyline.lineColor = Color.rgb(255, 255 - (num - 20) * 15, 0)
        }
        points.forEach { polyline.addPoint(MapPoint.mapPointWithGeoCoord(it.y, it.x)) }
        return polyline
    }

    fun checkGPSPermission(activity: Activity, requestCode: Int, callback: () -> Unit) {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), requestCode)
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), requestCode)
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        } else {
            callback()
        }
    }
}
