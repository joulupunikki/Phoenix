##### Tools needed

1. Minimum
  * Apache Maven (3.0.5 or later)
  * Java 1.8 JDK
2. Recommended
  * An IDE with Maven support. NetBeans (8.0.2) and Eclipse (Luna 4.4.2) have been tested. 

##### Repositories needed
1. [math](https://github.com/joulupunikki/math) and of course
2. [Phoenix](https://github.com/joulupunikki/Phoenix)

You should do a `mvn clean install -DskipTests` of math first, since it is a dependency for phoenix.

##### To do after cloning

After cloning, copy "per-developer-pom.xml.STATIC" to "per-developer-pom.xml" and paste your local EFS installation directory to perDeveloper.efsDirectory property in "per-developer-pom.xml". This should be all the basic configuration you need to build Phoenix. If "per-developer-pom.xml" is not accessible from "pom.xml" then you may see the following error:

```
[ERROR] The build could not read 1 project -> [Help 1]
[ERROR]
[ERROR]   The project com.github.joulupunikki:Phoenix:0.11-alpha (/home/joulupunikki/projects/java/Phoenix/pom.xml) has 1 error
[ERROR]     Non-resolvable parent POM: Could not find artifact com.github.joulupunikki:Phoenix-config:pom:0.11-alpha in central (http://repo.maven.apache.org/maven2) and 'parent.relativePath' points at wrong local POM @ line 3, column 13 -> [Help 2]
```

##### NetBeans

If you have NetBeans you can copy "Run/Debug/Profile Project" configuration from ide/netbeans to Phoenix root directory where "README.md" resides. These will enable you to run and profile Phoenix with the press of a button. An important thing to do is to make sure your IDE formats only modified lines on save. Choose "Tools"->"Options"->"Editor"->"On Save" and check that for "Java" "reformat" and "remove trailing whitespace" options are set to "modified lines only".

##### Eclipse

If you have Eclipse you can copy Eclipse project configuration files from ide/eclipse to Phoenix root directory where "README.md" resides. Note that Eclipse "Run Project" does not copy resources (eg. PHOENIX/PHOENIX.INI) automatically to EFS installation directory, so you have to do that yourself (if anybody knows how to automate this, without too much tinkering, please let me know.) An important thing to do is to make sure your IDE formats only modified lines on save. Also, the import groups should not have an empty line between them and the default Eclipse import ordering should be removed. All this should be set automatically if you use the project configuration files in ide/eclipse.

##### Configuration

An important thing to do is to make sure your IDE formats only modified lines on save. Also, the import groups should not have an empty line between them and the default Eclipse import ordering should be removed. All this should be set automatically if you use the project configuration files stored in ide/eclipse-directory.

Another important thing is to make sure git handles line endings properly. On a MSWindows machine this can be ensured by executing `git config --local core.autocrlf true` in Phoenix root. On any other OS the proper command is `git config --local core.autocrlf input`.
