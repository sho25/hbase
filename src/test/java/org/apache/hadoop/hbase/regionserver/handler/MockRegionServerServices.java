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
name|regionserver
operator|.
name|handler
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
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
name|catalog
operator|.
name|CatalogTracker
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
name|HBaseRpcMetrics
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
name|HRegion
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
name|wal
operator|.
name|HLog
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

begin_comment
comment|/**  * Basic mock region server services.  */
end_comment

begin_class
class|class
name|MockRegionServerServices
implements|implements
name|RegionServerServices
block|{
specifier|private
specifier|final
name|Map
argument_list|<
name|String
argument_list|,
name|HRegion
argument_list|>
name|regions
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|HRegion
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|boolean
name|stopping
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|rit
init|=
operator|new
name|HashSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|boolean
name|removeFromOnlineRegions
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
name|remove
argument_list|(
name|encodedRegionName
argument_list|)
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|HRegion
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
name|void
name|addToOnlineRegions
parameter_list|(
name|HRegion
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
name|HRegion
name|r
parameter_list|,
name|CatalogTracker
name|ct
parameter_list|,
name|boolean
name|daughter
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|IOException
block|{   }
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
name|HLog
name|getWAL
parameter_list|()
block|{
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|HBaseRpcMetrics
name|getRpcMetrics
parameter_list|()
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
name|byte
index|[]
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
name|CatalogTracker
name|getCatalogTracker
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
literal|null
return|;
block|}
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
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
literal|null
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
literal|null
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
comment|//no-op
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
comment|//no-op
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
end_class

end_unit

