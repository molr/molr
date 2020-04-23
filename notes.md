To generate pom files, one could run something like this (to be fully tried and confirmed)

gradlew molr-commons:generatePomFileForCustomBintrayPublication -Ddeployment=true

Note: Als the VCS tag would have to be set, otherwise the script throws an exception ...

