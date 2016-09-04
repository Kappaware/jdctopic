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

import org.log4s._
import kafka.utils.ZkUtils
import com.kappaware.jdctopic.config.Description
import java.util.ArrayList
import kafka.admin.AdminUtils
import kafka.server.ConfigType
//import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import java.util.HashMap


object TopicInspector {
  private val logger = getLogger

  def inspect(zkUtils: ZkUtils): Description = {
    val description = new Description()
    val allTopics = zkUtils.getAllTopics().sorted
    description.topics = new ArrayList[Description.Topic]
    for (topic <- allTopics) {
      zkUtils.getPartitionAssignmentForTopics(List(topic)).get(topic) match {
        case Some(topicPartitionAssignment) =>
          val dTopic = new Description.Topic()
          dTopic.name = topic
          if (zkUtils.pathExists(ZkUtils.getDeleteTopicPath(topic))) {
            logger.warn(s"Topic ${topic} flagged as deleted!")
          }
          dTopic.properties = AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic, topic)
          dTopic.partitionFactor = topicPartitionAssignment.size
          dTopic.replicationFactor = topicPartitionAssignment.head._2.size

          dTopic.assignments = new HashMap[Integer, java.util.List[Integer]]
          for(p: Int <- topicPartitionAssignment.keys) {
            //var x = topicPartitionAssignment(p).map( i => i:java.lang.Integer).asJava
            //var y = topicPartitionAssignment(p).asInstanceOf[java.util.List[java.lang.Integer]] DOes not works
            dTopic.assignments.put(p.asInstanceOf[java.lang.Integer], topicPartitionAssignment(p).map( i => i:java.lang.Integer).asJava)
          }
          
          description.topics.add(dTopic)
        case None =>
          logger.error("Topic " + topic + " doesn't exist!")
      }
    }
    description
  }

}