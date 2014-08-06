cd /opt/jboss-patric/jboss-deploy/deploy/jboss-web.deployer/ROOT.war/patric/static
#cd /Users/hyun/Applications/jboss-epp-4.3/jboss-as/server/patric/deploy/jboss-web.deployer/ROOT.war/patric/static/

/usr/bin/curl http://enews.patricbrc.org/php/vbi.php > bacteriacloud_data.new.js

if [ -s 'bacteriacloud_data.new.js' ] 
then
#echo "not empty"
if [ `cat bacteriacloud_data.new.js | grep null | wc -l` -eq 0 ]
then
mv bacteriacloud_data.new.js bacteriacloud_data.js
else
echo "null inside"
fi
else
echo "empty"
fi
