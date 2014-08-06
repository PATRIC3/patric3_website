function createLayout(){
	var Page = $Page,
		property = Page.getPageProperties(),
		hash = property.hash;
	
	Ext.create('Ext.panel.Panel', {
		 id: 'tab-layout',
		 border: true,
		 monitorResize: true,
		 autoScroll:true,
		 items:[{
			layout: 'fit',
			region:'north',
			border: false,
			height: 22,
			xtype: 'tabpanel',
			id: 'tabs',
			items: [{title:"Virulence Genes", id:"0"},{title:"Virulence Gene Homologs", id:"1"}],
			ClickFromTab: true, 
			listeners: {
				'tabchange': function(tabPanel, tab){
					hash.cwVFG = (hash.cwVFG && hash.cwVFG == "true")?true:(false,hash.VFGId="");
					if(!this.ClickFromTab){
		        		loadGrid();
		        		this.ClickFromTab = true;
		        	}else{
		        		hash.aT = parseInt(tab.getId());
		        		hash.aP[tab.getId()] = 1,
			        	createURL();	
		        	}
				}
			}
		},{
			layout: 'fit',
			region:'center',
			id: 'center-panel',
		 	contentEl: 'grid_result_summary',
			padding: '6 8',
			style: 'background-color:#DDE8F4',
			bodyStyle: 'background-color:#DDE8F4',
			bodyCls: 'bold',
			border: false
		},{
			layout:'fit',
			region:'south',
			id:'south-panel',
			html:'<div id="PATRICGrid"></div>',
			height:571,
			border:false,
			autoScroll:true
		}],
		renderTo: 'sample-layout'
	});
	
	Ext.EventManager.onWindowResize(function() {
		var width = Ext.get('sample-layout').getWidth(),
			Page = $Page,
			grid = Page.getGrid();
	
		Ext.get('center-panel').setWidth(width);
		Ext.get('tabs').setWidth(width);
		Ext.get(Ext.get('tabs').dom.childNodes[0].id).setWidth(width);
		Ext.get(Ext.getDom('south-panel').childNodes[0].id).setWidth(width);
		Ext.get(Ext.getDom('tab-layout').childNodes[0].id).setWidth(width);
		grid.setWidth(width);
	});
}


function loadFBCD(){
	var tabs = Ext.getCmp("tabs"),
		id = tabs.getActiveTab().getId(),
		Page = $Page,
		property = Page.getPageProperties(),
		hash = property.hash;
			
	SetLoadParameters();
		
	if(hash.aT == parseInt(id)){
		loadGrid();
	}else{
		tabs.ClickFromTab = false;
		tabs.setActiveTab(hash.aT);
	}
}

function getExtraParams(){
	var Page = $Page,
		property = Page.getPageProperties(),
		hash = property.hash;
	
	return {
		type:getType(),
		name:getName(),
		cId:Ext.getDom("cId").value,
		vfgId:hash.VFGId?hash.VFGId:""
	};
}

function CallBack(){
		
	var Page = $Page;

	writeBreadCrumb(getType());
	if(Page.getGrid().sortchangeOption)
		Page.getGrid().setSortDirectionColumnHeader();
}


function getSelectedFeatures(){
	
	var vfgid = [],
		Page = $Page,
		property = Page.getPageProperties(),
		hash = property.hash,
		sl = Page.getCheckBox().getSelections(),
		i,
		fids = property.fids;
	
	if(hash.aT == "0"){
		
		for (i=0; i<sl.length; i++) {
			vfgid.push("'" + sl[i].data.vfg_id +"'");
		}
		
		Ext.Ajax.request({
		    url: "/patric-diseaseview/jsp/get_na_feature_ids.jsp",
		    method: 'POST',
		    params: {cId:Ext.getDom("cId").value, vfgId:vfgid.join(",")},
		    success: function(response, opts) {
		    	var na_features = Ext.JSON.decode(response.responseText);			
		    	for(i = 0; i < na_features.genes.length; i++)
		    		fids.push(na_features.genes[i].genes);	
		    					    
			}
		});
		
	}else{
		for (i=0; i<sl.length;i++) {
			fids.push(sl[i].data.na_feature_id);
		}
	}
}

function ShowFeatureTab(VFGId){
	var Page = $Page,
		property = Page.getPageProperties(),
		hash = property.hash;

	hash.aT = 1,
	hash.aP[1] = 1,
	hash.cwVFG = true,
	hash.VFGId = VFGId,
	property.reconfigure = true,
	createURL();
}

function DownloadFile() {
	"use strict";
	
	var form = Ext.getDom("fTableForm");
	
	form.action = "/patric-diseaseview/jsp/table_download_handler.jsp",
	form.target = "",
	form.fileformat.value = arguments[0];
	getHashFieldsToDownload(form);
	form.submit();
}

function renderDiseaseOverviewPubMed(value, metadata, record, rowIndex, colIndex, store) {
	
	metadata.tdAttr = 'data-qtip="PubMed" data-qclass="x-tip"';
	
	var title = record.data.Title;
	var authors = record.data.fullAuthorList;
	var journal_name = record.data.FullJournalName;
	var pub_date = record.data.PubDate;
	var pmid = record.data.pubmed_id;
	var strPub = Ext.String.format('<div class="bold">{0}</div><div class="far">{1}</div><div>{2} ({3})</div>', title, authors, journal_name, pub_date);
	var strPubmedLink;
	if (pmid) {
		strPubmedLink = Ext.String.format('<div>PubMed: <a href="http://view.ncbi.nlm.nih.gov/pubmed/{0}" target="_blank">{0}</a></div>', pmid);
	} else {
		
		strPubmedLink = ", PubMed: Not Available";
	}	
	return "<div style=\"white-space:normal\">"+strPub+strPubmedLink+"</div>";
}

function listenLinkColumn(a) {
	document.location.href = a.href;
}

function renderVFDB(value, metadata, record, rowIndex, colIndex, store) {

	var vfdb = value.split("<br>");
	var taxons = record.data.taxon_id.split("<br>");
	
	var i = 0;
	
	var text = "";
	if(vfdb.length - 1 > 0)
		text += '<div id="multiple_lines_'+(multiple_lines++)+'" style="height:'+vfdb.length*18+'px">';
	
	for(i=0; i< vfdb.length - 1; i++){
		
		if(vfdb[i] != "0")
			text += "<a href=\"DiseaseTable?cType="+Ext.getDom("cType").value+"&cId="+taxons[i]+"&type=vfdb\" onclick=\"listenLinkColumn(this)\">"+vfdb[i]+"</a><br>";
		else
			text += "0" + "<br>"; 
	}
	
	if(vfdb[i] != "0")
		text += "<a href=\"DiseaseTable?cType="+Ext.getDom("cType").value+"&cId="+taxons[vfdb.length - 1]+"&type=vfdb\" onclick=\"listenLinkColumn(this)\">"+vfdb[vfdb.length - 1]+"</a><br>";
	else 
		text += "0";
	
	
	if(vfdb.length - 1 > 0)
		text += "</div>";
	
	return Ext.String.format(text);
	
	
}

function renderFeatureCount(value, metadata, record, rowIndex, colIndex, store) {
	
	if(record.data.feature_count > 0){
		metadata.tdAttr = 'data-qtip="'+record.data.feature_count+'" data-qclass="x-tip"';
		return Ext.String.format("<a href=\"javascript:void(0);\" onclick=\"ShowFeatureTab('"+record.data.vfg_id+"');\"/>"+record.data.feature_count+"</a>");
	}else{
		metadata.tdAttr = 'data-qtip="'+record.data.feature_count+'" data-qclass="x-tip"';
		return Ext.String.format(record.data.feature_count);
	}
}

function renderVFG(value, metadata, record, rowIndex, colIndex, store) {
	metadata.tdAttr = 'data-qtip="'+value+'" data-qclass="x-tip"';
	return Ext.String.format("<a href=\"http://www.mgc.ac.cn/cgi-bin/VFs/gene.cgi?GeneID={0}\" target=\"_blank\">{1}</a>", value, value);
	
}

function renderVF(value, metadata, record, rowIndex, colIndex, store) {
	metadata.tdAttr = 'data-qtip="'+value+'" data-qclass="x-tip"';
	return Ext.String.format("<a href=\"http://www.mgc.ac.cn/cgi-bin/VFs/vfs.cgi?VFID={0}\" target=\"_blank\">{1}</a>", value, value);
	
}

function renderGenome(value, metadata, record, rowIndex, colIndex, store) {

	var genome = value.split("<br>");
	var taxons = record.data.taxon_id.split("<br>");
		
	var i = 0;
	
	var text = "";
	
	if(genome.length - 1 > 0)
		text += '<div id="multiple_lines_'+(multiple_lines++)+'" style="height:'+genome.length*18+'px">';
	
	
	for(i=0; i< genome.length - 1; i++){
		
		if(genome[i] != "0")
			text += "<a href=\"GenomeList?cType=taxon&cId="+taxons[i]+ "&displayMode=&dataSource=RAST&pk=&kw=\" onclick=\"listenLinkColumn(this)\">"+genome[i]+"</a><br>";
		else
			text += "0" + "<br>"; 
	}
	
	if(genome[i] != "0")
		text += "<a href=\"GenomeList?cType=taxon&cId="+taxons[genome.length - 1]+ "&displayMode=&dataSource=RAST&pk=&kw=\" onclick=\"listenLinkColumn(this)\">"+genome[genome.length - 1]+"</a><br>";
	else 
		text += "0";
	
	if(genome.length - 1 > 0)
		text += "</div>";
	
	return Ext.String.format(text);
}

function renderCTD(value, metadata, record, rowIndex, colIndex, store) {

	var ctd = value.split("<br>");
	var taxons = record.data.taxon_id.split("<br>");
		
	var i = 0;
	
	var text = "";
	
	if(ctd.length - 1 > 0)
		text += '<div id="multiple_lines_'+(multiple_lines++)+'" style="height:'+ctd.length*18+'px">';
	
	
	for(i=0; i< ctd.length - 1; i++){
		
		if(ctd[i] != "0")
			text += "<a href=\"DiseaseTable?cType="+Ext.getDom("cType").value+"&cId="+taxons[i]+"&name="+record.data.disease_name+"&type=ctd\" onclick=\"listenLinkColumn(this)\">"+ctd[i]+"</a><br>";
		else
			text += "0" + "<br>"; 
	}
	
	if(ctd[i] != "0")
		text += "<a href=\"DiseaseTable?cType="+Ext.getDom("cType").value+"&cId="+taxons[ctd.length - 1]+"&name="+record.data.disease_name+"&type=ctd\" onclick=\"listenLinkColumn(this)\">"+ctd[ctd.length - 1]+"</a><br>";
	else 
		text += "0";
	
	if(ctd.length - 1 > 0)
		text += "</div>";
	
	return Ext.String.format(text);
}

function renderGAD(value, metadata, record, rowIndex, colIndex, store) {

	var gad = value.split("<br>");
	var taxons = record.data.taxon_id.split("<br>");
	
	var i = 0;
	
	var text = "";
	
	if(gad.length - 1 > 0)
		text += '<div id="multiple_lines_'+(multiple_lines++)+'" style="height:'+gad.length*18+'px">';
	
	for(i=0; i< gad.length - 1; i++){
		
		if(gad[i] != "0")
			text += "<a href=\"DiseaseTable?cType="+Ext.getDom("cType").value+"&cId="+taxons[i]+"&name="+record.data.disease_name+"&type=gad\" onclick=\"listenLinkColumn(this)\">"+gad[i]+"</a><br>";
		else
			text += "0" + "<br>"; 
	}
	
	if(gad[i] != "0")
		text += "<a href=\"DiseaseTable?cType="+Ext.getDom("cType").value+"&cId="+taxons[gad.length - 1]+"&name="+record.data.disease_name+"&type=gad\" onclick=\"listenLinkColumn(this)\">"+gad[gad.length - 1]+"</a><br>";
	else 
		text += "0";
	
	if(gad.length - 1 > 0)
		text += "</div>";
	
	return Ext.String.format(text);
	
}

function renderAssociation(value, metadata, record, rowIndex, colIndex, store) {

	if(record.association == 'Y'){
		metadata.tdAttr = 'data-qtip="Confirmed" data-qclass="x-tip"';
		return Ext.String.format('Confirmed');
	}else{
		metadata.tdAttr = 'data-qtip="Unconfirmed" data-qclass="x-tip"';
		return Ext.String.format('Unconfirmed');
	}
}

function renderMesH(value, metadata, record, rowIndex, colIndex, store) {
	return Ext.String.format("<a href=\"http://www.nlm.nih.gov/cgi/mesh/2011/MB_cgi?field=uid&term={0}\" target=\"_blank\">{1}</a>", record.data.disease_id, record.data.disease_name);
}

function renderMesHOverview(value, metadata, record, rowIndex, colIndex, store) {
	return Ext.String.format("<a href=\"http://www.nlm.nih.gov/cgi/mesh/2011/MB_cgi?field=uid&term={0}\" target=\"_blank\" onclick=\"window.open(this.href);\">{1}</a>", record.data.disease_id, record.data.disease_name);
}

function renderGenomeName(value, metadata, record, rowIndex, colIndex, store) {
	
	var values = value.split("<br>");
	var taxons = record.data.taxon_id.split("<br>");
	
	var i = 0;
	
	var text = "";
	if(values.length - 1 > 0)
		text += '<div id="multiple_lines_'+(multiple_lines++)+'" style="height:'+values.length*18+'px">';
	
	for(i=0; i< values.length - 1; i++){
		
		text += "<a href=\"Taxon?cType=taxon&cId="+taxons[i]+"\" onclick=\"listenLinkColumn(this)\">"+values[i]+"</a><br> ";
		
	}
	
	text += "<a href=\"Taxon?cType=taxon&cId="+taxons[values.length - 1]+"\" onclick=\"listenLinkColumn(this)\">"+values[values.length - 1]+"</a><br> ";
	
	if(values.length - 1 > 0)
		text += "</div>";
	
	return Ext.String.format(text);
}

function renderGenomeNameVFDB(value, metadata, record, rowIndex, colIndex, store) {
	metadata.tdAttr = 'data-qtip="'+value+'" data-qclass="x-tip"';
	
	return Ext.String.format("<a href=\"Genome?cType=genome&cId={0}\">{1}</a>", record.data.genome_info_id, value);
	
}

function renderXREF(value, metadata, record, rowIndex, colIndex, store) {

		return Ext.String.format("<a href=\"http://geneticassociationdb.nih.gov/cgi-bin/tableview.cgi?table=diseaseview&cond=upper(GENE)='{0}'\" target=\"_blank\">GAD</a>  <a href=\"http://www.ncbi.nlm.nih.gov/gene/{1}\" target=\"_blank\">NCBI</a>  <a href=\"http://www.genecards.org/cgi-bin/carddisp.pl?gene={2}\" target=\"_blank\">GeneCards</a>", record.data.gene_sym, record.data.gene_id, record.data.gene_sym);
}

function renderXREFCTD(value, p, record){

	return Ext.String.format("<a href=\"http://ctd.mdibl.org/detail.go?type=gene&db=GENE&acc={0}\" target=\"_blank\">CTD</a>  <a href=\"http://www.ncbi.nlm.nih.gov/gene/{1}\" target=\"_blank\">NCBI</a>  <a href=\"http://www.genecards.org/cgi-bin/carddisp.pl?gene={2}\" target=\"_blank\">GeneCards</a>", record.data.gene_id, record.data.gene_id, record.data.gene_sym);
}

function createToolbar_Graph(){
	
	topToolbar_graph = new Ext.Toolbar({
		items:[{
		   	text: 'Filter',
		   	xtype:'splitbutton',
			menu: [{
			  text: 'Show All',
				  handler: function(){
				 		idvgraph.filterDecorators();
					}
			},{
			  text: 'Hide Host Genes',
				  handler: function(){
						idvgraph.filterDecorators("hg");
					}
			},{
			  text: 'Hide Virulence Genes',
				  handler: function(){
						idvgraph.filterDecorators("vf");
					} 
			},{
			  text: 'Hide All Genes',
				  handler: function(){
						idvgraph.filterDecorators("all");
					}
			}],
			handler: function(){
				// scope: this
			}
			},{
		   	text: 'View',
		   	xtype:'splitbutton',
			menu: [{
			  text: 'Zoom to Fit',
				handler: function(){
					idvgraph.zoomToFit();
				}
			},{
			  text: 'Show/Hide Node Labels',
			  handler: function(){
					idvgraph.toggleNodeLabels();
			}
			}]},{
		   	text: 'Layout',
		   	xtype:'splitbutton',
			menu: [{
			  text: 'Mirrored Trees',
			  handler: function(){
						idvgraph.chLayout("mt");
				}
			},{
			  text: 'Force-directed',
				  handler: function(){
						idvgraph.chLayout("fd");
					}
			}]},{
		   	text: 'Export',
		   	xtype:'splitbutton',
			menu: [{
			  text: 'PDF (.pdf)',
				  handler: function(){
						idvgraph.exportAs({ format: "pdf", w: 1920, h: 1080});
					}
			},{
			  text: 'PNG (.png)',
				  handler: function(){
						idvgraph.exportAs({ format: "png", w: 1920, h: 1080 });
					}
			},{
			  text: 'XGMML (.xgmml)',
				  handler: function(){
						idvgraph.exportAs({ format: "xgmml" });
					}
			}]}, '-', 
			{
		   	text: 'Help',
		   	xtype:'splitbutton',
			menu: [{
			  text: 'FAQ',
			  handler: function(){
			    window.open ("http://enews.patricbrc.org/faqs/", "_new","menubar=1,resizable=1,scrollbars=1, fullscreen=1, toolbar=1,titlebar=1,status=1"); 
			  }
			},{
				text: 'Show Graph Legend',
				  handler: function(){ 
					Ext.getCmp('graph_west-panel').expand();
				  }
			}]
		}]
	});
}