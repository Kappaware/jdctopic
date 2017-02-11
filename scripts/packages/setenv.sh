
# Set JAVA_HOME. Must be at least 1.7.
# If not set, will try to lookup a correct version.
# JAVA_HOME=/some/place/where/to/find/java

# You may have to edit this line to adjust to your environement
# The launching script will try to use one of these folder to find the Kafka jars.
CANDIDATE_KLIBS="/usr/share/java/kafka /usr/hdp/current/kafka-broker/libs"


# Set the log configuration file
JOPTS="$JOPTS -Dlog4j.configuration=file:/etc/jdctopic/log4j.xml"


# Set zookeeper quorum
# OPTS="$OPTS --zookeeper 'zk1:2181,zk2:2181,zk3:2181'"



