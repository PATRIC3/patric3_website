<link rel="stylesheet" id="vennCompare" href="/patric-common/js/gse.css" type="text/css"/>
<script type="text/javascript" src="/patric-common/js/gse.js"></script>
<script type="text/javascript" src="/patric-common/js/addtocarts.js"></script>
<script type="text/javascript" src="/patric/js/libs/d3.v3.min.js"></script>
<script type="text/javascript" src="/patric-common/js/jquery.tablesorter.js"></script> 
<%
String hostName=request.getServerName();
String _grIdxString = request.getParameter("group_id");
String _grTypeString = request.getParameter("group_type");
String data_url ="/portal/portal/patric/BreadCrumb/WorkspaceWindow?action=b&cacheability=PAGE&action_type=GSESupport&action=items&groupIds=" + _grIdxString;
String groups_url ="/portal/portal/patric/BreadCrumb/WorkspaceWindow?action=b&cacheability=PAGE&action_type=GSESupport&action=groups&groupIds=" + _grIdxString;
String list_url ="/portal/portal/patric/BreadCrumb/WorkspaceWindow?action=b&cacheability=PAGE&action_type=GSESupport&action=group_list";
String create_group_url ="/portal/portal/patric/BreadCrumb/WorkspaceWindow?action=b&cacheability=PAGE&action_type=groupAction&action=create&group_type=" + _grTypeString;

if (_grIdxString != null) {
%>
<div id="gse">
	<p>
		The Group Explorer allows you to compare and contrast selected groups from within
		 your Workspace using a Venn diagram-based interactive visualization.
		 To learn more see
		<a href="//enews.patricbrc.org/faqs/group-explorer-faqs/" target="_blank">Group Explorer FAQs</a>.
	</p>
	&nbsp;&nbsp;<input type="radio" name="color_type" id="default" value="true" onclick="colorChoice('Y');" checked/> <label for="default">default color</label>
	&nbsp;&nbsp;<input type="radio" name="color_type" id="alter_color" value="false" onclick="colorChoice('N');" /> <label for="alter_color">alternative color</label>
	<button class="button" id="save_btn2" onclick=saveSVG();>Save image as SVG</button>
	<button class="button" id="save_btn" onclick=saveSVG2PNG();>Save image as PNG</button>
	<button class="button" id="create_btn" onclick=createGroup();>Create group from selected region</button>
	<br>
	<div id="gse-members"></div>
	<div id="gse-venndiagram"></div>
	<!-- hidden canvas for image download -->
	<canvas width="500" height="500" style="display:none"></canvas>
</div>
<% } else { %>
<div></div>
<% } %>

<script type="text/javascript">
//<![CDATA[
var groupCompare = null;
var myHash = null;
var myURL = null;
var myType = null;
var ids = "";
var regionName = null;
var groups = null;

var selectionListener = function() {
  var selected = groupCompare.getSelectedMembers();
  var regions = groupCompare.getSelectedRegions();

  if (regions.length == 0) {
    d3.selectAll("#gse-members-tbl").remove();
    regionName = "";
  } else if (selected) {
    ids = "";

    d3.selectAll("#gse-members-tbl").remove();
    regionName = createRegionName(regions);

    var memberArea = d3.select("#gse-members");
    var memberTable = memberArea.append("table").attr("id", "gse-members-tbl").attr("style", "overflow-y:scroll");
    memberTable.append("th").text("Region: " + regionName);

    if (selected.length > 0) { // tablesorter gets inactivated when there is no member in selected region
      var innerTable = memberTable.append("tr").append("table").attr("id", "inner_table").attr("class", "tablesorter");
      var thead = innerTable.append("thead").append("tr");
      if (myType === 'Genome') {
        // genome name, status, country, host, disease, collection date, completion date
        thead.append("th").text("Genome Name");
        thead.append("th").text("Status");
        thead.append("th").text("Isolation Country");
        thead.append("th").text("Host");
        thead.append("th").text("Disease");
        thead.append("th").text("Collection Date");
        thead.append("th").text("Completion Date");
        var tbody = innerTable.append("tbody");
        for (var i = 0, ilen = selected.length; i < ilen; ++i) {
          var row = myHash[selected[i]]; // console.log(row);
          var tr = tbody.append("tr");
          // tr.append("td").text(row["Genome Id"]);
          tr.append("td").text(row["Genome Name"]);
          tr.append("td").text(row["Status"]);
          tr.append("td").html(row["Isolation Country"]!=""?row["Isolation Country"]:"&nbsp;");
          tr.append("td").html(row["Host"]!=""?row["Host"]:"&nbsp;");
          tr.append("td").html(row["Disease"]!=""?row["Disease"]:"&nbsp;");
          tr.append("td").html(row["Collection Date"]!=""?row["Collection Date"]:"&nbsp;");
          tr.append("td").html(row["Completion Date"]!=""?row["Completion Date"]:"&nbsp;");
          if (i == 0) {
            ids = selected[i];
          } else {
            ids += "," + selected[i];
          }
        }
      }
      else if (myType === 'Feature') {
        // genome name, locus_tag, refseq locus tag, gene, product
        thead.append("th").text("Genome Name");
        thead.append("th").text("Locus Tag");
        thead.append("th").text("RefSeq Locus Tag");
        thead.append("th").text("Gene");
        thead.append("th").text("Product");
        var tbody = innerTable.append("tbody");
        for (var i = 0, ilen = selected.length; i < ilen; ++i) {
          var row = myHash[selected[i]];
          var tr = tbody.append("tr");
          // tr.append("td").text(selected[i]);
          tr.append("td").text(row["Genome Name"]);
          tr.append("td").text(row["Locus Tag"]);
          tr.append("td").html(row["RefSeq Locus Tag"]!=""?row["RefSeq Locus Tag"]:"&nbsp;");
          tr.append("td").html(row["Gene Symbol"]!=""?row["Gene Symbol"]:"&nbsp;");
          tr.append("td").text(row["Product"]);
          if (i == 0) {
            ids = selected[i];
          } else {
            ids += "," + selected[i];
          }
        }
      }
      else if (myType === 'ExpressionExperiment') {
        // source, datatype, title, accession
        thead.append("th").text("Source");
        thead.append("th").text("Data Type");
        thead.append("th").text("Title");
        thead.append("th").text("Accession");
        var tbody = innerTable.append("tbody");
        for (var i = 0, ilen = selected.length; i < ilen; ++i) {
          var row = myHash[selected[i]];
          var tr = tbody.append("tr");
          // tr.append("td").text(selected[i]);
          tr.append("td").text(row["Source"]);
          tr.append("td").html(row["Data Type"]!=""?row["Data Type"]:"&nbsp;");
          tr.append("td").text(row["Title"]);
          tr.append("td").html(row["Accession"]!=""?row["Accession"]:"&nbsp;");
          if (i == 0) {
            ids = selected[i];
          } else {
            ids += "," + selected[i];
          }
        }
      }

      $(document).ready(function() {
        // call the tablesorter plugin
        $("#inner_table").tablesorter({
          // sort on the first column and third column, order asc
          sortList: [[0, 0]]
        });
      });
    }
  }
};

function createRegionName(selectedRegions) {
  var max_mask = 0;
  var mask_array = [];
  var name_array = [];
  regionName = "";
  if (groups.length == 2) {
    max_mask = 3;
  } else if (groups.length == 3) {
    max_mask = 7;
  }

  for (var i = 0, ilen = max_mask; i < ilen; ++i) {
    mask_array[i] = 0;
    name_array[i] = "";
  }

  for (var i = 0, ilen = selectedRegions.length; i < ilen; ++i) {
    if (i == 0) {
      regionName = selectedRegions[i].region_name;
    } else {
      regionName = "(" + regionName + ") U (" + selectedRegions[i].region_name + ")";
    }
    mask_array[selectedRegions[i].region_mask - 1] = 1;
    name_array[selectedRegions[i].region_mask - 1] = selectedRegions[i].region_name;
    // console.log("i=" + i + " region_mask=" + selectedRegions[i].region_mask + " name=" +selectedRegions[i].region_name);
  }

  if (max_mask == 3) {
    if (mask_array[0] && mask_array[1] && mask_array[2]) {
      regionName = "(" + groups[0].name + ") U (" + groups[1].name + ")";
    } else if (mask_array[0] && mask_array[2]) {
      regionName = groups[0].name;
    } else if (mask_array[1] && mask_array[2]) {
      regionName = groups[1].name;
    }
  } else if (max_mask == 7) {
    var center_name = "(" + groups[0].name + ") + (" + groups[1].name + ") + (" + groups[2].name + ")";
    if (mask_array[0] && mask_array[1] && mask_array[2] && mask_array[3] && mask_array[4] && mask_array[5] && mask_array[6]) {
      regionName = "(" + groups[0].name + ") U (" + groups[1].name + ") U (" + groups[2].name + ")";
    } else if (mask_array[0] && mask_array[1] && mask_array[2] && mask_array[3] && mask_array[4] && mask_array[5] && mask_array[6] == 0) {
      regionName = "(" + groups[0].name + ") U (" + groups[1].name + ") U (" + groups[2].name + ") - (" + center_name + ")";
    } else if (mask_array[0] && mask_array[1] && mask_array[2] && mask_array[3] == 0 && mask_array[4] && mask_array[5] && mask_array[6]) {
      regionName = "(" + groups[0].name + ") U (" + groups[1].name + ")";
    } else if (mask_array[0] && mask_array[1] == 0 && mask_array[2] && mask_array[3] && mask_array[4] && mask_array[5] && mask_array[6]) {
      regionName = "(" + groups[0].name + ") U (" + groups[2].name + ")";
    } else if (mask_array[0] == 0 && mask_array[1] && mask_array[2] && mask_array[3] && mask_array[4] && mask_array[5] && mask_array[6]) {
      regionName = "(" + groups[1].name + ") U (" + groups[2].name + ")";
    } else if (mask_array[0] && mask_array[1] && mask_array[2] == 0 && mask_array[3] && mask_array[4] && mask_array[5] && mask_array[6]) {
      regionName = "(" + groups[2].name + ") U (" + name_array[0] + ") U (" + name_array[1] + ")";
    } else if (mask_array[0] && mask_array[1] == 0 && mask_array[2] == 0 && mask_array[3] && mask_array[4] && mask_array[5] && mask_array[6]) {
      regionName = "(" + groups[2].name + ") U (" + name_array[0] + ")";
    } else if (mask_array[0] == 0 && mask_array[1] && mask_array[2] == 0 && mask_array[3] && mask_array[4] && mask_array[5] && mask_array[6]) {
      regionName = "(" + groups[2].name + ") U (" + name_array[1] + ")";
    } else if (mask_array[0] == 0 && mask_array[1] == 0 && mask_array[2] && mask_array[3] && mask_array[4] && mask_array[5] && mask_array[6]) {
      regionName = "(" + groups[2].name + ") U (" + name_array[2] + ")";
    } else if (mask_array[0] == 0 && mask_array[1] == 0 && mask_array[2] == 0 && mask_array[3] && mask_array[4] && mask_array[5] && mask_array[6]) {
      regionName = groups[2].name;
    } else if (mask_array[0] && mask_array[1] && mask_array[2] && mask_array[3] && mask_array[4] == 0 && mask_array[5] && mask_array[6]) {
      regionName = "(" + groups[1].name + ") U (" + name_array[0] + ") U (" + name_array[3] + ")";
    } else if (mask_array[0] && mask_array[1] && mask_array[2] && mask_array[3] == 0 && mask_array[4] == 0 && mask_array[5] && mask_array[6]) {
      regionName = "(" + groups[1].name + ") U (" + name_array[0] + ")";
    } else if (mask_array[0] == 0 && mask_array[1] && mask_array[2] && mask_array[3] && mask_array[4] == 0 && mask_array[5] && mask_array[6]) {
      regionName = "(" + groups[1].name + ") U (" + name_array[3] + ")";
    } else if (mask_array[0] == 0 && mask_array[1] && mask_array[2] && mask_array[3] == 0 && mask_array[4] && mask_array[5] && mask_array[6]) {
      regionName = "(" + groups[1].name + ") U (" + name_array[4] + ")";
    } else if (mask_array[0] == 0 && mask_array[1] && mask_array[2] && mask_array[3] == 0 && mask_array[4] == 0 && mask_array[5] && mask_array[6]) {
      regionName = groups[1].name;
    } else if (mask_array[0] && mask_array[1] && mask_array[2] && mask_array[3] && mask_array[4] && mask_array[5] == 0 && mask_array[6]) {
      regionName = "(" + groups[0].name + ") U (" + name_array[1] + ") U (" + name_array[3] + ")";
    } else if (mask_array[0] && mask_array[1] && mask_array[2] && mask_array[3] == 0 && mask_array[4] && mask_array[5] == 0 && mask_array[6]) {
      regionName = "(" + groups[0].name + ") U (" + name_array[1] + ")";
    } else if (mask_array[0] && mask_array[1] == 0 && mask_array[2] && mask_array[3] && mask_array[4] && mask_array[5] == 0 && mask_array[6]) {
      regionName = "(" + groups[0].name + ") U (" + name_array[3] + ")";
    } else if (mask_array[0] && mask_array[1] == 0 && mask_array[2] && mask_array[3] == 0 && mask_array[4] && mask_array[5] && mask_array[6]) {
      regionName = "(" + groups[0].name + ") U (" + name_array[5] + ")";
    } else if (mask_array[0] && mask_array[1] == 0 && mask_array[2] && mask_array[3] == 0 && mask_array[4] && mask_array[5] == 0 && mask_array[6]) {
      regionName = groups[0].name;
    } else if (mask_array[0] && mask_array[1] && mask_array[2] && mask_array[3] == 0 && mask_array[4] == 0 && mask_array[5] == 0
            && mask_array[6] == 0) {
      regionName = "(" + groups[0].name + ") U (" + groups[1].name + ") - (" + groups[2].name + ")";
    } else if (mask_array[0] && mask_array[1] == 0 && mask_array[2] == 0 && mask_array[3] && mask_array[4] && mask_array[5] == 0
            && mask_array[6] == 0) {
      regionName = "(" + groups[0].name + ") U (" + groups[2].name + ") - (" + groups[1].name + ")";
    } else if (mask_array[0] == 0 && mask_array[1] && mask_array[2] == 0 && mask_array[3] && mask_array[4] == 0 && mask_array[5]
            && mask_array[6] == 0) {
      regionName = "(" + groups[1].name + ") U (" + groups[2].name + ") - (" + groups[0].name + ")";
    } else if (mask_array[0] && mask_array[1] && mask_array[2] == 0 && mask_array[3] && mask_array[4] && mask_array[5] && mask_array[6] == 0) {
      regionName = "(" + groups[0].name + ") U (" + groups[1].name + ") U (" + groups[2].name + ") - (" + center_name + ") - (" + "("
              + groups[0].name + ") + (" + groups[1].name + ") - (" + groups[2].name + ")" + ")";
    } else if (mask_array[0] && mask_array[1] && mask_array[2] && mask_array[3] && mask_array[4] && mask_array[5] == 0 && mask_array[6] == 0) {
      regionName = "(" + groups[0].name + ") U (" + groups[1].name + ") U (" + groups[2].name + ") - (" + center_name + ") - (" + "("
              + groups[1].name + ") + (" + groups[2].name + ") - (" + groups[0].name + ")" + ")";
    } else if (mask_array[0] && mask_array[1] && mask_array[2] && mask_array[3] && mask_array[4] == 0 && mask_array[5] && mask_array[6] == 0) {
      regionName = "(" + groups[0].name + ") U (" + groups[1].name + ") U (" + groups[2].name + ") - (" + center_name + ") - (" + "("
              + groups[0].name + ") + (" + groups[2].name + ") - (" + groups[1].name + ")" + ")";
    }
  }
  return regionName;
}

function createGroup() {
  console.log("clicked " + myURL + "create " + myType);
  if (regionName && ids.length > 0) {
    Ext.Ajax.request({
      method: 'POST',
      params: {
        group_name: regionName,
        group_desc: "",
        group_type: myType,
        tracks: ids,
        tags: ""
      },
      url: '/portal/portal/patric/BreadCrumb/WorkspaceWindow?action=b&cacheability=PAGE&action_type=groupAction&action=create',
      success: function(response, opts) {
        console.log("message");
        var msgCt = Ext.DomHelper.insertFirst("gse-members", {
          id: 'member_area_id'
        }, true);
        var m = Ext.DomHelper.append(msgCt, '<div class="msg">Selected members are added to a group.</div>', true).hide();
        m.slideIn('l').ghost("l", {
          delay: 2000,
          remove: true
        });
        updateCartInfo();
      }
    });
  } else if (regionName) {
    alert("Please select the regions which have members.");
  } else {
    alert("Please select a region or multiple regions from the diagram.");
  }
}

function init_g(xmlurl, tsvurl, create_url, group_type) {
  myURL = create_url;
  myType = group_type;
  myHash = new Array();
  var gcConfig = {
    vennPanel: "gse-venndiagram",
    groups: []
  };
  groupCompare = new GroupCompare.GroupCompare(gcConfig);
  groupCompare.addSelectionListener(selectionListener);

  d3.xml(xmlurl, "application/xml", function(xml) {
    var groups = xml.documentElement.getElementsByTagName("group");
    // console.log("group_type=" + group_type + " groups: " + groups);
    // console.log(groups);
    for (var i = 0; i < groups.length; i++) {
      var group = createGroupFromXML(groups[i]);
      groupCompare.addGroup(group);
    }
    groupsLoaded(groups.length);
  });

  d3.tsv(tsvurl, function(data) {
    data.forEach(function(d) {
      if (group_type === 'Genome') {
        myHash[d["Genome Id"]] = d;// d[" Genome Name "];
      }
      else if (group_type === 'Feature') {
        myHash[d["Feature Id"]] = d;// d[" Locus Tag "];
      }
      else {
        myHash[d["Experiment Id"]] = d;// d["Title"];
      }
    });
  });
}

function groupsLoaded(length) {
  if (length == 1 || length > 3) {
    alert("Please select two or three groups to compare");
  } else if (length == 2) {
    groupCompare.createDisplayTwo();
    populateGroupTable();
  } else {
    groupCompare.createDisplay();
    populateGroupTable();
  }
}

function createGroupFromXML(groupElement) {
  var group = {};
  var name = groupElement.getElementsByTagName("name")[0].childNodes[0].nodeValue.trim();
  group.name = name.trim();
  var members = groupElement.getElementsByTagName("members")[0].childNodes[0].nodeValue.trim();
  var memberSplit = members.split("\n");
  group.members = memberSplit;
  for (var i = 0; i < group.members.length; i++) {
    group.members[i] = group.members[i].trim();
  }
  return group;
}

function populateGroupTable() {
  groups = groupCompare.getGroups();
  /*
  var groupArea = d3.select("#gse-groups");
  var groupTable = groupArea.append("table");
  var thead = groupTable.append("thead");
  thead.append("th").attr("width", "130px").text("Group Name");
  thead.append("th").text("Members");
  var tbody = groupTable.append("tbody");
  for (var i = 0, ilen = groups.length; i < ilen; ++i) {
    var tr = tbody.append("tr");
    tr.append("td").text(groups[i].name);
    tr.append("td").text(groups[i].members.length);
  } */
}

function colorChoice(default_color) {
  var groups = groupCompare.getGroups();
  var g0 = d3.select("#g0_circle");
  var g1 = d3.select("#g1_circle");
  var g2 = d3.select("#g2_circle");
  var g0_s = d3.select("#g0_stroke");
  var g1_s = d3.select("#g1_stroke");
  var g2_s = d3.select("#g2_stroke");

  if (default_color === 'N') {
    g0.classed("venn_circle", false);
    g0.classed("venn_circle_color1", true);
    g0_s.classed("venn_circle_stroke", false);
    g0_s.classed("venn_circle_stroke_color", true);

    g1.classed("venn_circle", false);
    g1.classed("venn_circle_color2", true);
    g1_s.classed("venn_circle_stroke", false);
    g1_s.classed("venn_circle_stroke_color", true);

    if (groups.length > 2) {
      g2.classed("venn_circle", false);
      g2.classed("venn_circle_color3", true);
      g2_s.classed("venn_circle_stroke", false);
      g2_s.classed("venn_circle_stroke_color", true);
    }
  } else {
    g0.classed("venn_circle_color1", false);
    g0.classed("venn_circle", true);
    g0_s.classed("venn_circle_stroke_color", false);
    g0_s.classed("venn_circle_stroke", true);

    g1.classed("venn_circle_color2", false);
    g1.classed("venn_circle", true);
    g1_s.classed("venn_circle_stroke_color", false);
    g1_s.classed("venn_circle_stroke", true);

    if (groups.length > 2) {
      g2.classed("venn_circle_color3", false);
      g2.classed("venn_circle", true);
      g2_s.classed("venn_circle_stroke_color", false);
      g2_s.classed("venn_circle_stroke", true);
    }
  }
}

function replaceSVGClass(svghtml) {
  var venn_circle = 'class="venn_circle"';
  var venn_circle_color1 = 'class="venn_circle_color1"';
  var venn_circle_color2 = 'class="venn_circle_color2"';
  var venn_circle_color3 = 'class="venn_circle_color3"';
  var venn_region = 'class="venn_region"';
  var venn_region_active = 'class="venn_region active"';
  var venn_circle_stroke = 'class="venn_circle_stroke"';
  var venn_circle_stroke_color = 'class="venn_circle_stroke_color"';
  var region_label = 'class="region_label"';
  var circle_label = 'class="circle_label"';
  var venn_circle_replace = 'style="fill: #D2D2D2; fill-opacity: 1;"';
  var venn_circle_color1_replace = 'style="fill: blue; fill-opacity: 0\.6;"';
  var venn_circle_color2_replace = 'style="fill: yellow; fill-opacity: 0\.6;"';
  var venn_circle_color3_replace = 'style="fill: green; fill-opacity: 0\.6;"';
  var venn_region_replace = 'style="fill-opacity: 0;"';
  var venn_region_active_replace = 'style="fill: #DBE8EE; fill-opacity: 1;"';
  var venn_circle_stroke_replace = 'style="stroke: #34698E; stroke-width:2px; fill-opacity: 0;"';
  var venn_circle_stroke_color_replace = 'style="stroke: black; stroke-width:1px; fill-opacity: 0;"';
  var region_label_replace = 'style="fill: black; font-size: 10px;"';
  var circle_label_replace = 'style="fill: black; font-size: 10px;"';
  var replace_circle = svghtml
    .replace(new RegExp(venn_circle, 'g'), venn_circle_replace)
    .replace(new RegExp(venn_circle_color1, 'g'), venn_circle_color1_replace)
    .replace(new RegExp(venn_circle_color2, 'g'), venn_circle_color2_replace)
    .replace(new RegExp(venn_circle_color3, 'g'), venn_circle_color3_replace);

  var replace_region = replace_circle
    .replace(new RegExp(venn_region, 'g'), venn_region_replace)
    .replace(new RegExp(venn_region_active, 'g'), venn_region_active_replace)
    .replace(new RegExp(venn_circle_stroke, 'g'), venn_circle_stroke_replace)
    .replace(new RegExp(venn_circle_stroke_color, 'g'), venn_circle_stroke_color_replace);

  var replace_svghtml = replace_region
    .replace(new RegExp(region_label, 'g'), region_label_replace)
    .replace(new RegExp(circle_label, 'g'), circle_label_replace);

  // console.log(replace_svghtml);
  return replace_svghtml;
}

// This works for Chrome, Firefox, may not for safari
function saveSVG2PNG() {
  var html = d3.select("svg").attr("version", 1.1).attr("xmlns", "http://www.w3.org/2000/svg").node();
  // need to declare namespace, set xmlns:xlink using setAttribute; .attr("xmlns:xlink", "http://www.w3.org/1999/xlink") does not work
  html.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");

  // viewbox doesn't work for downloading PNG in Firefox
  var viewbox = 'viewBox="0 0 400 400"';
  var viewbox2 = 'preserveAspectRatio="xMinYMin meet"';
  var viewbox_replace = 'width="500" height="500"';

  var innerhtml = replaceSVGClass(html.parentNode.innerHTML)
     .replace(new RegExp(viewbox, 'g'), viewbox_replace)
     .replace(new RegExp(viewbox2, 'g'), "");
  // console.log(innerhtml);

  var imgsrc = 'data:image/svg+xml;base64,' + btoa(innerhtml);
  var image = new Image;
  image.src = imgsrc;
  image.onload = function() {
    // console.log("in image onload");
    // console.log(image);
    if (!image.complete) {
      console.log("load not complete");
    }
    downloadImg(image);
  };
}

function downloadImg(image) {
  var canvas = document.querySelector("canvas");
  var context = canvas.getContext("2d");
  // console.log("in image download");

  // catch NS_ERROR_NOT_AVAILABLE error for Firefox
  try {
    context.drawImage(image, 0, 0);
  } catch (e) {
    if (e.name == "NS_ERROR_NOT_AVAILABLE") {
      console.log(image.complete);
    } else {
      throw e;
    }
  }

  // handle browser Safari
  if (navigator.userAgent.search("Safari") >= 0 && navigator.userAgent.search("Chrome") < 0) {
    alert("Saving image as PNG is not supported for Safari. Please try Chrome or Firefox.");
  }

  var canvasdata = canvas.toDataURL("image/png");
  // var newdata = canvasdata.replace(/^data:image\/png/,'data:application/octet-stream'); //working
  var newdata = canvasdata.replace("image/png", "image/octet-stream");
  var a = document.createElement("a"); // required for Firefox, optional for Chrome
  document.body.appendChild(a);
  a.download = "venndiagram.png";
  a.href = newdata;
  a.target = "_self"; // required for Firefox, optional for Chrome
  a.click();
}

function saveSVG() {
  var html = d3.select("svg").attr("version", 1.1).attr("xmlns", "http://www.w3.org/2000/svg").node();
  // need to declare namespace, set xmlns:xlink using setAttribute;
  // .attr("xmlns:xlink", "http://www.w3.org/1999/xlink") does not work
  html.setAttribute("xmlns:xlink", "http://www.w3.org/1999/xlink");

  var innerhtml = replaceSVGClass(html.parentNode.innerHTML);

  var imgsrc = 'data:image/svg+xml;base64,' + btoa(innerhtml);
  var a = document.createElement("a"); // required for Firefox, optional for Chrome
  document.body.appendChild(a); // TODO: why add this before setting attributes of a tag?
  a.download = "venndiagram.svg";
  a.href = imgsrc;
  a.target = "_self"; // required for Firefox, optional for Chrome
  a.click();
}

Ext.onReady(function() {
  init_g('<%=groups_url%>', '<%=data_url%>', '<%=create_group_url%>', '<%=_grTypeString%>');
});
//]]>
</script>