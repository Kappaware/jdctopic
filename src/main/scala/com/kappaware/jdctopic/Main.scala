package com.kappaware.jdctopic

import org.log4s._
import com.kappaware.jdctopic.config.ConfigurationException
import com.kappaware.jdctopic.config.Configuration
import com.kappaware.jdctopic.config.ConfigurationImpl
import com.kappaware.jdctopic.config.Parameters

class Main {
  private val logger = getLogger

  def main(args: Array[String]): Unit = {
    try {
      val config: Configuration = new ConfigurationImpl(new Parameters(args))
      Engine.run(config)
      System.exit(0)
    } catch {
      case ce: ConfigurationException =>
        logger.error(ce.getMessage)
        System.exit(1)
      case e: Exception =>
        logger.error(e)("ERROR")
        System.exit(3)
    }
  }
}