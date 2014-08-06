
/**
	* @class edu.vt.vbi.patric.vis.graph.CWGraph
	* A graph visualization wrapper for Cytoscape Web.
	*
	* @extends edu.vt.vbi.patric.vis.Graph
	*
	* @history
	*
	* @constructor
	* Create a new CWGraph
	* @param {Object} config Configuration options
*/
edu.vt.vbi.patric.vis.graph.CWGraph = function(cfg) {
	
	edu.vt.vbi.patric.vis.Graph.call(this);
	
	var validCfg = {
		network		: { required: true, validTypes: [edu.vt.vbi.patric.vis.Network] }, 
		style			: { required: true, validTypes: [edu.vt.vbi.patric.vis.VisualStyle] }, 
		canvas		: { required: true, validTypes: ['string'] }, 
		swfPath		: { required: true, validTypes: ['string'] }, 
		flashPath : { required: true, validTypes: ['string'] } 
	};
	
	try {
	
		config(cfg, validCfg);
	
		this.network = cfg.network;
	
		this.style = cfg.style;
		
		this.canvas = getObj(cfg.canvas);
		
		this.vis = new org.cytoscapeweb.Visualization(
			cfg.canvas, 
			{ swfPath : cfg.swfPath, flashInstallerPath : cfg.flashPath }
		);

	} catch (err) {
		handleException(err);
	}
};
edu.vt.vbi.patric.vis.graph.CWGraph.inheritsFrom(edu.vt.vbi.patric.vis.Graph);
	
	
	
edu.vt.vbi.patric.vis.graph.CWGraph.prototype.draw = function() {
	var networkModel = this._getNetworkModel();
	this.vis.ready(this._onReady);
	var v = undefined;
	v = this.vis.draw({
		network: { dataSchema: this.network.getSchema(), data: networkModel }, 
		nodeLabelsVisible: 	 true, 
		nodeTooltipsEnabled: false, 
		edgeLabelsVisible: 	 false, 
		edgeTooltipsEnabled: false, 
		layout: { name: "ForceDirected", options: { mass: 5 } }, 
		visualStyle: { global: this.style.global, 
									 nodes : this.style.nodes, 
									 edges : this.style.edges }
	});
	if (typeof v == "undefined") {
		throw new edu.vt.vbi.patric.vis.GraphException(
			"Something went wrong while drawing the graph."
		);
	}
};

edu.vt.vbi.patric.vis.graph.CWGraph.prototype.addStyleMapper = function(fcnName, property, grp, fcn) {
	/* register the custom callback function */
	this.vis[fcnName] = fcn;
	/* add the mapper to the style property for the group */
	var style = this.vis.visualStyle();
	style[grp][property] = { customMapper: { functionName: fcnName } };
	this.vis.visualStyle(style);
};

edu.vt.vbi.patric.vis.graph.CWGraph.prototype.addListener = function(/*id, evt, [grp,] listener*/) {
	var id, evt, grp, listener;
	id  = arguments[0];
	evt = arguments[1];
	if (arguments.length == 3) {
		grp = "all";
		listener = arguments[2];
	} else if (arguments.length > 3) {
		grp = arguments[2];
		listener = arguments[3];
	} else {
		return;
	}
	this.parent.addListener.call(this, id, evt, grp, listener);
	
	if (evt == "mousedown" || evt == "mousewheel" || evt == "mousemove") {
		// CW can't handle these events so capture them in js
		hookEvent(this.canvas, evt, listener);
	} else {
		switch (grp) {
			case "nodes" : 
				this.vis.addListener(evt, "nodes", listener);
				break;
			case "edges" :
				this.vis.addListener(evt, "edges", listener);
				break;
			case "graph" : 
				this.vis.addListener(evt, listener);
				break;
			case "all" : 
				this.vis.addListener(evt, "nodes", listener);
				this.vis.addListener(evt, "edges", listener);
				break;
		}
	}
};

edu.vt.vbi.patric.vis.graph.CWGraph.prototype.select = function(grp, list) {
	this.vis.select(grp, list);
};

edu.vt.vbi.patric.vis.graph.CWGraph.prototype.zoom = function(scale) {
	if (arguments.length > 0) {
		var z = this.vis.zoom() + scale;
		if (z<0) z =0;
		this.vis.zoom(z);
	}
	return this.vis.zoom();
};

edu.vt.vbi.patric.vis.graph.CWGraph.prototype.node = function(id) {
	return this.vis.node(id);
};

edu.vt.vbi.patric.vis.graph.CWGraph.prototype.edge = function(id) {
	return this.vis.edge(id);
};

/**
	* Compiles a list of category properties that should be hidden, then 
	* applies a Cytoscape filter to hide them from view. This function does 
	* not change anything except the visibility of the visual graph elements.
*/
edu.vt.vbi.patric.vis.graph.CWGraph.prototype.filterByCategory = function() {
	
	// retrive list of all hidden categories in the network
	// saves us from having to loop through all categories for each node/edge
	var hideList = { nodes: [], edges: [] };
	var cats = this.network.getCategories();	// { name, group, idInGroup }
	for (var catId in cats) {
		if (cats.hasOwnProperty(catId)) {
			var entries = cats[catId].getEntries();
			for (var entryId in entries) {
				if (entries.hasOwnProperty(entryId) && 
						entries[entryId].hasOwnProperty("visible") && 
						!entries[entryId]["visible"]) {
					// add to hide list
					hideList[cats[catId].getGroup()].push({ 
						prop: cats[catId].getIdInGroup(), 
						val: entryId 
					});
				}
			}
		}
	}
	
	// filter the graph for edges with hidden categories
	this.vis.filter("edges", function(edge) {
		var vis = true;
		for (var j=0; j<hideList.edges.length; j++) {
			var prop = hideList.edges[j].prop;
			if (edge.data.hasOwnProperty(prop) && edge.data[prop] == hideList.edges[j].val) {
				vis = false;
				break;
			}
		}
		return vis;
	}, true);
	
	// filter the graph for nodes with hidden categories
	this.vis.filter("nodes", function(node) {
		var vis = true;
		for (var j=0; j<hideList.nodes.length; j++) {
			var prop = hideList.nodes[j].prop;
			if (node.data.hasOwnProperty(prop) && node.data[prop] == hideList.nodes[j].val) {
				vis = false;
				break;
			}
		}
		return vis;
	}, true);

	/*
	var edgeList = this.vis.edges();
	for (var i=0; i<edgeList.length; i++) {
		var edge = edgeList[i];
		var vis = true;
		for (var j=0; j<hideList.length; j++) {
			var prop = hideList[j].prop;
			if (edge.data.hasOwnProperty(prop) && edge.data[prop] == hideList[j].val) {
				vis = false;
				break;
			}
		}
		edge.visible = vis;
	}
	*/
};


edu.vt.vbi.patric.vis.graph.CWGraph.prototype._getNetworkModel = function() {
	var model = {nodes: [], edges: []};
	var nlist = this.network.getNodes();
	for (var i=0; i<nlist.length; i++) {
		model.nodes.push(nlist[i].getAllProps());
	}
	var elist = this.network.getEdges();
	for (var i=0; i<elist.length; i++) {
		model.edges.push(elist[i].getAllProps());
	}
	return model;
};


