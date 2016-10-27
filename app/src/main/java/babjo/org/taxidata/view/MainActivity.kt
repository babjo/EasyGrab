package babjo.org.taxidata.view

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import babjo.org.taxidata.MapApiConst
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
    private var isTracking: Boolean = false
    private var mTaxiPresenter: TaxiPresenter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapLayout = MapLayout(this)
        mMapView = mapLayout.mapView

        mMapView?.setDaumMapApiKey(MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY)
        mMapView?.setOpenAPIKeyAuthenticationResultListener(DefaultOpenAPIKeyAuthenticationResultListener())
        mMapView?.setMapViewEventListener(this)
        mMapView?.setCurrentLocationEventListener(this)
        mMapView?.mapType = MapView.MapType.Standard

        val mapViewContainer = findViewById(R.id.map_view) as ViewGroup
        mapViewContainer.addView(mapLayout)

        mProgressBar = findViewById(R.id.progress) as ProgressBar
        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            if(isTracking) {
                trackingModeOff()
                fab.setImageDrawable(resources.getDrawable(R.drawable.ic_gps_fixed_black_36dp))
                fab.backgroundTintList = ColorStateList.valueOf(Color.WHITE)
                isTracking = false
                mProgressBar?.visibility = View.INVISIBLE
            }
            else {
                trackingModeOn()
                fab.setImageDrawable(resources.getDrawable(R.drawable.ic_gps_fixed_white_36dp))
                fab.backgroundTintList = ColorStateList.valueOf(Color.parseColor(resources.getString(R.color.colorAccent)))
                isTracking = true
                mProgressBar?.visibility = View.VISIBLE
            }
        }

        mTaxiPresenter = TaxiPresenter(GetTaxiDataNearMeUseCase(), this)

        val searchCntOnView = findViewById(R.id.searchTaxiNearMe) as RelativeLayout
        searchCntOnView.setOnClickListener {
            mTaxiPresenter?.searchTaxiNearMe(mMapView?.mapCenterPoint!!.mapPointGeoCoord.longitude, mMapView?.mapCenterPoint!!.mapPointGeoCoord.latitude)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        trackingModeOff()
        mTaxiPresenter?.destroy()
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
}
