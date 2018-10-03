/**
 *  应用页面程序入口
 */
var analysisObject = (function () {
    // 判断是否是第一次进入页面
    var iFi = true;

    // -- 当前显示的所有echarts对象, key: domId, value: chartInstance
    var chart = {};
    var setChart = function (key, value) {
        chart[key] = value;
    };

    // -- 当前选中的数据片段data, key: domId, value: data
    var chartData = {};
    var setChartData = function (key, value) {
        chartData[key] = value;
    };
    var getChartData = function () {
        return $.extend(true, {}, chartData);
    };

    // -- 分析页面中选中查看的图表，key: domId, value: chartInstance
    var selectedChart = {};
    var setSelectedChart = function (key, value) {
        selectedChart[key] = value;
    };
    var removeSelectedChart = function (key) {
        delete selectedChart[key];
    };

    // --


    // -- 视频dom
    var video = {};

    var setVideo = function (key, value) {
        video[key] = value;
    };

    // -- 视频的起始时间，主要用于控制子片段视频的播放
    var videoStartTime = 0;

    // -- 当前片段的时间轴数据，如：2018-03-04 00:00:00
    var timeline = [];
    var timelineStart, timelineEnd;

    // -- 当前片段的时间秒数数据，如：00:00
    var secondLine = [];

    // -- node 数据
    var rDataMap = {};

    // -- 实验片段信息
    var currentRecorderId = null;

    // -- 标记是否正在回放 (play, pause, normal)
    var playStatus = "normal";

    var shareSelectedTags = [];
    var addTag = function (tag) {
        shareSelectedTags.push(tag);
    };
    var clearTag = function () {
        shareSelectedTags.empty();
    };

    // 分享内容的图片
    var contentImg = "";

    return {
        iFi: iFi,

        chart: chart,
        setChart: setChart,

        chartData: chartData,
        setChartData: setChartData,
        getChartData: getChartData,

        selectedChart: selectedChart,
        setSelectedChart: setSelectedChart,
        removeSelectedChart: removeSelectedChart,

        video: video,
        setVideo: setVideo,
        videoStartTime: videoStartTime,
        timeline: timeline,
        timelineStart: timelineStart,
        timelineEnd: timelineEnd,
        secondLine: secondLine,
        rDataMap: rDataMap,
        currentRecorderId: currentRecorderId,
        playStatus: playStatus,
        shareSelectedTags: shareSelectedTags,
        addTag: addTag,
        clearTag: clearTag,
        contentImg: contentImg
    };
})();

var expObject = (function (){
    var iFi = true;

    // -- 当前显示的所有echarts对象, key: domId, value: chartInstance
    var chart = {};

    var setChart = function (key, value) {
        chart[key] = value;
    };
    var currentExpId = 0;

    // -- 视频dom，临时演示用
    var video = {};

    var setVideo = function (key, value) {
        video[key] = value;
    };

    return {
        iFi: iFi,
        currentExpId: currentExpId,
        chart: chart,
        setChart: setChart,
        video: video,
        setVideo: setVideo
    };
})();

$(function () {
    if (typeof(app) !== "undefined" && null != app) {
        var $appMenu = $('#app-menu');
        var $contentMenu = $('#content-menu');

        // -- 进入页面的动作
        if (getQueryString("tab") === "2") {
            $contentMenu.attr("hidden", false);
            $appMenu.attr("hidden", true);
            initTreeDom(analysisObject.iFi);
        } else {
            initExperiment(expObject.iFi);
        }

        // --- 若有数据片段标识，则展示数据
        var recorder = getQueryString("recorder");
        if (recorder != null){
            Object.keys(recorders).forEach(function (rid) {
                if (recorder === rid){
                    showRecorderContent(findParent(recorder) + '', recorder);
                }
            });
        }

        // -- tab页面切换时切换菜单
        var $app_main_tab = $('#app-main-tab');
        $app_main_tab.find('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
            var page = e.target.getAttribute('href');
            if (page === "#app-experiment"){
                $contentMenu.attr("hidden", true);
                $appMenu.attr("hidden", false);
                initExperiment(expObject.iFi);
            } else if (page === "#app-analysis") {
                $contentMenu.attr("hidden", false);
                $appMenu.attr("hidden", true);
                initTreeDom(analysisObject.iFi);
            }
        });

        // -- 添加传感器时的多选框
        initExpSelect();
    }
});