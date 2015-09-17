cd patric-libs
mvn install; cp build/patric-libs.jar            		~/jboss_patric/deploy/patric-war/
cd ..
cd patric-common
mvn package; cp build/patric-common.war        			~/jboss_patric/deploy/patric-war/
cd ..
#cd patric-searches-and-tools
#mvn package; cp build/patric-searches-and-tools.war ~/jboss_patric/deploy/patric-war/
#cd ..
#cd patric-overviews
#mvn package; cp build/patric-overviews.war				~/jboss_patric/deploy/patric-war/
#cd ..
#cd patric-transcriptomics
#mvn package; cp build/patric-transcriptomics.war				~/jboss_patric/deploy/patric-war/
#cd ..
#cd patric-diseaseview
#mvn package; cp build/patric-diseaseview.war				~/jboss_patric/deploy/patric-war/
#cd ..
#cd patric-jbrowse
#mvn package; cp build/patric-jbrowse.war				~/jboss_patric/deploy/patric-war/
#cd ..
