/**
 *  Belongs to data-camera-web
 *  Author: liye on 2018/1/26
 *  Description:
 */

/**
 * 发布内容
 */
$('#content-form').formValidation({
    framework: 'bootstrap',
    icon: {
        valid: 'glyphicon glyphicon-ok',
        invalid: 'glyphicon glyphicon-remove'
    },
    fields: {
        'content-name': {validators: {notEmpty: {message: '不能为空'}}},
        'content-desc': {validators: {notEmpty: {message: '不能为空'}}},
        'content-tag': {validators: {notEmpty: {message: '不能为空'}}}
    }
}).on('success.form.fv', function (evt){
    evt.preventDefault();

    var selectedTags = $('#content-tag').find('option:selected');
    if (selectedTags.length == 0){
        message_info("请为分享的内容选择tag", "info");
    }
    var tags = [];
    for (var index = 0; index < selectedTags.length; index ++ ){
        tags.push(selectedTags[index]['text']);
    }

    $.ajax({
        type: 'post',
        url: crud_address + '/content/new',
        data: $(this).serialize() + "&recorder-id=" + analysisObject.currentRecorderId + "&tags=" + tags + "&share-img=" + analysisObject.contentImg,
        success: function (response) {
            if (response.code == "0000"){
                window.location.href = base_address + "/content?id=" + response.data['id'];
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