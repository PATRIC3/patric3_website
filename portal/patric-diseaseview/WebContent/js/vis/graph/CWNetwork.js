if (!this.edu) 												{ this.edu = {}; }
edu.vt.vbi.patric.vis.network.CWNetwork = function(cfg) {

	edu.vt.vbi.patric.vis.Network.call(this);
	
	var validCfg = { 
		schema : { required: true, validTypes: [edu.vt.vbi.patric.vis.Schema] } 
	};
	
	try {
		config(cfg, validCfg);
		this.schema = cfg.schema;
	} catch (err) {
		handleException(err);
	}
		
	this.categories = {};	// lookup hash for network DataCategory objects
	
	this.listeners.categoryChange = {};
	
};
edu.vt.vbi.patric.vis.network.CWNetwork.inheritsFrom(edu.vt.vbi.patric.vis.Network);


edu.vt.vbi.patric.vis.network.CWNetwork.prototype.fireCategoryChange = function(evtObj) {
	if (this.listeners.hasOwnProperty("categoryChange")) {
		for (var name in this.listeners["categoryChange"]) {
			if (this.listeners["categoryChange"].hasOwnProperty(name)) {
				this.listeners["categoryChange"][name](evtObj);
			}
		}
	}
};


edu.vt.vbi.patric.vis.network.CWNetwork.prototype.getNbrhd = function(nodeId) {

	var nbrhd = { nodes: [nodeId], edges: [] };
	var nodeIndx = this._getNodeIndx(nodeId);
	
	if (nodeIndx == -1) return nbrhd;
	
	// get the connected nodes
	var nbrIndxList = this.aListF[nodeIndx].concat(this.aListR[nodeIndx]);
	for (var i=0; i<nbrIndxList.length; i++) {
		nbrhd.nodes.push(this.nodes[nbrIndxList[i]].getProp('id'));
	}
	
	return nbrhd;
	
};


/** 
	* catObj is a network DataCategory object.
*/
edu.vt.vbi.patric.vis.network.CWNetwork.prototype.addCategory = function(name, catObj) {
	this.categories[name] = catObj;
};


edu.vt.vbi.patric.vis.network.CWNetwork.prototype.getCategory = function(name) {
	if (this.categories.hasOwnProperty(name)) {
		return this.categories[name];
	} else {
		throw new edu.vt.vbi.patric.vis.NetworkException(
			"NetworkException: Parameter error. No category named " + name + " in network."
		);
	}
};


edu.vt.vbi.patric.vis.network.CWNetwork.prototype.getCategories = function() {
	return this.categories;
};


edu.vt.vbi.patric.vis.network.CWNetwork.prototype.getCategoryList = function() {
	var catList = [];
	for (var c in this.categories) {
		if (this.categories.hasOwnProperty(c)) {
			catList.push(this.categories[c]);
		}
	}
	return catList;
};


/**
	* updateList is array of { category: c, id: i, prop: p, newVal: newVal }
*/
edu.vt.vbi.patric.vis.network.CWNetwork.prototype.updateCategories = function(updateList) {
	var evtObj = { evtType: "categoryChange", events: [] };
	for (var i=0; i<updateList.length; i++) {
		var obj = updateList[i];
		if (this.categories.hasOwnProperty(obj.category)) {
			var cat = this.categories[obj.category];
			var entry = cat.getEntry(obj.id);
			var oldVal = null;
			if (entry.hasOwnProperty(obj.prop)) {
				oldVal = entry[obj.prop];
			}
			
			cat.updateEntry(obj.id, obj.prop, obj.newVal);
			
			evtObj.events.push({ 
				category: obj.category, 
				id			: obj.id, 
				prop		: obj.prop, 
				oldVal	: oldVal, 
				newVal	: obj.newVal 
			});
		}
	}
	
	if (evtObj.events.length > 0) {
		this.fireCategoryChange(evtObj);
	}
	
};


