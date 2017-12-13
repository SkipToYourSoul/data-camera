/**
 *  Belongs to data-camera-web
 *  Author: liye on 2017/12/13
 *  Description:
 */

function inAppPage(){
    var tab = getQueryString("tab");
    if (tab == null){
        initResourceOfExperimentPage();
    } else if (tab != null && tab == 2){
        $('#app-main-tab').find('li:eq(1) a').tab('show');
        initResourceOfAnalysisPage();
    }
}