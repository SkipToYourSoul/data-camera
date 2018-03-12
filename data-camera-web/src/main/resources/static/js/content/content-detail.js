/**
 *  Belongs to data-camera-web
 *  Author: liye on 2018/1/19
 *  Description:
 */

$(function () {
    var recorderId = currentContent['recorderInfo']['id'];
    $('.app-analysis-chart-dom').attr('hidden', false);
    askForRecorderDataAndInitDom(recorderId);
});