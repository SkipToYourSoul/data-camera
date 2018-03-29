/**
 *  Belongs to data-camera-web
 *  Author: liye on 2018/1/26
 *  Description:
 */

$(function () {
    // -- 初始化标签选择框
    $('.content-tag-dropdown').dropdown({
        limitCount: 5,
        multipleMode: 'label',
        input: '<input type="text" maxLength="20" placeholder="输入标签">',
        data: [
            {
                "id": 1, // value值
                "disabled": false, // 是否禁选
                "selected": false, // 是否选中
                "name": "温湿度" // 名称
            },
            {
                "id": 2,
                "disabled": false,
                "selected": false,
                "name": "光照"
            },
            {
                "id": 3,
                "disabled": false,
                "selected": false,
                "name": "摄像头"
            }
        ],
        choice: function (event, data) {
            console.log(arguments,this);
            console.log(event);
            console.log(data);
        }
    });

    // -- 初始化图片上传插件，暂时不用
    /*$("#file-upload-input").fileinput({
        language: 'zh',
        uploadUrl: data_address + "/file-upload?from=content", // server upload action
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
            analysisObject.contentImg = data.response.data;
        } else {
            commonObject.printExceptionMsg(data.response.data);
        }
    });*/

    // -- 单选框插件
    $('input').iCheck({
        checkboxClass: 'icheckbox_minimal-blue',
        radioClass: 'iradio_minimal-blue',
        increaseArea: '20%' // optional
    });

    // -- 分享内容表单提交
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
        var avatar = $('.avatar-group').find('input:radio:checked').attr("src");
        if (avatar == null) {
            avatar = "/camera/img/avatar/01.jpg";
        }

        $.ajax({
            type: 'post',
            url: crud_address + '/content/new',
            data: $(this).serialize() + "&recorder-id=" + getQueryString("rid") + "&tags=" + tags + "&share-img=" + /*analysisObject.contentImg*/avatar,
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
});