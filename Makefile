all:
	mvn install

javadoc:
	mvn javadoc:javadoc -Dlinksource=true
	@echo "Developer reminder: Copy the contents of target/site/apidocs to the gh-pages branch to update the online Javadocs."

jacoco:
	mvn org.jacoco:jacoco-maven-plugin:prepare-agent test org.jacoco:jacoco-maven-plugin:report
	@echo "Developer note: firefox target/site/jacoco/index.html"

clean:
	mvn clean

install:
	mkdir -p $(to)
	cp -f README.md $(to)/ClaferChocoIG-README.md
	cp -f target/claferchocoig-0.4.2-jar-with-dependencies.jar $(to)/claferchocoig.jar

snapshotInstall:
	mkdir -p $(to)
	cp -f README.md $(to)/ClaferChocoIG-README.md
	mv -f target/claferchocoig-0.4.2-SNAPSHOT-jar-with-dependencies.jar $(to)/claferchocoig.jar
