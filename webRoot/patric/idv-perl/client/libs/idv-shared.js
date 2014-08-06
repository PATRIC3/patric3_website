Object.size = function(obj) {
	var size = 0, key;
	for (key in obj) {
		if (obj.hasOwnProperty(key)) size++;
	}
	return size;
};

Object.isInt = function(val) {
	if (val === "0") { return true; }
	return parseInt(val+0) == val;
};

Array.has = function(arr, val) {
	for (var i=0; i<arr.length; i++) {
		if (arr[i] == val) { return true; }
	}
	return false;
};

Array.rmIndx = function(arr, indx) {
	arr.splice(indx, 1);
	return arr;
};

Array.rmVal = function(arr, val) {
	for (var i=0; i<arr.length; i++) {
		if (val == arr[i]) { arr.splice(i, 1); break; }
	}
	return arr;
};

String.capwords = function(str) {
	return str.replace( /(^|\s)([a-z])/g , function(m,p1,p2) { 
			return p1+p2.toUpperCase(); 
	});
};

/**
	* returns a formatted url for querying an external db.
	* accepted input (case insensitive):
	* 
	* 	pubmed-id:pubmed id
	* 	uniprot-id:uniprot id
	* 	tax-id:taxon id
	* 	tax-name:organism name
	* 	patric-id: patric feature id
	* 	ncbi-taxid:ncbi taxon id
	* 	ncbi-pid:gi number (protein)
	* 	ncbi-nid:gi number (gene)
	* 	ncbi-term:ncbi term
	* 	mesh-id:mesh id
	* 	mesh-term:mesh term
	* 	mesh-tree:mesh tree node
	* 	vfdb-id:vf id
	* 	vfdb-gid:vf gene id
	* 	ext:resource
*/
String.xref = function(str) {
	var url = undefined;
	var tokens = str.split(":");
	if (!tokens[0] || !tokens[1]) {
		return "http://www.ncbi.nlm.nih.gov/gquery/?term=" + str.replace(/\s/gi, "+");
	}
	tokens[0] = tokens[0].toLowerCase();
	var term = tokens[1];
	
	// for general resources
	if ("ext" == tokens[0]) {
		switch (tokens[1].toLowerCase()) {
			case "gad" : 
				url = "http://geneticassociationdb.nih.gov/cgi-bin/index.cgi";
				break;
			case "ctd" : 
				url = "http://ctd.mdibl.org/";
				break;
			case "ncbi" : 
				url = "http://www.ncbi.nlm.nih.gov/";
				break;
			case "mesh" : 
				url = "http://www.nlm.nih.gov/mesh/meshhome.html";
				break;
			case "patric" : 
				url = "http://www.patricbrc.org/";
				break;
			case "vfdb" : 
				url = "http://www.mgc.ac.cn/VFs/main.htm";
				break;
			default    : 
				url = "http://www.patricbrc.org/";
		}
		return url;
	}
	// for specific queries to particular resources
	var db = tokens[0].toLowerCase().split("-");
	if (!db[0] || !db[1]) {
		return "http://www.ncbi.nlm.nih.gov/gquery/?term=" + term.replace(/\s/gi, "+");
	}
	switch (db[0]) {
		case "patric" : 
			switch (db[1]) {
				case "id" : 
					url = "http://www.patricbrc.org/portal/portal/patric/Feature?cType=feature&cId=" + term;
					break;
			}
			break;
		case "pubmed" : 
			switch (db[1]) {
				case "id" : 
					url = "http://www.ncbi.nlm.nih.gov/pubmed/" + term;
					break;
			}
			break;
		case "uniprot" : 
			switch (db[1]) {
				case "id" : 
					url = "http://www.uniprot.org/uniprot/" + term;
					break;
			}
			break;
		case "tax" : 
			switch (db[1]) {
				case "id" : 
					url = "http://www.patricbrc.org/portal/portal/patric/Taxon?cType=taxon&cId=" + 
								term;
					break;
				case "name" : 
					url = "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Info&name=" + 
								term.replace(/\s/gi, "+");
					break;
			}
			break;
		case "ncbi" : 
			switch (db[1]) {
				case "taxid" : 
					url = "http://www.ncbi.nlm.nih.gov/Taxonomy/Browser/wwwtax.cgi?mode=Info&id=" + 
								term;
					break;
				case "pid" : 
					url = "http://www.ncbi.nlm.nih.gov/protein/" + term;
					break;
				case "gid" : 
					url = "http://www.ncbi.nlm.nih.gov/nucleotide/" + term;
					break;
				case "term" : 
					url = "http://www.ncbi.nlm.nih.gov/gquery/?term=" + term.replace(/\s/gi, "+");
					break;
			}
			break;
		case "mesh" : 
			switch (db[1]) {
				case "id" : 
					url = "http://www.ncbi.nlm.nih.gov/gquery/?term=MeSH+" + term;
					break;
				case "tree" : 
					url = "http://www.ncbi.nlm.nih.gov/gquery/?term=MeSH+" + term;
					break;
				case "term" : 
					url = "http://www.nlm.nih.gov/cgi/mesh/2011/MB_cgi?mode=&term=" + 
								term.replace(/\s/gi, "+");
					break;
			}
			break;
		case "vfdb" : 
			switch (db[1]) {
				case "id" : 
					url = "http://www.mgc.ac.cn/VFs/search_VFs.htm";
					break;
				case "gid" : 
					url = "http://www.mgc.ac.cn/VFs/search_VFs.htm";
					break;
			}
			break;
	}
	if (!url) {
		url = "http://www.ncbi.nlm.nih.gov/gquery/?term=" + term.replace(/\s/gi, "+");
	}
	return url;
};


/**
	* adds an event handler to a DOM node
*/
function hookEvent(elem, eventName, callback) {
	if (typeof(elem) == "string") { elem = document.getElementById(elem); } 
	if (elem == null) { return; }
	if (elem.addEventListener) {
		if (eventName == "mousewheel") {
			elem.addEventListener("DOMMouseSCroll", callback, false);
		}
		elem.addEventListener(eventName, callback, false);
	} else if (elem.attachEvent) {
		elem.attachEvent("on" + eventName, callback);
	}
}

/**
	* removes an event handler from a DOM node
*/
function unhookEvent(elem, eventName, callback) {
	if (typeof(elem) == "string") { elem = document.getElementById(elem); }
	if (elem == null) { return; }
	if (elem.removeEventListener) {
		if (eventName == "mousewheel") {
			elem.removeEventListener("DOMMouseSCroll", callback, false);
		}
		elem.removeEventListener(eventName, callback, false);
	} else if (elem.detachEvent) {
		elem.detachEvent("on" + eventName, callback);
	}
}

/**
	* wrapper to mask browser differences in handling events
*/
function getEvent(evt) {
	if (!evt) { return window.event; }
	return evt;
}

/**
	* wrapper to mask browser differences in handling event targets
*/
function getEventTarget(evt) {
	if (!evt) { evt = window.event; }
	if (evt.target) { return evt.target; }
	return evt.srcElement;
}


/** 
	* stops an event from bubbling up to all DOM nodes
*/
function cancelEvent(evt) {
	if (!evt) { evt = window.event; }
	if (evt.stopPropagation) { evt.stopPropagation(); }
	if (evt.preventDefault) { evt.preventDefault(); }
	evt.cancelBubble = true;
	evt.cancel = true;
	evt.returnValue = false;
	return false;
}


/** 
	* mask browser differences in how wheel data is returned. follows the 
	* Firefox model:
	*
	* 	scroll down : wheel roll toward user 		: negative delta
	* 	scroll up   : wheel roll away from user : positive delta
	* 
*/
function getWheelDelta(evt) {
	if (!evt) { evt = window.event; }
	var wheelDelta = evt.detail ? evt.detail * -1 : evt.wheelDelta / 40;
	return wheelDelta;
}

