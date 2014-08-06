#!/usr/bin/python
import urllib2
import math
import sys
import gzip

# setting default encoding to utf-8
reload(sys)
sys.setdefaultencoding("UTF-8")

# Parameters
fetchSize = 50000
urlBase = "http://macleod.vbi.vt.edu:8983/solr/genomesummary/select?q=*:*&wt=python&fl=genome_info_id&sort=genome_info_id+asc&rows=%d" % (fetchSize)
urlPage = "&start=0"
lastModDate = "2013-10-09"

# get total count
rs = eval(urllib2.urlopen(urlBase+urlPage).read());
rsTotal = rs.get("response").get("numFound");
print "total result: %d" % (rsTotal);
#rsTotal = 100; #temporary
cntPage = int(math.ceil(float(rsTotal)/fetchSize))

# start generating list
for fileNo in range(cntPage):
    #if fileNo < 462:
    #	continue
    outfile = gzip.open("./sitemaps/genomes-%04d.xml.gz"%(fileNo), "wb")
    
    print >>outfile, """<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">"""
    
    if fileNo*fetchSize > rsTotal: 
        break
    urlPage = "&start=%d" % (fileNo*fetchSize)
    print "fetching:%s" % (urlBase+urlPage)
    rs = eval(urllib2.urlopen(urlBase+urlPage).read())
    items = rs.get("response").get("docs");
    for item in items:
       	print >>outfile, """        <url>
        <loc>http://patricbrc.org/portal/portal/patric/Genome?cType=genome&amp;cId=%d</loc>
        <lastmod>%s</lastmod>
    </url>""" % (item.get("genome_info_id"), lastModDate)
    
    print >>outfile, "</urlset>"
    outfile.close()
