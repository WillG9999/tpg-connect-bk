app_path =/app
cd $app_path
jar_file=`ls $app_path/*.jar`
echo "Enviroment profile--> $PROFILE"
java -Dspring.profiles.active=$PROFILE -jar $jar_file