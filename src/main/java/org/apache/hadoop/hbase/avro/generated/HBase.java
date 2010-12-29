begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Autogenerated by Avro  *   * DO NOT EDIT DIRECTLY  */
end_comment

begin_package
package|package
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
package|;
end_package

begin_interface
annotation|@
name|SuppressWarnings
argument_list|(
literal|"all"
argument_list|)
specifier|public
interface|interface
name|HBase
block|{
specifier|public
specifier|static
specifier|final
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Protocol
name|PROTOCOL
init|=
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|Protocol
operator|.
name|parse
argument_list|(
literal|"{\"protocol\":\"HBase\",\"namespace\":\"org.apache.hadoop.hbase.avro.generated\",\"types\":[{\"type\":\"record\",\"name\":\"AServerAddress\",\"fields\":[{\"name\":\"hostname\",\"type\":\"string\"},{\"name\":\"inetSocketAddress\",\"type\":\"string\"},{\"name\":\"port\",\"type\":\"int\"}]},{\"type\":\"record\",\"name\":\"ARegionLoad\",\"fields\":[{\"name\":\"memStoreSizeMB\",\"type\":\"int\"},{\"name\":\"name\",\"type\":\"bytes\"},{\"name\":\"storefileIndexSizeMB\",\"type\":\"int\"},{\"name\":\"storefiles\",\"type\":\"int\"},{\"name\":\"storefileSizeMB\",\"type\":\"int\"},{\"name\":\"stores\",\"type\":\"int\"}]},{\"type\":\"record\",\"name\":\"AServerLoad\",\"fields\":[{\"name\":\"load\",\"type\":\"int\"},{\"name\":\"maxHeapMB\",\"type\":\"int\"},{\"name\":\"memStoreSizeInMB\",\"type\":\"int\"},{\"name\":\"numberOfRegions\",\"type\":\"int\"},{\"name\":\"numberOfRequests\",\"type\":\"int\"},{\"name\":\"regionsLoad\",\"type\":{\"type\":\"array\",\"items\":\"ARegionLoad\"}},{\"name\":\"storefileIndexSizeInMB\",\"type\":\"int\"},{\"name\":\"storefiles\",\"type\":\"int\"},{\"name\":\"storefileSizeInMB\",\"type\":\"int\"},{\"name\":\"usedHeapMB\",\"type\":\"int\"}]},{\"type\":\"record\",\"name\":\"AServerInfo\",\"fields\":[{\"name\":\"infoPort\",\"type\":\"int\"},{\"name\":\"load\",\"type\":\"AServerLoad\"},{\"name\":\"serverAddress\",\"type\":\"AServerAddress\"},{\"name\":\"serverName\",\"type\":\"string\"},{\"name\":\"startCode\",\"type\":\"long\"}]},{\"type\":\"record\",\"name\":\"AClusterStatus\",\"fields\":[{\"name\":\"averageLoad\",\"type\":\"double\"},{\"name\":\"deadServerNames\",\"type\":{\"type\":\"array\",\"items\":\"string\"}},{\"name\":\"deadServers\",\"type\":\"int\"},{\"name\":\"hbaseVersion\",\"type\":\"string\"},{\"name\":\"regionsCount\",\"type\":\"int\"},{\"name\":\"requestsCount\",\"type\":\"int\"},{\"name\":\"serverInfos\",\"type\":{\"type\":\"array\",\"items\":\"AServerInfo\"}},{\"name\":\"servers\",\"type\":\"int\"}]},{\"type\":\"enum\",\"name\":\"ACompressionAlgorithm\",\"symbols\":[\"LZO\",\"GZ\",\"NONE\"]},{\"type\":\"record\",\"name\":\"AFamilyDescriptor\",\"fields\":[{\"name\":\"name\",\"type\":\"bytes\"},{\"name\":\"compression\",\"type\":[\"ACompressionAlgorithm\",\"null\"]},{\"name\":\"maxVersions\",\"type\":[\"int\",\"null\"]},{\"name\":\"blocksize\",\"type\":[\"int\",\"null\"]},{\"name\":\"inMemory\",\"type\":[\"boolean\",\"null\"]},{\"name\":\"timeToLive\",\"type\":[\"int\",\"null\"]},{\"name\":\"blockCacheEnabled\",\"type\":[\"boolean\",\"null\"]}]},{\"type\":\"record\",\"name\":\"ATableDescriptor\",\"fields\":[{\"name\":\"name\",\"type\":\"bytes\"},{\"name\":\"families\",\"type\":[{\"type\":\"array\",\"items\":\"AFamilyDescriptor\"},\"null\"]},{\"name\":\"maxFileSize\",\"type\":[\"long\",\"null\"]},{\"name\":\"memStoreFlushSize\",\"type\":[\"long\",\"null\"]},{\"name\":\"rootRegion\",\"type\":[\"boolean\",\"null\"]},{\"name\":\"metaRegion\",\"type\":[\"boolean\",\"null\"]},{\"name\":\"metaTable\",\"type\":[\"boolean\",\"null\"]},{\"name\":\"readOnly\",\"type\":[\"boolean\",\"null\"]},{\"name\":\"deferredLogFlush\",\"type\":[\"boolean\",\"null\"]}]},{\"type\":\"record\",\"name\":\"AColumn\",\"fields\":[{\"name\":\"family\",\"type\":\"bytes\"},{\"name\":\"qualifier\",\"type\":[\"bytes\",\"null\"]}]},{\"type\":\"record\",\"name\":\"ATimeRange\",\"fields\":[{\"name\":\"minStamp\",\"type\":\"long\"},{\"name\":\"maxStamp\",\"type\":\"long\"}]},{\"type\":\"record\",\"name\":\"AGet\",\"fields\":[{\"name\":\"row\",\"type\":\"bytes\"},{\"name\":\"columns\",\"type\":[{\"type\":\"array\",\"items\":\"AColumn\"},\"null\"]},{\"name\":\"timestamp\",\"type\":[\"long\",\"null\"]},{\"name\":\"timerange\",\"type\":[\"ATimeRange\",\"null\"]},{\"name\":\"maxVersions\",\"type\":[\"int\",\"null\"]}]},{\"type\":\"record\",\"name\":\"AResultEntry\",\"fields\":[{\"name\":\"family\",\"type\":\"bytes\"},{\"name\":\"qualifier\",\"type\":\"bytes\"},{\"name\":\"value\",\"type\":\"bytes\"},{\"name\":\"timestamp\",\"type\":\"long\"}]},{\"type\":\"record\",\"name\":\"AResult\",\"fields\":[{\"name\":\"row\",\"type\":\"bytes\"},{\"name\":\"entries\",\"type\":{\"type\":\"array\",\"items\":\"AResultEntry\"}}]},{\"type\":\"record\",\"name\":\"AColumnValue\",\"fields\":[{\"name\":\"family\",\"type\":\"bytes\"},{\"name\":\"qualifier\",\"type\":\"bytes\"},{\"name\":\"value\",\"type\":\"bytes\"},{\"name\":\"timestamp\",\"type\":[\"long\",\"null\"]}]},{\"type\":\"record\",\"name\":\"APut\",\"fields\":[{\"name\":\"row\",\"type\":\"bytes\"},{\"name\":\"columnValues\",\"type\":{\"type\":\"array\",\"items\":\"AColumnValue\"}}]},{\"type\":\"record\",\"name\":\"ADelete\",\"fields\":[{\"name\":\"row\",\"type\":\"bytes\"},{\"name\":\"columns\",\"type\":[{\"type\":\"array\",\"items\":\"AColumn\"},\"null\"]}]},{\"type\":\"record\",\"name\":\"AScan\",\"fields\":[{\"name\":\"startRow\",\"type\":[\"bytes\",\"null\"]},{\"name\":\"stopRow\",\"type\":[\"bytes\",\"null\"]},{\"name\":\"columns\",\"type\":[{\"type\":\"array\",\"items\":\"AColumn\"},\"null\"]},{\"name\":\"timestamp\",\"type\":[\"long\",\"null\"]},{\"name\":\"timerange\",\"type\":[\"ATimeRange\",\"null\"]},{\"name\":\"maxVersions\",\"type\":[\"int\",\"null\"]}]},{\"type\":\"error\",\"name\":\"AIOError\",\"fields\":[{\"name\":\"message\",\"type\":\"string\"}]},{\"type\":\"error\",\"name\":\"AIllegalArgument\",\"fields\":[{\"name\":\"message\",\"type\":\"string\"}]},{\"type\":\"error\",\"name\":\"ATableExists\",\"fields\":[{\"name\":\"message\",\"type\":\"string\"}]},{\"type\":\"error\",\"name\":\"AMasterNotRunning\",\"fields\":[{\"name\":\"message\",\"type\":\"string\"}]}],\"messages\":{\"getHBaseVersion\":{\"request\":[],\"response\":\"string\",\"errors\":[\"AIOError\"]},\"getClusterStatus\":{\"request\":[],\"response\":\"AClusterStatus\",\"errors\":[\"AIOError\"]},\"listTables\":{\"request\":[],\"response\":{\"type\":\"array\",\"items\":\"ATableDescriptor\"},\"errors\":[\"AIOError\"]},\"describeTable\":{\"request\":[{\"name\":\"table\",\"type\":\"bytes\"}],\"response\":\"ATableDescriptor\",\"errors\":[\"AIOError\"]},\"isTableEnabled\":{\"request\":[{\"name\":\"table\",\"type\":\"bytes\"}],\"response\":\"boolean\",\"errors\":[\"AIOError\"]},\"tableExists\":{\"request\":[{\"name\":\"table\",\"type\":\"bytes\"}],\"response\":\"boolean\",\"errors\":[\"AIOError\"]},\"describeFamily\":{\"request\":[{\"name\":\"table\",\"type\":\"bytes\"},{\"name\":\"family\",\"type\":\"bytes\"}],\"response\":\"AFamilyDescriptor\",\"errors\":[\"AIOError\"]},\"createTable\":{\"request\":[{\"name\":\"table\",\"type\":\"ATableDescriptor\"}],\"response\":\"null\",\"errors\":[\"AIOError\",\"AIllegalArgument\",\"ATableExists\",\"AMasterNotRunning\"]},\"deleteTable\":{\"request\":[{\"name\":\"table\",\"type\":\"bytes\"}],\"response\":\"null\",\"errors\":[\"AIOError\"]},\"modifyTable\":{\"request\":[{\"name\":\"table\",\"type\":\"bytes\"},{\"name\":\"tableDescriptor\",\"type\":\"ATableDescriptor\"}],\"response\":\"null\",\"errors\":[\"AIOError\"]},\"enableTable\":{\"request\":[{\"name\":\"table\",\"type\":\"bytes\"}],\"response\":\"null\",\"errors\":[\"AIOError\"]},\"disableTable\":{\"request\":[{\"name\":\"table\",\"type\":\"bytes\"}],\"response\":\"null\",\"errors\":[\"AIOError\"]},\"flush\":{\"request\":[{\"name\":\"table\",\"type\":\"bytes\"}],\"response\":\"null\",\"errors\":[\"AIOError\"]},\"split\":{\"request\":[{\"name\":\"table\",\"type\":\"bytes\"}],\"response\":\"null\",\"errors\":[\"AIOError\"]},\"addFamily\":{\"request\":[{\"name\":\"table\",\"type\":\"bytes\"},{\"name\":\"family\",\"type\":\"AFamilyDescriptor\"}],\"response\":\"null\",\"errors\":[\"AIOError\"]},\"deleteFamily\":{\"request\":[{\"name\":\"table\",\"type\":\"bytes\"},{\"name\":\"family\",\"type\":\"bytes\"}],\"response\":\"null\",\"errors\":[\"AIOError\"]},\"modifyFamily\":{\"request\":[{\"name\":\"table\",\"type\":\"bytes\"},{\"name\":\"familyName\",\"type\":\"bytes\"},{\"name\":\"familyDescriptor\",\"type\":\"AFamilyDescriptor\"}],\"response\":\"null\",\"errors\":[\"AIOError\"]},\"get\":{\"request\":[{\"name\":\"table\",\"type\":\"bytes\"},{\"name\":\"get\",\"type\":\"AGet\"}],\"response\":\"AResult\",\"errors\":[\"AIOError\"]},\"exists\":{\"request\":[{\"name\":\"table\",\"type\":\"bytes\"},{\"name\":\"get\",\"type\":\"AGet\"}],\"response\":\"boolean\",\"errors\":[\"AIOError\"]},\"put\":{\"request\":[{\"name\":\"table\",\"type\":\"bytes\"},{\"name\":\"put\",\"type\":\"APut\"}],\"response\":\"null\",\"errors\":[\"AIOError\"]},\"delete\":{\"request\":[{\"name\":\"table\",\"type\":\"bytes\"},{\"name\":\"delete\",\"type\":\"ADelete\"}],\"response\":\"null\",\"errors\":[\"AIOError\"]},\"incrementColumnValue\":{\"request\":[{\"name\":\"table\",\"type\":\"bytes\"},{\"name\":\"row\",\"type\":\"bytes\"},{\"name\":\"family\",\"type\":\"bytes\"},{\"name\":\"qualifier\",\"type\":\"bytes\"},{\"name\":\"amount\",\"type\":\"long\"},{\"name\":\"writeToWAL\",\"type\":\"boolean\"}],\"response\":\"long\",\"errors\":[\"AIOError\"]},\"scannerOpen\":{\"request\":[{\"name\":\"table\",\"type\":\"bytes\"},{\"name\":\"scan\",\"type\":\"AScan\"}],\"response\":\"int\",\"errors\":[\"AIOError\"]},\"scannerClose\":{\"request\":[{\"name\":\"scannerId\",\"type\":\"int\"}],\"response\":\"null\",\"errors\":[\"AIOError\",\"AIllegalArgument\"]},\"scannerGetRows\":{\"request\":[{\"name\":\"scannerId\",\"type\":\"int\"},{\"name\":\"numberOfRows\",\"type\":\"int\"}],\"response\":{\"type\":\"array\",\"items\":\"AResult\"},\"errors\":[\"AIOError\",\"AIllegalArgument\"]}}}"
argument_list|)
decl_stmt|;
name|java
operator|.
name|lang
operator|.
name|CharSequence
name|getHBaseVersion
parameter_list|()
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AClusterStatus
name|getClusterStatus
parameter_list|()
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|java
operator|.
name|util
operator|.
name|List
argument_list|<
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|ATableDescriptor
argument_list|>
name|listTables
parameter_list|()
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|ATableDescriptor
name|describeTable
parameter_list|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|table
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|boolean
name|isTableEnabled
parameter_list|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|table
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|boolean
name|tableExists
parameter_list|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|table
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AFamilyDescriptor
name|describeFamily
parameter_list|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|table
parameter_list|,
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|family
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|java
operator|.
name|lang
operator|.
name|Void
name|createTable
parameter_list|(
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|ATableDescriptor
name|table
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIllegalArgument
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|ATableExists
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AMasterNotRunning
function_decl|;
name|java
operator|.
name|lang
operator|.
name|Void
name|deleteTable
parameter_list|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|table
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|java
operator|.
name|lang
operator|.
name|Void
name|modifyTable
parameter_list|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|table
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|ATableDescriptor
name|tableDescriptor
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|java
operator|.
name|lang
operator|.
name|Void
name|enableTable
parameter_list|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|table
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|java
operator|.
name|lang
operator|.
name|Void
name|disableTable
parameter_list|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|table
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|java
operator|.
name|lang
operator|.
name|Void
name|flush
parameter_list|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|table
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|java
operator|.
name|lang
operator|.
name|Void
name|split
parameter_list|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|table
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|java
operator|.
name|lang
operator|.
name|Void
name|addFamily
parameter_list|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|table
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AFamilyDescriptor
name|family
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|java
operator|.
name|lang
operator|.
name|Void
name|deleteFamily
parameter_list|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|table
parameter_list|,
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|family
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|java
operator|.
name|lang
operator|.
name|Void
name|modifyFamily
parameter_list|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|table
parameter_list|,
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|familyName
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AFamilyDescriptor
name|familyDescriptor
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AResult
name|get
parameter_list|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|table
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AGet
name|get
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|boolean
name|exists
parameter_list|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|table
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AGet
name|get
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|java
operator|.
name|lang
operator|.
name|Void
name|put
parameter_list|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|table
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|APut
name|put
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|java
operator|.
name|lang
operator|.
name|Void
name|delete
parameter_list|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|table
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|ADelete
name|delete
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|long
name|incrementColumnValue
parameter_list|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|table
parameter_list|,
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|row
parameter_list|,
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|family
parameter_list|,
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|qualifier
parameter_list|,
name|long
name|amount
parameter_list|,
name|boolean
name|writeToWAL
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|int
name|scannerOpen
parameter_list|(
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
name|table
parameter_list|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AScan
name|scan
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
function_decl|;
name|java
operator|.
name|lang
operator|.
name|Void
name|scannerClose
parameter_list|(
name|int
name|scannerId
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIllegalArgument
function_decl|;
name|java
operator|.
name|util
operator|.
name|List
argument_list|<
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AResult
argument_list|>
name|scannerGetRows
parameter_list|(
name|int
name|scannerId
parameter_list|,
name|int
name|numberOfRows
parameter_list|)
throws|throws
name|org
operator|.
name|apache
operator|.
name|avro
operator|.
name|ipc
operator|.
name|AvroRemoteException
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIOError
throws|,
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|avro
operator|.
name|generated
operator|.
name|AIllegalArgument
function_decl|;
block|}
end_interface

end_unit

