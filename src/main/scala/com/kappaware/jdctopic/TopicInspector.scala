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
            ???
          }
          dTopic.properties = AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic, topic)
          val sortedPartitions = topicPartitionAssignment.toList.sortWith((m1, m2) => m1._1 < m2._1)
          dTopic.partitionFactor = topicPartitionAssignment.size
          dTopic.replicationFactor = topicPartitionAssignment.head._2.size
          description.topics.add(dTopic)
        case None =>
          logger.error("Topic " + topic + " doesn't exist!")
      }
    }
    description
  }

}