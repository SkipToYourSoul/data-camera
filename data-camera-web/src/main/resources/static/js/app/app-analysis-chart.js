/**
 *  Belongs to data-camera-web
 *  Author: liye on 2017/12/23
 *  Description:
 *    生成实验片段对应的数据图表
 *    使用的后台数据(app, recorders)
 */

// -- 入口函数，初始化数据图表
function initChartDom(recorderId){
    console.info("Request recorder: " + recorderId);
    var recorder = findRecorderInfo(recorderId);
    if (recorder == null){
        console.log("Null data recorder");
        return;
    }
    // 异步请求实验片段数据
    $.ajax({
        type: 'get',
        url: data_addrss + "/get-recorder-data",
        data: {
            "recorder-id": recorderId
        },
        success: function (response) {
            if (response.code == "0000") {
                message_info("请求数据成功", 'success');
                var chartData = response.data['CHART'];
                var videoData = response.data['VIDEO'];
                initDom(chartData, videoData);
            } else if (response.code == "1111") {
                message_info("加载数据失败，失败原因为：" + response.data, 'error');
            }
        },
        error: function (response) {
            message_info("数据请求被拒绝", 'error');
        }
    });

    function initDom(chartData, videoData){
        var $dom = $('#app-analysis-chart-dom');
        // chart
        for (var sensorId in chartData){
            if (!chartData.hasOwnProperty(sensorId)){
                continue;
            }
            for (var legend in chartData[sensorId]){
                var $id = sensorId + "-" + legend;
                $dom.append(generate($id));
            }
        }

        function generate(id) {
            return '<div class="panel panel-default my-panel">' +
                '<div class="my-panel-heading">' +
                '<div class="panel-title">' +
                '<a role="button" data-toggle="collapse" href="#' + id + '" aria-expanded="true"><i class="fa fa-arrows-v"></i>&nbsp; title2</a>' +
                '</div></div>' +
                '<div id="' + id + '" class="panel-collapse collapse in" role="tabpanel">' +
                '<div class="panel-body">body2</div>' +
                '</div></div>';
        }
    }

    /**
     * 找出当前需要展示的recorder
     * @param recorderId
     */
    function findRecorderInfo(recorderId){
        if (!recorders.hasOwnProperty(app['id'])){
            return;
        }
        for (var index=0; index<recorders[app['id']].length; index++){
            if (recorders[app['id']][index]['id'] == recorderId){
                return recorders[app['id']][index];
            }
        }
    }
}
