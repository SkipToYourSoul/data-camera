/**
 *  Belongs to smart-sensor
 *  Author: liye on 2017/10/10
 *  Description:
 */

// --- table settings
$('#table').bootstrapTable({
    data: sensors,
    columns: [{
        field: 'radio',
        radio: 'true'
    }, {
        field: 'id',
        sortable: 'true',
        align: 'center',
        title: '设备ID'
    }, {
        field: 'name',
        sortable: 'true',
        align: 'center',
        title: '设备名'
    }, {
        field: 'code',
        sortable: 'true',
        align: 'center',
        title: '设备编号(唯一)'
    }, {
        field: 'sensorConfig',
        sortable: 'true',
        align: 'center',
        title: '设备类型',
        formatter: 'deviceTypeFormatter'
    }, {
        field: 'description',
        sortable: 'true',
        align: 'center',
        title: '设备描述'
    }, {
        field: 'createTime',
        sortable: 'true',
        align: 'center',
        title: '创建时间',
        formatter: 'timeFormatter'
    }, {
        field: 'appId',
        sortable: 'true',
        align: 'center',
        title: '所属应用',
        formatter: 'deviceBelongFormatter',
        cellStyle: 'deviceBelongStyle'
    }, {
        field: 'expId',
        sortable: 'true',
        align: 'center',
        title: '所属实验ID',
        formatter: 'deviceBelongFormatter',
        cellStyle: 'deviceBelongStyle'
    }]
});

function timeFormatter(value){
    return parseTime(value);
}

function deviceTypeFormatter(value) {
    var type = {
        1: "数值型传感器",
        2: "摄像头"
    };
    return type[value['type']];
}

function deviceBelongFormatter(value) {
    if (value == 0){
        return "该设备尚未被分配";
    } else
        return value;
}

function deviceBelongStyle(value, row, index){
    if (value == "该设备尚未被分配")
        return {
            classes: 'danger'
        };
    return {};
}

// --- sensor operations
var new_device_text = "确认创建";
var edit_device_text = "确认修改";
var $sensor_modal = $('#sensor-modal');
var $edit_sensor_form = $('#edit-sensor-form');
var current_sensor_id = -1;
// --- new, edit and delete sensor
$edit_sensor_form.formValidation({
    framework: 'bootstrap',
    icon: {
        valid: 'glyphicon glyphicon-ok',
        invalid: 'glyphicon glyphicon-remove'
    },
    fields: {
        'sensor-name': {validators: {notEmpty: {message: '设备名不能为空'}}},
        'sensor-code': {validators: {notEmpty: {message: '设备编号不能为空'}}},
        'sensor-desc': {validators: {notEmpty: {message: '设备描述不能为空'}}}
    }
}).on('success.form.fv', function (evt){
    evt.preventDefault();
    var action = $('#sensor-confirm-btn').text();
    var url = crud_address + '/sensor/update';
    var data = edit_device_text == action?$(this).serialize() + "&sensor-id=" + current_sensor_id:$(this).serialize();
    $.ajax({
        type: 'post',
        url: url,
        data: data,
        success: function (response) {
            if (response.code == "0000"){
                window.location.href = current_address;
            } else if (response.code == "1111") {
                message_info('操作无效: ' + response.data, "error");
            }
        },
        error: function (response) {
            message_info("操作失败，失败原因为：" + response, 'error');
        }
    });
}).on('err.form.fv', function (evt) {
    message_info("应用表单提交失败", 'error');
});

$sensor_modal.on('shown.bs.modal', function (event) {
    var button = $(event.relatedTarget);
    var action = button.attr('todo');

    if (action == "new"){
        // new sensor
        $('#sensor-confirm-btn').text(new_device_text);
        $('#sensor-name').val("");
        $('#sensor-code').val("").removeAttr("disabled");
        $('#sensor-desc').val("");
    } else if (action == "edit") {
        var selectRow = $('#table').bootstrapTable('getSelections');
        if (selectRow.length == 0){
            $sensor_modal.modal('hide');
            message_info("请先在表格中选中设备", "info");
            return;
        }

        // edit sensor
        $('#sensor-confirm-btn').text(edit_device_text);
        $('#sensor-name').val(selectRow[0]['name']);
        $('#sensor-code').val(selectRow[0]['code']).attr("disabled", "disabled");
        $('#sensor-desc').val(selectRow[0]['description']);
        current_sensor_id = selectRow[0].id;
    }
});

function deleteSensor() {
    var selectRow = $('#table').bootstrapTable('getSelections');
    if (selectRow.length == 0){
        $sensor_modal.modal('hide');
        message_info("请先在表格中选中设备", "info");
        return;
    }

    bootbox.confirm({
        title: "删除设备?",
        message: "确认删除设备吗? 设备相关数据也会被删除.",
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
                    url: crud_address + '/sensor/delete',
                    data: {
                        'sensor-id': selectRow[0]['id'],
                        'sensor-code': selectRow[0]['code']
                    },
                    success: function (response) {
                        if (response.code == "0000"){
                            window.location.href = current_address;
                        } else if (response.code == "1111") {
                            message_info('操作无效: ' + response.data, "error");
                        }
                    },
                    error: function (response) {
                        message_info("操作失败，失败原因为：" + response, 'error');
                    }
                });
            }
        }
    });
}