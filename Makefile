
ifeq ($(shell uname -o),Cygwin)
ROOT_DIR := $(shell cygpath --mixed $(shell dirname $(realpath $(firstword $(MAKEFILE_LIST)))))
else
ROOT_DIR := $(shell dirname $(realpath $(firstword $(MAKEFILE_LIST))))
endif

USER_ID := $(shell id -u)
GROUP_ID := $(shell id -g)

build:
	docker run --rm -ti -v "$(ROOT_DIR)":/usr/src/mymaven -w /usr/src/mymaven -u $(USER_ID):$(GROUP_ID) maven:3.6.3-adoptopenjdk-8 mvn clean install
	docker build . --tag bfblog/spark

run:
	echo "mounting "$(ROOT_DIR)" to /data"
	docker run -v $(ROOT_DIR):/data -ti bfblog/spark

shell:
	echo "mounting "$(ROOT_DIR)" to /data"
	docker run -v $(ROOT_DIR):/data -ti bfblog/spark bash