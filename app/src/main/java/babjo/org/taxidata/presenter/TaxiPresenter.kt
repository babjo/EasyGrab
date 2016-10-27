package babjo.org.taxidata.presenter

import android.util.Log
import babjo.org.taxidata.api.GetTaxiDataNearMeRequest
import babjo.org.taxidata.api.GetTaxiDataNearMeResponse
import babjo.org.taxidata.usecase.DefaultSubscriber
import babjo.org.taxidata.usecase.GetTaxiDataNearMeUseCase
import babjo.org.taxidata.view.MainView

/**
 * Created by LCH on 2016. 10. 27..
 */

class TaxiPresenter(val getTaxiDataNearMeUseCase: GetTaxiDataNearMeUseCase, val mainView : MainView) : Presenter{

    private val TAG = TaxiPresenter::class.simpleName

    override fun pause() {
    }

    override fun destroy() {
        getTaxiDataNearMeUseCase.unsubscribe()
    }

    override fun resume() {
    }

    fun searchCntOn(longitude: Double, latitude: Double) {
        mainView.onPreSearchCntOn()
        getTaxiDataNearMeUseCase.execute(GetTaxiDataNearMeRequest(longitude, latitude, 0.1),
                object : DefaultSubscriber<GetTaxiDataNearMeResponse>() {
                    override fun onNext(o: GetTaxiDataNearMeResponse) {
                        Log.d(TAG, ""+o.data.resultList.size)
                        mainView.onPostSearchCntOn(o.data.resultList)
                    }
            })
    }

    fun  searchCntOff(longitude: Double, latitude: Double) {
        mainView.onPreSearchCntOff()
        getTaxiDataNearMeUseCase.execute(GetTaxiDataNearMeRequest(longitude, latitude, 0.1),
                object : DefaultSubscriber<GetTaxiDataNearMeResponse>() {
                    override fun onNext(o: GetTaxiDataNearMeResponse) {
                        Log.d(TAG, "" + o.data.resultList.size)
                        mainView.onPostSearchCntOn(o.data.resultList)
                    }
                })
    }
}
