/**
 *  Belongs to data-camera-web
 *  Author: liye on 2018/3/8
 *  Description:
 */

var analysisObject = (function () {
    // 判断是否是第一次进入页面
    var iFi = true;

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

    // -- 当前片段的时间轴数据，如：2018-03-04 00:00:00
    var timeline = [];
    var timelineStart, timelineEnd;

    // -- 当前片段的时间秒数数据，如：00:00
    var secondLine = [];

    // -- node 数据
    var rDataMap = {};

    // -- 实验片段信息
    var currentRecorderId = null;

    // -- 标记是否正在回放
    var playStatus = false;

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
        playStatus: playStatus,
        shareSelectedTags: shareSelectedTags,
        addTag: addTag,
        clearTag: clearTag,
        contentImg: contentImg
    };
})();

var expObject = (function (){
    var iFi = true;

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

    // -- 当前显示的所有echarts对象, key: domId, value: chartInstance
    var chart = {};

    var setChart = function (key, value) {
        chart[key] = value;
    };

    var currentExpId = 0;

    return {
        iFi: iFi,
        newestTimestamp: newestTimestamp,
        setNewTime: setNewTime,
        recorderTimestamp: recorderTimestamp,
        setRecorderTime: setRecorderTime,
        currentExpId: currentExpId,
        chart: chart,
        setChart: setChart
    };
})();

// 初始化APP页面信息
if (typeof(app) != "undefined" && null != app) {
    inAppPage();
}

function inAppPage(){
    // -- 进入页面的动作
    var tab = getQueryString("tab");
    var recorder = getQueryString("recorder");
    if (tab == 2){
        $('#content-menu').attr("hidden", false);
        $('#app-menu').attr("hidden", true);
        initTreeDom(analysisObject.iFi);
    } else {
        initExperiment(expObject.iFi);
    }

    // --- 若有数据片段标识，则展示数据
    if (recorder != null){
        for (var index=0; index<recorders[app['id']].length; index++){
            if (recorders[app['id']][index]['id'] == recorder){
                showRecorderContent(findParent(recorder) + '', recorder);
            }
        }
    }

    // -- tab change
    var $app_main_tab = $('#app-main-tab');
    $app_main_tab.find('a[data-toggle="tab"]').on('shown.bs.tab', function (e) {
        var page = e.target.getAttribute('href');
        if (page == "#app-experiment"){
            $('#content-menu').attr("hidden", true);
            $('#app-menu').attr("hidden", false);
            initExperiment(expObject.iFi);
        } else if (page == "#app-analysis") {
            $('#content-menu').attr("hidden", false);
            $('#app-menu').attr("hidden", true);
            initTreeDom(analysisObject.iFi);
        }
    });

    // -- 添加传感器时的多选框
    initExpSelect();
    function initExpSelect(){
        var $exp_select = $('#exp-select');
        var valueHtml = '<optgroup label="分组：数值型传感器">';
        var videoHtml = '<optgroup label="分组：摄像头">';
        sensors.forEach(function (sensor, index) {
            var id = sensor['id'];
            var type = sensor['sensorConfig']['type'];
            var text = sensor['name'];
            if (type == 1){
                if (freeSensors.hasOwnProperty(id)){
                    valueHtml += '<option value="' + sensor.id + '">' + text + '</option>';
                } else {
                    text += "(已绑定)";
                    valueHtml += '<option value="' + sensor.id + '" disabled="disabled">' + text + '</option>';
                }
            } else if (type == 2){
                if (freeSensors.hasOwnProperty(id)){
                    videoHtml += '<option value="' + sensor.id + '">' + text + '</option>';
                } else {
                    text += "(已绑定)";
                    videoHtml += '<option value="' + sensor.id + '" disabled="disabled">' + text + '</option>';
                }
            }
        });
        valueHtml += '</optgroup>';
        videoHtml += '</optgroup>';
        $exp_select.html(valueHtml + videoHtml);

        $exp_select.multiSelect({
            selectableHeader: "<input type='text' class='form-control muti-select-search-input' autocomplete='off' placeholder='try search'>",
            selectionHeader: "<div style='height: 40px'><strong>已选择的传感器</strong></div>",
            afterInit: function(ms){
                var that = this,
                    $selectableSearch = that.$selectableUl.prev(),
                    selectableSearchString = '#'+that.$container.attr('id')+' .ms-elem-selectable:not(.ms-selected)';

                that.qs1 = $selectableSearch.quicksearch(selectableSearchString)
                    .on('keydown', function(e){
                        if (e.which === 40){
                            that.$selectableUl.focus();
                            return false;
                        }
                    });
            },
            afterSelect: function(){
                this.qs1.cache();
            },
            afterDeselect: function(){
                this.qs1.cache();
            }
        });
    }
}