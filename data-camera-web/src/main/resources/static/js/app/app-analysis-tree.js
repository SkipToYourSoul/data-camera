/**
 * Belongs to
 * Author: liye on 2017/12/22
 * Description:
 *  生成数据片段关系的树形图
 *  使用的后台数据(app, recorders)
 */

// -- 入口函数，初始化树形图
function initTreeDom(){
    if (!recorders.hasOwnProperty(app['id'])){
        console.log("Empty data recorders in this app.");
        return;
    }

    var $go = go.GraphObject.make;  // for conciseness in defining templates
    // Define a simple node template consisting of text followed by an expand/collapse button
    var nodeTemplate = $go(go.Node, "Horizontal",
        { selectionChanged: nodeSelectionChanged },  // this event handler is defined below
        $go(go.Panel, "Auto",
            $go(go.Shape, "RoundedRectangle", { fill: "#1F4963", stroke: null }),
            $go(go.TextBlock,
                { font: "bold 14px Helvetica, bold Arial, sans-serif",
                    stroke: "white", margin: 10 },
                new go.Binding("text", "name"))
        ),
        $go("TreeExpanderButton")
    );
    // Define a trivial link template with no arrowhead.
    var linkTemplate = $go(go.Link,
        { selectable: false },
        $go(go.Shape));  // the link shape

    // Construct node data
    var rData = {};
    var appRecorders = recorders[app['id']];
    for (var index = 0; index < appRecorders.length; index ++){
        var recorder = appRecorders[index];
        analysisObject.rDataMap[recorder['id']] = recorder['parentId'];
    }
    for (var index = 0; index < appRecorders.length; index ++){
        var recorder = appRecorders[index];
        var rid = recorder['id'];
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
    }

    // 画图
    for (var index = 0; index < appRecorders.length; index ++){
        var recorder = appRecorders[index];
        var id = recorder['id'];
        if (recorder['isUserGen'] == 0){
            var myDiagram =
                $go(go.Diagram, "tree-chart-" + id,
                    {
                        initialAutoScale: go.Diagram.UniformToFill,
                        initialContentAlignment: go.Spot.LeftCenter,
                        isReadOnly: true,  // do not allow users to modify or select in this view
                        allowSelect: true,
                        allowMove: false,
                        // define the layout for the diagram
                        layout: $go(go.TreeLayout, { nodeSpacing: 5, layerSpacing: 30 })
                    });
            myDiagram.nodeTemplate = nodeTemplate;
            myDiagram.linkTemplate = linkTemplate;
            myDiagram.model = $go(go.TreeModel, {
                isReadOnly: true,  // don't allow the user to delete or copy nodes
                // build up the tree in an Array of node data
                nodeDataArray: rData[id]
            });
        }
    }
}

function findParent(id) {
    if (analysisObject.rDataMap[id] == -1){
        return id;
    } else {
        return findParent(analysisObject.rDataMap[id]);
    }
}

// -- node选中事件
function nodeSelectionChanged(node) {
    if (node.isSelected && node.data.key != analysisObject.currentRecorderId) {
        var target = findParent(node.data.key) + '';
        console.log("Select tree-dom: " + node.data.key + ", target tree-dom: " + target);
        showRecorderContent(target, node.data.key);
    }
}

// -- 边栏菜单点击事件
$('.menu-content ul li').click(function (e) {
    var target = $(e.target).parent().attr('data');
    console.log("Click the menu: " + target);
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

    // 只展示当前选中的树形图
    var dom = $('.app-analysis-tree-group').find('.app-analysis-tree-dom');
    for (var index=0; index<dom.length; index++){
        var $dom = $(dom[index]);
        if ($dom.attr('data') == target){
            console.log("Show tree-dom:" + $dom.attr('data'));
            $dom.attr("hidden", false);
        } else {
            console.log("Hide tree-dom:" + $dom.attr('data'));
            $dom.attr("hidden", true);
        }
    }
    // 展示数据图表
    $('.app-analysis-chart-dom').attr("hidden", false);
    initRecorderContentDom(node);

    // 激活菜单
    var menu = $('.menu-content ul li');
    for (var index=0; index<menu.length; index++){
        var $menu = $(menu[index]);
        if ($menu.attr('data') == target){
            console.log("Active li:" + $menu.attr('data'));
            $menu.addClass('active');
        } else {
            console.log("InActive li:" + $menu.attr('data'));
            $menu.removeClass('active');
        }
    }
}
