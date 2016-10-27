package babjo.org.taxidata.view.component
import android.graphics.Color
import babjo.org.taxidata.Const
import babjo.org.taxidata.api.Point
import babjo.org.taxidata.view.DefaultOpenAPIKeyAuthenticationResultListener
import net.daum.mf.map.api.MapCircle
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapPolyline
import net.daum.mf.map.api.MapView
/**
 * Created by LCH on 2016. 10. 27..
 */
class TaxiMap{
    private val mMapView : MapView
    private var isTracking: Boolean = false

    constructor(mapView: MapView){
        mMapView = mapView
        mapView.setDaumMapApiKey(Const.DAUM_MAPS_ANDROID_APP_API_KEY)
        mapView.setOpenAPIKeyAuthenticationResultListener(DefaultOpenAPIKeyAuthenticationResultListener())
        mapView.mapType = MapView.MapType.Standard
    }

    fun setMapViewEventListener(listener : MapView.MapViewEventListener){
        mMapView.setMapViewEventListener(listener)
    }

    fun setCurrentLocationEventListener(listener : MapView.CurrentLocationEventListener){
        mMapView.setCurrentLocationEventListener(listener)
    }

    fun switchTracking() : Boolean{
        if (isTracking) {
            trackingModeOff()
            isTracking = false
        } else {
            trackingModeOn()
            isTracking = true
        }
        return isTracking
    }

    fun trackingModeOff() {
        mMapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
    }

    fun trackingModeOn() {
        mMapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading
    }

    fun getMapCenterPoint(): MapPoint = mMapView.mapCenterPoint!!
    fun removeAllPolylines() {
        mMapView.removeAllPolylines()
    }

    fun removeAllAndAddCircle(mapPoint: MapPoint?) {
        mMapView.removeAllCircles()
        mMapView.addCircle(MapCircle(
                mapPoint, // center
                10, // radius
                Color.argb(128, 0, 255, 0), // strokeColor
                Color.argb(128, 0, 255, 0) // fillColor
        ))
    }

    fun addPolyline(points : Array<Point>, num:Int) {
        mMapView.addPolyline(createPolyline(points, num))
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