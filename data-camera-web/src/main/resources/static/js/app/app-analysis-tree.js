/**
 * Belongs to
 * Author: liye on 2017/12/22
 * Description:
 *  生成数据片段关系的树形图
 *  使用的后台数据(app, recorders)
 */
// -- node 数据
var rDataMap = {};

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
        var rid = recorder['id'];
        var name = recorder['name'];
        var parentId = recorder['parentId'];
        if (recorder['isUserGen'] == 0){
            if (!rData.hasOwnProperty(rid)){
                rData[rid] = [];
            }
            rData[rid].push({'key': rid, 'name': name});
            rDataMap[rid] = -1;
        } else {
            if (!rData.hasOwnProperty(parentId)){
                rData[parentId] = [];
            }
            rData[parentId].push({'key': rid, 'parent': parentId, 'name': name});
            rDataMap[rid] = parentId;
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
                        initialContentAlignment: go.Spot.Center,
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

// -- node选中事件
function nodeSelectionChanged(node) {
    if (node.isSelected) {
        var target = findParent(node.data.key) + '';
        console.log("Select tree-dom: " + node.data.key + ", target tree-dom: " + target);
        showRecorderContent(target);
        initChartDom(node.data.key);
    }
    
    function findParent(id) {
        if (rDataMap[id] == -1){
            return id;
        } else {
            return findParent(rDataMap[id]);
        }
    }
}

// -- 边栏菜单点击事件
$('.menu-content ul li').click(function (e) {
    var target = $(e.target).parent().attr('data');
    showRecorderContent(target);
    initChartDom(target);
});

/**
 * 展示当前实验片段的内容
 * @param target 选中的内容id
 */
function showRecorderContent(target){
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
