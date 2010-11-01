begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|ipc
operator|.
name|HRegionInterface
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/**  * HConstants holds a bunch of HBase-related constants  */
end_comment

begin_class
specifier|public
specifier|final
class|class
name|HConstants
block|{
comment|/**    * Status codes used for return values of bulk operations.    */
specifier|public
enum|enum
name|OperationStatusCode
block|{
name|NOT_RUN
block|,
name|SUCCESS
block|,
name|BAD_FAMILY
block|,
name|FAILURE
block|;   }
comment|/** long constant for zero */
specifier|public
specifier|static
specifier|final
name|Long
name|ZERO_L
init|=
name|Long
operator|.
name|valueOf
argument_list|(
literal|0L
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|NINES
init|=
literal|"99999999999999"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ZEROES
init|=
literal|"00000000000000"
decl_stmt|;
comment|// For migration
comment|/** name of version file */
specifier|public
specifier|static
specifier|final
name|String
name|VERSION_FILE_NAME
init|=
literal|"hbase.version"
decl_stmt|;
comment|/**    * Current version of file system.    * Version 4 supports only one kind of bloom filter.    * Version 5 changes versions in catalog table regions.    * Version 6 enables blockcaching on catalog tables.    * Version 7 introduces hfile -- hbase 0.19 to 0.20..    */
comment|// public static final String FILE_SYSTEM_VERSION = "6";
specifier|public
specifier|static
specifier|final
name|String
name|FILE_SYSTEM_VERSION
init|=
literal|"7"
decl_stmt|;
comment|// Configuration parameters
comment|//TODO: Is having HBase homed on port 60k OK?
comment|/** Cluster is in distributed mode or not */
specifier|public
specifier|static
specifier|final
name|String
name|CLUSTER_DISTRIBUTED
init|=
literal|"hbase.cluster.distributed"
decl_stmt|;
comment|/** Cluster is standalone or pseudo-distributed */
specifier|public
specifier|static
specifier|final
name|String
name|CLUSTER_IS_LOCAL
init|=
literal|"false"
decl_stmt|;
comment|/** Cluster is fully-distributed */
specifier|public
specifier|static
specifier|final
name|String
name|CLUSTER_IS_DISTRIBUTED
init|=
literal|"true"
decl_stmt|;
comment|/** default host address */
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_HOST
init|=
literal|"0.0.0.0"
decl_stmt|;
comment|/** Parameter name for port master listens on. */
specifier|public
specifier|static
specifier|final
name|String
name|MASTER_PORT
init|=
literal|"hbase.master.port"
decl_stmt|;
comment|/** default port that the master listens on */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MASTER_PORT
init|=
literal|60000
decl_stmt|;
comment|/** default port for master web api */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_MASTER_INFOPORT
init|=
literal|60010
decl_stmt|;
comment|/** Parameter name for the master type being backup (waits for primary to go inactive). */
specifier|public
specifier|static
specifier|final
name|String
name|MASTER_TYPE_BACKUP
init|=
literal|"hbase.master.backup"
decl_stmt|;
comment|/** by default every master is a possible primary master unless the conf explicitly overrides it */
specifier|public
specifier|static
specifier|final
name|boolean
name|DEFAULT_MASTER_TYPE_BACKUP
init|=
literal|false
decl_stmt|;
comment|/** Name of ZooKeeper quorum configuration parameter. */
specifier|public
specifier|static
specifier|final
name|String
name|ZOOKEEPER_QUORUM
init|=
literal|"hbase.zookeeper.quorum"
decl_stmt|;
comment|/** Name of ZooKeeper config file in conf/ directory. */
specifier|public
specifier|static
specifier|final
name|String
name|ZOOKEEPER_CONFIG_NAME
init|=
literal|"zoo.cfg"
decl_stmt|;
comment|/** Parameter name for number of times to retry writes to ZooKeeper. */
specifier|public
specifier|static
specifier|final
name|String
name|ZOOKEEPER_RETRIES
init|=
literal|"zookeeper.retries"
decl_stmt|;
comment|/** Default number of times to retry writes to ZooKeeper. */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_ZOOKEEPER_RETRIES
init|=
literal|5
decl_stmt|;
comment|/** Parameter name for ZooKeeper pause between retries. In milliseconds. */
specifier|public
specifier|static
specifier|final
name|String
name|ZOOKEEPER_PAUSE
init|=
literal|"zookeeper.pause"
decl_stmt|;
comment|/** Default ZooKeeper pause value. In milliseconds. */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_ZOOKEEPER_PAUSE
init|=
literal|2
operator|*
literal|1000
decl_stmt|;
comment|/** default client port that the zookeeper listens on */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_ZOOKEPER_CLIENT_PORT
init|=
literal|2181
decl_stmt|;
comment|/** Parameter name for the root dir in ZK for this cluster */
specifier|public
specifier|static
specifier|final
name|String
name|ZOOKEEPER_ZNODE_PARENT
init|=
literal|"zookeeper.znode.parent"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_ZOOKEEPER_ZNODE_PARENT
init|=
literal|"/hbase"
decl_stmt|;
comment|/** Parameter name for port region server listens on. */
specifier|public
specifier|static
specifier|final
name|String
name|REGIONSERVER_PORT
init|=
literal|"hbase.regionserver.port"
decl_stmt|;
comment|/** Default port region server listens on. */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_REGIONSERVER_PORT
init|=
literal|60020
decl_stmt|;
comment|/** default port for region server web api */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_REGIONSERVER_INFOPORT
init|=
literal|60030
decl_stmt|;
comment|/** Parameter name for what region server interface to use. */
specifier|public
specifier|static
specifier|final
name|String
name|REGION_SERVER_CLASS
init|=
literal|"hbase.regionserver.class"
decl_stmt|;
comment|/** Parameter name for what region server implementation to use. */
specifier|public
specifier|static
specifier|final
name|String
name|REGION_SERVER_IMPL
init|=
literal|"hbase.regionserver.impl"
decl_stmt|;
comment|/** Default region server interface class name. */
specifier|public
specifier|static
specifier|final
name|String
name|DEFAULT_REGION_SERVER_CLASS
init|=
name|HRegionInterface
operator|.
name|class
operator|.
name|getName
argument_list|()
decl_stmt|;
comment|/** Parameter name for what master implementation to use. */
specifier|public
specifier|static
specifier|final
name|String
name|MASTER_IMPL
init|=
literal|"hbase.master.impl"
decl_stmt|;
comment|/** Parameter name for how often threads should wake up */
specifier|public
specifier|static
specifier|final
name|String
name|THREAD_WAKE_FREQUENCY
init|=
literal|"hbase.server.thread.wakefrequency"
decl_stmt|;
comment|/** Default value for thread wake frequency */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_THREAD_WAKE_FREQUENCY
init|=
literal|10
operator|*
literal|1000
decl_stmt|;
comment|/** Number of retries for the client */
specifier|public
specifier|static
specifier|final
name|String
name|NUM_CLIENT_RETRIES
init|=
literal|"hbase.client.retries.number"
decl_stmt|;
comment|/** Default number of retries for the client */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_NUM_CLIENT_RETRIES
init|=
literal|2
decl_stmt|;
comment|/** Parameter name for how often a region should should perform a major compaction */
specifier|public
specifier|static
specifier|final
name|String
name|MAJOR_COMPACTION_PERIOD
init|=
literal|"hbase.hregion.majorcompaction"
decl_stmt|;
comment|/** Parameter name for HBase instance root directory */
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_DIR
init|=
literal|"hbase.rootdir"
decl_stmt|;
comment|/** Used to construct the name of the log directory for a region server    * Use '.' as a special character to seperate the log files from table data */
specifier|public
specifier|static
specifier|final
name|String
name|HREGION_LOGDIR_NAME
init|=
literal|".logs"
decl_stmt|;
comment|/** Like the previous, but for old logs that are about to be deleted */
specifier|public
specifier|static
specifier|final
name|String
name|HREGION_OLDLOGDIR_NAME
init|=
literal|".oldlogs"
decl_stmt|;
comment|/** Used to construct the name of the compaction directory during compaction */
specifier|public
specifier|static
specifier|final
name|String
name|HREGION_COMPACTIONDIR_NAME
init|=
literal|"compaction.dir"
decl_stmt|;
comment|/** Default maximum file size */
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_MAX_FILE_SIZE
init|=
literal|256
operator|*
literal|1024
operator|*
literal|1024
decl_stmt|;
comment|/** Default size of a reservation block   */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_SIZE_RESERVATION_BLOCK
init|=
literal|1024
operator|*
literal|1024
operator|*
literal|5
decl_stmt|;
comment|/** Maximum value length, enforced on KeyValue construction */
specifier|public
specifier|static
specifier|final
name|int
name|MAXIMUM_VALUE_LENGTH
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
comment|// Always store the location of the root table's HRegion.
comment|// This HRegion is never split.
comment|// region name = table + startkey + regionid. This is the row key.
comment|// each row in the root and meta tables describes exactly 1 region
comment|// Do we ever need to know all the information that we are storing?
comment|// Note that the name of the root table starts with "-" and the name of the
comment|// meta table starts with "." Why? it's a trick. It turns out that when we
comment|// store region names in memory, we use a SortedMap. Since "-" sorts before
comment|// "." (and since no other table name can start with either of these
comment|// characters, the root region will always be the first entry in such a Map,
comment|// followed by all the meta regions (which will be ordered by their starting
comment|// row key as well), followed by all user tables. So when the Master is
comment|// choosing regions to assign, it will always choose the root region first,
comment|// followed by the meta regions, followed by user regions. Since the root
comment|// and meta regions always need to be on-line, this ensures that they will
comment|// be the first to be reassigned if the server(s) they are being served by
comment|// should go down.
comment|//
comment|// New stuff.  Making a slow transition.
comment|//
comment|/** The root table's name.*/
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|ROOT_TABLE_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"-ROOT-"
argument_list|)
decl_stmt|;
comment|/** The META table's name. */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|META_TABLE_NAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|".META."
argument_list|)
decl_stmt|;
comment|/** delimiter used between portions of a region name */
specifier|public
specifier|static
specifier|final
name|int
name|META_ROW_DELIMITER
init|=
literal|','
decl_stmt|;
comment|/** The catalog family as a string*/
specifier|public
specifier|static
specifier|final
name|String
name|CATALOG_FAMILY_STR
init|=
literal|"info"
decl_stmt|;
comment|/** The catalog family */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|CATALOG_FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|CATALOG_FAMILY_STR
argument_list|)
decl_stmt|;
comment|/** The regioninfo column qualifier */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|REGIONINFO_QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"regioninfo"
argument_list|)
decl_stmt|;
comment|/** The server column qualifier */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|SERVER_QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"server"
argument_list|)
decl_stmt|;
comment|/** The startcode column qualifier */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|STARTCODE_QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"serverstartcode"
argument_list|)
decl_stmt|;
comment|/** The lower-half split region column qualifier */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|SPLITA_QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"splitA"
argument_list|)
decl_stmt|;
comment|/** The upper-half split region column qualifier */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|SPLITB_QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"splitB"
argument_list|)
decl_stmt|;
comment|// Other constants
comment|/**    * An empty instance.    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|EMPTY_BYTE_ARRAY
init|=
operator|new
name|byte
index|[
literal|0
index|]
decl_stmt|;
comment|/**    * Used by scanners, etc when they want to start at the beginning of a region    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|EMPTY_START_ROW
init|=
name|EMPTY_BYTE_ARRAY
decl_stmt|;
comment|/**    * Last row in a table.    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|EMPTY_END_ROW
init|=
name|EMPTY_START_ROW
decl_stmt|;
comment|/**     * Used by scanners and others when they're trying to detect the end of a     * table     */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|LAST_ROW
init|=
name|EMPTY_BYTE_ARRAY
decl_stmt|;
comment|/**    * Max length a row can have because of the limitation in TFile.    */
specifier|public
specifier|static
specifier|final
name|int
name|MAX_ROW_LENGTH
init|=
name|Short
operator|.
name|MAX_VALUE
decl_stmt|;
comment|/** When we encode strings, we always specify UTF8 encoding */
specifier|public
specifier|static
specifier|final
name|String
name|UTF8_ENCODING
init|=
literal|"UTF-8"
decl_stmt|;
comment|/**    * Timestamp to use when we want to refer to the latest cell.    * This is the timestamp sent by clients when no timestamp is specified on    * commit.    */
specifier|public
specifier|static
specifier|final
name|long
name|LATEST_TIMESTAMP
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
comment|/**    * Timestamp to use when we want to refer to the oldest cell.    */
specifier|public
specifier|static
specifier|final
name|long
name|OLDEST_TIMESTAMP
init|=
name|Long
operator|.
name|MIN_VALUE
decl_stmt|;
comment|/**    * LATEST_TIMESTAMP in bytes form    */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|LATEST_TIMESTAMP_BYTES
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|LATEST_TIMESTAMP
argument_list|)
decl_stmt|;
comment|/**    * Define for 'return-all-versions'.    */
specifier|public
specifier|static
specifier|final
name|int
name|ALL_VERSIONS
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
comment|/**    * Unlimited time-to-live.    */
comment|//  public static final int FOREVER = -1;
specifier|public
specifier|static
specifier|final
name|int
name|FOREVER
init|=
name|Integer
operator|.
name|MAX_VALUE
decl_stmt|;
comment|/**    * Seconds in a week    */
specifier|public
specifier|static
specifier|final
name|int
name|WEEK_IN_SECONDS
init|=
literal|7
operator|*
literal|24
operator|*
literal|3600
decl_stmt|;
comment|//TODO: HBASE_CLIENT_RETRIES_NUMBER_KEY is only used by TestMigrate. Move it
comment|//      there.
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_CLIENT_RETRIES_NUMBER_KEY
init|=
literal|"hbase.client.retries.number"
decl_stmt|;
comment|//TODO: although the following are referenced widely to format strings for
comment|//      the shell. They really aren't a part of the public API. It would be
comment|//      nice if we could put them somewhere where they did not need to be
comment|//      public. They could have package visibility
specifier|public
specifier|static
specifier|final
name|String
name|NAME
init|=
literal|"NAME"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|VERSIONS
init|=
literal|"VERSIONS"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|IN_MEMORY
init|=
literal|"IN_MEMORY"
decl_stmt|;
comment|/**    * This is a retry backoff multiplier table similar to the BSD TCP syn    * backoff table, a bit more aggressive than simple exponential backoff.    */
specifier|public
specifier|static
name|int
name|RETRY_BACKOFF
index|[]
init|=
block|{
literal|1
block|,
literal|1
block|,
literal|1
block|,
literal|2
block|,
literal|2
block|,
literal|4
block|,
literal|4
block|,
literal|8
block|,
literal|16
block|,
literal|32
block|}
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|REGION_IMPL
init|=
literal|"hbase.hregion.impl"
decl_stmt|;
comment|/** modifyTable op for replacing the table descriptor */
specifier|public
specifier|static
enum|enum
name|Modify
block|{
name|CLOSE_REGION
block|,
name|TABLE_COMPACT
block|,
name|TABLE_FLUSH
block|,
name|TABLE_MAJOR_COMPACT
block|,
name|TABLE_SET_HTD
block|,
name|TABLE_SPLIT
block|}
comment|/**    * Scope tag for locally scoped data.    * This data will not be replicated.    */
specifier|public
specifier|static
specifier|final
name|int
name|REPLICATION_SCOPE_LOCAL
init|=
literal|0
decl_stmt|;
comment|/**    * Scope tag for globally scoped data.    * This data will be replicated to all peers.    */
specifier|public
specifier|static
specifier|final
name|int
name|REPLICATION_SCOPE_GLOBAL
init|=
literal|1
decl_stmt|;
comment|/**    * Default cluster ID, cannot be used to identify a cluster so a key with    * this value means it wasn't meant for replication.    */
specifier|public
specifier|static
specifier|final
name|byte
name|DEFAULT_CLUSTER_ID
init|=
literal|0
decl_stmt|;
comment|/**      * Parameter name for maximum number of bytes returned when calling a      * scanner's next method.      */
specifier|public
specifier|static
name|String
name|HBASE_CLIENT_SCANNER_MAX_RESULT_SIZE_KEY
init|=
literal|"hbase.client.scanner.max.result.size"
decl_stmt|;
comment|/**    * Maximum number of bytes returned when calling a scanner's next method.    * Note that when a single row is larger than this limit the row is still    * returned completely.    *    * The default value is unlimited.    */
specifier|public
specifier|static
name|long
name|DEFAULT_HBASE_CLIENT_SCANNER_MAX_RESULT_SIZE
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
comment|/**    * HRegion server lease period in milliseconds. Clients must report in within this period    * else they are considered dead. Unit measured in ms (milliseconds).    */
specifier|public
specifier|static
name|String
name|HBASE_REGIONSERVER_LEASE_PERIOD_KEY
init|=
literal|"hbase.regionserver.lease.period"
decl_stmt|;
comment|/**    * Default value of {@link #HBASE_REGIONSERVER_LEASE_PERIOD_KEY}.    */
specifier|public
specifier|static
name|long
name|DEFAULT_HBASE_REGIONSERVER_LEASE_PERIOD
init|=
literal|60000
decl_stmt|;
comment|/**    * timeout for each RPC    */
specifier|public
specifier|static
name|String
name|HBASE_RPC_TIMEOUT_KEY
init|=
literal|"hbase.rpc.timeout"
decl_stmt|;
comment|/**    * Default value of {@link #HBASE_RPC_TIMEOUT_KEY}    */
specifier|public
specifier|static
name|int
name|DEFAULT_HBASE_RPC_TIMEOUT
init|=
literal|60000
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|REPLICATION_ENABLE_KEY
init|=
literal|"hbase.replication"
decl_stmt|;
specifier|private
name|HConstants
parameter_list|()
block|{
comment|// Can't be instantiated with this ctor.
block|}
block|}
end_class

end_unit

