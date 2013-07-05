#!/usr/bin/env python

# Taken from gist https://gist.github.com/neothemachine/4060735

import sys
import os
import os.path
import xml.dom.minidom
 
if os.environ["TRAVIS_SECURE_ENV_VARS"] == "false":
  print "no secure env vars available, skipping deployment"
  sys.exit()
 
homedir = os.path.expanduser("~")
 
m2 = xml.dom.minidom.parse(homedir + '/.m2/settings.xml')
settings = m2.getElementsByTagName("settings")[0]
 
serversNodes = settings.getElementsByTagName("servers")
if not serversNodes:
  serversNode = m2.createElement("servers")
  settings.appendChild(serversNode)
else:
  serversNode = serversNodes[0]

sonatypeServerNode = m2.createElement("server")
sonatypeServerId = m2.createElement("id")
sonatypeServerUser = m2.createElement("username")
sonatypeServerPass = m2.createElement("password")
 
idNode = m2.createTextNode("emustudio-repository")
userNode = m2.createTextNode(os.environ["EMUSTUDIO_USERNAME"])
passNode = m2.createTextNode(os.environ["EMUSTUDIO_PASSWORD"])
 
sonatypeServerId.appendChild(idNode)
sonatypeServerUser.appendChild(userNode)
sonatypeServerPass.appendChild(passNode)

configNode = m2.createElement("configuration")
knownHostsNode = m2.createElement("knownHostsProvider")
configNode.appendChild(knownHostsNode)
knownHostsNode.setAttribute('implementation',"org.apache.maven.wagon.providers.ssh.knownhost.NullKnownHostProvider")
disableHostNode = m2.createElement("hostKeyChecking")
disableHostValue = m2.createTextNode("no")
disableHostNode.appendChild(disableHostValue)
knownHostsNode.appendChild(disableHostNode)
 
sonatypeServerNode.appendChild(sonatypeServerId)
sonatypeServerNode.appendChild(sonatypeServerUser)
sonatypeServerNode.appendChild(sonatypeServerPass)
sonatypeServerNode.appendChild(configNode)
 
serversNode.appendChild(sonatypeServerNode)

# Turn off interactive mode
interactiveNode = m2.createElement("interactiveMode")
settings.appendChild(interactiveNode)
interactiveValue = m2.createTextNode("false")
interactiveNode.appendChild(interactiveValue)
  
m2Str = m2.toxml()
f = open(homedir + '/.m2/mySettings.xml', 'w')
f.write(m2Str)
f.close()