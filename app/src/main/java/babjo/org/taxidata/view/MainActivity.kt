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
import babjo.org.taxidata.presenter.TaxiPresenter
import babjo.org.taxidata.usecase.GetTaxiDataNearMeUseCase
import babjo.org.taxidata.view.component.TaxiMap
import net.daum.mf.map.api.MapLayout
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView

class MainActivity : AppCompatActivity(),
        MapView.MapViewEventListener, MapView.CurrentLocationEventListener, MainView {

    private val TAG = MainActivity::class.simpleName
    private var mTaxiMap: TaxiMap? = null
    private var mProgressBar: ProgressBar? = null
    private var mFloatingBtn: FloatingActionButton? = null
    private var mTaxiPresenter: TaxiPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapLayout = MapLayout(this)

        mTaxiMap = TaxiMap(mapLayout.mapView)
        mTaxiMap!!.setMapViewEventListener(this)
        mTaxiMap!!.setCurrentLocationEventListener(this)

        val mapViewContainer = findViewById(R.id.map_view) as ViewGroup
        mapViewContainer.addView(mapLayout)

        mProgressBar = findViewById(R.id.progress) as ProgressBar
        mFloatingBtn = findViewById(R.id.fab) as FloatingActionButton

        mFloatingBtn?.setOnClickListener {
            checkGPSPermission(this, Const.PERMISSION_REQUEST_TRACKING, {
                switchTracking()
            })
        }

        mTaxiPresenter = TaxiPresenter(GetTaxiDataNearMeUseCase(), this)

        val searchCntOnView = findViewById(R.id.searchTaxiNearMe) as RelativeLayout
        searchCntOnView.setOnClickListener {
            checkGPSPermission(this, Const.PERMISSION_REQUEST_SEARCHING, {mTaxiPresenter?.searchTaxiNearMe(mTaxiMap!!.getMapCenterPoint().mapPointGeoCoord.longitude, mTaxiMap!!.getMapCenterPoint().mapPointGeoCoord.latitude)})
        }

    }

    private fun switchTracking() {
        if (mTaxiMap!!.switchTracking()) {
            mFloatingBtn?.setImageDrawable(resources.getDrawable(R.drawable.ic_gps_fixed_white_36dp))
            mFloatingBtn?.backgroundTintList = ColorStateList.valueOf(Color.parseColor(resources.getString(R.color.colorAccent)))
            mProgressBar?.visibility = View.VISIBLE
        } else {
            mFloatingBtn?.setImageDrawable(resources.getDrawable(R.drawable.ic_gps_fixed_black_36dp))
            mFloatingBtn?.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
            mProgressBar?.visibility = View.INVISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mTaxiMap?.trackingModeOff()
        mTaxiPresenter?.destroy()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            Const.PERMISSION_REQUEST_TRACKING -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    switchTracking()
                } else {
                    Toast.makeText(this, Const.PERMISSION_GSP_FAIL_MSG, Toast.LENGTH_LONG).show();
                }
                return
            }
            Const.PERMISSION_REQUEST_SEARCHING ->{
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mTaxiPresenter?.searchTaxiNearMe(mTaxiMap!!.getMapCenterPoint().mapPointGeoCoord.longitude, mTaxiMap!!.getMapCenterPoint().mapPointGeoCoord.latitude)
                } else {
                    Toast.makeText(this, Const.PERMISSION_GSP_FAIL_MSG, Toast.LENGTH_LONG).show();
                }
                return
            }
        }
    }

    override fun onMapViewInitialized(mapView: MapView?) {
        Log.i(TAG, "MapView had loaded. Now, MapView APIs could be called safely")
        mTaxiMap?.removeAllAndAddCircle(mapView?.mapCenterPoint)
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
        mTaxiMap?.removeAllAndAddCircle(mapPoint)
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
        mTaxiMap?.removeAllAndAddCircle(currentLocation)
    }

    override fun onPreSearchTaxiOn() {
        mTaxiMap?.removeAllPolylines()
    }

    override fun onPostSearchTaxiOn(resultList: Array<GetTaxiData>) {
        resultList.sortByDescending { it.cntOn + it.cntOff }
        Log.d(TAG, "Searched TaxiData is a total ${resultList.size}")
        resultList.take(24).forEachIndexed { i, taxiData ->
            mTaxiMap?.addPolyline(taxiData.points, i)
        }

    }

    override fun onErrorSearchTaxiOn(e: Throwable) {
        Toast.makeText(this, Const.USE_CASE_SEARCH_TAXI_EXCEPTION_MSG, Toast.LENGTH_LONG).show()
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
