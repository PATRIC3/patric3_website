cp -r ~/dev/git-repo/jbrowse/JBrowse-patric/* WebContent/jbrowse/
mvn package
cp build/patric-jbrowse.war ~/jboss_patric/deploy/patric-war/
