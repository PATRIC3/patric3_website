(function () {
	"use strict";

	if (!this.vbi) {
		this.vbi = {};
	}
	if (!this.vbi.patric) {
		this.vbi.patric = {};
	}
	if (!this.vbi.patric.hpi) {
		this.vbi.patric.hpi = {};
	}
	if (!this.vbi.patric.idv) {
		this.vbi.patric.hpi.idv = {};
	}


	vbi.patric.hpi.idv.IDVGraph = function (options) {
		this.graphid;
		this.cygraph;
		this.mtl;
		this.exportHandlerURL;
		
		this.options = { init: { caller: options.caller }, 
										 load: { swfPath						: options.swfPath, 
														 flashInstallerPath : options.flashInstallerPath }, 
										 draw: { canvas: document.body }
									 };
		if (options.exportHandler) { this.exportHandlerURL = options.exportHandler; }
		
		this.vis = {
			graph  : { styles : { backgroundColor: "#ffffff", 
														tooltipDelay   : 1 }, 
								 layout : "fd" 
			},
			nodes  : { styles : {
									labelFontName: 					"Verdana", 
									labelHorizontalAnchor: 	"left", 
									selectionGlowColor: 		"#FFFF00", 
									selectionGlowOpacity: 	0.5, 
									tooltipFont: 						"Verdana", 
									tooltipFontSize: 				12, 
									tooltipFontColor: 			"#000000", 
									tooltipBackgroundColor: "#b5ceed", 
									tooltipBorderColor: 		"#83ace2", 
									hoverOpacity: 					1.0, 
									selectionOpacity: 			1.0, 
									labelXOffset: {
										 defaultValue: -30, 
										 discreteMapper: {
											 attrName: "role", 
											 entries: [ 
												 {attrValue: "virulence factor", value: -2}, 
												 {attrValue: "gene", value: -2}, 
												 {attrValue: "collection:virulence factor", value: -2}, 
												 {attrValue: "collection:gene", value: -2} 
											 ] 
										 }
									 }, 
									labelFontColor: {
										 defaultValue: "#000000", 
										 discreteMapper: {
											 attrName: "role", 
											 entries: [ 
												 {attrValue: "virulence factor", value: "#333333"}, 
												 {attrValue: "gene", value: "#333333"}, 
												 {attrValue: "collection:virulence factor", value: "#333333"}, 
												 {attrValue: "collection:gene", value: "#333333"} 
											 ] 
										 }
									 }, 
									labelFontStyle: {
										 defaultValue: "normal", 
										 discreteMapper: {
											 attrName: "role", 
											 entries: [ 
												 {attrValue: "pathogen", value: "italic"}, 
												 {attrValue: "collection:pathogen", value: "italic"}, 
												 {attrValue: "gene", value: "italic"}, 
												 {attrValue: "virulence factor", value: "italic"}, 
											 ] 
										 }
									 }, 
									labelFontWeight: {
										 defaultValue: "normal", 
										 discreteMapper: {
											 attrName: "role", 
											 entries: [ 
												 {attrValue: "disease", value: "normal"}, 
												 {attrValue: "virulence factor", value: "normal"}, 
												 {attrValue: "gene", value: "normal"}, 
												 {attrValue: "collection:virulence factor", value: "normal"}, 
												 {attrValue: "collection:gene", value: "normal"} 
											 ] 
										 }
									 }, 
									labelFontSize: {
										 defaultValue: 9, 
										 discreteMapper: {
											 attrName: "role", 
											 entries: [ 
												 {attrValue: "disease", value: 10}, 
												 {attrValue: "pathogen", value: 9}, 
												 {attrValue: "collection:pathogen", value: 9}, 
												 {attrValue: "virulence factor", value: 9}, 
												 {attrValue: "gene", value: 9}, 
												 {attrValue: "collection:virulence factor", value: 9}, 
												 {attrValue: "collection:gene", value: 9} 
											 ] 
										 }
									 }, 
									opacity: {
										 defaultValue: 0.7, 
										 discreteMapper: {
											 attrName: "role", 
											 entries: [ 
												 {attrValue: "virulence factor", value: 0.7}, 
												 {attrValue: "gene", value: 0.7}, 
												 {attrValue: "collection:virulence factor", value: 0.7}, 
												 {attrValue: "collection:gene", value: 0.7} 
											 ] 
										 }
									 }, 
									size: { 
										 defaultValue: 20, 
										 discreteMapper: {
											 attrName: "role", 
											 entries: [ 
												 {attrValue: "disease", value: 20}, 
												 {attrValue: "pathogen", value: 20}, 
												 {attrValue: "collection:pathogen", value: 20}, 
												 {attrValue: "gene", value: 12}, 
												 {attrValue: "collection:gene", value: 9}, 
												 {attrValue: "virulence factor", value: 12}, 
												 {attrValue: "collection:virulence factor", value: 9} 
											 ] 
										 } 
									}, 
									shape: { 
										 defaultValue: "ELLIPSE", 
										 discreteMapper: {
											 attrName: "role", 
											 entries: [ 
												 {attrValue: "disease", value: "RECTANGLE"}, 
												 {attrValue: "pathogen", value: "TRIANGLE"}, 
												 {attrValue: "collection:pathogen", value: "TRIANGLE"}, 
												 {attrValue: "gene", value: "ELLIPSE"}, 
												 {attrValue: "collection:gene", value: "ELLIPSE"}, 
												 {attrValue: "virulence factor", value: "ELLIPSE"}, 
												 {attrValue: "collection:virulence factor", value: "ELLIPSE"} 
											 ] 
										 } 
									 }, 
									color: { 
										 defaultValue: "#0080ff", 
										 discreteMapper: {
											 attrName: "role", 
											 entries: [ 
												 {attrValue: "disease", value: "#0080ff"}, 
												 {attrValue: "pathogen", value: "#cc66ff"}, 
												 {attrValue: "collection:pathogen", value: "#cc66ff"}, 
												 {attrValue: "gene", value: "#008000"}, 
												 {attrValue: "collection:gene", value: "#008000"}, 
												 {attrValue: "virulence factor", value: "#FF5E5B"}, 
												 {attrValue: "collection:virulence factor", value: "#FF5E5B"} 
											 ] 
										 } 
									 }, 
									borderColor: { 
										 defaultValue: "#666666", 
										 discreteMapper: {
											 attrName: "role", 
											 entries: [ 
												 {attrValue: "collection:pathogen", value: "#cc66ff"}, 
												 {attrValue: "collection:gene", value: "#008000"}, 
												 {attrValue: "collection:virulence factor", value: "#FF5E5B"} 
											 ] 
										 } 
									 }, 
									borderWidth: { 
										 defaultValue: 1, 
										 discreteMapper: {
											 attrName: "role", 
											 entries: [ 
												 {attrValue: "collection:pathogen", value: 10}, 
												 {attrValue: "collection:gene", value: 10}, 
												 {attrValue: "collection:virulence factor", value: 10} 
											 ] 
										 } 
									 }, 
									label: { passthroughMapper: { attrName: "label" } }, 
									tooltipText: { passthroughMapper: { attrName: "label" } }
							 }, 
							 labels:			 true, 
							 tooltips:		 false, 
							 atts:				 {}, 
							 selectByAtts: [], 
							 hoverAtts:		 [] 
			},
			edges  : { styles : { 
									labelFontName: 					"Verdana", 
									labelFontWeight: 				"normal", 
									labelFontStyle: 				"italic", 
									labelFontSize: 					19, 
									labelFontColor: 				"#000000", 
									labelGlowColor: 				"#ffffff", 
									labelGlowOpacity: 			0, 
									labelGlowBlur: 					0, 
									labelGlowStrength: 			0, 
									selectionGlowColor: 		"#ffff00", 
									selectionGlowOpacity: 	0.7, 
									hoverOpacity: 					1.0, 
									tooltipFont: 						"Verdana", 
									tooltipFontSize: 				12, 
									tooltipFontColor: 			"#000000", 
									tooltipBackgroundColor: "#b5ceed", 
									tooltipBorderColor: 		"#83ace2", 
									width: {
										 defaultValue: 2, 
										 discreteMapper: {
											 attrName: "role", 
											 entries: [ 
												 {attrValue: "hierarchy:disease", value: 2}, 
												 {attrValue: "hierarchy:taxonomy", value: 2}, 
												 {attrValue: "association:pathogen-disease", value: 2} 
											 ] 
										 }
									 }, 
									opacity: {
										 defaultValue: 0.7, 
										 discreteMapper: {
											 attrName: "role", 
											 entries: [ 
												 {attrValue: "hierarchy:disease", value: 0.4}, 
												 {attrValue: "hierarchy:taxonomy", value: 0.4}, 
												 {attrValue: "association:pathogen-disease", value: 0.7} 
											 ] 
										 }
									 }, 
									color: {
										 defaultValue: "#f0f0f0", 
										 discreteMapper: {
											 attrName: "role", 
											 entries: [ 
												 {attrValue: "hierarchy:disease", value: "#408bff"}, 
												 {attrValue: "hierarchy:taxonomy", value: "#DB94FF"}, 
												 {attrValue: "association:pathogen-disease", value: "#d0d0d0"}, 
												 {attrValue: "association:gene-disease", value: "#c3ffc3"}, 
												 {attrValue: "association:pathogen-virulence factor", value: "#FFACA3"}, 
											 ] 
										 }
									 }, 
									sourceArrowColor: { 
										 defaultValue: "#f0f0f0", 
										 discreteMapper: {
											 attrName: "role", 
											 entries: [ 
												 {attrValue: "hierarchy:disease", value: "#0080ff"}, 
												 {attrValue: "hierarchy:taxonomy", value: "#DB94FF"}
											 ] 
										 } 
									 }, 
									targetArrowShape: { 
										 defaultValue: "NONE", 
										 discreteMapper: {
											 attrName: "role", 
											 entries: [ 
												 {attrValue: "hierarchy:disease", value: "ARROW"}, 
												 {attrValue: "hierarchy:taxonomy", value: "ARROW"}
											 ] 
										 } 
									 }, 
									targetArrowColor: { 
										 defaultValue: "#f0f0f0", 
										 discreteMapper: {
											 attrName: "role", 
											 entries: [ 
												 {attrValue: "hierarchy:disease", value: "#0080ff"}, 
												 {attrValue: "hierarchy:taxonomy", value: "#DB94FF"} 
											 ] 
										 } 
									 }, 
									label: { passthroughMapper: { attrName: "label" } }, 
									tooltipText: { passthroughMapper: { attrName: "label" } }
							 },
							 labels:			 false, 
							 tooltips: 		 true, 
							 atts:				 {}, 
							 selectByAtts: [], 
							 hoverAtts:		 [] 
			}
		};
		
		this.layouts = { defaultValue: "fd", 
										 fd: { name		: "ForceDirected",
										 			 options: { mass: 3, tension: 0.1, minDistance: 20 } 
										 }, 
										 mt: {} 
									 };
		
		this.taxRanks = { 0: "superkingdom", 
											1: "kingdom", 
											2: "subkingdom", 
											3: "superphylum", 
											4: "phylum", 
											5: "subphylum", 
											6: "superclass", 
											7: "class", 
											8: "subclass", 
											9: "superorder", 
											10: "order", 
											11: "suborder", 
											12: "superfamily", 
											13: "family", 
											14: "subfamily", 
											15: "supergenus", 
											16: "genus", 
											17: "subgenus", 
											18: "superspecies", 
											19: "species", 
											20: "subspecies", 
											21: "superstrain", 
											22: "strain", 
											23: "substrain", 
											24: "isolate", 
											25: "biovar", 
											10000: "no rank" };

		/** 
		set the draw options and render the graph.
		
			canvas : string : the DOM node that will hold the graph. defaults to 
												the document body.

			graph  : object : the graph data plus config options for initial load.
			
					id       		: string : an id for the graph. required.

					data     		: mixed  : the actual graph data (nodes and edges). 
																 required.
														 
					format   		: string : the format of the data. currently supports 
																 graphml or json. optional; defaults to 
																 graphml.
																 
					edgeLabels  : object : show   : boolean : show edge labels. default is 
																										false.
																 attVal : string  : edge attribute used as the 
																 										label. default is 'label'. 
																 										this is optional.
																 										
					nodeLabels  : object : show   : boolean : show node labels. default is 
																										false.
																 attVal : string  : node attribute used as the 
																 										label. default is 'label'. 
																 										this is optional.

		**/
		this.draw = function (options) {
			this.graphid = options.graph.id;
			
			if (options.canvas) { this.options.draw.canvas = options.canvas; }
			
			// initialize the CytoscapeWeb vis, using load params passed in via 
			// the constructor.
			var g = new org.cytoscapeweb.Visualization(this.options.draw.canvas, 
																								 this.options.load);
			
			// allow UI control over initial label visibility and label attribute
			if (options.graph.edgeLabels) {
				this.vis.edges.labels = options.graph.edgeLabels.show; 
				if (options.graph.edgeLabels.attVal) { 
					this.vis.edges.styles.label = { 
							passthroughMapper: { attrName: options.graph.edgeLabels.attVal } 
					};
					this.vis.edges.styles.tooltipText = { 
							passthroughMapper: { attrName: options.graph.edgeLabels.attVal } 
					};
				}
				this.vis.edges.tooltips = !this.vis.edges.labels;
			}
			if (options.graph.nodeLabels) {
				this.vis.nodes.labels = options.graph.nodeLabels.show; 
				if (options.graph.nodeLabels.attVal) { 
					this.vis.nodes.styles.label = { 
							passthroughMapper: { attrName: options.graph.nodeLabels.attVal } 
					};
					this.vis.nodes.styles.tooltipText = { 
							passthroughMapper: { attrName: options.graph.nodeLabels.attVal } 
					};
				}
				this.vis.nodes.tooltips = !this.vis.nodes.labels;
			}

			if (options.graph.mtl) {
				this.mtl = options.graph.mtl;
			}

			// render the graph to canvas
			g.draw({ network: options.graph.data, 
							 layout: this.layouts[this.vis.graph.layout], 
							 visualStyle: { global: this.vis.graph.styles, 
															nodes:  this.vis.nodes.styles, 
															edges:  this.vis.edges.styles }, 
							 nodeLabelsVisible: false, 
							 edgeLabelsVisible: false, 
							 nodeTooltipsEnabled: this.vis.nodes.tooltips, 
							 edgeTooltipsEnabled: this.vis.edges.tooltips 
			});

			// set up actions to fire once the vis is finished rendering
			g.ready(function() { 
				eval("idvgraph.ready();");
			});

			this.cygraph = g;
		};
		
		this.ready = function () {
		
			if (this.cygraph.nodes().length < 1) {
				var msg = "<div class=\"app-error-msg\">No data available.<\/div>";
				document.getElementById(this.options.draw.canvas).innerHTML = msg;
				return;
			}
			
			this.fillNodeAtts();
			this.fillEdgeAtts();
			
			// calculate the mirrored-tree layout
			if (!this.mtl) {
				this.mtl = new vbi.patric.hpi.idv.MirroredTreeLayout({ 
											graph: this, 
											trees: { prop: "role", 
															 valA: ["disease"], 
															 valB: ["pathogen", "collection:pathogen"] }, 
											canvas: this.options.draw.canvas 
				});
				this.mtl.extract();
				this.mtl.assign();
				this.mtl.calculate();
			}
			this.layouts.mt = this.mtl.getLayoutCW();
			this.vis.graph.layout = "mt";
			this.cygraph.layout(this.layouts.mt);
			
			// CM: zoom graph to fit canvas
			this.cygraph.addContextMenuItem(
					"Zoom to Fit", 
					"none", 
					function(event) { eval("idvgraph.zoomToFit()"); });

			// CM: toggle node labels
			this.cygraph.addContextMenuItem(
					"Show/Hide Node Labels", 
					"none", 
					function(event) { eval("idvgraph.toggleNodeLabels()"); });

			// CM: apply mirrored trees layout
			this.cygraph.addContextMenuItem(
					"Apply Mirrored Tree Layout", 
					"none", 
					function(event) { eval("idvgraph.chLayout(\"mt\")"); });
			// CM: apply force-directed layout
			this.cygraph.addContextMenuItem(
					"Apply Force-Directed Layout", 
					"none", 
					function(event) { eval("idvgraph.chLayout(\"fd\")"); });
			
			// CM: remove any filters
			this.cygraph.addContextMenuItem(
					"Show All Genes", 
					"none", 
					function(event) { eval("idvgraph.filterDecorators()"); });
			// CM: filter host genes
			this.cygraph.addContextMenuItem(
					"Hide Host Genes", 
					"none", 
					function(event) {
						eval("idvgraph.filterDecorators(\"hg\")");
					}
			);
			// CM: filter VFs
			this.cygraph.addContextMenuItem(
					"Hide Virulence Factors", 
					"none", 
					function(event) {
						eval("idvgraph.filterDecorators(\"vf\")");
					}
			);
			// CM: filter all genes
			this.cygraph.addContextMenuItem(
					"Hide All Genes", 
					"none", 
					function(event) {
						eval("idvgraph.filterDecorators(\"all\")");
					}
			);

			// CM: export to pdf
			this.cygraph.addContextMenuItem(
					"Export Graph to PDF", 
					"none", 
					function(event) {
						eval("idvgraph.exportAs({format: \"pdf\", w: 1920, h: 1080})");
					}
			);
			// CM: export to png
			this.cygraph.addContextMenuItem(
					"Export Graph to PNG", 
					"none", 
					function(event) {
						eval("idvgraph.exportAs({format: \"png\", w: 1920, h: 1080})");
					}
			);
			// CM: export to xgmml
			this.cygraph.addContextMenuItem(
					"Export Graph to XGMML", 
					"none", 
					function(event) {
						eval("idvgraph.exportAs({format: \"xgmml\"})");
					}
			);

			// CM: FAQ on graph
			this.cygraph.addContextMenuItem(
					"FAQ", 
					"none", 
					function(event) {
						eval("idvgraph.openExt({ url: \"http://www.patricbrc.org/\", target: \"_blank\" })");
					}
			);

			// CM: show info on the active node
			this.cygraph.addContextMenuItem(
					"Show Info", 
					"nodes", 
					function(event) { showGraphElementInfo(event); });

			// CM: show info on the active edge
			this.cygraph.addContextMenuItem(
					"Show Info", 
					"edges", 
					function(event) { showGraphElementInfo(event); });
			
			// visual effects on node and edge rollover/rollout
			this.enableMOs();
			
			// clicking in the background deselects all selected elements
			this.cygraph.addListener("click", function(event) {
				eval("idvgraph.deselect()"); 
			});
			
			document.getElementById(this.options.draw.canvas).onmousedown = idvgraph.mdcap;
			document.getElementById(this.options.draw.canvas).onmouseup = idvgraph.mucap;
			
			// enable grab to pan mode
			this.cygraph.panEnabled(true);
			// turn off custom cursors
			this.cygraph.customCursorsEnabled(false);
			
			// enable scroll-to-zoom with the mousewheel
			hookEvent(this.options.draw.canvas, "mousewheel", function(evt) {
				var d = getWheelDelta(evt);
				var z = d/80;
				eval("idvgraph.zoom(z)");
				//idvgraph.zoomAndPan(z, cursorX, cursorY);
				cancelEvent(evt);
			});
			
			// turn off control widget
			this.cygraph.panZoomControlVisible(false);
			
			setTimeout("idvgraph.label(\"nodes\", true)", 500);
		};
		
		this.enableMOs = function() {
			// visual effect on node rollover/rollout
			this.cygraph.addListener("mouseover", "nodes", function(event) {
				eval("idvgraph.selectNeighborhood(event.target)"); 
			});
			this.cygraph.addListener("mouseout", "nodes", function(event) {
				eval("idvgraph.deselect()"); 
			});
			this.cygraph.addListener("mouseover", "edges", function(event) {
				var sel = { nodes: [event.target.data.source, event.target.data.target], 
										edges: [event.target.data.id] };
				eval("idvgraph.select(sel)"); 
			});
			this.cygraph.addListener("mouseout", "edges", function(event) {
				var sel = { nodes: [event.target.data.source, event.target.data.target], 
										edges: [event.target.data.id] };
				eval("idvgraph.deselect(sel)"); 
			});
		};
		
		this.disableMOs = function() {
			// visual effect on node rollover/rollout
			this.cygraph.removeListener("mouseover", "nodes", function(event) {
				eval("idvgraph.selectNeighborhood(event.target)"); 
			});
			this.cygraph.removeListener("mouseout", "nodes", function(event) {
				eval("idvgraph.deselect()"); 
			});
			this.cygraph.removeListener("mouseover", "edges", function(event) {
				var sel = { nodes: [event.target.data.source, event.target.data.target], 
										edges: [event.target.data.id] };
				eval("idvgraph.select(sel)"); 
			});
			this.cygraph.removeListener("mouseout", "edges", function(event) {
				var sel = { nodes: [event.target.data.source, event.target.data.target], 
										edges: [event.target.data.id] };
				eval("idvgraph.deselect(sel)"); 
			});
		};
		
		
		this.mdcap = function (evt) {
			eval("idvgraph.deselect();idvgraph.disableMOs();");
		};
		
		this.mucap = function (evt) {
			eval("idvgraph.deselect();idvgraph.enableMOs();");
		};
		
		this.addListener = function(eventType, group, fcn) {
			if (group == "") {
				this.cygraph.addListener(eventType, fcn);
			} else {
				this.cygraph.addListener(eventType, group, fcn);
			}
		};
		
		this.zoomToFit = function() {
			this.cygraph.zoomToFit();
		};
		
		/** 
			* implements a zoom of the network in or out by increment.
		*/
		this.zoom = function (incr) {
			//alert(incr);
			var newZoom = this.cygraph.zoom() + incr;
			if (newZoom < 0) { newZoom = 0; }
			//else if (newZoom > 1) { newZoom = 1; }
			this.cygraph.zoom(newZoom);
		};
		
		/** 
			* implements a pure zoom of the network to newZoom.
		*/
		this.zoomTo = function (newZoom) {
			if (newZoom < 0) { newZoom = 0; }
			else if (newZoom > 1) { newZoom = 1; }
			this.cygraph.zoom(newZoom);
		};
		
		/** 
			* pointer-sensitive method that couples network zoom with network pan. 
			* in addition to zooming, pans the network so the x,y coordinates of the 
			* pointer move closer to the center of the canvas.
			*
			* currently disabled.
		*/
		this.zoomAndPan = function (incr, x, y) {
			// if the zoom is already at limit, do nothing
			if (this.cygraph.zoom() == 0 || this.cygraph.zoom() == 1) { return; }

			var newZoom = this.cygraph.zoom() + incr;
			if (newZoom < 0) { newZoom = 0; }
			else if (newZoom > 1) { newZoom = 1; }
			this.zoomTo(newZoom);
			
			var dims = this.getCanvasDims();
			var mX = dims.w/10;
			if (mX < dims.w/10) { mX = 1; }
			var mY = dims.h/10;
			if (mY < dims.h/10) { mY = 1; }
			
			var pan = { x: parseInt((dims.center.x - x)/mX), 
									y: parseInt((dims.center.y - y)/mY) };
			this.pan(pan.x, pan.y);
		};
		
		/**
			* pans the network by panX, panY.
		*/
		this.pan = function (panX, panY) {
			this.cygraph.panBy(panX, panY);
		};
		
		/**
			* returns the dimensions and center (absolute coordinates) of the graph 
			* canvas, relative to the enclosing document (not the enclosing div).
		*/
		this.getCanvasDims = function() {
			var h = parseInt(document.getElementById(this.options.draw.canvas).offsetHeight);
			var w = parseInt(document.getElementById(this.options.draw.canvas).offsetWidth);
			var top = parseInt(document.getElementById(this.options.draw.canvas).offsetTop);
			var left = parseInt(document.getElementById(this.options.draw.canvas).offsetLeft);
			var x = parseInt(left + (w/2));
			var y = parseInt(top + (h/2));
			return { center: { x: x, y: y }, 
							 h: h, 
							 w: w, 
							 top: top, 
							 left: left };
		};
		
		this.getNode = function (id) {
			return this.cygraph.node(id);
		};
		
		this.getNodeID = function (node) {
			return node.data.id;
		};
		
		this.getNodeSize = function (node) {
			return node.size;
		};
		
		this.getEdge = function (id) {
			return this.cygraph.edge(id);
		};
		
		this.getEdgeID = function (edge) {
			return edge.data.id;
		};
		
		this.getSourceNodeID = function (edge) {
			return edge.data.source;
		};
		
		this.getTargetNodeID = function (edge) {
			return edge.data.target;
		};
		
		this.getEdgeWeight = function (edge) {
			var wt = 1;
			if (edge.data && edge.data.weight) { wt = edge.data.weight; }
			return wt;
		};
		
		this.nodecount = function () {
			return this.cygraph.nodes().length;
		};
		
		this.edgecount = function () {
			return this.cygraph.edges().length;
		};
		
		this.getNeighborIDs = function (nid) {
		var nbrids = [];
			var list = this.cygraph.edges();
			for (var i in list) {
				// loop thru this graph's edges
				var edge = list[i];
				if (edge && edge.data) {
					if (edge.data.source == nid) {
						nbrids.push(edge.data.target);
					} else if (edge.data.target == nid) {
						nbrids.push(edge.data.source);
					}
				}
			}
			return nbrids;
		};
		
		/**
			* returns an object containing the neighbors of nid:
			* nbrs = { nbrID: {edge: edgeID, wt: edgeWeight}, ... }
		**/
		this.getNeighbors = function (nid) {
		var nbrs = {};
			var list = this.cygraph.edges();
			for (var i=0; i<list.length; i++) {
				// loop thru this graph's edges
				var edge = list[i];
				if (edge && edge.data) {
					if (edge.data.source == nid) {
						nbrs[edge.data.target] = { edge: edge.data.id, 
																			 wt: this.getEdgeWeight(edge) };
					} else if (edge.data.target == nid) {
						nbrs[edge.data.source] = { edge: edge.data.id, 
																			 wt: this.getEdgeWeight(edge) };
					}
				}
			}
			return nbrs;
		};
		
		this.deselect = function (sel) {
			if (!sel) {
				this.cygraph.deselect();
			}
			if (sel && sel.nodes) {
				if (sel.nodes.length > 0) {
					this.cygraph.deselect("nodes", sel.nodes);
				} else {
					this.cygraph.deselect("nodes");
				}
			}
			if (sel && sel.edges) {
				if (sel.edges.length > 0) {
					this.cygraph.deselect("edges", sel.edges);
				} else {
					this.cygraph.deselect("edges");
				}
			}
		};
		
		this.select = function (sel) {
			if (sel && sel.nodes) {
				if (sel.nodes.length > 0) {
					this.cygraph.select("nodes", sel.nodes);
				} else {
					this.cygraph.select("nodes");
				}
			}
			if (sel && sel.edges) {
				if (sel.edges.length > 0) {
					this.cygraph.select("edges", sel.edges);
				} else {
					this.cygraph.select("edges");
				}
			}
		};
		
		this.chLayout = function(newLayout) {
			this.vis.graph.layout = newLayout;
			if (newLayout == "mtl") {
				this.mtl.calculate();
				this.layouts["mtl"] = this.mtl.getLayoutCW();
			}
			var send = this.layouts[newLayout];
			if (!send) {
				this.newLayout = this.layouts.defaultValue;
				this.vis.graph.layout = this.layouts.defaultValue;
				send = this.layouts[this.layouts.defaultValue];
			}
			this.cygraph.layout(send);
		};
		
		this.label = function(grp, state) {
			this.vis[grp].labels = state;
			this.vis[grp].tooltips = !state;
			this.cygraph.nodeLabelsVisible(this.vis.nodes.labels);
			this.cygraph.edgeLabelsVisible(this.vis.edges.labels); 
			this.cygraph.nodeTooltipsEnabled(this.vis.nodes.tooltips);
			this.cygraph.edgeTooltipsEnabled(this.vis.edges.tooltips); 
		};
		
		this.getInfoHeader = function (hdr, domclass) {
			var content = "<div class=\"" + domclass + "-header-box\">" + 
											 "<div class=\"" + domclass + "-header\">" + 
													hdr + 
											 	 "<div class=\"" + domclass + "-header-r\">" + 
											 	   "<a href=\"javascript:hidePopup()\">X<\/a>" + 
											 	 "<\/div>" + 
											 "<\/div>" + 
										"<\/div>";
			return content;
		};
		
		/** 
			* retrieve an html div containing the relevant node data.
			* designed to be shown in a popup or info div.
		*/
		this.getNodeInfo = function (node, domclass) {
			var openD   = "<div class=\"" + domclass + "\">";
			var closeD  = "<\/div>";
			var content;
			switch (node.data.role) {
				case "pathogen" : 
					content = openD + this.getPathInfo(node, domclass) + closeD;
					break;
				case "collection:pathogen" : 
					content = openD + this.getPathCollInfo(node, domclass) + closeD;
					break;
				case "disease" : 
					content = openD + this.getDiseaseInfo(node, domclass) + closeD;
					break;
				case "virulence factor" : 
					content = openD + this.getPAVFInfo(node, domclass) + closeD;
					break;
				case "collection:virulence factor" : 
					content = openD + this.getPAVFCollInfo(node, domclass) + closeD;
					break;
				case "gene" : 
					content = openD + this.getDAHGInfo(node, domclass) + closeD;
					break;
				case "collection:gene" : 
					content = openD + this.getDAHGCollInfo(node, domclass) + closeD;
					break;
			}
			return content;
		};
		
		this.getPathInfo = function(node, domclass) {
			var counts = this.getTerms(node.data.counts);
			var xrefs = this.getTerms(node.data.xrefs);
			var content = this.getInfoHeader("<i>" + node.data.label + "<\/i>", domclass);
			content += 
				"<div class=\"" + domclass + "-body\"><table>" + 
				"<tr class=\"row\">" + 
					"<td class=\"label\">Full name:<\/td>" + 
					"<td class=\"value\">" + 
						"<a href=\"" + String.xref("tax-id:" + xrefs["tax-id"][0]) + "\" target=\"_NEW\">" + 
						"<i>" + node.data.name + "<\/i><\/a>" + 
					"<\/td>" + 
				"<\/tr>" + 
				"<tr class=\"row-alt\">" + 
					"<td class=\"label\">Role:<\/td>" + 
					"<td class=\"value\">" + String.capwords(node.data.role) + "<\/td>" + 
				"<\/tr>";
			content += 
				"<tr class=\"row\">" + 
					"<td class=\"label\">Rank:<\/td>" + 
					"<td class=\"value\">" + this.getTaxonomyRank(node.data.rank) + "<\/td>" + 
				"<\/tr>";
			content += 
				"<tr class=\"row-alt\">" + 
					"<td class=\"label\">Virulence factors:<\/td>" + 
					"<td class=\"value\">" + counts["pavfs"][0]  + "<\/td>" + 
				"<\/tr>";
			content += 
				"<tr class=\"row\">" + 
					"<td class=\"label\">Associated diseases:<\/td>" + 
					"<td class=\"value\">" + counts["paids"][0]  + "<\/td>" + 
				"<\/tr>";
			content += 
				"<tr class=\"row-alt\">" + 
					"<td class=\"label\">Associated host genes:<\/td>" + 
					"<td class=\"value\">" + counts["dahgs"][0]  + " (by disease)" + 
					"<\/td>" + 
				"<\/tr>";
			// add xrefs
			/*
			var xrefs = node.data.xrefs.split("|");
			if (xrefs.length > 0) {
				content += "<tr class=\"row\"><td class=\"label\">More info:<\/td>" + 
									 "<td class=\"value\">";
				for (var i=0; i<xrefs.length; i++) {
					var xref = xrefs[i];
					if (!xref || typeof xref != "string") { continue; }
					var terms = xref.split(":");
					if (terms.length < 2) { terms[1] = terms[0]; }
					content += terms[0] + ":<a href=\"" + String.xref(xref) + "\" target=\"_NEW\">" + 
										 terms[1] + "<\/a>";
					content += "<br \/>";
				}
				content += "<\/td><\/tr>";
			}
			*/
			content += "<\/table><\/div>";
			return content;
		};
		
		this.getPathCollInfo = function(node, domclass) {
			var parent = this.getTerms(node.data.groups, "tax-name")["tax-name"];
			var xrefs = this.getTerms(node.data.xrefs);
			var content = this.getInfoHeader("<i>" + node.data.label + "<\/i>", domclass);
			content += 
				"<div class=\"" + domclass + "-body\"><table>" + 
				"<tr class=\"row\">" + 
					"<td class=\"label\">Taxonomy parent:<\/td>" + 
					"<td class=\"value\">" + 
						"<a href=\"" + String.xref("tax-id:" + xrefs["tax-id"][0]) + "\" target=\"_NEW\">" + 
						"<i>" + parent + "<\/i><\/a>" + 
					"<\/td>" + 
				"<\/tr>";
			content += 
				"<tr class=\"row-alt\">" + 
					"<td class=\"label\">Associated diseases:<\/td>" + 
					"<td class=\"value\">" + this.getTerms(node.data.counts, "paids")["paids"][0]  + "<\/td>" + 
				"<\/tr>";
			// add xrefs
			/*
			var xrefs = node.data.xrefs.split("|");
			if (xrefs.length > 0) {
				content += "<tr class=\"row\"><td class=\"label\">More info:<\/td>" + 
									 "<td class=\"value\">";
				for (var i=0; i<xrefs.length; i++) {
					var xref = xrefs[i];
					if (!xref || typeof xref != "string") { continue; }
					var terms = xref.split(":");
					if (terms.length < 2) { terms[1] = terms[0]; }
					content += terms[0] + ":<a href=\"" + String.xref(xref) + "\" target=\"_NEW\">" + 
										 terms[1] + "<\/a>";
					content += "<br \/>";
				}
				content += "<\/td><\/tr>";
			}
			*/
			content += 
				"<tr class=\"row\">" + 
					"<td class=\"note\" colspan=\"2\">" + 
						"This node represent a nonspecific group of " + parent + " species, " + 
						"at least some of which are associated with infectious disease." + 
					"<\/td>" + 
				"<\/tr>";
			content += "<\/table><\/div>";
			return content;
		};
		
		this.getDiseaseInfo = function (node, domclass) {
			var counts = this.getTerms(node.data.counts);
			var content = this.getInfoHeader(node.data.label, domclass);
			content += 
				"<div class=\"" + domclass + "-body\"><table>" + 
				"<tr class=\"row\">" + 
					"<td class=\"label\">MeSH term:<\/td>" + 
					"<td class=\"value\">" + 
						"<a href=\"" + String.xref("mesh-term:" + node.data.name) + "\" target=\"_NEW\">" + 
						node.data.name + "<\/a>" + 
					"<\/td>" + 
				"<\/tr>" + 
				"<tr class=\"row-alt\">" + 
					"<td class=\"label\">Role:<\/td>" + 
					"<td class=\"value\">" + String.capwords(node.data.role) + "<\/td>" + 
				"<\/tr>";
			content += 
				"<tr class=\"row\">" + 
					"<td class=\"label\">MeSH tree node:<\/td>" + 
					"<td class=\"value\">" + this.getTerms(node.data.groups, "mesh-tree")["mesh-tree"][0] + 
						"<\/td>" + 
				"<\/tr>";
			content += 
				"<tr class=\"row-alt\">" + 
					"<td class=\"label\">Associated human genes:<\/td>" + 
					"<td class=\"value\">" + 
						counts["dahgs"][0]  + 
					"<\/td>" + 
				"<\/tr>";
			// add xrefs
			/*
			var xrefs = node.data.xrefs.split("|");
			if (xrefs.length > 0) {
				content += "<tr class=\"row\"><td class=\"label\">More info:<\/td>" + 
									 "<td class=\"value\">";
				for (var i=0; i<xrefs.length; i++) {
					var xref = xrefs[i];
					if (!xref || typeof xref != "string") { continue; }
					var terms = xref.split(":");
					if (terms.length < 2) { terms[1] = terms[0]; }
					content += terms[0] + ":<a href=\"" + String.xref(xref) + "\" target=\"_NEW\">" + 
										 terms[1] + "<\/a>";
					content += "<br \/>";
				}
				content += "<\/td><\/tr>";
			}
			*/
			content += "<\/table><\/div>";
			return content;
		};
		
		this.getPAVFInfo = function (node, domclass) {
			var xrefs = this.getTerms(node.data.xrefs);
			var content = this.getInfoHeader(node.data.label, domclass);
			content += 
				"<div class=\"" + domclass + "-body\"><table>" + 
				"<tr class=\"row\">" + 
					"<td class=\"label\">Gene symbol:<\/td>" + 
					"<td class=\"value\">" + 
						"<a href=\"" + String.xref("patric-id:" + xrefs["patric-id"][0]) + 
						"\" target=\"_NEW\"><i>" + node.data.label + "<\/i><\/a>" + 
					"<\/td>" + 
				"<\/tr>" + 
				"<tr class=\"row-alt\">" + 
					"<td class=\"label\">Gene product:<\/td>" + 
					"<td class=\"value\">" + node.data.name + "<\/td>" + 
				"<\/tr>" + 
				"<tr class=\"row\">" + 
					"<td class=\"label\">Role:<\/td>" + 
					"<td class=\"value\">" + String.capwords(node.data.role) + "<\/td>" + 
				"<\/tr>";
			content += 
				"<tr class=\"row-alt\">" + 
					"<td class=\"label\">Organism:<\/td>" + 
					"<td class=\"value\">" + 
						"<a href=\"" + String.xref("tax-id:" + xrefs["tax-id"][0]) + 
						"\" target=\"_NEW\"><i>" + 
							this.getTerms(node.data.groups, "tax-name")["tax-name"][0] + 
						"<\/i><\/a>" + 
					"<\/td>" + 
				"<\/tr>";
			// add xrefs
			/*
			var xrefs = node.data.xrefs.split("|");
			if (xrefs.length > 0) {
				content += "<tr class=\"row\"><td class=\"label\">More info:<\/td>" + 
									 "<td class=\"value\">";
				for (var i=0; i<xrefs.length; i++) {
					var xref = xrefs[i];
					if (!xref || typeof xref != "string") { continue; }
					var terms = xref.split(":");
					if (terms.length < 2) { terms[1] = terms[0]; }
					content += terms[0] + ":<a href=\"" + String.xref(xref) + "\" target=\"_NEW\">" + 
										 terms[1] + "<\/a>";
					content += "<br \/>";
				}
				content += "<\/td><\/tr>";
			}
			*/
			content += "<\/table><\/div>";
			return content;
		};
		
		this.getPAVFCollInfo = function (node, domclass) {
			var org = this.getTerms(node.data.groups, "tax-name")["tax-name"][0];
			var count  = this.getTerms(node.data.counts, "pavfs")["pavfs"][0];
			var xrefs = this.getTerms(node.data.xrefs);
			var content = this.getInfoHeader(node.data.label, domclass);
			content += 
				"<div class=\"" + domclass + "-body\"><table>" + 
				"<tr class=\"row\">" + 
					"<td class=\"label\">Organism:<\/td>" + 
					"<td class=\"value\">" + 
						"<a href=\"" + String.xref("tax-id:" + xrefs["tax-id"][0]) + 
						"\" target=\"_NEW\"><i>" + org + "<\/i><\/a>" + 
					"<\/td>" + 
				"<\/tr>";
			// add xrefs
			/*
			var xrefs = node.data.xrefs.split("|");
			if (xrefs.length > 0) {
				content += "<tr class=\"row-alt\"><td class=\"label\">More info:<\/td>" + 
									 "<td class=\"value\">";
				for (var i=0; i<xrefs.length; i++) {
					var xref = xrefs[i];
					if (!xref || typeof xref != "string") { continue; }
					var terms = xref.split(":");
					if (terms.length < 2) { terms[1] = terms[0]; }
					content += terms[0] + ":<a href=\"" + String.xref(xref) + "\" target=\"_NEW\">" + 
										 terms[1] + "<\/a>";
					content += "<br \/>";
				}
				content += "<\/td><\/tr>";
			}
			*/
			content += 
				"<tr class=\"row-alt\">" + 
					"<td class=\"note\" colspan=\"2\">" + 
						"This node represents a set of " + count + " virulence factors " + 
						"from <i>" + org + "<\/>. See a table of all virulence factors " + 
						"from <i>" + org + "<\/> " + 
						"<a href=\"" + this.buildPAVFCurl(org) + "\">here<\/a>." + 
					"<\/td>" + 
				"<\/tr>";
			content += "<\/table><\/div>";
			return content;
		};
		
		this.getDAHGInfo = function (node, domclass) {
			var xrefs = this.getTerms(node.data.xrefs);
			var content = this.getInfoHeader(node.data.label, domclass);
			content += 
				"<div class=\"" + domclass + "-body\"><table>" + 
				"<tr class=\"row\">" + 
					"<td class=\"label\">Gene symbol:<\/td>" + 
					"<td class=\"value\"><i>" + node.data.label + "<\/i><\/td>" + 
				"<\/tr>" + 
				"<tr class=\"row-alt\">" + 
					"<td class=\"label\">Full name:<\/td>" + 
					"<td class=\"value\">" + node.data.name + "<\/td>" + 
				"<\/tr>" + 
				"<tr class=\"row\">" + 
					"<td class=\"label\">Role:<\/td>" + 
					"<td class=\"value\">" + String.capwords(node.data.role) + "<\/td>" + 
				"<\/tr>";
			content += 
				"<tr class=\"row-alt\">" + 
					"<td class=\"label\">Organism:<\/td>" + 
					"<td class=\"value\">" + 
						"<a href=\"" + String.xref("ncbi-taxid:" + xrefs["tax-id"][0]) + 
						"\" target=\"_NEW\"><i>" + 
							this.getTerms(node.data.groups, "tax-name")["tax-name"][0] + 
						"<\/i><\/a>" + 
					"<\/td>" + 
				"<\/tr>";
			// add xrefs
			/*
			var xrefs = node.data.xrefs.split("|");
			if (xrefs.length > 0) {
				content += "<tr class=\"row\"><td class=\"label\">More info:<\/td>" + 
									 "<td class=\"value\">";
				for (var i=0; i<xrefs.length; i++) {
					var xref = xrefs[i];
					if (!xref || typeof xref != "string") { continue; }
					var terms = xref.split(":");
					if (terms.length < 2) { terms[1] = terms[0]; }
					content += terms[0] + ":<a href=\"" + String.xref(xref) + "\" target=\"_NEW\">" + 
										 terms[1] + "<\/a>";
					content += "<br \/>";
				}
				content += "<\/td><\/tr>";
			}
			*/
			content += "<\/table><\/div>";
			return content;
		};
		
		this.getDAHGCollInfo = function (node, domclass) {
			var xrefs = this.getTerms(node.data.xrefs);
			var org     = this.getTerms(node.data.groups, "tax-name")["tax-name"][0];
			var disease = this.getTerms(node.data.xrefs, "mesh-term")["mesh-term"][0];
			var count   = this.getTerms(node.data.counts, "dahgs")["dahgs"][0];
			var counts = this.getTerms(node.data.counts);
			
			var content = this.getInfoHeader(node.data.label, domclass);
			content += 
				"<div class=\"" + domclass + "-body\"><table>" + 
				"<tr class=\"row\">" + 
					"<td class=\"label\">Organism:<\/td>" + 
					"<td class=\"value\">" + 
						"<a href=\"" + String.xref("ncbi-taxid:" + xrefs["tax-id"][0]) + 
						"\" target=\"_NEW\"><i>" + org + "<\/i><\/a>" + 
					"<\/td>" + 
					"<\/tr>" + 
				"<tr class=\"row-alt\">" + 
					"<td class=\"label\">Disease:<\/td>" + 
					"<td class=\"value\">" + 
						"<a href=\"" + String.xref("mesh-term:" + disease) + 
						"\" target=\"_NEW\">" + disease + "<\/a>" + 
					"<\/td>" + 
				"<\/tr>";
			content += 
				"<tr class=\"row\">" + 
					"<td class=\"label\">CTD genes:<\/td>" + 
					"<td class=\"value\">";
			if (counts["dahgs-ctd"][0] > 0) {
				content += 
						"<a href=\"" + this.buildDAHGCurl("ctd", disease) + "\">" + 
						counts["dahgs-ctd"][0] + 
						"<\/a>";
			} else {
				content += counts["dahgs-ctd"][0];
			}
			content += "<\/td><\/tr>";
			content += 
				"<tr class=\"row\">" + 
					"<td class=\"label\">GAD genes:<\/td>" + 
					"<td class=\"value\">";
			if (counts["dahgs-gad"][0] > 0) {
				content += 
						"<a href=\"" + this.buildDAHGCurl("gad", disease) + "\">" + 
						counts["dahgs-gad"][0] + 
						"<\/a>";
			} else {
				content += counts["dahgs-gad"][0];
			}
			content += "<\/td><\/tr>";
			// add xrefs
			/*
			var xrefs = node.data.xrefs.split("|");
			if (xrefs.length > 0) {
				content += "<tr class=\"row-alt\"><td class=\"label\">More info:<\/td>" + 
									 "<td class=\"value\">";
				for (var i=0; i<xrefs.length; i++) {
					var xref = xrefs[i];
					if (!xref || typeof xref != "string") { continue; }
					var terms = xref.split(":");
					if (terms.length < 2) { terms[1] = terms[0]; }
					content += terms[0] + ":<a href=\"" + String.xref(xref) + "\" target=\"_NEW\">" + 
										 terms[1] + "<\/a>";
					content += "<br \/>";
				}
				content += "<\/td><\/tr>";
			}
			content += 
				"<tr class=\"row-alt\">" + 
					"<td class=\"note\" colspan=\"2\">" + 
						"This node represents a set of " + count + " genes " + 
						"from <i>" + org + "<\/i> associated with " + disease + 
						". See a table of all genes associated with " + disease + 
						"<a href=\"" + this.buildDAHGCurl(disease) + "\">here<\/a>." + 
					"<\/td>" + 
				"<\/tr>";
			*/
			content += "<\/table><\/div>";
			return content;
		};
		
		this.getEdgeInfo = function (edge, domclass) {
			var openD   = "<div class=\"" + domclass + "\">";
			var closeD  = "<\/div>";
			var evidences = this.parseEvidence(edge.data.evidence);
			var content = this.getInfoHeader(edge.data.role, domclass);
			content +=  "<div class=\"" + domclass + "-body\"><table>" + 
									"<tr class=\"row\">" + 
										"<td class=\"label\">Edge type:<\/td>" + 
										"<td class=\"value\">" + edge.data.label + "<\/a>" + 
										"<\/td>" + 
									"<\/tr>" + 
									"<tr class=\"row-alt\">" + 
										"<td class=\"label\">Source:<\/td>" + 
										"<td class=\"value\">" + this.getNode(edge.data.source).data.label + "<\/td>" + 
									"<\/tr>" + 
									"<tr class=\"row\">" + 
										"<td class=\"label\">Target:<\/td>" + 
										"<td class=\"value\">" + this.getNode(edge.data.target).data.label + "<\/td>" + 
									"<\/tr>";
			// add evidences
			content += "<tr class=\"row-alt\"><td class=\"label\">Evidence:<\/td>" + 
								 "<td class=\"value\">";
			for (var i=0; i<evidences.length; i++) {
				var obj = evidences[i];
				var str = "";
				if (obj["xref"] != "") {
					var terms = obj["xref"].split(":");
					if (terms.length < 2) { terms[1] = terms[0]; }
					str += "<a href=\"" + String.xref(obj["xref"]) + "\" target=\"_NEW\">" + 
										 terms[1] + "<\/a> (";
				}
				if (obj["type"] != "") {
					str += obj["type"] + " via " + obj["source"];
				} else {
					str += obj["source"];
				}
				if (obj["xref"] != "") { str += ")"; }
				content += str + "<br \/>";
			}
			content += "<\/td><\/tr>";
			// add xrefs
			var xrefs = edge.data.xrefs.split("|");
			if (xrefs.length > 0) {
				content += "<tr class=\"row\"><td class=\"label\">More info:<\/td>" + 
									 "<td class=\"value\">";
				for (var i=0; i<xrefs.length; i++) {
					var xref = xrefs[i];
					if (!xref || typeof xref != "string") { continue; }
					var terms = xref.split(":");
					if (terms.length < 2) { terms[1] = terms[0]; }
					content += "<a href=\"" + String.xref(xref) + "\" target=\"_NEW\">" + 
										 terms[1] + "<\/a>";
					content += "<br \/>";
				}
				content += "<\/td><\/tr>";
			}
			content += "<\/table><\/div>";
			return openD + content + closeD;
		};

		this.getTerms = function (data, key) {
			var arr = data.split("|");
			var val = {};
			for (var i=0; i<arr.length; i++) {
				if (typeof arr[i] != "string") { continue; }
				var termArr = arr[i].split(":");
				if (!termArr[1] || typeof termArr[0] != "string") { continue; }
				if (!key || termArr[0] == key) {
					if (!val[termArr[0]]) { val[termArr[0]] = []; }
					val[termArr[0]].push(termArr[1]);
				}
			}
			return val;
		};
		
		this.parseEvidence = function (data) {
			var arr = data.split("|");
			var vals = [];
			for (var i=0; i<arr.length; i++) {
				if (typeof arr[i] != "string") { continue; }
				var termArr = arr[i].split(";");
				if (termArr.length < 1 || typeof termArr[0] != "string") { continue; }
				if (termArr.length < 2) {
					termArr[1] = "";
					termArr[2] = "";
				}
				if (termArr.length < 3) {
					termArr[2] = "";
				}
				var obj = { "source" : termArr[0], 
										"type" 	 : termArr[1], 
										"xref" 	 : termArr[2] };
				vals.push(obj);
			}
			return vals;
		};
		
		this.getTaxonomyRank = function (rankval) {
			if (this.taxRanks[rankval]) {
				return this.taxRanks[rankval];
			} else {
				return "no rank";
			}
		};
		
		this.buildPAVFCurl = function (org) {
			return "";
		};
		
		this.buildDAHGCurl = function (disease) {
			return "";
		};
		
		this.fillNodeAtts = function () {
			// parse out the node attributes and store them
			//this.vis.nodes.atts = {};
			var list = this.cygraph.nodes();
			for (var i in list) {
				// loop thru this graph's nodes
				var node = list[i];
				if (!node || !node.data) { continue; }
				for (var attName in node.data) {
					if (!attName) { continue; }
					var attVal = node.data[attName];
					if (!attVal) { continue; }
					if (!this.vis.nodes.atts[attName]) { 
						this.vis.nodes.atts[attName] = [attVal]; 
					} else if (!Array.has(this.vis.nodes.atts[attName], attVal)) { 
						this.vis.nodes.atts[attName].push(attVal); 
					}
				}
			}
		};
		
		this.fillEdgeAtts = function () {
			// parse out the node attributes and store them
			//this.vis.edges.atts = {};
			var list = this.cygraph.edges();
			for (var i in list) {
				// loop thru this graph's nodes
				var edge = list[i];
				if (!edge || !edge.data) { continue; }
				for (var attName in edge.data) {
					if (!attName) { continue; }
					var attVal = edge.data[attName];
					if (!attVal) { continue; }
					if (!this.vis.edges.atts[attName]) { 
						this.vis.edges.atts[attName] = [attVal]; 
					} else if (!Array.has(this.vis.edges.atts[attName], attVal)) { 
						this.vis.edges.atts[attName].push(attVal); 
					}
				}
			}
		};
		
		this.getEdgesAsArray = function() {
			if (this.cygraph) { return this.cygraph.edges(); }
			else { return []; }
		};
		
		this.getNodesAsArray = function() {
			if (this.cygraph) { return this.cygraph.nodes(); }
			else { return []; }
		};
		
		this.deselectNodes = function (ids) {
			this.cygraph.deselect("nodes", ids);
		};
		
		this.deselectEdges = function (ids) {
			this.cygraph.deselect("edges", ids);
		};
		
		this.selectNodes = function (ids) {
			this.cygraph.deselect();
			this.cygraph.select("nodes", ids);
		};
		
		this.selectEdges = function (ids) {
			this.cygraph.deselect();
			this.cygraph.select("edges", ids);
		};
		
		this.selectNeighbors = function (node) {
		this.cygraph.deselect();
		var nbrids = [node.data.id];
			var list = this.cygraph.edges();
			for (var i in list) {
				// loop thru this graph's edges
				var edge = list[i];
				if (edge && edge.data) {
					if (edge.data.source == node.data.id) {
						nbrids.push(edge.data.target);
					} else if (edge.data.target == node.data.id) {
						nbrids.push(edge.data.source);
					}
				}
			}
			this.cygraph.select("nodes", nbrids);
		};
		
		this.selectNeighborhood = function (node) {
			this.deselect();
			var nids = {};
			nids[node.data.id] = 1;
			var eids = {};
			var list = this.cygraph.edges();
			for (var i=0; i<list.length; i++) {
				// loop thru this graph's edges
				var edge = list[i];
				if (edge && edge.data) {
					if (edge.data.source == node.data.id || edge.data.target == node.data.id) {
						nids[edge.data.source] = 1;
						nids[edge.data.target] = 1;
						eids[edge.data.id] = 1;
					}
				}
			}
			var sel = { "nodes" : [], "edges" : [] };
			for (var nid in nids) {
				sel.nodes.push(nid);
			}
			for (var eid in eids) {
				sel.edges.push(eid);
			}
			this.select(sel);
		};
		
		this.selectSimilar = function (prop, target) {
			//alert("from app: trying to select by " + att);
			//alert(this.cygraph.containerId);
			this.deselect();
			var val = null;
			for (var i in target.data) {
				if (target.data[i] && i == prop) { val = target.data[i]; break; }
			}
			//alert(val);
			
			var sel = [];
			var elements;
			if (target.group == "nodes") { 
				elements = this.cygraph.nodes();
			} else if (target.group == "edges") {
				elements = this.cygraph.edges();
			} else {
				return;
			}
			for (var j in elements) {
				if (elements[j] && elements[j].data && elements[j].data[prop] && 
					 (elements[j].data[prop] == val)) {
					//alert(nodes[j].data.id);
					sel.push(elements[j].data.id);
					//alert(sel);
				}
			}
			//alert(prop + ": " + sel);
			this.cygraph.select(target.group, sel);
		}

		this.toggleNodeLabels = function () {
			this.label("nodes", !this.vis.nodes.labels);
		};
		
		this.filterDecorators = function(toHide) {
			if (!toHide) {
				// fix tree node positions to current x, y
				this.mtl.fixTrees();
				// recalc positions of decorator nodes that may have been hidden
				this.mtl.decorateTrees();
				this.layouts.mt = this.mtl.getLayoutCW();
				// show all nodes
				this.filterOn();
				this.cygraph.layout(this.layouts.mt);
				return;
			}
			var filter;
			switch (toHide) {
				case "hg"  : 
					// fix tree node positions to current x, y
					this.mtl.fixTrees();
					// recalc positions of any decorator nodes that had been hidden
					this.mtl.decorateTrees();
					this.layouts.mt = this.mtl.getLayoutCW();
					this.cygraph.layout(this.layouts.mt);
					var hide = [ { prop: "role", val: "gene" }, 
											 { prop: "role", val: "collection:gene" } ];
					filter = { group: "nodes", hide : hide };
					// clear position of gene nodes, to force a recalc
					this.mtl.clearDecoratorPts(hide);
					break;
				case "vf"  : 
					// fix tree node positions to current x, y
					this.mtl.fixTrees();
					// recalc positions of any decorator nodes that had been hidden
					this.mtl.decorateTrees();
					this.layouts.mt = this.mtl.getLayoutCW();
					this.cygraph.layout(this.layouts.mt);
					var hide = [ { prop: "role", val: "virulence factor" }, 
											 { prop: "role", val: "collection:virulence factor" } ];
					filter = { group: "nodes", hide : hide };
					// clear position of virulence nodes
					this.mtl.clearDecoratorPts(hide);
					break;
				case "all" : 
					var hide = [ { prop: "role", val: "gene" }, 
											 { prop: "role", val: "collection:gene" }, 
											 { prop: "role", val: "virulence factor" }, 
											 { prop: "role", val: "collection:virulence factor" } ];
					filter = { group: "nodes", hide : hide };
					// clear position of all decorator nodes
					this.mtl.clearDecoratorPts(hide);
					break;
			}
			if (filter) { this.filterOn(filter); }
		};
		
		this.filterOn = function (opts) {
			// { group: "nodes", hide: [{ prop: "role", val: "gene" }] }
			if (!opts) { this.cygraph.removeFilter(); return; }
			if (!opts.group) {
				opts.group = "nodes";
				this.filterOn(opts);
				opts.group = "edges";
				this.filterOn(opts);
				return;
			}
			this.cygraph.filter(opts.group, function(target) {
				if (opts.hide) {
					for (var i=0; i<opts.hide.length; i++) {
						var prop = opts.hide[i].prop;
						var val  = opts.hide[i].val;
						if (target.data[prop] == val) { return false; }
					}
					return true;
				} else if (opts.show) {
					for (var i=0; i<opts.show.length; i++) {
						var prop = opts.show[i].prop;
						var val  = opts.show[i].val;
						if (target.data[prop] != val) { return true; }
					}
					return false;
				}
			});
		};
		
		this.exportAs = function (opts) {
			if (!this.exportHandlerURL) {
				alert(" Network export is not available. No export handler defined.");
				return;
			}
			var valid = { "pdf": 1, "png": 1, "xgmml" : 1 };
			var format;
			(opts.format) ? format = opts.format.toLowerCase() : format = "pdf";
			if (!valid[format]) { format = "pdf"; }
			var w;
			(opts.w) ? w = opts.w : w = this.getCanvasDims().w;
			var h;
			(opts.h) ? h = opts.h : h = this.getCanvasDims().h;
			this.cygraph.exportNetwork( format, 
																  this.exportHandlerURL + "?type=" + format, 
																	{ width: w, height: h, window: "_blank" });
		};
		
		this.openExt = function (opts) {
			if (!opts || !opts.url) { return; }
			var target;
			(opts.target) ? target = opts.target : target = "_new";
			window.open(opts.url);
		};
		
		this.getMtlObj = function () {
			return this.mtl;
		};
		
		this.updateMtl = function () {
			this.mtl.fixAllPositions();
		};
		
		
	};
	
	/**
		* Constructor. Accepts an untyped options object and initializes the layout.
		* the actual layout is not computed until the calculate() method is run.
		*
		* options = { graph: graphObject, 									REQUIRED.
		*																										the graph to layout.
		*
		*							trees: { prop : nodeProperty, 				REQUIRED.
		*																										the node property that 
		*																										separates the two trees.
		*
		*											 valA : [propertyValue], 			the values that define 
		*																										the first tree.
		*
		*											 valB : [propertyValue] 			the values that define 
		*																										the second tree.
		*										 }, 
		*							canvas: htmlElementId, 								OPTIONAL.
		*																										the DOM node that holds 
		*																										the graph. used to calculate 
		*																										node positions. defaults 
		*																										to the document body.
		*
		*							orient: leftToRight|topToBottom|auto	OPTIONAL
		*																										the orientation of the 
		*																										layout. defaults to 'auto', 
		*																										which optimizes the 
		*																										orientation based on the 
		*																										canvas shape.
		*						}
	*/
	vbi.patric.hpi.idv.MirroredTreeLayout = function (options) {
		
		this.points = {};	// { id: {x: x, y: y}, }
/*		
		this.adjListA  = {};
		this.nodelistA = {};
		
		this.adjListB  = {};
		this.nodelistB = {};
*/		
		this.adjlists  = [];
		this.roots     = [];
		this.treelists = [];
		this.layers    = [];
		
		this.intertreeEdgeIDs = [];
		this.decoratedSystems = {};
		this.decoratorNodes   = {};
		this.outgroupNodeIDs  = [];
		
		this.graph = options.graph;
		this.trees = options.trees;
		this.canvas = document.body;
		options.canvas && (this.canvas = options.canvas);
		this.orient = "auto";
		options.orient && (this.orient = options.orient);
				
		/**
			* extract the two subtrees from the graph. along the way, also identify 
			* the tree roots, intertree edges, decorator nodes, and outgroups 
			* (edges that have neither node in a tree).
		*/
		this.extract = function () {
			this.adjlists[0]  = {};
			this.adjlists[1]  = {};
			this.treelists[0] = {};
			this.treelists[1] = {};
			this.roots[0] 		= [];
			this.roots[1] 		= [];
			
			var list = this.graph.getEdgesAsArray();
			for (var i=0; i<list.length; i++) {
				var edge = list[i];
				// skip if this is not a valid edge
				if (!edge || !edge.group) { continue; }
				var eid = this.graph.getEdgeID(edge);
				var sid = this.graph.getSourceNodeID(edge);
				var tid = this.graph.getTargetNodeID(edge);
				var snode = this.graph.getNode(sid);
				var tnode = this.graph.getNode(tid);
				if (snode.data[this.trees.prop] && Array.has(this.trees.valA, snode.data[this.trees.prop]) && 
						tnode.data[this.trees.prop] && Array.has(this.trees.valA, tnode.data[this.trees.prop])) {
					// both nodes are in treeA so add to adjListA.
					if (!this.adjlists[0][sid]) { this.adjlists[0][sid] = []; }
					if (!this.adjlists[0][tid]) { this.adjlists[0][tid] = []; }
					this.adjlists[0][sid].push(tid);
					this.treelists[0][sid] = 1;
					this.treelists[0][tid] = 1;
				} else if (snode.data[this.trees.prop] && Array.has(this.trees.valB, snode.data[this.trees.prop]) && 
									 tnode.data[this.trees.prop] && Array.has(this.trees.valB, tnode.data[this.trees.prop])) {
					// both nodes are in treeB so add to adjListB.
					if (!this.adjlists[1][sid]) { this.adjlists[1][sid] = []; }
					if (!this.adjlists[1][tid]) { this.adjlists[1][tid] = []; }
					this.adjlists[1][sid].push(tid);
					this.treelists[1][sid] = 1;
					this.treelists[1][tid] = 1;
				} else if ((snode.data[this.trees.prop] && Array.has(this.trees.valA, snode.data[this.trees.prop]) && 
									  tnode.data[this.trees.prop] && Array.has(this.trees.valB, tnode.data[this.trees.prop])) || 
									 (snode.data[this.trees.prop] && Array.has(this.trees.valB, snode.data[this.trees.prop]) && 
									  tnode.data[this.trees.prop] && Array.has(this.trees.valA, tnode.data[this.trees.prop]))) {
					// one node is in treeA, the other in treeB, so add to intertreeEdgeIDs
					if (!Array.has(this.intertreeEdgeIDs, eid)) { this.intertreeEdgeIDs.push(eid); }
				} else if ((snode.data[this.trees.prop] && Array.has(this.trees.valA, snode.data[this.trees.prop])) || 
									 (snode.data[this.trees.prop] && Array.has(this.trees.valB, snode.data[this.trees.prop]))) {
					// source is in a tree, but target is not, so add target to decoratedSystems
					if (!this.decoratedSystems[sid]) { this.decoratedSystems[sid] = {}; }
					this.decoratedSystems[sid][tid] = eid;
					if (!this.decoratorNodes[tid]) { this.decoratorNodes[tid] = {}; }
					this.decoratorNodes[tid][sid] = eid;
				} else if ((tnode.data[this.trees.prop] && Array.has(this.trees.valA, tnode.data[this.trees.prop])) || 
									 (tnode.data[this.trees.prop] && Array.has(this.trees.valB, tnode.data[this.trees.prop]))) {
					// target is in a tree, but source is not, so add source to decoratorNodeIDs
					if (!this.decoratedSystems[tid]) { this.decoratedSystems[tid] = {}; }
					this.decoratedSystems[tid][sid] = eid;
					if (!this.decoratorNodes[sid]) { this.decoratorNodes[sid] = {}; }
					this.decoratorNodes[sid][tid] = eid;
				} else {
					// neither node is in a tree, so add both to outgroupNodeIDs
					if (!Array.has(this.outgroupNodeIDs, sid)) { this.outgroupNodeIDs.push(sid); }
					if (!Array.has(this.outgroupNodeIDs, tid)) { this.outgroupNodeIDs.push(tid); }
				}
			}
			
			// extract roots for each tree
			for (var i=0; i<this.treelists.length; i++) {
				for (var tid in this.treelists[i]) {
					if (Array.has(this.roots[i], tid)) { continue; }
					var root = true;
					for (var sid in this.treelists[i]) {
						if (sid == tid) { continue; }
						if (Array.has(this.adjlists[i][sid], tid)) {
							root = false;
							break;
						}
					}
					if (root) { this.roots[i].push(tid); }
				}
			}
		};
		
		this.assign = function () {
			// for each tree, bfs from each root node to assign nodes to layers
			for (var k=0; k<this.treelists.length; k++) {
				var full = [];
				for (var j=0; j<this.roots[k].length; j++) {
					var bfs = this.bfs(this.roots[k][j], this.adjlists[k]);
					for (var i=0; i<bfs.length; i++) {
						if (full[i]) {
							full[i] = full[i].concat(bfs[i]);
						} else {
							full[i] = bfs[i];
						}
					}
				}
				this.layers[k] = full;
			}
		};
		
		this.countLeaves = function(root, adjlist) {
			var count = 0;
			var bfs = this.bfs(root, adjlist);
			for (var i=0; i<bfs.length; i++) {
				var layer = bfs[i];
				for (var j=0; j<layer.length; j++) {
					var sid = layer[j];
					if (!adjlist[sid] || adjlist[sid].length == 0) { count++; }
				}
			}
			return count;
		};
		
		this.bfs = function (root, adjlist) {
			var lcount = 0;
			var layers = [[root]];
			var found = {};
			while (layers[lcount].length > 0) {
				layers[lcount+1] = new Array();
				for (var i=0; i<layers[lcount].length; i++) {
					var sid = layers[lcount][i];
					for (var j=0; j< adjlist[sid].length; j++) {
						var tid = adjlist[sid][j];
						if (!found[tid]) {
							found[tid] = [];
						}
							found[tid].push(lcount+1);
							layers[lcount+1].push(tid);
						//}
					}
				}
				lcount++;
			}
			if (layers[layers.length-1].length == 0) { layers.pop(); }
			// reconcile nodes with indegree > 1
			// always stored in the bottom-most layer
			for (var tid in found) {
				if (!found[tid] || found[tid].length < 2) { continue; }
				for (var i=0; i<found[tid].length-1; i++) {
					// remove from layers
					var layerIndx = found[tid][i];
					layers[layerIndx] = Array.rmVal(layers[layerIndx], tid);
				}
			}
			return layers;
		};
		
		this.visited = {};
		this.offset  = 0;
		this.xPosition = function (root, winW, fullW, allLeaves, adjlist) {
			this.visited[root] = true;
			// set the x position of this node
			// offset is used to shift the window to the right
			if (!this.points[root]) { this.points[root] = { x: -1, y: -1 } };
			if (this.points[root].x == -1) {
				this.points[root].x = this.offset + winW/2;
			}
			// if this node has no adjacenct nodes, we've reached the terminal node 
			// and can just return
			if (!adjlist[root] || adjlist[root].length == 0) { 
				// when we backtrack up the tree, we move the offset
				this.offset += (fullW/allLeaves);
				return;
			}
			// loop through each target of this node and recurse down the tree (dfs)
			for (var i=0; i<adjlist[root].length; i++) {
				var tid = adjlist[root][i];
				if (this.visited[tid]) { continue; }
				// calculate width of the win that this target node should occupy
				// this is based on the number of leaf nodes under the target
				var leafCount = this.countLeaves(tid, adjlist);
				var winW = (leafCount/allLeaves)*fullW;
				this.xPosition(tid, winW, fullW, allLeaves, adjlist);
			}
		};
		
		this.calculate = function () {
			this.plantTrees();
			//this.optimizeTreeLeaves();
			this.decorateTrees();
		};
		
		this.plantTrees = function () {
			this.points  = {};
			var dims = this.graph.getCanvasDims();	// { center: {x: x, y: y}, h: h, w: w, top: top, left: left }
			var availW = dims.w;
			var hlineTotal = 0;
			for (var i=0; i<this.layers.length; i++) {
				hlineTotal += this.layers[i].length;
			}
			for (var j=0; j<this.treelists.length; j++) {
				// set the x coordinates for the tree nodes
				this.visited = {};
				this.offset  = 0;
				var allLeaves = 0;
				for (var i=0; i<this.roots[j].length; i++) {
					allLeaves += this.countLeaves(this.roots[j][i], this.adjlists[j]);
				}
				for (var i=0; i<this.roots[j].length; i++) {
					var leafCount = this.countLeaves(this.roots[j][i], this.adjlists[j]);
					var winW = (leafCount/allLeaves)*availW;
					this.xPosition(this.roots[j][i], winW, availW, allLeaves, this.adjlists[j]);
				}
				// set the y coordinates for the tree nodes
				var availH = (this.layers[j].length*dims.h)/hlineTotal - 25;
				var hoffset = 2 + (0.5 * this.graph.getNodeSize(this.graph.getNode(this.roots[j][0])));
				var hlines = this.section(this.layers[j].length, availH-hoffset);
				for (var i=this.layers[j].length-1; i>-1; i--) {
					var jiggle = 0;
					var layer = this.layers[j][i];
					var pt = Math.abs((dims.h * j) - (hoffset + hlines[i]));
					for (var k=0; k<layer.length; k++) {
						this.points[layer[k]].y = pt + jiggle;
						switch (jiggle) {
							case 0   : jiggle = 9; break;
							case 9   : jiggle = 0; break;
							//case 9  : jiggle = -9; break;
							//case -9 : jiggle = 0;   break;
						}
					}
				}
			}
		};
		
		this.fixTrees = function () {
			for (var i=0; i<this.treelists.length; i++) {
				for (var nid in this.treelists[i]) {
					var node = this.graph.getNode(nid);
					this.points[nid] = { x: node.x, y: node.y };
				}
			}
		};
		
		this.fixAllPositions = function () {
			for (var nid in this.points) {
				var node = this.graph.getNode(nid);
				this.points[nid] = { x: node.x, y: node.y };
			}
		};
		
		this.section = function(n, aspace) {
			var spacing = aspace/(n-1);
			var divs = [];
			for (var i=0; i<n; i++) {
				divs.push(spacing*i);
			}
			return divs;
		};
		
		this.evenspace = function(items, range) {
			var spacing = (range[1] - range[0])/(items.length+1);
			var pts = {};
			for (var i=0; i<items.length; i++) {
				pts[items[i]] = spacing + (spacing*i);
			}
			return pts;
		};
		
		this.clearDecoratorPts = function (toClear) {
			for (var treeNode in this.decoratedSystems) {
				// get all decorator nodes for this tree node
				var dnodes = this.decoratedSystems[treeNode];
				for (var did in dnodes) {
					if (!toClear) { this.points[did] = { x: -1, y: -1 }; }
					else {
						for (var i=0; i<toClear.length; i++) {
							var prop = toClear[i].prop;
							var val  = toClear[i].val;
							if (this.graph.getNode(did).data[prop] == val) {
								this.points[did] = { x: -1, y: -1 };
								break;
							}
						}
					}
				}
			}
		};
		
		/**
			* positions decorator nodes for each decoratedSystem. 
			*
		**/
		this.decorateTrees = function () {
			var dims = this.graph.getCanvasDims();	// { center: {x: x, y: y}, h: h, w: w, top: top, left: left }
			for (var treeNode in this.decoratedSystems) {
				// get all decorator nodes for this tree node
				var dnodes = this.decoratedSystems[treeNode];
				// count the decorator nodes not shared by any other tree nodes
				var uniques = 0;
				for (var did in dnodes) {
					if (Object.size(this.decoratorNodes[did]) == 1) { uniques++; }
				}
				// place each decorator for this treeNode
				var step = 6.28/uniques;
				var mult = 0;
				for (var did in dnodes) {
					// skip if the node has already been placed
					if (this.points[did] && this.points[did].x && this.points[did].x != -1) { continue; }
					// ignore if it does not exist (bug catcher)
					if (!this.decoratorNodes[did] || Object.size(this.decoratorNodes[did]) < 1) {
						this.points[did] = { x: dims.center.x, y: dims.center.y };
						continue;
					}
					// place on shell if unique to this tree node
					if (Object.size(this.decoratorNodes[did]) == 1) {
						// calculate a weighted distance of the node from the anchor
						var h = this.graph.getNodeSize(this.graph.getNode(did)) + 15;
						var A = 0.5 + step*mult;
						// place the decorator to bottom-right of the tree node by default
						this.points[did] = { x: this.points[treeNode].x + h*Math.cos(A), 
																 y: this.points[treeNode].y + h*Math.sin(A) };
						mult++;
					} else if (Object.size(this.decoratorNodes[did]) > 1) {
						// place this decorator at the weighted midpoint of all its 
						// neighbor tree nodes
						var nbrs = this.decoratorNodes[did];
						var xpt = 0;
						var ypt = 0;
						var wtotal = 0;
						for (var nid in nbrs) {
							var wt = this.graph.getEdgeWeight(this.graph.getEdge(nbrs[nid]));
							xpt += this.points[nid].x * wt;
							ypt += this.points[nid].y * wt;
							wtotal += wt;
						}
						this.points[did] = { x: xpt/wtotal, y: ypt/wtotal };
					}
				}
			}
		};
				
		/**
			* determines if there is significant overlap between a target shape and a 
			* set of shapes. accepts an untyped obj with the following syntax:
			*
			* { target: {x: x, y: y, w: w, h: h}, 
					nbrhd: [{x: x, y: y, w: w, h: h},...] }
			*
		*/
		this.overlap = function (target, nbrhd) {
			var trad = target.w;
			if (target.h > trad) { trad = target.h; }
			for (var i=0; i<nbrhd.length; i++) {
				var nrad = nbrhd[i].w;
				if (nbrhd[i].h > nrad) { nrad = nbrhd[i].h; }
				var c = Math.sqrt( Math.pow((target.x-nbrhd[i].x), 2) + 
													 Math.pow((target.y-nbrhd[i].y), 2) 
				);
				if (trad+nrad < c) { return true; }
			}
			return false;
		};
		

		/**
			* finds all placed nodes that have some part in the defined region:
			*
			* region = { left: left, right: right, top: top, bottom: bottom}
			*
			* returns a list of node obj: { id: { x: x, y: y, w: w, h: h }, ... }.
		*/
		this.getNodesInRegion = function (region) {
			var nodes = {};
			for (var nid in this.points) {
				var pt = this.points[nid];								// xy coords of node center
				var size = this.graph.getNodeSize(this.graph.getNode(nid));
				if (this.inBounds(region, nid, pt)) {
					nodes[nid] = { x: pt.x, y: pt.y, w: size, h: size };
				}
			}
			return nodes;
		};
		
		/**
			* determines if placing the given node at the given point will keep it 
			* within the given bounds or not.
			* bounds = { top: top, bottom: bottom, left: left, right: right }
			* the top left corner of the graph is (0, 0).
		*/
		this.inBounds = function (bounds, nid, pt) {
			var rad = this.graph.getNodeSize(this.graph.getNode(nid))/2;
			if (bounds.left-rad < 0) { bounds.left = 0; }
			if (bounds.top-rad < 0)  { bounds.top  = 0; }
			if ((pt.y+rad <= bounds.bottom) && (pt.y-rad >= bounds.top) && 
					(pt.x-rad >= bounds.left) && (pt.x+rad <= bounds.right)) {
				return true;
			} else {
				return false;
			}
		};
		
		/**
			*
			* finds the distance r that minimizes the L-J pair potential:
			* v(lj) = 4E [ (1/r)^12 - (2/r)^6 ]
			* 
			* where e = 1/wt and r = distance between nodes
			* stops if r exceeds max.
		*/
		this.minTwoParticleLJ = function (e, max) {
			var r = 0.1;
			var v = Infinity;
			var vtemp = 0;
			var b = 4*e;
			for (var r=0.1; r<=max; r+=0.1) {
				vtemp = b*(Math.pow((1/r), 12) - Math.pow((2/r), 6));
				if (vtemp > v) { break; }
				v = vtemp;
			}
			return r;
		};
		
		this.getLayoutCW = function () {
			
			return { name: "Preset", 
							 options: { fitToScreen: this.fitToScreen, 
							 						points: 		 this.getPointsAsArray() }
						 };
		};
		
		this.getPoint = function (id) {
			if (id && this.points[id] && this.points[id].x && this.points[id].y) {
				return { id: id, 
								 x:  this.points[id].x, 
								 y:  this.points[id].y };
			}
			else {
				return { id : id, x: -1, y: -1 };
			}
		};
		
		this.getPointsAsArray = function () {
			var pArr = [];
			for (var id in this.points) {
				if (this.points[id] && this.points[id].x && this.points[id].y) {
					var obj = { id: id, 
											x : this.points[id].x, 
											y : this.points[id].y };
					pArr.push(obj);
				}
			}
			return pArr;
		};
	
	};

})();
