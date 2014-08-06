#!/usr/bin/perl

print "Content-type: text/html\n\n";
print "<body onload=\"parent.haveBlast(window.name);\">";

# Add hyperlinks to blast subject ids. Check boxes to get fasta sequences

$gepUrl = "Feature?cType=feature&cId=";
#$ncbiUrl = "http://www.ncbi.nlm.nih.gov/entrez/viewer.fcgi?db=nucleotide&amp;val=";
$ncbiUrl = "http://www.ncbi.nlm.nih.gov/nuccore/";
$matchString = "ISMAP></CENTER>\n<HR>";

$tableHeader = '<form action="#" method="post" id="searchResult" name="searchResult">
<input type="hidden" id="fastaaction" name="fastaaction" value="" />
<input type="hidden" id="fastascope" name="fastascope" value="Selected" />
<input type="hidden" id="fids" name="fids" value="" />

<div class="table-container with-border">
	<div class="table-container-heading">
		<div id="cart-actions-heading"><input type="button" class="button" id="btnAddToCart" title="Add Feature(s) to Workspace" value="Add Feature(s) to Workspace" onclick="add_to_cart()" />&nbsp;</div>
		<div style="float:left"><input type="checkbox" name="selectall" id="selectallCheckboxes" onclick="toggleAllCheckBoxes()" />
			<label for="selectallCheckboxes">Select all </label>
		</div>
		<div id="sequence-data">
			<div id="sequence-data-text"><h3>Retrieve <br />Sequence Data for:</h3></div>
			<div id="sequence-data-form">					
				<span><label for="type">Type:</label>
					<select name="fastatype" id="fastatype">
						<option value="dna">FASTA DNA Sequence(s)</option>
						<option value="protein">FASTA Protein Sequence(s)</option>
						<option value="both">FASTA DNA/Protein Sequence(s)</option>
					</select> 
				</span>
				<span>
					<img src="/patric/images/btn_show_data.gif" alt="Show Data" style="cursor:pointer" onclick="show_fasta_files()" /> 
					<img src="/patric/images/btn_download_data.gif" alt="Download Data" style="cursor:pointer" onclick="download_fasta_files()" />
				</span>
			</div>
			<div class="clear"></div>
		</div>
        <div class="clear"></div>
    </div>
</div>';




$result = `export BLASTDB=db; ./blast.REAL | /usr/bin/fgrep -vf removeFromNCBIhtml`;
$result=~ s/fid\|([^|]*)\|/<input type="checkbox" name="fids[]" value="$1" \/>fid\|<a href=$gepUrl$1 target=_blank>$1<\/a\>\|/g;
#$result=~ s/fid\|([^|]*)\|/fid\|<a href=$gepUrl$1 target=_blank>$1<\/a\>\|/g;
$result=~ s/accn\|(NC_[^\s]*)/accn\|<a href=$ncbiUrl$1 target=_blank>$1<\/a\>/g;
$result=~ s/accn\|(NZ_[^\s]*)/accn\|<a href=$ncbiUrl$1 target=_blank>$1<\/a\>/g;

$result=~ s/$matchString/$matchString<br \/>$tableHeader/; # print a header for the table


# Cleanup tags to make things more valid
$result=~ s/<\/BODY>\n<\/HTML>//; # Strips out invalid closing body and html tags
$result=~ s/&(?![a-zA-Z]{3,4};)/&amp;/gi; # Fix invalid (unencoded) ampersands

# Remove elements we don't want in the page
$result=~ s/<FORM NAME=\"BLASTFORM1">//gi; # Remove the BLAST form. We need our own here.
$result=~ s/<\/form>//gi; # Remove the BLAST form closing tag too.

print '<div id="blast-results-table">'; # Required for add to cart button to work.
print "$result";
print "</div>";

exit;

