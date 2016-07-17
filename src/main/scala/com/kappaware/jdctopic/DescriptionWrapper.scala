package com.kappaware.jdctopic

import com.kappaware.jdctopic.config.Description
import scala.collection.JavaConversions._
import com.kappaware.jdctopic.config.ConfigurationException
import kafka.log.LogConfig

case class DescriptionWrapper(description: Description, relaxPropertiesCheck: Boolean) {

  val topicByName = collection.mutable.Map[String, Description.Topic]()
  description.topics.foreach { topic =>
    if (topicByName.containsKey(topic.name)) {
      throw new ConfigurationException(s"Topic name ${topic.name} is defined twice!")
    }
    // Test all properties are correctly spelled
    if (!relaxPropertiesCheck) {
      LogConfig.validate(topic.properties)
    }
    topicByName += topic.name -> topic
  }
}