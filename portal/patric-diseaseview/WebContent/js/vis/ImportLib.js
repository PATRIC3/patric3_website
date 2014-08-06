
/**
	* @class edu.vt.vbi.patric.vis.ImportException
	* Class for handling import errors.
	*
	* @history
	*
	* @constructor
	* Creates a new ImportException.
	* @param {String} msg A user-friendly message describing the error.
*/
edu.vt.vbi.patric.vis.ImportException = function(msg) {
	this.message = "ImportException from [" + arguments.callee.caller.name + "]. " + msg;
};



/**
 * @class edu.vt.vbi.patric.vis.Importer
 * Parent class for a vis data importer. Specific implementations will extend 
 * this class.
 *
 * @history
 *
 * @constructor
 * Creates a new Importer.
 */
edu.vt.vbi.patric.vis.Importer = function() {};
edu.vt.vbi.patric.vis.Importer.prototype = {};


if (!this.edu.vt.vbi.patric.vis.importer)	{ this.edu.vt.vbi.patric.vis.importer = {}; }


/**
	* @class edu.vt.vbi.patric.vis.importer.MItab
	* A class for importing MItab (Molecular Interaction) data.
	*
	* @extends edu.vt.vbi.patric.vis.Importer
	*
	* @history
	*
	* @constructor
	* Create a new MItab importer.
*/
edu.vt.vbi.patric.vis.importer.MItab = function() {

	edu.vt.vbi.patric.vis.Importer.call(this);

	// column headings for mitab data
	this.colHdrs = [
		"ids-a", 													// 0
		"ids-b", 													// 1
		"ids-alt-a", 											// 2
		"ids-alt-b", 											// 3
		"aliases-a", 											// 4
		"aliases-b", 											// 5
		"interaction-detection-methods", 	// 6
		"authors", 												// 7
		"pubids", 												// 8
		"taxa-a", 												// 9
		"taxa-b", 												// 10
		"interaction-types", 							// 11
		"interaction-sources", 						// 12
		"interaction-ids-in-sources", 		// 13
		"confidences" 										// 14
	];

	// regular expression to extract a mitab entry
	// for in-progress mod parseInteraction2
	this.entryRE = /(.+?)\:(.+?)\||$/gi;
	//edu.vt.vbi.patric.importer.Mitab.prototype.entryRE = /(.+)\|/gi;

};
edu.vt.vbi.patric.vis.importer.MItab.inheritsFrom(edu.vt.vbi.patric.vis.Importer);

/** 
	* Reads a mitab network from a string and returns a result object 
	* containing the network. See parseNetwork for more info.
	* @param {String} data The mitab data as a single string.
*/
edu.vt.vbi.patric.vis.importer.MItab.prototype.readFromString = function(data) {
	return this.parseNetwork(data);
};

/**
	* Parses network data from a mitab-formatted string. MItab is a tab-delimited, 
	* headerless format for molecular interactions. It has the following 
	* required columns:
	*
	* 	 0	unique id(s) for interactorA		databaseName:ac|..
	* 	 1	unique id(s) for interactorB		databaseName:ac|..
	* 	 2	alt id(s) for interactor A			databaseName:ac|..
	* 	 3	alt id(s) for interactor B			databaseName:ac|..
	* 	 4	aliases(s) for interactor A			databaseName:ac|..
	* 	 5	aliases(s) for interactor B			databaseName:ac|..
	* 	 6	interaction detection methods		databaseName:methodId(methodName)|..
	* 	 7	first author(s) on pub(s)				Surname-altids|..
	* 	 8	publication id									databaseName:pubid|..
	* 	 9	taxon id for interactorA				databaseName:taxid(name)|..
	* 	10	taxon id for interactorB				databaseName:taxid(name)|..
	* 	11	interaction type(s)							databaseName:id(interactionType)|..
	* 	12	source(s)												databaseName:id(sourceName)|..
	* 	13	interaction id in source(s)			databaseName:id|..
	* 	14	confidence score								scoreType:value|..
	* 
	* @param {String} data The mitab data as a single string.
*/
edu.vt.vbi.patric.vis.importer.MItab.prototype.parseNetwork = function(data) {

	var result = { nodes: [], edges: [] };
	
	if (!data) { return result; }
	
	var nodeMap = {};
	var rows = data.split("\n");
	//this.entryRE.compile(this.entryRE);
	for (var i=0; i<rows.length; i++) {
		var row = rows[i].split("\t");
		// skip malformed rows
		if (row.length < this.colHdrs.length) { continue; }
		var interaction = this.parseInteraction(row);
		
		// get nodeA, or add if does not exist
		var idA = undefined;
		for (var k=0; k<interaction["ids-a"].length; k++) {
			var id = interaction["ids-a"][k].key + ":" + interaction["ids-a"][k].val;
			if (id in nodeMap) {
				idA = id;
				break;
			}
		}
		if (typeof idA == "undefined") {
			idA = interaction["ids-a"][0].key + ":" + interaction["ids-a"][0].val;
			var nodeA = { id		: idA, 
										label : idA, 
										taxid : parseInt(interaction["taxa-a"][0].val), 
										taxon	: interaction["taxa-a"][0].alt 
			};
			if (interaction["aliases-a"].length > 0) {
				nodeA.alias = interaction["aliases-a"][0].key + ":" + interaction["aliases-a"][0].val;
				nodeA.label = interaction["aliases-a"][0].val;
			}
			result.nodes.push(nodeA);
			nodeMap[idA] = true;
			/*
			if (!(interaction["taxa-a"][0].alt in spMap)) {
				spMap[interaction["taxa-a"][0].alt] = {};
				counts[interaction["taxa-a"][0].alt] = 0;
			}
			spMap[interaction["taxa-a"][0].alt][idA] = true;
			*/
		}
		
		// get nodeB, or add if does not exist
		var idB = undefined;
		for (var k=0; k<interaction["ids-b"].length; k++) {
			var id = interaction["ids-b"][k].key + ":" + interaction["ids-b"][k].val;
			if (id in nodeMap) {
				idB = id;
				break;
			}
		}
		if (typeof idB == "undefined") {
			idB = interaction["ids-b"][0].key + ":" + interaction["ids-b"][0].val;
			var nodeB = { id		: idB, 
										label : idB, 
										taxid : parseInt(interaction["taxa-b"][0].val), 
										taxon	: interaction["taxa-b"][0].alt 
			};
			if (interaction["aliases-b"].length > 0) {
				nodeB.alias = interaction["aliases-b"][0].key + ":" + interaction["aliases-b"][0].val;
				nodeB.label = interaction["aliases-b"][0].val;
			}
			result.nodes.push(nodeB);
			nodeMap[idB] = true;
			/*
			if (!(interaction["taxa-b"][0].alt in spMap)) {
				spMap[interaction["taxa-b"][0].alt] = {};
				counts[interaction["taxa-b"][0].alt] = 0;
			}
			spMap[interaction["taxa-b"][0].alt][idB] = true;
			*/
		}
		
		// add the a-b edge
		var edge = { id: "e" + result.network.edges.length, 
								 source				: idA, 
								 target				: idB, 
								 methods 			: [], 
								 types 				: [], 
								 refs 				: [], 
								 scores				: [], 
								 score_types	: [] };
		for (var k=0; k<interaction["interaction-detection-methods"].length; k++) {
			var str = interaction["interaction-detection-methods"][k].key + ":" + 
								interaction["interaction-detection-methods"][k].val;
			if ("alt" in interaction["interaction-detection-methods"][k]) {
				str = interaction["interaction-detection-methods"][k].alt + " - " + str;
			}
			edge.methods.push(str);
		}
		edge.methods = edge.methods.join(";");

		for (var k=0; k<interaction["interaction-types"].length; k++) {
			var str = interaction["interaction-types"][k].key + ":" + 
								interaction["interaction-types"][k].val;
			if ("alt" in interaction["interaction-types"][k]) {
				str = interaction["interaction-types"][k].alt + " - " + str;
			}
			edge.types.push(str);
		}
		edge.types = edge.types.join(";");
		
		for (var k=0; k<interaction["pubids"].length; k++) {
			var str = interaction["pubids"][k].key + ":" + interaction["pubids"][k].val;
			edge.refs.push(str);
		}
		edge.refs = edge.refs.join(";");

		for (var k=0; k<interaction["confidences"].length; k++) {
			edge.scores.push(parseFloat(interaction["confidences"][k].val));
			edge.score_types.push(interaction["confidences"][k].key);
		}
		edge.scores = edge.scores.join(";");
		edge.score_types = edge.score_types.join(";");

		result.edges.push(edge);
	}
	return result;
};

/**
	* Reads a MItab interaction from an array of strings in mitab format (e.g., 
	* databaseName:value). Returns an object of arrays of objects, each containing 
	* the key:value pairs. Any parenthetical value is stored in the obj.alt field. 
	* Any bare value is stored in the obj.val field.
	*
	* example output:
	*
	* interaction["id-a"] = [ {key: "uniprotkb", val: "Q86675"}, 
	*													{key: "intact", val: "EBI-930865"} ];
	*
	* @param {Array} cols The 14 required columns of MItab data for this interaction.
*/
edu.vt.vbi.patric.vis.importer.MItab.prototype.parseInteraction = function(cols) {
	var interaction = {};
	for (var j=0; j<cols.length; j++) {
		var entries = [];
		var arr = cols[j].split("|");
		for (var i=0; i<arr.length; i++) {
			if (arr[i] == "" || arr[i] == "-") { continue; }
			var obj = {};
			var nvpair = arr[i].split(":");
			if (nvpair.length == 1) {
				obj.val = nvpair[0];
			} else if (nvpair.length > 1) {
				obj.key = nvpair[0];
				// check for parenthetical value
				var atoms = nvpair[1].split("(");
				if (atoms.length > 1) {
					obj.val = atoms.shift();
					obj.alt = atoms.join("(").slice(0, -1);
				} else {
					obj.val = nvpair[1];
				}
			}
			entries.push(obj);
		}
		interaction[this.colHdrs[j]] = entries;
	}
	return interaction;
}; 

edu.vt.vbi.patric.vis.importer.MItab.prototype.parseInteraction2 = function(cols) {
	var interaction = {};
	for (var i=0; i<cols.length; i++) {
		var match = this.entryRE.exec(cols[i]);
		var m = match;
	}
};



/**
	* @class edu.vt.vbi.patric.importer.HPI
	* A class for importing host-pathogen interaction data.
	*
	* @extends edu.vt.vbi.patric.vis.Importer
	*
	* @history
	*
	* @constructor
	* Create a new HPI importer.
*/
edu.vt.vbi.patric.vis.importer.HPI = function() {
	edu.vt.vbi.patric.vis.Importer.call(this);
};
edu.vt.vbi.patric.vis.importer.HPI.inheritsFrom(edu.vt.vbi.patric.vis.Importer);


/**
	* Reads data from a json data structure and formats it for a network.
	* @param {Object} data The json object containg the data.
*/
edu.vt.vbi.patric.vis.importer.HPI.prototype.readFromJson = function(data) {
	var output = {};
	// minimum required for a graph is a set of nodes
	if (data.hasOwnProperty("nodes")) {
		output.nodes = data.nodes;
		(data.hasOwnProperty("edges")) ? output.edges = data.edges : output.edges = [];
		output.categories = {};
		(data.hasOwnProperty("taxa")) ? output.categories.taxa = data.taxa : output.categories.taxa = {};
		(data.hasOwnProperty("type")) ? output.categories.type = data.type : output.categories.type = {};
		(data.hasOwnProperty("method")) ? output.categories.method = data.method : output.categories.method = {};
		(data.hasOwnProperty("source")) ? output.categories.source = data.source : output.categories.source = {};
		(data.hasOwnProperty("reference")) ? output.categories.reference = data.reference : output.categories.reference = {};
		(data.hasOwnProperty("mol")) ? output.categories.mol = data.mol : output.categories.mol = {};
		//(data.hasOwnProperty("synonyms")) ? output.categories.synonyms = data.synonyms : output.categories.synonyms = {};
		return output;
	} else {
		throw new edu.vt.vbi.patric.vis.ImportException(
			"Unable to import HPI network. No recognizable nodes array in input data."
		);
	}
};
	


/**
	* @class edu.vt.vbi.patric.importer.IDV
	* A class for importing Infectious Disease View data.
	*
	* @extends edu.vt.vbi.patric.vis.Importer
	*
	* @history
	*
	* @constructor
	* Create a new IDV importer.
*/
edu.vt.vbi.patric.vis.importer.IDV = function() {
	edu.vt.vbi.patric.vis.Importer.call(this);
};
edu.vt.vbi.patric.vis.importer.IDV.inheritsFrom(edu.vt.vbi.patric.vis.Importer);


/**
	* Reads data from a json data structure and formats it for an IDV network.
	*
	* { nodes: {schema: [ {},... ], data:[ {},...]}, 
	* 	edges: {schema: [ {},... ], data:[ {},...]} }
	*
	* @param {Object} data The json object containg the data.
*/
edu.vt.vbi.patric.vis.importer.IDV.prototype.readFromJson = function(json) {
	var output = {};
	// minimum required for a graph is a set of nodes
	if (json.hasOwnProperty("nodes")) {
		output.nodes = json.nodes.data;
		output.edges = [];
		if (json.hasOwnProperty("edges")) output.edges = json.edges.data;
		output.schema = { nodes : json.nodes.schema, edges: json.edges.schema };
		return output;
	} else {
		throw new edu.vt.vbi.patric.vis.ImportException(
			"Unable to import IDV network. No recognizable nodes array in input data."
		);
	}
};
	

/*
	this.loadTables = function(args) {
		if (!args || !("nodes" in args) || !("data" in args.nodes)) { return -1; }
		
		// parse node table
		if (!("rowDelim" in args.nodes)) 		 { args.nodes.rowDelim 		 = "\n"; }
		if (!("colDelim" in args.nodes)) 		 { args.nodes.colDelim 		 = "\t"; }
		if (!("headerRow" in args.nodes)) 	 { args.nodes.headerRow    = 0;    }
		if (!("firstDataRow" in args.nodes)) { args.nodes.firstDataRow = 1; 	 }
		
		var nrows = args.nodes.data.split(args.nodes.rowDelim);
		if (args.nodes.headerRow < 0 || args.nodes.headerRow > nrows.length) {
			args.nodes.headerRow = 0;
		}
		if (args.nodes.firstDataRow < 0 || args.nodes.firstDataRow > nrows.length) {
			args.nodes.firstDataRow = 1;
		}
		var nhcols = nrows[args.nodes.headerRow].split(args.nodes.colDelim);
		for (var i=args.nodes.firstDataRow; i<nrows.length; i++) {
			var ncols = nrows[i].split(args.nodes.colDelim);
			if (ncols.length < nhcols.length) { continue; }
			var node = { data: {} };
			var id;
			for (var j=0; j<ncols.length; j++) {
				node.data[nhcols[j]] = ncols[j];
				if (nhcols[j].toLowerCase() == "id") { id = ncols[j]; }
			}
			this.addNode(node,id);
		}

		// parse edge table
		if (!("rowDelim" in args.edges)) 		 { args.edges.rowDelim 		 = "\n"; }
		if (!("colDelim" in args.edges)) 		 { args.edges.colDelim 		 = "\t"; }
		if (!("headerRow" in args.edges)) 	 { args.edges.headerRow    = 0;    }
		if (!("firstDataRow" in args.edges)) { args.edges.firstDataRow = 1; 	 }
		
		var erows = args.edges.data.split(args.edges.rowDelim);
		if (args.edges.headerRow < 0 || args.edges.headerRow > erows.length) {
			args.edges.headerRow = 0;
		}
		if (args.edges.firstDataRow < 0 || args.edges.firstDataRow > erows.length) {
			args.edges.firstDataRow = 1;
		}
		var ehcols = erows[args.edges.headerRow].split(args.edges.colDelim);
		for (var i=args.edges.firstDataRow; i<erows.length; i++) {
			var ecols = erows[i].split(args.edges.colDelim);
			if (ecols.length < ehcols.length) { continue; }
			var edge = { data: {} };
			var sid;
			var tid;
			for (var j=0; j<ecols.length; j++) {
				edge.data[ehcols[j]] = ecols[j];
				if (ehcols[j].toLowerCase() == "source") 			{ sid = ecols[j]; }
				else if (ehcols[j].toLowerCase() == "target") { tid = ecols[j]; }
			}
			if (sid && tid) { this.addEdge(edge, sid, tid); }
		}
		return this.countNodes();
	};
	
*/
