cp Phoenix/per-developer-pom.xml.STATIC Phoenix/per-developer-pom.xml
cd math
mvn clean install -DskipTests
cd ..
cd Phoenix
mvn clean install -DskipTests
mv target/Phoenix.jar ../..
