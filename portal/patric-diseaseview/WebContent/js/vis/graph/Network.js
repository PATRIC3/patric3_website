
/**
 * @class edu.vt.vbi.patric.vis.NetworkException
 * Provides exception handling for Network errors.
 *
 * @history
 *
 * @constructor
 * Creates a new NetworkException
 * @param {String} msg A message describing the exception
 */
edu.vt.vbi.patric.vis.NetworkException = function(msg) {
	this.message = "NetworkException from [" + arguments.callee.caller.name + "]. " + msg;
};



/**
 * @class edu.vt.vbi.patric.vis.Network
 * Data representation of a network, a set of nodes joined by edges.
 *
 * @history
 *
 * @constructor
 * Creates a new Network
 */
edu.vt.vbi.patric.vis.Network = function() {

	this.schema;
	this.nodes  	= [];		// node objects
	this.edges  	= [];		// edge objects
		
	this.nid2indx = {};		// id lookup for nodes
	this.eid2indx = {};		// id lookup for edges
		
	this.aListF 	= [];		// adjacency list s->t
	this.aListR 	= [];		// adjacency list t->s
	this.aListD 	= [];		// adjacency list p->c
	this.aListU 	= [];		// adjacency list c->p
	
	this.listeners = { dataChange: {} };

};

edu.vt.vbi.patric.vis.Network.prototype = {
	
	addListener: function(evt, id, listener) {
		if (this.listeners.hasOwnProperty(evt)) {
			this.listeners[evt][id] = listener;
		}
	}, 
	
	removeListener: function(evt, id) {
		if (this.listeners.hasOwnProperty(evt)) {
			delete this.listeners[evt][id];
		}
	}, 
	
	fireDataChange: function(evtObj) {
		if (this.listeners.hasOwnProperty("dataChange")) {
			for (var name in this.listeners["dataChange"]) {
				if (this.listeners["dataChange"].hasOwnProperty(name)) {
					this.listeners["dataChange"][name](evtObj);
				}
			}
		}
	}, 
	
	getSchema: function() {
		return this.schema;
	}, 
	
	addNode: function(props) {
		if (props.hasOwnProperty("id")) {
			if (this.nid2indx.hasOwnProperty(props.id)) {
				return -1;
			} else {
			
				var n = new edu.vt.vbi.patric.vis.network.Node({
					id: props.id
				});
				
				n.addProps(props);
				var indx = this.nodes.length;
				this.nodes.push(n);
				this.nid2indx[props.id] = indx;
				this.aListF[indx] = [];
				this.aListR[indx] = [];
				this.aListU[indx] = [];
				this.aListD[indx] = [];
				return indx;
			}
		} else {
			throw new edu.vt.vbi.patric.vis.NetworkException(
				"Unable to add node to network; missing required node id in the input data."
			);
		}
	},

	addNodes: function(nodes) {
		var count = 0;
		for (var i=0; i<nodes.length; i++) {
			try {
				var indx = this.addNode(nodes[i]);
				if (indx > -1) { count++; }
			} catch (err) {
				// node is silently ignored on error
				continue;
			}
		}
		return count;
	}, 
	
	hasNode: function(id) {
		return this.nid2indx.hasOwnProperty(id);
	},

	countNodes: function() {
		return this.nodes.length;
	},

	getNode: function(id) {
		if (this.nid2indx.hasOwnProperty(id)) {
			return this.nodes[this.nid2indx[id]];
		} else {
			return undefined;
		}
	},

	getNodes: function() {
		return this.nodes;
	},

	getNodeIds: function() {
		var arr = [];
		for (var id in this.nid2indx) {
			if (this.nid2indx.hasOwnProperty(id)) arr.push(id);
		}
		return arr;
	},
	
	_getNodeIndx: function(nodeId) {
		var indx = -1;
		this.nid2indx.hasOwnProperty(nodeId) ? (indx = this.nid2indx[nodeId]) : (indx = -1);
		return indx;
	},
	
	getNbrhd: function(nodeId/*,dir */) {
		var dir = 'h';
		if (arguments.length>1) dir = arguments[1];
	
		var nbrhd = { nodes: [nodeId], edges: [] };
		var nodeIndx = this._getNodeIndx(nodeId);
		
		if (nodeIndx == -1) return nbrhd;
		
		// get the connected nodes
		var nbrIndxList = this.aListU[nodeIndx].concat(this.aListD[nodeIndx]);
		if (dir == 'v') nbrIndxList = this.aListF[nodeIndx].concat(this.aListR[nodeIndx]);
		for (var i=0; i<nbrIndxList.length; i++) {
			nbrhd.nodes.push(this.nodes[nbrIndxList[i]].getProp('id'));
		}
		
		return nbrhd;
		
	}, 
	

	addEdge: function(props) {
	
		if (props.hasOwnProperty("source") && 
				props.hasOwnProperty("target") && 
				this.hasNode(props.source) 		 && 
				this.hasNode(props.target)) {
				
			var e = new edu.vt.vbi.patric.vis.network.Edge({
				source: props.source, 
				target: props.target
			});
			
			e.addProps(props);
			var indx = this.edges.length;
			this.edges.push(e);
			this.eid2indx[e.getProp('id')] = indx;
			
			if (e.getProp('type') == 'v') {
				this.aListD[this.nid2indx[props.source]].push(this.nid2indx[props.target]);
				this.aListU[this.nid2indx[props.target]].push(this.nid2indx[props.source]);
			} else {
				this.aListF[this.nid2indx[props.source]].push(this.nid2indx[props.target]);
				this.aListR[this.nid2indx[props.target]].push(this.nid2indx[props.source]);
			}

			return indx;

		} else {
			throw new edu.vt.vbi.patric.vis.NetworkException(
				"Unable to add edge to network; missing required input data."
			);
		}
	},
	
	addEdges: function(edges) {
		var count = 0;
		for (var i=0; i<edges.length; i++) {
			try {
				this.addEdge(edges[i]);
				count++;
			} catch (err) {
				// edge is silently ignored on error
				continue;
			}
		}
		return count;
	}, 
	
	hasEdge: function(id) {
		return this.eid2indx.hasOwnProperty(id);
	},

	countEdges: function() {
		return this.edges.length;
	},
	
	getEdge: function(id) {
		if (this.eid2indx.hasOwnProperty(id)) {
			return this.edges[this.eid2indx[id]];
		} else {
			return undefined;
		}
	}, 
	
	getEdges: function() {
		return this.edges;
	}, 
	
	getEdgeIds: function(/*nodeId*/) {
		var arr = [];
		for (var id in this.eid2indx) {
			if (this.eid2indx.hasOwnProperty(id)) { arr.push(id); }
			}
		return arr;
	},

	_getEdgeIndx: function(id) {
		var indx = -1;
		this.eid2indx.hasOwnProperty(id) ? (indx = this.eid2indx[id]) : (indx = -1);
		return indx;
	},
	

	areAdj: function(a, b) {
		return this.aListF[a].has(b);
	}, 
	
	areAdjF: function(a, b) {
		return this.aListF[a].has(b);
	}, 
	
	areAdjB: function(a, b) {
		return this.aListB[a].has(b);
	}, 
		
	areAdjD: function(a, b) {
		return this.aListD[a].has(b);
	}, 
		
	areAdjU: function(a, b) {
		return this.aListU[a].has(b);
	}
	
};


if (!this.edu.vt.vbi.patric.vis.network)	{ this.edu.vt.vbi.patric.vis.network = {}; }

edu.vt.vbi.patric.vis.network.Entity = function() {
	this.props = {};
};

edu.vt.vbi.patric.vis.network.Entity.prototype = {
	
	addProps: function(obj) {
		for (var k in obj) {
			if (obj.hasOwnProperty(k)) { this.props[k] = obj[k]; }
		}
	}, 
	
	getProp: function(k) {
		if (this.props.hasOwnProperty(k)) {
			return this.props[k];
		} else {
			return undefined;
		}
	}, 

	setProp: function(k, v) {
		this.props[k] = v;
	}, 
	
	getAllProps: function() {
		return this.props;
	}
	
};



edu.vt.vbi.patric.vis.network.Node = function(cfg) {
	
	edu.vt.vbi.patric.vis.network.Entity.call(this);
	
	var validCfg = {
		id : { required: true, validTypes: ['string', 'number'] } 
	};
	
	try {
	
		config(cfg, validCfg);
	
		/**
			* @cfg {String/Number} id A unique id for this node.
		*/
		this.props.id = cfg.id;

	} catch (err) {
		handleException(err);
	}

};
edu.vt.vbi.patric.vis.network.Node.inheritsFrom(edu.vt.vbi.patric.vis.network.Entity);


edu.vt.vbi.patric.vis.network.Node.prototype.getId = function() {
	return this.parent.getProp.call(this, 'id');
};



edu.vt.vbi.patric.vis.network.Edge = function(cfg) {

	edu.vt.vbi.patric.vis.network.Entity.call(this);
	
	var validCfg = {
		source : { required: true, validTypes: ['string', 'number'] }, 
		target : { required: true, validTypes: ['string', 'number'] }, 
		type  		: { required: false, validTypes: ['string'] }, 
		id		 		: { required: false, validTypes: ['string', 'number'] }, 
		directed  : { required: false, validTypes: ['boolean'] } 
	};
	
	try {
	
		config(cfg, validCfg);
	
		/**
			* @cfg {String/Number} source The id of the source node.
		*/
		this.props.source = cfg.source;

		/**
			* @cfg {String/Number} target The id of the target node.
		*/
		this.props.target = cfg.target;

		/**
			* @cfg {String} etype The type of edge.
		*/
		(cfg.hasOwnProperty('type')) ? 
			(this.props.type = cfg.type) : 
			(this.props.type = 'h');

		/**
			* @cfg {String/Number} id A unique id for this edge.
		*/
		(cfg.hasOwnProperty('id')) ? 
			(this.props.id = cfg.id) : 
			(this.props.id = cfg.source + '-' + cfg.target);

		/**
			* @cfg {Boolean} directed Whether the edge is directed or not.
		*/
		(cfg.hasOwnProperty('directed')) ? 
			(this.props.directed = cfg.directed) : 
			((this.props.type == 'v') ? 
				(this.props.directed = true) : 
				(this.props.directed = false)
			);

	} catch (err) {
		handleException(err);
	}
};
edu.vt.vbi.patric.vis.network.Edge.inheritsFrom(edu.vt.vbi.patric.vis.network.Entity);


edu.vt.vbi.patric.vis.network.Edge.prototype.getId = function() {
	return this.parent.getProp.call(this, 'id');
};

edu.vt.vbi.patric.vis.network.Edge.prototype.getType = function() {
	return this.parent.getProp.call(this, 'type');
};

edu.vt.vbi.patric.vis.network.Edge.prototype.getSourceId = function() {
	return this.parent.getProp.call(this, 'source');
};

edu.vt.vbi.patric.vis.network.Edge.prototype.getTargetId = function() {
	return this.parent.getProp.call(this, 'target');
};

edu.vt.vbi.patric.vis.network.Edge.prototype.isDirected = function() {
	return this.parent.getProp.call(this, 'directed');
};



/**
 * @class edu.vt.vbi.patric.vis.network.DataCategory
 * Stores metadata about entities in a network. The primary function of this 
 * class is to replace redundant String properties with integers in the network 
 * object. DataCategory entries are untyped objects.
 *
 * @history
 *
 * @constructor
 * Creates a new DataCategory
 * @param {String} name A name for the new DataCategory
 */
edu.vt.vbi.patric.vis.network.DataCategory = function(name) {
	
	/** 
		* @cfg {String} name The name/id of this DataCategory.
	*/
	this.name = name;
	
	/** 
		* @cfg {String} group The entity group this DataCategory describes (nodes|edges).
	*/
	this.group;

	/** 
		* @cfg {String} idInGroup The name of the property in the network entity that 
		* references this DataCategory.
	*/
	this.idInGroup;

	/** 
		* @cfg {Object} entries The possible entries for this DataCategory, mapped to entry id.
	*/
	this.entries = {};
};

	
edu.vt.vbi.patric.vis.network.DataCategory.prototype = {
	
	/** 
		* Populates this DataCategory with entries from a json data store. Accepts  
		* json with the following format: 
		* group {String} nodes|edges
		* idInGroup {String} name in the network entities
		* entries {Array} list of possible entries for this DataCategory. Each entry 
		* is an Object with at least an 'id' field.
		* @param {Object} json The json data used to populate this DataCategory
	*/
	initFromJson: function(json) {
		json.hasOwnProperty("group") 		 ? (this.group = json.group) : (this.group = null);
		json.hasOwnProperty("idInGroup") ? (this.idInGroup = json.idInGroup) : (this.idInGroup = null);
		
		if (json.hasOwnProperty("entries")) {
			for (var i=0; i<json.entries.length; i++) {
				var id;
				json.entries[i].hasOwnProperty("id") ? (id = json.entries[i].id) : id = i;
				this.entries[id] = json.entries[i];
			}
		}
	}, 
	
	getName: function() {
		return this.name;
	}, 
	
	getGroup: function() {
		return this.group;
	}, 
	
	getIdInGroup: function() {
		return this.idInGroup;
	}, 

	hasEntry: function(entryId) {
		return this.entries.hasOwnProperty(entryId);
	}, 
	
	/** 
		* Retrieves a single entry object.
		* @param {String} entryId The id of the entry to retrieve
		* @return {Object} The corresponding entry, or null
	*/
	getEntry: function(entryId) {
		if (this.hasEntry(entryId)) {
			return this.entries[entryId];
		} else {
			return null;
		}
	}, 

	/** 
		* Retrieves a list of all entry ids.
		* @return {Array} The entry ids
	*/
	getEntryIds: function() {
		var list = [];
		for (var id in this.entries) {
			if (this.entries.hasOwnProperty(id)) { list.push(id); }
		}
		return list;
	}, 

	/** 
		* Retrieves all entries.
		* @return {Object} All entries in this DataCategory
	*/
	getEntries: function() {
		return this.entries;
	}, 
	
	/** 
		* Updates the value of a given property for a single entry. If the entry does 
		* not contain the property, *it is added*.
		* @param {String} id The id of the entry to update
		* @param {String} prop The property to modify
		* @param {String} val The new value
		* @return {Boolean} true on success; false if entry does not exist
	*/
	updateEntry: function(id, prop, val) {
		if (this.hasEntry(id)) {
			this.entries[id][prop] = val;
			return true;
		}
		else {
			return false;
		}
	}, 
	
	/** 
		* Adds a new property to every entry in this DataCategory.
		* @param {String} prop The name of the new property
		* @param {String} defaultVal The default value for the new property
	*/
	addProp: function(prop, defaultVal) {
		for (var id in this.entries) {
			if (this.entries.hasOwnProperty(id) && !this.entries[id].hasOwnProperty(prop)) {
				this.entries[id][prop] = defaultVal;
			}
		}
	}
	
};


