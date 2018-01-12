/**
 *  Belongs to data-camera-web
 *  Author: liye on 2017/12/13
 *  Description:
 */

function inAppPage(){
    var $loader = $("#app-loading");
    var $app_main_tab = $('#app-main-tab');

    // -- 进入页面的动作
    var tab = getQueryString("tab");
    var recorder = getQueryString("recorder");
    if (tab == null){
        // 进入页面时默认为实验模式
        initExperiment();
        appObject.iFe = false;
    } else if (tab != null && tab == 2){
        $app_main_tab.find('li:eq(1) a').tab('show');
        $('#content-menu').attr("hidden", false);
        $('#app-menu').attr("hidden", true);
        initTreeDom(true);
        appObject.iFa = false;
    }
    if (recorder != null){
        for (var index=0; index<recorders[app['id']].length; index++){
            if (recorders[app['id']][index]['id'] == recorder){
                showRecorderContent(findParent(recorder) + '', recorder);
            }
        }
    }

    // -- tab change
    $app_main_tab.find('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        var page = e.target.getAttribute('href');
        if (page == "#app-experiment"){
            initExperiment();
            $('#content-menu').attr("hidden", true);
            $('#app-menu').attr("hidden", false);
        } else if (page == "#app-analysis") {
            // initResourceOfAnalysisPage();
            $('#content-menu').attr("hidden", false);
            $('#app-menu').attr("hidden", true);
            initTreeDom();
        }
    });
}

var appObject = (function () {
    var isFirstTimeInExp = true;
    var isFirstTimeInAnalysis = true;

    return {
        iFe: isFirstTimeInExp,
        iFa: isFirstTimeInAnalysis
    }
})();

var analysisObject = (function () {
    // -- 当前显示的所有echarts对象, key: domId, value: chartInstance
    var chart = {};

    // -- 当前选中的数据片段data, key: domId, value: data
    var chartData = {};

    var setChart = function (key, value) {
        chart[key] = value;
    };

    // deep copy
    var getChart = function () {
        return $.extend(true, {}, chart);
    };

    var setChartData = function (key, value) {
        chartData[key] = value;
    };

    var getChartData = function () {
        return $.extend(true, {}, chartData);
    };

    // -- 视频dom
    var video = {};

    var setVideo = function (key, value) {
        video[key] = value;
    };

    // -- 当前片段的时间轴数据
    var timeline = [];
    var timelineStart, timelineEnd;

    // -- 当前片段的时间秒数数据
    var secondLine = [];

    // -- node 数据
    var rDataMap = {};

    // -- 实验片段信息
    var currentRecorderId = null;

    // -- 标记是否正在回放
    var recorderInterval = null;

    return {
        chart: chart,
        setChart: setChart,
        getChart: getChart,
        chartData: chartData,
        setChartData: setChartData,
        getChartData: getChartData,
        video: video,
        setVideo: setVideo,
        timeline: timeline,
        timelineStart: timelineStart,
        timelineEnd: timelineEnd,
        secondLine: secondLine,
        rDataMap: rDataMap,
        currentRecorderId: currentRecorderId,
        recorderInterval: recorderInterval
    };
})();

var expObject = (function (){
    /**
     * key: exp_id
     * value: timestamp
     * @type {{}}
     */
    var newestTimestamp = {};

    /**
     * key: exp_id
     * value: 2017-08-17T18:16:31.000+08:00
     * @type {{}}
     */
    var recorderTimestamp = {};

    var setNewTime = function (key, value) {
        newestTimestamp[key] = value;
    };

    var setRecorderTime = function (key, value) {
        recorderTimestamp[key] = value;
    };

    var newObjectText = "确认创建";
    var editObjectText = "确认修改";

    var currentExpId = 0;
    
    return {
        newestTimestamp: newestTimestamp,
        setNewTime: setNewTime,
        recorderTimestamp: recorderTimestamp,
        setRecorderTime: setRecorderTime,
        newObjectText: newObjectText,
        editObjectText: editObjectText,
        currentExpId: currentExpId
    };
})();