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
        field: 'type',
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
    return type[value];
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
// --- new, edit and delete sensor
$new_sensor_form = $('#new-sensor-form');
$new_sensor_form.formValidation({
    framework: 'bootstrap',
    icon: {
        valid: 'glyphicon glyphicon-ok',
        invalid: 'glyphicon glyphicon-remove'
    },
    fields: {
        'new-sensor-name': {validators: {notEmpty: {message: '传感器名称不能为空'}}},
        'new-sensor-description': {validators: {notEmpty: {message: '传感器描述不能为空'}}},
        'new-sensor-code': {validators: {notEmpty: {message: '传感器编号不能为空'}}}
    }
}).on('success.form.fv', function (evt){
    evt.preventDefault();
    $.ajax({
        type: 'post',
        url: current_address + '/new/sensor',
        data: $(this).serialize() + "&sensorId=" + $('#is-new-sensor').attr('sensor-id'),
        success: function (id) {
            location.replace(location.href);
        },
        error: function (id) {
            message_info("操作传感器失败", 'error');
        }
    });
}).on('err.form.fv', function (evt) {
    message_info("提交失败", 'error');
});

$new_sensor_modal = $('#new-sensor-modal');
$new_sensor_modal.on('shown.bs.modal', function (event) {
    var button = $(event.relatedTarget);
    var action = button.attr('action');
    var modal = $(this);

    if (action == "new"){
        // new sensor
        modal.find('.modal-title').text('新增设备');
        $('#sensor-confirm-btn').text("新增设备");
        $('#is-new-sensor').attr('value', 1);

        $('#new-sensor-name').val("");
        $('#new-sensor-code').val("");
        $('#new-sensor-description').val("");
    } else if (action == "edit") {
        var selectRow = $('#table').bootstrapTable('getSelections');
        if (selectRow.length == 0){
            $new_sensor_modal.modal('hide');
            message_info("请先在表格中选中设备", "info");
            return;
        }

        // edit sensor
        modal.find('.modal-title').text('编辑设备');
        $('#sensor-confirm-btn').text("确认修改");
        $('#is-new-sensor').attr('value', 0).attr('sensor-id', selectRow[0].id);

        $('#new-sensor-name').val(selectRow[0]['name']);
        $('#new-sensor-code').val(selectRow[0]['code']);
        $('#new-sensor-description').val(selectRow[0]['description']);
    }
});

function deleteSensor() {
    var selectRow = $('#table').bootstrapTable('getSelections');
    if (selectRow.length == 0){
        $new_sensor_modal.modal('hide');
        message_info("请先在表格中选中设备", "info");
        return;
    }

    var id = selectRow[0]['id'];
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
                    url: current_address + '/delete/sensor?id=' + id,
                    success: function (id) {
                        location.replace(location.href);
                    },
                    error: function (id) {
                        message_info("操作失败", 'error');
                    }
                });
            }
        }
    });
}