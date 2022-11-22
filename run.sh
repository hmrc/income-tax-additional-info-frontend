#!/usr/bin/env bash

sbt run -Dconfig.resource=application.conf -Dapplication.router=testOnlyDoNotUseInAppConf.Routes -J-Xmx256m -J-Xms64m -Dhttp.port=10005 -Drun.mode=Dev
