/**
 *  Belongs to data-camera-web
 *  Author: liye on 2017/11/11
 *  Description: charts options
 */

var app_chart_data_zoom = [{
    show: true,
    height: 30,
    xAxisIndex: [0],
    bottom: 0,
    start: 0,
    end: 100,
    handleIcon: 'M10.7,11.9v-1.3H9.3v1.3c-4.9,0.3-8.8,4.4-8.8,9.4c0,5,3.9,9.1,8.8,9.4v1.3h1.3v-1.3c4.9-0.3,8.8-4.4,8.8-9.4C19.5,16.3,15.6,12.2,10.7,11.9z M13.3,24.4H6.7V23h6.6V24.4z M13.3,19.6H6.7v-1.4h6.6V19.6z',
    handleSize: '80%',
    handleStyle: {
        color: '#fff',
        shadowBlur: 3,
        shadowColor: 'rgba(0, 0, 0, 0.6)',
        shadowOffsetX: 2,
        shadowOffsetY: 2
    },
    textStyle:{
        color:"#fff"},
    borderColor:"#eee"
}, {
    type: "inside",
    xAxisIndex: [0],
    start: 0,
    end: 100
}];

var app_chart_tooltip = {
    trigger: 'axis',
    axisPointer: {
        type: 'cross'
    },
    backgroundColor: 'rgba(245, 245, 245, 0.8)',
    borderWidth: 1,
    borderColor: '#ccc',
    padding: 10,
    textStyle: {
        color: '#000'
    },
    position: function (pos, params, el, elRect, size) {
        var obj = {top: 10};
        obj[['left', 'right'][+(pos[0] < size.viewSize[0] / 2)]] = 30;
        return obj;
    },
    extraCssText: 'width: 170px'
};

var experimentChartOption = function (legend) {
    var series = [];
    for (var index in legend){
        var name = legend[index];
        series.push({
            name: name,
            type: 'line',
            symbolSize: 6,
            symbol:'circle',
            hoverAnimation: false,
            markArea: {
                silent: true,
                data: []
            },
            markPoint: {
                data: [
                    {type: 'max', name: '最大值'},
                    {type: 'min', name: '最小值'}
                ]
            },
            data: []
        })
    }

    return {
        tooltip: app_chart_tooltip,
        legend: {
            top: '0%',
            left: 'center',
            data: legend
        },
        grid: [{
            borderWidth: 0,
            top: '10%',
            bottom: '20%',
            left: '50px',
            right: '50px',
            textStyle: {
                color: "#fff"
            }
        }],
        calculable: true,
        toolbox: {
            show: true,
            feature: {
                dataView: {readOnly: false},
                magicType : {show: true, type: ['line', 'bar']},
                restore: {},
                saveAsImage: {},
                brush: {
                    type: ['lineX', 'clear']
                }
            },
            right: 20
        },
        dataZoom: app_chart_data_zoom,
        xAxis: [
            {
                type: 'time',
                name: 'TIME',
                nameRotate: 45,
                boundaryGap : ['20%', '20%'],
                axisPointer: {
                    show: true,
                    type: 'line',
                    snap: true,
                    z: 100
                }
            }
        ],
        yAxis: [
            {
                type: 'value',
                name: 'VALUE',
                scale: true,
                splitArea: {
                    show: true
                },
                boundaryGap: ['0%', '0%']
            }
        ],
        series: series
    };
};

