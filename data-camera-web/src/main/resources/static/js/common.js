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

var SUCCESS = "0000";
var FAILURE = "1111";

var commonObject = (function () {
    // ajax msg
    var ajaxRejectMsg = function () {
        message_info("请求服务器数据被拒绝", 'error');
        console.info("请求服务器数据被拒绝");
    };

    var ajaxExceptionMsg = function (e) {
        message_info("后台服务异常，原因为: " + e, 'error');
        console.info("后台服务异常，原因为: " + e);
    };

    var newObjectText = "确认创建";
    var editObjectText = "确认修改";

    return {
        printRejectMsg: ajaxRejectMsg,
        printExceptionMsg: ajaxExceptionMsg,
        newText: newObjectText,
        editText: editObjectText
    }
})();

$._messengerDefaults = {
    extraClasses: 'messenger-fixed messenger-on-top messenger-on-right',
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
    if (typeof (time) == "number"){
        return new Date(time).Format("yyyy-MM-dd HH:mm:ss");
    } else if (typeof (time) == "string"){
        return time.split('.')[0].replace('T', ' ');
    }
}

// 2017-08-17 18:16:31.100 -->> number
// number -->> 2017-08-17 18:16:31.100
function transferTime(time) {
    if (typeof (time) === "string") {
        return new Date(time).getTime();
    } else if (typeof (time) === "number") {
        return new Date(time).Format("yyyy-MM-dd HH:mm:ss.S");
    }
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

/**
 * 新增浏览器窗口大小监控事件、调整echarts图表大小
 * @param charts
 */
function onChartResize(charts) {
    Object.keys(charts).forEach(function (id) {
        if (null != echarts.getInstanceByDom(document.getElementById(id)) && typeof(echarts.getInstanceByDom(document.getElementById(id))) != "undefine" ){
            var chart = charts[id];
            chart.resize();
        }
    })
}

var deviceObject = (function () {
    var currentSensorId = -1;
    var deviceImg = "";
    return {
        currentSensorId: currentSensorId,
        deviceImg: deviceImg
    }
})();