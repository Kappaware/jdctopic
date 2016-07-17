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
package com.kappaware.jdctopic.config;

import java.util.ArrayList;
import java.util.Properties;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.kappaware.jdctopic.Misc;

public class Description {
	public enum State {
		present, absent
	}

	public String zookeeper;
	public ArrayList<Topic> topics;


	static YamlConfig yamlConfig = new YamlConfig();
	static {
		yamlConfig.setPropertyElementType(Description.class, "topics", Topic.class);
		yamlConfig.writeConfig.setWriteRootTags(false);
		yamlConfig.writeConfig.setWriteRootElementTags(false);
	}


	void polish(State defaultState) throws ConfigurationException {
		if(this.zookeeper == null) {
			throw new ConfigurationException("zookeeper connnection string must be defined");
		}
		if(this.topics == null) {
			// To have an empty list
			this.topics = new ArrayList<Topic>();
		}
		for(Topic topic : this.topics) {
			topic.polish(defaultState);
		}
	}

	static public class Topic {
		public String name;
		public State state;
		public Integer replicationFactor;
		public Integer partitionFactor;
		public Properties properties;
		
		void polish(State defaultState) throws ConfigurationException {
			if(name == null || this.replicationFactor == null | this.partitionFactor == null) {
				throw new ConfigurationException("name, replicationFactor and partitionFactor must be defined");
			}
			if(state == null) {
				state = defaultState;
			}
			if(this.properties == null) {
				this.properties = new Properties();
			}
		}
		
		public String toString() {
			return Misc.toYamlString(this, Description.yamlConfig);
		}
	}

}
