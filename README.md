# Status

[![Build Status](https://api.travis-ci.org/repos/rexhoffman/Testing.svg?branch=master)](https://travis-ci.org/rexhoffman/Testing/#)
[![Maven Site](https://img.shields.io/badge/maven_site-1.1.0-green.svg)](http://rexhoffman.github.io/Testing/1.1.0-SNAPSHOT/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.e-hoffman.testing/Testing/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.e-hoffman.testing/Testing/)

# Testing

Utilities to make multi-threaded testing sane, and decouple fixtures from testing framework apis.

Abilities include:

* Evict classes that include static mutable state (whitelisting is also easy).
* Capture all logging on a thread (and report it easily on test failure) with logback.
* Spring support through a generic IoC mechanism.

