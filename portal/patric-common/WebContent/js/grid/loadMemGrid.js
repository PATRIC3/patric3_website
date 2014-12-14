var loadMemStore = function(reload) {

	var Page = $Page, property = Page.getPageProperties(), hash = property.hash, which = hash.hasOwnProperty('cat') ? hash.cat : hash.aT ? hash.aT : 0, grid = Page.getGrid() || null, store = Page.getStore(which) || null, checkbox = Page.getCheckBox() || null, hash = property.hash, scm = property.scm[which], i, s, plugin = property.plugin, plugintype = property.plugintype, state = property.stateId ? Ext.state.Manager.get(property.stateId[which]) : null, pageSize = 20, isExceededMaxPageSize = false, global;

	if (Ext.state.Manager.get('pagesize') != undefined && Ext.state.Manager.get('pagesize').value != undefined) {
		global = Ext.state.Manager.get('pagesize').value;
		if (property.maxPageSize != undefined && property.maxPageSize < global) {
			pageSize = property.maxPageSize;
			isExceededMaxPageSize = true;
		} else {
			pageSize = global;
		}
	}

	if (!store || (reload != null && reload == true)) {

		if (grid != undefined) {
			grid.container.mask('loading');
		}
		Ext.Ajax.request({
			url: hash.hasOwnProperty('cat') ? configuration[property.name[which]].url : (property.url[which] ? property.url[which] : property.url[0]),
			params: property.extraParams(),
			success: function(rs) {
				var myData = Ext.decode(rs.responseText);

				store = Page.store[which] = Ext.create('Ext.data.Store', {
					storeId : 'ds',
					model : property.model[which],
					proxy : {
						type : 'pagingmemory',
						data : myData,
						reader : {
							type : 'json',
							root : 'results',
							totalProperty : 'total'
						}
					},
					listeners : {
						datachanged : function(me, options) {
							MaskGrid(me.getTotalCount(), property.callBackFn);
						}
					},
					autoLoad : false,
					remoteSort : property.remoteSort,
					pageSize : pageSize
				}).load();

				var sorter = [];
				if (property.remoteSort && ((state && state.sort) || property.sort)) {
					s = (state && state.sort) ? state.sort : property.sort[which];

					for (i = 0; i < s.length; i++) {
						var aSorter = {
							property: s[i].property,
							direction: s[i].direction
						};
						sorter.push(aSorter);
					}
				}
				// console.log("default sorting on: ", sorter);
				store.sort(sorter);

				loadMemGrid(store);
				if (grid != undefined) {
					grid.container.unmask();
				}
			}
		});
	}
	else {
		store.load();
		loadMemGrid(store);
	}
}

var loadMemGrid = function(store) {

	var Page = $Page, property = Page.getPageProperties(), hash = property.hash, which = hash.hasOwnProperty('cat') ? hash.cat : hash.aT ? hash.aT : 0, grid = Page.getGrid() || null, /*store = Page.getStore(which) || null,*/ checkbox = Page.getCheckBox() || null, hash = property.hash, scm = property.scm[which], i, s, plugin = property.plugin, plugintype = property.plugintype, state = property.stateId ? Ext.state.Manager.get(property.stateId[which]) : null, pageSize = 20, isExceededMaxPageSize = false, global;

	if (!store) {
		scm.forEach(function(element, index, array) {
			if (!element.id) {
				element.id = property.model[which] + '_' + element.dataIndex;
			}
		});
	}

	if (!grid) {
		grid = Page.grid = Ext.create('Ext.grid.PATRICGrid', {
			store : store,
			columns : {
				items : scm
			},
			tbar : property.cart ? createToolbar(property.cartType, (property.WoWorkspace!=undefined && property.WoWorkspace) ? "table_wo_workspace" : "", property.gridType) : "",
			plugins : (plugin && plugintype == "checkbox") ? checkbox : property.pluginConfig,
			dockedItems : [{
				dock : 'bottom',
				xtype : 'patricpagingtoolbar',
				store : store,
				id : 'pagingtoolbar',
				displayMsg : property.pagingBarMsg ? property.pagingBarMsg[which] : 'Displaying record {0} - {1} of {2}',
				maxPageSize : (property.maxPageSize != undefined) ? property.maxPageSize : 5000
			}],
			renderTo : 'PATRICGrid',
			border : property.border ? property.border : false,
			stateful : true,
			stateEvents : ['hide', 'show', 'columnmove', 'columnresize', 'sortchange'],
			stateId : property.stateId ? property.stateId[which] : "NA",
			viewConfig: {
				stripeRows: true
			}
		});

		if (property.hideToolbar)
			grid.removeDocked(grid.getDockedItems()[0]);

	} else {
		if (property.stateId)
			grid.stateId = property.stateId[which];

		// update display message
		if (property.pagingBarMsg != undefined) {
			grid.getDockedItems('pagingtoolbar')[0].displayMsg = property.pagingBarMsg[which];
		}

		grid.sortchangeOption = false;

		if (plugin && plugintype == 'rowexpander') {
			grid.reconfigure(store);
		} else {
			grid.reconfigure(store, scm);
		}

		grid.getDockedItems("pagingtoolbar")[0].bindStore(store);
		grid.getDockedItems('pagingtoolbar')[0].updateInfo();
		grid.getDockedItems('pagingtoolbar')[0].setPageSize(store.pageSize);
		grid.sortchangeOption = true;
	}
	if (property.stateId) {
		grid.stateId = property.stateId[which];
	}
	if (state) {
		ApplyState(state, grid);
	}

	if (Modernizr && !Modernizr.history) {
		if (property.current_hash != window.location.href.split("#")[1]) {
			property.current_hash = window.location.href.split("#")[1];
		}
	}
	if (isExceededMaxPageSize == true && store.totalCount > property.maxPageSize) {
		grid.getDockedItems("pagingtoolbar")[0].showMessageTip("Message", "At this time, we can only show " + property.maxPageSize + " records at a time for these type of tables.");
	}
};