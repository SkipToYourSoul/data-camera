/**
 *  Belongs to smart-sensor
 *  Author: liye on 2017/10/11
 *  Description:
 */
var new_app_text = "新建应用";
var edit_app_text = "确认修改";

// --- new, edit, delete app
$new_app_form = $('#new-app-form');
$new_app_form.formValidation({
    framework: 'bootstrap',
    icon: {
        valid: 'glyphicon glyphicon-ok',
        invalid: 'glyphicon glyphicon-remove'
    },
    fields: {
        'app-name': {validators: {notEmpty: {message: '应用名不能为空'}}},
        'app-description': {validators: {notEmpty: {message: '应用描述不能为空'}}}
    }
}).on('success.form.fv', function (evt){
    evt.preventDefault();
    var action = $('#app-confirm-btn').text();
    var url = crud_address + '/app/new';
    var data = $(this).serialize();
    if (edit_app_text == action){
        url = current_address + '/app/update';
        data = $(this).serialize() + "&app-id=" + app['id']
    }
    $.ajax({
        type: 'post',
        url: url,
        data: data,
        success: function (id) {
            window.location.href = current_address + "?id=" + id;
        },
        error: function (id) {
            message_info("操作应用失败", 'error');
        }
    });
}).on('err.form.fv', function (evt) {
    message_info("表单提交失败", 'error');
});

$app_modal = $('#app-modal');
$app_modal.on('shown.bs.modal', function (event) {
    var button = $(event.relatedTarget);
    var todo = button.attr('todo');
    var modal = $(this);

    if (todo == "new"){
        modal.find('.modal-title').text('新建应用');
        $('#app-confirm-btn').text(new_app_text);
        $('#app-name').val("");
        $('#app-desc').val("");
    } else if (todo == "edit"){
        modal.find('.modal-title').text('编辑应用');
        $('#app-confirm-btn').text("确认修改");
        $('#app-name').val(app['name']);
        $('#app-desc').val(app['description']);
    }
});

function deleteApp(){
    bootbox.confirm({
        title: "删除应用?",
        message: "确认删除应用吗? 应用相关的传感器相关数据也会被删除.",
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
                    url: crud_address + '/app/delete?id=' + app['id'],
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

