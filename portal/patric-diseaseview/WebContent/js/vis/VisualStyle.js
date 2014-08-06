edu.vt.vbi.patric.vis.VisualStyle = function() {};
edu.vt.vbi.patric.vis.VisualStyle.prototype = {};


edu.vt.vbi.patric.vis.VisualStyleException = function(msg) {
	this.message = "VisualStyleException from [" + arguments.callee.caller.name + "]. " + msg;
};


if (!this.edu.vt.vbi.patric.vis.style)	{ this.edu.vt.vbi.patric.vis.style = {}; }

