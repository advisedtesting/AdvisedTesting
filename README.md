[![Build Status](https://api.travis-ci.org/repos/advisedtesting/AdvisedTesting.svg?branch=master)](https://travis-ci.org/advisedtesting/AdvisedTesting/#)
[![Maven Site](https://img.shields.io/badge/maven_site-1.3.0-green.svg)](http://advisedtesting.github.io/AdvisedTesting/1.3.0/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.advisedtesting/AdvisedTesting/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.advisedtesting/AdvisedTesting/)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/78e8506caab14df69835626deb3a39a9)](https://www.codacy.com/app/advisedtesting/AdvisedTesting?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=advisedtesting/AdvisedTesting&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/advisedtesting/AdvisedTesting/branch/master/graph/badge.svg)](https://codecov.io/gh/advisedtesting/AdvisedTesting)
[![codebeat badge](https://codebeat.co/badges/bf76c593-de44-4a99-be52-b02353da1c40)](https://codebeat.co/projects/github-com-advisedtesting-advisedtesting-master)

# AdvisedTesting

<img align="left" width="180" height="180" src="./src/site/resources/advisedtesting.png">

Utilities to make multi-threaded testing sane, and decouple fixtures from testing framework apis.

Abilities include:

* Evict classes that include static mutable state (whitelisting is also easy).
* Capture all logging on a thread (and report it easily on test failure) with logback.
* Spring support through a generic IoC mechanism.

