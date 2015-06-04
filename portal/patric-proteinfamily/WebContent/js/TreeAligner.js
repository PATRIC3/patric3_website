function TreeAlignerStateObject(windowID, serveResource, getContextPath, figfamIds, product) {
	// save initial values
	this.windowID = windowID;
	this.serveURL = serveResource;
	this.contextPath = getContextPath;
	// set duration at 30 days or desired value
	this.duration = defaultDuration;
	this.figfamNames = figfamIds;
	this.description = product;

	this.loadData = TreeAlignerLoadData;
	this.saveData = TreeAlignerSaveData;
}

//create a method for the required saveData link
function TreeAlignerSaveData(windowID, namespace) {
}

function TreeAlignerLoadData(namespace) {
}

function TreeAlignerOnReady(windowID, resourceURL, contextPath, featureIds, figfamId, product) {
	/*var waitMask =
	 new Ext.LoadMask(windowID + "_forApplet",
	 {msg:"Computing Tree and Alignment ..."});
	 waitMask.show();*/

	Ext.get(windowID).mask('Computing Tree and Alignment ...');

	// register windowID to insure that state values gets stored to a cookie
	//   on page exits or refreshes
	addWindowID(windowID);

	// create a default state object with the critical values
	//   provided by the server
	var stateObject = new TreeAlignerStateObject(windowID, resourceURL, contextPath, figfamId, product);

	//  try to get other state values that might have been saved in a cookie
	loadStateObject(windowID, stateObject);

	Ext.Ajax.request({
		url : stateObject.serveURL,
		method : 'POST',
		timeout : 6000000,
		params : {
			callType : "alignFromFeatures",
			featureIds : featureIds
		},
		success : function(rs) {
			insertMSAJS(windowID, rs);
		}
	});
}

function insertMSAJS(windowID, ajaxHttp) {
	var stateObject = getStateObject(windowID);
	var data = (ajaxHttp.responseText).split("\f");
	path = data[1];
	var nexusData = data[0].split("\t");

	var sumText = "<table width='100%' border='0' cellpadding='10'>";
	sumText += "<tr>";
	if (0 < (stateObject.figfamNames).length) {
		sumText += "<td width='40%' valign='top'  style='line-height:180%'>";
		sumText += "<b>Protein Family ID: </b>" + stateObject.figfamNames + "<br />";
		sumText += "<b>Product: </b>" + stateObject.description + "</td>";
	}
	var rowCount = nexusData[0];
	sumText += "<td width='20%' valign=top style='line-height:150%'>";
	sumText += "<b>No. of Members: </b>" + rowCount + "<br />";
	sumText += "<b>No. of Species: </b>" + nexusData[1];
	sumText += "</td><td width='20%' valign=top style='line-height:150%'>";
	sumText += "<b>Min AA Length: </b>" + nexusData[2] + "<br />";
	sumText += "<b>Max AA Length: </b>" + nexusData[3];
	sumText += "</td><td width='20%' valign=top style='line-height:150%'>";
	sumText += "<b>Printable Alignment </b>";
	sumText += "<a href=\"javascript:getGblocks('" + windowID + "')\" >For all " + rowCount + " members</a>";
	//&nbsp; (";
	//sumText += "<a href=\"javascript:getClustalW('"+ windowID + "')\" >ClustalW</a>)";
	sumText += "<br /><b>Printable Tree </b>";
	sumText += "<a href=\"javascript:getPrintableTree('" + windowID + "')\" >For all " + rowCount + " members</a>&nbsp; (";
	sumText += "<a href=\"javascript:getNewickTree('" + windowID + "')\" >Newick File</a>)";

	sumText += "</td>";

	sumText += "</tr></table>";

	var toSet = document.getElementById(windowID + "_summary");
	toSet.innerHTML = sumText;

	toSet = document.getElementById(windowID + "_forApplet");
        toSet.style.overflowY="scroll";
    var menuDiv =  document.createElement("div");
    menuDiv.setAttribute("id", "menuDiv");
    var msaDiv = document.createElement("div")
    msaDiv.setAttribute("id", "msaDiv");
    toSet.appendChild(menuDiv);
    toSet.appendChild(msaDiv);

    // this is a way how you use a bundled file parser
    var msa = require("msa");
        
    var opts = {};
    // set your custom properties
    // @see: https://github.com/greenify/biojs-vis-msa/tree/master/src/g
    opts.el = msaDiv;//document.getElementById("msaDiv");
    opts.bootstrapMenu=false;
    opts.vis = {
        conserv: false,
        overviewbox: false,
        seqlogo: true,
        labelName: false,
        labelId: false,
    };
    opts.conf = {
        dropImport: true
    };
    opts.zoomer = {
        menuFontsize: "12px",
        //autoResize: false,
        labelNameLength: 150,
        alignmentHeight: 4000,
        alignmentWidth: 700,
        residueFont: "12",
        rowHeight: 14.5
    };

    // init msa
    var m = new msa.msa(opts);

    // search in URL for fasta or clustal
    function getURLParameter(name) {
        return decodeURIComponent((new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(location.search) || [, ""])[1].replace(/\+/g, '%20')) || null;
    }

    function importText(text){
        var fileName, objs, ref, type;
        ref = this.parseText(text), objs = ref[0], type = ref[1];
        if (type === "error") {
        return "error";
        }
        if (type === "seqs") {
        this.msa.seqs.reset(objs);
        this.msa.g.config.set("url", "userimport");
        this.msa.g.trigger("url:userImport");
        } else if (type === "features") {
        this.msa.seqs.addFeatures(objs);
        } else if (type === "newick") {
        this.msa.u.tree.loadTree((function(_this) {
            return function() {
            return _this.msa.u.tree.showTree(file);
            };
        })(this));
        }
        return
    }
    //create a clustalw format string that can be passed to biojs msa
    function clustalData(data){
        var input=data.slice(5);
        var clustal=["CLUSTAL"];
        var orgs=[];
        for (var i = 0; i < input.length-2; i+=3) {
            clustal.push(input[i]+"\t"+input[i+2]);
            orgs.push(input[i+1]);
        }
        return ([clustal,orgs]);
    }


    //var defaultURL = "";
    //var defaultURL = "https://cdn.rawgit.com/greenify/msa/master/snippets/data/tree/B2014122194A560KL7I.4.ids.fa";
    //var url = getURLParameter('seq') || defaultURL;
    //m.u.file.importURL(url, renderMSA);
    
    //var nexusData= ["11", "11", "39", "53", "(((fig|1310754.3.peg.2921:0.0,fig|1310727.3.peg.2939:0.0):1.17036,(fig|47716.4.peg.2238:0.11103,fig|66869.3.peg.5501:0.00641):0.99240):2.47910,((fig|1310683.3.peg.2856:0.0,fig|1310905.3.peg.2925:0.0):1.28000,fig|1235820.4.peg.2163:0.69592):2.18740,(((fig|321314.9.peg.37:0.0,fig|476213.4.peg.33:0.0):0.29056,fig|936157.3.peg.4962:0.43338):2.89151,fig|882800.3.peg.6267:0.00016):0.00019);", "fig|1310683.3.peg.2856", "Acinetobacter_baumannii_1566109", "--------MRFLQRYPSYQDFYCRFDVICF-DFPQKIAKTVQQDFSK-FHYDLQWIENVFTLD--", "fig|1310905.3.peg.2925", "Acinetobacter_baumannii_25977_1", "--------MRFLQRYPSYQDFYCRFDVICF-DFPQKIAKTVQQDFSK-FHYDLQWIENVFTLD--", "fig|1235820.4.peg.2163", "Prevotella_oris_JCM_12252", "-----------MKERAIWDDL--RFDLISI-------VGTAPENFK------LEHIVDAFNPLLV", "fig|321314.9.peg.37", "Salmonella_enterica_subsp._enterica_serovar_Choleraesuis_str._SC-B67", "MSSPGNPGKTSDGRHTEVGSF--NYSRAAD-RSNSENVLSSGMTQS-------------------", "fig|476213.4.peg.33", "Salmonella_enterica_subsp._enterica_serovar_Paratyphi_C_strain_RKS4594", "MSSPGNPGKTSDGRHTEVGSF--NYSRAAD-RSNSENVLSSGMTQS-------------------", "fig|936157.3.peg.4962", "Salmonella_enterica_subsp._enterica_serovar_Weltevreden_str._2007-60-3289-1", "-------MIVADGRNTQVGSF--NFSRAAD-RSNSENVLVVWDDPVLARSYLNHWTSR-------", "fig|1310754.3.peg.2921", "Acinetobacter_baumannii_2887", "-----------MLVAQQLGQW--AEQTALK-LLKEQNYEWVASNYHS-RRGEVDLIENAVTN---", "fig|1310727.3.peg.2939", "Acinetobacter_baumannii_836190", "-----------MLVAQQLGQW--AEQTALK-LLKEQNYEWVASNYHS-RRGEVDLIENAVTN---", "fig|882800.3.peg.6267", "Methylobacterium_extorquens_DSM_13060", "-----------MSRAAR--AWLARHPLAADATLRADAVFVAPRRWPR-------HLPNAFEIEGL", "fig|47716.4.peg.2238", "Streptomyces_olivaceus", "-----------MNARSALGRY--GETLAAR-RLADAGMTVLERNWRCGRTGEIDIVARDKQDELH", "fig|66869.3.peg.5501", "Streptomyces_atroolivaceus", "-----------MNARGALGRY--GEDLAAR-LLADAGMTVLDRNWRC-RTGEIDIVARDEQDELH"]

    var replacement=[];
    nexusData.forEach(function(item){replacement.push(item.replace(/\|/g, "."));}, this);
    nexusData=replacement;
    var clustal,orgs;
    var inputData= clustalData(nexusData), clustal=inputData[0], orgs=inputData[1];

    //m.u.file.parseText(test, renderMSA);

    //importText.bind(this.m.u.file)(test);
    m.u.file.importFile(nexusData[4]);
    m.u.file.importFile(clustal.join("\n"));

    m.g.vis.on("change:treeLoaded", function(){
        var treeDiv=document.getElementsByClassName("tnt_groupDiv");
        treeDiv[0].parentElement.setAttribute("style", "padding-top:96px; vertical-align:top; display:inline-block");
        var msaDiv=document.getElementsByClassName("biojs_msa_stage");
        msaDiv[0].setAttribute("style", "display:inline-block");

    });
    renderMSA();
    Ext.get(windowID).unmask();
    function renderMSA() {
            // the menu is independent to the MSA container
            var menuOpts = {};
            menuOpts.el = document.getElementById('menuDiv');
            var msaDiv = document.getElementById('msaDiv');
            msaDiv.setAttribute("style", "overflow-x:scroll; white-space: nowrap;");
            menuOpts.msa = m;
            var defMenu = new msa.menu.defaultmenu(menuOpts);

            var noMenu = ["10_import", "15_ordering", "20_filter", "30_selection", "70_extra", "90_help", "95_debug"];
            noMenu.forEach(function(toRemove){delete defMenu.views[toRemove];});
            m.addView("menu", defMenu);
            // call render at the end to display the whole MSA
            m.render();
            m.el.parentElement.insertBefore(menuOpts.el, m.el);
        }
}
    
function insertTreeApplet(windowID, ajaxHttp) {
	var stateObject = getStateObject(windowID);
	var data = (ajaxHttp.responseText).split("\f");
	path = data[1];
	var nexusData = data[0].split("\t");

	var sumText = "<table width='100%' border='0' cellpadding='10'>";
	sumText += "<tr>";
	if (0 < (stateObject.figfamNames).length) {
		sumText += "<td width='40%' valign='top'  style='line-height:180%'>";
		sumText += "<b>Protein Family ID: </b>" + stateObject.figfamNames + "<br />";
		sumText += "<b>Product: </b>" + stateObject.description + "</td>";
	}
	var rowCount = nexusData[0];
	sumText += "<td width='20%' valign=top style='line-height:150%'>";
	sumText += "<b>No. of Members: </b>" + rowCount + "<br />";
	sumText += "<b>No. of Species: </b>" + nexusData[1];
	sumText += "</td><td width='20%' valign=top style='line-height:150%'>";
	sumText += "<b>Min AA Length: </b>" + nexusData[2] + "<br />";
	sumText += "<b>Max AA Length: </b>" + nexusData[3];
	sumText += "</td><td width='20%' valign=top style='line-height:150%'>";
	sumText += "<b>Printable Alignment </b>";
	sumText += "<a href=\"javascript:getGblocks('" + windowID + "')\" >For all " + rowCount + " members</a>";
	//&nbsp; (";
	//sumText += "<a href=\"javascript:getClustalW('"+ windowID + "')\" >ClustalW</a>)";
	sumText += "<br /><b>Printable Tree </b>";
	sumText += "<a href=\"javascript:getPrintableTree('" + windowID + "')\" >For all " + rowCount + " members</a>&nbsp; (";
	sumText += "<a href=\"javascript:getNewickTree('" + windowID + "')\" >Newick File</a>)";

	sumText += "</td>";

	sumText += "</tr></table>";

	var toSet = document.getElementById(windowID + "_summary");
	toSet.innerHTML = sumText;

	var jarUrl = stateObject.contextPath + '/TreeViewer.jar';
	toSet = document.getElementById(windowID + "_forApplet");

	var setter = "<object type=\"application/x-java-applet\" codebase=\".\" id=\"" + windowID + "_applet\" width='100%' height='100%'>";
	setter += "<param name=\"code\" value=\"edu/vt/vbi/patric/applets/treealign/TreeAlignment.class\" />";
	setter += "<param name=\"archive\" value=\"" + jarUrl + "\" />";
	setter += "<param name=\"NEWICK\" value=\"" + nexusData[4] + "\">";
	var alignNum = 0;
	for (var i = 5; i < nexusData.length; i++) {++alignNum;
		var pName = "ALIGN" + alignNum;
		pName = " <param name=\"" + pName + "\" value=\"" + nexusData[i] + "\">";
		setter += pName;
	}
	setter += "Applet failed to run. No Java plug-in was found. You can download Java JRE from <a href=\"http://java.com/en/download/\" target=_blank>here</a>";
	setter += " </object>";

	toSet.innerHTML = setter;
	Ext.get(windowID).unmask();
}

function getClustalW(windowID) {

	var stateObject = getStateObject(windowID);
	var insides = "<!DOCTYPE html PUBLIC " + "\"-//W3C//DTD XHTML 1.0 Transitional//EN\" " + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\" >";
	insides += "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>";
	insides += "<meta http-equiv=\"Content-Type\" " + "content=\"text/html;charset=utf-8\">";

	insides += "<link rel=\"stylesheet\" type=\"text/css\" " + "href=\"/patric/css/popup.css\" />";

	insides += "</head>\n";
	insides += "<body id='popup'>\n";
	insides += "<div id='header'>" + "<div id=\"masthead\">" + "<a href=\"/\">PATRIC" + "<span class=\"sub\">Pathosystems Resouce Integration Center" + "</span></a></div>" + "</div>\n";
	insides += "<div id=\"toppage\"><div class=\"content\">";
	insides += "<div>";
	insides += "<br />";
	insides += "<div id='forTreeView'>";
	insides += "</div></div></div></div>";
	insides += "<div id=\"footer\"> &nbsp</div></body></html>";
	var showIn = window.open("", "", "width = 1000, height=760, scrollbars = 1");
	showIn.document.write(insides);
	showIn.document.close();

	Ext.Ajax.request({
		url : stateObject.serveURL,
		method : 'POST',
		timeout : 6000000,
		params : {
			"path" : path,
			callType : "clustalW"
		},
		success : function(rs) {
			var toSet = (showIn.document).getElementById("forTreeView");
			toSet.innerHTML = rs.responseText;
		}
	});

}

function getNewickTree(windowID) {

	var appFinder = document.getElementById(windowID + "_applet");
	var treeBase = appFinder.getTreePrintData();
	var text = treeBase.split("&")[1].split("=")[1];
	text = text.substring(0, text.length - 1);

	Ext.getDom(windowID + "_form").action = "/patric-proteinfamily/jsp/GetAlignerFiles.jsp";
	Ext.getDom("data").value = text;
	Ext.getDom("fileformat").value = "newick";
	Ext.getDom(windowID + "_form").target = "";
	Ext.getDom(windowID + "_form").submit();

}

function getPrintableTree(windowID) {
	var stateObject = getStateObject(windowID);
	var insides = "<!DOCTYPE html PUBLIC " + "\"-//W3C//DTD XHTML 1.0 Transitional//EN\" " + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\" >";
	insides += "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>";
	insides += "<meta http-equiv=\"Content-Type\" " + "content=\"text/html;charset=utf-8\">";
	insides += "<title>" + stateObject.figfamNames + ": " + stateObject.description + "</title>";
	insides += "<link rel=\"stylesheet\" type=\"text/css\" " + "href=\"/patric/css/popup.css\" />";

	insides += "</head>\n";
	insides += "<body id='popup'>\n";
	insides += "<div id='header'>" + "<div id=\"masthead\">" + "<a href=\"/\">PATRIC" + "<span class=\"sub\">Pathosystems Resouce Integration Center" + "</span></a></div>" + "</div>\n";
	insides += "<div id=\"toppage\"><div class=\"content\">";
	insides += "<div>";
	if (0 < (stateObject.figfamNames).length) {
		insides += "<b>";
		insides += stateObject.figfamNames + ": " + stateObject.description;
		insides += "</b><br />";
	}
	insides += "<br />";
	insides += "<div id='forTreeView'>";
	insides += "</div></div></div></div>";
	insides += "<div id=\"footer\"> &nbsp</div></body></html>";
	var showIn = window.open("", "", "width = 1000, height=760, scrollbars = 1");
	showIn.document.write(insides);
	showIn.document.close();

	var appFinder = document.getElementById(windowID + "_applet");
	var treeBase = appFinder.getTreePrintData();
	var paramList = packParameters(treeBase, stateObject);
	Ext.Ajax.request({
		url : stateObject.serveURL,
		method : 'POST',
		timeout : 6000000,
		params : paramList,
		success : function(rs) {
			popRawTree(rs, stateObject.contextPath, stateObject.serveURL, showIn);
		}
	});

}

function popRawTree(ajaxHttp, contextPath, ajaxURL, showIn) {
	var treeSplit = (ajaxHttp.responseText).split("\t");
	var url = ajaxURL + '&' + "callType=retrieveTreePng";
	url += '&' + "TREE_PNG=" + treeSplit[0];
	var toSet = (showIn.document).getElementById("forTreeView");
	toSet.innerHTML = "";
	var setter = "<IMG src='" + url + "' /><br />";
	for (var i = 1; i < treeSplit.length; i++) {
		setter += "<br />" + treeSplit[i];
	}
	toSet.innerHTML = setter;
}

function getGblocks(windowID) {
	var stateObject = getStateObject(windowID);
	var insides = "<!DOCTYPE html PUBLIC " + "\"-//W3C//DTD XHTML 1.0 Transitional//EN\" " + "\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\" >";
	insides += "<html xmlns=\"http://www.w3.org/1999/xhtml\"><head>";
	insides += "<meta http-equiv=\"Content-Type\" " + "content=\"text/html;charset=utf-8\">";

	insides += "<style media=screen type=text/css><!--\n" + ".BL {background-color:navy;color:navy}\n" + ".A1 {background-color:black;color:lime}\n" + ".G1 {background-color:black;color:lime}\n" + ".S1 {background-color:black;color:lime}\n" + ".T1 {background-color:black;color:lime}\n" + ".C1 {background-color:black;color:orange}\n" + ".P1 {background-color:black;color:aqua}" + ".D1 {background-color:black;color:white}\n" + ".E1 {background-color:black;color:white}\n" + ".Q1 {background-color:black;color:white}\n" + ".N1 {background-color:black;color:white}\n" + ".F1 {background-color:black;color:yellow}\n" + ".W1 {background-color:black;color:yellow}\n" + ".Y1 {background-color:black;color:yellow}\n" + ".H1 {background-color:black;color:red}\n" + ".K1 {background-color:black;color:red}\n" + ".R1 {background-color:black;color:red}\n" + ".I1 {background-color:black;color:fuchsia}\n" + ".L1 {background-color:black;color:fuchsia}\n" + ".M1 {background-color:black;color:fuchsia}\n" + ".V1 {background-color:black;color:fuchsia}\n" + "--></style>\n";
	insides += "<link rel=\"stylesheet\" type=\"text/css\" " + "href=\"/patric/css/popup.css\" />";
	insides += "</head>\n";
	insides += "<body id='popup'>\n";
	insides += "<div id='header'>" + "<div id=\"masthead\">" + "<a href=\"/\">PATRIC" + "<span class=\"sub\">Pathosystems Resouce Integration Center" + "</span></a></div>" + "</div>\n";
	insides += "<div id=\"toppage\"><div class=\"content\">";
	insides += "<div id='forGblocks'>";
	insides += "</div></div></div>";
	insides += "<div id=\"footer\"> &nbsp</div></body></html>";
	var showIn = window.open("", "", "width = 1000, height=760, scrollbars = 1");
	showIn.document.write(insides);
	showIn.document.close();

	var appFinder = document.getElementById(windowID + "_applet");
	var gblocksBase = appFinder.getGblocksData();
	var paramList = packParameters(gblocksBase, stateObject);
	Ext.Ajax.request({
		url : stateObject.serveURL,
		method : 'POST',
		timeout : 6000000,
		params : paramList,
		success : function(rs) {
			popGblocks(rs, showIn);
		}
	});

}

function packParameters(fromApplet, stateObject) {
	fromApplet += '&figfamId=' + stateObject.figfamNames;
	fromApplet += '&description=' + stateObject.description;
	return fromApplet;
}

function popGblocks(ajaxHttp, showIn) {
	var toSet = (showIn.document).getElementById("forGblocks");
	toSet.innerHTML = "";
	toSet.innerHTML = ajaxHttp.responseText;
}

