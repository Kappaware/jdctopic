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
import com.kappaware.jdctopic.config.ConfigurationException
import com.kappaware.jdctopic.config.Configuration
import com.kappaware.jdctopic.config.ConfigurationImpl
import com.kappaware.jdctopic.config.Parameters

object Main {
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