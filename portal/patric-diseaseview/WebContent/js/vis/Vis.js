if (!this.edu) 											{ this.edu = {}; }
if (!this.edu.vt)										{ this.edu.vt = {}; }
if (!this.edu.vt.vbi)								{ this.edu.vt.vbi = {}; }
if (!this.edu.vt.vbi.patric)				{ this.edu.vt.vbi.patric = {}; }


/**
 * @class edu.vt.vbi.patric.Vis
 * Parent class for all visualizations.
 *
 * @history
 *
 * @constructor
 * Creates a new Vis
 */
edu.vt.vbi.patric.Vis = function() {

	/** @cfg {Object} listeners Set of callback functions for associated events. */
	this.listeners  = {};
	
	/** @cfg {boolean} throttled Whether or not to run in low-res mode. */
	this.throttled  = false;

	/** @cfg {Object} vis The vis object, specific to the type of Visualization. */
	this.vis;
		
	/** @cfg {Object} canvas The DOM element that holds the Visualization. */
	this.canvas;

	/** @cfg {edu.vt.vbi.patric.VisualStyle} style A default set of visual style definitions. */
	this.style;
		
};

edu.vt.vbi.patric.Vis.prototype = {
	
	getStyle: function() {
		return this.style;
	},
	
	setStyle: function(s) {
		this.style = s;
	},
	
	getCanvas: function() {
		return this.canvas;
	},
	
	setCanvas: function(c) {
		this.canvas = c;
	},
	
	addEvent: function(evt) {
		if (!this.listeners.hasOwnProperty(evt)) this.listeners[evt] = {};
	}, 
	
	removeEvent: function(evt) {
		if (this.listeners.hasOwnProperty(evt)) delete this.listeners[evt];
	}, 
	
	fireEvent: function(evt, evtObj) {
		if (this.listeners.hasOwnProperty(evt)) {
			for (var name in this.listeners[evt]) {
				if (this.listeners[evt].hasOwnProperty(name)) 
					this.listeners[evt][name](evtObj);
			}
		}
	}, 

	addListener: function(evt, id, listener) {
		if (!this.listeners.hasOwnProperty(evt)) this.addEvent(evt);
		this.listeners[evt][id] = listener;
	}, 
	
	removeListener: function(id) {
		for (var evt in this.listeners) {
			if (this.listeners.hasOwnProperty(evt) && 
					this.listeners[evt].hasOwnProperty(id)) delete this.listeners[evt][id];
		}
	}, 
	
	removeListeners: function(/* evt */) {
		if (arguments.length == 0) {
			for (var evt in this.listeners) {
				if (this.listeners.hasOwnProperty(evt)) 
					this.listeners[evt] = {};
			}
		} else {
			for (var i=0; i<arguments.length; i++) {
				if (this.listeners.hasOwnProperty(arguments[i])) 
					this.listeners[arguments[i]] = {};
			}
		}
	}, 
	
	draw: function(){}, 
	
	ready: function(fn) {
		if (!fn) this._onReady = function () {/*do nothing*/};
		else this._onReady = fn;
		return this;
	}, 

	_onReady: function() {
		// do nothing by default
	}, 
	
	throttle: function() {
		this.throttled = true;
	}, 
	
	dethrottle: function() {
		this.throttled = false;
	}, 

	isThrottled: function() {
		return this.throttled;
	}

};


