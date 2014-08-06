/**
	* Functions in this library are integrated with the main jsp caller page. 
	* They use gloabl variables defined in that page.
	* This library is not designed to stand alone.
*/


var tooltipTimeout;
var ttipDelay=500;

/*
function initGraphListeners() {
	
	// grab the mouse coordinates to enable contextual tooltip rollovers
	graph.addListener("captureMouseCoords", "mousemove", function(evt) {
		mouseX_graph = evt.pageX;
		mouseY_graph = evt.pageY;
	});
	
	// add to fids on select
	graph.addListener("recordOnSelect", "select", "nodes", function(evt) {
		
		var fidsArr = Ext.getDom("fids").value.split(",");
		
		if (Ext.getDom("fids").value == "") fidsArr = [];
		
		for (var i=0; i<evt.target.length; i++) {
			var node = evt.target[i];
			if (node.data.hasOwnProperty('na_feature_id') && 
					node.data.hasOwnProperty('taxid') && 
					node.data.taxid != 9606) {
	
				fidsArr.push(node.data.na_feature_id);
			}
		}

		Ext.getDom("fids").value = fidsArr.join(",");
		
		//alert(Ext.getDom("fids").value);

	});
	
	// remove from fids on deselect
	graph.addListener("rmOnDeselect", "deselect", "nodes", function(evt) {

		var fidsArr = Ext.getDom("fids").value.split(",");
		
		if (Ext.getDom("fids").value == "") fidsArr = [];
		
		for (var i=0; i<evt.target.length; i++) {
			var node = evt.target[i];
			if (node.data.hasOwnProperty('na_feature_id') && 
					node.data.hasOwnProperty('taxid') && 
					node.data.taxid != 9606) {
				
				for (var j=0; j<fidsArr.length; j++) {
					if (fidsArr[j] == node.data.na_feature_id) {
						// remove j from fidsArr;
						fidsArr.splice(j,1);
						break;
					}
				}
				
			}
		}
		
		Ext.getDom("fids").value = fidsArr.join(",");
		
		//alert(Ext.getDom("fids").value);

	});
	
	// show tooltip for nodes
	graph.addListener("showNodeTip", "mouseover", "nodes", function(evt) {
		var off_x  = Ext.getCmp("graph_center-panel").getPosition()[0];
		var ctr_x  = off_x + evt.target.x;
		var dist_x = (evt.target.size*graph.zoom())/2 - 5;

		var off_y  = Ext.getCmp("graph_center-panel").getPosition()[1];
		var ctr_y  = off_y + evt.target.y;
		var dist_y = dist_x;
		
		var x = parseInt( ctr_x + ( dist_x * Math.cos(5.49) ) );
		var y = parseInt( ctr_y + ( dist_y * Math.sin(0.78) ) );

		//var x = mouseX + 5;
		//var y = mouseY + 5;
		
		var taxaCat = network.getCategory("taxa");
		var taxon = taxaCat.getEntry(evt.target.data[taxaCat.getIdInGroup()]);

		var locus_tag = evt.target.data.locus_tag;
		
		var txt = 
			"<div class='patric-graph-tooltip'>" + 
			"<table>" + 
			"<tr><td class='patric-graph-tooltip-key'>Protein<\/td><td class='patric-graph-tooltip-val'>" + evt.target.data.name + "<\/td><\/tr>" + 
			"<tr><td class='patric-graph-tooltip-key'>Organism<\/td><td class='patric-graph-tooltip-val'><i>" + taxon.name + "<\/i><\/td><\/tr>" + 
			"<tr><td class='patric-graph-tooltip-key'>Locus tag<\/td><td class='patric-graph-tooltip-val'>" + locus_tag + "<\/td><\/tr>";


		var description = "";
		var indx = ds.find("label_a", evt.target.data.name);
		if (indx == -1) {
			indx = ds.find("label_b", evt.target.data.name);
			if (indx != -1) {
				description = ds.getAt(indx).get("description_b");
			}
		} else {
			description = ds.getAt(indx).get("description_a");
		}
		
		if (description != "" && typeof description != "undefined") {
			txt += "<tr><td class='patric-graph-tooltip-key'>Description<\/td><td class='patric-graph-tooltip-val'>" + description + "<\/td><\/tr>";
		}
		
		txt += "<\/table><\/div>";
		
		var fcn = 'openTooltip("'+txt+'",'+x+','+y+')';
		tooltipTimeout = setTimeout(fcn, ttipDelay);
	});
	
	// show tooltip for edges
	graph.addListener("showEdgeTip", "mouseover", "edges", function(evt) {
		
		/*
		// top left corner of panel
		var ex = Ext.getCmp("graph_center-panel").getPosition()[0];
		var ey = Ext.getCmp("graph_center-panel").getPosition()[1];

		var offset = evt.target.width*graph.zoom();
		
		var ctrX = (graph.node(evt.target.data.source).x+graph.node(evt.target.data.target).x)/2;
		var ctrY = (graph.node(evt.target.data.source).y+graph.node(evt.target.data.target).y)/2;
		
		var x = parseInt(ex + ctrX + offset);
		var y = parseInt(ey + ctrY + offset);
		*/
		
		/*
		var x = mouseX_graph + 5;
		var y = mouseY_graph + 5;
		
		var typeCat = network.getCategory("type");
		var type = typeCat.getEntry(evt.target.data[typeCat.getIdInGroup()]);
		var methodCat = network.getCategory("method");
		var method = methodCat.getEntry(evt.target.data[methodCat.getIdInGroup()]);
		var srcCat = network.getCategory("source");
		var src = srcCat.getEntry(evt.target.data[srcCat.getIdInGroup()]);
		var refCat = network.getCategory("reference");
		var ref = refCat.getEntry(evt.target.data[refCat.getIdInGroup()]);
		
		var txt = 
			"<div class='patric-graph-tooltip'>" + 
			"<table>" + 
			"<tr><td class='patric-graph-tooltip-key'>Interaction type<\/td><td class='patric-graph-tooltip-val'>" + type.name + "<\/td><\/tr>" + 
			"<tr><td class='patric-graph-tooltip-key'>Detection method<\/td><td class='patric-graph-tooltip-val'>" + method.name + "<\/td><\/tr>" + 
			"<tr><td class='patric-graph-tooltip-key'>Original source<\/td><td class='patric-graph-tooltip-val'>" + src.name + "<\/td><\/tr>" + 
			"<tr><td class='patric-graph-tooltip-key'>Literature reference<\/td><td class='patric-graph-tooltip-val'>" + 
					ref.source + ": " + ref.source_id + 
			"<\/td><\/tr>";
		
		txt += "<\/table><\/div>";
		
		var fcn = 'openTooltip("'+txt+'",'+x+','+y+')';
		tooltipTimeout = setTimeout(fcn, ttipDelay);
	});
	
	// hide tooltip
	graph.addListener("hideTooltip", "mouseout", function(evt) {
		clearTimeout(tooltipTimeout);
		tooltip.setVisible(false);
	});
		
	// a mousedown also hides any open tooltip; allows node dragging
	graph.addListener("hideTooltipClick", "mousedown", function(evt) {
		clearTimeout(tooltipTimeout);
		tooltip.setVisible(false);
		//cancelEvent(evt);
	});
		
	// enable scroll-to-zoom with the mousewheel
	graph.addListener("zoomGraph", "mousewheel", function(evt) {
		var d = getWheelDelta(evt);
		var z = d/80;
		graph.zoom(z);
		cancelEvent(evt);
	});
	
	// double-clicking a node shows it's neighborhood
	graph.addListener("showNbrhd", "dblclick", "nodes", function(evt) {
		clearTimeout(tooltipTimeout);
		var anchorId = evt.target.data.id;
		var nbrhd = network.getNbrhd(anchorId);
		graph.select("nodes", nbrhd.nodes);
		//graph.select("edges", nbrhd.edges);
	});
	
	
}
*/

function openTooltip(txt, x, y) {
	if (tooltip.isVisible()) {
		tooltip.setVisible(false);
	}
	
	tooltip.setVisible(true);
	tooltip.update(txt);
	
	tooltip.setPosition(parseInt(x), parseInt(y));
}


function mkLegend_Graph(opts) {
	
	if (typeof opts == "undefined" || 
			!opts.hasOwnProperty("renderTo")) {
		return;
	}
	if (!opts.hasOwnProperty("align")) opts.align = "left";

	var txt = "";
	
	for (var i=0; i<opts.filters.length; i++) {
		var subTxt = "";
		var grp = opts.filters[i].group;
		var prop = opts.filters[i].prop;
		var sort = "asc";
		if (opts.filters[i].hasOwnProperty("sort")) sort = opts.filters[i].sort;
		
		var category = network.getCategory(opts.filters[i].category);

		subTxt += 
			"<div class='patric-graph-legend-block-" + opts.align + "'>" + 
			"<div class='patric-graph-legend-title'>" + grp.wordCaps() + "<\/div>";
		
		var ids = category.getEntryIds();
		if (sort == "desc") {
			ids.sort(function(a,b) {
				var a_name = category.getEntry(a).name;
				var b_name = category.getEntry(b).name;
				if (a_name < b_name) return 1;
				if (a_name > b_name) return -1;
				return 0;
			});
		} else {
			ids.sort(function(a,b) {
				var a_name = category.getEntry(a).name;
				var b_name = category.getEntry(b).name;
				if (a_name < b_name) return -1;
				if (a_name > b_name) return 1;
				return 0;
			});
		}
		
		for (var j=0; j<ids.length; j++) {
			var entry = category.getEntry(ids[j]);
			var imgSrc = entry[prop] + ".png";
			imgSrc = imgSrc.replace(/#/g, "_");
			//alert(imgSrc);
			subTxt += mkLegendEntry(entry.name, imgSrc, opts.align);
		}
		
		txt += subTxt;
	}
	
	txt += "<\/div>";

	Ext.getDom(opts.renderTo).innerHTML = txt;
}

function mkLegendEntry(label, imgSrc, align) {
	var entry = 
		"<div class='patric-graph-legend-entry-" + align + "'>" + 
		"<span class='patric-graph-legend-symbol-" + align + "'>" + 
		"<img src='\/patric-hpi\/images\/legend\/legend" + imgSrc + "' \/>" + 
		"<\/span>" + 
		"<span class='patric-graph-legend-label-" + align + "'>" + label + "<\/span>" + 
		"<\/div>";
	return entry;
}


/* graph-specific toolbar and toolbar methods. */

function mkToolbar_Graph() {
	graphToolbar = new Ext.Toolbar({
		items:[
					 {title: 'Workspace',  
					columns: 1, 
					xtype: 'buttongroup', 
					width: 120,
					height:80,
					items:[{
						scale: 'large',  
						rowspan: 2, 
						 width: 120,
						iconAlign: 'top', 
						text:'Add to Group', 
						icon: '/patric/images/toolbar_cart.png', 
						handler: function(){
							AddToCart_Graph();
						}
					}]
					}, '-',
					{title: 'View', 
					 columns: 2, 
					 xtype: 'buttongroup', 
					 width: 120,
					 height:80,
					 items:[{
						 scale: 'small', 
						 iconAlign: 'left', 
						 text:'FASTA DNA', 
							icon: '/patric/images/toolbar_dna.png',
							handler: function(){
							ShowDownloadFasta_Graph('display', 'dna');
						}
					 },{
						 scale: 'small',  
						 iconAlign: 'left', 
						 text:'FASTA Protein', 
						 icon: '/patric/images/toolbar_protein.png',
						 handler: function(){
							ShowDownloadFasta_Graph('display', 'protein');
						}
					 },{
						 scale: 'small',  
						 iconAlign: 'left', 
						 text:'FASTA DNA/Protein', 
						icon: '/patric/images/toolbar_dna_protein.png',
						 handler: function(){
							ShowDownloadFasta_Graph('display', 'both');
						}
					 }]
					}, '-',
					{title: 'Download', 
					 columns: 1, 
					 xtype: 'buttongroup', 
						width:120,
						height:80,
					// items:[btnTableDownload, btnFastaDownload]
					 items:[{
						 scale: 'small',  
						 iconAlign: 'left', 
						 width: 120,
						 text:'Table', 
						 icon: '/patric/images/toolbar_table.png', 
						 xtype:'splitbutton',
						 menu: [{text: 'Text File (.txt)',
									icon: '/patric/images/toolbar_text.png',
								handler: function(){
									DownloadFile('txt');
									
							
								}
								}, 
								{text: 'Excel file (.xls)',
								icon: '/patric/images/toolbar_excel.png',
								handler: function(){
									DownloadFile('xls');
									
								}
							}]
						},{
						 scale: 'small',  
						 iconAlign: 'left', 
						 text:'FASTA', 
						 width: 120,
						 icon: '/patric/images/toolbar_fasta.png', 
						 xtype:'splitbutton',
						 menu: [{text: 'DNA',
									icon: '/patric/images/toolbar_dna.png',
							handler: function(){
								ShowDownloadFasta_Graph('download', 'dna');
							}
							}, 
							{text: 'Protein',
							icon: '/patric/images/toolbar_protein.png',
							handler: function(){
								ShowDownloadFasta_Graph('download', 'protein');
							}
							}, 
							{text: 'DNA/Protein',
							icon: '/patric/images/toolbar_dna_protein.png',
							handler: function(){
								ShowDownloadFasta_Graph('download', 'both');
								
							}
						}]
					 }]
					}, '-',
					{title: 'Tools', 
					 columns: 2, 
						width:120,
						height:80,
					 xtype: 'buttongroup',   
					 items:[{
							scale: 'large',  
							rowspan: 2, 
							iconAlign: 'top', 
							width:80,
							text:'M S A', 
							icon: '/patric/images/toolbar_msa.png', 
							handler: function(){
								callAlign_Graph();
							}
						 
					 },{
							scale: 'large',  
							rowspan: 2, 
							iconAlign: 'top', 
							width:80,
							text:'ID Mapping', 
							icon: '/patric/images/toolbar_id_mapping.png', 
							menu: {
									layout: 'column',
													width: 411,
													autoHeight: true,
													cls:'x-menu-cstm',
													defaults: {
															xtype: 'menu',
															floating: false,
															columnWidth: 0.33,
															hidden: false,
															style: {
																	'border-color': 'transparent',
																	'background-image': 'none'
															}
													},
													items:[{items:[
																	 '<b class="menu-title">PATRIC Identifiers</b>', 
																 {text: 'PATRIC Locus Tag', 
																itemCls:'x-menu-item-cstm',
																handler: function(){
												callIDMapping_Graph("PATRIC Locus Tag");
											}},
																 {text: 'PATRIC ID', 
											itemCls:'x-menu-item-cstm',
																handler: function(){
												callIDMapping_Graph("PATRIC ID");
											}},
																 {text: 'PSEED ID', 
											itemCls:'x-menu-item-cstm',
																handler: function(){
												callIDMapping_Graph("PSEED ID");
											}}]
									}, 
									{items:['<b class="menu-title">REFSEQ Identifiers</b>',
													{text: 'RefSeq Locus Tag', 
											itemCls:'x-menu-item-cstm',
																	handler: function(){
													callIDMapping_Graph("RefSeq Locus Tag");
											}},
													{text: 'RefSeq', 
											itemCls:'x-menu-item-cstm',
																	handler: function(){
													callIDMapping_Graph("RefSeq");
											}},
													{text: 'Gene ID', 
											itemCls:'x-menu-item-cstm',
																	handler: function(){
													callIDMapping_Graph("Gene ID");
											}},
													{text: 'GI', 
											itemCls:'x-menu-item-cstm',
																	handler: function(){
													callIDMapping_Graph("GI");
											}}]
									},
															{items:['<b class="menu-title">Other Identifiers</b>',
													{text:'Allergome', 
											itemCls:'x-menu-item-cstm',
																	handler: function(){
													callIDMapping_Graph("Allergome");
											}},
											{text:'BioCyc', 
											itemCls:'x-menu-item-cstm',
																	handler: function(){
													callIDMapping_Graph("BioCyc");
											}},
											{text:'DIP', 
											itemCls:'x-menu-item-cstm',
																	handler: function(){
													callIDMapping_Graph("DIP");
											}},
											{text:'DisProt', 
											itemCls:'x-menu-item-cstm',
																	handler: function(){
													callIDMapping_Graph("DisProt");
											}},
												{text:'DrugBank', 
											itemCls:'x-menu-item-cstm',
																	handler: function(){
													callIDMapping_Graph("DrugBank");
											}},
												{text:'ECO2DBASE', 
											itemCls:'x-menu-item-cstm',
																	handler: function(){
													callIDMapping_Graph("ECO2DBASE");
											}},
												{text:'EMBL', 
											itemCls:'x-menu-item-cstm',
																	handler: function(){
													callIDMapping_Graph("EMBL");
											}},
												{text:'EMBL-CDS', 
											itemCls:'x-menu-item-cstm',
																	handler: function(){
													callIDMapping_Graph("EMBL-CDS");
											}},
											{text:'More ...', 
											itemCls:'x-menu-item-cstm',
											menu:{
														layout: 'column',
																	width: 411,
																	autoHeight: true,
																	cls:'x-menu-cstm',
																	defaults: {
																			xtype: 'menu',
																				floating: false,
																				columnWidth: 0.33,
																				hidden: false,
																				style: {
																						'border-color': 'transparent',
																						'background-image': 'none'
																				}
																		},
																		items:[{
																			items:[{
																			text:'EchoBASE',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("EchoBASE");
																			}},{
																			text:'EcoGene',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("EcoGene");
																			}	
																			},{
																			text:'EnsemblGenome',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("EnsemblGenome");
																			}
																			},{
																			text:'EchoBASE',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("EchoBASE");
																			}
																			},{
																			text:'EnsemblGenome_PRO',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("EnsemblGenome_PRO");
																			}
																			},{
																			text:'EnsemblGenome_TRS',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("EnsemblGenome_TRS");
																			}
																			},{
																			text:'GeneTree',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("GeneTree");
																			}
																			},{
																			text:'GenoList',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("GenoList");
																			}
																			},{
																			text:'GenomeReviews',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("GenomeReviews");
																			}
																			},{
																			text:'HOGENOM',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("HOGENOM");
																			}
																			},{
																			text:'HSSP',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("HSSP");
																			}
																			},{
																			text:'KEGG',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("KEGG");
																			}
																			}]
																		},{
																			items:[{
																			text:'LegioList',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("LegioList");
																			}
																			},{
																			text:'Leproma',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("Leproma");
																			}
																			},{
																			text:'MEROPS',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("MEROPS");
																			}},{
																			text:'MINT',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("MINT");
																			}},{
																			text:'NMPDR',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("NMPDR");
																			}},{
																			text:'OMA',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("OMA");
																			}},{
																			text:'OrthoDB',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("OrthoDB");
																			}},{
																			text:'PDB',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("PDB");
																			}},{
																			text:'PeroxiBase',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("PeroxiBase");
																			}},{
																			text:'PptaseDB',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("PptaseDB");
																			}},{
																			text:'ProtClustDB',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("ProtClustDB");
																			}},{
																			text:'PseudoCAP',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("PseudoCAP");
																			}}]
																		},
																		{
																			items:[{
																			text:'REBASE',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("REBASE");
																			}},{
																			text:'Reactome',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("Reactome");
																			}},{
																			text:'RefSeq_NT',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("RefSeq_NT");
																			}},{
																			text:'TCDB',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("TCDB");
																			}},{
																			text:'TIGR',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("TIGR");
																			}},{
																			text:'TubercuList',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("TubercuList");
																			}},{
																			text:'UniParc',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("UniParc");
																			}},{
																			text:'UniProtKB-ID',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("UniProtKB-ID");
																			}},{
																			text:'UniRef100',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("UniRef100");
																			}},{
																			text:'UniRef50',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("UniRef50");
																			}},{
																			text:'UniRef90',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("UniRef90");
																			}},{
																			text:'World-2DPAGE',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("World-2DPAGE");
																			}},{
																			text:'eggNOG',
																			itemCls:'x-menu-item-cstm',
																			handler: function(){
																				callIDMapping_Graph("eggNOG");
																			}}]
																			}]
																		}
																	}]
										}
									]
													}
						 
					 }]
					},'->', '-',
					{title: 'Help', 
					 columns: 1, 
					width:120,
					height:80,
					 xtype: 'buttongroup',
					 items:[{
							scale: 'large',  
							rowspan: 2, 
							width:120,
							iconAlign: 'top',
							text:'PATRIC FAQs', 
							style: 'padding-left:5px; padding-right:5px;',
							icon: '/patric/images/toolbar_faq.png', 
							handler: function(){
								window.open ("http://enews.patricbrc.org/faqs/", "_new","menubar=1,resizable=1,scrollbars=1, fullscreen=1, toolbar=1,titlebar=1,status=1"); 

							}
					 }]
					}]
		});
}


function AddToCart_Graph() {
	//alert("adding to cart: " + Ext.getDom('fids').value);
	if (Ext.getDom('fids').value != "") {
		addSelectedItems("Feature");
	} else {
		alert("No item(s) are selected. To add to group, at least one item must be selected.");
	}
}

function ShowDownloadFasta_Graph(showdownload, type) {

	if (Ext.getDom('fids').value != "") {
		
		Ext.getDom("fTableForm").action = "/patric-common/jsp/fasta_download_handler.jsp";
		Ext.getDom("fastaaction").value = showdownload;
		Ext.getDom("fastascope").value = "Selected";
		Ext.getDom("fastatype").value = type;

		if (showdownload == "display") {
			window.open("","disp","width=920,height=400,scrollbars,resizable");
			Ext.getDom("fTableForm").target = "disp";
		} else {
			Ext.getDom("fTableForm").target = "";
		}
		
		Ext.getDom("fTableForm").submit();

	} else {
		alert("No item(s) are selected. To show Fasta sequence, at least one item must be selected.");
	}
}

function callAlign_Graph(){

	if (Ext.getDom('fids').value != "") {
		Ext.Ajax.request({
		    url: "/portal/portal/patric/FIGfamSorter/FigFamSorterWindow?action=b&cacheability=PAGE",
		    method: 'POST',
		    params: {featureIds: Ext.getDom("fids").value, callType:"toAligner"},
		    success: function(response, opts) {

				document.location.href = "TreeAligner?pk=" + response.responseText;
			    
			}
		});	
		 				
	} else {
		alert("No item(s) are selected. To align, at least one item must be selected.");
	}
}

function callIDMapping_Graph(to){

	if (Ext.getDom('fids').value != "") {
		
		Ext.Ajax.request({
				url: "/portal/portal/patric/IDMapping/IDMappingWindow?action=b&cacheability=PAGE",
				method: 'POST',
				params: {keyword: Ext.getDom("fids").value, from:'PATRIC ID', to:to, sraction:'save_params'},
				success: function(response, opts) {

				document.location.href = "IDMapping?cType=&cId=&dm=result&pk=" + response.responseText;
					
			}
		});	
			
	} else {
		alert("No item(s) are selected. At least one item must be selected.");
	}
}

