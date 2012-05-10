obj-store-lib
=============

Java API Bindings to "object storage" (Currently EMC Atmos)

This library provides an API to "object storage", currently the only implementation is [EMC Atmos](http://www.emc.com/atmos).

At this point, I am not sure the abstractions will hold up against another system.

These bindings used the [EMC Java Bindings](http://code.google.com/p/atmos-java/) as a reference, however the abstractions are different.

Changes include:

 * This API extracts the user information from the API object.
   This allows the API object to be created as a singleton, while supporting multiple users if your application has a multi-tenancy model
 * The only implementation in this library uses Apache HttpClient
   * HttpClient has a dependency on commons-logging, but there is no dependency on any other logging framework in this project
 * This API assumes you will be working with InputStreams, no support for byte[]
 * No threaded uploads
 * No support for Extents
 * Improved code reuse