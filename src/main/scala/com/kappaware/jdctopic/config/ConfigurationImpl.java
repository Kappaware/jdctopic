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
