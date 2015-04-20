Ext.onReady(function() {
  Ext.EventManager.onWindowResize(resizeGraphPanel);

  var browserWidth = Ext.getBody().getSize().width;
  var image_dimemsion = Math.floor((browserWidth - 300) / 100) * 100;

  Ext.create('Ext.form.Panel', {
    renderTo: 'circosPanel',
    border: false,
    layout: 'hbox',
    items: [{
      id: 'configPanel',
      collapsible: true,
      collapseDirection: 'left',
      layout: {
        type: 'vbox'
      },
      defaults: {
        border: false,
        bodyPadding: 5,
        width: '100%'
      },
      width: 300,
      items: [{
        xtype: 'container',
        items: [{
          xtype: 'button',
          cls: 'button',
          text: '<div style="color: #fff">Create Circular Map</div>',
          handler: function() {
            var form = this.up('form').getForm();
            submitCircosRequest(form);
          }
        }]
      }, {
        title: 'Default tracks',
        items: [{
          xtype: 'hiddenfield',
          name: 'genome_id',
          value: genome_id
        }, {
          xtype: 'checkboxfield',
          boxLabel: 'Chromosomes / Plasmids / Contigs',
          name: 'include_outer_track',
          checked: true
        }, {
          xtype: 'checkboxfield',
          boxLabel: 'CDS, forward strand',
          name: 'cds_forward',
          checked: true
        }, {
          xtype: 'checkboxfield',
          boxLabel: 'CDS, reverse strand',
          name: 'cds_reverse',
          checked: true
        }, {
          xtype: 'checkboxfield',
          boxLabel: 'RNAs',
          name: 'rna_both',
          checked: true
        }, {
          xtype: 'checkboxfield',
          boxLabel: 'Miscellaneous features',
          name: 'misc_both'
        }, {
          xtype: 'container',
          layout: 'hbox',
          padding: '0 0 3px 0',
          items: [{
            xtype: 'checkboxfield',
            width: 120,
            boxLabel: 'GC Content',
            name: 'gc_content',
            checked: true,
            handler: function(self, checked) {
              self.findParentByType("container").child("#gc_content_plot_type").setDisabled(!checked);
            }
          }, {
            xtype: 'combo',
            width: 160,
            itemId: 'gc_content_plot_type',
            name: 'gc_content_plot_type',
            disabled: false,
            valueField: 'value',
            editable: false,
            value: 'line',
            store: Ext.create('Ext.data.Store', {
              fields: ['text', 'value'],
              data: [{
                'text': 'Line Plot',
                'value': 'line'
              }, {
                'text': 'Histogram',
                'value': 'histogram'
              }, {
                'text': 'Heatmap',
                'value': 'heatmap'
              }]
            })
          }]
        }, {
          xtype: 'container',
          layout: 'hbox',
          items: [{
            xtype: 'checkboxfield',
            width: 120,
            boxLabel: 'GC Skew',
            name: 'gc_skew',
            checked: true,
            handler: function(self, checked) {
              self.findParentByType("container").child("#gc_skew_plot_type").setDisabled(!checked);
            }
          }, {
            xtype: 'combo',
            width: 160,
            itemId: 'gc_skew_plot_type',
            name: 'gc_skew_plot_type',
            disabled: false,
            displayField: 'name',
            valueField: 'value',
            editable: false,
            value: 'line',
            store: Ext.create('Ext.data.Store', {
              fields: ['name', 'value'],
              data: [{
                'name': 'Line Plot',
                'value': 'line'
              }, {
                'name': 'Histogram',
                'value': 'histogram'
              }, {
                'name': 'Heatmap',
                'value': 'heatmap'
              }]
            })
          }]
        }]
      }, {
        title: 'Custom tracks',
        items: [{
          xtype: 'container',
          layout: {
            type: 'hbox'
          },
          items: [{
            xtype: 'button',
            text: 'ADD',
            handler: addCustomTrack
          }, {
            xtype: 'component',
            autoEl: { tag: 'img', src: '/patric/images/toolbar_faq_small.png'},
            maxHeight: 17,
            margin: '3px 0 0 10px',
            listeners: {
              'afterrender': function(self, e) {
                Ext.create('Ext.tip.ToolTip', {
                  target: self.el,
                  autoHide: false,
                  closable: true,
                  html: 'Display custom tracks showing genes of interest by specifying keywords. <br>' +
                  ' For example, <br>' +
                  ' secretion <br> membrane <br> transposon OR transposase OR insertion OR mobile'
                });
              }
            }
          }]
        }, {
          id: 'custom_tracks',
          cls: 'clear',
          border: false,
          minHeight: 0,
          items: []
        }]
      }, {
        title: 'Upload your own data',
        items: [{
          xtype: 'container',
          layout: {
            type: 'hbox'
          },
          items: [{
            xtype: 'button',
            text: 'ADD',
            handler: addFileTrack,
          }, {
            xtype: 'component',
            autoEl: { tag: 'img', src: '/patric/images/toolbar_faq_small.png'},
            maxHeight: 17,
            margin: '3px 0 0 10px',
            listeners: {
              'afterrender': function(self, e) {
                Ext.create('Ext.tip.ToolTip', {
                  target: self.el,
                  autoHide: false,
                  closable: true,
                  html: 'To display your data as "Tiles" plot, upload data file containing accession, start, and end position delimited by tabs.<br>' + 
                  'For example, <br>NC_000962&nbsp;&nbsp;&nbsp;&nbsp;34&nbsp;&nbsp;&nbsp;&nbsp;1524<br>NC_000962&nbsp;&nbsp;&nbsp;&nbsp;2052&nbsp;&nbsp;&nbsp;&nbsp;3260<br><br>' +
                  'To display your data as "Line, Histogram, or Heatmap" plot, upload data file containing accession, start, end, and quantitative value delimited by tabs.<br>' +
                  'For example, <br>NC_000962&nbsp;&nbsp;&nbsp;&nbsp;3596001&nbsp;&nbsp;&nbsp;&nbsp;3598000&nbsp;&nbsp;&nbsp;&nbsp;0.639500<br>NC_000962&nbsp;&nbsp;&nbsp;&nbsp;1498001&nbsp;&nbsp;&nbsp;&nbsp;1500000&nbsp;&nbsp;&nbsp;&nbsp;0.673000'
                });
              }
            }
          }]
        }, {
          id: 'file_tracks',
          cls: 'clear',
          border: false,
          minHeight: 0,
          items: []
        }, {
          xtype: 'filefield',
          name: 'hidden_file_field',
          hidden: true
        }]
      }, {
        title: 'Image properties',
        items: [{
          xtype: 'textfield',
          fieldLabel: 'Image Width/Height',
          name: 'image_dimensions',
          labelWidth: 150,
          width: 280,
          emptyText: image_dimemsion
        }, {
          xtype: 'sliderfield',
          fieldLabel: 'Track Width (1-5% of image size)',
          name: 'track_width',
          labelWidth: 150,
          width: 280,
          value: 3,
          minValue: 1,
          maxValue: 5
        }]
      }, {
        xtype: 'container',
        items: [{
          xtype: 'button',
          cls: 'button',
          text: '<div style="color: #fff">Create Circular Map</div>',
          handler: function() {
            var form = this.up('form').getForm();
            submitCircosRequest(form);
          }
        }]
      }],
      listeners: {
        collapse: {
          fn: function() {
            Ext.getCmp("graphPanel").setWidth(Ext.getBody().getSize().width - 30);
          }
        },
        expand: resizeGraphPanel
      }
    }, {
      id: 'graphPanel',
      border: false,
      overflowX: 'scroll',
      overflowY: 'scroll',
      minWidth: (browserWidth - 300),
      minHeight: image_dimemsion,
      contentEl: 'circosGraph'
    }]
  });

  if (Ext.get("tabs_circosviewer")) {
    Ext.get("tabs_circosviewer").addCls("sel");
  }

  // submit form initially
  var form = Ext.getCmp("configPanel").up('form').getForm();
  submitCircosRequest(form);
});

function resizeGraphPanel() {
  Ext.getCmp("graphPanel").setWidth(Ext.getBody().getSize().width - 300);
}

function submitCircosRequest(form) {
  if (countSequences > 200) {
    Ext.MessageBox.alert("", "This genome has more than 200 contigs. The circular genome map is not displayed as it can be too busy and uninterpretable.");
  }
  else {
    if (form.isValid()) {
      Ext.get("circosGraph").mask('Generating circular genome map. This may take a few minutes.');
      form.submit({
        url: form_url,
        success: function(form, action) {
          loadCircosMap(action.result);
        }
      });
    }
  }
}

function loadCircosMap(conf) {
  Ext.Ajax.request({
            url: '/patric/shared_tmp/circos/' + conf.imageId + '/circos.html',
            success: function(rs) {
              Ext.get("circosGraph").unmask();
              var graph = Ext.getDom("circosGraph");
              graph.innerHTML = rs.responseText
                      + '<div id="circosLegend"><label for="trlist">List of tracks, from outside to inside: </label><span id="trlist">'
                      + conf.trackList
                      + '</span></div><button class="button" onclick="saveImage()">Download Image</button><div class="clear"></div><img src="/patric/shared_tmp/circos/'
                      + conf.imageId + '/circos.svg" usemap="#circosmap">';
            }
          });
}

// Variable to store current number of custom tracks
var customTrackCount = 0;
var fileTrackCount = 0;

function addCustomTrack() {
  var panelParent = Ext.getCmp("custom_tracks");

  var ct = Ext.create('Ext.panel.Panel', {
    layout: 'hbox',
    border: false,
    id: ('custom_track_' + customTrackCount),
    padding: '3 0 0 0',
    items: [{
      xtype: 'combo',
      name: ('custom_track_type_' + customTrackCount),
      displayField: 'name',
      valueField: 'value',
      editable: false,
      value: '',
      width: 90,
      store: Ext.create('Ext.data.Store', {
        fields: ['name', 'value'],
        data: [{
          'name': 'Type',
          'value': ''
        }, {
          'name': 'CDS',
          'value': 'cds'
        }, {
          'name': 'RNA',
          'value': 'rna'
        }, {
          'name': 'Miscellaneous',
          'value': 'misc'
        }]
      }),
      padding: '0 3 0 0',
      validator: function(value) {
        if (value === 'Type') {
          return false;
        } else {
          return true;
        }
      }
    }, {
      xtype: 'combo',
      name: ('custom_track_strand_' + customTrackCount),
      displayField: 'name',
      valueField: 'value',
      editable: false,
      value: '',
      width: 70,
      store: Ext.create('Ext.data.Store', {
        fields: ['name', 'value'],
        data: [{
          'name': 'Strand',
          'value': ''
        }, {
          'name': 'Both',
          'value': 'both'
        }, {
          'name': 'Forward',
          'value': 'forward'
        }, {
          'name': 'Reverse',
          'value': 'reverse'
        }]
      }),
      padding: '0 3 0 0',
      validator: function(value) {
        if (value === 'Strand') {
          return false;
        } else {
          return true;
        }
      }
    }, {
      xtype: 'textfield',
      name: ('custom_track_keyword_' + customTrackCount),
      emptyText: 'keyword',
      width: 100,
      padding: '0 3 0 0',
      validator: function(value) {
        if (value === '') {
          return false;
        } else {
          return true;
        }
      }
    }, {
      xtype: 'button',
      text: '-',
      handler: function() {
        var parent = this.findParentByType('panel');
        Ext.getCmp("custom_tracks").remove(parent.id);
      }
    }]
  });

  panelParent.insert(customTrackCount, ct);
  customTrackCount++;
}

function addFileTrack() {
  var panelParent = Ext.getCmp("file_tracks");

  var ct = Ext.create('Ext.panel.Panel', {
    layout: 'hbox',
    border: false,
    id: ('file_' + fileTrackCount),
    padding: '3 0 0 0',
    items: [{
      xtype: 'combo',
      name: ('file_plot_type_' + fileTrackCount),
      displayField: 'name',
      valueField: 'value',
      editable: false,
      value: 'tile',
      width: 80,
      store: Ext.create('Ext.data.Store', {
        fields: ['name', 'value'],
        data: [{
          'name': 'Tiles',
          'value': 'tile'
        }, {
          'name': 'Line Plot',
          'value': 'line'
        }, {
          'name': 'Histogram',
          'value': 'histogram'
        }, {
          'name': 'Heatmap',
          'value': 'heatmap'
        }]
      }),
      padding: '0 3 0 0'
    }, {
      xtype: 'filefield',
      clearOnSubmit: false,
      name: ('file_' + fileTrackCount),
      width: 180,
      padding: '0 3 0 0'
    }, {
      xtype: 'button',
      text: '-',
      handler: function() {
        var parent = this.findParentByType('panel');
        Ext.getCmp("file_tracks").remove(parent.id);
        fileTrackCount--;
      }
    }]
  });

  panelParent.insert(fileTrackCount, ct);
  fileTrackCount++;
}
function linkFeature(id) {
  Ext.Ajax.request({
    url: ds_url + '/genome_feature/' + id,
    success: function(rs) {
      createPopup(JSON.parse(rs.responseText));
    }
  });
}

function createPopup(feature) {
  var strFeature = Ext.String.format("{0}{1}{2}", feature.seed_id, (feature.refseq_locus_tag != null ? " | " + feature.refseq_locus_tag : ""),
          (feature.gene != null ? " | " + feature.gene : ""));
  var strLoc = Ext.String.format("{0}: {1}..{2} ({3})", feature.feature_type, feature.start, feature.end, feature.strand);

  var linkFeature = Ext.String.format("Feature?cType=feature&cId={0}", feature.feature_id);
  var linkGB = Ext.String.format("GenomeBrowser?cType=feature&cId={0}&loc={1}..{2}&tracks=DNA,PATRICGenes", feature.feature_id, feature.start,
          feature.end);
  var linkCRV = Ext.String.format("CompareRegionViewer?cType=feature&cId={0}&tracks=&regions=5&window=10000&loc=1..10000", feature.feature_id);
  var linkPW = Ext.String.format("PathwayTable?cType=feature&cId={0}", feature.feature_id);
  var linkTR = Ext.String.format("TranscriptomicsGeneExp?cType=feature&cId={0}&sampleId=&colId=&log_ratio=&zscore=", feature.feature_id);
  var linkCR = Ext.String.format("TranscriptomicsGeneCorrelated?cType=feature&cId={0}", feature.feature_id);

  Ext.create(
          'Ext.window.Window',
          {
            width: 500,
            border: false,
            bodyStyle: {
              'background-color': '#fff'
            },
            items: [
                {
                  xtype: 'displayfield',
                  value: '<h2>Feature Details</h2>',
                  padding: 5
                },
                {
                  xtype: 'panel',
                  border: false,
                  items: [{
                    html: '<a href="' + linkFeature + '" target="_blank">' + strFeature + '</a>',
                    border: false,
                    padding: '5 0 5 15'
                  }, {
                    xtype: 'displayfield',
                    value: feature.product,
                    padding: '5 0 5 15'
                  }, {
                    xtype: 'displayfield',
                    value: strLoc,
                    padding: '5 0 5 15'
                  }]
                },
                {
                  xtype: 'displayfield',
                  value: '<h2>For this feature, view:</h2>',
                  padding: 5
                },
                {
                  xtype: 'panel',
                  border: false,
                  items: [{
                    html: '<a href="' + linkGB + '" target="_blank">Genome Browser</a> &nbsp; <a href="' + linkCRV
                            + '" target="_blank">Compare Region Viewer</a> &nbsp; <a href="' + linkPW
                            + '" target="_blank">Pathways</a> &nbsp; <a href="' + linkTR
                            + '" target="_blank">Transcriptomics Data</a> &nbsp; <a href="' + linkCR + '" target="_blank">Correlated genes</a>',
                    padding: '5 0 5 15',
                    style: {
                      'line-height': '1.8em'
                    },
                    border: false
                  }]
                }],
            buttons: [{
              text: 'OK',
              handler: function() {
                var w = this.up('window');
                w.close();
              }
            }]
          }).show();
}

var tooltipFired = false;
var tooltipTarget, tooltipId;

function tooltipFeature(target, id) {
  tooltipTarget = target;
  tooltipId = id;

  if (tooltipFired !== false) {
    clearTimeout(tooltipFired);
  }
  tooltipFired = setTimeout(createTooltip, 1000);
}
function createTooltip() {
  if (tooltipTarget.id == "") {
    Ext.create('Ext.tip.ToolTip', {
      target: tooltipTarget.id || tooltipTarget,
      trackMouse: true,
      html: '',
      tooltip: 10,
      bodyStyle: {
        'line-height': '2em'
      },
      listeners: {
        beforeshow: function updateTipBody(tip) {
          if (tip.html === "") {
            Ext.Ajax.request({
              url: ds_url + '/genome_feature/' + tooltipId,
              success: function(rs) {
                var feature = JSON.parse(rs.responseText);
                var strFeature = Ext.String.format("{0}{1}{2}", feature.seed_id, (feature.refseq_locus_tag != null ? " | "
                        + feature.refseq_locus_tag : ""), (feature.gene != null ? " | " + feature.gene : ""));
                var strLoc = Ext.String.format("{0}: {1} .. {2} ({3})", feature.feature_type, feature.start, feature.end, feature.strand);
                tip.update('<b>' + strFeature + '</b><br/>' + feature.product + '<br/>' + strLoc + '<br/><i>Click for detail information</i>');
              }
            });
          }
        }
      }
    });
  }
}
function saveImage() {
  var imgsrc = Ext.query("#circosGraph > img")[0].src;

  var a = document.createElement("a");
  document.body.appendChild(a);
  a.download = "circular_viewer.svg";
  a.href = imgsrc;
  a.target = "_self";
  a.click();
}
