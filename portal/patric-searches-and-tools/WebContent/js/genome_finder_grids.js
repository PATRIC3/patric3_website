function createLayout() {
	var Page = $Page, property = Page.getPageProperties(), hash = property.hash;

//	Ext.create('Ext.panel.Panel', {
Ext.create('Ext.container.Container', {
		id : 'tabLayout',
		border : true,
		autoScroll : false,
		width : $(window).width() - 250,
		items : [{
			region : 'north',
			border : false,
			height : 22,
			xtype : 'tabpanel',
			id : 'tabPanel',
			items : [{
				title: "Genomes",
				id: "0"
			}, {
				title: "Sequences",
				id: "1"
//			}, {
//				title: "Chart",
//				id: "2"
			}],
			ClickFromTab : true,
			listeners : {
				'tabchange' : function(tabPanel, tab) {
					if (tab.getId() == 0) {
//						Ext.getCmp("southPanel").setVisible(true);
//						Ext.getCmp("southPanelChart").setVisible(false);
						hash.cwG = false, hash.gId = "", hash.gName = "";
					} else if (tab.getId() == 1) {
//						Ext.getCmp("southPanel").setVisible(true);
//						Ext.getCmp("southPanelChart").setVisible(false);
						hash.cwG = (hash.cwG || hash.cwG == "true"), hash.gId = !hash.cwG ? "" : hash.gId, hash.gName = !hash.cwG ? "" : hash.gName;
//					} else if (tab.getId() == 2) {
//						Ext.getCmp("southPanel").setVisible(false);
//						Ext.getCmp("southPanelChart").setVisible(true);
//						updateChart();
//						this.ClickFromTab = false;
					}

					if (!this.ClickFromTab) {
						loadGrid();
						this.ClickFromTab = true;
					} else {
						hash.aT = parseInt(tab.getId());
						hash.aP[tab.getId()] = 1, createURL();
					}
				}
			}
		}, {
			region : 'center',
			xtype: 'container',
			id : 'centerPanel',
			contentEl : 'information',
			border : false,
			split : false
		}, {
			region : 'south',
			xtype: 'container',
			id : 'southPanel',
			html : '<div id="PATRICGrid"></div>',
			height : 570,
			border : false,
			autoScroll : true
//		}, {
//			region: 'south',
//			id: 'southPanelChart',
//			contentEl: 'chart-main',
//			hidden: true,
//			height: 570,
//			border: false,
//			autoScroll: true
		}],
		renderTo : 'sample-layout'
	});

Ext.create('Ext.panel.Panel', {
		title : 'Filter By',
		renderTo : 'tree-panel',
		width : 239,
		height : 620,
		border : true,
		resizable : true,
		autoScroll : false,
		collapsible : true,
		collapseDirection : 'left',
		id : 'treePanel',
		items : [{
			id : 'westPanel',
			region : 'west',
			html : '<div id="GenericSelector" style="padding-left: 3px;"></div>',
			width : 239,
			height : 596,
			split : true,
			tbar : [{
				xtype : 'textfield',
				itemId : 'keyword',
				width : 150,
				hideLabel : true,
				allowBlank : true,
				value : hash.kW,
				emptyText : 'keyword',
				listeners : {
					specialKey : function(field, e) {
						if (e.getKey() == e.ENTER) {
							hash.kW = field.value.trim();
							Ext.getDom("keyword").value = getOriginalKeyword(hash);
							refresh();
						}
					}
				}
			}, '->', {
				text : 'Clear All',
				handler : function() {
					hash.kW = "";
					Ext.getDom("keyword").value = getOriginalKeyword(hash);
					this.ownerCt.getComponent("keyword").setRawValue("");
					refresh("clear_all");
				}
			}]
		}],
		listeners : {
			resize : function(cmp, width, height, oldWidth, oldHeight, eOpts) {
				var Page = $Page;
				if (Page.getGrid()) {
					Ext.getCmp("westPanel").setWidth(width);
					Ext.getCmp("GenericSelectorTree").setWidth(width - 7);
					Page.doTabLayout();
				}
			}
		}
	});
}

function loadFBCD() {
	var tabs = Ext.getCmp("tabPanel"), id = tabs.getActiveTab().getId(), hash = $Page.getPageProperties().hash;

	SetLoadParameters();

	Ext.getCmp('westPanel').getDockedItems()[0].getComponent("keyword").setRawValue(hash.kW);

	if (hash.aT == parseInt(id)) {
		loadGrid();
	} else {
		tabs.ClickFromTab = false;
		tabs.setActiveTab(hash.aT);
	}
}

function getExtraParams() {
	var Page = $Page, property = Page.getPageProperties(), hash = property.hash, which = hash.aT, name = property.name, tree = property.tree;

	return {
		pk : hash.key,
		need : which,
		taxonId : hash.tId,
		genomeId : hash.gId,
		keyword : constructKeyword((tree) ? tree.getSelectedTerms() : {}, name),
		facet : JSON.stringify({
			"facet" : configuration[name].display_facets.join(","),
			"facet_text" : configuration[name].display_facets_texts.join(","),
			'field_facets': configuration[name].field_facets.join(','),
			'date_range_facets': configuration[name].date_range_facets.join(',')
		})
	};
}

function CallBack() {
	var Page = $Page, property = Page.getPageProperties(), hash = property.hash, totalcount = Page.getStore(hash.aT).totalCount, tree = property.tree, treeDS = property.treeDS, name = property.name;

	if (hash.aT == "1") {
		if (hash.cwG && hash.cwG != "false") {
			Ext.getDom('grid_result_summary').innerHTML = "<b>" + totalcount + " sequences found in " + hash.gName + "</b><br/>";
		} else {
			Ext.getDom('grid_result_summary').innerHTML = "<b>" + totalcount + " sequences found</b><br/>";
		}
	}
	else {
		Ext.getDom('grid_result_summary').innerHTML = "<b>" + totalcount + " genomes found</b><br/>";
	}
	if (tree) {
		treeDS.proxy.extraParams = {
			need : "tree",
			pk : hash.key,
			keyword : constructKeyword((tree != null) ? tree.getSelectedTerms() : {}, name),
			facet : JSON.stringify({
				"facet" : configuration[name].display_facets.join(","),
				"facet_text" : configuration[name].display_facets_texts.join(","),
				'field_facets': configuration[name].field_facets.join(','),
				'date_range_facets': configuration[name].date_range_facets.join(',')
			}),
			state : JSON.stringify(tree.getState())
		};
		treeDS.load();
	} else {
		createTree();
	}
	if (Page.getGrid().sortchangeOption) {
		Page.getGrid().setSortDirectionColumnHeader();
	}
}

function renderCDS_Count_RAST(value, metadata, record, rowIndex, colIndex, store) {
	if (value != "0") {
		metadata.tdAttr = 'data-qtip="' + value + '" data-qclass="x-tip"';
		return Ext.String.format('<a href="FeatureTable?cType=genome&amp;cId={0}&amp;featuretype=CDS&amp;annotation=PATRIC&amp;filtertype=">{1}</a>', record.data.genome_id, value);
	} else {
		metadata.tdAttr = 'data-qtip="0" data-qclass="x-tip"';
		return "0";
	}
}

function renderCDS_Count_BRC(value, metadata, record, rowIndex, colIndex, store) {
	if (value != "0") {
		metadata.tdAttr = 'data-qtip="' + value + '" data-qclass="x-tip"';
		return Ext.String.format('<a href="FeatureTable?cType=genome&amp;cId={0}&amp;featuretype=CDS&amp;annotation=BRC1&amp;filtertype=">{1}</a>', record.data.genome_id, value);
	} else {
		metadata.tdAttr = 'data-qtip="0" data-qclass="x-tip"';
		return "0";
	}
}

function renderCDS_Count_RefSeq(value, metadata, record, rowIndex, colIndex, store) {

	if (value != "0") {
		metadata.tdAttr = 'data-qtip="' + value + '" data-qclass="x-tip"';
		return Ext.String.format('<a href="FeatureTable?cType=genome&amp;cId={0}&amp;featuretype=CDS&amp;annotation=RefSeq&amp;filtertype=">{1}</a>', record.data.genome_id, value);
	} else {
		metadata.tdAttr = 'data-qtip="0" data-qclass="x-tip"';
		return "0";
	}
}

function renderTotal(value, metadata, record, rowIndex, colIndex, store) {
	metadata.tdAttr = 'data-qtip="' + value + '" data-qclass="x-tip"';
	return Ext.String.format("<a href=\"javascript:void(0);\" onclick=\"ShowSequenceTab('" + record.data.genome_id + "','" + record.data.genome_name.replace(/'/gi, '`') + "');\"/>" + value + "</a>");
}

function renderCompletionDate(value, metadata, record, rowIndex, colIndex, store) {
	metadata.tdAttr = 'data-qtip="' + value + '" data-qclass="x-tip"';
	return value;
}

function ShowSequenceTab(genomeId, genomeName) {

	var Page = $Page, property = Page.getPageProperties(), hash = property.hash;

	hash.aT = 1, hash.aP[1] = 1, hash.gName = genomeName, hash.gId = genomeId, hash.cwG = true;

	createURL();
}

//Download File
//BEGIN

function DownloadFile(type) {"use strict";

	var Page = $Page, property = Page.getPageProperties(), hash = property.hash, form = Ext.getDom("fTableForm"), name = property.name, tree = property.tree;

	if (isOverDownloadLimit()) {
		return false;
	}

	if (tree.getSelectedTerms()["Keyword"] == null) {
		tree.selectedTerm["Keyword"] = Ext.getDom("keyword").value;
	}

	// form.action = "/patric-searches-and-tools/jsp/grid_download_handler.jsp";
	form.action = "/portal/portal/patric/GenomeFinder/GenomeFinderWindow?action=b&cacheability=PAGE&need=download";
	// form.download_keyword.value = constructKeyword(tree.getSelectedTerms(), name);
	form.pk.value = hash.key;
	form.fileformat.value = arguments[0];
	form.target = "";
	getHashFieldsToDownload(form);

	var grid = Page.getGrid();
	var sort = [];
	sort.push(getSortersInText(grid.store));
	form.sort.value = JSON.stringify(sort);
	form.submit();
}
