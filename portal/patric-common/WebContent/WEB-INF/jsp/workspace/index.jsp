<script type="text/javascript" src="/patric/js/vbi/AddToWorkspace.min.js"></script>
<script type="text/javascript" src="/patric/js/vbi/TranscriptomicsUpload.min.js"></script>
<script type="text/javascript" src="/patric-transcriptomics/js/TranscriptomicsGene.js"></script>
<script type="text/javascript" src="/patric-transcriptomics/js/namespace.js"></script>
<script type="text/javascript">var popup; </script>
<script type="text/javascript">
function launchTranscriptomicsUploader() {
    alert("In order to support new functionality in PATRIC, we have implemented a new workspace which includes a new form for upload of transcriptomics data.");

    // link to new app page (/app/Expression)
    document.location = "/app/Expression";
}
</script>
<script type="text/javascript" src="/patric/js/vbi/Workspace.min.js"></script>

<div id="wksp" style="width:100%;min-height:530px"></div>
<form id="fTableForm" action="#" method="post">

	<input type="hidden" id="tablesource" name="tablesource" value="" />
	<input type="hidden" id="fileformat" name="fileformat" value="" />

	<!-- fasta download specific param -->
	<input type="hidden" id="fastaaction" name="fastaaction" value="" />
	<input type="hidden" id="fastatype" name="fastatype" value="" />
	<input type="hidden" id="fastascope" name="fastascope" value="" />
	<input type="hidden" id="fids" name="fids" value="" />

	<input type="hidden" id="idType" name="idType" value="" />
</form>