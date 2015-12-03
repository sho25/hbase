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
name|java
operator|.
name|net
operator|.
name|InetSocketAddress
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashMap
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
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ConcurrentSkipListMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|executor
operator|.
name|ExecutorService
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
name|fs
operator|.
name|HFileSystem
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
name|ipc
operator|.
name|RpcServerInterface
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
name|master
operator|.
name|TableLockManager
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
name|master
operator|.
name|TableLockManager
operator|.
name|NullTableLockManager
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
name|protobuf
operator|.
name|generated
operator|.
name|RegionServerStatusProtos
operator|.
name|RegionStateTransition
operator|.
name|TransitionCode
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
name|quotas
operator|.
name|RegionServerQuotaManager
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
name|regionserver
operator|.
name|CompactionRequestor
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
name|regionserver
operator|.
name|FlushRequester
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
name|regionserver
operator|.
name|HeapMemoryManager
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
name|regionserver
operator|.
name|Leases
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
name|regionserver
operator|.
name|Region
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
name|regionserver
operator|.
name|RegionServerAccounting
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
name|regionserver
operator|.
name|RegionServerServices
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
name|regionserver
operator|.
name|ServerNonceManager
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
name|wal
operator|.
name|WAL
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
name|MetaTableLocator
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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Service
import|;
end_import

begin_comment
comment|/**  * Basic mock region server services.  Should only be instantiated by HBaseTestingUtility.b  */
end_comment

begin_class
specifier|public
class|class
name|MockRegionServerServices
implements|implements
name|RegionServerServices
block|{
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MockRegionServerServices
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|Region
argument_list|>
name|regions
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Region
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|ConcurrentSkipListMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Boolean
argument_list|>
name|rit
init|=
operator|new
name|ConcurrentSkipListMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Boolean
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
specifier|private
name|HFileSystem
name|hfs
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|Configuration
name|conf
decl_stmt|;
specifier|private
name|ZooKeeperWatcher
name|zkw
init|=
literal|null
decl_stmt|;
specifier|private
name|ServerName
name|serverName
init|=
literal|null
decl_stmt|;
specifier|private
name|RpcServerInterface
name|rpcServer
init|=
literal|null
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|abortRequested
decl_stmt|;
specifier|private
specifier|volatile
name|boolean
name|stopping
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|AtomicBoolean
name|running
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|true
argument_list|)
decl_stmt|;
name|MockRegionServerServices
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|)
block|{
name|this
argument_list|(
name|zkw
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
name|MockRegionServerServices
parameter_list|(
name|ZooKeeperWatcher
name|zkw
parameter_list|,
name|ServerName
name|serverName
parameter_list|)
block|{
name|this
operator|.
name|zkw
operator|=
name|zkw
expr_stmt|;
name|this
operator|.
name|serverName
operator|=
name|serverName
expr_stmt|;
name|this
operator|.
name|conf
operator|=
operator|(
name|zkw
operator|==
literal|null
condition|?
operator|new
name|Configuration
argument_list|()
else|:
name|zkw
operator|.
name|getConfiguration
argument_list|()
operator|)
expr_stmt|;
block|}
name|MockRegionServerServices
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MockRegionServerServices
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|removeFromOnlineRegions
parameter_list|(
name|Region
name|r
parameter_list|,
name|ServerName
name|destination
parameter_list|)
block|{
return|return
name|this
operator|.
name|regions
operator|.
name|remove
argument_list|(
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|)
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Region
name|getFromOnlineRegions
parameter_list|(
name|String
name|encodedRegionName
parameter_list|)
block|{
return|return
name|this
operator|.
name|regions
operator|.
name|get
argument_list|(
name|encodedRegionName
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|Region
argument_list|>
name|getOnlineRegions
parameter_list|(
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Set
argument_list|<
name|TableName
argument_list|>
name|getOnlineTables
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addToOnlineRegions
parameter_list|(
name|Region
name|r
parameter_list|)
block|{
name|this
operator|.
name|regions
operator|.
name|put
argument_list|(
name|r
operator|.
name|getRegionInfo
argument_list|()
operator|.
name|getEncodedName
argument_list|()
argument_list|,
name|r
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postOpenDeployTasks
parameter_list|(
name|Region
name|r
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|IOException
block|{
name|addToOnlineRegions
argument_list|(
name|r
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|postOpenDeployTasks
parameter_list|(
name|PostOpenDeployContext
name|context
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|IOException
block|{
name|addToOnlineRegions
argument_list|(
name|context
operator|.
name|getRegion
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopping
parameter_list|()
block|{
return|return
name|this
operator|.
name|stopping
return|;
block|}
annotation|@
name|Override
specifier|public
name|RpcServerInterface
name|getRpcServer
parameter_list|()
block|{
return|return
name|rpcServer
return|;
block|}
specifier|public
name|void
name|setRpcServer
parameter_list|(
name|RpcServerInterface
name|rpc
parameter_list|)
block|{
name|this
operator|.
name|rpcServer
operator|=
name|rpc
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ConcurrentSkipListMap
argument_list|<
name|byte
index|[]
argument_list|,
name|Boolean
argument_list|>
name|getRegionsInTransitionInRS
parameter_list|()
block|{
return|return
name|rit
return|;
block|}
annotation|@
name|Override
specifier|public
name|FlushRequester
name|getFlushRequester
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|CompactionRequestor
name|getCompactionRequester
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ClusterConnection
name|getConnection
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|MetaTableLocator
name|getMetaTableLocator
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ZooKeeperWatcher
name|getZooKeeper
parameter_list|()
block|{
return|return
name|zkw
return|;
block|}
annotation|@
name|Override
specifier|public
name|CoordinatedStateManager
name|getCoordinatedStateManager
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|RegionServerAccounting
name|getRegionServerAccounting
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableLockManager
name|getTableLockManager
parameter_list|()
block|{
return|return
operator|new
name|NullTableLockManager
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|RegionServerQuotaManager
name|getRegionServerQuotaManager
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|this
operator|.
name|serverName
return|;
block|}
annotation|@
name|Override
specifier|public
name|Configuration
name|getConfiguration
parameter_list|()
block|{
return|return
name|conf
return|;
block|}
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
name|this
operator|.
name|abortRequested
operator|=
literal|true
expr_stmt|;
name|stop
argument_list|(
name|why
argument_list|)
expr_stmt|;
block|}
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
name|this
operator|.
name|stopping
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|running
operator|.
name|compareAndSet
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Shutting down due to request '"
operator|+
name|why
operator|+
literal|"'"
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
operator|!
operator|(
name|running
operator|.
name|get
argument_list|()
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isAborted
parameter_list|()
block|{
return|return
name|this
operator|.
name|abortRequested
return|;
block|}
annotation|@
name|Override
specifier|public
name|HFileSystem
name|getFileSystem
parameter_list|()
block|{
return|return
name|this
operator|.
name|hfs
return|;
block|}
specifier|public
name|void
name|setFileSystem
parameter_list|(
name|FileSystem
name|hfs
parameter_list|)
block|{
name|this
operator|.
name|hfs
operator|=
operator|(
name|HFileSystem
operator|)
name|hfs
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Leases
name|getLeases
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|WAL
name|getWAL
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ExecutorService
name|getExecutorService
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ChoreService
name|getChoreService
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|updateRegionFavoredNodesMapping
parameter_list|(
name|String
name|encodedRegionName
parameter_list|,
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|ServerName
argument_list|>
name|favoredNodes
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|InetSocketAddress
index|[]
name|getFavoredNodesForRegion
parameter_list|(
name|String
name|encodedRegionName
parameter_list|)
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Region
argument_list|>
name|getRecoveringRegions
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerNonceManager
name|getNonceManager
parameter_list|()
block|{
comment|// TODO Auto-generated method stub
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|reportRegionStateTransition
parameter_list|(
name|TransitionCode
name|code
parameter_list|,
name|long
name|openSeqNum
parameter_list|,
name|HRegionInfo
modifier|...
name|hris
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|reportRegionStateTransition
parameter_list|(
name|TransitionCode
name|code
parameter_list|,
name|HRegionInfo
modifier|...
name|hris
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|reportRegionStateTransition
parameter_list|(
name|RegionStateTransitionContext
name|context
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|registerService
parameter_list|(
name|Service
name|service
parameter_list|)
block|{
comment|// TODO Auto-generated method stub
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|HeapMemoryManager
name|getHeapMemoryManager
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|getCompactionPressure
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
block|}
end_class

end_unit

