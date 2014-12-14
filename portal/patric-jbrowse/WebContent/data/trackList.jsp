<%
response.setContentType("application/json");
%>
{
	"tracks": [
		{
			"type": "SequenceTrack",
			"urlTemplate": "/portal/portal/patric/GenomeBrowser/GBWindow?action=b&cacheability=PAGE&mode=getSequence&sequence_id={refseq}&chunk=",
			"key": "Reference sequence",
			"label": "DNA",
			"chunkSize": 20000,
			"maxExportSpan": 10000000
		}
		,{
			"type": "FeatureTrack",
			"urlTemplate": "/portal/portal/patric/GenomeBrowser/GBWindow?action=b&cacheability=PAGE&mode=getTrackInfo&accession={refseq}&annotation=PATRIC",
			"storeClass": "JBrowse/Store/SeqFeature/NCList",
			"key": "PATRIC Annotation",
			"label": "PATRICGenes",
			"style": {
				"label": "function( feature ) { return feature.get('seed_id'); }"
			},
			"hooks": {
				"modify": "function(track, feature, div) { div.style.backgroundColor = ['#17487d','#5190d5','#c7daf1'][feature.get('phase')];}"
			},
			"tooltip": "<div style='line-height:1.7em'><b>{seed_id}</b> | {refseq_locus_tag} | {alt_locus_Tag} | {gene}<br>{product}<br>{type}: {start_str} .. {end} ({strand_str})<br> <i>Click for detail information</i></div>",
			"metadata": {
				"Description": "PATRIC annotated genes"
			},
			"maxExportFeatures": 10000,
			"maxExportSpan": 10000000
		}
		, {
			"type": "FeatureTrack",
			"urlTemplate": "/portal/portal/patric/GenomeBrowser/GBWindow?action=b&cacheability=PAGE&mode=getTrackInfo&accession={refseq}&annotation=RefSeq",
			"storeClass": "JBrowse/Store/SeqFeature/NCList",
			"key": "RefSeq Annotation",
			"label": "RefSeqGenes",
			"style": {
				"className": "feature3",
				"label": "function( feature ) { return feature.get('refseq_locus_tag'); }"
			},
			"hooks": {
				"modify": "function(track, feature, div) { div.style.backgroundColor = ['#4c5e22','#9ab957','#c4d59b'][feature.get('phase')];}"
			},
			"tooltip": "<div style='line-height:1.7em'><b>{refseq_locus_tag}</b> | {gene}<br>{product}<br>{type}: {start_str} .. {end} ({strand_str})<br> <i>Click for detail information</i></div>",
			"metadata": {
				"Description": "RefSeq annotated genes"
			},
			"maxExportFeatures": 10000,
			"maxExportSpan": 10000000
		}
		<%--
		, {
			"type": "JBrowse/View/Track/Alignments2",
			"storeClass": "JBrowse/Store/SeqFeature/BAM",
			"urlTemplate": "/rnaseq/datasets/e31f696dd5f4d830/display?to_ext=bam",
			"baiUrlTemplate": "/rnaseq/dataset/get_metadata_file?hda_id=e31f696dd5f4d830&metadata_name=bam_index",
			"key": "Alignment Sample",
			"label": "bam_test_alignment",
			"metadata": {
				"Description": "PATRIC test bam"
			}
		}, {
			"type": "JBrowse/View/Track/SNPCoverage",
			"storeClass": "JBrowse/Store/SeqFeature/BAM",
			"urlTemplate": "/rnaseq/datasets/e31f696dd5f4d830/display?to_ext=bam",
			"baiUrlTemplate": "/rnaseq/dataset/get_metadata_file?hda_id=e31f696dd5f4d830&metadata_name=bam_index",
			"key": "Coverage Sample",
			"label": "bam_test_coverage"
		}, {
			"type": "JBrowse/View/Track/Wiggle/Density",
			"storeClass": "JBrowse/Store/BigWig",
			"urlTemplate": "/jbrowse/data/MarkDups_R73-L2-P2_dedup.bw",
			"key": "BigWig Sample Density",
			"label": "bigwig_test_density",
			"scale": "log",
			"bicolor_pivot": "mean"
		}, {
			"type": "JBrowse/View/Track/Wiggle/XYPlot",
			"storeClass": "JBrowse/Store/BigWig",
			"urlTemplate": "/jbrowse/data/MarkDups_R73-L2-P2_dedup.bw",
			"key": "BigWig Sample XY",
			"label": "bigwig_test_xyplot",
			"variance_band": true,
			"scale": "log",
			"style": {
				"height": 100
			}
		} --%>
	],
	"formatVersion": 1
}
