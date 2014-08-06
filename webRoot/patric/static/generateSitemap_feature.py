#!/usr/bin/python
import urllib2
import math
import sys
import gzip

# setting default encoding to utf-8
reload(sys)
sys.setdefaultencoding("UTF-8")

# Parameters
annotationSources = ["PATRIC", "RefSeq", "BRC"]
annotation = annotationSources[2] #0, 1, 2
fetchSize = 50000
urlBase = "http://macleod.vbi.vt.edu:8983/solr/dnafeature/select?q=*:*&wt=python&fl=na_feature_id,locus_tag&sort=na_feature_id+asc&fq=annotation:%s&rows=%d" % (annotation, fetchSize)
urlPage = "&start=0"
lastModDate = "2013-01-26"

# get total count
rs = eval(urllib2.urlopen(urlBase+urlPage).read());
rsTotal = rs.get("response").get("numFound");
print "total result: %d" % (rsTotal);
#rsTotal = 140; #temporary
cntPage = int(math.ceil(float(rsTotal)/fetchSize))

# start generating list
for fileNo in range(cntPage):
    #if fileNo < 462:
    #	continue
    outfile = gzip.open("./sitemaps/features-%s-%04d.xml.gz"%(annotation,fileNo), "wb")
    
    print >>outfile, """<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">"""
    
    if fileNo*fetchSize > rsTotal: 
        break
    urlPage = "&start=%d" % (fileNo*fetchSize)
    print "fetching:%s" % (urlBase+urlPage)
    rs = eval(urllib2.urlopen(urlBase+urlPage).read())
    features = rs.get("response").get("docs");
    for feature in features:
	if feature.get("locus_tag"):
        	print >>outfile, """        <url>
        <loc>http://patricbrc.org/portal/portal/patric/Feature?cType=feature&amp;cId=%d</loc>
        <lastmod>%s</lastmod>
    </url>""" % (feature.get("na_feature_id"), lastModDate)
    
    print >>outfile, "</urlset>"
    outfile.close()
