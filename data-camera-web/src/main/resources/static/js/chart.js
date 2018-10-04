/**
 *  Belongs to data-camera-web
 *  Author: liye on 2018/1/22
 *  Description: echarts options
 */

var app_chart_data_zoom = [{
    show: true,
    height: 20,
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

/**
 * 实验页面的图表
 * @param legend
 * @returns
 */
function buildExperimentChartOption(legend) {
    return {
        backgroundColor: '#ffffff',
        tooltip: {
            trigger: 'axis',
            backgroundColor: 'rgba(245, 245, 245, 0.8)',
            borderWidth: 1,
            borderColor: '#ccc',
            padding: 10,
            formatter: function (params) {
                params = params[0];
                var html = "<b>TIME: </b>" + params.value[0] + "<br/>";
                if (params.value.length > 1){
                    html += "<b>VALUE: </b>" + params.value[1] + "<br/>";
                }
                return html;
            },
            textStyle: {
                color: '#000'
            }
        },
        legend: {
            top: '0%',
            left: 'center',
            data: legend
        },
        grid: [{
            borderWidth: 0,
            top: 10,
            bottom: 35,
            left: 25,
            right: 25,
            textStyle: {
                color: "#fff"
            }
        }],
        calculable: true,
        animation: false,
        xAxis: [
            {
                type: 'time',
                boundaryGap : ['20%', '20%'],
                // X轴的直线指示器
                axisPointer: {
                    show: true,
                    type: 'line',
                    snap: true,
                    z: 100
                },
                // 分割线
                splitLine: {
                    show: true,
                    lineStyle: {
                        type: 'dashed'
                    }
                }
            }
        ],
        yAxis: [
            {
                type: 'value',
                scale: true,
                splitLine: {
                    show: true,
                    lineStyle: {
                        type: 'dashed'
                    }
                },
                boundaryGap: true,
                minInterval: 1,
                splitNumber: 3
            }
        ],
        series: {
            name: legend,
            type: 'line',
            smooth: true,

            /* 点，线，面的样式 */
            symbolSize: 1,
            symbol:'circle',
            itemStyle: {
                normal: {
                    color: '#4CCBFF'
                }
            },
            lineStyle: {
                normal: {
                    color: '#4CCBFF',
                    width: 1
                }
            },
            areaStyle: {
                normal: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                        offset: 0,
                        color: '#4CCBFF'
                    }, {
                        offset: 1,
                        color: '#4CADFF'
                    }])
                }
            },

            /* 标记的样式 */
            markArea: {
                silent: true,
                data: []
            },
            data: [{
                value : [new Date().Format("yyyy-MM-dd HH:mm:ss"), 0],
                mark: "init"
            }]
        }
    };
}

/**
 * 分析页面的图表
 * @param data
 * @param legend
 * @returns
 * */
function buildAnalysisChartOption(data, legend) {
    return {
        tooltip: {
            trigger: 'axis',
            backgroundColor: 'rgba(245, 245, 245, 0.8)',
            borderWidth: 1,
            borderColor: '#ccc',
            padding: 10,
            formatter: function (params) {
                params = params[0];
                var html = "<b>时间: </b>" + params.value[0] + "<br/>";
                if (params.value.length > 1){
                    html += "<b>" + legend + "</b>: " + params.value[1] + "<br/>";
                }
                if (params.name !== null){
                    html += "<b>标记: </b>"+ params.name;
                }
                return html;
            },
            textStyle: {
                color: '#000'
            }
        },
        grid: [{
            top: 5,
            bottom: 5,
            left: 50,
            right: 15
        }],
        dataZoom: {
            type: 'inside',
            realtime: true,
            start: 0,
            end: 100,
            xAxisIndex: [0]
        },
        calculable: true,
        animation: false,
        xAxis: [
            {
                type: 'time',
                splitLine: {
                    show: true,
                    lineStyle: {
                        type: 'dashed'
                    }
                },
                axisLabel: {
                    show: false
                },
                axisTick: {
                    show: false
                }
            }
        ],
        yAxis: [
            {
                type: 'value',
                scale: true,
                boundaryGap: false,
                splitLine: {
                    show: true,
                    lineStyle: {
                        type: 'dashed'
                    }
                },
                minInterval: 1,
                splitNumber: 3
            }
        ],
        series: [
            {
                name: legend,
                type: 'line',
                smooth: true,
                sampling: "average",
                hoverAnimation: false,

                /* 点，线，面的样式 */
                /*symbol:'circle',*/
                symbolSize: 5,

                lineStyle: {
                    normal: {
                        width: 1
                    }
                },

                /*areaStyle: {
                    normal: {
                        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{
                            offset: 0.1,
                            color: '#4CCBFF'
                        }, {
                            offset: 1,
                            color: '#4CADFF'
                        }])
                    }
                },*/

                /* 标记的样式 */
                /*markArea: {
                    silent: true,
                    itemStyle: {
                        normal: {
                            color: '#35b5eb',
                            opacity: 0.5
                        }
                    },
                    data: [[{
                        xAxis: analysisObject.timeline[0]
                    },{
                        xAxis: analysisObject.timeline[analysisObject.timeline.length - 1]
                    }]]
                },*/

                markLine: {
                    silent: true,
                    itemStyle: {
                        normal: {
                            color: '#35b5eb',
                            lineStyle: {
                                type: 'solid',
                                width: 1
                            }
                        }
                    },
                    symbol: 'none',
                    symbolSize: 0,
                    data: []
                },

                data: data
            }
        ]
    }
}

/**
 * 分析页面用户自定图表的设置
 * @param dInfo
 * @param legend
 */
function buildAnalysisDefineChartOption(dInfo, legend) {
    return {
        tooltip: {
            trigger: 'axis',
            axisPointer: {
                type: 'cross'
            }
        },
        grid: [{
            top: 30,
            bottom: 30,
            left: 50,
            right: 15
        }],
        xAxis: [
            {
                name: dInfo['info']['x'],
                nameLocation: 'start',
                type: 'value',
                min: dInfo['xMin'],
                max: dInfo['xMax'],
                splitLine: {
                    show: true,
                    lineStyle: {
                        type: 'dashed'
                    }
                }
            }
        ],
        yAxis: [
            {
                name: dInfo['info']['y'],
                type: 'value',
                min: dInfo['yMin'],
                max: dInfo['yMax'],
                splitLine: {
                    show: true,
                    lineStyle: {
                        type: 'dashed'
                    }
                }
            }
        ],
        series: [
            {
                id: 'a',
                name: legend,
                type: 'scatter',
                smooth: true,
                symbolSize: 10,
                data: dInfo['data']
            }
        ]
    }
}