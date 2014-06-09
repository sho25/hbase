begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|UUID
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
name|ReplicationQueues
name|rq
parameter_list|,
name|ReplicationPeers
name|rp
parameter_list|,
name|Stoppable
name|stopper
parameter_list|,
name|String
name|peerClusterId
parameter_list|,
name|UUID
name|clusterId
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
block|{    }
annotation|@
name|Override
specifier|public
name|void
name|terminate
parameter_list|(
name|String
name|reason
parameter_list|)
block|{    }
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
block|{    }
annotation|@
name|Override
specifier|public
name|String
name|getPeerClusterZnode
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
name|getPeerClusterId
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
name|getStats
parameter_list|()
block|{
return|return
literal|""
return|;
block|}
block|}
end_class

end_unit

