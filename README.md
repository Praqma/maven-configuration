# maven-configuration

Repository with stuff for maven

## Why this repo?

We need to release stuff for Jenkins, and in order to do that we have to have a `settings.xml` file with credentials. 
We can hide this in multiple ways, but an easy one is to use `withCredentials` closure in Jenkins pipeline, and for other tools
you can add parameters which hold this secret value. And we do not want to add this file to every plugin we want to release.

## Usage

Basically... `curl settings.xml -O` to download this settings file.
