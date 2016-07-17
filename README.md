# jdctopic

An idempotent tool to easily create and maintain kafka topics.

When deploying an application in the Kafka/streaming world, a common issue is to create the required topics.

This is usually achieved using scripts. But, this can quickly become cumbersome and hard to maintain. And a nightmare when it come to updating a running application.

'jdc' stand for 'Just DesCribe'. You define all the topics with relevant parameters in a simple YAML file and jdctopic will take care of deploying all theses object on your Kafka cluster.

In case of schema evolution, just change the description file, apply it again, and appropriate modification will be issued.

jdctopic is a convergence tool. Its aim is to align the real configuration to what is described on the source file, while applying only strictly necessary modification.

This make jdctopic a fully idempotent tool, as all modern DevOps tools should be.

***
## Usage

jdctopic is provided as rpm packages (Sorry, only this packaging is currently provided. Contribution welcome), on the [release pages](https://github.com/BROADSoftware/jdctopic/releases).

Once installed, usage is the following:

    $ jdctopic --inputFile yourDescription.yml
    
Where `yourDescription.yml` is a file containing your target Kafka topics description. jdctopic will then perform all required operation to reach this target state.

Note than if `yourDescription.yml` content match the current configuration, no operation will be performed.

Here is a sample of such `description.yml` file:

    zookeeper: "zknode1.yourdomain.com,zknode2.yourdomain.com,zknode3.yourdomain.com"
    topics:
	- name: topic1
	  replicationFactor: 1
	  partitionFactor: 50
	- name: topic2
	  replicationFactor: 3
	  partitionFactor: 4
	  properties:
	    delete.retention.ms: 315360000000
	    retention.ms: 215360000000
	    cleanup.policy: delete
          
          
* `zookeeper:` Must be filled with the zookeeper's quorum of your target cluster. 

   Note this parameter may be missing. In such case, you will need to provide the `--zookeeper` parameter on the command line.

* Then, each topic is described with:

  * A name: attribute, providing the topic name.
  
  * A replicationFactor
  
  * A partitionFactor

  * An optional list of properties, allowing definition of topic properties.


### Other launch option

When launching the jdctopic command you may provide some optional parameters:

* `--zookeeper` parameter will allow to override the corresponding value in the `description.yml` file.

* `--defaultState` parameter will allow setting of all `state` value which are not explicitly defined. See below 

* `--dryRun` parameter will just display action needed to converge to target state, but do nothing

* `--forceProperties` parameter will skip properties validity check. May be used to force a property this tool was not aware of.  
  

### Topic deletion

All topics not described in the `description.yml` file will be left untouched.

To allow deletion to be performed, each topic got a `state:` attribute. When not defined, it default to  `present`, or to the value provided by the `--defaultState` parameter. But it could be set to `absent` to trigger the deletion of the corresponding topic.

For example: 

    zookeeper: "zknode1.yourdomain.com,zknode2.yourdomain.com,zknode3.yourdomain.com"
    topics:
	- name: topic1
	  state: absent
	- name: topic2
	  state: absent
	  replicationFactor: 3
	  partitionFactor: 4
	  properties:
	    delete.retention.ms: 315360000000
	    retention.ms: 215360000000
	    cleanup.policy: delete

Will delete columnFamily `topic1` and `topic2` (if existing) from previous configuration. 

***
## Ansible integration

With its idempotence property, jdctopic is very easy to be orchestrated by usual DevOps tools like Chef, Puppet or Ansible.

You will find an Ansible role [at this location](http://github.com/BROADSoftware/bsx-roles/tree/master/kappatools/jdctopic).

This role can be used as following;
	
	- hosts: zookeepers
	
	- hosts: cmd_node
	  vars:
        jdctopic_rpm_url: http://git2.broadsoftware.com/jenkins/job/jdctopic/lastSuccessfulBuild/artifact/build/distributions/jdctopic-0.1.0-1.noarch.rpm
	    topic_list1:
	      topics:
	      - name: test1a
	        replicationFactor: 3
	        partitionFactor: 3
	        properties:
	          retention.ms: 315360000000
	          cleanup.policy: delete
	  roles:
	  - { role: kappatools/jdctopic, jdctopic_description: "{{topic_list1}}" }
	  
> Note `- hosts: zookeepers` at the beginning, which force ansible to grab info about the hosts in the [zookeepers] group, to be able to fulfill this info into jdctopic configuration. Of course, such a group must be defined in the inventory. 

***
## Build

Just clone this repository and then:

    $ gradlew rpm

This should build everything. You should be able to find generated packages in build/distribution folder.

***
## License

    Copyright (C) 2016 BROADSoftware

	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	
	    http://www.apache.org/licenses/LICENSE-2.0
	
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
