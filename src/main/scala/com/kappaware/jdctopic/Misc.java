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
package com.kappaware.jdctopic;

import java.io.StringWriter;

import com.esotericsoftware.yamlbeans.YamlConfig;
import com.esotericsoftware.yamlbeans.YamlWriter;

public class Misc {

	public static String toYamlString(Object o, YamlConfig yamlConfig) {
		StringWriter sw = new StringWriter();
		YamlWriter yamlWriter = new YamlWriter(sw, yamlConfig);
		try {
			yamlWriter.write(o);
			yamlWriter.close();
			sw.close();
		} catch (Exception e) {
			throw new RuntimeException("Exception in YAML generation", e);
		}
		return sw.toString();
	}
	
	
	public static String indent(String prefix, String value) {
		StringBuffer sb = new StringBuffer();
		boolean toIndent = true;
		for(int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			if(c == '\n') {
				toIndent = true;
				sb.append("\n");
			} else {
				if(toIndent) {
					sb.append(prefix);
					toIndent = false;
				}
				sb.append(c);
			}
		}
		return sb.toString();
	}

}
