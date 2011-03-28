#!/bin/bash
exec mvn -DupdateReleaseInfo=true clean source:jar deploy
