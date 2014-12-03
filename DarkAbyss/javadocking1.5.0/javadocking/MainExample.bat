::-----------------------------------------------------------------------
:: WorkspaceExample Startup Script
::-----------------------------------------------------------------------


:: ----------------------------------------------------------------------
:: Construct the classpath
:: ----------------------------------------------------------------------

set CLASSPATH=.;javadocking.jar;samples.jar;lib/tinylaf.jar;lib/liquidlnf.jar;lib/nimrodlf.jar;lib/substance-lite.jar;lib/jfreechart.jar;lib/jcommon.jar


:: ----------------------------------------------------------------------
:: Add the application arguments.
:: ----------------------------------------------------------------------

set APP_ARGS=%1 %2 %3 %4 %5 %6 %7 %8 %9

:: ----------------------------------------------------------------------
:: Start the application.
:: ----------------------------------------------------------------------
"%JAVA_HOME%\bin\java" -classpath %CLASSPATH% com.javadocking.mainexample.MainExample %APP_ARGS%
