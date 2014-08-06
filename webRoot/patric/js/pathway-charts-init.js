(function() {
	var figfamMouseoutHandler, figfamMouseoverHandler, genericClickHandler;

	//generic handlers
	genericClickHandler = function(d, i, meta) {
		if (typeof console !== "undefined" && console !== null) {
			console.log(d, i);
		}
		return typeof console !== "undefined" && console !== null ? console.log(meta) : void 0;
	};

	// figfam handlers
	pathwayMouseoverHandler = function(d, i, meta) {
		var barwidth, content, str;
		$(meta.chartTarget).find(".data-tooltip").remove();
		barwidth = $(meta.clickTarget).attr("width");
		
		//console.log(d, i, meta);
		content = d[meta.set][meta.barindex];
		str = "<div class='data-tooltip'>" + content + "</div>";
		
		/*
			Position the tooltip a little above the chart element that called it. We also add
			the dataset as a class so that we can create different styles for the toolips in
			the CSS file.
		*/
		return $(meta.chartTarget).append(str).find(".data-tooltip").addClass(meta.set).position({
			at: 'top',
			of: $(meta.clickTarget),
			my: 'bottom',
			offset: (barwidth / 2) + " 3"
		});
	};

	pathwayMouseoutHandler = function(d, i, meta) {
		return $(meta.chartTarget).find(".data-tooltip").remove();
	};

	$().ready(function(event) {
		new PathwayChart({
			target: "#dlp-pathways-conservation",
			datafile: "/patric-common/data/pathways.json",
			headerSelector: "#dlp-pathways-conservation-header h3",
			descSelector: "#dlp-pathways-conservation-header .desc",
			clickHandler: genericClickHandler,
			mouseoverHandler: pathwayMouseoverHandler,
			mouseoutHandler: pathwayMouseoutHandler
		});
		return;
	});
}).call(this);
