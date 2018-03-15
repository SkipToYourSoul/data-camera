/**
 *  Belongs to data-camera-web
 *  Author: liye on 2018/3/9
 *  Description:
 */

$(function(){
    // lazy loading设置
    var refreshTime = 0;
    var stemFilter = "全部";
    var tagFilter = "全部";
    var showContent = hotContent;

    function inFilter(stem, tags) {
        return !!((tags.indexOf(tagFilter) != -1 || tagFilter == "全部") && (stem == stemFilter || stemFilter == "全部"));
    }

    var myRefreshContent = new MiniRefresh({
        container: '#content-minirefresh',
        down: {
            isLock: true
        },
        up: {
            isAuto: true,
            callback: function() {
                refreshActionFunction();
            }
        }
    });

    function refreshActionFunction() {
        if (refreshTime < showContent.length){
            $('#content-data').append(appendContentData(showContent[refreshTime]));
            refreshTime ++;
            myRefreshContent.endUpLoading(false);
        } else {
            console.info("ending");
            myRefreshContent.endUpLoading(true);
        }
    }

    // 切换过滤器
    $('.content-filter ul li').click(function () {
        $(this).addClass('active').siblings().removeClass("active");
        stemFilter = $('.stem-filter li.active').text();
        tagFilter = $('.tag-filter li.active').text();
        showContent.forEach(function (content) {
            var $template = $('.content-id-' + content['id']);
            if ($template.length != 0) {
                if (inFilter(content['category'], content['tag'])) {
                    $template.attr('hidden',false);
                } else {
                    $template.attr('hidden',true);
                }
                console.log("load template" + content['id']);
            }
        });
        myRefreshContent.triggerUpLoading();
    });

    // 搜索热门内容
    $('#content-search-btn').click(function () {
        $.ajax({
            type: 'get',
            url: data_address + "/search-hot-content",
            data: {
                "search": $('#content-search-input').val()
            },
            success: function (response) {
                if (response.code == "0000") {
                    $('#content-data').empty();
                    refreshTime = 0;
                    stemFilter = $('.stem-filter li.active').text();
                    tagFilter = $('.tag-filter li.active').text();
                    showContent = response.data;
                    myRefreshContent.triggerUpLoading();
                } else if (response.code == "1111") {
                    message_info("加载数据失败，失败原因为：" + response.data, 'error');
                    myRefreshContent.endUpLoading(true);
                }
            },
            error: function (response) {
                message_info("数据请求被拒绝", 'error');
                myRefreshContent.endUpLoading(true);
            }
        });
    });

    function appendContentData(content) {
        console.log("init load template" + content['id']);
        var template = '';
        if (inFilter(content['category'], content['tag'])) {
            template = '<div class="col-sm-12 col-md-12 col-xs-12 hot-content-container content-id-' + content['id'] + '">';
        } else {
            template = '<div class="col-sm-12 col-md-12 col-xs-12 hot-content-container content-id-' + content['id'] + '" hidden="hidden">'
        }
        template += '<div class="share-view-container row" style="padding: 20px 30px">';
        // -- 图片
        if (content['img'] != null){
            template += '<div class="share-view-photo col-md-2"><img src="' + content['img'] + '" class="share-img center-block"/></div>';
        } else {
            template += '<div class="share-view-photo col-md-2"><img src="img/note.png" class="share-img center-block"/></div>'
        }
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