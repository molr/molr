# molr

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/0158f5fd2de44a3db54f387096a4476a)](https://app.codacy.com/app/yassine-kr/molr-remote?utm_source=github.com&utm_medium=referral&utm_content=molr/molr-remote&utm_campaign=badger)
[![Travis Badge](https://travis-ci.org/molr/molr-remote.svg?branch=master)](https://travis-ci.org/molr/molr-remote)
[![Codecov Badge](https://codecov.io/gh/molr/molr-remote/branch/master/graph/badge.svg)](https://codecov.io/gh/molr/molr-remote/branch/master)


A Modular Remote Execution and Debugging Framework.

As every connoisseur of spy movies knows, a mole is an agent who works for the other side. 
During the development of molr, we decided that we need fresh words ... so our _agents_ are called _moles_.
Furthermore the framwork is very modular. This results in the name _molr_:
```$xslt
mol[e] + mo[dula]r = molr
``` 

### Getting Started
For the moment, there are no packages released yet. Therefore, the only way to try molr for the moment is to clone 
this repository. The currently most up-to-date branch is `op-proposal`.

> The current version is heavily under development and not yet stable at all. We hope this to change within a few weeks. 
> Stay tuned! 

### Concept & Vision
The original document about the vision and purpose of molr can be found in a separate repo:

https://github.com/molr/molr-docs/blob/master/README.md

### Package structure

The following is a proposed structure of packages/jars. The main aspect which shall be taken into 
account while splitting packages is that of dependencies. 

| package | description|
|---------| -----------|
|molr-commons | No Spring dependency! contains all the elemnts which shall be used in all other packages. (E.g.: Domain objects, DTOs, Utilities...)|
|molr-agency-core | contains the all the classes required in agency related packages. (E.g. interfaces)|
|molr-agency-local | Shall contain the local default implementation (tbd, currently this is in agency-core as it does not really bring additional deps)|
|molr-agency-remote | The remote implementation of an agency. It uses spring webflux to connect to an agency server|
|molr-agency-server | This package has dependencies on e.g. tomcat (or similar). It provides a rest service representing an agency |
|molr-mole-core | Contains the interfaces and default implementation for moles, as well as utility methods.|
|molr-mole-local | tbd |
|molr-mole-remote | contains the implementation of a remote mole, which can delegate to any mole reachable through a web API. Depends on spring webflux. |
|molr-mole-server |    This package has a dep on e.g. tomcat (or similar) to explose any mole as a rest service. |
