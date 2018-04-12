

killweb(){

  if [ ! -f web.id ] ; then
        echo  CDCP web is not running yet.
        exit 0
  fi

  kill `cat web.id`

  if [ $? -ne 0 ] ;  then
        exit 0
  fi

  echo 'CDCP Web Killed!'

  rm web.id

}


if [ ! -f admin.id ] ; then
	echo  CDCP Server is not running yet.
        killweb
	exit 0
fi

kill `cat admin.id`

if [ $? -ne 0 ] ;  then
	exit 0
fi

echo 'CDCP Server Killed! '

rm admin.id

killweb
