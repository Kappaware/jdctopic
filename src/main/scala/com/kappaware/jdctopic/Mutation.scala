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


import java.util.Properties
import scala.collection.JavaConversions._
import org.I0Itec.zkclient.ZkClient
import kafka.admin.AdminUtils
import com.kappaware.jdctopic.config.Description
import com.kappaware.jdctopic.config.ConfigurationException

/**
 * @author Serge ALEXANDRE
 */
object Mutation {

  abstract class Mutation(topic: String) {
    // This sort is mainly intended to be able to compare two Operation set. 
    val sortKey = topic + "_" + getClass.getName

    def execute(kafkaAdmin: KafkaAdmin, onlyCheck: Boolean)

    def display(): String = {
      toString()
    }
  }

  case class CreateTopic(topicConfig: Description.Topic) extends Mutation(topicConfig.name) {
    def execute(kafkaAdmin: KafkaAdmin, onlyCheck: Boolean): Unit = {
      if (!onlyCheck) {
        kafkaAdmin.createTopic(topicConfig.name, topicConfig.partitionFactor, topicConfig.replicationFactor, topicConfig.properties)
      }
    }
    override def display(): String = {
      s"==> Will create a new topic:\n${Misc.indent("    ", topicConfig.toString())}"
    }
  }

  case class DeleteTopic(topic: String) extends Mutation(topic) {
    def execute(kafkaAdmin: KafkaAdmin, onlyCheck: Boolean) = {
      if (kafkaAdmin.destructive) {
        if (!onlyCheck) {
          kafkaAdmin.deleteTopic(topic)
        }
      } else {
        throw new ConfigurationException(s"Unable to delete topic ${topic}. Activate 'destructive' mode")
      }
    }
    override def display(): String = {
      s"==> Will delete topic '${topic}'"
    }
  }

  case class ChangeReplicationFactor(topic: String, oldValue: Int, newValue: Int) extends Mutation(topic) {
    def execute(kafkaAdmin: KafkaAdmin, onlyCheck: Boolean) = {
      throw new ConfigurationException(s"Unable to change replication factor on topic '${topic}':  ${oldValue} -> ${newValue}. NOT IMPLEMENTED")
    }
    override def display(): String = {
      s"==> Unable to change replication factor on topic '${topic}':  ${oldValue} -> ${newValue}. NOT IMPLEMENTED"
    }
  }

  case class ChangePartitionFactor(topic: String, oldValue: Int, newValue: Int) extends Mutation(topic) {
    def execute(kafkaAdmin: KafkaAdmin, onlyCheck: Boolean) = {
      if (newValue < oldValue) {
        throw new ConfigurationException(s"Unable to change partition factor on topic '${topic}':  ${oldValue} -> ${newValue}. CAN'T DECREASE THIS VALUE")
      } else {
        if (!onlyCheck) {
          kafkaAdmin.increasePartition(topic, newValue)
        }
      }
    }
    override def display(): String = {
      if (newValue < oldValue) {
        s"==> Unable to change partition factor on topic '${topic}':  ${oldValue} -> ${newValue}. CAN'T DECREASE THIS VALUE"
      } else {
        s"==> Will change partition factor on topic '${topic}':  ${oldValue} -> ${newValue}"
      }
    }
  }

  case class ChangeProperties(topic: String, toAddOrUpdate: Properties, toRemove: Seq[String]) extends Mutation(topic) {
    def execute(kafkaAdmin: KafkaAdmin, onlyCheck: Boolean) = {
      if (!onlyCheck) {
        val configs = kafkaAdmin.fetchTopiConfig(topic)
        configs.putAll(toAddOrUpdate)
        toRemove.foreach(config => configs.remove(config))
        kafkaAdmin.changeTopiConfig(topic, configs)
      }
    }
    override def display(): String = {
      var str = s"==> Will change properties on topic '${topic}':\n"
      if (toAddOrUpdate.size > 0) {
        str += "    Add:\n"
        toAddOrUpdate.keys.foreach { k => str += s"      ${k}: ${toAddOrUpdate.get(k)}\n" }
      }
      if (toRemove.size > 0) {
        str += "    Remove:\n"
        toRemove.foreach { k => str += s"      ${k}\n" }
      }
      str
    }
  }

}