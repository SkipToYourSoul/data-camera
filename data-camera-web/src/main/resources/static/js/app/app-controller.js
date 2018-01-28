/**
 *  Belongs to smart-sensor
 *  Author: liye on 2017/10/11
 *  Description: new/edit/delete app,exp
 */
// ----------------------------
// --- new, edit, delete app
// ----------------------------
/**
 * 新建和编辑场景
 */
$('#edit-app-form').formValidation({
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
    $.ajax({
        type: 'post',
        url: commonObject.editText == action?crud_address + '/app/update':crud_address + '/app/new',
        data: commonObject.editText == action?$(this).serialize() + "&app-id=" + app['id']:$(this).serialize(),
        success: function (response) {
            if (response.code == "0000"){
                window.location.href = current_address + "?id=" + response.data['id'];
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

$('#app-modal').on('show.bs.modal', function (event) {
    var button = $(event.relatedTarget);
    var todo = button.attr('todo');

    if (todo == "new"){
        $('#app-modal-title').html("新建场景");
        $('#app-confirm-btn').text(commonObject.newText);
        $('#app-name').val("");
        $('#app-desc').val("");
    } else if (todo == "edit"){
        $('#app-modal-title').html("编辑场景");
        $('#app-confirm-btn').text(commonObject.editText);
        $('#app-name').val(app['name']);
        $('#app-desc').val(app['description']);
    }
});

/**
 * 删除场景
 */
function deleteApp(){
    bootbox.confirm({
        title: "删除场景",
        message: "确认删除场景吗? 场景相关的数据也会被删除",
        buttons: {
            cancel: { label: '<i class="fa fa-times"></i> 取消' },
            confirm: { label: '<i class="fa fa-check"></i> 确认删除' }
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
                            commonObject.printExceptionMsg(response.data);
                        }
                    },
                    error: function () {
                        commonObject.printRejectMsg();
                    }
                });
            }
        }
    });
}

// ----------------------------
// --- new, edit, delete exp
// ----------------------------
/**
 * 新建和编辑传感器组
 */
$('#edit-exp-form').formValidation({
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
    $.ajax({
        type: 'post',
        url: commonObject.editText == action?crud_address + '/exp/update':crud_address + '/exp/new',
        data: commonObject.editText == action?$(this).serialize() + "&exp-id=" + expObject.currentExpId:$(this).serialize() + "&app-id=" + app['id'],
        success: function (response) {
            if (response.code == "0000"){
                window.location.href = current_address + "?id=" + app['id'];
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

$('#exp-modal').on('show.bs.modal', function (event) {
    var button = $(event.relatedTarget);
    var todo = button.attr('todo');
    var from = button.attr('from');
    expObject.currentExpId = button.attr('data');

    if (todo == "new"){
        $('#exp-modal-title').html("添加传感器组");
        $('#exp-confirm-btn').text(commonObject.newText);
        $('#exp-name').val("");
        $('#exp-desc').val("");
        $('#exp-select-group').attr("hidden", false);
    } else if (todo == "edit"){
        $('#exp-confirm-btn').text(commonObject.editText);
        if (from == "modify-desc"){
            $('#exp-modal-title').html("编辑传感器组");
            $('#exp-name-group').attr("hidden", false);
            $('#exp-desc-group').attr("hidden", false);
            $('#exp-select-group').attr("hidden", "hidden");
        } else if (from == "modify-sensor"){
            $('#exp-modal-title').html("添加新传感器");
            $('#exp-name-group').attr("hidden", "hidden");
            $('#exp-desc-group').attr("hidden", "hidden");
            $('#exp-select-group').attr("hidden", false);
        }
        $('#exp-name').val(experiments[expObject.currentExpId]['name']);
        $('#exp-desc').val(experiments[expObject.currentExpId]['description']);
    }
});

/**
 * 删除传感器组
 * @param evt
 */
function deleteExp(evt) {
    var expId = evt.getAttribute('data');
    bootbox.confirm({
        title: "删除传感器组",
        message: "确认删除传感器组吗? 传感器组相关的数据也会被删除",
        buttons: {
            cancel: { label: '<i class="fa fa-times"></i> 取消' },
            confirm: { label: '<i class="fa fa-check"></i> 确认删除' }
        },
        callback: function (result) {
            if (result){
                $.ajax({
                    type: 'get',
                    url: crud_address + '/exp/delete?exp-id=' + expId,
                    success: function (response) {
                        if (response.code == "0000"){
                            window.location.href = current_address + "?id=" + app['id'];
                        } else if (response.code == "1111") {
                            commonObject.printExceptionMsg(response.data);
                        }
                    },
                    error: function () {
                        commonObject.printRejectMsg();
                    }
                });
            }
        }
    });
}

/**
 * 删除轨迹
 * @param evt
 */
function deleteTrack(evt) {
    var trackId = evt.getAttribute('data');
    bootbox.confirm({
        title: "删除轨迹",
        message: "确认删除轨迹吗? 轨迹相关的数据也会被删除",
        buttons: {
            cancel: { label: '<i class="fa fa-times"></i> 取消' },
            confirm: { label: '<i class="fa fa-check"></i> 确认删除' }
        },
        callback: function (result) {
            if (result){
                $.ajax({
                    type: 'get',
                    url: crud_address + '/track/delete?track-id=' + trackId,
                    success: function (response) {
                        if (response.code == "0000"){
                            window.location.href = current_address + "?id=" + app['id'];
                        } else if (response.code == "1111") {
                            commonObject.printExceptionMsg(response.data);
                        }
                    },
                    error: function () {
                        commonObject.printRejectMsg();
                    }
                });
            }
        }
    });
}