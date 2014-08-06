function getGraphData(id){

	Ext.Ajax.request({
		
	    url: "/patric-diseaseview/jsp/get_graph_data.jsp",
	    method: 'GET',
	    timeout: 600000,
	    params: {cId:id},
	    success: function(response, opts) {

	    	for(var i=0; i<multiple_lines; i++)
				if(Ext.getDom("multiple_lines_"+i) != null)
					Ext.getDom("multiple_lines_"+i).parentNode.style.cssText += " height:"+Ext.getDom("multiple_lines_"+i).style.height;
	    	
	    	drawGraph(response.responseText);	
	    	Ext.get("graph_container").unmask();

	    }
	});
	
}

function drawGraph(data){
		
	idvgraph.draw({ 
		canvas  : "patric_idvgraph_graph", 
		graph   : { id : Ext.getDom("cId").value,
			 edgeLabels : { show: false },
			 nodeLabels : { show: true },
			 format     : "graphml",
			 data       : data }
		});
		
	idvgraph.addListener("click", "nodes", function(event) {
		showGraphElementInfo(event);
	});
	
	idvgraph.addListener("click", "edges", function(event) {
		showGraphElementInfo(event);
	});
		
}

function showGraphElementInfo(event) {


   var x = Ext.getCmp("graph_center-panel").getPosition()[0];
   var y = Ext.getCmp("graph_center-panel").getPosition()[1];

   var txt = "";
   if (event.target.group == "nodes") { txt = idvgraph.getNodeInfo(event.target); }
   else if (event.target.group == "edges") { txt = idvgraph.getEdgeInfo(event.target); }

   if(tooltip.isVisible()){
       tooltip.setVisible(false);

   }

   tooltip.setVisible(true);
   tooltip.update(txt);

   var coords = idvgraph.getCoords(event.target);
   
   tooltip.setPosition(parseInt(x) + parseInt(coords.x), parseInt(y) + parseInt(coords.y));

   idvgraph.deselect(event.target);
	
}