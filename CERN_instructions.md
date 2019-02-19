# CERN specific instructions

In order to build the project inside CERN TN, you need to use an external Gradle distribution. CBNG does not support gradle `project` dependencies at the moment.
You can download a Gradle distribution (you can use the same version as the wrapper task, check build.gradle file) from [gradle.org](https://gradle.org/releases/).
Then you can instruct Eclipse or IntelliJ to use this Gradle distribution instead of CBNG.