help:
	echo -e ' make help - to see this menu \n make run - to build & run project \n make db-init - to init db for services \n run-docker - to create docker image and run container in docker'
run:
	sbt clean
	sbt compile
	sbt run
run-docker:
	docker run -d -p 5000:5000 --restart=always --name registry registry:2
	sbt clean
	sbt compile
	sbt run
	sbt docker:publish
	docker-compose up -d
db-init:
	docker-compose up db &