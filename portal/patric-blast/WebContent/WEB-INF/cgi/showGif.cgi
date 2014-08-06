#!/usr/bin/perl -w use strict;
use CGI;

my $cgi=new CGI;
my $gifName=$cgi->param('fileName');
open IMAGE, "./TmpGifs/$gifName";
my ($image, $buff);
while(read IMAGE, $buff, 1024) {
   $image .= $buff;
} close IMAGE;
print "Content-type: image/gif\n\n";
print $image;
