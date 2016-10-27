package babjo.org.taxidata.view

import babjo.org.taxidata.api.GetTaxiData

/**
 * Created by LCH on 2016. 10. 27..
 */

interface MainView {
    fun onPostSearchCntOn(resultList: Array<GetTaxiData>?)
    fun onPreSearchCntOn()
    fun onPostSearchCntOff(resultList: Array<GetTaxiData>?)
    fun onPreSearchCntOff()
}
