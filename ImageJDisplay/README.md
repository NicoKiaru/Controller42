This is a simple ImageJ2 Command with JOGL maven dependencies.
Natives loading in Fiji can be tedious (cf http://forum.imagej.net/t/fiji-command-unable-to-find-jogl-library-minimal-example/6484).
In this branch, two problems are overcomed :

1. By using <scope>provided</scope> into pom files (thanks @stelfrich), the jogl jars are not overriden during deployement of the plugin

2. By using a dirty hack, JOGL natives are loaded by executing a dummy groovy script before any JOGL method is called (thanks @imagejan and @kephale).

It works, but it's ugly, but it works.

GAV used in the project : 

	<dependency>
      		<groupId>org.jogamp.gluegen</groupId>
      		<artifactId>gluegen-rt-main</artifactId>
		<scope>provided</scope>
    	</dependency>
    	<dependency>
      		<groupId>org.jogamp.jogl</groupId>
      		<artifactId>jogl-all-main</artifactId>
		<scope>provided</scope>
    	</dependency>
