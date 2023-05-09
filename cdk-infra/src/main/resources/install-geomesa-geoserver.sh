#!/bin/bash

#https://www.geomesa.org/documentation/stable/user/filesystem/install.html#installing-geomesa-filesystem-in-geoserver
set -x

GEOSERVER_VERSION="2.17.3"
GEOMESA_VERSION="3.5.0"

GEOSERVER_URL=https://sourceforge.net/projects/geoserver/files/GeoServer
GEOMESA_URL=https://github.com/locationtech/geomesa/releases/download

cd ${HOME}

sudo yum install -y java-11-amazon-corretto

export JAVA_HOME=/usr/lib/jvm/java-11-amazon-corretto.x86_64/

export GEOSERVER_HOME=${HOME}/geoserver-${GEOSERVER_VERSION}

wget ${GEOSERVER_URL}/${GEOSERVER_VERSION}/geoserver-${GEOSERVER_VERSION}-bin.zip

wget ${GEOMESA_URL}/geomesa-${GEOMESA_VERSION}/geomesa-fs_2.12-${GEOMESA_VERSION}-bin.tar.gz

unzip geoserver-${GEOSERVER_VERSION}-bin.zip -d geoserver-${GEOSERVER_VERSION}

tar xvf geomesa-fs_2.12-${GEOMESA_VERSION}-bin.tar.gz

tar -xzvf \
  geomesa-fs_2.12-${GEOMESA_VERSION}/dist/gs-plugins/geomesa-fs-gs-plugin_2.12-${GEOMESA_VERSION}-install.tar.gz \
  -C geoserver-${GEOSERVER_VERSION}/webapps/geoserver/WEB-INF/lib

echo "Y" | sh geomesa-fs_2.12-${GEOMESA_VERSION}/bin/install-dependencies.sh --no-prompt \
  geoserver-${GEOSERVER_VERSION}/webapps/geoserver/WEB-INF/lib

JAVA_OPTS=-Xmx8g geoserver-${GEOSERVER_VERSION}/bin/startup.sh &