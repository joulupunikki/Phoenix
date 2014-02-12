Phoenix
=======

Java clone/remake/patch of the game [Emperor of the Fading Suns (EFS)](http://en.wikipedia.org/wiki/Emperor_of_the_Fading_Suns). Uses original EFS data files and requires EFS 1.4 to be installed. 

Contributing
============

Currently contributing is not possible as pull requests will not be accepted. Sorry.

Usage
=====

Phoenix.jar intended to replace EFS.EXE. Place into your EFS directory where EFS.EXE resides. Java 7 jdk or jre is needed to run Phoenix.jar.

Important note:
Due to the large size of the uncompressed save files (10MB) and the fact that java's saving process (serialization) is a recursive function the game will likely choke up (stack overflow) during loading and saving with the default stack size. The default stack size thus propably needs to be increased. On windows this is done automatically by clicking on `Phoenix.bat` instead of `Phoenix.jar` or from the command line issue eg. `java -Xss32m -jar Phoenix.jar`. For 1280x1024 window click on `Phoenix1280x1024.bat`.

Binary distribution
-------------------
Copy `Phoenix.jar`,`Phoenix.bat` and `Phoenix1280x1024.bat` to your EFS directory where `EFS.EXE` resides. To start double click on the `Phoenix.bat` or if you use commandline for a 640x480 window type `java -Xss32m -jar Phoenix.jar`. For a 1280x1024 window type `java -Xss32m -jar Phoenix.jar 2 GALAXY.GAL` or double click on `Phoenix1280x1024.bat`.

If you get an error saying java not found then likely java is not in the path and you need either to put java into the path or use absolute path name. Eg. on windows if your java jdk is installed into 
`C:\Program Files\Java\jdk1.7.0` you would type `"C:\Program Files\Java\jdk1.7.0\bin\java.exe" -jar Phoenix.jar 1 GALAXY.GAL`.

Source distribution
-------------------

Unzip the package then go to `Phoenix/src`,
- on Windows click on `build.bat` to build `Phoenix.jar` or type `javac phoenix\Phoenix.java` followed by `jar cfm Phoenix.jar manifest.txt @classes.list`

- on Linux/Unix type `javac phoenix/Phoenix.java` followed by `jar cfm Phoenix.jar manifest.txt */*.class`. 

Copy `Phoenix.jar`,`Phoenix.bat` and `Phoenix1280x1024.bat` to your EFS directory where `EFS.EXE` resides. To run the game follow the instructions for the binary distribution.

Changes
=======

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
