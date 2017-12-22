/**
 * Belongs to
 * Author: liye on 2017/12/22
 * Description: 生成数据片段关系的树形图
 */
function initTreeDom(){
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
            rData[rid] = [];
            rData[rid].push({'key': rid, 'name': name});
        } else {
            rData[parentId].push({'key': rid, 'parent': parentId, 'name': name});
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

function nodeSelectionChanged(node) {
    if (node.isSelected) {
        console.log(node.data.key);
    }
}
