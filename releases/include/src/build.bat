javac "@srclist.txt"
jar cfm Phoenix.jar manifest.mf "@classlist.txt"
move Phoenix.jar ..
