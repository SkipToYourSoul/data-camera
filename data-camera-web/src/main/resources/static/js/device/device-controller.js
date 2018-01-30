/**
 *  Belongs to smart-sensor
 *  Author: liye on 2017/10/10
 *  Description:
 */

// --- table settings
$('#table').bootstrapTable({
    data: sensors,
    rowStyle:function(row,index){  
        
    },  
    columns: [{
        field: 'radio',
        radio: 'true'
    }, /*{
        field: 'id',
        sortable: 'true',
        align: 'center',
        title: '设备ID'
    },*/{
        field: 'code',
        sortable: 'true',
        align: 'center',
        title: '设备编号(唯一)'
    }, {
        field: 'name',
        sortable: 'true',
        align: 'center',
        title: '设备名'
    }, {
        field: 'sensorConfig',
        sortable: 'true',
        align: 'center',
        title: '设备类型',
        formatter: 'deviceTypeFormatter'
    },{
        field: '-',
        sortable: 'true',
        align: 'center',
        title: '电量显示'
    }, /*{
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
    },*/ {
        field: 'appId',
        sortable: 'true',
        align: 'center',
        title: '所属场景',
        formatter: 'deviceBelongFormatter',
        cellStyle: 'deviceBelongStyle'
    }, {
        field: 'expId',
        sortable: 'true',
        align: 'center',
        title: '所属传感器组',
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
// --- new, edit and delete sensor
$('#edit-sensor-form').formValidation({
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
    var url = commonObject.editText == action?crud_address + '/sensor/update':crud_address + '/sensor/new';
    var data = $(this).serialize() + "&sensor-id=" + deviceObject.currentSensorId + "&device-img=" + deviceObject.deviceImg;
    $.ajax({
        type: 'post',
        url: url,
        data: data,
        success: function (response) {
            if (response.code == "0000"){
                window.location.href = current_address;
            } else if (response.code == "1111") {
                commonObject.printExceptionMsg(response.data);
            }
        },
        error: function (response) {
            commonObject.printRejectMsg();
        }
    });
}).on('err.form.fv', function (evt) {
    commonObject.printRejectMsg();
});

$('#sensor-modal').on('shown.bs.modal', function (event) {
    var button = $(event.relatedTarget);
    var action = button.attr('todo');

    if (action == "new"){
        // new sensor
        $('#sensor-confirm-btn').text(commonObject.newText);
        $('#sensor-name').val("");
        $('#sensor-code').val("").removeAttr("disabled");
        $('#sensor-desc').val("");
    } else if (action == "edit") {
        var selectRow = $('#table').bootstrapTable('getSelections');
        if (selectRow.length == 0){
            $('#sensor-modal').modal('hide');
            message_info("请先在表格中选中设备", "info");
            return;
        }

        // edit sensor
        $('#sensor-confirm-btn').text(commonObject.editText);
        $('#sensor-name').val(selectRow[0]['name']);
        $('#sensor-code').val(selectRow[0]['code']).attr("disabled", "disabled");
        $('#sensor-desc').val(selectRow[0]['description']);
        deviceObject.currentSensorId = selectRow[0].id;
    }
});

function deleteSensor() {
    var selectRow = $('#table').bootstrapTable('getSelections');
    if (selectRow.length == 0){
        $('#sensor-modal').modal('hide');
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

// -- 文件上传服务
$("#file-upload-input").fileinput({
    language: 'zh',
    uploadUrl: data_address + "/file-upload?from=device", // server upload action
    allowedFileExtensions: ['jpg', 'png'],
    uploadAsync: true,
    dropZoneEnabled: true,
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
        deviceObject.deviceImg = data.response.data;
    } else {
        commonObject.printExceptionMsg(data.response.data);
    }
});