if (!this.edu) 								{ this.edu = {}; }
if (!this.edu.vt)							{ this.edu.vt = {}; }
if (!this.edu.vt.vbi)					{ this.edu.vt.vbi = {}; }
if (!this.edu.vt.vbi.patric)	{ this.edu.vt.vbi.patric = {}; }
if (!this.edu.vt.vbi.patric.vis)	{ this.edu.vt.vbi.patric.vis = {}; }


function handleException(e) {
	var m = "ERROR: " + e.message;
	if (e.hasOwnProperty('line')) m += "\nLINE: " + e.line;
	if (e.hasOwnProperty('sourceURL')) m += "\nFILE: " + e.sourceURL;
	alert(m);
}

/**
	* Retrieves a DOM object by its id.
*/
function getObj(id) {
	var returnVal = {};
	
	if (document.getElementById)
		returnVar = document.getElementById(id);

	else if (document.all)
		returnVar = document.all[id];

	else if (document.layers)
		returnVar = document.layers[id];

	return returnVar;
}


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


String.prototype.wordCaps = function() {
	return this.replace( /(^|\W)([a-z])/g , function(m,p1,p2) {
		return p1+p2.toUpperCase();
	});
};

Array.prototype.has = function(val) {
	for (var i=0; i<this.length; i++) {
		if (this[i] == val) { return true; }
	}
	return false;
};

Array.prototype.rmIndx = function(indx) {
	this.splice(indx, 1);
};

Array.prototype.rmVal = function(val) {
	var newArr = [];
	for (var i=0; i<this.length; i++) {
		if (val != this[i])
			newArr.push(this[i]);
	}
	return newArr;
};

Array.prototype.rmDups = function() {
	var newArr = [];
	for (var i=0; i<this.length; i++) {
		if (!this.has(newArr, this[i]))
			newArr.push(this[i]);
	}
	return newArr;
};

Array.prototype.keys = function() {
	var newArr = [];
	for (var i=0; i<this.length; i++) {
		if (typeof this[i] != 'undefined')
			newArr.push(i);
	}
	return newArr;
};



/**
	* A utility method for validating config objects.
	* @param {Object} cfg Set of configuration values.
	* @param {Number} valid Set of valid values.
	* @throws {edu.vt.vbi.patric.vis.ConfigException}
*/
function config(cfg, valid) {

	var required = {};
	for (var key in valid) {
		if (valid.hasOwnProperty(key) && valid[key].required) 
			required[key] = true;
	}
	
	for (var key in cfg) {
		if (cfg.hasOwnProperty(key) && valid.hasOwnProperty(key)) {

			var cfgVal = cfg[key];
			var typeValid = false;
			for (var i=0; i<valid[key].validTypes.length; i++) {
				var fcn_ = valid[key].validTypes[i];
				if ((typeof(fcn_) == "string" && typeof(cfgVal) == fcn_) || 
						(typeof(cfgVal) == "object" && cfgVal instanceof fcn_)) {
					typeValid = true;
					break;
				}
			}
			if (!typeValid) {
				throw new edu.vt.vbi.patric.vis.ConfigException(
					arguments.callee.caller.name + " option '" + key + "' is not a valid type."
				);
			}
			
		} else if (required.hasOwnProperty(key)) {
			throw new edu.vt.vbi.patric.vis.ConfigException(
				arguments.callee.caller.name + " option '" + key + "' (required) is missing."
			);
		}
	}
}


/** 
	* Extend the Function prototype to allow easy inheritance.
*/
Function.prototype.inheritsFrom = function( parentClassOrObject ){ 
	if ( parentClassOrObject.constructor == Function ) {
		//Normal Inheritance 
		this.prototype = new parentClassOrObject;
		this.prototype.constructor = this;
		this.prototype.parent = parentClassOrObject.prototype;
	} else {
		//Pure Virtual Inheritance 
		this.prototype = parentClassOrObject;
		this.prototype.constructor = this;
		this.prototype.parent = parentClassOrObject;
	}
	return this;
}



/**
	* @class edu.vt.vbi.patric.vis.ConfigException
	* Class for handling config errors.
	*
	* @history
	*
	* @constructor
	* Creates a new ConfigException.
	* @param {String} msg A user-friendly message describing the error.
*/
edu.vt.vbi.patric.vis.ConfigException = function(msg) {
	this.message = "ConfigException from [" + arguments.callee.caller.name + "]. " + msg;
};
edu.vt.vbi.patric.vis.ConfigException.prototype.constructor = edu.vt.vbi.patric.vis.ConfigException;


/**
	* @class edu.vt.vbi.patric.vis.Vector
	* A utility class for manipulating two-dimensional vectors.
	*
	* @history
	*
	* @constructor
	* Create a new Vector
	* @param {Number} x Value for dimension one.
	* @param {Number} y Value for dimension two.
*/
edu.vt.vbi.patric.vis.Vector = function(x, y) {
	this.x = x;
	this.y = y;
}

edu.vt.vbi.patric.vis.Vector.prototype.constructor = edu.vt.vbi.patric.vis.Vector;
 
edu.vt.vbi.patric.vis.Vector.prototype = {

	random: function(n1, n2) {
		var v1 = Math.random();
		var v2 = Math.random();
		if (typeof n1 != "undefined") {
			v1 *= n1;
			(typeof n2 != "undefined") ? v2 *= n2 : v2 *= n1;
		}
		return new edu.vt.vbi.patric.vis.Vector(v1,v2);
	}, 
 
	add: function(v2) {
		return new edu.vt.vbi.patric.vis.Vector(this.x + v2.x, this.y + v2.y);
	}, 
 
	subtract: function(v2) {
		return new edu.vt.vbi.patric.vis.Vector(this.x - v2.x, this.y - v2.y);
	},
 
	multiply: function(n) {
	return new edu.vt.vbi.patric.vis.Vector(this.x * n, this.y * n);
	}, 
 
	divide: function(n) {
		return new edu.vt.vbi.patric.vis.Vector(this.x / n, this.y / n);
	},
 
	magnitude: function() {
		return Math.sqrt(this.x*this.x + this.y*this.y);
	}, 
 
	normalize: function() {
		return this.divide(this.magnitude());
	}
}

 

