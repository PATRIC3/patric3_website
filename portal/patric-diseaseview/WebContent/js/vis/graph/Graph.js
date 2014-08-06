/**
 * @class edu.vt.vbi.patric.vis.Graph
 * Parent class for graph visualizations onto Network data. Specific 
 * implementations of graph visualizations will override this class.
 *
	* @extends edu.vt.vbi.patric.Vis
	*
 * @history
 *
 * @constructor
 * Creates a new Graph
 */
edu.vt.vbi.patric.vis.Graph = function() {
	
	edu.vt.vbi.patric.Vis.call(this);
	
	/**
		* @cfg {edu.vt.vbi.patric.vis.Network} network The underlying data for the graph.
	*/
	this.network;

};
edu.vt.vbi.patric.vis.Graph.inheritsFrom(edu.vt.vbi.patric.Vis);

	
	/**
		* Applies a simple mapping of node category values to a palette of colors. 
		* Color values are stored in the 'color' attribute of the network category 
		* being mapped. 
		*
		* @param {Object} opts Set of config options
		* 	{String} name The name of the network category
		* 	{Array} values Hex colors to use as palette
		* 	{String} defaultValue A hex color to use if mapping fails
		* 	{Object} exclude (optional) Pairs of value:color to exclude from mapping
	*/
edu.vt.vbi.patric.vis.Graph.prototype.map_CategoryToColor = function(opts) {
	
	var dataCategory = this.network.getCategory(opts.name);
	
	// add the color property to the category
	dataCategory.addProp("color", opts.defaultValue);
	
	var idsToMap = dataCategory.getEntryIds();
		
	// set the values for any excluded entries
	if (opts.hasOwnProperty("exclude")) {

		for (var k in opts.exclude) {
			if (opts.exclude.hasOwnProperty(k)) {
			
				dataCategory.updateEntry(k, "color", opts.exclude[k]);
				
				// remove it from the entry ids to map
				var indxRm = -1;
				for (var i=0; i<idsToMap.length; i++) {
					if (idsToMap[i] == k) {
						indxRm = i;
						break;
					}
				}
				if (indxRm != -1) idsToMap.splice(indxRm, 1);
			
			}
		}
	}
	
	if (idsToMap.length < 2) {
	
		// no math required; just use the first color
		
		//var midpt = opts.values[Math.round(opts.values.length/2)];
		for (var i=0; i<idsToMap.length; i++) {
			dataCategory.updateEntry(idsToMap[i], "color", opts.values[i]);
		}
		
	} else {
	
		// maximize the separation on the color palette, driven by the number of 
		// unique values to map.
		
		for (var i=0; i<idsToMap.length; i++) {
			if (i > opts.values.length-1) {
				dataCategory.updateEntry(idsToMap[i], "color", opts.defaultValue);
			} else {
				var indx = Math.round(((opts.values.length-1)/(idsToMap.length-1))*i);
				dataCategory.updateEntry(idsToMap[i], "color", opts.values[indx]);
			}
		}
	}
};


edu.vt.vbi.patric.vis.GraphException = function(msg) {
	this.message = "GraphException from [" + arguments.callee.caller.name + "]. " + msg;
};

if (!this.edu.vt.vbi.patric.vis.graph)	{ this.edu.vt.vbi.patric.vis.graph = {}; }

