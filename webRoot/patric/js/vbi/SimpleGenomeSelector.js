Ext.Loader.setConfig({enabled: true});
Ext.Loader.setPath('Ext.ux', '/patric/js/extjs/extjs/examples/ux');
Ext.require([
	'Ext.ux.form.MultiSelect'
]);

Ext.define('VBI.SimpleGenomeSelector', {
	extend: 'Ext.panel.Panel',
	width: 380,
	renderTo: 'GenomeSelector',
	items:[{
		xtype: 'textfield',
		itemId: 'searchBox',
		emptyText: 'type genome name for search (minimum 4 chracters)',
		enableKeyEvents: true,
		listeners: {
			keyup: {
				fn: function(me, e, eOpts) {
					if (me.getValue().length >= 4) {
						var store = me.findParentByType('panel').child('#searchResult').getStore();
						store.proxy.extraParams.query = me.getValue();
						store.proxy.extraParams.searchon = 'azlist';
						store.load();
					}
				},
				buffer: 500
			}
		}
	}, {
		xtype: 'multiselect',
		itemId: 'searchResult',
		allowBlank: false,
		minHeight: 250,
		maxHeight: 400,
		store: {
			fields: [{name:'genome_info_id', type:'int'},'genome_name'],
			proxy: {
				type: 'ajax',
				url: '/portal/portal/patric/TaxonomyTree/TaxonomyTreeWindow?action=b&cacheability=PAGE&mode=search&taxonId=131567',
				startParam: undefined,
				limitParam: undefined,
				pageParam: undefined,
				reader: {
					type: 'json',
					root: 'genomeList',
					totalProperty: 'totalCount'
				}
			},
			autoLoad: false
		},
		valueField: 'genome_info_id',
		displayField: 'genome_name',
		value:[]
	}]
});
