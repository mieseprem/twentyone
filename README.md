# Useless code

It depends :wink:

This repo is just to get an insight on how to easily achieve parallel processing by using features from Java Project Loom (but only those that are GA in Java 21).

To test virtual threads, less code and dependencies would have been sufficient. I just wanted to try it out in an environment that was similar to the code I'm used to.

## App logic

In terms of content, the code is indeed meaningless. Roughly speaking, the following happens here:

* A series of numbers (intended to represent the workload) is passed in parallel to a "runner", which performs all the work steps to be done.
* Firstly, data must be fetched from various REST services for each workload. This should also be done in parallel.
* The summarised data is then written away via another web service (this does not actually happen)

## Flow control

Even if you could process thousands of threads - and in my example workloads - in parallel with Virtual Threads, the web services called up usually don't like being called up so massively in reality.
I have therefore implemented two measures to reduce external access:
* Semaphores, when parallel requests are definitely allowed, but only up to a certain level
* Lock, if a service should only be accessed individually despite parallelism. Project Loom recommends using Lock instead of Synchronised blocks to prevent the complete blocking of a platform thread.
