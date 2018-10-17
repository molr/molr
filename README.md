# molr-remote

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/0158f5fd2de44a3db54f387096a4476a)](https://app.codacy.com/app/yassine-kr/molr-remote?utm_source=github.com&utm_medium=referral&utm_content=molr/molr-remote&utm_campaign=badger)
[![Travis Badge](https://travis-ci.org/molr/molr-remote.svg?branch=master)](https://travis-ci.org/molr/molr-remote)
[![Codecov Badge](https://codecov.io/gh/molr/molr-remote/branch/master/graph/badge.svg)](https://codecov.io/gh/molr/molr-remote/branch/master)


A delegation framework for accelerator commissioning with remote execution

For a high level overview on Molr, check out this presentation (includes slides and video): https://indico.cern.ch/event/658004/contributions/2682253/


# Package structure

molr-agency-core
molr-agency-local
molr-agency-remote
molr-agency-server
    * tomcat

molr-mole-core
molr-mole-local
molr-mole-remote
molr-mole-server
    * tomcat

molr-commons
    * NO spring