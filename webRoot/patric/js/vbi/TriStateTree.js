/**
 * @class Ext.ux.tree.tristate.Model
 * @extends Ext.data.Model
 * 
 * This class defines a data model for three state tree.
 */

Ext.define('Ext.ux.tree.tristate.Model', {
	extend: 'Ext.data.Model',
	idProperty: 'id',
	fields: [{
		name: 'id', type: 'int'
	}, {
		name: 'name', type: 'string'
	}, {
		name: 'checked', type: 'boolean', defaultValue: false
	}, {
		name: 'partial', type: 'boolean', defaultValue: false
	}, {
		name: 'iconCls', type: 'string', defaultValue: 'x-tree-noicon'
	}, {
		name: 'leaf', type: 'boolean'
	}]
});


/**
 * @class Ext.ux.tree.tristate.Plugin
 * @extends Ext.AbstractPlugin
 * 
 * This class defines a plugin for tristate tree view
 */
 
Ext.define('Ext.ux.tree.tristate.Plugin', {
	extend: 'Ext.AbstractPlugin',
	alias: 'plugin.tristatetreeplugin',
	init: function(view) {
		view.on('checkchange', this.updateRecord, this);
	},
	updateRecord: function(record, value) {
		var me = this;
		//console.log("updteRecord is called");
		record.cascadeBy(function(child) {
			child.set('checked', value);
			child.set('partial', false);
		});
		if (record.parentNode != undefined) {
			me.updateAncestor(record.parentNode);
		}
	},
	updateAncestor: function(record) {
		//console.log("updateAncestor is called");
		var count,
			partial;
		record.bubble(function(parent) {
			count = 0,
			partial = false;
			parent.eachChild(function(sibling) {
				if (sibling.get('partial')) {
					partial = true;
				}
				if (sibling.get('checked')) {
					count++;
				}
			});
			if (partial) {
				parent.set('partial', true);
			} else {
				if (count == parent.childNodes.length) {
					parent.set('checked', true);
					parent.set('partial', false);
				}
				else if (count == 0) {
					parent.set('checked', false);
					parent.set('partial', false);
				}
				else {
					parent.set('partial', true);
				}
			}
		});
	}	
});


/**
 * @class Ext.ux.tree.tristate.Column
 * @extends Ext.grid.column.Column
 * 
 * see http://docs.sencha.com/extjs/4.2.1/#!/api/Ext.tree.Column for original code
 */
 
Ext.define('Ext.ux.tree.tristate.Column', {
	extend: 'Ext.grid.column.Column',
	alias: 'widget.tristatetreecolumn',
    tdCls: Ext.baseCSSPrefix + 'grid-cell-treecolumn',

    autoLock: true,
    lockable: false,
    draggable: false,
    hideable: false,

    iconCls: Ext.baseCSSPrefix + 'tree-icon',
    checkboxCls: Ext.baseCSSPrefix + 'tree-checkbox',
    elbowCls: Ext.baseCSSPrefix + 'tree-elbow',
    expanderCls: Ext.baseCSSPrefix + 'tree-expander',
    textCls: Ext.baseCSSPrefix + 'tree-node-text',
    innerCls: Ext.baseCSSPrefix + 'grid-cell-inner-treecolumn',
    isTreeColumn: true,

    cellTpl: [
        '<tpl for="lines">',
            '<img src="{parent.blankUrl}" class="{parent.childCls} {parent.elbowCls}-img ',
            '{parent.elbowCls}-<tpl if=".">line<tpl else>empty</tpl>"/>',
        '</tpl>',
        '<img src="{blankUrl}" class="{childCls} {elbowCls}-img {elbowCls}',
            '<tpl if="isLast">-end</tpl><tpl if="expandable">-plus {expanderCls}</tpl>"/>',
        '<tpl if="checked !== null">',
            '<input type="button" role="checkbox" <tpl if="checked">aria-checked="true" </tpl>',
                'class="{childCls} {checkboxCls}<tpl if="partial"> {checkboxCls}-partial<tpl elseif="checked"> {checkboxCls}-checked</tpl>"/>',
        '</tpl>',
        '<img src="{blankUrl}" class="{childCls} {baseIconCls} ',
            '{baseIconCls}-<tpl if="leaf">leaf<tpl else>parent</tpl> {iconCls}"',
            '<tpl if="icon">style="background-image:url({icon})"</tpl>/>',
        '<tpl if="href">',
            '<a href="{href}" target="{hrefTarget}" class="{textCls} {childCls}">{value}</a>',
        '<tpl else>',
            '<span class="{textCls} {childCls}">{value}</span>',
        '</tpl>'
    ],
    initComponent: function() {
        var me = this;

        me.origRenderer = me.renderer;
        me.origScope = me.scope || window;

        me.renderer = me.treeRenderer;
        me.scope = me;

        me.callParent();
    },
    treeRenderer: function(value, metaData, record, rowIdx, colIdx, store, view){
        var me = this,
            cls = record.get('cls'),
            renderer = me.origRenderer,
            data = record.data,
            parent = record.parentNode,
            rootVisible = view.rootVisible,
            lines = [],
            parentData;

        if (cls) {
            metaData.tdCls += ' ' + cls;
        }

        while (parent && (rootVisible || parent.data.depth > 0)) {
            parentData = parent.data;
            lines[rootVisible ? parentData.depth : parentData.depth - 1] =
                    parentData.isLast ? 0 : 1;
            parent = parent.parentNode;
        }

        return me.getTpl('cellTpl').apply({
            record: record,
            baseIconCls: me.iconCls,
            iconCls: data.iconCls,
            icon: data.icon,
            checkboxCls: me.checkboxCls,
			partial: data.partial,
            checked: data.checked,
            elbowCls: me.elbowCls,
            expanderCls: me.expanderCls,
            textCls: me.textCls,
            leaf: data.leaf,
            expandable: record.isExpandable(),
            isLast: data.isLast,
            blankUrl: Ext.BLANK_IMAGE_URL,
            href: data.href,
            hrefTarget: data.hrefTarget,
            lines: lines,
            metaData: metaData,
            // subclasses or overrides can implement a getChildCls() method, which can
            // return an extra class to add to all of the cell's child elements (icon,
            // expander, elbow, checkbox).  This is used by the rtl override to add the
            // "x-rtl" class to these elements.
            childCls: me.getChildCls ? me.getChildCls() + ' ' : '',
            value: renderer ? renderer.apply(me.origScope, arguments) : value
        });
    }
});