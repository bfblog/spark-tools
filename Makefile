
ifeq ($(shell uname -o),Cygwin)
ROOT_DIR := $(shell cygpath --mixed $(shell dirname $(realpath $(firstword $(MAKEFILE_LIST)))))
else
ROOT_DIR := $(shell dirname $(realpath $(firstword $(MAKEFILE_LIST))))
endif

build:
	docker build . --tag bfblog/spark

run:
	echo "mounting "$(ROOT_DIR)" to /data"
	docker run -v $(ROOT_DIR):/data -ti bfblog/spark

shell:
	echo "mounting "$(ROOT_DIR)" to /data"
	docker run -v $(ROOT_DIR):/data -ti bfblog/spark bash