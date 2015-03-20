<%
// process post params
String dataUrl = request.getParameter("data_url");
String fileType = request.getParameter("file_type");
String fileFormat = request.getParameter("file_format");

%>
<script type="text/javascript" src="/patric/js/vbi/AddToWorkspace.min.js"></script>
<script type="text/javascript" src="/patric/js/vbi/TranscriptomicsUpload.min.js"></script>
<script type="text/javascript">
function launchTranscriptomicsUploader() {
    alert("In order to support new functionality in PATRIC, we have implemented a new workspace which includes a new form for upload of transcriptomics data.");

    // link to new app page (/app/Expression)
    document.location = "/app/Expression";
}

Ext.onReady(function () {
	launchTranscriptomicsUploader();
});

</script>
<div id="atc-msg-div"></div>