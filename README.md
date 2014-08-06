#PATRIC 3.0 - Website source

This repo contains website source for PATRIC 3.0 migration.

## JBoss Portlet Source
The directory named portal contains multiple JBoss web projects, which individually builds war (Web Application Archive) files. You can also build ear (Enterprise ARchive) to deploy in JBoss EPP server.
* portal
	* patric-common
	* patric-static
	* ...
	* patricbrc

```
// before build any project
$ cd portal/patric-libs
$ mvn install

// in order to build an individual project
$ cd porta/patric-common
$ mvn package
$ ls -al build/
// you should be able to see patric-common.war in here

// in order to build entire projects (EAR)
$ cd portal
$ mvn package
$ ls -al patricbrc/target/
// you should be able to see patricbrc-xxx.ear in here
```

## Javascript & css
The directory webRoots contains files to be used as web static files. 

* webRoot
	* patric
		* css
		* html
		* idv-perl
		* images
		* js
		* pig
		* presentations
		* static 

## Development environment
The directory named devEnv includes shared developer settings such as code convention for eclipse formatting.
* devEnv
	* spring-code-conventions.xml
	* google-style-guide-javascript-eclipse.xml