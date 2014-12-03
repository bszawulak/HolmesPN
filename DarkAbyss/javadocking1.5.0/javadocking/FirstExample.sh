#!/bin/bash
#-----------------------------------------------------------------------
# FirstExample Startup Script
#-----------------------------------------------------------------------

#Change the current dir to the location of the script
cd `dirname $0`

# The main class to load
MAIN_CLASS="com.javadocking.firstexample.FirstExample"

# The base classpath
CLASSPATH=".:javadocking.jar:samples.jar"

# The startup script will search for jar and zip files in each of the 
# directories specified in this variable. Directories must be seperated 
# with a space
CLASSPATH_DIRECTORIES=". lib"


# The path of the JRE. This is only used if $JAVA_HOME is not defined
JRE_PATH="/usr/java"

# Arguments to pass to the JVM
JVM_ARGS="-Xmx256m -Duser.language=en -Duser.region=US"

# Arguments to pass to the application
APP_ARGS="$@"

JVM_ARGS="$JVM_ARGS"

# ----------------------------------------------------------------------
# Before you can run DockGallery, please, specify the location of the 
# Java Runtime Environment (JRE) you wish to use.
# The JRE you specify here will be used to start the application.
# If this environment variable is already set on your system the
# value you enter here will be ignored.
# ----------------------------------------------------------------------

if [ -z $JAVA_HOME ]; then
  JAVA_HOME="$JRE_PATH"
fi

JAVA_EXE="$JAVA_HOME/bin/java"
if [ ! -f $JAVA_EXE ]; then
  echo "---------------------------------------------------------------------"
  echo " ERROR: cannot start application."
  echo " JAVA_HOME is either not set or not set to a valid jre path."
  echo " Please set JAVA_HOME to a valid jre path or define the"
  echo " JRE_PATH variable in this shell script."
  echo "---------------------------------------------------------------------"
  exit 1
fi

$JAVA_EXE $JVM_ARGS -classpath "$CLASSPATH" $MAIN_CLASS $APP_ARGS 
