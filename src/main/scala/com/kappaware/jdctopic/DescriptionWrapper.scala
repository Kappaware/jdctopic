/*
 * Copyright (C) 2016 BROADSoftware
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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