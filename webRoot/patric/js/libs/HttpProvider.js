/*******************************************************************************
 * Copyright (c) 2012 by Jan Philipp, Herrmann & Lenz Solutions GmbH
 *
 * The MIT License (MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

/**
 * Default state model.
 *
 * This implementation only contains <key,value> pairs. This model contains no relation to the actual user session.
 */
Ext.define('hlx.base.state.Model', {

    extend: 'Ext.data.Model',

    idProperty: 'name',

    fields: [
        {
            name: 'name',
            type: 'string'
        },
        {
            name: 'value',
            type: 'string'
        }
    ]

});/*******************************************************************************
 * Copyright (c) 2012 by Jan Philipp, Herrmann & Lenz Solutions GmbH
 *
 * The MIT License (MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

/**
 * Default state store.
 *
 * This implementation relies on a simple remote HTTP based storage.
 */
Ext.define('hlx.base.state.Store', {

    extend  : 'Ext.data.Store',
    requires: [ 'hlx.base.state.Model' ],

    model: 'hlx.base.state.Model',

    pageSize: -1,

    proxy: {
        type  : 'ajax',
        //url   : 'cookies.json',
        url: '/portal/portal/patric/BreadCrumb/WorkspaceWindow?action=b&cacheability=PAGE&action_type=HTTPProvider&action=storage',
        reader: {
            type: 'json',
            root: 'data'
        },
        writer: {
            type: 'json'
        }
    }

});/*******************************************************************************
 * Copyright (c) 2012 by Jan Philipp, Herrmann & Lenz Solutions GmbH
 *
 * The MIT License (MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/

/**
 * A StateProvider solution for Ext JS applications using backend driven states via HTTP.
 *
 * The default configuration enables buffering to avoid multiple write requests.
 */
Ext.define('hlx.base.state.HttpProvider', {

    extend: 'Ext.state.Provider',

    requires: [ 'hlx.base.state.Store' ],

    uses: [ 'Ext.state.Provider', 'Ext.util.Observable' ],

    /**
     * The internal store.
     */
    store: null,

    /**
     * If set to true (not default), the store will be loaded at startup.
     */
    storeSyncOnLoadEnabled: true,

    /**
     * If set to true (default), the store's write event will be buffered to avoid multiple calls at the same time.
     */
    buffered: true,

    /**
     * Defines the buffer time (in milliseconds) for the buffered store.
     */
    writeBuffer: 2000,

    /**
     * Callback which will be called on the first store load. This requires storeSyncOnLoadEnabled = true.
     */
    onFirstLoad: Ext.emptyFn,

    constructor: function (config) {
        config = config || {};
        var me = this;
        Ext.apply(me, config);

        if (!me.store) {
            me.store = me.buildStore();
        }

        /**
         * Unless we found a better solution, the following lines ensure that the component's constructor will be
         * returned only if the store was loaded the first time.
         *
         * See also: http://www.sencha.com/forum/showthread.php?141207
         */
        if (me.storeSyncOnLoadEnabled) {
            // Have to block in order to load the store before leaving the
            // constructor, otherwise, the first query may be against an
            // empty store. There must be a better way...
            var oldValue = Ext.data.Connection.prototype.async;
            Ext.data.Connection.prototype.async = false;
            me.store.load({
                callback: me.onFirstLoad
            });
            Ext.data.Connection.prototype.async = oldValue;
        }

        // Call super
        me.callParent(arguments);

        if (me.buffered) {
            me.on({
                'statechange': {
                    scope : me,
                    buffer: me.writeBuffer,
                    fn    : me.sync
                }
            });
        } else {
            me.on({
                'statechange': {
                    scope: me,
                    fn   : me.sync
                }
            });
        }
    },

    set: function (name, value) {
        var me = this, pos = me.store.find('name', name), row;

        if (pos > -1) {
            row = me.store.getAt(pos);
            row.set('value', me.encodeValue(value));
        } else {
            me.store.add({
                name : name,
                value: me.encodeValue(value)
            });
        }

        me.fireEvent('statechange', me, name, value);
    },

    get: function (name, defaultValue) {
        var me = this, pos = me.store.findExact('name', name), row, value;
        if (pos > -1) {
            row = me.store.getAt(pos);
            value = me.decodeValue(row.get('value'));
        } else {
            value = defaultValue;
        }
        return value;
    },

    clear: function (name) {
        var me = this, pos = me.store.find('name', name);
        if (pos > -1) {
            me.store.removeAt(pos);
            me.fireEvent('statechange', me, name, null);
        }
    },

    sync: function () {
        this.store.sync();
    },

    buildStore: function () {
        return Ext.create('hlx.base.state.Store');
    }

});