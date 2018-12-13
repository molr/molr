[![Latest release](https://img.shields.io/github/release/molr/molr.svg?maxAge=1000)](https://github.com/molr/molr/releases)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/78b10d06c5474a908dcf5ad7da08e269)](https://app.codacy.com/app/molr-developers/molr?utm_source=github.com&utm_medium=referral&utm_content=molr/molr&utm_campaign=Badge_Grade_Dashboard)
[![Build Status](https://travis-ci.com/molr/molr.svg?branch=master)](https://travis-ci.com/molr/molr)
[![codecov](https://codecov.io/gh/molr/molr/branch/master/graph/badge.svg)](https://codecov.io/gh/molr/molr)
[![GitHub license](https://img.shields.io/github/license/molr/molr.svg)](https://github.com/molr/molr/blob/master/LICENSE)

# molr

A Modular Distributed Execution and Debugging Framework.

Molr provides a unified way to interact (locally or remotely) with executable code in order to run it and/or debug it.
It is possible to provide input to it and retrieve output, return values and exceptions from this.
The reference implementation (in the molr repository) is implemented in java. 
However, remote interaction is performed through a well defined REST API. This makes it very easy to implement 
'plugins' ([Moles](#moles)) in any programming language.

Molr is designed to be completely asynchronous. For this purpose, reactive streams are used. The chosen implementation
for this in java is [Project Reactor](https://projectreactor.io/).

## The Name

As every connoisseur of spy movies knows, a mole is an agent who works for the other side. 
During the development of molr, we decided that we need fresh words ... so our _agents_ are called __moles__ 
(Although we hope they are still good guys ;-).

Furthermore the framework is very modular. This results in the name __molr__:
```
mol[e] + mo[dula]r = molr
``` 

## Missions
A mission is something that can be run and (in most cases) produces side effects. 

>This is important to note: 
>The main purpose of molr is to produce side effects (e.g. change settings of a device, reconfigure something, ...). 
>Despite the missions can produce outputs, this is not the main focus of molr. In case you are looking for a framework 
>for data processing, molr is definitely not what you need.

This is pretty much the whole definition of a mission. There is no language to which a mission is bound nor any 
interface it has to implement or so. If a mission can finally be used in molr, depends on the fact if there exists a 
_mole_ that can execute (run/debug) a given mission.

## Moles   
A mole ist the central component in molr. It has the responsibility to execute certain type of missions. 
In principle, a mole is also not bound to be 
implemented in a certain programming language, as the remote moles can be plugged into 
molr as long as they implement a well defined REST API.

However, if a mole is implemented in Java, than it can (in addition to the remote usage) also be used embedded in 
the same jvm as the client application. The responsibilities of a mole include: 

* providing information of available missions
* providing information about running missions (result/output/state)
* running/stepping through missions

Fur further details, please refer to the 
[Mole](https://www.javadoc.io/page/io.molr/molr-mole-core/latest/io/molr/mole/core/api/Mole.html) interface. 

### Out-of-the-shelf moles

The following moles are already implemented in the molr-mole-core package and can be used right out of the box:

* __[SingleNodeMole](https://www.javadoc.io/page/io.molr/molr-mole-core/latest/io/molr/mole/core/single/SingleNodeMole.html):__ 
Supports Missions that basically consist of single nodes (e.g. Runnables, Functions) which can only run, but never stepped through.
* __[RunnableLeafsMole](https://www.javadoc.io/page/io.molr/molr-mole-core/latest/io/molr/mole/core/runnable/RunnableLeafsMole.html):__ 
This is up to now probably the most complicated java mole; It provides a simple DSL to generate missions that are organized in a 
tree form, can have sequential or parallel branches and have simple nodes, that execute simple java code, as leafs.
* __[LocalSuperMole](https://www.javadoc.io/page/io.molr/molr-mole-core/latest/io/molr/mole/core/local/LocalSuperMole.html):__
Orchestrates a set of other moles, summarizes the available missions and delegates to the underlying moles accordingly. 
This allows a chaining/composition of moles. If remote moles are used here, then a microservice architecture
can be created, which can lead out of '[dependency hell](https://en.wikipedia.org/wiki/Dependency_hell)'. 

### Complementary Moles

Moles, which depend on additional libraries and/or are implemented in different languages are not contained in the 
core packages, but are organized in different repositories. Currently, the following ones are available:

* __JUnit5Mole:__ A mole implemented in java that can run junit 5 tests. 
It resides in [https://github.com/molr/molr-mole-junit5](https://github.com/molr/molr-mole-junit5).
* __Python Mole:__ This mole discovers functions in certain python packages as missions and can run them and 
even partially step through them. It resides in [https://github.com/molr/molr-pymole](https://github.com/molr/molr-pymole). 
Since it is implemented in python itself, it only can be used as remote mole and not embedded in java. 


## Packages

The following is the molr structure of packages/jars. The main aspect which is taken into 
account while splitting packages is that of dependencies. 

Click on the download badge to get to bintray, where you can find the code snippets to put into your gradle/maven files.

| &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;package&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; |description| to be used when |
|:-------:|:----------|:----------------|
|molr-commons <br> [ ![Download](https://api.bintray.com/packages/molr/molr-repo/molr-commons/images/download.svg) ](https://bintray.com/molr/molr-repo/molr-commons/_latestVersion) |Common elements for the molr project (e.g. domain objects and DTOs). No Spring dependency! | always |
|molr-mole-core <br> [ ![Download](https://api.bintray.com/packages/molr/molr-repo/molr-mole-core/images/download.svg) ](https://bintray.com/molr/molr-repo/molr-mole-core/_latestVersion) | Contains the interfaces and default implementations for moles, as well as utility methods. Depends on spring context only| always (e.g. implementing a mole, using a mole - embedded or as client)|
|molr-mole-remote <br> [ ![Download](https://api.bintray.com/packages/molr/molr-repo/molr-mole-remote/images/download.svg) ](https://bintray.com/molr/molr-repo/molr-mole-remote/_latestVersion) | Contains the implementation of a remote mole, which can delegate to any mole reachable through a Web API. Depends on Spring webflux. | using a mole as remote client |
|molr-mole-server <br> [ ![Download](https://api.bintray.com/packages/molr/molr-repo/molr-mole-server/images/download.svg) ](https://bintray.com/molr/molr-repo/molr-mole-server/_latestVersion) |Exposes any mole as a REST service. This package has Java server dependency (e.g. Tomcat). | exposing a mole as a rest service |


