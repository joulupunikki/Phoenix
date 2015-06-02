javac */*.java
jar cfm Phoenix.jar manifest.mf static.ini */*.class
mv Phoenix.jar ..
