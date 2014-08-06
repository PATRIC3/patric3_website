function writeBreadCrumb(type) {

  var cId = "", name = "", h = Ext.getDom("grid_result_summary"), vfgId = $Page.getPageProperties().hash.VFGId;

  if (type == 0 || type == 1)
    cId = Ext.getDom("cId").value;
  else
    name = Ext.getDom("name").value;

  Ext.Ajax.request({
    url: "/patric-diseaseview/jsp/get_breadcrumb.jsp",
    method: 'GET',
    params: {
      cId: cId,
      name: name,
      type: type == 1 ? 0 : type
    },
    success: function(response, opts) {
      if (type == 0 || type == 1) {
        if (type == 1) {
          Ext.Ajax.request({
            url: "/patric-diseaseview/jsp/get_breadcrumb.jsp",
            method: 'GET',
            params: {
              cId: cId,
              type: type,
              vfgId: vfgId
            },
            success: function(response1, opts1) {
              h.innerHTML = (!vfgId) ? "<b>" + response1.responseText + " distinct homologs found for " + response.responseText
                      + " Virulence Genes</b><br/>" : "<b>" + response1.responseText + " distinct homologs found for Virulence Gene " + vfgId
                      + "</b><br/>";
            }
          });
        } else {
          h.innerHTML = "<b>" + response.responseText + " distinct Virulence Genes from Virulence Factor Database (VFDB) found</b><br/>";
        }
      } else if (type == "ctd" || type == "ctdgraph") {
        h.innerHTML = "<b>" + response.responseText + " distinct Human Disease Genes from Comparative Toxicogenomics Database (CTD) found</b><br/>";
      } else if (type == "gad" || type == "gadgraph") {
        h.innerHTML = "<b>" + response.responseText + " distinct Human Disease Genes from Genetic Association Database (GAD) found</b><br/>";
      }
    }
  });
}