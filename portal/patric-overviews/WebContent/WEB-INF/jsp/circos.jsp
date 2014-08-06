<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet"
%><%@ page import="java.util.ArrayList" 
%><%@ page import="java.util.Arrays" 
%><portlet:defineObjects /><%
	String actionUrl = "/portal/portal/patric/CircosGenomeViewer/CircosGenomeViewerWindow?action=1"; // (renderResponse.createActionURL()).toString();
	String genomeId = request.getParameter("context_id");
%>
<script type="text/javascript" src="/patric-overviews/circos/main.js"></script>
<link rel="stylesheet" type="text/css" href="/patric-overviews/circos/styles.css">

<h1 id="page_title">Circos Image Generator</h1>
<div id="error_message"></div>
<div id="container">
	<form id="image_data_form" action="<%=actionUrl %>" method="POST" enctype="multipart/form-data">
	<div id="left_side">
		<h2>1. Enter the GID you want to display</h2>
		<label for="gid">GID:</label>
		<input name="gid" id="gid" type="text" value=<%=genomeId %> />
		<br><br><hr>
		<h2>2. Default Data Tracks:</h2>
		<%
		// Generate checkboxes for each data option
		ArrayList<String> fields = new ArrayList<String>();
		fields.addAll(Arrays.asList("CDS Forward", "CDS Reverse", "RNA Forward", "RNA Reverse", "Misc Forward", "Misc Reverse"));
		for (String field : fields) {
			String field_name = field.toLowerCase().replace(" ", "_");
		%>
			<input name="<%=field_name%>" id="<%=field_name%>" type="checkbox">
			<label for="<%=field_name%>"><%=field%></label><br>
		<% } %>
		<br>
		<input name="gc_content" id="gc_content" type="checkbox" onclick="addGCContentDropdown();">
		<label for="gc_content">Include track for GC Content?</label>
		<div id="gc_content_dropdown"></div>
		<br>
		<input name="gc_skew" id="gc_skew" type="checkbox" onclick="addGCSkewDropdown();">
		<label for="gc_skew">Include track for GC Skew?</label>
		<div id="gc_skew_dropdown"></div>
<br><hr><br>

		<input name="add_custom_track" id="add_custom_track" type="checkbox" onclick="firstCustomTrack();"/>
		<label for="add_custom_track">Custom Tracks?</label>
		<div id="custom_tracks"><br></div>
		<hr>
		<div>
			<h2>3. Image Customization:</h2>
			<input name="include_outer_track" id="include_outer_track" type="checkbox" checked="checked" />
			<label for="include_outer_track">Include outer track?</label>
			<br>
			<small>This track only makes the starts and ends of each accession clearer</small>
			<br><br>
			<label for="image_dimensions">Image Width/Height:</label>
			<input name="image_dimensions" id="image_dimensions" type="text" placeholder="1000"/>
			<br>
			<small>If left empty, default size is 1000 pixels</small>
			<br><br>
			<label for="track_width">Track Width (1-10% of plot's radius)</label>
			<br>
			<input name="track_width" id="track_width" type="range" min="1" max="10" value="3">
			<span name="slider_value" id="slider_value">3%</span>
			<br>
			<small>The default width is 3% of the entire plot's radius</small>
			<p>Your image will use the SVG format by default.</p>
		</div>
		<br>
		<div style="text-align: center;">
			<button name="go" id="go">Go</button>
		</div>
	</div>
	<div id="right_side">
		<h2>4. Upload your own data files</h2>
		<h4>Your files will be used in conjunction with PATRIC's own data for the GID you enter to the left.</h4>
		<label for="file_chooser">Upload files:</label>
		<input name="file_chooser[]" id="file_chooser" type="file" multiple>
		<br><br>
		<div name="file_list" id="file_list"></div>

		<h4>The file should use the following format:</h4>
		<pre>
accession_name    strand_start    strand_end
</pre>
		<h4>For example:</h4>
<pre>
AZJI01000001    711302    712414
AZJI01000001    712941    713504
AZJI01000004    713       901
</pre>
		<h4>Or a fourth column can contain a scalar value between 0 and 1 that will be associated with the strand:</h4>
<pre>
AZJI01000001    711302    712414    0.75
AZJI01000001    712941    713504    0.56
AZJI01000004    713       901       0.39
</pre>
		<h3>Three column files should be displayed as tiles. Four column files can be displayed as line plots, histograms, or heatmaps.</h3>
	</div>
	</form>
</div>
<br><hr><br>
<div id="circos_result">
	<% if (request.getAttribute("imageId") != null) {%>
	<h2>Your generated Circos plot:</h2>
	<img src="/patric-overviews/images/<%=request.getAttribute("imageId") %>/circos.svg" usemap="#circosmap">
	<% } %>
</div>