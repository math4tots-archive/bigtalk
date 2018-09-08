The modules in this directory are meant bind to platform specific
APIs, and all other modules under simple are meant to depend on them.
This means that if all modules under simple.core are implemented
for a given platform, all other modules under simple should just work.
