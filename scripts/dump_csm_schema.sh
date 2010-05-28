#!/bin/bash

#set -o verbose

USAGE='
Usage: `basename $0` [OPTIONS]

OPTIONS
  -e TABLE    Exclude table with name TABLE. This option can be used multiple
              times to exclude multiple tables.
  -d DBNAME   The database name.
  -h DBHOST   The hostname of the machine running the MySQL server.
  -u DBUSER   The user to use on the MySQL server.
  -p PWD      The password to use on the MySQL server.
  -o FILE     The file to save the dump file to.
'

MYSQL=/usr/bin/mysql
MYSQLDUMP=/usr/bin/mysqldump
SED=/bin/sed
HOST="localhost"
DBNAME=biobank2

function in_array () {
    haystack=( "$@" )
    haystack_size=( "${#haystack[@]}" )
    needle=${haystack[$((${haystack_size}-1))]}
    for ((i=0;i<$(($haystack_size-1));i++)); do
        h=${haystack[${i}]};
        [ $h = $needle ] && return 1
    done
    return 0
}


while getopts "e:d:h:u:p:o:" OPTION
do
  case $OPTION in
        e) exclude=(${exclude[@]} $OPTARG );;
        d) DBNAME=$OPTARG;;
        h) DBHOST=$OPTARG;;
        u) DBUSER=$OPTARG;;
        p) DBPWD=$OPTARG;;
        o) OUTFILE=$OPTARG
  esac
done

if [ -z "$DBUSER" ]; then
    echo "ERROR: user not specified"
    echo "$USAGE"
    exit
fi

if [ -z "$DBPWD" ]; then
    echo "ERROR: password not specified"
    echo "$USAGE"
    exit
fi

for table in `$MYSQL -h$DBHOST -u$DBUSER -p$DBPWD $DBNAME -BNe "show tables"` ; do
#    echo "\"$table\""
    if [[ $table =~ ^csm_ ]]; then
        CSM_TABLES=(${CSM_TABLES[@]} $table )
    fi
done

if [ "${#CSM_TABLES[@]}" == "0" ]; then
    echo "ERROR: database does not contain any CSM tables"
fi

echo "" > csm_info.sql

for table in "${CSM_TABLES[@]}"
do
    in_array "${exclude[@]}" $table
    if [ $? -eq 0 ]; then
        if [ -z "$OUTFILE" ]; then
            $MYSQLDUMP -h$DBHOST -u$DBUSER -p$DBPWD $DBNAME $table
        else
            $MYSQLDUMP -h$DBHOST -u$DBUSER -p$DBPWD $DBNAME $table >> $OUTFILE
        fi
    fi
done
