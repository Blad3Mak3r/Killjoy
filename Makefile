build:
	gradlew clean shadowJar

docker:
	docker build . -t blademaker/killjoy:latest