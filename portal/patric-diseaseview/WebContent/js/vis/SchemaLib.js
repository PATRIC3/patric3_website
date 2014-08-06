

/**
 * @class edu.vt.vbi.patric.vis.Schema
 * Parent class for a vis data schema.
 *
 * @history
 *
 * @constructor
 * Creates a new Schema.
 */
edu.vt.vbi.patric.vis.Schema = function() {
	this.entries = [];
};

edu.vt.vbi.patric.vis.Schema.prototype = {

	/** 
		* Add a schema definition.
		* @param {Object} def the name,type pair to add.
	*/
	addDef: function(def) {
		if (!def.hasOwnProperty("name") || !def.hasOwnProperty("type")) {
			return;
		}
		var replaced = false;
		for (var i=0; i<this.entities.length; i++) {
			if (this.entities[i].name == def.name) {
				this.entities[i].type = def.type;
				replaced = true;
				break;
			}
		}
		if (!replaced) this.entities.push(def);
	},
	
	/** 
		* Remove a schema definition.
		* @param {String} name the name of the schema definition to remove.
	*/
	rmDef: function(name) {
		for (var i=0; i<this.entities.length; i++) {
			if (this.entities[i].name == name) {
				this.entities.splice(i,1);
				break;
			}
		}
	}, 
	
	clrDefs: function() {
		this.entities = [];
	}, 
	
	/** 
		* Get the type for a schema definition.
		* @param {String} name the name of the schema definition to type.
	*/
	getDefType: function(name) {
		for (var i=0; i<this.entities.length; i++) {
			if (this.entities[i].name == name) {
				return this.entities[i].type;
			}
		}
		return undefined;
	}
	
};


if (!this.edu.vt.vbi.patric.vis.schema)	{ this.edu.vt.vbi.patric.vis.schema = {}; }

/**
 * @class edu.vt.vbi.patric.vis.schema.GenericMI
 * @extends edu.vt.vbi.patric.vis.Schema
 * Provides the schema for generic molecular interaction data.
 *
 * @history
 *
 * @constructor
 * Creates a new GenericMI.
edu.vt.vbi.patric.vis.schema.GenericMI = function() {
	
	edu.vt.vbi.patric.vis.Schema.call(this);
	
	this.nodes = [
		{name: "alias", type: "string"}, 
		{name: "label", type: "string"}, 
		{name: "taxid", type: "int"}, 
		{name: "taxon", type: "string"} 
	];
	
	this.edges = [
		{name: "methods", type: "string"}, 
		{name: "types", type: "string"}, 
		{name: "refs", type: "string"}, 
		{name: "scores", type: "string"}, 
		{name: "score_types", type: "string"} 
	];

};
edu.vt.vbi.patric.vis.schema.GenericMI.inheritsFrom(edu.vt.vbi.patric.vis.Schema);
 */



/**
 * @class edu.vt.vbi.patric.vis.schema.Graph
 * @extends edu.vt.vbi.patric.vis.Schema
 * Provides the schema for graph data.
 *
 * @history
 *
 * @constructor
 * Creates a new graph schema. This schema uses node and edge arrays instead of 
 * the base entries array, and consequently implements new add, rm, and clr functions.
 */
edu.vt.vbi.patric.vis.schema.Graph = function() {
	edu.vt.vbi.patric.vis.Schema.call(this);
	this.nodes = [];
	this.edges = [];
};
edu.vt.vbi.patric.vis.schema.Graph.inheritsFrom(edu.vt.vbi.patric.vis.Schema);

edu.vt.vbi.patric.vis.schema.Graph.prototype.clrDefs = function() {
	this.clrNodeDefs();
	this.clrEdgeDefs();
};

edu.vt.vbi.patric.vis.schema.Graph.prototype.clrNodeDefs = function() {
	this.nodes = [];
};

edu.vt.vbi.patric.vis.schema.Graph.prototype.clrEdgeDefs = function() {
	this.edges = [];
};

edu.vt.vbi.patric.vis.schema.Graph.prototype.addDef = function(def) {
	this.addNodeDef(def);
};

/** 
	* Add a schema definition for a node.
	* @param {Object} def the name,type pair to add.
*/
edu.vt.vbi.patric.vis.schema.Graph.prototype.addNodeDef = function(def) {
	if (!def.hasOwnProperty("name") || !def.hasOwnProperty("type")) {
		return;
	}
	var replaced = false;
	for (var i=0; i<this.nodes.length; i++) {
		if (this.nodes[i].name == def.name) {
			this.nodes[i].type = def.type;
			replaced = true;
			break;
		}
	}
	if (!replaced) this.nodes.push(def);
};

/** 
	* Remove a node schema definition.
	* @param {String} name the name of the node schema definition to remove.
*/
edu.vt.vbi.patric.vis.schema.Graph.prototype.rmNodeDef = function(name) {
	for (var i=0; i<this.nodes.length; i++) {
		if (this.nodes[i].name == name) {
			this.nodes.splice(i,1);
			break;
		}
	}
};
	

/** 
	* Add a schema definition for an edge.
	* @param {Object} def the name,type pair to add.
*/
edu.vt.vbi.patric.vis.schema.Graph.prototype.addEdgeDef = function(def) {
	if (!def.hasOwnProperty("name") || !def.hasOwnProperty("type")) {
		return;
	}
	var replaced = false;
	for (var i=0; i<this.edges.length; i++) {
		if (this.edges[i].name == def.name) {
			this.edges[i].type = def.type;
			replaced = true;
			break;
		}
	}
	if (!replaced) this.edges.push(def);
};

/** 
	* Remove an edge schema definition.
	* @param {String} name the name of the edge schema definition to remove.
*/
edu.vt.vbi.patric.vis.schema.Graph.prototype.rmEdgeDef = function(name) {
	for (var i=0; i<this.edges.length; i++) {
		if (this.edges[i].name == name) {
			this.edges.splice(i,1);
			break;
		}
	}
};
	
/** 
	* Get the type for a node schema definition.
	* @param {String} name the name of the schema definition to type.
*/
edu.vt.vbi.patric.vis.schema.Graph.prototype.getNodeDefType = function(name) {
	for (var i=0; i<this.nodes.length; i++) {
		if (this.nodes[i].name == name) {
			return this.nodes[i].type;
		}
	}
	return undefined;
};

/** 
	* Get the type for a edge schema definition.
	* @param {String} name the name of the schema definition to type.
*/
edu.vt.vbi.patric.vis.schema.Graph.prototype.getEdgeDefType = function(name) {
	for (var i=0; i<this.edges.length; i++) {
		if (this.edges[i].name == name) {
			return this.edges[i].type;
		}
	}
	return undefined;
};
	
	


/**
 * @class edu.vt.vbi.patric.vis.schema.HPI
 * @extends edu.vt.vbi.patric.vis.Schema
 * Provides the schema for PATRIC Host-Pathogen Interaction data.
 *
 * @history
 *
 * @constructor
 * Creates a new HPI.
 */
edu.vt.vbi.patric.vis.schema.HPI = function() {

	edu.vt.vbi.patric.vis.Schema.call(this);

	this.nodes = [
		{name: "na_feature_id", type: "string"}, 
		{name: "locus_tag", type: "string"}, 
		{name: "taxid", type: "int"}, 
		{name: "mol_id", type: "int"}, 
		{name: "name", type: "string"}
	];

	this.edges = [
		{name: "id", type: "string"}, 
		{name: "type", type: "string"}, 
		{name: "pig_id", type: "int"}, 
		{name: "method_id", type: "int"}, 
		{name: "type_id", type: "int"}, 
		{name: "source_id", type: "int"}, 
		{name: "reference_id", type: "int"}, 
		{name: "score", type: "number"} 
	];
};
edu.vt.vbi.patric.vis.schema.HPI.inheritsFrom(edu.vt.vbi.patric.vis.Schema);



if (!this.edu.vt.vbi.patric.vis.schema.categories)	{ this.edu.vt.vbi.patric.vis.schema.categories = {}; }

/**
 * @class edu.vt.vbi.patric.vis.schema.categories.HPI
 * Provides the data categories for PATRIC Host-Pathogen Interaction data.
 *
 * @history
 *
 * @constructor
 * Creates a new HPI.
 */
edu.vt.vbi.patric.vis.schema.categories.HPI = function() {

	this.taxa = [
		{name: "id", type: "int"}, 
		{name: "name", type: "string"}, 
		{name: "visible", type: "boolean"}
	];

	this.type = [
		{name: "id", type: "int"}, 
		{name: "name", type: "string"}, 
		{name: "source", type: "string"}, 
		{name: "source_id", type: "string"}, 
		{name: "visible", type: "boolean"}
	];

	this.method = [
		{name: "id", type: "int"}, 
		{name: "name", type: "string"}, 
		{name: "source", type: "string"}, 
		{name: "source_id", type: "string"}, 
		{name: "visible", type: "boolean"}
	];

	this.source = [
		{name: "id", type: "int"}, 
		{name: "name", type: "string"}, 
		{name: "source_dbid", type: "string"}, 
		{name: "visible", type: "boolean"}
	];

	this.reference = [
		{name: "id", type: "int"}, 
		{name: "name", type: "string"}, 
		{name: "source", type: "string"}, 
		{name: "source_id", type: "string"} 
	];

	this.mol = [
		{name: "id", type: "int"}, 
		{name: "name", type: "string"}, 
		{name: "visible", type: "boolean"}
	];

};


