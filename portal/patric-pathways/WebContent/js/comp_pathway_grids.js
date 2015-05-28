Ext.define('Pathway', {
		extend: 'Ext.data.Model',
		fields: [
			{name:'idx',	type:'string'},
			{name:'pathway_id',	type:'string'},
			{name:'pathway_name',	type:'string'},
			{name:'pathway_class',	type:'string'},
			{name:'genome_count',	type:'int'},
			{name:'ec_count',	type:'int'},
			{name:'gene_count',		type:'int'},
			{name:'ec_cons',	type:'int'},
			{name:'gene_cons',	type:'int'},
			{name:'algorithm',	type:'string'}
		]
	});

Ext.define('Ec', {
	extend: 'Ext.data.Model',
	fields: [
		{name:'idx',	type:'string'},
		{name:'pathway_id',	type:'string'},
		{name:'pathway_name',	type:'string'},
		{name:'pathway_class',	type:'string'},
		{name:'algorithm',	type:'string'},
		{name:'ec_number',	type:'string'},
		{name:'ec_name',		type:'string'},
		{name:'genome_count',	type:'int'},
		{name:'gene_count',	type:'int'},
		{name:'algorithm',	type:'string'}
	]
});

Ext.define('Feature', {
	extend: 'Ext.data.Model',
	fields: [
		{name:'idx',	type:'string'},
		{name:'genome_name',	type:'string'},
		{name:'accession',	type:'string'},
		{name:'alt_locus_tag',	type:'string'},
		{name:'patric_id',    type:'string'},
		{name:'feature_id',	type:'string'},
		{name:'algorithm',	type:'string'},
		{name:'gene',		type:'string'},
		{name:'product',	type:'string'},
		{name:'pathway_id',	type:'string'},
		{name:'pathway_name',	type:'string'},
		{name:'ec_number',	type:'string'},
		{name:'ec_name',	type:'string'},
		{name:'genome_id',	type:'string'}
	]
});


function createLayout(){
	var Page = $Page,
		property = Page.getPageProperties(),
		hash = property.hash;
	
	Ext.create('Ext.panel.Panel', {
		 id: 'tabLayout',
		 border: false,
		 autoScroll: false,
		 items:[{
			layout: 'fit',
			region:'north',
			border: true,
			height: 22,
			xtype: 'tabpanel',
			id: 'tabPanel',
			items: [{title:"Pathways", id:"0"},{title:"EC Numbers", id:"1"},{title:"Genes", id:"2"}],
			ClickFromTab: true, 
			listeners: {
				'tabchange': function(tabPanel, tab){
						hash.cwP = (hash.cwP && hash.cwP == "true")?true:false,
						hash.cwEC = (hash.cwEC && hash.cwEC == "true")?true:false;
						if(property.pageType == "Finder")KeepParameters();
						if(!this.ClickFromTab){
							// loadGrid();
							loadMemStore();
			        		this.ClickFromTab = true;
			        	}else{
			        		hash.aT = parseInt(tab.getId());
			        		hash.aP[tab.getId()] = 1,
				        	createURL();	
			        	}
					}
				}
			},{
				region:'center',
				id: 'centerPanel',
			 	html: '<div id="grid_result_summary">Loading...</div>',
				padding: '6 8',
				style: 'background-color:#DDE8F4',
				bodyStyle: 'background-color:#DDE8F4',
				bodyCls: 'bold',
				border: false
			}
		],
		renderTo: 'sample-layout'
	});
}

function createLoadComboBoxes(){
	
	var Page = $Page,
		property = Page.getPageProperties(),
		hash = property.hash;
	
	Ext.create('Ext.form.ComboBox', {
		id:'cb_pClass',
		renderTo: 'f_pathway_class',
		fieldLabel: 'Pathway Class',
	    displayField: 'name',
	    valueField: 'name',
	    width: 335,
	    labelWidth: 90,
	    editable: false,
	    typeHead: true,
	    store: Ext.create('Ext.data.Store', {
	    	fields:['name', 'value']
	    }),
	    queryMode: 'local',
	    listeners:{
	         'select': function(combo, records, eOpts){
	        	 hash.pClass = records[0].get("value").toLowerCase() == "all"?"":records[0].get("value");
	        	 loadCombo("cb_pId", "pathway");
	        	 loadCombo("cb_ecN", "ec");
	        	 loadCombo("cb_alg", "algorithm");
	         }
    	}
	});
	
	Ext.create('Ext.form.ComboBox', {
		id:'cb_pId',
		renderTo: 'f_pathway_name',
		fieldLabel: 'Pathway Name',
	    displayField: 'name',
	    valueField: 'value',
	    width: 305,
	    labelWidth: 90,
	    editable: false,
	    typeHead: true,
	    store: Ext.create('Ext.data.Store', {
	    	fields:['name', 'value']
	    }),
	    queryMode: 'local',
	    listeners:{
	         'select': function(combo, records, eOpts){
	        	hash.pId = records[0].get("value").toLowerCase() == "all"?"":records[0].get("value");
	        	//loadCombo("cb_pClass", "parent");
	        	loadCombo("cb_ecN", "ec");
	        	loadCombo("cb_alg", "algorithm");
	         }
	    }
	});
	
	Ext.create('Ext.form.ComboBox', {
		id:'cb_ecN',
		renderTo: 'f_ec_number',
		fieldLabel: 'EC Number',
	    displayField: 'name',
	    valueField: 'name',
	    width: 165,
	    labelWidth: 70,
	    editable: false,
	    typeHead: true,
	    store: Ext.create('Ext.data.Store', {
	    	fields:['name', 'value']
	    }),
	    queryMode: 'local',
	    listeners:{
	         'select': function(combo, records, eOpts){
	        	hash.ecN = records[0].get("value").toLowerCase() == "all"?"":records[0].get("value");
	        	loadCombo("cb_alg", "algorithm");
	         }
	    }
	});
	
	Ext.create('Ext.form.ComboBox', {
		id:'cb_alg',
		renderTo: 'f_algorithm',
		fieldLabel: 'Annotation',
	    displayField: 'name',
	    valueField: 'value',
	    width: 175,
	    labelWidth: 60,
	    editable: false,
	    typeHead: true,
	    store: Ext.create('Ext.data.Store', {
	    	fields:['name', 'value'],
	    	data:[{"name":"ALL", "value":"ALL"},
	    	      {"name":"PATRIC", "value":"PATRIC"},
	    	      {"name":"RefSeq", "value":"RefSeq"},
	    	      {"name":"BRC1", "value":"BRC1"}]
	    }),
	    queryMode: 'local',
	    listeners:{
	    	'select': function(combo, records, eOpts){
	    		hash.alg = records[0].get("value").toLowerCase() == "all"?"":records[0].get("value");
	    	}
	    }
	});
	
	
	loadCombo("cb_pClass", "parent");
	loadCombo("cb_pId", "pathway");
	loadCombo("cb_ecN", "ec");
	loadCombo("cb_alg", "algorithm");
	
}

function loadFBCD(){
	var tabs = Ext.getCmp("tabPanel"),
		id = tabs.getActiveTab().getId(),
		Page = $Page,
		property = Page.getPageProperties(),
		hash = property.hash;
			
	SetLoadParameters();
	
	if(property.pageType == "Table")
		SetComboBoxes();
		
	if (hash.aT == parseInt(id)) {
		// loadGrid()
		loadMemStore();
	}
	else{
		tabs.ClickFromTab = false;
		tabs.setActiveTab(hash.aT);
	}
	
	Ext.getDom("grid_result_summary").innerHTML = "Loading...";
}

function getExtraParams(){
	var Page = $Page,
		property = Page.getPageProperties(),
		hash = property.hash,
		which = hash.aT;
	
	return (property.pageType == "Table")?{
		cId: Ext.getDom("cId").value,
    	cType: Ext.getDom("cType").value,
    	algorithm: hash.alg,
    	pathway_class: hash.pClass,
    	pathway_id: hash.pId,
    	ec_number: hash.ecN,
    	need: which
	}:{
		pk: property.pk?property.pk:"",
    	algorithm: hash.alg,
    	pathway_id: hash.pId,
    	ec_number: hash.ecN,
    	need: which
	};
	
}

function CallBack(){
	var Page = $Page;

	var property = Page.getPageProperties(), hash = property.hash
	var summary = Ext.getDom('grid_result_summary');
    var uniqueCount = Page.getGrid().store.proxy.reader.rawData.unique;
    if (hash.aT == 0) {
        summary.innerHTML = "<b> " + uniqueCount + " unique pathway(s) found</b><br/>";
    }
    else if (hash.aT == 1) {
        summary.innerHTML = "<b>" + uniqueCount + " unique EC Number(s) found</b><br/>";
    }
    else if (hash.aT == 2) {
        summary.innerHTML = "<b>" + uniqueCount + " unique gene(s) found</b><br/>";
    }

	if(Page.getGrid().sortchangeOption)
		Page.getGrid().setSortDirectionColumnHeader();
}

function ShowECTab(pathway_id, pathway_name, pathway_class, algorithm){

	var Page = $Page,
		property = Page.getPageProperties(),
		hash = property.hash;
	
	hash.aT = 1,
	hash.aP[1] = 1,
	hash.pId = pathway_id,
	(property.gridType == "Table")?hash.pClass = pathway_class:"",
    hash.alg = algorithm,
	hash.cwP = true,
	hash.cwEC = false,
	(property.pageType == "Finder" && Ext.getDom("search_on").value == "Ec_Number")?"":hash.ecN = '',
	(property.pageType == "Finder")?property.breadcrumbParams.pName = pathway_name:"";
	(property.gridType == "Table")?SetComboBoxes():"";
	property.reconfigure = true;
	createURL();
}

function ShowFeatureTab(pathway_id, pathway_name, pathway_class, ec_number, algorithm){

	var Page = $Page,
		property = Page.getPageProperties(),
		hash = property.hash;

	hash.aT = 2,
	hash.aP[2] = 1,
	hash.pId = pathway_id,
	(property.gridType == "Table")?hash.pClass = pathway_class:"",
    hash.alg = algorithm,
	hash.cwP = (ec_number?false:true),
	hash.cwEC = (ec_number?true:false),
	ec_number?hash.ecN = ec_number:"",
	(property.pageType == "Finder")?property.breadcrumbParams.pName = pathway_name:"",
	(property.pageType == "Finder")?property.breadcrumbParams.ecN = ec_number:"";
	(property.gridType == "Table")?SetComboBoxes():"";
	property.reconfigure = true;
	createURL();
}

function filter() {

	var Page = $Page,
		property = Page.getPageProperties(),
		hash = property.hash,
		alg = Ext.getCmp('cb_alg').getValue(),
		pId = Ext.getCmp('cb_pId').getValue(),
		pClass = Ext.getCmp('cb_pClass').getValue(),
		ecN = Ext.getCmp('cb_ecN').getValue();
	
	hash.aP[hash.aT] = 1,
	hash.cwP = (pId.toLowerCase() == "all")?false:true,
	hash.cwEC = (ecN.toLowerCase() == "all")?false:true,
    hash.alg = alg,
	hash.pId = (pId.toLowerCase() == "all")?"":pId,
	hash.pClass = (pClass.toLowerCase() == "all")?"":pClass,
	hash.ecN = (ecN.toLowerCase() == "all")?"":ecN,
			
	createURL();
	loadMemStore(true);
}

function SetComboBoxes(){
	
	var hash = $Page.getPageProperties().hash,
		alg = Ext.getCmp('cb_alg'),
		pId = Ext.getCmp('cb_pId'),
		pClass = Ext.getCmp('cb_pClass'),
		ecN = Ext.getCmp('cb_ecN'),
		timeoutId = null;
	
	function setInputs(){
		if(pId.getStore().data.items.length > 0 && pClass.getStore().data.items.length  > 0 && ecN.getStore().data.items.length > 0){
            (hash.alg == "")?alg.setValue("ALL"):alg.setValue(hash.alg),
			(hash.pId == "")?pId.setValue("ALL"):pId.setValue(hash.pId),
			(hash.pClass == "")?pClass.setValue("ALL"):pClass.setValue(hash.pClass),
			(hash.ecN == "")?ecN.setValue("ALL"):ecN.setValue(hash.ecN);
			clearTimeout(timeoutId);
		}
	}
	
	if(pId.getStore().data.items.length == 0 && pClass.getStore().data.items.length == 0 && ecN.getStore().data.items.length == 0){
		timeoutId = setInterval(setInputs, 1000);
	}else{
		setInputs();
	}
	
}

function loadCombo(id, need){

	var obj = {},
		hash = $Page.getPageProperties().hash,
		list = {"pId":1, "pClass":1, "ecN":1, "alg":1};
	
	for(var i in hash)
		if(list[i] && hash.hasOwnProperty(i))
			obj[i] = hash[i];
	
	obj["cType"] = Ext.getDom("cType").value;
	obj["cId"] = Ext.getDom("cId").value;
	obj["need"] = need;
	
	Ext.Ajax.request({
	    url: "CompPathwayTable/CompPathwayTableWindow?action=b&cacheability=PAGE&need=filter",
	    method: 'POST',
	    params: {val:Ext.JSON.encode(obj)},
	    success: function(response, opts){
	    	var decoded = Ext.JSON.decode(response.responseText);
	    	decoded[need].sort(sortRowsData(id =="cb_pId"?"name":"value"));
	    	Ext.getCmp(id).getStore().loadData(decoded[need]);
	    }
	});
}

function sortRowsData(value) {
    return function (a, b) {
    	if(a[value] == "ALL")
    		return -1;
    	if(a[value] < b[value])
    		return -1;
    	if(a[value] > b[value])
    		return 1;
    	return 0;
    };
}

// render pathway name in pathway tab
function renderPathwayName(value, metadata, record, rowIndex, colIndex, store) {
	metadata.tdAttr = 'data-qtip="'+record.data.pathway_name+'" data-qclass="x-tip"';
	return Ext.String.format('<a href="CompPathwayMap?cType={0}&amp;cId={1}&amp;dm={2}&amp;feature_id={3}&amp;ec_number={4}&amp;map={5}&amp;algorithm={6}&amp;pk={7}">{8}</a>',
		Ext.getDom("cType").value, Ext.getDom("cId").value, '', '', '', record.data.pathway_id, record.data.algorithm, (Ext.getDom("pk")!=undefined?Ext.getDom("pk").value:''), record.data.pathway_name);
}

// render Unique Gene Count in pathway tab
function renderGeneCountPathway(value, metadata, record, rowIndex, colIndex, store) {
	metadata.tdAttr = 'data-qtip="'+record.data.gene_count+'" data-qclass="x-tip"';
	return Ext.String.format('<a href="javascript:void(0);" onclick=ShowFeatureTab("{0}","{1}","{2}","{3}","{4}"); />{5}</a>',
		record.data.pathway_id, record.data.pathway_name, record.data.pathway_class, '', record.data.algorithm, record.data.gene_count);
}

// render Unique EC Count in pathway tab
function renderAvgECCount(value, metadata, record, rowIndex, colIndex, store) {
	metadata.tdAttr = 'data-qtip="'+record.data.ec_count+'" data-qclass="x-tip"';
	return Ext.String.format('<a href="javascript:void(0);" onclick=ShowECTab("{0}","{1}","{2}","{3}"); />{4}</a>',
		record.data.pathway_id, record.data.pathway_name, record.data.pathway_class, record.data.algorithm, record.data.ec_count);
}

// render pathway name in ec tab
function renderPathwayEc(value, metadata, record, rowIndex, colIndex, store) {
	metadata.tdAttr = 'data-qtip="'+record.data.pathway_name+'" data-qclass="x-tip"';
	return Ext.String.format('<a href="CompPathwayMap?cType={0}&amp;cId={1}&amp;dm={2}&amp;feature_id={3}&amp;ec_number={4}&amp;map={5}&amp;algorithm={6}&amp;pk={7}">{8}</a>',
		Ext.getDom("cType").value, Ext.getDom("cId").value, 'ec', '', record.data.ec_number, record.data.pathway_id, record.data.algorithm, (Ext.getDom("pk")!=undefined?Ext.getDom("pk").value:''), record.data.pathway_name);
}

// render Unique Gene Count in ec tab
function renderGeneCountEc(value, metadata, record, rowIndex, colIndex, store) {
	metadata.tdAttr = 'data-qtip="'+record.data.gene_count+'" data-qclass="x-tip"';
	return Ext.String.format('<a href="javascript:void(0);" onclick=ShowFeatureTab("{0}","{1}","{2}","{3}","{4}"); />{5}</a>',
		record.data.pathway_id, record.data.pathway_name, record.data.pathway_class, record.data.ec_number, record.data.algorithm, record.data.gene_count);
}

// render pathway name in gene tab
function renderPathwayFeature(value, metadata, record, rowIndex, colIndex, store) {
	metadata.tdAttr = 'data-qtip="'+record.data.pathway_name+'" data-qclass="x-tip"';
	return Ext.String.format('<a href="CompPathwayMap?cType={0}&amp;cId={1}&amp;dm={2}&amp;feature_id={3}&amp;ec_number={4}&amp;map={5}&amp;algorithm={6}&amp;pk={7}">{8}</a>',
		Ext.getDom("cType").value, Ext.getDom("cId").value, 'feature', record.data.feature_id, '', record.data.pathway_id, record.data.algorithm, (Ext.getDom("pk")!=undefined?Ext.getDom("pk").value:''), record.data.pathway_name);
}


function getSelectedFeatures(actiontype, showdownload, fastatype, to){

	var pid = [],
		ecid = [],
		aid = [],
		cType = "",
		cId = "",
		Page = $Page,
		property = Page.getPageProperties(),
		hash = property.hash,
		sl = Page.getCheckBox().getSelections(),
		i,
		fids = property.fids;

	if (property.pageType == "Finder") {
		if(Ext.getDom("genomeId").value != ""){
			cId =  Ext.getDom("genomeId").value;
			cType = "genome";
		}
		else {
			cId =  Ext.getDom("taxonId").value;
			cType = "taxon";
		}
	}
	else {
		cType = Ext.getDom("cType").value;
		cId = Ext.getDom("cId").value;
	}

	if (hash.aT != "2") {

		for (i=0; i<sl.length; i++) {
			pid.push(sl[i].data.pathway_id);

			aid.push(sl[i].data.algorithm);

			if (hash.aT == "1")
				ecid.push(sl[i].data.ec_number);
		}

		if(Ext.getDom("ecN").value)
	    	ecid.push(Ext.getDom("ecN").value);

		Ext.Ajax.request({
		    url: "/portal/portal/patric/CompPathwayTable/CompPathwayTableWindow?action=b&cacheability=PAGE&need=getFeatureIds",
		    method: 'POST',
		    params: {cId:cId, cType:cType, map:pid.join(" OR "), algorithm:aid.join(" OR "), ec_number:ecid.join(" OR ")},
		    success: function(response, opts) {
		        var resp = Ext.JSON.decode(response.responseText);
                for (i = 0; i < resp.length; i++) {
                    fids.push(resp[i]);
                }
		    	// var na_features = Ext.JSON.decode(response.responseText);
		    	// for(i = 0; i < na_features.genes.length; i++)
		    	//	fids.push(na_features.genes[i].genes);
			}
		});
	} else {
		for (i=0; i<sl.length;i++) {
			fids.push(sl[i].data.feature_id);
		}
	}
}

function KeepParameters(){
	"use strict";

	var Page = $Page,
		property = Page.getPageProperties(),
		hash = property.hash;

	if(!(hash.cwP || hash.cwEC))
	    hash.alg = Ext.getDom("alg").value;
	if(Ext.getDom("search_on").value == "Ec_Number"){
		hash.ecN = Ext.getDom("ecN").value;
		hash.pId = "";
	}else if(Ext.getDom("search_on").value == "Map_ID"){
		hash.pId = Ext.getDom("pId").value;
		hash.ecN = "";
	}

}

function DownloadFile(){
	"use strict";

	if (isOverDownloadLimit()) {
		return false;
	}
	var form = Ext.getDom("fTableForm");

	form.action = "/portal/portal/patric/CompPathwayTable/CompPathwayTableWindow?action=b&cacheability=PAGE&need=download";
	form.fileformat.value = arguments[0];
	form.target = "";
	getHashFieldsToDownload(form);
	form.submit();
}
