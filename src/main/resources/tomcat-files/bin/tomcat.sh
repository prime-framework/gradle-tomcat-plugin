#!/bin/bash

# This script runs tomcat from a CATALINA_BASE and must
# be executed from the build/tomcat/bin directory

##
# Verify CATALINA_HOME is set
##
if [ -z "$CATALINA_HOME" ]; then
  echo "Please setup the CATALINA_HOME variable"
  exit 1
fi

##
# Calculate the bin directory name
##
BIN_DIR=$(dirname $0)
cd $BIN_DIR
BIN_DIR=$PWD

##
# Set the project root directory
##
cd ../../../
PROJECT_ROOT=$PWD

##
# set the CATALINA_BASE so we can run outside of the tomcat install
##
cd build/tomcat
export CATALINA_BASE=$PWD

##
# Set the webapp root.
##
WEBAPP_ROOT=$PROJECT_ROOT/@WEBAPP_ROOT@


##
# Set the java opts environment variable
##
export JAVA_OPTS="-Drundir=$WEBAPP_ROOT -Xmx@MAX_MEMORY@ -Djava.awt.headless=true"

##
# Run the catalina home's catalina.sh
##
echo "Starting Tomcat from $CATALINA_BASE"
echo "Starting webapp from $WEBAPP_ROOT"
$CATALINA_HOME/bin/catalina.sh $@
