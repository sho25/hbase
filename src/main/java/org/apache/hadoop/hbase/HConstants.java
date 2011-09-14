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

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|UUID
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|regex
operator|.
name|Pattern
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
comment|/** Parameter name for the client port that the zookeeper listens on */
specifier|public
specifier|static
specifier|final
name|String
name|ZOOKEEPER_CLIENT_PORT
init|=
literal|"hbase.zookeeper.property.clientPort"
decl_stmt|;
comment|/** Default client port that the zookeeper listens on */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_ZOOKEPER_CLIENT_PORT
init|=
literal|2181
decl_stmt|;
comment|/** Parameter name for the wait time for the recoverable zookeeper */
specifier|public
specifier|static
specifier|final
name|String
name|ZOOKEEPER_RECOVERABLE_WAITTIME
init|=
literal|"hbase.zookeeper.recoverable.waittime"
decl_stmt|;
comment|/** Default wait time for the recoverable zookeeper */
specifier|public
specifier|static
specifier|final
name|long
name|DEFAULT_ZOOKEPER_RECOVERABLE_WAITIME
init|=
literal|10000
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
comment|/** Parameter name for the limit on concurrent client-side zookeeper connections */
specifier|public
specifier|static
specifier|final
name|String
name|ZOOKEEPER_MAX_CLIENT_CNXNS
init|=
literal|"hbase.zookeeper.property.maxClientCnxns"
decl_stmt|;
comment|/** Default limit on concurrent client-side zookeeper connections */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_ZOOKEPER_MAX_CLIENT_CNXNS
init|=
literal|30
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
comment|/** Parameter name for HBase client IPC pool type */
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_CLIENT_IPC_POOL_TYPE
init|=
literal|"hbase.client.ipc.pool.type"
decl_stmt|;
comment|/** Parameter name for HBase client IPC pool size */
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_CLIENT_IPC_POOL_SIZE
init|=
literal|"hbase.client.ipc.pool.size"
decl_stmt|;
comment|/** Parameter name for HBase client operation timeout, which overrides RPC timeout */
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_CLIENT_OPERATION_TIMEOUT
init|=
literal|"hbase.client.operation.timeout"
decl_stmt|;
comment|/** Default HBase client operation timeout, which is tantamount to a blocking call */
specifier|public
specifier|static
specifier|final
name|int
name|DEFAULT_HBASE_CLIENT_OPERATION_TIMEOUT
init|=
name|Integer
operator|.
name|MAX_VALUE
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
comment|/** Used to construct the name of the splitlog directory for a region server */
specifier|public
specifier|static
specifier|final
name|String
name|SPLIT_LOGDIR_NAME
init|=
literal|"splitlog"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CORRUPT_DIR_NAME
init|=
literal|".corrupt"
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
comment|/** The file name used to store HTD in HDFS  */
specifier|public
specifier|static
specifier|final
name|String
name|TABLEINFO_NAME
init|=
literal|".tableinfo"
decl_stmt|;
comment|/** The metaupdated column qualifier */
specifier|public
specifier|static
specifier|final
name|byte
index|[]
name|META_MIGRATION_QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"metamigrated"
argument_list|)
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
comment|/** name of the file for unique cluster ID */
specifier|public
specifier|static
specifier|final
name|String
name|CLUSTER_ID_FILE_NAME
init|=
literal|"hbase.id"
decl_stmt|;
comment|/** Configuration key storing the cluster ID */
specifier|public
specifier|static
specifier|final
name|String
name|CLUSTER_ID
init|=
literal|"hbase.cluster.id"
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
name|UUID
name|DEFAULT_CLUSTER_ID
init|=
operator|new
name|UUID
argument_list|(
literal|0L
argument_list|,
literal|0L
argument_list|)
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
comment|/**    * Parameter name for client pause value, used mostly as value to wait    * before running a retry of a failed get, region lookup, etc.    */
specifier|public
specifier|static
name|String
name|HBASE_CLIENT_PAUSE
init|=
literal|"hbase.client.pause"
decl_stmt|;
comment|/**    * Default value of {@link #HBASE_CLIENT_PAUSE}.    */
specifier|public
specifier|static
name|long
name|DEFAULT_HBASE_CLIENT_PAUSE
init|=
literal|1000
decl_stmt|;
comment|/**    * Parameter name for maximum retries, used as maximum for all retryable    * operations such as fetching of the root region from root region server,    * getting a cell's value, starting a row update, etc.    */
specifier|public
specifier|static
name|String
name|HBASE_CLIENT_RETRIES_NUMBER
init|=
literal|"hbase.client.retries.number"
decl_stmt|;
comment|/**    * Default value of {@link #HBASE_CLIENT_RETRIES_NUMBER}.    */
specifier|public
specifier|static
name|int
name|DEFAULT_HBASE_CLIENT_RETRIES_NUMBER
init|=
literal|10
decl_stmt|;
comment|/**    * Parameter name for maximum attempts, used to limit the number of times the    * client will try to obtain the proxy for a given region server.    */
specifier|public
specifier|static
name|String
name|HBASE_CLIENT_RPC_MAXATTEMPTS
init|=
literal|"hbase.client.rpc.maxattempts"
decl_stmt|;
comment|/**    * Default value of {@link #HBASE_CLIENT_RPC_MAXATTEMPTS}.    */
specifier|public
specifier|static
name|int
name|DEFAULT_HBASE_CLIENT_RPC_MAXATTEMPTS
init|=
literal|1
decl_stmt|;
comment|/**    * Parameter name for client prefetch limit, used as the maximum number of regions    * info that will be prefetched.    */
specifier|public
specifier|static
name|String
name|HBASE_CLIENT_PREFETCH_LIMIT
init|=
literal|"hbase.client.prefetch.limit"
decl_stmt|;
comment|/**    * Default value of {@link #HBASE_CLIENT_PREFETCH_LIMIT}.    */
specifier|public
specifier|static
name|int
name|DEFAULT_HBASE_CLIENT_PREFETCH_LIMIT
init|=
literal|10
decl_stmt|;
comment|/**    * Parameter name for number of rows that will be fetched when calling next on    * a scanner if it is not served from memory. Higher caching values will    * enable faster scanners but will eat up more memory and some calls of next    * may take longer and longer times when the cache is empty.    */
specifier|public
specifier|static
name|String
name|HBASE_META_SCANNER_CACHING
init|=
literal|"hbase.meta.scanner.caching"
decl_stmt|;
comment|/**    * Default value of {@link #HBASE_META_SCANNER_CACHING}.    */
specifier|public
specifier|static
name|int
name|DEFAULT_HBASE_META_SCANNER_CACHING
init|=
literal|100
decl_stmt|;
comment|/**    * Parameter name for unique identifier for this {@link Configuration}    * instance. If there are two or more {@link Configuration} instances that,    * for all intents and purposes, are the same except for their instance ids,    * then they will not be able to share the same {@link Connection} instance.    * On the other hand, even if the instance ids are the same, it could result    * in non-shared {@link Connection} instances if some of the other connection    * parameters differ.    */
specifier|public
specifier|static
name|String
name|HBASE_CLIENT_INSTANCE_ID
init|=
literal|"hbase.client.instance.id"
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
comment|/** HBCK special code name used as server name when manipulating ZK nodes */
specifier|public
specifier|static
specifier|final
name|String
name|HBCK_CODE_NAME
init|=
literal|"HBCKServerName"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|ServerName
name|HBCK_CODE_SERVERNAME
init|=
operator|new
name|ServerName
argument_list|(
name|HBCK_CODE_NAME
argument_list|,
operator|-
literal|1
argument_list|,
operator|-
literal|1L
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|KEY_FOR_HOSTNAME_SEEN_BY_MASTER
init|=
literal|"hbase.regionserver.hostname.seen.by.master"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_MASTER_LOGCLEANER_PLUGINS
init|=
literal|"hbase.master.logcleaner.plugins"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|HBASE_REGION_SPLIT_POLICY_KEY
init|=
literal|"hbase.regionserver.region.split.policy"
decl_stmt|;
comment|/*     * Minimum percentage of free heap necessary for a successful cluster startup.     */
specifier|public
specifier|static
specifier|final
name|float
name|HBASE_CLUSTER_MINIMUM_MEMORY_THRESHOLD
init|=
literal|0.2f
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|List
argument_list|<
name|String
argument_list|>
name|HBASE_NON_USER_TABLE_DIRS
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|String
index|[]
block|{
name|HREGION_LOGDIR_NAME
block|,
name|HREGION_OLDLOGDIR_NAME
block|,
name|CORRUPT_DIR_NAME
block|,
name|Bytes
operator|.
name|toString
argument_list|(
name|META_TABLE_NAME
argument_list|)
block|,
name|Bytes
operator|.
name|toString
argument_list|(
name|ROOT_TABLE_NAME
argument_list|)
block|,
name|SPLIT_LOGDIR_NAME
block|}
argument_list|)
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Pattern
name|CP_HTD_ATTR_KEY_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"coprocessor\\$([0-9]+)"
argument_list|,
name|Pattern
operator|.
name|CASE_INSENSITIVE
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Pattern
name|CP_HTD_ATTR_VALUE_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"([^\\|]*)\\|([^\\|]+)\\|[\\s]*([\\d]*)[\\s]*(\\|.*)?"
argument_list|)
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CP_HTD_ATTR_VALUE_PARAM_KEY_PATTERN
init|=
literal|"[^=,]+"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|CP_HTD_ATTR_VALUE_PARAM_VALUE_PATTERN
init|=
literal|"[^,]+"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|Pattern
name|CP_HTD_ATTR_VALUE_PARAM_PATTERN
init|=
name|Pattern
operator|.
name|compile
argument_list|(
literal|"("
operator|+
name|CP_HTD_ATTR_VALUE_PARAM_KEY_PATTERN
operator|+
literal|")=("
operator|+
name|CP_HTD_ATTR_VALUE_PARAM_VALUE_PATTERN
operator|+
literal|"),?"
argument_list|)
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

