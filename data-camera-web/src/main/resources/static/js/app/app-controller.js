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
        'app-name': {validators: {notEmpty: {message: '应用名不能为空'}}},
        'app-desc': {validators: {notEmpty: {message: '应用描述不能为空'}}}
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
        success: function (id) {
            window.location.href = current_address + "?id=" + id;
        },
        error: function (id) {
            message_info("操作应用失败，失败ID为：" + id, 'error');
        }
    });
}).on('err.form.fv', function (evt) {
    message_info("应用表单提交失败", 'error');
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
        title: "删除应用?",
        message: "确认删除应用吗? 应用相关的数据也会被删除.",
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
                    success: function (id) {
                        location.replace(current_address);
                    },
                    error: function (id) {
                        message_info("删除应用失败", 'error');
                    }
                });
            }
        }
    });
}

// ----------------------------
// --- new, edit, delete exp
// ----------------------------
function initExpSelect(free_sensors){
    var valueHtml = '<optgroup label="数值型传感器" data-max-options="2">';
    var videoHtml = '<optgroup label="摄像头" data-max-options="2">';
    for (var i in free_sensors){
        var sensor = free_sensors[i];
        var text = sensor['name'] + '(' + sensor['code'] + ')';
        if (sensor['sensorConfig']['type'] == 1){
            valueHtml += '<option value="' + sensor.id + '">' + text + '</option>';
        } else if (sensor['sensorConfig']['type'] == 2){
            videoHtml += '<option value="' + sensor.id + '">' + text + '</option>';
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
        'exp-name': {validators: {notEmpty: {message: '实验名不能为空'}}},
        'exp-desc': {validators: {notEmpty: {message: '实验描述不能为空'}}}
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
        success: function (id) {
            window.location.href = current_address + "?id=" + app['id'];
        },
        error: function (id) {
            message_info("操作应用失败，失败ID为：" + id, 'error');
        }
    });
}).on('err.form.fv', function (evt) {
    message_info("实验表单提交失败", 'error');
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
        initExpSelect(freeSensors);
    } else if (todo == "edit"){
        $('#exp-confirm-btn').removeClass('btn-success').addClass('btn-warning').text(edit_device_text);
        for (var index in experiments){
            if ( current_exp_id == '' + experiments[index].id){
                $('#exp-name').val(experiments[index]['name']);
                $('#exp-desc').val(experiments[index]['description']);
                break;
            }
        }
        initExpSelect(freeSensors);
    }
});

function deleteExp(evt) {
    var exp_id = evt.getAttribute('data');
    bootbox.confirm({
        title: "删除实验?",
        message: "确认删除实验吗? 实验相关的数据也会被删除.",
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
                    success: function (id) {
                        location.replace(current_address);
                    },
                    error: function (id) {
                        message_info("删除应用失败", 'error');
                    }
                });
            }
        }
    });
}