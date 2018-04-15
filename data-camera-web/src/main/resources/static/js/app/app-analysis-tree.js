/**
 * Belongs to
 * Author: liye on 2017/12/22
 * Description:
 *  生成数据片段关系的树形图
 *  使用的后台数据(app, recorders)
 */

// -- 入口函数，初始化树形图
function initTreeDom(iFi){
    if (iFi == false) {
        // 不是第一次加载
        return;
    } else {
        analysisObject.iFi = false;
    }

    console.log("场景数据片段: ", recorders);
    if (isEmptyObject(recorders) || null == recorders){
        return;
    }

    var $go = go.GraphObject.make;  // for conciseness in defining templates
    // Define a simple node template consisting of text followed by an expand/collapse button
    var nodeTemplate = $go(go.Node, "Horizontal",
        { selectionChanged: nodeSelectionChanged },  // this event handler is defined below
        $go(go.Panel, "Auto",
            $go(go.Shape, "RoundedRectangle", { fill: "#35b5eb", stroke: null }),
            $go(go.TextBlock, { font: "bold 14px Microsoft YaHei, 微软雅黑, Microsoft YaHei, Hiragino Sans GB, sans-serif", stroke: "white", margin: 10 }, new go.Binding("text", "name"))
        ),
        $go("TreeExpanderButton")
    );
    // Define a trivial link template with no arrowhead.
    var linkTemplate = $go(go.Link,
        { selectable: false },
        $go(go.Shape));  // the link shape

    // Construct node data
    var rData = {};
    // 构造数据片段间的节点关系
    Object.keys(recorders).forEach(function (rid) {
        var recorder = recorders[rid];
        analysisObject.rDataMap[rid] = recorder['parentId'];
    });
    // 数据构造
    Object.keys(recorders).forEach(function (rid) {
        var recorder = recorders[rid];
        var name = recorder['name'];
        var parentId = recorder['parentId'];
        var ancestor = findParent(rid);
        if (recorder['isUserGen'] == 0){
            if (!rData.hasOwnProperty(rid)){
                rData[rid] = [];
            }
            rData[rid].push({'key': rid, 'name': name});
        } else {
            if (!rData.hasOwnProperty(ancestor)){
                rData[ancestor] = [];
            }
            rData[ancestor].push({'key': rid, 'parent': parentId, 'name': name});
        }
    });

    // 画图
    Object.keys(recorders).forEach(function (rid) {
        var recorder = recorders[rid];
        if (recorder['isUserGen'] == 0 && recorder['isRecorder'] == 0){
            var myDiagram =
                $go(go.Diagram, "tree-chart-" + rid,
                    {
                        initialAutoScale: go.Diagram.UniformToFill,
                        initialContentAlignment: go.Spot.LeftCenter,
                        isReadOnly: false,  // do not allow users to modify or select in this view
                        allowSelect: true,
                        allowMove: false,
                        allowVerticalScroll: rData[rid].length >= 5,
                        allowHorizontalScroll: rData[rid].length >= 5,
                        allowZoom: true,
                        // define the layout for the diagram
                        layout: $go(go.TreeLayout, { nodeSpacing: 5, layerSpacing: 30 })
                    });
            myDiagram.nodeTemplate = nodeTemplate;
            myDiagram.linkTemplate = linkTemplate;
            myDiagram.model = $go(go.TreeModel, {
                isReadOnly: true,  // don't allow the user to delete or copy nodes
                // build up the tree in an Array of node data
                nodeDataArray: rData[rid]
            });
        }
    });
}

// -- node选中事件
function nodeSelectionChanged(node) {
    if (node.isSelected && node.data.key != analysisObject.currentRecorderId) {
        var target = findParent(node.data.key) + '';
        showRecorderContent(target, node.data.key);
    }
}

// -- 边栏菜单点击事件
$('.menu-content ul li').click(function (e) {
    var target = $(e.target).parent().attr('data');
    showRecorderContent(target, target);
});

/**
 * 展示当前实验片段的内容
 * @param target 选中的内容parent id
 * @param node 选中的内容id
 */
function showRecorderContent(target, node){
    // 更新当前recorder id
    analysisObject.currentRecorderId = node;

    // 展示数据图表
    $('.app-analysis-chart-dom').attr("hidden", false);
    initRecorderContentDom(node);

    // 只展示当前选中的树形图
    $('.app-analysis-tree-group').find('.app-analysis-tree-dom').each(function () {
        if ($(this).attr('data') == target){
            $(this).attr("hidden", false);
        } else {
            $(this).attr("hidden", true);
        }
    });

    // 激活菜单
    $('.menu-content ul li').each(function () {
        if ($(this).attr('data') == target){
            $(this).addClass('active');
        } else {
            $(this).removeClass('active');
        }
    });
}
