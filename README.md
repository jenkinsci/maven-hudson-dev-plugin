# Maven plugin for developing Jenkins core
This Maven plugin is intended to be the single plugin to contain all
the goals needed for hacking [core](https://github.com/jenkinsci/jenkins).
IOW, the core equivalent of [maven-hpi-plugin](https://github.com/jenkinsci/maven-hpi-plugin).

In reality, this is a patched version of [jetty-maven-plugin](http://www.eclipse.org/jetty/documentation/current/jetty-maven-plugin.html)
that defines only one goal `hudson-dev:run` to run the web application.

## Upstream tracking
This repository uses the `incoming` branch that keeps track of the prestine copy of the upstream jetty-maven-plugin,
and the `master` branch that maintains local patches. Thus you can see the exact list of local patches by
`git diff incoming master`.

To update this plugin to a new version of `jetty-maven-plugin`, checkout the `incoming` branch, remove all files,
unzip the copy from the upstream, add all the new files, then commit.

To reflect this new `incoming` branch into the `master` branch, checkout the `master` branch, `git merge incoming`
and resolve conflicts. Check the diff before and after the merge to make sure there's no unintended merge problem.
Git merge algorithm sometimes doesn't do the right thing.
