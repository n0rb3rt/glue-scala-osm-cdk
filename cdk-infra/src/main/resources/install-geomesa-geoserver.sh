##!/bin/bash
set -x

#https://www.geomesa.org/documentation/stable/user/filesystem/install.html#installing-geomesa-filesystem-in-geoserver

GEOSERVER_VERSION="2.22.2"
GEOMESA_VERSION="4.0.1"

GEOSERVER_URL=https://sourceforge.net/projects/geoserver/files/GeoServer
GEOMESA_URL=https://github.com/locationtech/geomesa/releases/download

GEOSERVER_PATH=geoserver-${GEOSERVER_VERSION}
GEOMESA_PATH=geomesa-fs_2.12-${GEOMESA_VERSION}
GEOSERVER_WEBINF=${GEOSERVER_PATH}/webapps/geoserver/WEB-INF

cd ${HOME}

sudo yum install -y java-11-amazon-corretto

wget ${GEOSERVER_URL}/${GEOSERVER_VERSION}/${GEOSERVER_PATH}-bin.zip

wget ${GEOMESA_URL}/geomesa-${GEOMESA_VERSION}/${GEOMESA_PATH}-bin.tar.gz

unzip ${GEOSERVER_PATH}-bin.zip -d ${GEOSERVER_PATH}

tar xvf ${GEOMESA_PATH}-bin.tar.gz

tar xvf ${GEOMESA_PATH}/dist/gs-plugins/geomesa-fs-gs-plugin_2.12-${GEOMESA_VERSION}-install.tar.gz \
  -C ${GEOSERVER_WEBINF}/lib

echo "Y" | sh ${GEOMESA_PATH}/bin/install-dependencies.sh ${GEOSERVER_WEBINF}/lib

cat << EOF > ${GEOSERVER_WEBINF}/classes/core-site.xml
<?xml version="1.0"?>
<configuration>
  <property>
    <name>fs.s3a.aws.credentials.provider</name>
    <value>com.amazonaws.auth.InstanceProfileCredentialsProvider</value>
  </property>
  <property>
    <name>fs.AbstractFileSystem.s3.impl</name>
    <value>org.apache.hadoop.fs.s3a.S3A</value>
  </property>
</configuration>
EOF

GEOSERVER_HOME=${HOME}/${GEOSERVER_PATH} \
  JAVA_HOME=/usr/lib/jvm/java-11-amazon-corretto.x86_64 \
  JAVA_OPTS=-Xmx8g \
  geoserver-${GEOSERVER_VERSION}/bin/startup.sh &