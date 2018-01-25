/**
 *  Belongs to smart-sensor
 *  Author: liye on 2017/7/25
 *  Description: some common function
 */
var context_path = "/camera";
var base_address = window.location.origin + context_path;
var crud_address = base_address + "/crud";
var action_address = base_address + "/action";
var data_address = base_address + "/data";

var current_address = window.location.href;
if (current_address.indexOf('?') >= 0){
    current_address = current_address.substring(0, window.location.href.indexOf('?'));
}

var commonObject = (function () {
    // ajax msg
    var ajaxRejectMsg = function () {
        message_info("请求服务器数据被拒绝", 'error');
        console.warn("请求服务器数据被拒绝");
    };

    var ajaxExceptionMsg = function (e) {
        message_info("后台服务异常，原因为: " + e, 'error');
        console.warn("后台服务异常，原因为: " + e);
    };

    return {
        printRejectMsg: ajaxRejectMsg,
        printExceptionMsg: ajaxExceptionMsg
    }
})();

$._messengerDefaults = {
    extraClasses: 'messenger-fixed messenger-on-top',
    theme: 'air'
};

function message_info(text, type) {
    var hide = arguments[2]?arguments[2]:10;
    Messenger().post({
        message: text,
        type: type,
        hideAfter: hide,
        hideOnNavigate: true,
        showCloseButton: true
    });
}

// parse 2017-08-17T18:16:31.000+08:00 to 2017-08-17 18:16:31
function parseTime(time) {
    return time.split('.')[0].replace('T', ' ');
}

// --- rewrite format function, new Date(xx,xx,xx).Format("yyyy-MM-dd HH:mm:ss")
Date.prototype.Format = function (fmt) {
    var o = {
        "M+": this.getMonth() + 1, //月份
        "d+": this.getDate(), //日
        "H+": this.getHours(), //小时
        "m+": this.getMinutes(), //分
        "s+": this.getSeconds(), //秒
        "q+": Math.floor((this.getMonth() + 3) / 3), //季度
        "S": this.getMilliseconds() //毫秒
    };
    if (/(y+)/.test(fmt)) fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "").substr(4 - RegExp.$1.length));
    for (var k in o)
        if (new RegExp("(" + k + ")").test(fmt)) fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k]) : (("00" + o[k]).substr(("" + o[k]).length)));
    return fmt;
};

// --- some basic operations of object
function isEmptyObject(obj) {
    for (var key in obj){
        if (obj.hasOwnProperty(key)){
            return false;//返回false，不为空对象
        }
    }
    return true;//返回true，为空对象
}

function getObjectLength(obj){
    var n, count = 0;
    for(n in obj){
        if(obj.hasOwnProperty(n)){
            count++;
        }
    }
    return count;
}

// --- 判断dom是否存在, 如: $('#id').exist()
$.fn.exist = function(){
    if($(this).length>=1){
        return true;
    }
    return false;
};

/**
 * 获取url参数
 *
 * @param name
 * @returns {null}
 * @constructor
 */
function getQueryString(name) {
    var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if(r!=null){
        return  unescape(r[2]);
    }
    return null;
}

// --- APP PAGE
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

    // -- 标签选择框
    $('.content-tag-dropdown').dropdown({
        limitCount: 5,
        multipleMode: 'label',
        input: '<input type="text" maxLength="20" placeholder="输入标签">',
        data: [
            {
                "id": 1, // value值
                "disabled": false, // 是否禁选
                "selected": false, // 是否选中
                "name": "温湿度" // 名称
            },
            {
                "id": 2,
                "disabled": false,
                "selected": false,
                "name": "光照"
            },
            {
                "id": 3,
                "disabled": false,
                "selected": false,
                "name": "摄像头"
            }
        ],
        choice: function (event, data) {
            console.log(arguments,this);
            console.log(event);
            console.log(data);
        }
    });

    // -- 图片上传
    $("#file-upload-input").fileinput({
        language: 'zh',
        uploadUrl: data_address + "/file-upload", // server upload action
        allowedFileExtensions: ['jpg', 'png'],
        uploadAsync: true,
        dropZoneEnabled: false,
        showUpload: false,
        autoReplace: true,
        maxFileSize: 1024,
        maxFileCount: 1
    }).on('fileselect', function (event, files) {
        // 选择文件后自动上传
        $("#file-upload-input").fileinput('upload');
    }).on('filesuccessremove', function(event, id) {//点击删除后立即执行
        $('#fileId').fileinput('refresh');//文件框刷新操作
        console.info("file remove");
    }).on('fileuploaded', function(event, data, previewId, index) {
        console.info("file uploaded");
        if (data.response.code == "0000"){
            analysisObject.contentImg = data.response.data;
        } else {
            commonObject.printExceptionMsg(data.response.data);
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
        recorderInterval: recorderInterval,
        shareSelectedTags: shareSelectedTags,
        addTag: addTag,
        clearTag: clearTag,
        contentImg: contentImg
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