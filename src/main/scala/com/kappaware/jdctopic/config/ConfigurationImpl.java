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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

public class ConfigurationImpl implements Configuration {
	private Parameters parameters;
	private Description description;
	

	public ConfigurationImpl(Parameters parameters) throws ConfigurationException, FileNotFoundException, YamlException {
		this.parameters = parameters;
		File file = new File(parameters.getInputFile());

		if (!file.canRead()) {
			throw new ConfigurationException(String.format("Unable to open '%s' for reading", file.getAbsolutePath()));
		}
		YamlReader yamlReader = new YamlReader(new FileReader(file), Description.yamlConfig);
		description = yamlReader.read(Description.class);
		if(description == null) {
			throw new ConfigurationException(String.format("Missing or invalide content in '%s'", file.getAbsolutePath()));
		}
		if (parameters.getZookeeper() != null) {
			description.zookeeper = parameters.getZookeeper(); // Override the value in file
		}
		description.polish(parameters.getDefaultState());
	}

	@Override
	public Description getDescription() {
		return description;
	}

	@Override
	public boolean isDryRun() {
		return parameters.isDryRun();
	}

	@Override
	public boolean isRelaxPropertyCheck() {
		return parameters.isRelaxPropertyCheck();
	}

	
	
}
