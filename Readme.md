- Guide

docker run --name mysql-container -e MYSQL_ROOT_PASSWORD=password -e MYSQL_PASSWORD=password -e MYSQL_DATABASE=analysis -e MYSQL_AUTHENTICATION_PLUGIN=mysql_native_password -p 3306:3306 -d mysql:8.4.2