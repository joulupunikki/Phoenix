Phoenix
=======

Java clone/remake/patch of the game [Emperor of the Fading Suns (EFS)](http://en.wikipedia.org/wiki/Emperor_of_the_Fading_Suns). Uses original EFS data files and requires EFS 1.4 to be installed. Should work with all mods.

If you have a mod that works with EFS but not with Phoenix open an [issue](https://github.com/joulupunikki/Phoenix/issues) (if you have problems with Hyperion, read [New in version 0.9.1](https://github.com/joulupunikki/Phoenix/#new-in-version-091) below first.) If you encounter a Java Exception or Error, or Phoenix won't start or crashes, open an issue. Being bug free is important, being crash free and supporting all EFS mods is critically important.

TOC
===

* [News](https://github.com/joulupunikki/Phoenix#news)
* [Contributing](https://github.com/joulupunikki/Phoenix#contributing)
* [1: Getting Phoenix](https://github.com/joulupunikki/Phoenix#1-getting-phoenix)
* [2: Installing and running](https://github.com/joulupunikki/Phoenix#2-installing-and-running)
* [3: Changes](https://github.com/joulupunikki/Phoenix#3-changes)

News
====

Contributing policy change to "Yes for predefined tasks" as of 28 apr 2015

Required java version for precompiled release binaries changed from java 7 to java 8 as of 5 may 2015

Project format changed from native NetBeans to Apache Maven as of 18 may 2015

Contributing
============

##### Policy
"Yes for predefined tasks." (Tasks which require no programming also available, see below.)

*Important note*: to maximize the chances that Phoenix stays afloat legally, the Phoenix distribution SHALL NOT contain any copyrighted material beyond that which is necessary to reproduce "EFS.EXE" functionality (such as faction names, some static GUI texts and the GUI layout specifications. But NOT the values of variables in "DAT" directory.) Instead, all material SHALL be read from the separately user installed EFS1.4 (and mod) files. As a rule of thumb, if it is not in "EFS.EXE" it must be excluded from the Phoenix distribution. This rule includes the project [wiki](https://github.com/joulupunikki/Phoenix/wiki) (and actually everything published by the legal entity distributing Phoenix.)

Another note: this is my first time officially administering a project publicly, so expect things not necessarily working as expected out of the box.

##### Project format and development tools
Before v0.11 all Phoenix code has so far been Java 7 compatible and [NetBeans](https://netbeans.org/) alone has been the IDE of choice. This is now changing, at least partially. The core Phoenix code will likely stay purely Java, but if convenient, certain functionality could be implemented with other languages and/or even be independent side projects. All such solutions must however be distributable in formats compatible with the major Java platforms (at least BSD/OSX, Linux and Windows.)

Phoenix used to be a NetBeans native project with NetBeans metadata. The project has now been converted to Apache [Maven](https://maven.apache.org/) format (the directory structure is so far not converted to Maven default.) The rationale was that Maven format and tools are supported by all major Java IDEs (eg. Eclipse, IntelliJ IDEA and NetBeans.) A recent [survey](http://zeroturnaround.com/rebellabs/java-tools-and-technologies-landscape-for-2014/6/) had 97% of responders say they use an IDE, with 48% using Eclipse, 26% using IntelliJ IDEA non-free, 10% using NetBeans and 7% using IntelliJ IDEA free. Now, this is just one web survey, but if it has a grain of truth in it, then it seems that by having a common free project format, possibly an order of magnitude increase in potential contributors could be achieved if free choice of IDE is a major deciding factor.

I have most experience with NetBeans, some with Eclipse and none with IntelliJ IDEA, and so far only NetBeans and Eclipse have been tested with the Maven format Phoenix. Thus, if someone comes along with a strong background of IntelliJ IDEA or something else, and would like to stay exclusively with them, the Phoenix project currently does not provide much out of the box support beyond being Maven format. But then again with my short experience in doing larger than small real world projects, I would not expect to be able to advice veteran developers. 



##### A bit of history
This section used to officially discourage all contribution. It's not that I don't value cooperation (and some people have actually provided code) but I considered myself a lousy project manager (still do) and will rather spend my time coding than integrating. But as a project, Phoenix has advanced far more than I wagered when I started. 

I thought the project is probably going to be abandoned before any running code is produced. So far (as of 28.04.2015) it has taken 2,5 years of real time to get here (actually, code reading the various graphics formats was written in spring 2010.) If Phoenix is to reach a stage of playability equaling (and hopefully above and beyond) that of EFS1.4 and I work at the same pace and with the same mentality it may take 2,5 to 5 years to finish at minimum. I would rather it didn't take so long. Thus, the policy of no contributions will be at least temporarily suspended.

Mostly this will depend on my ability as a project manager. I find myself bad at managing teams of people. Thus, contribution process will be as loosely integrated as possible, without compromising ultimate project integrity. This will hopefully be achieved by trying to identify sub projects which require maximally independent implementation.

##### Defined tasks and being assigned
Potential sub projects or simply "tasks" are defined as issues. A preliminary list of tasks is found in issue [#4](https://github.com/joulupunikki/Phoenix/issues/4). All contributable tasks can be found by selecting [issues with the contributable label](https://github.com/joulupunikki/Phoenix/issues?q=is%3Aopen+is%3Aissue+label%3Acontributable) Read the issue of the task that you think you can do and post your intentions to "Task assignment thread" [#12](https://github.com/joulupunikki/Phoenix/issues/12). From simple implementation POV (assuming maximally independent tasks), task assignment may not be strictly necessary. From project POV, task assignment ensures no duplicate, useful, work is produced needlessly.

##### Don't worry about possible failure
Now, I can't force anyone to enlist. But, do not fail to enlist because you think that you may not produce any useful results and that you will be embarassed later for having to publicly admit that, implicitly or explicitly. When I considered setting up camp at GitHub and thus releasing Phoenix source to the public I worried about what people would say about the quality of the code (consider [this](https://github.com/joulupunikki/Phoenix/blob/8818c5a6d78e52681a4b04499706b10599798d92/src/gui/Gui.java#L221) brilliant 30 line display of craftmanship ... of course, that should not be taken as a style suggestion) but I have had nothing but positive responses to Phoenix, even from those who have certainly taken a deeper look at the code. If you enlist, and then fail to produce anything beyond "Sorry, I failed :(" then consider that at maximum, all that was wasted was your time and effort, and even then that was probably a learning experience. You weren't paid and the only administrative effort is removing @yourusername from the assiged list for the task.

Consider these words of famous physicist Freeman Dyson: [You can't possibly get a good technology going without an enormous number of failures. It's a universal rule.](http://en.wikipedia.org/wiki/Freeman_Dyson#The_role_of_failure)

For those who are thinking of coding, tasks "Automated testing" [#5](https://github.com/joulupunikki/Phoenix/issues/5), "Automated test generation" [#6](https://github.com/joulupunikki/Phoenix/issues/6), "Unit window animations" [#7](https://github.com/joulupunikki/Phoenix/issues/7) and "Random galaxy generator" [#8](https://github.com/joulupunikki/Phoenix/issues/8) should provide tasks with simple or no necessary administrative Phoenix code integration. The tasks that require no coding obviously require no code integration and as such cannot have negative impact on Phoenix code.

##### Main repository contract

Notice: the following "repository contract" is not a guarantee or warranty. "Contract" is to be considered a software engineering term, not a legal term.

In the main [Phoenix](https://github.com/joulupunikki/Phoenix) repository the default branch is master. As per git default configuration this is the branch which will be shown when the [Phoenix](https://github.com/joulupunikki/Phoenix) link is clicked. The master branch SHALL contain all the releases, excluding orange "Pre-releases", and is intended to be kept at production quality at all times. A master branch HEAD which does not compile or run is considered a critical bug and should be reported as an [issue](https://github.com/joulupunikki/Phoenix/issues) (presently, I only have access to Ubuntu 1404 and Windows 7 so in the hopefully rare event that errors concern BSD/OSX or something else I am not in the position to make specific attempts to fix bugs in those cases.) Initial feature development and/or testing SHALL not be done on the master branch. The master branch SHALL not be rebased, amended or its history otherwise altered unless critical issues such as serious errors or copyright violations are revealed or the proper authorities (eg. GitHub administrators) issue directives.

For other branches the history is not necessarily conserved as rigidly as that of the master branch. Especially, a branch named "test_release" may be used before releases for testing purposes and should be considered entirely temporary. Tags from "test_release" may appear in [releases](https://github.com/joulupunikki/Phoenix/releases) as transient test releases with the orange "Pre-release" labels. These test releases are for internal testing purposes only.

##### Workflow

GitHub Fork & Pull (on the other side of the fence, [they have](https://www.atlassian.com/git/tutorials/comparing-workflows/forking-workflow) a tutorial on Fork & Pull.)

The file [README_DEV.md](https://github.com/joulupunikki/Phoenix/blob/master/README_DEV.md) contains info on tools needed and necessary configuration to compile Phoenix.

1: Getting Phoenix
==================

If you just want to take a look at where Phoenix is try the "Binary distribution" below. If you are suspicious of precompiled binaries provided by strangers on the Internet, try the "Source distribution" below. The "Raw database" gets you a snapshot of the bare source code and/or git database; some Java knowledge is required to build and run with this, especially on Windows.

1.1: Binary distribution
------------------------
People who just want to try Phoenix should get the binary distribution package named `Phoenix_X.YY.Z.zip`, where X.YY.Z are version numbers, downloadable at [Phoenix releases](https://github.com/joulupunikki/Phoenix/releases) on GitHub.

1.2: Source distribution
------------------------
Build yourself from provided sources, instructions below at "Installing and running, Source distribution". The package is named `Phoenix_src_X.YY.Z.zip`, where X.YY.Z are version numbers, and is downloadable at [Phoenix releases](https://github.com/joulupunikki/Phoenix/releases) on GitHub.

1.3: Raw database
-----------------
(Note: does not contain explicit build or startup files for Windows; Phoenix is buildable and runnable but will require some familiarity with building beyond what can be found in this document.) Command line with git: `git clone https://github.com/joulupunikki/Phoenix.git`. Packages labeled "Source code" at [Phoenix releases](https://github.com/joulupunikki/Phoenix/releases). Or any of the standard methods on GitHub.

2: Installing and running
=========================

The main component of Phoenix is the file `Phoenix.jar` which is intended to be a feature complete, less buggy and additional modern wargame feature implementing replacent for `EFS.EXE`. It comes precompiled with the "Binary distribution" and (hopefully) easily compilable with the "Source distribution". Java knowledge beyond this document is required to compile with the raw sources from the "Raw database". Java 8 (or higher) jdk or jre is needed to run Phoenix.jar (so far, no java 8 features are used so compiling to java 7 compliance is possible. This may change in the future and everyone is encouraged to move to java 8 if possible.)

Important note:
Due to the large size of the uncompressed save files (10MB) and the fact that java's saving process (serialization) is a recursive function the game will likely choke up (stack overflow) during loading and saving with the default stack size. The default stack size thus probably needs to be increased. On windows this is done automatically by clicking on `Phoenix.bat` instead of `Phoenix.jar` or from the command line issue eg. `java -Xss32m -jar Phoenix.jar`. For 1280x1024 window click on `Phoenix1280x1024.bat`.

2.1: Binary distribution
------------------------
Copy `Phoenix.jar`,`Phoenix.bat` and `Phoenix1280x1024.bat` and `PHOENIX` directory to your EFS directory where `EFS.EXE` resides. (If you do not see
all of these files then you probably have got a source distribution or raw database package. See "Getting Phoenix, Binary distribution" above to get the executable version.) To start double click on the `Phoenix.bat` or if you use commandline for a 640x480 window type `java -Xss32m -jar Phoenix.jar`. For a 1280x1024 window type `java -Xss32m -jar Phoenix.jar 2 GALAXY.GAL` or double click on `Phoenix1280x1024.bat`.

If you get an error saying java not found then likely java is not in the path and you need either to put java into the path or use absolute path name. Eg. on windows if your java jdk is installed into 
`C:\Program Files\Java\jdk1.8.0` you would type `"C:\Program Files\Java\jdk1.8.0\bin\java.exe" -jar Phoenix.jar 1 GALAXY.GAL`.

2.2: Source distribution
------------------------
(If you do not see all of the files mentioned here then you probably have got a binary distribution or raw database package. See "Getting Phoenix, Source distribution" above to get the buildable source version.)

Unzip the package then go to the `etc` directory and copy the contents to your EFS directory where `EFS.EXE` resides.

Then go to the `src` directory,
- on Windows click on `build.bat` to build `Phoenix.jar` or type `javac phoenix\Phoenix.java` followed by `jar cfm Phoenix.jar manifest.txt @classes.list`

- on Linux/Unix type `javac phoenix/Phoenix.java` followed by `jar cfm Phoenix.jar manifest.txt */*.class`. 

Copy `Phoenix.jar`,`Phoenix.bat` and `Phoenix1280x1024.bat` to your EFS directory where `EFS.EXE` resides. To run the game follow the instructions for the binary distribution.

2.3: Raw database
-----------------
This is a repository for development. The development versions may not receive explicit notice, documentation or releases. Also does not contain explicit build and startup files. Users just looking to see where Phoenix is currently are encouraged to try out "Binary distribution" or "Source distribution" sections in "Getting Phoenix" header above.

3: Changes
==========

New in version 0.10.2
---------------------
A bit of rewording in documentation. See version 0.10.1 below for details of documentation/packaging fixes.

New in version 0.10.1
---------------------
Notice: this is a quick bugfix release. To browse the new features in the 0.10 series please see New in version 0.10.0 below.

Documentation changes: Change "Usage" heading to "Installing and running". Add "Getting Phoenix" heading.

Documentation and packaging Bugfix: README.md did not have a "Getting Phoenix" heading. Also, the git database does not contain the explicit files necessary to build the sources as instructed in "Installing and running, Source distribution". As a result, users who cloned or otherwise got copy of the GitHub git database and read README.md or README.txt had mismatching package contents and documentation. On Windows especially, it was not possible to build a runnable `Phoenix.jar` following the instructions in the provided documentation unless one knew some details about building.

New in version 0.10.0
--------------------------
City capture with planetside units implemented.

City building by engineers implemented. Road building not yet possible.

PBEM with mandatory security implemented. Includes passwords and datafile integrity checking. Password revocation feature also included. If at least three factions are human controlled then at the start of a players turn, instead of entering the password, password revocation sequence of the current player may be initiated by pressing the "password revocation" button. Dialog will be then displayed querying the revocation action which may be either password zeroing or setting faction to computer control. After that the revocation sequence is initiated by saving the game. The revocation sequence is like a full year of pbem game except that instead of playing their turn each human player other than the one who is the target of the revocation will be asked for their password to confirm the revocation. When the last player gives their consent by giving their password the sequence completes and the password of the target player will be zeroed and thus when the target player's turn comes he will be asked for a new password. Or if computer control was selected as revocation action the target player's faction will be permanently set to computer control. PBEM game resumes normally from this point onwards.

Messages partially implemented. Selecting "messages"->"read messages" from menubar will bring up messages window. Messages accumulated since the end of your previous turn will be listed in a table. Double click on the "message" column to show message details. Read messages are displayed in light gray color. Currently only combat reports are viewable. The "location" column will show the planet (and possibly coordinates on planet) where the message originates. Click on the "location" column to goto the message origin.

When units are at shoreline, loading and unloading of cargo at ocean shores works more or less as in original EFS. You can currently load air units as normal cargo.

Cargo transfer dialog implemented. In stack window/unit info window drag and drop cargo pods onto each other to join them and to empty slots to split them.

Galaxy name parameter for command line must be either GALAXY.GAL or GAL\<galaxyname> so you can have a GAL directory containing galaxy files.

New in version 0.9.1
--------------------
Notice: this is a quick bugfix release. To browse the new features in the 0.9 series please see New in version 0.9.0 below.

There is a bug(*) in Hyperion 1.4g which prevents Phoenix from loading DAT/UNITSPOT.DAT. In UNITSPOT.DAT line 67 reads `     "desert"  "2.0 1.0 2.2.0.0 1.0 0.5 2.0 0.5 1.0 1.0"` when it should be `     "desert"  "2.0 1.0 2.0 2.0 1.0 0.5 2.0 0.5 1.0 1.0"` ie there is a dot between the Trd and Air columns which makes Phoenix choke. Correct this and Hyperion should start.

Also in Hyperion 1.4g remember to replace UNIT.DAT with UNIT3.DAT to enable building of advanced units. This must be done before a game is started since Phoenix loads datafiles only once during program start.

(*) Technically, this is a feature, not a bug, since it works with EFS1.4 "EFS.EXE".

New in version 0.9.0
--------------------

Resources implemented. Primary resource harvesting and secondary resource production implemented by RSW. Resource icons and resource amount display added to planet window and build panel. Click on a resource icon in planet window to see resource production and consumption statistics. Resource amount on planet window shown in red if planetary (galactic if universal warehouse on) resource consumption exceeds production. Note that since messages are not fully implemented yet you won't be notified if you lack the necessary resources for secondary production.

Food consumption implemented by RSW.

Unit building implemented. To build select "orders"->"build units" form menubar or use standard EFS methods. Differs from standard EFS in that on the lower left of the build window you have a list of planets where you have cities. Select planet by  clicking and then on the lower right a list of cities on the planet will be displayed. Select a city by clicking and you will have EFS style selection of buildable units on the upper left and on the upper right you have a build queue. Only units for which you have required technologies will be buildable. If you click on the build button in planet window or city window the planet and city will be pre-selected so that the current city will be used. Double click on units to add to the build queue. Double click on units in the build queue to remove units from the build queue. If resources necessary to build unit are missing the unit will be shown red in the selection of buildable units. If the input unit is missing the unit will be shown in white. It is possible to queue units for which you don't have enough resources or input units. In such a case the city build queue will be put on hold and the first unit in the queue will be shown in gray color. Units will be built on city hex or immediately surrounding hexes if city hex is full. If all hexes are full production will be delayed by one turn. Note that since messages are not fully implemented yet you won't be notified when building is completed.

Research and technologies implemented. To research select "orders"->"research" form menubar. Research works more or less as in standard EFS except that all labs are assigned as one. Doubleclick on technologies to assign labs. Note that since messages are not fully implemented yet you won't be notified at beginning of your turn if research is completed.

Manowitz archives implemented. Manowitz works as in standard EFS except for the previous button which takes you to the beginning of the previous chapter.

PHOENIX/PHOENIX.INI file introduced to contain configurable game parameters beyond those in EFS.INI.

Currently Phoenix has a Wizard mode menu to aid in testing. You can get all techs, 999 of all resources on current planet or randomize the RNG.

Currently the double red triangle button in planet and space windows goes through cargo pods instead of all faction units.

New in version 0.8.3
--------------------

Added space button to planet window.

Changed hex grid color to darker gray.

See also New in version 0.8.2

New in version 0.8.2
--------------------

Bugfix: java cmd line options: use -Xss32m instead of -Xss99m to prevent out of memory errors especially during saving and loading. Thanks to RSW.

See also New in version 0.8.1

New in version 0.8.1
--------------------

Bugfix: Nova mod datafiles are supported.

Bugfix: loading of datafiles will log failures to read files.

Bugfix: java: loading and saving games will catch throwables.

Errors, exceptions and failures to read data files will be logged to `phoenixlog.txt`. Previous log will be saved to `phoenixlog.txt.1`.

New in version 0.8.0
--------------------

Switched to [semantic version](http://semver.org) numbering .

Stack selection. On planet/space window click center mouse button on 
stack display to open stack selection pop-up menu.

Spotting, unseen planets and unexplored hexes implemented. Spotting works like in original EFS except space spotting which simply compares spot and camo values and spots if spot >= camo.

New in version 7:
-----------------

Loading and saving games works, the progress bar displays are a bit off-tune yet.

Important note:
Due to the large size of the uncompressed save files (10MB) and the fact that java's saving process (serialization) is a recursive function the game will likely choke up (stack overflow) during loading and saving with the default stack size. The default stack size thus propably needs to be increased. On windows this is done automatically by clicking on `Phoenix.bat` instead of `Phoenix.jar` or from the command line issue eg. `java -Xss99m -jar Phoenix.jar` for 1280x960 window click `Phoenix1280x960.bat`

Basic space combat works with fighters and bombers aboard carriers participating in combat. Marauder legions and psychic cargo units do not yet participate in combat.

Navigable global minimap added to planet window. Click on minimap to navigate.

Navigable galactic minimap added to space window. Click on minimap to navigate.

New in version 6:
-----------------

Turns and factions implemented. Game begins with a main menu window where you select which factions are human controlled. Since there is no AI, factions which are unselected are simply skipped during game. During game click on the end turn button to end turn and the green arrows to skip to next unmoved units.

Planet to space movement implemented. When only spaceships are selected on planets click on the rocket button to launch into space. Landing spaceships also works. Spaceships can land on dry ocean floor on barren planets.
 
Loading & Unloading cargo works by drag & drop. 

Basic ground combat works; note that combat resolution differs from EFS so that you are first presented with a display of units participating in combat and need to click the "Do combat"-button to initiate battle and that there is no display of intermediate results. Instead the final results are immediately displayed. Routed units do not display health on the combat window.

Capturing routed units works.

New in version 5:
-----------------

Movement in space works, note its not possible to select a route spanning multiple jump points. PlanetToSpace movement not yet implemented. Right clicking the stack display on the left side of planet and space maps brings up the unit info window, which really doesn't do much yet.

New in version 4:
----------------- 

Structures added to map.
 
Units and basic movement is partially implemented. In the space and planet windows right click on the stacks in the maps to select them, then left click on the stack display on the left to select units in a stack. On the planet window with unit(s) selected left click on a map hex to select a destination and calculate a path. With path selected left click on the end point of a path to begin moving units along the path. Click again to stop moving. 

Note: turns not implemented yet so units have infinite movement points. Factions not implemented so stacks can be mixed. If the movement would result in a stack having more than 20 units the move fails. 

Copyright disclaimer
====================

Phoenix is not associated with or authorized by Segasoft Inc. or Holistic Design Inc. Fading Suns is a trademark and copyright of Holistic Design Inc. Emperor of the Fading Suns software product is copyright of Holistic Design Inc. and/or Segasoft Inc.  The mention of or reference to any material in this site, document or material is not a challenge to the trademarks or copyrights concerned.
