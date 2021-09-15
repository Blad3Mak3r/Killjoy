build:
	gradlew clean shadowJar

docker:
	cd docker && docker-compose -p "killjoy-dev-environment" -f development.yml up -d