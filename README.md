# What is OData Dynamic Virtual Services?

The OData Dynamic Virtual Services (DVS) allows model driven creation of Service Virtualizations emulating an OData service that run under CA Service Virtualization (formerly CA-LISA).

With limited exceptions as noted in the documentation, the Service Virtualizations created with DVS provide full support at the OData Minimal Conformance Level for an updatable 
OData v4 service, as well as a significant number of intermediate and Advanced features.   Legacy Odata v3 compliance is provided with exceptions as noted.

Data will persist so long as the Virtual Service is running, and a REST API is provided to allow data to be saved and restored to known points across Virtual Service shutdowns. 

The OData Virtual Service Model can be defined from a RAML file, or by directly creating or editing an XML configuration file referred to as an AEDM.

DVS functionality may be accessed either through an Eclipse plug-in, or a REST API provided by Servlet which can be hosted under Tomcat.
   
See the documentation for full list of features, and installation instructions.

README.txt contains the list of defects currently known.

## Pre-requisites

The OData Virtual Services created run in a CA Service Virtualization (CA LISA) 7.5, or 8.0 Virtual Service Environment which have the “CA Service Virtualization OData Extension” installed.

DVS Eclipse plug-in requires Eclipse (surprise!), and has been tested using the Kepler distribution.

DVS Servlet war has been tested using Tomcat Webserver version 7.0.57. 

Building from source using the build procedure provided will require 3rd Party Components as listed in the build instructions. 

## Documentation

DVS documentation is available from the TBD-URL-FIX-ME.

You must have a support.ca.com account to access this documentation.

## License 
 
Contents of the OData Dynamic Virtualization Services project are released under the Eclipse Public License 1.0, with the exception of dvs.lisa-extensions.odata.jar, dvs.lisa-extensions.ddme.jar, and dvs.utilities.commonUtils.jar (the “CA Service Virtualization OData Extension” files) for which all rights are retained except as set forth below.   The “CA Service Virtualization OData Extension” files are not directly required by the OData Dynamic Virtualization Services project but are needed in order to run virtual services created from this project.  You are permitted to use the “CA Service Virtualization OData Extension” only for your internal use in conjunction with your authorized use of the CA Service Virtualization product.  CA provides the “CA Service Virtualization OData Extension” files without obligation of support and “AS IS,” WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING, WITHOUT LIMITATION, THE WARRANTY OF TITLE, NON-INFRINGEMENT OR NON-INTERFERENCE AND ANY IMPLIED WARRANTIES AND CONDITIONS OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE.
 
Contents of the OData Dynamic Virtualization Services project that are being released under the Eclipse license will so indicate in their respective source code headers.

The Eclipse Public License 1.0 may be found at http://www.eclipse.org/legal/epl-v10.html, and a copy is included within LICENSE.TXT
 
OData Dynamic Virtualization Services project does not include but will require use of various 3rd Party components which have their own licenses.
 
Please see LICENSE.txt or the Documentation for a full list of Licenses and acknowledgements.


## Making contributions

This code base is published on the public GitHub at https://github.com/DevTestSolutions/dvs_ce

