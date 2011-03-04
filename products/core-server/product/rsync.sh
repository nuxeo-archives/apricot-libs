#!/bin/bash

rsync -av --delete --progress target/repository osgi@osgi.nuxeo.org:~/www/p2/core-server/current
