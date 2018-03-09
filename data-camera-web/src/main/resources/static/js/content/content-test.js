/**
 *  Belongs to data-camera-web
 *  Author: liye on 2018/3/9
 *  Description:
 */

$(function(){
    var refreshTime = 0;
    var myRefreshContent = new MiniRefresh({
        container: '#content-minirefresh',
        down: {
            isLock: true
        },
        up: {
            callback: function() {
                refreshAction();
            }
        }
    });

    function refreshAction() {
        if (refreshTime < hotContent.length){
            setTimeout(function () {
                $('#content-data').append(appendContentData(hotContent[refreshTime]));
                refreshTime ++;
                myRefreshContent.endUpLoading(false);
            }, 50);
        } else {
            myRefreshContent.endUpLoading(true);
        }
    }

    function appendContentData(content) {
        var template = '<div class="col-sm-12 col-md-12 col-xs-12">';
        template += '<div class="share-view-container row" style="padding: 20px 30px">';
        // -- 图片
        template += '<div class="share-view-photo col-md-2"><img src="' + content['img'] + '" class="share-img center-block"/></div>';
        template += '<div class="col-md-10">';
        template += '<div class="share-view-title">';
        // -- 标题
        template += '<div><a class="content-title" href="/camera/content?id=' + content['id'] + '">' + content['title'] + '</a> <span class="content-owner">发布作者：' + content['owner'] + '</span></div>';
        // -- 描述
        template += '<div class="content-des">' + content['description'] + '</div>';
        // -- 标签
        template += '<div class="content-tag">';
        if (content['tag'].length == 0){
            template += '<span class="label label-default content-tag-style">无标签</span>';
        } else {
            content['tag'].split(',').forEach(function (tag) {
                template += '<span class="label label-default content-tag-style">' + tag + '</span>';
            });
        }
        template += '</div>';
        // -- 分类
        template += '<div class="content-category">' + content['category'] + '</div>';
        template += '</div>';
        // -- 细节信息
        template += '<div class="share-view-info">';
        template += '<span class="share-view-user"> <span class="content-time">发布时间：' + parseTime(content['createTime']) + '</span></span>';
        template += '<span class="pull-right">';
        template += '<i class="fa fa-user-o content-icon"></i>&nbsp;<span class="content-num">' + content['view'] + '</span>&nbsp;&nbsp;';
        template += '<i class="fa fa-commenting-o content-icon"></i>&nbsp;<span class="content-num">' + content['comment'] + '</span>&nbsp;&nbsp;';
        template += '<i class="fa fa-heart-o content-icon"></i>&nbsp;<span class="content-num">' + content['like'] + '</span>';
        template += '</span>';
        template += '</div>';
        // -- ending
        template += '</div></div></div>';

        return template;
    }
});