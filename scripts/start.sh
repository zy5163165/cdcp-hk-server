#!/bin/sh


runweb() {
 echo "Starting WEB"

 if [ -z "$TOMCAT_HOME" ]
 then
	TOMCAT_HOME=../web/apache-tomcat-7.0.26
    #echo "please set the TOMCAT_HOME environment variable"
    #exit -1;
 fi

 bash ${TOMCAT_HOME}/bin/catalina.sh run > web.out &
 echo "$!" > web.id
 echo "waiting..."
 tail -f -n 100 web.out | while read LINE
 do 
    echo  $LINE '@'
    echo $LINE | grep "INFO: Server startup in"
    if  [ $? -eq 0 ]
    then
      echo "CDCP web start success"
    fi
 done

}


if [[ -z "$JAVA_HOME" ]]
then 
    echo "Please set the JAVA_HOME environment variable"
    exit
fi

JAVA=${JAVA_HOME}/bin/java
LIB=./lib
EXTLIB=${LIB}/ext
PATCH1=../patch/patch1
PATCH2=../patch/patch2
PATCH3=../patch/patch3
PATCH4=../patch/patch4
PATCH5=../patch/patch5


# set the classpath
CP=$(echo ${PATCH5}/*.jar ${PATCH5}/*.zip ${PATCH4}/*.jar ${PATCH4}/*.zip ${PATCH3}/*.jar ${PATCH3}/*.zip ${PATCH2}/*.jar ${PATCH2}/*.zip ${PATCH1}/*.jar ${PATCH1}/*.zip ${LIB}/*.jar ${LIB}/*.zip ${EXTLIB}/*.jar ${EXTLIB}/*.zip | tr ' ' ':')
CP=${CP}:./properties:./resources:./Fonts:..

#======================================
echo  ${CP}
#======================================

${JAVA} -cp ${CP} -DAdminConfigFile=hippo_cdcp.xml -Dtag=cdcp_build com.alcatelsbell.hippo.framework.admin.Main datadomain s1  & > server.out
echo "$!" > admin.id
sleep 3
tail -f  -n 1 server.out | while read LINE 
do
  echo $LINE  "@"
  echo $LINE | grep -q "Admin Server has started completely!"
  if  [ $? -eq 0 ]
  then
    runweb
    break
  fi
done