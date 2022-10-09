FROM alpine:3.16.2 AS builder

WORKDIR /tmp

ARG SPARK_VERSION=3.3.0
ARG HADOOP_VERSION=3
ARG DELTA_VERSION=2.1.0
ARG ICEBERG_VERSION=0.14.1
ARG ES_HADOOP_VERSION=8.2.2
ARG CASSANDRA_VERSION=3.2.0
ARG SPARK_SQL_KAFKA_VERSION=3.3.0

SHELL ["/bin/ash", "-eo", "pipefail", "-c"]

RUN apk add --no-cache gnupg=2.2.35-r4 maven\
    && wget https://downloads.apache.org/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz \
    && wget https://downloads.apache.org/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz.asc \
    && wget https://dist.apache.org/repos/dist/dev/spark/KEYS    

# copy prepared files
COPY ./gnupg /root/.gnupg

RUN chmod 700 /root/.gnupg 
RUN chmod 600 /root/.gnupg/*
RUN gpg --list-sigs
RUN gpg --import KEYS
RUN gpg --verify spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz.asc spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz
RUN mkdir spark_runtime

WORKDIR /spark_home
RUN tar xzvf /tmp/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz --strip-components=1 
RUN mvn dependency:copy -Dartifact=io.delta:delta-core_2.12:${DELTA_VERSION} -DoutputDirectory=/spark_home/jars
RUN mvn dependency:copy -Dartifact=io.delta:delta-storage:${DELTA_VERSION} -DoutputDirectory=/spark_home/jars
RUN mvn dependency:copy -Dartifact=org.elasticsearch:elasticsearch-hadoop:${ES_HADOOP_VERSION} -DoutputDirectory=/spark_home/jars
RUN mvn dependency:copy -Dartifact=org.apache.iceberg:iceberg-core:${ICEBERG_VERSION} -DoutputDirectory=/spark_home/jars
RUN mvn dependency:copy -Dartifact=com.datastax.spark:spark-cassandra-connector_2.12:${CASSANDRA_VERSION} -DoutputDirectory=/spark_home/jars
RUN mvn dependency:copy -Dartifact=org.apache.spark:spark-sql-kafka-0-10_2.12:${SPARK_SQL_KAFKA_VERSION} -DoutputDirectory=/spark_home/jars
RUN cd /spark_home/jars && wget https://artifacts.opensearch.org/opensearch-clients/jdbc/opensearch-sql-jdbc-1.1.0.1.jar

FROM openjdk:8-jre-slim 

ARG BUILD_DATE
ARG REVISION
ARG VERSION
ARG IMAGE

LABEL name="${IMAGE}" \
      version="${VERSION}" \
      release="1.0" \
      architecture="x86_64" \
      vendor="markus" \
      maintainer="markus" \
      io.k8s.description="unknown" \
      io.k8s.display-name="unknown" \
      io.openshift.expose-services="1313:http" \
      io.openshift.tags="builder" \
      org.opencontainers.image.created="${BUILD_DATE}" \
      org.opencontainers.image.authors="Markus Breuer" \
      org.opencontainers.image.url="https://github.com/skippi1/docker-stuff" \
      org.opencontainers.image.documentation="bytefusion.de examples" \
      org.opencontainers.image.source="https://github.com/skippi1/docker-stuff" \
      org.opencontainers.image.version="${VERSION}" \
      org.opencontainers.image.revision="${REVISION}" \
      org.opencontainers.image.vendor="n/a" \
      org.opencontainers.image.licenses="n/a" \
      org.opencontainers.image.ref.name="${IMAGE}"

SHELL ["/bin/bash", "-o", "pipefail", "-c"]

HEALTHCHECK --interval=5s --timeout=3s CMD if [ -f /src/public/index.html ] ; then exit 0; else exit 1; fi

# hadolint ignore=DL4005
RUN set -x && sed -i 's/http:/https:/g' /etc/apt/sources.list \
    && apt-get update -y && apt-get upgrade -y  \
    && ln -s /lib /lib64 \
    && DEBIAN_FRONTEND=noninteractive apt-get install --no-install-recommends -y procps=2:3.3.17-5 bash=5.1-2+deb11u1 tini=0.19.0-1 libc6=2.31-13+deb11u4 libpam-modules=1.4.0-9+deb11u1 krb5-user=1.18.3-6+deb11u2 libnss3=2:3.61-1+deb11u2 \
    && rm /bin/sh \
    && ln -sv /bin/bash /bin/sh \
    && echo "auth required pam_wheel.so use_uid" >> /etc/pam.d/su \
    && chgrp root /etc/passwd && chmod ug+rw /etc/passwd \
    && rm -rf /var/cache/apt/* \
    && adduser --uid 202020 --shell /bin/bash --home /home/spark spark

USER spark

WORKDIR /home/spark

COPY --from=builder /spark_home/jars ./jars
COPY --from=builder /spark_home/bin ./bin
COPY --from=builder /spark_home/sbin ./sbin
COPY --from=builder /spark_home/kubernetes/dockerfiles/spark/entrypoint.sh ./
COPY --from=builder /spark_home/examples ./examples
COPY --from=builder /spark_home/kubernetes/tests ./tests
COPY --from=builder /spark_home/data ./data

ENV SPARK_HOME=/home/spark

COPY --from=builder /spark_home .
COPY target/spark-tools*.jar ./jars/
COPY ./spark-tools.scala .

ENTRYPOINT ["/usr/bin/tini", "--"] 

CMD ["/bin/bash", "-c", "/home/spark/bin/spark-shell -I spark-tools.scala --conf 'spark.sql.extensions=io.delta.sql.DeltaSparkSessionExtension' --conf 'spark.sql.catalog.spark_catalog=org.apache.spark.sql.delta.catalog.DeltaCatalog'" ]