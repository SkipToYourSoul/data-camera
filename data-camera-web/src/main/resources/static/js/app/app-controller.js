/**
 *  Belongs to smart-sensor
 *  Author: liye on 2017/10/11
 *  Description: new/edit/delete app,exp
 */
var new_device_text = "确认创建";
var edit_device_text = "确认修改";
var $edit_app_form = $('#edit-app-form');
var $app_modal = $('#app-modal');
var $edit_exp_form = $('#edit-exp-form');
var $exp_modal = $('#exp-modal');
var $exp_select = $('#exp-select');
var current_exp_id = 0;

// ----------------------------
// --- new, edit, delete app
// ----------------------------
$edit_app_form.formValidation({
    framework: 'bootstrap',
    icon: {
        valid: 'glyphicon glyphicon-ok',
        invalid: 'glyphicon glyphicon-remove'
    },
    fields: {
        'app-name': {validators: {notEmpty: {message: '场景名不能为空'}}},
        'app-desc': {validators: {notEmpty: {message: '场景描述不能为空'}}}
    }
}).on('success.form.fv', function (evt){
    evt.preventDefault();
    var action = $('#app-confirm-btn').text();
    var url = crud_address + '/app/update';
    var data = edit_device_text == action?$(this).serialize() + "&app-id=" + app['id']:$(this).serialize();
    $.ajax({
        type: 'post',
        url: url,
        data: data,
        success: function (response) {
            if (response.code == "0000"){
                window.location.href = current_address + "?id=" + response.data;
            } else if (response.code == "1111") {
                message_info('操作无效: ' + response.data, "error");
            }
        },
        error: function (response) {
            message_info("操作失败，失败原因为：" + response, 'error');
        }
    });
}).on('err.form.fv', function (evt) {
    message_info("场景表单提交失败", 'error');
});

$app_modal.on('show.bs.modal', function (event) {
    var button = $(event.relatedTarget);
    var todo = button.attr('todo');
    var modal = $(this);

    if (todo == "new"){
        $('#app-confirm-btn').text(new_device_text);
        $('#app-name').val("");
        $('#app-desc').val("");
    } else if (todo == "edit"){
        $('#app-confirm-btn').removeClass('btn-success').addClass('btn-warning').text(edit_device_text);
        $('#app-name').val(app['name']);
        $('#app-desc').val(app['description']);
    }
});

function deleteApp(){
    bootbox.confirm({
        title: "删除场景?",
        message: "确认删除场景吗? 场景相关的数据也会被删除.",
        buttons: {
            cancel: {
                label: '<i class="fa fa-times"></i> 取消'
            },
            confirm: {
                label: '<i class="fa fa-check"></i> 确认删除'
            }
        },
        callback: function (result) {
            if (result){
                $.ajax({
                    type: 'get',
                    url: crud_address + '/app/delete?app-id=' + app['id'],
                    success: function (response) {
                        if (response.code == "0000"){
                            location.replace(current_address);
                        } else if (response.code == "1111") {
                            message_info('操作无效: ' + response.data, "error");
                        }
                    },
                    error: function (id) {
                        message_info("删除场景失败", 'error');
                    }
                });
            }
        }
    });
}

// ----------------------------
// --- new, edit, delete exp
// ----------------------------
function initExpSelect(){
    var valueHtml = '<optgroup label="数值型传感器" data-max-options="2">';
    var videoHtml = '<optgroup label="摄像头" data-max-options="2">';
    for (var i in freeSensors){
        var sensor = freeSensors[i];
        var text = sensor['name'] + '(' + sensor['code'] + ')';
        if (sensor['sensorConfig']['type'] == 1){
            valueHtml += '<option value="' + sensor.id + '">' + text + '</option>';
        } else if (sensor['sensorConfig']['type'] == 2){
            videoHtml += '<option value="' + sensor.id + '">' + text + '</option>';
        }
    }
    for (var boundApp in boundSensors){
        if (boundSensors.hasOwnProperty(boundApp)) {
            for (var index in boundSensors[boundApp]){
                if (boundSensors[boundApp].hasOwnProperty(index)){
                    var sensor = boundSensors[boundApp][index];
                    var text = boundSensors[boundApp][index].name + "(绑定于：" + apps[boundApp]['name'] + ")";
                    if (sensor['sensorConfig']['type'] == 1){
                        valueHtml += '<option value="' + sensor.id + '" disabled="disabled">' + text + '</option>';
                    } else if (sensor['sensorConfig']['type'] == 2){
                        videoHtml += '<option value="' + sensor.id + '" disabled="disabled">' + text + '</option>';
                    }
                }
            }
        }
    }
    valueHtml += '</optgroup>';
    videoHtml += '</optgroup>';
    $exp_select.html(valueHtml + videoHtml);
    $exp_select.selectpicker('refresh');
}

$edit_exp_form.formValidation({
    framework: 'bootstrap',
    icon: {
        valid: 'glyphicon glyphicon-ok',
        invalid: 'glyphicon glyphicon-remove'
    },
    fields: {
        'exp-name': {validators: {notEmpty: {message: '传感器组名不能为空'}}},
        'exp-desc': {validators: {notEmpty: {message: '传感器组描述不能为空'}}}
    }
}).on('success.form.fv', function (evt){
    evt.preventDefault();
    var action = $('#exp-confirm-btn').text();
    var url = crud_address + '/exp/update';
    var data = $(this).serialize() + "&app-id=" + app['id'];
    data = edit_device_text == action?data + "&exp-id=" + current_exp_id:data;
    $.ajax({
        type: 'post',
        url: url,
        data: data,
        success: function (response) {
            if (response.code == "0000"){
                window.location.href = current_address + "?id=" + app['id'];
            } else if (response.code == "1111") {
                message_info('操作无效: ' + response.data, "error");
            }
        },
        error: function (response) {
            message_info("编辑传感器组失败", 'error');
        }
    });
}).on('err.form.fv', function (evt) {
    message_info("传感器组表单提交失败", 'error');
});

$exp_modal.on('show.bs.modal', function (event) {
    var button = $(event.relatedTarget);
    var todo = button.attr('todo');
    current_exp_id = button.attr('data');
    var modal = $(this);

    if (todo == "new"){
        $('#exp-confirm-btn').text(new_device_text);
        $('#exp-name').val("");
        $('#exp-desc').val("");
        initExpSelect();
    } else if (todo == "edit"){
        $('#exp-confirm-btn').removeClass('btn-success').addClass('btn-warning').text(edit_device_text);
        for (var exp_id in experiments){
            if ( experiments.hasOwnProperty(exp_id) && current_exp_id == '' + exp_id){
                $('#exp-name').val(experiments[exp_id]['name']);
                $('#exp-desc').val(experiments[exp_id]['description']);
                break;
            }
        }
        initExpSelect();
    }
});

function deleteExp(evt) {
    var exp_id = evt.getAttribute('data');
    bootbox.confirm({
        title: "删除传感器组?",
        message: "确认删除传感器组吗? 传感器组相关的数据也会被删除.",
        buttons: {
            cancel: {
                label: '<i class="fa fa-times"></i> 取消'
            },
            confirm: {
                label: '<i class="fa fa-check"></i> 确认删除'
            }
        },
        callback: function (result) {
            if (result){
                $.ajax({
                    type: 'get',
                    url: crud_address + '/exp/delete?exp-id=' + exp_id,
                    success: function (response) {
                        if (response.code == "0000"){
                            window.location.href = current_address + "?id=" + app['id'];
                        } else if (response.code == "1111") {
                            message_info('操作无效: ' + response.data, "error");
                        }
                    },
                    error: function (id) {
                        message_info("删除传感器组失败", 'error');
                    }
                });
            }
        }
    });
}

// ----------------------------
// --- delete track
// ----------------------------
function deleteTrack(evt) {
    var track_id = evt.getAttribute('data');
    bootbox.confirm({
        title: "删除轨迹?",
        message: "确认删除轨迹吗? 轨迹相关的数据也会被删除.",
        buttons: {
            cancel: {
                label: '<i class="fa fa-times"></i> 取消'
            },
            confirm: {
                label: '<i class="fa fa-check"></i> 确认删除'
            }
        },
        callback: function (result) {
            if (result){
                $.ajax({
                    type: 'get',
                    url: crud_address + '/track/delete?track-id=' + track_id,
                    success: function (id) {
                        window.location.href = current_address + "?id=" + app['id'];
                    },
                    error: function (id) {
                        message_info("删除轨迹失败", 'error');
                    }
                });
            }
        }
    });
}