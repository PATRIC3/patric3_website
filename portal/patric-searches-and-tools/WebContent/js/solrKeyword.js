Ext.define('Feature', {
	extend : 'Ext.data.Model',
	fields : [{
		name : 'genome_id',
		type : 'string'
	}, {
		name : 'genome_name',
		type : 'string'
	}, {
		name : 'sequence_id',
		type : 'string'
	}, {
		name : 'accession',
		type : 'string'
	}, {
		name : 'alt_locus_tag',
		type : 'string'
	}, {
		name : 'feature_id',
		type : 'string'
	}, {
		name : 'annotation',
		type : 'string'
	}, {
		name : 'feature_type',
		type : 'string'
	}, {
		name : 'start',
		type : 'int'
	}, {
		name : 'end',
		type : 'int'
	}, {
		name : 'na_length',
		type : 'int'
	}, {
		name : 'strand',
		type : 'string'
	}, {
		name : 'protein_id',
		type : 'string'
	}, {
		name : 'aa_length',
		type : 'int'
	}, {
		name : 'gene',
		type : 'string'
	}, {
		name : 'product',
		type : 'string'
	}, {
		name : 'refseq_locus_tag',
		type : 'string'
	}, {
		name : 'seed_id',
		type : 'string'
	}, {
		name : 'highlight'
	}, {
		name : 'figfam_id',
		type : 'string'
	}]
});

Ext.define('Genome', {
	extend : 'Ext.data.Model',
	fields : [{
		name : 'genome_name',
		type : 'string'
	}, {
		name : 'p2_genome_id',
		type : 'string'
	}, {
		name : 'genome_id',
		type : 'string'
	}, {
		name : 'genome_status',
		type : 'string'
	}, {
		name : 'isolation_country',
		type : 'string'
	}, {
		name : 'host_name',
		type : 'string'
	}, {
		name : 'oxygen_requirement',
		type : 'string'
	}, {
		name : 'sporulation',
		type : 'string'
	}, {
		name : 'temperature_range',
		type : 'string'
	}, {
		name : 'disease',
		type : 'string'
	}, {
		name : 'habitat',
		type : 'string'
	}, {
		name : 'motility',
		type : 'string'
	}, {
		name : 'sequences',
		type : 'int'
	}, {
		name : 'collection_date',
		type : 'string'
	}, {
		name : 'mlst',
		type : 'string'
	}, {
		name : 'other_typing',
		type : 'string'
	}, {
		name : 'genome_length',
		type : 'int'
	}, {
		name : 'complete',
		type : 'string'
	}, {
		name : 'patric_cds',
		type : 'int'
	}, {
		name : 'brc1_cds',
		type : 'int'
	}, {
		name : 'refseq_cds',
		type : 'int'
	}, {
		name : 'chromosomes',
		type : 'int'
	}, {
		name : 'plasmids',
		type : 'int'
	}, {
		name : 'contigs',
		type : 'int'
	}, {
		name : 'taxon_id',
		type : 'int'
	}, {
		name : 'organism_name',
		type : 'string'
	}, {
		name : 'strain',
		type : 'string'
	}, {
		name : 'serovar',
		type : 'string'
	}, {
		name : 'biovar',
		type : 'string'
	}, {
		name : 'pathovar',
		type : 'string'
	}, {
		name : 'culture_collection',
		type : 'string'
	}, {
		name : 'type_strain',
		type : 'string'
	}, {
		name : 'sequencing_centers',
		type : 'string'
	}, {
		name : 'completion_date',
		type : 'string'
	}, {
		name : 'publication',
		type : 'string'
	}, {
		name : 'completion_date',
		type : 'string'
	}, {
		name : 'bioproject_accession',
		type : 'string'
	}, {
		name : 'biosample_accession',
		type : 'string'
	}, {
		name : 'assembly_accession',
		type : 'string'
	}, {
		name : 'genbank_accessions',
		type : 'string'
	}, {
		name : 'refseq_accessions',
		type : 'string'
	}, {
		name : 'sequencing_status',
		type : 'string'
	}, {
		name : 'sequencing_platform',
		type : 'string'
	}, {
		name : 'sequencing_depth',
		type : 'string'
	}, {
		name : 'assembly_method',
		type : 'string'
	}, {
		name : 'gc_content',
		type : 'string'
	}, {
		name : 'isolation_site',
		type : 'string'
	}, {
		name : 'isolation_source',
		type : 'string'
	}, {
		name : 'isolation_comments',
		type : 'string'
	}, {
		name : 'geographic_location',
		type : 'string'
	}, {
		name : 'latitude',
		type : 'string'
	}, {
		name : 'longitude',
		type : 'string'
	}, {
		name : 'altitude',
		type : 'string'
	}, {
		name : 'depth',
		type : 'string'
	}, {
		name : 'other_environmental',
		type : 'string'
	}, {
		name : 'host_gender',
		type : 'string'
	}, {
		name : 'host_age',
		type : 'string'
	}, {
		name : 'host_health',
		type : 'string'
	}, {
		name : 'body_sample_site',
		type : 'string'
	}, {
		name : 'body_sample_subsite',
		type : 'string'
	}, {
		name : 'other_clinical',
		type : 'string'
	}, {
		name : 'antimicrobial_resistance',
		type : 'string'
	}, {
		name : 'antimicrobial_resistance_evidence',
		type : 'string'
	}, {
		name : 'gram_stain',
		type : 'string'
	}, {
		name : 'cell_shape',
		type : 'string'
	}, {
		name : 'temperature_range',
		type : 'string'
	}, {
		name : 'optimal_temperature',
		type : 'string'
	}, {
		name : 'salinity',
		type : 'string'
	}, {
		name : 'disease',
		type : 'string'
	}, {
		name : 'comments',
		type : 'string'
	}, {
		name : 'additional_metadata',
		type : 'string'
	}, {
		name : 'highlight'
	}]
});

Ext.define('Sequence', {
	extend : 'Ext.data.Model',
	fields : [{
		name : 'genome_name',
		type : 'string'
	}, {
		name : 'genome_id',
		type : 'string'
	}, {
		name : 'sequence_id',
		type : 'string'
	}, {
		name : 'accession',
		type : 'string'
	}, {
		name : 'length',
		type : 'string'
	}, {
		name : 'sequence_type',
		type : 'string'
	}, {
		name : 'topology',
		type : 'string'
	}, {
		name : 'gc_content',
		type : 'float'
	}, {
		name : 'description',
		type : 'string'
	}, {
		name : 'highlight'
	}]
});

Ext.define('Taxonomy', {
	extend : 'Ext.data.Model',
	fields : [{
		name : 'genomes',
		type : 'string'
	}, {
		name : 'taxon_id',
		type : 'string'
	}, {
		name : 'taxon_name',
		type : 'string'
	}, {
		name : 'taxon_rank',
		type : 'string'
	}, {
		name : 'highlight'
	}]
});


Ext.define('Cart', {
	extend : 'Ext.data.Model',
	fields : [{
		name : 'feature_id',
		type : 'string'
	}, {
		name : 'genome_id',
		type : 'string'
	}, {
		name : 'gid',
		type : 'string'
	}]
});

Ext.define('Experiment', {
	extend : 'Ext.data.Model',
	idProperty : 'expid',
	fields : [{
		name : 'accession',
		type : 'string'
	}, {
		name : 'author',
		type : 'string'
	}, {
		name : 'condition',
		type : 'string'
	}, {
		name : 'description',
		type : 'string'
	}, {
		name : 'eid',
		type : 'int'
	}, {
		name : 'expid',
		type : 'int'
	}, {
		name : 'genes',
		type : 'string'
	}, {
		name : 'institution',
		type : 'string'
	}, {
		name : 'mutant',
		type : 'string'
	}, {
		name : 'organism',
		type : 'string'
	}, {
		name : 'pi',
		type : 'string'
	}, {
		name : 'platforms',
		type : 'int'
	}, {
		name : 'pmid',
		type : 'string'
	}, {
		name : 'release_date',
		type : 'string'
	}, {
		name : 'samples',
		type : 'int'
	}, {
		name : 'strain',
		type : 'string'
	}, {
		name : 'timeseries',
		type : 'string'
	}, {
		name : 'title',
		type : 'string'
	}]
});

Ext.define('Sample', {
	extend : 'Ext.data.Model',
	idProperty : 'pid',
	fields : [{
		name : 'accession',
		type : 'string'
	}, {
		name : 'channels',
		type : 'int'
	}, {
		name : 'condition',
		type : 'string'
	}, {
		name : 'eid',
		type : 'int'
	}, {
		name : 'expid',
		type : 'int'
	}, {
		name : 'expmean',
		type : 'float'
	}, {
		name : 'expname',
		type : 'string'
	}, {
		name : 'expstddev',
		type : 'float'
	}, {
		name : 'genes',
		type : 'int'
	}, {
		name : 'mutant',
		type : 'string'
	}, {
		name : 'organism',
		type : 'string'
	}, {
		name : 'pi',
		type : 'string'
	}, {
		name : 'pid',
		type : 'int'
	}, {
		name : 'platform',
		type : 'string'
	}, {
		name : 'pmid',
		type : 'string'
	}, {
		name : 'release_date',
		type : 'string'
	}, {
		name : 'samples',
		type : 'string'
	}, {
		name : 'sig_log_ratio',
		type : 'float'
	}, {
		name : 'sig_z_score',
		type : 'float'
	}, {
		name : 'source',
		type : 'string'
	}, {
		name : 'strain',
		type : 'string'
	}, {
		name : 'timepoint',
		type : 'string'
	}]
});

//Ext.define('Proteomics_Experiment', {
//	extend : 'Ext.data.Model',
//	idProperty : 'pid',
//	fields : [{
//		name : 'sample_name',
//		type : 'string'
//	}, {
//		name : 'taxon_name',
//		type : 'string'
//	}, {
//		name : 'contact_details',
//		type : 'string'
//	}, {
//		name : 'accession',
//		type : 'int'
//	}, {
//		name : 'contact_name',
//		type : 'string'
//	}, {
//		name : 'proteins',
//		type : 'int'
//	}, {
//		name : 'taxon_id',
//		type : 'int'
//	}, {
//		name : 'project_name',
//		type : 'string'
//	}, {
//		name : 'experiment_label',
//		type : 'string'
//	}, {
//		name : 'experiment_title',
//		type : 'string'
//	}, {
//		name : 'institution',
//		type : 'string'
//	}, {
//		name : 'source',
//		type : 'string'
//	}, {
//		name : 'experiment_type',
//		type : 'string'
//	}, {
//		name : 'experiment_id',
//		type : 'int'
//	}]
//});
//
//Ext.define('Proteomics_Protein', {
//	extend : 'Ext.data.Model',
//	idProperty : 'pid',
//	fields : [{
//		name : 'accession',
//		type : 'string'
//	}, {
//		name : 'taxon_id',
//		type : 'int'
//	}, {
//		name : 'na_feature_id',
//		type : 'int'
//	}, {
//		name : 'experiment_label',
//		type : 'string'
//	}, {
//		name : 'experiment_title',
//		type : 'string'
//	}, {
//		name : 'source',
//		type : 'string'
//	}, {
//		name : 'experiment_id',
//		type : 'int'
//	}, {
//		name : 'taxon_name',
//		type : 'string'
//	}, {
//		name : 'genome_name',
//		type : 'string'
//	}, {
//		name : 'refseq_locus_tag',
//		type : 'string'
//	}, {
//		name : 'locus_tag',
//		type : 'string'
//	}, {
//		name : 'product',
//		type : 'string'
//	}, {
//		name : 'refseq_gene',
//		type : 'string'
//	}, {
//		name : 'pubmed_id',
//		type : 'string'
//	}]
//});

Ext.define('SpecialtyGene', {
	extend : 'Ext.data.Model',
	fields : ['property', 'source', 'source_id', 'gene_name', 'locus_tag', 'gene_id', 'gi', 'genus', 'species', 'organism', 'product', 'function', 'classification', 'pmid', 'assertion', 'homologs']
});

Ext.define('SpecialtyGeneMapping', {
	extend : 'Ext.data.Model',
	fields : ['genome_id', 'genome_name', {name: 'taxon_id', type: 'int'}, 'feature_id',
	          'seed_id', 'alt_locus_tag', 'refseq_locus_tag', 'gene', 'product',
	          'property', 'source', 'source_id', 'organism', 'function', 'classification', 'pmid', 'assertion', 
	          {name: 'query_coverage', type: 'int'}, {name: 'subject_coverage', type: 'int'}, {name: 'identity', type: 'int'}, 'e_value', 'evidence']
});

var configuration = {};
configuration['Genome'] = {
	display_facets : ['genome_status', 'reference_genome', 'antimicrobial_resistance', 'antimicrobial_resistance_evidence', 'isolation_country', 'host_name', 'disease', 'collection_date', 'completion_date'],
	display_facets_texts : ['Genome Status', 'Reference Genome', 'Antimicrobial Resistance', 'Antimicrobial Resistance Evidence', 'Isolation Country', 'Host Name', 'Disease', 'Collection Date', 'Completion Date'],
	all_facets : ['Keyword', 'genome_status', 'reference_genome', 'antimicrobial_resistance', 'antimicrobial_resistance_evidence', 'isolation_country', 'host_name', 'disease', 'collection_date', 'completion_date', 'genome_id', 'genome_name', 'annotation', 'taxon_id'],
	field_facets : ['genome_status', 'reference_genome', 'antimicrobial_resistance', 'antimicrobial_resistance_evidence', 'isolation_country', 'host_name', 'disease', 'collection_date'],
	date_range_facets : ['completion_date'],
	numeric_range_facets : [],
	search_fields : [{
		value : 'Keyword',
		text : 'Keyword'
	}, {
		value : 'genome_id',
		text : 'Genome ID'
	}, {
		value : 'genome_name',
		text : 'Genome Name'
	}, {
		value : 'genome_status',
		text : 'Genome Status'
	}, {
		value : 'isolation_country',
		text : 'Isolation Country'
	}, {
		value : 'host_name',
		text : 'Host Name'
	}, {
		value : 'disease',
		text : 'Disease'
	}, {
		value : 'collection_date',
		text : 'Collection Date'
	}, {
		value : 'completion_date',
		text : 'Completion Date'
	}],
	sort_fields : [],
	url : '/portal/portal/patric/GenomeFinder/GenomeFinderWindow?action=b&cacheability=PAGE'
};

configuration['Sequence'] = {
	sort_fields : []
};

configuration['Feature'] = {
	display_facets : [],
	display_facets_texts : [],
	all_facets : ['Keyword', 'feature_type', 'annotation', 'genome_id', 'go', 'ec', 'pathway', 'figfam_id'],
	search_fields : [{
		value : 'Keyword',
		text : 'Keyword'
	}, {
		value : 'feature_type',
		text : 'Feature Type'
	}, {
		value : 'annotation',
		text : 'Annotation'
	}, {
		value : 'sequence_status',
		text : 'Sequence Status'
	}],
	sort_fields : ['annotation'],
	url : '/portal/portal/patric/GenomicFeature/GenomicFeatureWindow?action=b&cacheability=PAGE'
};

configuration['SpecialtyGene'] = {
	display_facets : ['genus', 'species', 'organism','classification'],
	display_facets_texts : ['Genus', 'Species', 'Organism', 'Classification'],
	all_facets : ['Keyword', 'source', 'genus', 'species', 'organism', 'classification'],
	field_facets : ['genus', 'species', 'organism','classification'],
	search_fields : [{
		value : 'Keyword',
		text : 'Keyword'
	}, {
		value : 'property',
		text : 'Property'
	}, {
		value : 'source',
		text : 'Source'
	}, {
		value : 'source_id',
		text : 'Source ID'
	}],
	sort_fields : [],
	url : '/portal/portal/patric/SpecialtyGeneSource/SpecialtyGeneSourceWindow?action=b&cacheability=PAGE'
};

configuration['AntibioticResistanceGeneMapping'] = {
	display_facets : ['source', 'evidence'],
	display_facets_texts : ['Source', 'Evidence'],
	all_facets : ['Keyword', 'property', 'source', 'evidence', 'classification', 'genome_id'],
	field_facets : ['source', 'evidence'],
	search_fields : [{
		value : 'Keyword',
		text : 'Keyword'
	}, {
		value : 'source',
		text : 'Source'
	}, {
		value : 'evidence',
		text : 'Evidence'
	}, {
		value : 'source_id',
		text : 'Source ID'
	}],
	sort_fields : [],
	url : '/portal/portal/patric/SpecialtyGeneSearch/SpecialtyGeneSearchWindow?action=b&cacheability=PAGE'
};

configuration['SpecialtyGeneMapping'] = {
	display_facets : ['property', 'source', 'evidence'],
	display_facets_texts : ['Property', 'Source', 'Evidence'],
	all_facets : ['Keyword', 'property', 'source', 'evidence', 'genome_id'],
	field_facets : ['property', 'source', 'evidence'],
	search_fields : [{
		value : 'Keyword',
		text : 'Keyword'
	}, {
		value : 'property',
		text : 'Property'
	}, {
		value : 'source',
		text : 'Source'
	}, {
		value : 'evidence',
		text : 'Evidence'
	}, {
		value : 'source_id',
		text : 'Source ID'
	}],
	sort_fields : [],
	url : '/portal/portal/patric/SpecialtyGeneSearch/SpecialtyGeneSearchWindow?action=b&cacheability=PAGE'
};

configuration['GlobalTaxonomy'] = {
	display_facets : ['taxon_rank', 'genomes_f'],
	display_facets_texts : ['Taxon Rank', 'Genomes'],
	all_facets : ['Keyword', 'taxon_rank', 'genomes_f'],
	field_facets: ['taxon_rank', 'genomes_f'],
	date_range_facets: [],
	search_fields : ['taxon_name'],
	url : '/portal/portal/patric/GlobalTaxonomy/GlobalTaxonomyWindow?action=b&cacheability=PAGE'
};

configuration['GENEXP_Experiment'] = {
	display_facets : ['organism', 'strain', 'mutant', 'condition', 'timeseries', 'release_date'],
	display_facets_texts : ['Organism', 'Strain', 'Gene Modification', 'Experimental Condition', 'Time Series', 'Release Date'],
	all_facets : ['Keyword', 'organism', 'strain', 'mutant', 'condition', 'timeseries', 'release_date', 'eid'],
	field_facets: ['organism', 'strain', 'mutant', 'condition', 'timeseries'],
	date_range_facets : ['release_date'],
	search_fields : [{
		value : 'Keyword',
		text : 'Keyword'
	}, 'organism', 'strain', 'mutant', 'condition', 'timeseries', 'release_date'],
	url : "/portal/portal/patric/ExperimentList/ExperimentListWindow?action=b&cacheability=PAGE"
};

configuration['Proteomics_Experiment'] = {
		display_facets : ['experiment_type'],
		display_facets_texts : ['Experiment Type'],
		all_facets : ['experiment_type', 'taxon_id', 'experiment_id'],
		url : '/portal/portal/patric/ProteomicsList/ProteomicsListWindow?action=b&cacheability=PAGE'
	};

function constructKeyword(object, name) {

	var all_facets = configuration[name].all_facets, keyword = "", entries;

	keyword += (object["Keyword"] == null) ? "(*)" : keyword += object["Keyword"].split("&facet=true")[0];

	for (var i = 1; i < all_facets.length; i++) {

		var facet = all_facets[i];

		if (object[facet] != null && keyword.indexOf(facet + ":") != -1)
			keyword = removeThisField(keyword, facet);

		if (facet == "annotation" && name == "Genome") {

			if (object[facet] != null && (object[facet] == "PATRIC" || object[facet] == "RAST"))
				keyword += " AND (patric_cds:[1 TO *])";
			else if (object[facet] != null && (object[facet] == "Legacy BRC" || object[facet] == "BRC" || object[facet] == "BRC1"))
				keyword += " AND (brc1_cds:[1 TO *])";
			else if (object[facet] != null && object[facet] == "RefSeq")
				keyword += " AND (refseq_cds:[1 TO *])";

		} else if (object[facet] != null && object[facet] != "") {
			entries = object[facet].split("##");
			DateFormat(entries, facet, name);
			keyword += " AND (" + facet + ":(" + entries.join(" OR ") + "))";
		}
	}
	keyword = keyword.replace("(*) AND ", "");
	if (keyword == "(*)") {
		keyword = keyword.replace("(*)", "*:*");
	}
	return keyword;
}

function DateFormat(data, facet, name) {

	for (var i = 0; i < data.length; i++) {

		if (facet == "completion_date" || facet == "release_date") {

			if (data[i] == "*")
				data[i] = "[* TO *]";
			else
				data[i] = "[" + data[i] + "-01-01T00:00:00Z TO " + (parseInt(data[i].split('-')[0]) + 1) + "-01-01T00:00:00Z]";

		} else if (configuration[name].display_facets.indexOf(facet) >= 0) {
			if (data[i] != "*")
				data[i] = "\"" + data[i] + "\"";
		}
	}
}

function removeThisField(keyword, facet) {

	var ind1 = keyword.indexOf(" AND (" + facet + ":");
	var ind2 = keyword.indexOf(")", ind1);

	keyword = keyword.replace(keyword.substring(ind1, ind2 + 6), "");

	return keyword;
}
