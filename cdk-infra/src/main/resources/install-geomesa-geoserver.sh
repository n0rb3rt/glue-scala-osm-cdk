#https://www.geomesa.org/documentation/stable/user/filesystem/install.html#installing-geomesa-filesystem-in-geoserver

set -x

GEOSERVER_VERSION="2.17.3"
GEOMESA_VERSION="3.5.0"

GEOSERVER_URL=https://sourceforge.net/projects/geoserver/files/GeoServer
GEOMESA_URL=https://github.com/locationtech/geomesa/releases/download

GEOSERVER_HOME=geoserver-${GEOSERVER_VERSION}
GEOSERVER_WEBINF=${GEOSERVER_HOME}/webapps/geoserver/WEB-INF
GEOMESA_HOME=geomesa-fs_2.12-${GEOMESA_VERSION}

cd ${HOME}

sudo yum install -y java-11-amazon-corretto

wget ${GEOSERVER_URL}/${GEOSERVER_VERSION}/${GEOSERVER_HOME}-bin.zip

wget ${GEOMESA_URL}/geomesa-${GEOMESA_VERSION}/${GEOMESA_HOME}-bin.tar.gz

unzip ${GEOSERVER_HOME}-bin.zip -d ${GEOSERVER_HOME}

tar xvf ${GEOMESA_HOME}-bin.tar.gz

tar xzvf ${GEOMESA_HOME}/dist/gs-plugins/geomesa-fs-gs-plugin_2.12-${GEOMESA_VERSION}-install.tar.gz \
  -C ${GEOSERVER_WEBINF}/lib

echo "Y" | sh ${GEOMESA_HOME}/bin/install-dependencies.sh ${GEOSERVER_WEBINF}/lib

rm ${GEOSERVER_WEBINF}/lib/jackson-core-2.10.1.jar

cat << EOF > ${GEOSERVER_WEBINF}/classes/core-site.xml
<?xml version="1.0"?>
<configuration>
<property>
  <name>fs.s3a.aws.credentials.provider</name>
  <value>com.amazonaws.auth.InstanceProfileCredentialsProvider</value>
</property>
</configuration>
EOF

GEOSERVER_HOME=${HOME}/geoserver-${GEOSERVER_VERSION} \
  JAVA_HOME=/usr/lib/jvm/java-11-amazon-corretto.x86_64 \
  JAVA_OPTS=-Xmx8g \
  geoserver-${GEOSERVER_VERSION}/bin/startup.sh &