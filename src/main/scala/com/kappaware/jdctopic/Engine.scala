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

import com.kappaware.jdctopic.config.Configuration
import kafka.utils.ZkUtils
import org.log4s._

object Engine {
  private val logger = getLogger

  def run(config: Configuration): Unit = {
    val zkUtils = ZkUtils.apply(config.getDescription.zookeeper, 30000, 30000, false)
    val target = new DescriptionWrapper(config.getDescription, config.isRelaxPropertyCheck())
    val current = new DescriptionWrapper(TopicInspector.inspect(zkUtils), true)
    val kafkaAdmin = new KafkaAdmin(zkUtils, true)
    val mutation = MutationBuilder(current, target)
    if (config.isDryRun()) {
      mutation.foreach { x => logger.info(x.display()) }
    } else {
      mutation.foreach { x => x.execute(kafkaAdmin, true) }    // A first pass for validation
      mutation.foreach { x => logger.info(x.display()) }
      mutation.foreach { x => x.execute(kafkaAdmin, false) }
    }
    logger.info(s"jdctopic: ${mutation.size} modification(s)")
    println(s"jdctopic: ${mutation.size} modification(s)")
    zkUtils.close()

  }
}