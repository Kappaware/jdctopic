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
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.kappaware.jdctopic.Misc;

public class Description {
	static Logger log = LoggerFactory.getLogger(Description.class);

	
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
		if (this.zookeeper == null) {
			throw new ConfigurationException("zookeeper connnection string must be defined");
		}
		if (this.topics == null) {
			// To have an empty list
			this.topics = new ArrayList<Topic>();
		}
		for (Topic topic : this.topics) {
			topic.polish(defaultState);
		}
	}

	static public class Topic {
		public String name;
		public State state;
		public Integer replicationFactor;
		public Integer partitionFactor;
		public Properties properties;
		public Map<Integer, List<Integer>> assignments;		

		void polish(State defaultState) throws ConfigurationException {
			
			log.debug(this.toString());
			
			if (state == null) {
				state = defaultState;
			}
			if (name == null) {
				throw new ConfigurationException("A topic is defined without 'name' attribut!");
			}
			if (state == State.present) {
				if (this.assignments == null) {
					if (this.replicationFactor == null || this.partitionFactor == null) {
						throw new ConfigurationException(String.format("Topic '%s': If partitions layout is not explicit, both replicationFactor and partitionFactor must be defined", this.name));
					}
				} else {
					if (this.assignments.size() == 0) {
						throw new ConfigurationException(String.format("Topic '%s': At least one partition must be defined", this.name));
					}
					if(this.partitionFactor == null) {
						this.partitionFactor = this.assignments.size();
					} else if(this.partitionFactor != this.assignments.size()) {
						throw new ConfigurationException(String.format("Topic '%s': Partition count does not match (partitionFactor=%d while partition.size=%d)", this.name, this.partitionFactor, this.assignments.size()));
					}
					//log.debug(this.toString());
					// As Yaml parser does not care of type restriction, partitions is in fact build from String. Must convert to effective integer
					List<?> klist = new ArrayList<Object>(this.assignments.keySet());	// Create a list instance, as we will remove object in underlying array
					for(Object k : klist) {
						List<?> vlist = this.assignments.get(k);
						this.assignments.remove(k);
						ArrayList<Integer> vnlist = new ArrayList<Integer>();
						for(Object o : vlist) {
							vnlist.add(parseInteger(o));
						}
						this.assignments.put(parseInteger(k), vnlist);
					}
					//log.debug(this.toString());
					for(Integer i = 0; i < this.partitionFactor; i++) {
						List<Integer> bl = this.assignments.get(i);
						if(bl == null) {
							throw new ConfigurationException(String.format("Topic '%s': Partition#%d must be defined", this.name, i));
						}
						if(this.replicationFactor == null) {
							this.replicationFactor = bl.size();
						}
						if(this.replicationFactor != bl.size()) {
							throw new ConfigurationException(String.format("Topic '%s': Replica count does not match for at least one partition (%d != %d)", this.name, this.replicationFactor, bl.size()));
						}
					}
				}
			}

			if (this.properties == null) {
				this.properties = new Properties();
			}
		}

		public String toString() {
			return Misc.toYamlString(this, Description.yamlConfig);
		}
	}

	static Integer parseInteger(Object o) throws ConfigurationException {
		try {
			return Integer.parseInt(o.toString());
		} catch(Exception e) {
			throw new ConfigurationException(String.format("'%s' value is illegal. Only integer are allowed in such place", o.toString()));
		}
	}
	
}
