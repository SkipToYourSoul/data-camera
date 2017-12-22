/**
 *  Belongs to data-camera-web
 *  Author: liye on 2017/12/13
 *  Description:
 */

function inAppPage(){
    var $loader = $("#app-loading");
    var $app_main_tab = $('#app-main-tab');
    var tab = getQueryString("tab");
    if (tab == null){
        // initResourceOfExperimentPage();
        // initResourceOfAnalysisPage();
    } else if (tab != null && tab == 2){
        $loader.fakeLoader({
            timeToHide: 1000,
            spinner:"spinner3",
            bgColor:"rgba(154, 154, 154, 1)"
        });
        $app_main_tab.find('li:eq(1) a').tab('show');
        initResourceOfAnalysisPage();
    }

    initTreeDom();

    // -- tab change
    $app_main_tab.find('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        var page = e.target.getAttribute('href');
        if (page == "#app-experiment"){
            initResourceOfExperimentPage();
            $('#content-menu').attr("hidden", true);
            $('#app-menu').attr("hidden", false);
        } else if (page == "#app-analysis") {
            // initResourceOfAnalysisPage();
            $('#content-menu').attr("hidden", false);
            $('#app-menu').attr("hidden", true);
            init();
        }
    });

    $('#app-analysis-tab').find('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        var page = e.target.getAttribute('href');
        var exp_id = page.split('-')[2];
        initExpContentChart(exp_id);
    });
}