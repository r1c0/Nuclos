#!/bin/sh

exec "${server.java.home}/bin/java" -jar "${server.home}/bin/uninstaller.jar" "${server.home}/nuclos.xml"
