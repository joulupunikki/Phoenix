Phoenix
=======

Java clone/remake/patch of the game [Emperor of the Fading Suns (EFS)](http://en.wikipedia.org/wiki/Emperor_of_the_Fading_Suns). Uses original EFS data files and requires EFS 1.4 to be installed. Should work with all mods.

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
Copy `Phoenix.jar`,`Phoenix.bat` and `Phoenix1280x1024.bat` and `PHOENIX` directory to your EFS directory where `EFS.EXE` resides. To start double click on the `Phoenix.bat` or if you use commandline for a 640x480 window type `java -Xss32m -jar Phoenix.jar`. For a 1280x1024 window type `java -Xss32m -jar Phoenix.jar 2 GALAXY.GAL` or double click on `Phoenix1280x1024.bat`.

If you get an error saying java not found then likely java is not in the path and you need either to put java into the path or use absolute path name. Eg. on windows if your java jdk is installed into 
`C:\Program Files\Java\jdk1.7.0` you would type `"C:\Program Files\Java\jdk1.7.0\bin\java.exe" -jar Phoenix.jar 1 GALAXY.GAL`.

Source distribution
-------------------

Unzip the package then go to the `etc` directory and copy the contents to your EFS directory where `EFS.EXE` resides.

Then go to the `src` directory,
- on Windows click on `build.bat` to build `Phoenix.jar` or type `javac phoenix\Phoenix.java` followed by `jar cfm Phoenix.jar manifest.txt @classes.list`

- on Linux/Unix type `javac phoenix/Phoenix.java` followed by `jar cfm Phoenix.jar manifest.txt */*.class`. 

Copy `Phoenix.jar`,`Phoenix.bat` and `Phoenix1280x1024.bat` to your EFS directory where `EFS.EXE` resides. To run the game follow the instructions for the binary distribution.

Changes
=======

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

There is a bug in Hyperion 1.4g which prevents Phoenix from loading DAT/UNITSPOT.DAT. In UNITSPOT.DAT line 67 reads `     "desert"  "2.0 1.0 2.2.0.0 1.0 0.5 2.0 0.5 1.0 1.0"` when it should be `     "desert"  "2.0 1.0 2.0 2.0 1.0 0.5 2.0 0.5 1.0 1.0"` ie there is a dot between the Trd and Air columns which makes Phoenix choke. Correct this and Hyperion should start.

Also in Hyperion 1.4g remember to replace UNIT.DAT with UNIT3.DAT to enable building of advanced units. This must be done before a game is started since Phoenix loads datafiles only once during program start.

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
