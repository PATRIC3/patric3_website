function loadFBCD() {
	var Page = $Page, property = Page.getPageProperties(), hash = property.hash, checkbox = Page.getCheckBox() || null, plugin = property.plugin, plugintype = property.plugintype, which = hash.hasOwnProperty('cat') ? hash.cat : hash.aT ? hash.aT : 0, header, requested_data;

	(plugin && plugintype == "checkbox" && !checkbox) ? checkbox = Page.checkbox = createCheckBox(property.name) : checkbox.updateCheckAllIcon();


	if (hash.to == "refseq_locus_tag") {
		header = "RefSeq Locus Tag";
		requested_data = hash.to;
	}
	else if (hash.to == "protein_id") {
	    header = "Protein ID";
	    requested_data = hash.to;
	}
	else if (hash.to == "gene_id") {
	    header = "Gene ID";
	    requested_data = hash.to;
	}
	else if (hash.to == "gi") {
	    header = "GI";
	    requested_data = hash.to;
	}
	else if (hash.to == "feature_id") {
		header = "PATRIC ID";
		requested_data = hash.to;
	}
    else if (hash.to == "alt_locus_tag") {
        header = "Alt Locus Tag";
        requested_data = hash.to;
    }
	else if (hash.to == "patric_id") {
		if (hash.from == "refseq_locus_tag") {
		    header = "RefSeq Locus Tag";
			requested_data = hash.from;
		}
		else if (hash.from == "protein_id") {
		    header = "RefSeq";
			requested_data = hash.from;
		}
		else if (hash.from == "gene_id") {
		    header = "Gene ID";
			requested_data = hash.from;
		}
		else if (hash.from == "gi") {
		    header = "GI";
			requested_data = hash.from;
		}
		else if (hash.from == "feature_id") {
		    header = "Feature ID";
			requested_data = hash.from;
		}
		else if (hash.from == "alt_locus_tag") {
		    header = "Alt Locus Tag";
			requested_data = hash.from;
		}
		else if (hash.from == "patric_id") {
		    header = "PATRIC ID";
		    requested_data = hash.from;
		}
		else {
		    header = hash.from;
			requested_data = "target";
		}
	}
	else {
		header = hash.to;
		requested_data = "target";
	}

	if (!property.scm[which]) {
			property.scm[which] =  [checkbox,
			    {header:'Genome Name', flex:2, dataIndex: 'genome_name', renderer:renderGenomeName},
                {header:'Accession', flex:1, align:'center', hidden: true, dataIndex: 'accession', renderer:renderAccession},
			    {header:'PATRIC ID', flex:1, align:'center', dataIndex: 'patric_id', renderer:renderSeedId},
			    {header:'RefSeq Locus Tag', flex:1, align:'center', dataIndex: 'refseq_locus_tag', renderer:renderLocusTag},
			    {header:'Alt Locus Tag', flex:1, align:'center', dataIndex: 'alt_locus_tag', renderer:renderLocusTag},
			    {header: header, flex:1, align:'center', dataIndex: requested_data, renderer:renderURL},
				{header:'Gene Symbol', flex:1, align:'center', dataIndex: 'gene', renderer:BasicRenderer},
			    {header:'Genome Browser', flex:1, hidden: true, dataIndex:'feature_id', align:'center', renderer:renderGenomeBrowserByFeatureIDMapping},
				{header:'Annotation', flex:1, align:'center', hidden:true, dataIndex: 'annotation', renderer:BasicRenderer},
				{header:'Feature Type', flex:1, align:'center', hidden:true, dataIndex: 'feature_type', renderer:BasicRenderer},
			    {header:'Start', flex:1, hidden: true, dataIndex:'start', align:'center', renderer:BasicRenderer},
			    {header:'End', flex:1, hidden: true, dataIndex:'end', align:'center', renderer:BasicRenderer},
			    {header:'Length (NT)', flex:1, hidden: true, dataIndex:'na_length', align:'center', renderer:BasicRenderer},
			    {header:'Strand', flex:1, hidden: true, dataIndex:'strand',align:'center', renderer:BasicRenderer},
				{header:'FIGfam ID', flex:1, align:'center', dataIndex:'figfam_id', renderer:BasicRenderer},
				{header:'Protein ID', flex:1, align:'center', hidden: true, dataIndex:'protein_id', renderer:BasicRenderer},
				{header:'Length (AA)', flex:1, align:'center', hidden: true, dataIndex:'aa_length', renderer:BasicRenderer},
			    {header:'Product', flex:2, dataIndex:'product', renderer:BasicRenderer}]
	}
	loadMemStore();
}

function getExtraParams() {

	var Page = $Page, property = Page.getPageProperties(), hash = property.hash;

	return {
		pk : hash.key
	};
}

function getSelectedFeatures() {"use strict";

	var Page = $Page, property = Page.getPageProperties(), sl = Page.getCheckBox().getSelections(), i, fids = property.fids;

	for ( i = 0; i < sl.length; i++)
		fids.push(sl[i].data.feature_id);

}

function DownloadFile() {"use strict";

	var form = Ext.getDom("fTableForm");

	// form.action = "/patric-searches-and-tools/jsp/grid_download_handler.jsp";
	form.action = "/portal/portal/patric/IDMapping/IDMappingWindow?action=b&cacheability=PAGE&sraction=download";
	form.fileformat.value = arguments[0];
	form.target = "";
	getHashFieldsToDownload(form);
	form.submit();
}

function renderGenomeBrowserByFeatureIDMapping(value, p, record) {

	var tracks = "DNA,PATRICGenes,RefSeqGenes", Page = $Page, property = Page.getPageProperties(), hash = property.hash, window_start = Math.max(0, (record.data.start_max - 1000)), window_end = parseInt(record.data.end_min) + 1000;

	return Ext.String.format('<a href="GenomeBrowser?cType=feature&cId={0}&loc={1}:{2}..{3}&tracks={4}"><img src="/patric/images/icon_genome_browser.gif"  alt="Genome Browser" style="margin:-4px" /></a>', value, record.data.accession, window_start, window_end, tracks);
}

function CallBack() {
	var Page = $Page, property = Page.getPageProperties(), hash = property.hash, which = hash.hasOwnProperty('cat') ? hash.cat : hash.aT ? hash.aT : 0, store = Page.getStore(which), requested_data = "id";

	Ext.getDom('grid_result_summary').innerHTML = "<b>" + store.totalCount + " features found</b>"
}

function renderURL(value, p, record) {
	var Page = $Page, property = Page.getPageProperties();

	if (property.renderURL)
		return Ext.String.format("<a href=\"" + property.renderURL + "{0}\" target=\"_blank\">{0}</a>", value);
	else
		return value;
}

