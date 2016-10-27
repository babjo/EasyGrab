package babjo.org.taxidata

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
import babjo.org.taxidata.api.GetTaxiData
import babjo.org.taxidata.api.Point
import babjo.org.taxidata.presenter.TaxiPresenter
import babjo.org.taxidata.usecase.GetTaxiDataNearMeUseCase
import babjo.org.taxidata.view.MainView
import net.daum.mf.map.api.*
import java.util.*

class MainActivity : AppCompatActivity(),
        MapView.OpenAPIKeyAuthenticationResultListener,
        MapView.MapViewEventListener, MapView.CurrentLocationEventListener, MainView {

    private val TAG = MainActivity::class.simpleName
    private var mMapView: MapView? = null
    private var mProgressBar: ProgressBar? = null
    private var isTracking: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapLayout = MapLayout(this)
        mMapView = mapLayout.mapView

        mMapView?.setDaumMapApiKey(MapApiConst.DAUM_MAPS_ANDROID_APP_API_KEY)
        mMapView?.setOpenAPIKeyAuthenticationResultListener(this)
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

        val taxiPresenter = TaxiPresenter(GetTaxiDataNearMeUseCase(), this)

        val searchCntOnView = findViewById(R.id.searchCntOnNearMe) as RelativeLayout
        searchCntOnView.setOnClickListener {
            taxiPresenter.searchCntOn(mMapView?.mapCenterPoint!!.mapPointGeoCoord.longitude, mMapView?.mapCenterPoint!!.mapPointGeoCoord.latitude)
        }

        val searchCntOffView = findViewById(R.id.searchCntOffNearMe) as RelativeLayout
        searchCntOffView.setOnClickListener {
            taxiPresenter.searchCntOff(mMapView?.mapCenterPoint!!.mapPointGeoCoord.longitude, mMapView?.mapCenterPoint!!.mapPointGeoCoord.latitude)
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        trackingModeOff()
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

    var previousCircle : MapCircle? = null
    private fun removeAllAndAddCircle(mapPoint: MapPoint?) {
        if(previousCircle != null) mMapView?.removeCircle(previousCircle)
        previousCircle = MapCircle(
                mapPoint, // center
                10, // radius
                Color.argb(128, 0, 255, 0), // strokeColor
                Color.argb(128, 0, 255, 0) // fillColor
        )
        mMapView?.addCircle(previousCircle)
    }

    override fun onMapViewLongPressed(p0: MapView?, p1: MapPoint?) {
    }
    override fun onDaumMapOpenAPIKeyAuthenticationResult(mapView: MapView?, resultCode : Int, resultMessage: String?) {
        Log.i(TAG, "Open API Key Authentication Result : code=$resultCode, message=$resultMessage")
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

    val previousPolylineList : ArrayList<MapPolyline> = arrayListOf()
    override fun onPreSearchCntOn() {
        previousPolylineList.forEach { mMapView?.removePolyline(it) }
        previousPolylineList.clear()
    }

    override fun onPostSearchCntOn(resultList: Array<GetTaxiData>?) {
        resultList?.forEach {
            val polyline = createPolyline(it.points)
            previousPolylineList.add(polyline)
            mMapView?.addPolyline(polyline)
            //println(it.points.size)
            //println(it.cntOn)
            //println(it.cntOff)*/
        }
    }

    override fun onPreSearchCntOff() {
        onPreSearchCntOn()
    }

    override fun onPostSearchCntOff(resultList: Array<GetTaxiData>?) {
        resultList?.forEach {
            val polyline = createPolyline(it.points)
            previousPolylineList.add(polyline)
            mMapView?.addPolyline(polyline)
            //println(it.points.size)
            //println(it.cntOn)
            //println(it.cntOff)*/
        }
    }

    private fun createPolyline(points : Array<Point>) : MapPolyline {
        val polyline = MapPolyline()
        polyline.lineColor = Color.argb(Random().nextInt(255), Random().nextInt(255), Random().nextInt(255), 0)
        points.forEach { polyline.addPoint(MapPoint.mapPointWithGeoCoord(it.y, it.x)) }
        return polyline
    }
}
