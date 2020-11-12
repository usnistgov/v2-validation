FROM maven:3.6.3-openjdk-8

ARG SBT_VERSION=1.3.7

# Install SBT
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt && \
  sbt sbtVersion

# Install dependencies
COPY validation-report-1.0.0.jar /usr/local/share/validation-report-1.0.0.jar
RUN mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile=/usr/local/share/validation-report-1.0.0.jar

COPY xml-util-1.0.1-SNAPSHOT.jar /usr/local/share/xml-util-1.0.1-SNAPSHOT.jar
RUN mvn install:install-file -Dfile=/usr/local/share/xml-util-1.0.1-SNAPSHOT.jar -DgroupId=gov.nist -DartifactId=xml-util -Dversion=1.0.1-SNAPSHOT -Dpackaging=jar -DgeneratePom=true

# Copy source code
COPY . /var/source

# Compile and run tests
WORKDIR /var/source
RUN sbt test

ENV TERM xterm-256color

CMD /bin/bash


