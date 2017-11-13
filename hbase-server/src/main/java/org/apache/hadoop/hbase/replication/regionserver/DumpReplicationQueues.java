begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|replication
operator|.
name|regionserver
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|FileNotFoundException
import|;
end_import

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
name|LinkedList
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
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Queue
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|stream
operator|.
name|Collectors
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
name|fs
operator|.
name|FileStatus
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
name|fs
operator|.
name|FileSystem
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
name|Abortable
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
name|HBaseConfiguration
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
name|ServerName
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
name|Stoppable
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
name|Admin
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
name|ClusterConnection
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
name|ConnectionFactory
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
name|HBaseAdmin
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
name|replication
operator|.
name|TableCFs
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
name|WALLink
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
name|procedure2
operator|.
name|util
operator|.
name|StringUtils
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
name|ReplicationPeerDescription
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
name|replication
operator|.
name|ReplicationQueueInfo
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
name|ReplicationQueues
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
name|ReplicationQueuesClient
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
name|ReplicationQueuesClientArguments
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
name|ReplicationTracker
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
name|hbase
operator|.
name|zookeeper
operator|.
name|ZKWatcher
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
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
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|util
operator|.
name|concurrent
operator|.
name|AtomicLongMap
import|;
end_import

begin_comment
comment|/**  * Provides information about the existing states of replication, replication peers and queues.  *  * Usage: hbase org.apache.hadoop.hbase.replication.regionserver.DumpReplicationQueues [args]  * Arguments: --distributed    Polls each RS to dump information about the queue  *            --hdfs           Reports HDFS usage by the replication queues (note: can be overestimated).  */
end_comment

begin_class
specifier|public
class|class
name|DumpReplicationQueues
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
name|DumpReplicationQueues
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|deadRegionServers
decl_stmt|;
specifier|private
name|List
argument_list|<
name|String
argument_list|>
name|deletedQueues
decl_stmt|;
specifier|private
name|AtomicLongMap
argument_list|<
name|String
argument_list|>
name|peersQueueSize
decl_stmt|;
specifier|private
name|long
name|totalSizeOfWALs
decl_stmt|;
specifier|private
name|long
name|numWalsNotFound
decl_stmt|;
specifier|public
name|DumpReplicationQueues
parameter_list|()
block|{
name|deadRegionServers
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|deletedQueues
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
expr_stmt|;
name|peersQueueSize
operator|=
name|AtomicLongMap
operator|.
name|create
argument_list|()
expr_stmt|;
name|totalSizeOfWALs
operator|=
literal|0
expr_stmt|;
name|numWalsNotFound
operator|=
literal|0
expr_stmt|;
block|}
specifier|static
class|class
name|DumpOptions
block|{
name|boolean
name|hdfs
init|=
literal|false
decl_stmt|;
name|boolean
name|distributed
init|=
literal|false
decl_stmt|;
specifier|public
name|DumpOptions
parameter_list|()
block|{     }
specifier|public
name|DumpOptions
parameter_list|(
name|DumpOptions
name|that
parameter_list|)
block|{
name|this
operator|.
name|hdfs
operator|=
name|that
operator|.
name|hdfs
expr_stmt|;
name|this
operator|.
name|distributed
operator|=
name|that
operator|.
name|distributed
expr_stmt|;
block|}
name|boolean
name|isHdfs
parameter_list|()
block|{
return|return
name|hdfs
return|;
block|}
name|boolean
name|isDistributed
parameter_list|()
block|{
return|return
name|distributed
return|;
block|}
name|void
name|setHdfs
parameter_list|(
name|boolean
name|hdfs
parameter_list|)
block|{
name|this
operator|.
name|hdfs
operator|=
name|hdfs
expr_stmt|;
block|}
name|void
name|setDistributed
parameter_list|(
name|boolean
name|distributed
parameter_list|)
block|{
name|this
operator|.
name|distributed
operator|=
name|distributed
expr_stmt|;
block|}
block|}
specifier|static
name|DumpOptions
name|parseOpts
parameter_list|(
name|Queue
argument_list|<
name|String
argument_list|>
name|args
parameter_list|)
block|{
name|DumpOptions
name|opts
init|=
operator|new
name|DumpOptions
argument_list|()
decl_stmt|;
name|String
name|cmd
init|=
literal|null
decl_stmt|;
while|while
condition|(
operator|(
name|cmd
operator|=
name|args
operator|.
name|poll
argument_list|()
operator|)
operator|!=
literal|null
condition|)
block|{
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
name|equals
argument_list|(
literal|"--h"
argument_list|)
operator|||
name|cmd
operator|.
name|equals
argument_list|(
literal|"--help"
argument_list|)
condition|)
block|{
comment|// place item back onto queue so that caller knows parsing was incomplete
name|args
operator|.
name|add
argument_list|(
name|cmd
argument_list|)
expr_stmt|;
break|break;
block|}
specifier|final
name|String
name|hdfs
init|=
literal|"--hdfs"
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
name|hdfs
argument_list|)
condition|)
block|{
name|opts
operator|.
name|setHdfs
argument_list|(
literal|true
argument_list|)
expr_stmt|;
continue|continue;
block|}
specifier|final
name|String
name|distributed
init|=
literal|"--distributed"
decl_stmt|;
if|if
condition|(
name|cmd
operator|.
name|equals
argument_list|(
name|distributed
argument_list|)
condition|)
block|{
name|opts
operator|.
name|setDistributed
argument_list|(
literal|true
argument_list|)
expr_stmt|;
continue|continue;
block|}
else|else
block|{
name|printUsageAndExit
argument_list|(
literal|"ERROR: Unrecognized option/command: "
operator|+
name|cmd
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
comment|// check that --distributed is present when --hdfs is in the arguments
if|if
condition|(
operator|!
name|opts
operator|.
name|isDistributed
argument_list|()
operator|&&
name|opts
operator|.
name|isHdfs
argument_list|()
condition|)
block|{
name|printUsageAndExit
argument_list|(
literal|"ERROR: --hdfs option can only be used with --distributed: "
operator|+
name|cmd
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
block|}
block|}
return|return
name|opts
return|;
block|}
comment|/**    * Main    *    * @param args    * @throws Exception    */
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
name|Configuration
name|conf
init|=
name|HBaseConfiguration
operator|.
name|create
argument_list|()
decl_stmt|;
name|int
name|ret
init|=
name|ToolRunner
operator|.
name|run
argument_list|(
name|conf
argument_list|,
operator|new
name|DumpReplicationQueues
argument_list|()
argument_list|,
name|args
argument_list|)
decl_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|ret
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
name|int
name|errCode
init|=
operator|-
literal|1
decl_stmt|;
name|LinkedList
argument_list|<
name|String
argument_list|>
name|argv
init|=
operator|new
name|LinkedList
argument_list|<>
argument_list|()
decl_stmt|;
name|argv
operator|.
name|addAll
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|args
argument_list|)
argument_list|)
expr_stmt|;
name|DumpOptions
name|opts
init|=
name|parseOpts
argument_list|(
name|argv
argument_list|)
decl_stmt|;
comment|// args remaining, print help and exit
if|if
condition|(
operator|!
name|argv
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|errCode
operator|=
literal|0
expr_stmt|;
name|printUsage
argument_list|()
expr_stmt|;
return|return
name|errCode
return|;
block|}
return|return
name|dumpReplicationQueues
argument_list|(
name|opts
argument_list|)
return|;
block|}
specifier|protected
name|void
name|printUsage
parameter_list|()
block|{
name|printUsage
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|printUsage
parameter_list|(
specifier|final
name|String
name|message
parameter_list|)
block|{
name|printUsage
argument_list|(
name|DumpReplicationQueues
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|message
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|printUsage
parameter_list|(
specifier|final
name|String
name|className
parameter_list|,
specifier|final
name|String
name|message
parameter_list|)
block|{
if|if
condition|(
name|message
operator|!=
literal|null
operator|&&
name|message
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
name|message
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"Usage: hbase "
operator|+
name|className
operator|+
literal|" \\"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|"<OPTIONS> [-D<property=value>]*"
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
literal|"General Options:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" -h|--h|--help  Show this help and exit."
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" --distributed  Poll each RS and print its own replication queue. "
operator|+
literal|"Default only polls ZooKeeper"
argument_list|)
expr_stmt|;
name|System
operator|.
name|err
operator|.
name|println
argument_list|(
literal|" --hdfs         Use HDFS to calculate usage of WALs by replication."
operator|+
literal|" It could be overestimated if replicating to multiple peers."
operator|+
literal|" --distributed flag is also needed."
argument_list|)
expr_stmt|;
block|}
specifier|protected
specifier|static
name|void
name|printUsageAndExit
parameter_list|(
specifier|final
name|String
name|message
parameter_list|,
specifier|final
name|int
name|exitCode
parameter_list|)
block|{
name|printUsage
argument_list|(
name|message
argument_list|)
expr_stmt|;
name|System
operator|.
name|exit
argument_list|(
name|exitCode
argument_list|)
expr_stmt|;
block|}
specifier|private
name|int
name|dumpReplicationQueues
parameter_list|(
name|DumpOptions
name|opts
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|getConf
argument_list|()
decl_stmt|;
name|HBaseAdmin
operator|.
name|available
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|ClusterConnection
name|connection
init|=
operator|(
name|ClusterConnection
operator|)
name|ConnectionFactory
operator|.
name|createConnection
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|Admin
name|admin
init|=
name|connection
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
name|ZKWatcher
name|zkw
init|=
operator|new
name|ZKWatcher
argument_list|(
name|conf
argument_list|,
literal|"DumpReplicationQueues"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
operator|new
name|WarnOnlyAbortable
argument_list|()
argument_list|,
literal|true
argument_list|)
decl_stmt|;
try|try
block|{
comment|// Our zk watcher
name|LOG
operator|.
name|info
argument_list|(
literal|"Our Quorum: "
operator|+
name|zkw
operator|.
name|getQuorum
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|TableCFs
argument_list|>
name|replicatedTableCFs
init|=
name|admin
operator|.
name|listReplicatedTableCFs
argument_list|()
decl_stmt|;
if|if
condition|(
name|replicatedTableCFs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"No tables with a configured replication peer were found."
argument_list|)
expr_stmt|;
return|return
operator|(
literal|0
operator|)
return|;
block|}
else|else
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Replicated Tables: "
operator|+
name|replicatedTableCFs
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|ReplicationPeerDescription
argument_list|>
name|peers
init|=
name|admin
operator|.
name|listReplicationPeers
argument_list|()
decl_stmt|;
if|if
condition|(
name|peers
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Replication is enabled but no peer configuration was found."
argument_list|)
expr_stmt|;
block|}
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
literal|"Dumping replication peers and configurations:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|dumpPeersState
argument_list|(
name|peers
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|opts
operator|.
name|isDistributed
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Found [--distributed], will poll each RegionServer."
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|peerIds
init|=
name|peers
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
parameter_list|(
name|peer
parameter_list|)
lambda|->
name|peer
operator|.
name|getPeerId
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toSet
argument_list|()
argument_list|)
decl_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|dumpQueues
argument_list|(
name|connection
argument_list|,
name|zkw
argument_list|,
name|peerIds
argument_list|,
name|opts
operator|.
name|isHdfs
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|dumpReplicationSummary
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
comment|// use ZK instead
name|System
operator|.
name|out
operator|.
name|print
argument_list|(
literal|"Dumping replication znodes via ZooKeeper:"
argument_list|)
expr_stmt|;
name|System
operator|.
name|out
operator|.
name|println
argument_list|(
name|ZKUtil
operator|.
name|getReplicationZnodesDump
argument_list|(
name|zkw
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|(
literal|0
operator|)
return|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
return|return
operator|(
operator|-
literal|1
operator|)
return|;
block|}
finally|finally
block|{
name|zkw
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
specifier|public
name|String
name|dumpReplicationSummary
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
operator|!
name|deletedQueues
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"Found "
operator|+
name|deletedQueues
operator|.
name|size
argument_list|()
operator|+
literal|" deleted queues"
operator|+
literal|", run hbck -fixReplication in order to remove the deleted replication queues\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|deletedQueue
range|:
name|deletedQueues
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"    "
operator|+
name|deletedQueue
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|deadRegionServers
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"Found "
operator|+
name|deadRegionServers
operator|.
name|size
argument_list|()
operator|+
literal|" dead regionservers"
operator|+
literal|", restart one regionserver to transfer the queues of dead regionservers\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|deadRs
range|:
name|deadRegionServers
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"    "
operator|+
name|deadRs
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
block|}
if|if
condition|(
operator|!
name|peersQueueSize
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"Dumping all peers's number of WALs in replication queue\n"
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|String
argument_list|,
name|Long
argument_list|>
name|entry
range|:
name|peersQueueSize
operator|.
name|asMap
argument_list|()
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"    PeerId: "
operator|+
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|" , sizeOfLogQueue: "
operator|+
name|entry
operator|.
name|getValue
argument_list|()
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"    Total size of WALs on HDFS: "
operator|+
name|StringUtils
operator|.
name|humanSize
argument_list|(
name|totalSizeOfWALs
argument_list|)
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
if|if
condition|(
name|numWalsNotFound
operator|>
literal|0
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"    ERROR: There are "
operator|+
name|numWalsNotFound
operator|+
literal|" WALs not found!!!\n"
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|String
name|dumpPeersState
parameter_list|(
name|List
argument_list|<
name|ReplicationPeerDescription
argument_list|>
name|peers
parameter_list|)
throws|throws
name|Exception
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|String
argument_list|>
name|currentConf
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|ReplicationPeerDescription
name|peer
range|:
name|peers
control|)
block|{
name|ReplicationPeerConfig
name|peerConfig
init|=
name|peer
operator|.
name|getPeerConfig
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"Peer: "
operator|+
name|peer
operator|.
name|getPeerId
argument_list|()
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"    "
operator|+
literal|"State: "
operator|+
operator|(
name|peer
operator|.
name|isEnabled
argument_list|()
condition|?
literal|"ENABLED"
else|:
literal|"DISABLED"
operator|)
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"    "
operator|+
literal|"Cluster Name: "
operator|+
name|peerConfig
operator|.
name|getClusterKey
argument_list|()
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"    "
operator|+
literal|"Replication Endpoint: "
operator|+
name|peerConfig
operator|.
name|getReplicationEndpointImpl
argument_list|()
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|currentConf
operator|=
name|peerConfig
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
comment|// Only show when we have a custom configuration for the peer
if|if
condition|(
name|currentConf
operator|.
name|size
argument_list|()
operator|>
literal|1
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"    "
operator|+
literal|"Peer Configuration: "
operator|+
name|currentConf
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"    "
operator|+
literal|"Peer Table CFs: "
operator|+
name|peerConfig
operator|.
name|getTableCFsMap
argument_list|()
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"    "
operator|+
literal|"Peer Namespaces: "
operator|+
name|peerConfig
operator|.
name|getNamespaces
argument_list|()
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|public
name|String
name|dumpQueues
parameter_list|(
name|ClusterConnection
name|connection
parameter_list|,
name|ZKWatcher
name|zkw
parameter_list|,
name|Set
argument_list|<
name|String
argument_list|>
name|peerIds
parameter_list|,
name|boolean
name|hdfs
parameter_list|)
throws|throws
name|Exception
block|{
name|ReplicationQueuesClient
name|queuesClient
decl_stmt|;
name|ReplicationPeers
name|replicationPeers
decl_stmt|;
name|ReplicationQueues
name|replicationQueues
decl_stmt|;
name|ReplicationTracker
name|replicationTracker
decl_stmt|;
name|ReplicationQueuesClientArguments
name|replicationArgs
init|=
operator|new
name|ReplicationQueuesClientArguments
argument_list|(
name|getConf
argument_list|()
argument_list|,
operator|new
name|WarnOnlyAbortable
argument_list|()
argument_list|,
name|zkw
argument_list|)
decl_stmt|;
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|queuesClient
operator|=
name|ReplicationFactory
operator|.
name|getReplicationQueuesClient
argument_list|(
name|replicationArgs
argument_list|)
expr_stmt|;
name|queuesClient
operator|.
name|init
argument_list|()
expr_stmt|;
name|replicationQueues
operator|=
name|ReplicationFactory
operator|.
name|getReplicationQueues
argument_list|(
name|replicationArgs
argument_list|)
expr_stmt|;
name|replicationPeers
operator|=
name|ReplicationFactory
operator|.
name|getReplicationPeers
argument_list|(
name|zkw
argument_list|,
name|getConf
argument_list|()
argument_list|,
name|queuesClient
argument_list|,
name|connection
argument_list|)
expr_stmt|;
name|replicationTracker
operator|=
name|ReplicationFactory
operator|.
name|getReplicationTracker
argument_list|(
name|zkw
argument_list|,
name|replicationPeers
argument_list|,
name|getConf
argument_list|()
argument_list|,
operator|new
name|WarnOnlyAbortable
argument_list|()
argument_list|,
operator|new
name|WarnOnlyStoppable
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|liveRegionServers
init|=
name|replicationTracker
operator|.
name|getListOfRegionServers
argument_list|()
decl_stmt|;
comment|// Loops each peer on each RS and dumps the queues
try|try
block|{
name|List
argument_list|<
name|String
argument_list|>
name|regionservers
init|=
name|queuesClient
operator|.
name|getListOfReplicators
argument_list|()
decl_stmt|;
if|if
condition|(
name|regionservers
operator|==
literal|null
operator|||
name|regionservers
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
for|for
control|(
name|String
name|regionserver
range|:
name|regionservers
control|)
block|{
name|List
argument_list|<
name|String
argument_list|>
name|queueIds
init|=
name|queuesClient
operator|.
name|getAllQueues
argument_list|(
name|regionserver
argument_list|)
decl_stmt|;
name|replicationQueues
operator|.
name|init
argument_list|(
name|regionserver
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|liveRegionServers
operator|.
name|contains
argument_list|(
name|regionserver
argument_list|)
condition|)
block|{
name|deadRegionServers
operator|.
name|add
argument_list|(
name|regionserver
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|String
name|queueId
range|:
name|queueIds
control|)
block|{
name|ReplicationQueueInfo
name|queueInfo
init|=
operator|new
name|ReplicationQueueInfo
argument_list|(
name|queueId
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|wals
init|=
name|queuesClient
operator|.
name|getLogsInQueue
argument_list|(
name|regionserver
argument_list|,
name|queueId
argument_list|)
decl_stmt|;
if|if
condition|(
operator|!
name|peerIds
operator|.
name|contains
argument_list|(
name|queueInfo
operator|.
name|getPeerId
argument_list|()
argument_list|)
condition|)
block|{
name|deletedQueues
operator|.
name|add
argument_list|(
name|regionserver
operator|+
literal|"/"
operator|+
name|queueId
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|formatQueue
argument_list|(
name|regionserver
argument_list|,
name|replicationQueues
argument_list|,
name|queueInfo
argument_list|,
name|queueId
argument_list|,
name|wals
argument_list|,
literal|true
argument_list|,
name|hdfs
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|formatQueue
argument_list|(
name|regionserver
argument_list|,
name|replicationQueues
argument_list|,
name|queueInfo
argument_list|,
name|queueId
argument_list|,
name|wals
argument_list|,
literal|false
argument_list|,
name|hdfs
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
catch|catch
parameter_list|(
name|KeeperException
name|ke
parameter_list|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
name|ke
argument_list|)
throw|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
specifier|private
name|String
name|formatQueue
parameter_list|(
name|String
name|regionserver
parameter_list|,
name|ReplicationQueues
name|replicationQueues
parameter_list|,
name|ReplicationQueueInfo
name|queueInfo
parameter_list|,
name|String
name|queueId
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|wals
parameter_list|,
name|boolean
name|isDeleted
parameter_list|,
name|boolean
name|hdfs
parameter_list|)
throws|throws
name|Exception
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|deadServers
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"Dumping replication queue info for RegionServer: ["
operator|+
name|regionserver
operator|+
literal|"]"
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"    Queue znode: "
operator|+
name|queueId
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"    PeerID: "
operator|+
name|queueInfo
operator|.
name|getPeerId
argument_list|()
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"    Recovered: "
operator|+
name|queueInfo
operator|.
name|isQueueRecovered
argument_list|()
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|deadServers
operator|=
name|queueInfo
operator|.
name|getDeadRegionServers
argument_list|()
expr_stmt|;
if|if
condition|(
name|deadServers
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"    No dead RegionServers found in this queue."
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
literal|"    Dead RegionServers: "
operator|+
name|deadServers
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"    Was deleted: "
operator|+
name|isDeleted
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"    Number of WALs in replication queue: "
operator|+
name|wals
operator|.
name|size
argument_list|()
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
name|peersQueueSize
operator|.
name|addAndGet
argument_list|(
name|queueInfo
operator|.
name|getPeerId
argument_list|()
argument_list|,
name|wals
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|String
name|wal
range|:
name|wals
control|)
block|{
name|long
name|position
init|=
name|replicationQueues
operator|.
name|getLogPosition
argument_list|(
name|queueInfo
operator|.
name|getPeerId
argument_list|()
argument_list|,
name|wal
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"    Replication position for "
operator|+
name|wal
operator|+
literal|": "
operator|+
operator|(
name|position
operator|>
literal|0
condition|?
name|position
else|:
literal|"0"
operator|+
literal|" (not started or nothing to replicate)"
operator|)
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|hdfs
condition|)
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|getConf
argument_list|()
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"    Total size of WALs on HDFS for this queue: "
operator|+
name|StringUtils
operator|.
name|humanSize
argument_list|(
name|getTotalWALSize
argument_list|(
name|fs
argument_list|,
name|wals
argument_list|,
name|regionserver
argument_list|)
argument_list|)
operator|+
literal|"\n"
argument_list|)
expr_stmt|;
block|}
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|/**    *  return total size in bytes from a list of WALs    */
specifier|private
name|long
name|getTotalWALSize
parameter_list|(
name|FileSystem
name|fs
parameter_list|,
name|List
argument_list|<
name|String
argument_list|>
name|wals
parameter_list|,
name|String
name|server
parameter_list|)
throws|throws
name|IOException
block|{
name|int
name|size
init|=
literal|0
decl_stmt|;
name|FileStatus
name|fileStatus
decl_stmt|;
for|for
control|(
name|String
name|wal
range|:
name|wals
control|)
block|{
try|try
block|{
name|fileStatus
operator|=
operator|(
operator|new
name|WALLink
argument_list|(
name|getConf
argument_list|()
argument_list|,
name|server
argument_list|,
name|wal
argument_list|)
operator|)
operator|.
name|getFileStatus
argument_list|(
name|fs
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
name|e
operator|instanceof
name|FileNotFoundException
condition|)
block|{
name|numWalsNotFound
operator|++
expr_stmt|;
name|LOG
operator|.
name|warn
argument_list|(
literal|"WAL "
operator|+
name|wal
operator|+
literal|" couldn't be found, skipping"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Can't get file status of WAL "
operator|+
name|wal
operator|+
literal|", skipping"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
continue|continue;
block|}
name|size
operator|+=
name|fileStatus
operator|.
name|getLen
argument_list|()
expr_stmt|;
block|}
name|totalSizeOfWALs
operator|+=
name|size
expr_stmt|;
return|return
name|size
return|;
block|}
specifier|private
specifier|static
class|class
name|WarnOnlyAbortable
implements|implements
name|Abortable
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
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"DumpReplicationQueue received abort, ignoring.  Reason: "
operator|+
name|why
argument_list|)
expr_stmt|;
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
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
specifier|private
specifier|static
class|class
name|WarnOnlyStoppable
implements|implements
name|Stoppable
block|{
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"DumpReplicationQueue received stop, ignoring.  Reason: "
operator|+
name|why
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

