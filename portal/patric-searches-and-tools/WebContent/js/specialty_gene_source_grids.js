function createLayout() {
	var Page = $Page, property = Page.getPageProperties(), hash = property.hash;

	// Ext.create('Ext.panel.Panel', {
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
				title : "Specialty Genes",
				id : "0"
			}],
			ClickFromTab : true,
			listeners : {
				'tabchange' : function(tabPanel, tab) {
					if (tab.getId() == 0) {
						hash.cwG = false, hash.gId = "", hash.gName = "";
					} else {
						hash.cwG = (hash.cwG || hash.cwG == "true"), hash.gId = !hash.cwG ? "" : hash.gId, hash.gName = !hash.cwG ? "" : hash.gName;
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
			height : 620,
			border: 0,
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
		source: hash.source,
		keyword : constructKeyword((tree) ? tree.getSelectedTerms() : {}, name),
		facet : JSON.stringify({
			"facet" : configuration[name].display_facets.join(","),
			"facet_text" : configuration[name].display_facets_texts.join(","),
			'field_facets': configuration[name].field_facets.join(',')
		})
	};
}

function CallBack() {
	var Page = $Page, property = Page.getPageProperties(), hash = property.hash, totalcount = Page.getStore(hash.aT).totalCount, tree = property.tree, treeDS = property.treeDS, name = property.name;

	Ext.getDom('grid_result_summary').innerHTML = "<b>" + totalcount + " records found</b><br/>";
	
	if (tree) {
		treeDS.proxy.extraParams = {
			need : "tree",
			pk : hash.key,
			keyword : constructKeyword((tree != null) ? tree.getSelectedTerms() : {}, name),
			facet : JSON.stringify({
				"facet" : configuration[name].display_facets.join(","),
				"facet_text" : configuration[name].display_facets_texts.join(","),
				'field_facets': configuration[name].field_facets.join(',')
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

//function getSelectedFeatures() {"use strict";
//
//	var Page = $Page, property = Page.getPageProperties(), sl = Page.getCheckBox().getSelections(), i, fids = property.fids;
//
//	for ( i = 0; i < sl.length; i++) {
//		fids.push(sl[i].data.feature_id);
//	}
//}

function renderSourceId(value, metadata, record, rowIndex, colIndex, store) {
	metadata.tdAttr = 'data-qtip="' + value + '" data-qclass="x-tip"';
	if (record.data.source == "PATRIC_VF") {
		return Ext.String.format('<a href="SpecialtyGeneEvidence?source=PATRIC_VF&sourceId={0}">{0}</a>', value);
	} else {
		return value;
	}
}
function renderHomologCount(value, metadata, record, rowIndex, colIndex, store) {
	if (value != "" && value != 0) {
		return Ext.String.format('<a href="SpecialtyGeneList?cType=taxon&cId=2&kw=source:{1}+source_id:{2}">{0}</a>', value, record.data.source, record.data.source_id);
	} else {
		return value;
	}
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
	form.action = "/portal/portal/patric/SpecialtyGeneSource/SpecialtyGeneSourceWindow?action=b&cacheability=PAGE&need=download";
//	form.download_keyword.value = constructKeyword(tree.getSelectedTerms(), name);
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
