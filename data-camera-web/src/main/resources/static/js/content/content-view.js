/**
 *  Belongs to data-camera-web
 *  Author: liye on 2018/1/19
 *  Description:
 */

function deleteMyView(evt){
    var contentId = evt.getAttribute('data');
    bootbox.confirm({
        title: "删除内容",
        message: "确认删除内容吗?",
        buttons: {
            cancel: { label: '<i class="fa fa-times"></i> 取消' },
            confirm: { label: '<i class="fa fa-check"></i> 确认删除' }
        },
        callback: function (result) {
            if (result){
                $.ajax({
                    type: 'get',
                    url: crud_address + '/content/delete?content-id=' + contentId,
                    success: function (response) {
                        if (response.code == "0000"){
                            window.location.href = current_address;
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