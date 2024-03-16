
FROM ubuntu:22.04

ENV DEBIAN_FRONTEND=noninteractive
ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64

RUN apt-get update && \
    apt-get install -y software-properties-common && \
    add-apt-repository ppa:deadsnakes/ppa && \
    add-apt-repository ppa:openjdk-r/ppa && \
    apt-get update

RUN apt-get install -y \
	python3.8 \
	openjdk-8-jdk \
	kotlin \
	curl \
	zip \
	unzip
	
# gradle install
RUN curl -s "https://get.sdkman.io" | bash && \
   /bin/bash -c "source /root/.sdkman/bin/sdkman-init.sh && sdk install gradle"

# gradle project
#RUN gradle init --type java-library
#WORKDIR /usr/src/app

#RUN echo "dependencies {" >> build.gradle \
#   && echo "	implementation 'org.xerial:sqlite-jdbc:3.45.2.0'" >> build.gradle \
#   && echo "}" >> build.gradle

 


