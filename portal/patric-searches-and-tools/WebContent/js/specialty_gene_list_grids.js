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
			height : 340,
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
					// clear filter panel
					var fP = Ext.getCmp("filterPanel");
					fP.getComponent('sbjCoverage').setRawValue(0);
					fP.getComponent('qryCoverage').setRawValue(0);
					fP.getComponent('identity').setRawValue(0);
					fP.getComponent("organism_filter").setValue({scope:"All"});

					refresh("clear_all");
				}
			}]
		}, {
			id: 'filterPanel',
			xtype: 'container',
			region: 'south',
			height: 250,
			border: 0,
			margin: '0 0 0 10px',
			items: [{
				xtype: 'displayfield',
				fieldLabel: 'Filter BLAST Hits by:',
				labelStyle: 'color: #CC6600; font-weight: bold',
				labelWidth: 200
			}, {
				xtype: 'radiogroup',
				itemId: 'organism_filter',
				fieldLabel: 'Source organism',
				labelSeparator: '',
				labelAlign: 'top',
				margin: '5 0 5 16',
				columns: 1,
				items: [{
					boxLabel: 'Same Genome',
					name: 'scope',
					inputValue: 'Genome',
					margin: '0 0 0 16'
				}, {
					boxLabel: 'Same Species',
					name: 'scope',
					inputValue: 'Species',
					margin: '0 0 0 16'
				}, {
					boxLabel: 'Same Genus',
					name: 'scope',
					inputValue: 'Genus',
					margin: '0 0 0 16'
				}, {
					boxLabel: 'All',
					name: 'scope',
					inputValue: 'All',
					checked: true,
					margin: '0 0 0 16'
				}]
			}, {
				xtype: 'numberfield',
				itemId: 'qryCoverage',
				fieldLabel: '% Query Coverage',
				labelSeparator: ' >= ',
				labelAlign: 'left',
				value: 0,
				minValue: 0,
				maxValue: 100,
				step: 10,
				labelWidth: 140,
				width: 200,
				margin: '0 0 3 16'
			},{
				xtype: 'numberfield',
				itemId: 'sbjCoverage',
				fieldLabel: '% Subject Coverage',
				labelSeparator: ' >= ',
				labelAlign: 'left',
				value: 0,
				minValue: 0,
				maxValue: 100,
				step: 10,
				labelWidth: 140,
				width: 200,
				margin: '0 0 3 16'
			},{
				xtype: 'numberfield',
				itemId: 'identity',
				fieldLabel: '% Identity',
				labelSeparator: ' >= ',
				labelAlign: 'left',
				value: 0,
				minValue: 0,
				maxValue: 100,
				step: 10,
				labelWidth: 140,
				width: 200,
				margin: '0 0 3 16'
			},{
				xtype: 'button',
				text: '<span style="color:#fff">Filter</span>',
				cls: 'button',
				margin: '5 0 0 160',
				handler: function() {
					hash.sbjCoverage = this.ownerCt.getComponent('sbjCoverage').getValue();
					hash.qryCoverage = this.ownerCt.getComponent('qryCoverage').getValue();
					hash.identity = this.ownerCt.getComponent('identity').getValue();
					hash.scope = this.ownerCt.getComponent("organism_filter").getValue().scope;
					Ext.getDom("keyword").value = getOriginalKeyword(hash);
					refresh();
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
		keyword : constructKeyword((tree) ? tree.getSelectedTerms() : {}, name),
		facet : JSON.stringify({
			"facet" : configuration[name].display_facets.join(","),
			"facet_text" : configuration[name].display_facets_texts.join(",")
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
				"facet_text" : configuration[name].display_facets_texts.join(",")
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

function getSelectedFeatures() {"use strict";

	var Page = $Page, property = Page.getPageProperties(), sl = Page.getCheckBox().getSelections(), i, fids = property.fids;

	for ( i = 0; i < sl.length; i++) {
		fids.push(sl[i].data.feature_id);
	}
}

function renderSource(value, metadata, record, rowIndex, colIndex, store) {
	metadata.tdAttr = 'data-qtip="' + value + '" data-qclass="x-tip"';
	switch (record.data.source) {
		case "ARDB":
			return Ext.String.format('<a href="//ardb.cbcb.umd.edu/" target="_blank">{0}</a>', value);
			break;
		case "CARD":
			return Ext.String.format('<a href="//arpcard.mcmaster.ca/" target="_blank">{0}</a>', value);
		case "DrugBank":
			return Ext.String.format('<a href="//v3.drugbank.ca/" target="_blank">{0}</a>', value);
			break;
		case "TDD":
			return Ext.String.format('<a href="//bidd.nus.edu.sg/group/TTD/ttd.asp" target="_blank">{0}</a>', value);
			break;
		case "Human":
			return Ext.String.format('<a href="//www.ncbi.nlm.nih.gov/assembly/GCF_000001405.26" target="_blank">{0}</a>', value);
			break;
		case "PATRIC_VF":
			return Ext.String.format('<a href="SpecialtyGeneSource?source={0}&kw=">{0}</a>', value);
			break;
		case "VFDB":
			return Ext.String.format('<a href="//www.mgc.ac.cn/VFs/" target="_blank">{0}</a>', value);
			break;
		case "Victors":
			return Ext.String.format('<a href="//www.phidias.us/victors/" target="_blank">{0}</a>', value);
			break;
		default:
			return value;
	}
}
function renderSourceId(value, metadata, record, rowIndex, colIndex, store) {
	metadata.tdAttr = 'data-qtip="' + value + '" data-qclass="x-tip"';
	switch (record.data.source) {
		case "ARDB":
			return Ext.String.format('<a href="//ardb.cbcb.umd.edu/cgi/search.cgi?db=R&term={0}" target="_blank">{0}</a>', value);
			break;
		case "DrugBank":
			return Ext.String.format('<a href="//v3.drugbank.ca/molecules/{0}" target="_blank">{0}</a>', value);
			break;
		case "TDD":
			return Ext.String.format('<a href="//bidd.nus.edu.sg/group/TTD/ZFTTDDetail.asp?ID={0}" target="_blank">{0}</a>', value);
			break;
		case "Human":
			return Ext.String.format('<a href="//www.ncbi.nlm.nih.gov/protein/{0}" target="_blank">{0}</a>', value);
			break;
		case "PATRIC_VF":
			return Ext.String.format('<a href="SpecialtyGeneEvidence?source=PATRIC_VF&sourceId={0}">{0}</a>', value);
			break;
		case "VFDB":
			return Ext.String.format('<a href="//www.mgc.ac.cn/cgi-bin/VFs/gene.cgi?GeneID={0}" target="_blank">{0}</a>', value);
			break;
		case "Victors":
			return Ext.String.format('<a href="//www.phidias.us/victors/gene_detail.php?c_mc_victor_id={0}" target="_blank">{0}</a>', value);
			break;
		default: 
			return value;
	}
}

//Download File
//BEGIN

function DownloadFile(type) {"use strict";

	var Page = $Page, property = Page.getPageProperties(), form = Ext.getDom("fTableForm"), name = property.name, tree = property.tree;

	if (tree.getSelectedTerms()["Keyword"] == null) {
		tree.selectedTerm["Keyword"] = Ext.getDom("keyword").value;
	}

	form.action = "/patric-searches-and-tools/jsp/grid_download_handler.jsp";
	form.download_keyword.value = constructKeyword(tree.getSelectedTerms(), name);
	form.fileformat.value = arguments[0];
	form.target = "";
	getHashFieldsToDownload(form);
	form.submit();
}
