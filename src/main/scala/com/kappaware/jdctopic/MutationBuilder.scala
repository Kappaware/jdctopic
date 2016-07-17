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


import scala.collection.JavaConversions._
import java.util.Properties
import com.kappaware.jdctopic.config.Description

/**
 * @author Serge ALEXANDRE
 */
object MutationBuilder {
  import Mutation._

  def apply(initialState: DescriptionWrapper, targetState: DescriptionWrapper): Seq[Mutation] = {

    var result = collection.mutable.Buffer[Mutation]()
    // Compute the new topic to be created
    result ++= targetState.topicByName.filter( (t) => t._2.state == Description.State.present).keys.toSet.diff(initialState.topicByName.keys.toSet).map(topic => new CreateTopic(targetState.topicByName(topic)))
    // Compute the topic to be deleted
    result ++= targetState.topicByName.filter( (t) => t._2.state == Description.State.absent).keys.toSet.intersect(initialState.topicByName.keys.toSet).map(topic => new DeleteTopic(topic))

    initialState.topicByName.keys.toSet.intersect(targetState.topicByName.keys.toSet).foreach { topic =>
      val initialTopic = initialState.topicByName(topic)
      val targetTopic = targetState.topicByName(topic)
      if (initialTopic.partitionFactor != targetTopic.partitionFactor) {
        result += new ChangePartitionFactor(topic, initialTopic.partitionFactor, targetTopic.partitionFactor)
      }
      if (initialTopic.replicationFactor != targetTopic.replicationFactor) {
        result += new ChangeReplicationFactor(topic, initialTopic.replicationFactor, targetTopic.replicationFactor)
      }
      val propertiesToRemove = initialTopic.properties.keys.toSeq.diff(targetTopic.properties.keys.toSeq)
      val propertiesToAdd = new Properties()
      targetTopic.properties.foreach { prop => 
        val v = initialTopic.properties.get(prop._1)
        if(v == null || !v.equals(targetTopic.properties.get(prop._1))) {
          propertiesToAdd += prop
        }
      }
      if (propertiesToRemove.size > 0 || propertiesToAdd.size > 0) {
        result += new ChangeProperties(topic, propertiesToAdd, propertiesToRemove.asInstanceOf[Seq[String]])
      }
    }
    result.sortWith(_.sortKey < _.sortKey)
  }

}