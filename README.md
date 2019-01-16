# Status

[![Build Status](https://api.travis-ci.org/repos/rexhoffman/Testing.svg?branch=master)](https://travis-ci.org/rexhoffman/Testing/#)
[![Maven Site](https://img.shields.io/badge/maven_site-1.2.3-green.svg)](http://rexhoffman.github.io/Testing/1.2.3/)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.e-hoffman.testing/Testing/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.e-hoffman.testing/Testing/)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/e6f26c6b744844608e9c4ff1a1a3d967)](https://www.codacy.com/app/rexhoffman/Testing?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=rexhoffman/Testing&amp;utm_campaign=Badge_Grade)
[![codecov](https://codecov.io/gh/rexhoffman/Testing/branch/master/graph/badge.svg)](https://codecov.io/gh/rexhoffman/Testing)
[![codebeat badge](https://codebeat.co/badges/28947fa6-4897-45dd-bcd2-5817a726de20)](https://codebeat.co/projects/github-com-rexhoffman-testing-master)

# Testing

Utilities to make multi-threaded testing sane, and decouple fixtures from testing framework apis.

Abilities include:

* Evict classes that include static mutable state (whitelisting is also easy).
* Capture all logging on a thread (and report it easily on test failure) with logback.
* Spring support through a generic IoC mechanism.

