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
      mutation.foreach { x => println(x.display()) }
    } else {
      mutation.foreach { x => x.execute(kafkaAdmin, true) }
      mutation.foreach { x => x.execute(kafkaAdmin, false) }
    }
    println(s"jdctopic: ${mutation.size} modification(s)")
    zkUtils.close()

  }
}