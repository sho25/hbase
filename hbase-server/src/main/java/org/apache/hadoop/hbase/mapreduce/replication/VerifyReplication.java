begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|mapreduce
operator|.
name|replication
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|conf
operator|.
name|Configuration
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
name|conf
operator|.
name|Configured
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
name|*
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
name|client
operator|.
name|HConnectable
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
name|client
operator|.
name|HConnection
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
name|client
operator|.
name|HConnectionManager
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
name|client
operator|.
name|HTable
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
name|client
operator|.
name|Put
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
name|client
operator|.
name|Result
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
name|client
operator|.
name|ResultScanner
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
name|client
operator|.
name|Scan
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
name|io
operator|.
name|ImmutableBytesWritable
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
name|mapreduce
operator|.
name|TableInputFormat
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
name|mapreduce
operator|.
name|TableMapReduceUtil
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
name|mapreduce
operator|.
name|TableMapper
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
name|replication
operator|.
name|ReplicationException
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
name|replication
operator|.
name|ReplicationFactory
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
name|replication
operator|.
name|ReplicationPeerZKImpl
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
name|replication
operator|.
name|ReplicationPeerConfig
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
name|replication
operator|.
name|ReplicationPeers
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
name|Pair
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
name|zookeeper
operator|.
name|ZooKeeperWatcher
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
name|zookeeper
operator|.
name|ZKUtil
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
name|mapreduce
operator|.
name|Job
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
name|mapreduce
operator|.
name|lib
operator|.
name|output
operator|.
name|NullOutputFormat
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
name|util
operator|.
name|Tool
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
name|util
operator|.
name|ToolRunner
import|;
end_import

begin_comment
comment|/**  * This map-only job compares the data from a local table with a remote one.  * Every cell is compared and must have exactly the same keys (even timestamp)  * as well as same value. It is possible to restrict the job by time range and  * families. The peer id that's provided must match the one given when the  * replication stream was setup.  *<p>  * Two counters are provided, Verifier.Counters.GOODROWS and BADROWS. The reason  * for a why a row is different is shown in the map's log.  */
end_comment

begin_class
specifier|public
class|class
name|VerifyReplication
extends|extends
name|Configured
implements|implements
name|Tool
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|VerifyReplication
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
specifier|final
specifier|static
name|String
name|NAME
init|=
literal|"verifyrep"
decl_stmt|;
specifier|static
name|long
name|startTime
init|=
literal|0
decl_stmt|;
specifier|static
name|long
name|endTime
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
specifier|static
name|int
name|versions
init|=
operator|-
literal|1
decl_stmt|;
specifier|static
name|String
name|tableName
init|=
literal|null
decl_stmt|;
specifier|static
name|String
name|families
init|=
literal|null
decl_stmt|;
specifier|static
name|String
name|peerId
init|=
literal|null
decl_stmt|;
comment|/**    * Map-only comparator for 2 tables    */
specifier|public
specifier|static
class|class
name|Verifier
extends|extends
name|TableMapper
argument_list|<
name|ImmutableBytesWritable
argument_list|,
name|Put
argument_list|>
block|{
specifier|public
specifier|static
enum|enum
name|Counters
block|{
name|GOODROWS
block|,
name|BADROWS
block|}
specifier|private
name|ResultScanner
name|replicatedScanner
decl_stmt|;
comment|/**      * Map method that compares every scanned row with the equivalent from      * a distant cluster.      * @param row  The current table row key.      * @param value  The columns.      * @param context  The current context.      * @throws IOException When something is broken with the data.      */
annotation|@
name|Override
specifier|public
name|void
name|map
parameter_list|(
name|ImmutableBytesWritable
name|row
parameter_list|,
specifier|final
name|Result
name|value
parameter_list|,
name|Context
name|context
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|replicatedScanner
operator|==
literal|null
condition|)
block|{
name|Configuration
name|conf
init|=
name|context
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
specifier|final
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setCaching
argument_list|(
name|conf
operator|.
name|getInt
argument_list|(
name|TableInputFormat
operator|.
name|SCAN_CACHEDROWS
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|long
name|startTime
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|NAME
operator|+
literal|".startTime"
argument_list|,
literal|0
argument_list|)
decl_stmt|;
name|long
name|endTime
init|=
name|conf
operator|.
name|getLong
argument_list|(
name|NAME
operator|+
literal|".endTime"
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|String
name|families
init|=
name|conf
operator|.
name|get
argument_list|(
name|NAME
operator|+
literal|".families"
argument_list|,
literal|null
argument_list|)
decl_stmt|;
if|if
condition|(
name|families
operator|!=
literal|null
condition|)
block|{
name|String
index|[]
name|fams
init|=
name|families
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|fam
range|:
name|fams
control|)
block|{
name|scan
operator|.
name|addFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|fam
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|scan
operator|.
name|setTimeRange
argument_list|(
name|startTime
argument_list|,
name|endTime
argument_list|)
expr_stmt|;
if|if
condition|(
name|versions
operator|>=
literal|0
condition|)
block|{
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|versions
argument_list|)
expr_stmt|;
block|}
name|HConnectionManager
operator|.
name|execute
argument_list|(
operator|new
name|HConnectable
argument_list|<
name|Void
argument_list|>
argument_list|(
name|conf
argument_list|)
block|{
annotation|@
name|Override
specifier|public
name|Void
name|connect
parameter_list|(
name|HConnection
name|conn
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|zkClusterKey
init|=
name|conf
operator|.
name|get
argument_list|(
name|NAME
operator|+
literal|".peerQuorumAddress"
argument_list|)
decl_stmt|;
name|Configuration
name|peerConf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|ZKUtil
operator|.
name|applyClusterKeyToConf
argument_list|(
name|peerConf
argument_list|,
name|zkClusterKey
argument_list|)
expr_stmt|;
name|HTable
name|replicatedTable
init|=
operator|new
name|HTable
argument_list|(
name|peerConf
argument_list|,
name|conf
operator|.
name|get
argument_list|(
name|NAME
operator|+
literal|".tableName"
argument_list|)
argument_list|)
decl_stmt|;
name|scan
operator|.
name|setStartRow
argument_list|(
name|value
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
name|replicatedScanner
operator|=
name|replicatedTable
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
argument_list|)
expr_stmt|;
block|}
name|Result
name|res
init|=
name|replicatedScanner
operator|.
name|next
argument_list|()
decl_stmt|;
try|try
block|{
name|Result
operator|.
name|compareResults
argument_list|(
name|value
argument_list|,
name|res
argument_list|)
expr_stmt|;
name|context
operator|.
name|getCounter
argument_list|(
name|Counters
operator|.
name|GOODROWS
argument_list|)
operator|.
name|increment
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Bad row"
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|context
operator|.
name|getCounter
argument_list|(
name|Counters
operator|.
name|BADROWS
argument_list|)
operator|.
name|increment
argument_list|(
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|cleanup
parameter_list|(
name|Context
name|context
parameter_list|)
block|{
if|if
condition|(
name|replicatedScanner
operator|!=
literal|null
condition|)
block|{
name|replicatedScanner
operator|.
name|close
argument_list|()
expr_stmt|;
name|replicatedScanner
operator|=
literal|null
expr_stmt|;
block|}
block|}
block|}
specifier|private
specifier|static
name|String
name|getPeerQuorumAddress
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|ZooKeeperWatcher
name|localZKW
init|=
literal|null
decl_stmt|;
name|ReplicationPeerZKImpl
name|peer
init|=
literal|null
decl_stmt|;
try|try
block|{
name|localZKW
operator|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|conf
argument_list|,
literal|"VerifyReplication"
argument_list|,
operator|new
name|Abortable
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|why
parameter_list|,
name|Throwable
name|e
parameter_list|)
block|{}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|ReplicationPeers
name|rp
init|=
name|ReplicationFactory
operator|.
name|getReplicationPeers
argument_list|(
name|localZKW
argument_list|,
name|conf
argument_list|,
name|localZKW
argument_list|)
decl_stmt|;
name|rp
operator|.
name|init
argument_list|()
expr_stmt|;
name|Pair
argument_list|<
name|ReplicationPeerConfig
argument_list|,
name|Configuration
argument_list|>
name|pair
init|=
name|rp
operator|.
name|getPeerConf
argument_list|(
name|peerId
argument_list|)
decl_stmt|;
if|if
condition|(
name|pair
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Couldn't get peer conf!"
argument_list|)
throw|;
block|}
name|Configuration
name|peerConf
init|=
name|rp
operator|.
name|getPeerConf
argument_list|(
name|peerId
argument_list|)
operator|.
name|getSecond
argument_list|()
decl_stmt|;
return|return
name|ZKUtil
operator|.
name|getZooKeeperClusterKey
argument_list|(
name|peerConf
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|ReplicationException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"An error occured while trying to connect to the remove peer cluster"
argument_list|,
name|e
argument_list|)
throw|;
block|}
finally|finally
block|{
if|if
condition|(
name|peer
operator|!=
literal|null
condition|)
block|{
name|peer
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
if|if
condition|(
name|localZKW
operator|!=
literal|null
condition|)
block|{
name|localZKW
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Sets up the actual job.    *    * @param conf  The current configuration.    * @param args  The command line parameters.    * @return The newly created job.    * @throws java.io.IOException When setting up the job fails.    */
specifier|public
specifier|static
name|Job
name|createSubmittableJob
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
operator|!
name|doCommandLine
argument_list|(
name|args
argument_list|)
condition|)
block|{
return|return
literal|null
return|;
block|}
if|if
condition|(
operator|!
name|conf
operator|.
name|getBoolean
argument_list|(
name|HConstants
operator|.
name|REPLICATION_ENABLE_KEY
argument_list|,
name|HConstants
operator|.
name|REPLICATION_ENABLE_DEFAULT
argument_list|)
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Replication needs to be enabled to verify it."
argument_list|)
throw|;
block|}
name|conf
operator|.
name|set
argument_list|(
name|NAME
operator|+
literal|".peerId"
argument_list|,
name|peerId
argument_list|)
expr_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|NAME
operator|+
literal|".tableName"
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|NAME
operator|+
literal|".startTime"
argument_list|,
name|startTime
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setLong
argument_list|(
name|NAME
operator|+
literal|".endTime"
argument_list|,
name|endTime
argument_list|)
expr_stmt|;
if|if
condition|(
name|families
operator|!=
literal|null
condition|)
block|{
name|conf
operator|.
name|set
argument_list|(
name|NAME
operator|+
literal|".families"
argument_list|,
name|families
argument_list|)
expr_stmt|;
block|}
name|String
name|peerQuorumAddress
init|=
name|getPeerQuorumAddress
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|NAME
operator|+
literal|".peerQuorumAddress"
argument_list|,
name|peerQuorumAddress
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Peer Quorum Address: "
operator|+
name|peerQuorumAddress
argument_list|)
expr_stmt|;
name|Job
name|job
init|=
operator|new
name|Job
argument_list|(
name|conf
argument_list|,
name|NAME
operator|+
literal|"_"
operator|+
name|tableName
argument_list|)
decl_stmt|;
name|job
operator|.
name|setJarByClass
argument_list|(
name|VerifyReplication
operator|.
name|class
argument_list|)
expr_stmt|;
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
name|scan
operator|.
name|setTimeRange
argument_list|(
name|startTime
argument_list|,
name|endTime
argument_list|)
expr_stmt|;
if|if
condition|(
name|versions
operator|>=
literal|0
condition|)
block|{
name|scan
operator|.
name|setMaxVersions
argument_list|(
name|versions
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|families
operator|!=
literal|null
condition|)
block|{
name|String
index|[]
name|fams
init|=
name|families
operator|.
name|split
argument_list|(
literal|","
argument_list|)
decl_stmt|;
for|for
control|(
name|String
name|fam
range|:
name|fams
control|)
block|{
name|scan
operator|.
name|addFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|fam
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
name|TableMapReduceUtil
operator|.
name|initTableMapperJob
argument_list|(
name|tableName
argument_list|,
name|scan
argument_list|,
name|Verifier
operator|.
name|class
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
name|job
argument_list|)
expr_stmt|;
comment|// Obtain the auth token from peer cluster
name|TableMapReduceUtil
operator|.
name|initCredentialsForCluster
argument_list|(
name|job
argument_list|,
name|peerQuorumAddress
argument_list|)
expr_stmt|;
name|job
operator|.
name|setOutputFormatClass
argument_list|(
name|NullOutputFormat
operator|.
name|class
argument_list|)
expr_stmt|;
name|job
operator|.
name|setNumReduceTasks
argument_list|(
literal|0
argument_list|)
expr_stmt|;
return|return
name|job
return|;
block|}
specifier|private
specifier|static
name|boolean
name|doCommandLine
parameter_list|(
specifier|final
name|String
index|[]
name|args
parameter_list|)
block|{
if|if
condition|(
name|args
operator|.
name|length
operator|<
literal|2
condition|)
block|{
name|printUsage
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
try|try
block|{
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|args
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|String
name|cmd
init|=
name|args
index|[
name|i
index|]
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
literal|"-h"
argument_list|)
operator|||
name|cmd
operator|.
name|startsWith
argument_list|(
literal|"--h"
argument_list|)
condition|)
block|{
name|printUsage
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
specifier|final
name|String
name|startTimeArgKey
init|=
literal|"--starttime="
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|startsWith
argument_list|(
name|startTimeArgKey
argument_list|)
condition|)
block|{
name|startTime
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|cmd
operator|.
name|substring
argument_list|(
name|startTimeArgKey
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
specifier|final
name|String
name|endTimeArgKey
init|=
literal|"--endtime="
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|startsWith
argument_list|(
name|endTimeArgKey
argument_list|)
condition|)
block|{
name|endTime
operator|=
name|Long
operator|.
name|parseLong
argument_list|(
name|cmd
operator|.
name|substring
argument_list|(
name|endTimeArgKey
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
specifier|final
name|String
name|versionsArgKey
init|=
literal|"--versions="
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|startsWith
argument_list|(
name|versionsArgKey
argument_list|)
condition|)
block|{
name|versions
operator|=
name|Integer
operator|.
name|parseInt
argument_list|(
name|cmd
operator|.
name|substring
argument_list|(
name|versionsArgKey
operator|.
name|length
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
continue|continue;
block|}
specifier|final
name|String
name|familiesArgKey
init|=
literal|"--families="
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|startsWith
argument_list|(
name|familiesArgKey
argument_list|)
condition|)
block|{
name|families
operator|=
name|cmd
operator|.
name|substring
argument_list|(
name|familiesArgKey
operator|.
name|length
argument_list|()
argument_list|)
expr_stmt|;
continue|continue;
block|}
if|if
condition|(
name|i
operator|==
name|args
operator|.
name|length
operator|-
literal|2
condition|)
block|{
name|peerId
operator|=
name|cmd
expr_stmt|;
block|}
if|if
condition|(
name|i
operator|==
name|args
operator|.
name|length
operator|-
literal|1
condition|)
block|{
name|tableName
operator|=
name|cmd
expr_stmt|;
block|}
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|e
parameter_list|)
block|{
name|e
operator|.
name|printStackTrace
argument_list|()
expr_stmt|;
name|printUsage
argument_list|(
literal|"Can't start because "
operator|+
name|e
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
block|}
comment|/*    * @param errorMsg Error message.  Can be null.    */
specifier|private
specifier|static
name|void
name|printUsage
parameter_list|(
specifier|final
name|String
name|errorMsg
parameter_list|)
block|{
if|if
condition|(
name|errorMsg
operator|!=
literal|null
operator|&&
name|errorMsg
operator|.
name|length
argument_list|()
operator|>
literal|0
condition|)
block|{
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"ERROR: "
operator|+
name|errorMsg
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Usage: verifyrep [--starttime=X]"
operator|+
literal|" [--stoptime=Y] [--families=A]<peerid><tablename>"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|()
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Options:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" starttime    beginning of the time range"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"              without endtime means from starttime to forever"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" endtime      end of the time range"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" versions     number of cell versions to verify"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" families     comma-separated list of families to copy"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|()
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Args:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" peerid       Id of the peer used for verification, must match the one given for replication"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" tablename    Name of the table to verify"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|()
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Examples:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" To verify the data replicated from TestTable for a 1 hour window with peer #5 "
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" $ bin/hbase "
operator|+
literal|"org.apache.hadoop.hbase.mapreduce.replication.VerifyReplication"
operator|+
literal|" --starttime=1265875194289 --endtime=1265878794289 5 TestTable "
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|run
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|this
operator|.
name|getConf
argument_list|()
decl_stmt|;
name|Job
name|job
init|=
name|createSubmittableJob
argument_list|(
name|conf
argument_list|,
name|args
argument_list|)
decl_stmt|;
if|if
condition|(
name|job
operator|!=
literal|null
condition|)
block|{
return|return
name|job
operator|.
name|waitForCompletion
argument_list|(
literal|true
argument_list|)
condition|?
literal|0
else|:
literal|1
return|;
block|}
return|return
literal|1
return|;
block|}
comment|/**    * Main entry point.    *    * @param args  The command line parameters.    * @throws Exception When running the job fails.    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|res
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|HBaseConfiguration
operator|.
name|create
argument_list|()
argument_list|,
operator|new
name|VerifyReplication
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|res
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

