edu.vt.vbi.patric.vis.style.CWVisualStyle = function() {
	
	edu.vt.vbi.patric.vis.VisualStyle.call(this);
	
	this.global = {
		backgroundColor : "#ffffff"
	};

	this.nodes = {
		selectionGlowColor	: {
			 defaultValue  : "#FFFF00", 
			 discreteMapper: {
				 attrName: "taxid", 
				 entries : [ {attrValue: 9606, value: "#C800FF"} ] 
			 } 
		}, 
		selectionGlowOpacity: 0.5, 
		selectionGlowStrength: 50, 
		selectionOpacity	 	: 1.0, 
		hoverOpacity	 			: 1.0, 
		borderWidth					: 1, 
		size: { 
			 defaultValue  : 30, 
			 discreteMapper: {
				 attrName: "taxid", 
				 entries : [ {attrValue: 9606, value: 30} ] 
			 } 
		}, 
		shape: {
			 defaultValue  : "ELLIPSE", 
			 discreteMapper: {
				 attrName: "mol_id", 
				 entries : [ {attrValue: 1, value: "ELLIPSE"} ] 
			 } 
		}, 
		color: { 
			 defaultValue  : "#cc66ff", 
			 discreteMapper: {
				 attrName: "taxid", 
				 entries : [ {attrValue: 9606, value: "#008000"} ] 
			 } 
		}, /*
		borderColor: { 
			 defaultValue  : "#cc66ff", 
			 discreteMapper: {
				 attrName: "taxid", 
				 entries : [ {attrValue: 9606, value: "#008000"} ] 
			 } 
		}, */
		borderColor 					:	"#333333", 
		labelFontName 				:	"Verdana", 
		labelHorizontalAnchor :	"left", 
		labelFontColor: {
			 defaultValue  : "#000000", 
			 discreteMapper: {
				 attrName: "taxid", 
				 entries : [ {attrValue: 9606, value: "#000000"} ] 
			 }
		}, 
		labelFontSize: {
			 defaultValue  : 12, 
			 discreteMapper: {
				 attrName: "taxid", 
				 entries : [ {attrValue: 9606, value: 11} ] 
			 }
		}, 
		label: { passthroughMapper: { attrName: "name" } }/*, 
		tooltipFont 					:	"Verdana", 
		tooltipFontSize 			:	12, 
		tooltipFontColor	 		:	"#000000", 
		tooltipBackgroundColor: "#b5ceed", 
		tooltipBorderColor	 	:	"#83ace2", 
		tooltipText: { passthroughMapper: { attrName: "name" } }*/ 
	};
	
	this.edges = {
		width									: 2, 
		opacity								: 0.4, 
		color									: "#999999", 
		selectionGlowColor 		: "#FFFF00", 
		selectionGlowOpacity  :	0.5, 
		hoverOpacity	 				:	1.0, 
		selectionOpacity	 		:	1.0, 
		labelFontName 				:	"Verdana", 
		labelHorizontalAnchor :	"left", 
		labelFontSize					: 12, 
		label: { passthroughMapper: { attrName: "score" } }/*, 
		tooltipFont 					:	"Verdana", 
		tooltipFontSize 			:	12, 
		tooltipFontColor	 		:	"#000000", 
		tooltipBackgroundColor: "#b5ceed", 
		tooltipBorderColor	 	:	"#83ace2", 
		tooltipText: { passthroughMapper: { attrName: "score" } }*/
	};

};
edu.vt.vbi.patric.vis.style.CWVisualStyle.inheritsFrom(edu.vt.vbi.patric.vis.VisualStyle);

