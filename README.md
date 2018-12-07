[![Codacy Badge](https://api.codacy.com/project/badge/Grade/78b10d06c5474a908dcf5ad7da08e269)](https://app.codacy.com/app/molr-developers/molr?utm_source=github.com&utm_medium=referral&utm_content=molr/molr&utm_campaign=Badge_Grade_Dashboard)
[![Build Status](https://travis-ci.com/molr/molr.svg?branch=master)](https://travis-ci.com/molr/molr)
[![codecov](https://codecov.io/gh/molr/molr/branch/master/graph/badge.svg)](https://codecov.io/gh/molr/molr)
[![GitHub license](https://img.shields.io/github/license/molr/molr.svg)](https://github.com/molr/molr/blob/master/LICENSE)

# molr

A Modular Distributed Execution and Debugging Framework.

As every connoisseur of spy movies knows, a mole is an agent who works for the other side. 
During the development of molr, we decided that we need fresh words ... so our _agents_ are called __moles__ 
(Although we hope they are still good guys ;-).

Furthermore the framework is very modular. This results in the name __molr__:
```
mol[e] + mo[dula]r = molr
``` 

### Packages

The following is the molr structure of packages/jars. The main aspect which is taken into 
account while splitting packages is that of dependencies. 

Click on the download badge to get to bintray, where you can find the code snippets to put into your gradle/maven files.

|  &nbsp;&nbsp;package&nbsp;&nbsp; |description| to be used when |
|:-------:|:----------|:----------------|
|molr-commons <br> [ ![Download](https://api.bintray.com/packages/molr/molr-repo/molr-commons/images/download.svg) ](https://bintray.com/molr/molr-repo/molr-commons/_latestVersion) |Common elements for the molr project (e.g. domain objects and DTOs). No Spring dependency! | always |
|molr-mole-core <br> [ ![Download](https://api.bintray.com/packages/molr/molr-repo/molr-mole-core/images/download.svg) ](https://bintray.com/molr/molr-repo/molr-mole-core/_latestVersion) | Contains the interfaces and default implementations for moles, as well as utility methods. Depends on spring context only| always (e.g. implementing a mole, using a mole - embedded or as client)|
|molr-mole-remote <br> [ ![Download](https://api.bintray.com/packages/molr/molr-repo/molr-mole-remote/images/download.svg) ](https://bintray.com/molr/molr-repo/molr-mole-remote/_latestVersion) | Contains the implementation of a remote mole, which can delegate to any mole reachable through a Web API. Depends on Spring webflux. | using a mole as remote client |
|molr-mole-server <br> [ ![Download](https://api.bintray.com/packages/molr/molr-repo/molr-mole-server/images/download.svg) ](https://bintray.com/molr/molr-repo/molr-mole-server/_latestVersion) |Exposes any mole as a REST service. This package has Java server dependency (e.g. Tomcat). | exposing a mole as a rest service |



### Concept & Vision
The original document about the vision and purpose of molr can be found [here](docs/concepts-and-vision.md). 


As this document reflects the actual state of development, here the current definition of the key entities in molr...

#### Mission
A mission is something that can be run and (in most cases) produces side effects. 

>This is important to note: 
>The main purpose of molr is to produce side effects (e.g. change settings of a device, reconfigure something, ...). 
>Despite the missions can produce outputs, this is not the main focus of molr. In case you are looking for a framework 
>for data processing, molr is definitely not what you need.

This is pretty much the whole definition of a mission. There is no language to which a mission is bound nor any 
interface it has to implement or so. If a mission can finally be used in molr, depends on the fact if there exists a 
_mole_ that can execute (run/debug) a given mission.

#### Mole   
A mole ist the central interface in molr. It has the responsibility to execute certain type of missions. 
In principle, a mole is also not bound to be 
implemented in a certain programming language, as the remote moles can be plugged into 
molr as long as they implement a well defined REST API.

However, if a mole is implemented in Java, than it can (in addition to the remote usage) also be used embedded in 
the same jvm as the client application. The responsibilities of a mole are best described by the corresponding java interface:

[Mole.java](https://github.com/molr/molr/blob/master/molr-mole-core/src/main/java/org/molr/mole/core/api/Mole.java)

As seen from this interface, the mole also has quite some other responsibilities, like keeping track of running 
instances and states of the corresponding missions.

Molr is designed to be completely asynchronous. For this purpose, reactive streams are used. The chosen implementation
for this is [Project Reactor](https://projectreactor.io/), as can be seen from the used classes in the interfaces
(Flux and Mono).

#### Supermole
The real power of molr comes from the fact that moles are designed to be chainable and distributed. For different 
applications it is very useful to have one central place to perform certain missions, while the actual mission execution
might be better done by remote calls. This way a microservice architecture is possible, which has several advantages 
over a monolithic solution (where all missions are e.g. executed within the same jvm), of which the most important to 
mention might be the escape from '[dependency hell](https://en.wikipedia.org/wiki/Dependency_hell)'.

The supermole is nothing else than a mole (also implementing the mole interface), which manages several other moles,
summarizes their states and delegates mission execution to them.



