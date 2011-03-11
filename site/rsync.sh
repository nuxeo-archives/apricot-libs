#!/bin/bash

rsync -av --delete --progress target/. osgi@osgi.nuxeo.org:~/www/p2/update/current/.
