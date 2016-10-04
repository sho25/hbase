This maven module has the protobuf definition files used by hbase Coprocessor
Endpoints that ship with hbase core including tests. Coprocessor Endpoints
are meant to be standalone, independent code not reliant on hbase internals.
They define their Service using protobuf. The protobuf version they use can be
distinct from that used by HBase internally since HBase started shading its
protobuf references. Endpoints have no access to the shaded protobuf hbase uses.
They do have access to the content of hbase-protocol but avoid using as much
of this as you can as it is liable to change.

The produced java classes are generated and then checked in. The reasoning is
that they change infrequently.

To regenerate the classes after making definition file changes, in here or over
in hbase-protocol since we source some of those protos in this package, ensure
first that the protobuf protoc tool is in your $PATH. You may need to download
it and build it first; it is part of the protobuf package. For example, if using
v2.5.0 of protobuf, it is obtainable from here:

 https://github.com/google/protobuf/releases/tag/v2.5.0

HBase uses hadoop-maven-plugins:protoc goal to invoke the protoc command. You can
compile the protoc definitions by invoking maven with profile compile-protobuf or
passing in compile-protobuf property.

mvn compile -Dcompile-protobuf
or
mvn compile -Pcompile-protobuf

You may also want to define protoc.path for the protoc binary

mvn compile -Dcompile-protobuf -Dprotoc.path=/opt/local/bin/protoc

If you have added a new proto file, you should add it to the pom.xml file first.
Other modules also support the maven profile.

After you've done the above, check it in and then check it in (or post a patch
on a JIRA with your definition file changes and the generated files).

NOTE: The maven protoc plugin is a little broken. It will only source one dir
at a time. If changes in protobuf files, you will have to first do protoc with
the src directory pointing back into hbase-protocol module and then rerun it
after editing the pom to point in here to source .proto files.
