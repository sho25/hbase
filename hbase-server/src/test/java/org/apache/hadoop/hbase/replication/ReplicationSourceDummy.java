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
name|fs
operator|.
name|Path
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
name|Server
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
name|TableName
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
name|regionserver
operator|.
name|MetricsSource
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
name|regionserver
operator|.
name|ReplicationSourceInterface
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
name|regionserver
operator|.
name|ReplicationSourceManager
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
name|regionserver
operator|.
name|WALFileLengthProvider
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
name|wal
operator|.
name|WAL
operator|.
name|Entry
import|;
end_import

begin_comment
comment|/**  * Source that does nothing at all, helpful to test ReplicationSourceManager  */
end_comment

begin_class
specifier|public
class|class
name|ReplicationSourceDummy
implements|implements
name|ReplicationSourceInterface
block|{
name|ReplicationSourceManager
name|manager
decl_stmt|;
name|String
name|peerClusterId
decl_stmt|;
name|Path
name|currentPath
decl_stmt|;
name|MetricsSource
name|metrics
decl_stmt|;
name|WALFileLengthProvider
name|walFileLengthProvider
decl_stmt|;
name|AtomicBoolean
name|startup
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|FileSystem
name|fs
parameter_list|,
name|ReplicationSourceManager
name|manager
parameter_list|,
name|ReplicationQueueStorage
name|rq
parameter_list|,
name|ReplicationPeer
name|rp
parameter_list|,
name|Server
name|server
parameter_list|,
name|String
name|peerClusterId
parameter_list|,
name|UUID
name|clusterId
parameter_list|,
name|WALFileLengthProvider
name|walFileLengthProvider
parameter_list|,
name|MetricsSource
name|metrics
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|manager
operator|=
name|manager
expr_stmt|;
name|this
operator|.
name|peerClusterId
operator|=
name|peerClusterId
expr_stmt|;
name|this
operator|.
name|metrics
operator|=
name|metrics
expr_stmt|;
name|this
operator|.
name|walFileLengthProvider
operator|=
name|walFileLengthProvider
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|enqueueLog
parameter_list|(
name|Path
name|log
parameter_list|)
block|{
name|this
operator|.
name|currentPath
operator|=
name|log
expr_stmt|;
name|metrics
operator|.
name|incrSizeOfLogQueue
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Path
name|getCurrentPath
parameter_list|()
block|{
return|return
name|this
operator|.
name|currentPath
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|startup
parameter_list|()
block|{
name|startup
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|public
name|boolean
name|isStartup
parameter_list|()
block|{
return|return
name|startup
operator|.
name|get
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|terminate
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
name|terminate
argument_list|(
name|reason
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|terminate
parameter_list|(
name|String
name|reason
parameter_list|,
name|Exception
name|e
parameter_list|)
block|{
name|this
operator|.
name|metrics
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getQueueId
parameter_list|()
block|{
return|return
name|peerClusterId
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getPeerId
parameter_list|()
block|{
name|String
index|[]
name|parts
init|=
name|peerClusterId
operator|.
name|split
argument_list|(
literal|"-"
argument_list|,
literal|2
argument_list|)
decl_stmt|;
return|return
name|parts
operator|.
name|length
operator|!=
literal|1
condition|?
name|parts
index|[
literal|0
index|]
else|:
name|peerClusterId
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getStats
parameter_list|()
block|{
return|return
literal|""
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|addHFileRefs
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|byte
index|[]
name|family
parameter_list|,
name|List
argument_list|<
name|Pair
argument_list|<
name|Path
argument_list|,
name|Path
argument_list|>
argument_list|>
name|files
parameter_list|)
throws|throws
name|ReplicationException
block|{
return|return;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isPeerEnabled
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isSourceActive
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|MetricsSource
name|getSourceMetrics
parameter_list|()
block|{
return|return
name|metrics
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicationEndpoint
name|getReplicationEndpoint
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReplicationSourceManager
name|getSourceManager
parameter_list|()
block|{
return|return
name|manager
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|tryThrottle
parameter_list|(
name|int
name|batchSize
parameter_list|)
throws|throws
name|InterruptedException
block|{   }
annotation|@
name|Override
specifier|public
name|void
name|postShipEdits
parameter_list|(
name|List
argument_list|<
name|Entry
argument_list|>
name|entries
parameter_list|,
name|int
name|batchSize
parameter_list|)
block|{   }
annotation|@
name|Override
specifier|public
name|WALFileLengthProvider
name|getWALFileLengthProvider
parameter_list|()
block|{
return|return
name|walFileLengthProvider
return|;
block|}
annotation|@
name|Override
specifier|public
name|ServerName
name|getServerWALsBelongTo
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
block|}
end_class

end_unit

