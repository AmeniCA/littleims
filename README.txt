littleIMS 1.0-SNAPSHOT
======================

This is the readme file for the littleIMS project. 

littleIMS consists in an Open Source implementation of several IMS/TISPAN network 
elements. The goal of littleIMS is to provide most of the features found in an 
IMS network in a simple and extensible way. littleIMS is also a reference application 
showing how to develop converged SIP/HTTP/Diameter applications using cipango and the 
Spring/Hibernate/Wicket application stack.

littleIMS can be found at: http://confluence.cipango.org/display/LITTLEIMS

Contents
--------
 - License
 - Requirements
 - Getting started
 - Getting help

License
-------

littleIMS is distributed under the terms of the Apache Software Foundation
license, version 2.0.

Requirements
------------

littleIMS requires at least Java 1.5.


Getting started
---------------

To start littleIMS, 
1) Make the following hostname resolvable within a DNS query:
    * hss.cipango.voip
    * i-cscf.cipango.voip
    * s-cscf.cipango.voip
    * p-cscf.cipango.voip
      This could be achieved by
    * editing the resolv.conf file (in /etc for *nix systems or in C:\WINDOWS\system32\drivers\etc under Microsoft Windows.
    * contacting your system administrator to add these entries in the DNS server

2) If running under linux, create a new file $HOME/.cipangorc with the content (replace /home/littleims with the right value)
LITTLEIMS_HOME=/home/littleims
SCSCF_HOME=${LITTLEIMS_HOME}/S-CSCF
ICSCF_HOME=${LITTLEIMS_HOME}/I-CSCF
PCSCF_HOME=${LITTLEIMS_HOME}/P-CSCF
HSS_HOME=${LITTLEIMS_HOME}/HSS

3) Execute the script startLittleIMS.bat / startLittleIMS.sh

Getting help
------------

 - Read the online documentation available on our website
   (http://confluence.cipango.org/display/LITTLEIMS)
 - Send a complete message containing your problem, stacktrace and problem
   you're trying to solve to the Jira bugtracker: http://jira.cipango.org/secure/Dashboard.jspa