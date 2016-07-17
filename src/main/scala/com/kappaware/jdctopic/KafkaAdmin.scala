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

/**
 * @author Serge ALEXANDRE
 */
case class KafkaAdmin(zkUtils: ZkUtils, destructive: Boolean) extends Logging {

  def createTopic(name: String, partitionFactor: Int, replicationFactor: Int, properties: Properties = new Properties()) = {
    AdminUtils.createTopic(zkUtils, name, partitionFactor, replicationFactor, properties)
  }

  def fetchTopiConfig(topic: String): Properties = {
    AdminUtils.fetchEntityConfig(zkUtils, "topics",  topic)
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