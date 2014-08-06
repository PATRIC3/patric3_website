/*
	Define some mouse handlers for the charts. Rather than define the handlers in the
	charts themselves, I've added the ability to pass a handler function into the charts.
	I did this mostly because click behavior has to be defined by the VBI team, but it
	turns out to be a pretty useful pattern. 

	Mouse events return d, the data element; i, the data index; and meta, an object with
	other useful information. meta.clickTarget is always the element that fired the mouse
	event.
*/

var topGeneModifications, topExperimentConditions;
function updateLinkout(url) {
	$("#dlp-transcriptomics-linkout").attr("href", url);
};

(function() {
	var linktoExpSpecies = function(d, i, meta) {
		//window.open("ExperimentList?cType=taxon&cId=2&kw=organism:"+encodeURIComponent('"'+d.label+'"'),'Transcriptomics');
		window.location = "ExperimentList?cType=taxon&cId=2&kw=organism:"+encodeURIComponent('"'+d.label+'"');
	};

	var linktoExpGeneModifications = function(d, i, meta) {
		//window.open(meta.linkBase+'mutant:'+encodeURIComponent('"'+d.label+'"'),'Transcriptomics');
		window.location = meta.linkBase+'mutant:'+encodeURIComponent('"'+d.label+'"');
	};

	var linktoExpExperimentConditions = function(d, i, meta) {
		//window.open(meta.linkBase+'condition:'+encodeURIComponent('"'+d.label+'"'),'Transcriptomics');
		window.location = meta.linkBase+'condition:'+encodeURIComponent('"'+d.label+'"');
	};

	$().ready(function(event) {
		var topSpecies = new TopSpecies({
			target: "#dlp-transcriptomics-top-species",
			datafile: "/patric-common/data/transcriptomics.json",
			datakey: "topSpecies",
			clickHandler: linktoExpSpecies /*
			mouseoverHandler: top5MouseoverHandler,
			mouseoutHandler: top5MouseoutHandler*/
		});
		
		topGeneModifications = new HorizontalBarChart({
			target: "#dlp-transcriptomics-top-mutants",
			datafile: "/patric-common/data/transcriptomics.json",
			datakey: "GeneModifications",
			clickHandler: linktoExpGeneModifications
		});
		
		topExperimentConditions = new HorizontalBarChart({
			target: "#dlp-transcriptomics-top-conditions",
			datafile: "/patric-common/data/transcriptomics.json",
			datakey: "ExperimentConditions",
			clickHandler: linktoExpExperimentConditions
		});
	});
}).call(this);
