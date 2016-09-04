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

import kafka.utils.ZKStringSerializer
import org.I0Itec.zkclient.ZkClient
import java.util.Properties
import kafka.admin.AdminUtils
import kafka.utils.ZkUtils
import org.I0Itec.zkclient.exception.ZkNodeExistsException
import kafka.admin.AdminOperationException
import kafka.utils.Logging
import scala.collection.JavaConverters._
import com.kappaware.jdctopic.config.ConfigurationException

/**
 * @author Serge ALEXANDRE
 */
case class KafkaAdmin(zkUtils: ZkUtils, destructive: Boolean) extends Logging {

  def createTopic(name: String, partitionFactor: Int, replicationFactor: Int, jassignment: java.util.Map[Integer, java.util.List[Integer]], properties: Properties = new Properties()) = {
    if (jassignment == null) {
      AdminUtils.createTopic(zkUtils, name, partitionFactor, replicationFactor, properties)
    } else {
      val partitionReplicaAssignment = jassignment.asScala.map(x => (x._1.toInt, x._2.asScala.toSeq.map(_.toInt)))

      val brokerIds = zkUtils.getChildrenParentMayNotExist(ZkUtils.BrokerIdsPath).map(_.toInt).toSet
      partitionReplicaAssignment.values.foreach { brokerList =>
        brokerList.foreach { x =>
          if(!brokerIds.contains(x)) {
              throw new ConfigurationException(s"Topic '${name}': '${x}' is not a valid broker ID")
          }
        }
      }

      AdminUtils.createOrUpdateTopicPartitionAssignmentPathInZK(zkUtils, name, partitionReplicaAssignment, properties)
    }
  }

  def fetchTopiConfig(topic: String): Properties = {
    AdminUtils.fetchEntityConfig(zkUtils, "topics", topic)
  }

  def changeTopiConfig(topic: String, config: Properties): Unit = {
    AdminUtils.changeTopicConfig(zkUtils, topic, config)
  }

  def increasePartition(topic: String, partitionFactor: Int): Unit = {
    val configs = AdminUtils.fetchEntityConfig(zkUtils, "topics", topic)
    AdminUtils.addPartitions(zkUtils, topic, partitionFactor)
  }

  def deleteTopic(topic: String): Unit = {
    if (this.destructive) {
      try {
        zkUtils.createPersistentPath(ZkUtils.getDeleteTopicPath(topic))
        info("Topic %s is marked for deletion.".format(topic))
        info("Note: This will have no impact if delete.topic.enable is not set to true.")
      } catch {
        case e: ZkNodeExistsException =>
          warn("Topic %s is already marked for deletion.".format(topic))
        case e2: Throwable =>
          throw new AdminOperationException("Error while deleting topic %s".format(topic))
      }
    } else {
      warn(s"A request as been performed to delete topic ${topic}, but destructive mode has not being enabled")
    }
  }

}