begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|hbase
operator|.
name|HBaseTestingUtility
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
name|HConstants
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
name|HRegionInfo
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
name|HTableDescriptor
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
name|ZooKeeperConnectionException
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
name|ZKAssign
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
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
operator|.
name|NodeExistsException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|AfterClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|BeforeClass
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
import|;
end_import

begin_comment
comment|/**  * Test of the {@link OpenRegionHandler}.  */
end_comment

begin_class
specifier|public
class|class
name|TestOpenRegionHandler
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
name|TestOpenRegionHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|HTU
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|before
parameter_list|()
throws|throws
name|Exception
block|{
name|HTU
operator|.
name|startMiniZKCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|after
parameter_list|()
throws|throws
name|IOException
block|{
name|HTU
operator|.
name|shutdownMiniZKCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Basic mock Server    */
specifier|static
class|class
name|MockServer
implements|implements
name|Server
block|{
name|boolean
name|stopped
init|=
literal|false
decl_stmt|;
specifier|final
specifier|static
name|ServerName
name|NAME
init|=
operator|new
name|ServerName
argument_list|(
literal|"MockServer"
argument_list|,
literal|123
argument_list|,
operator|-
literal|1
argument_list|)
decl_stmt|;
specifier|final
name|ZooKeeperWatcher
name|zk
decl_stmt|;
name|MockServer
parameter_list|()
throws|throws
name|ZooKeeperConnectionException
throws|,
name|IOException
block|{
name|this
operator|.
name|zk
operator|=
operator|new
name|ZooKeeperWatcher
argument_list|(
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|NAME
operator|.
name|toString
argument_list|()
argument_list|,
name|this
argument_list|)
expr_stmt|;
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
name|LOG
operator|.
name|fatal
argument_list|(
literal|"Abort why="
operator|+
name|why
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|this
operator|.
name|stopped
operator|=
literal|true
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
name|LOG
operator|.
name|debug
argument_list|(
literal|"Stop why="
operator|+
name|why
argument_list|)
expr_stmt|;
name|this
operator|.
name|stopped
operator|=
literal|true
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
name|this
operator|.
name|stopped
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
name|HTU
operator|.
name|getConfiguration
argument_list|()
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
name|this
operator|.
name|zk
return|;
block|}
annotation|@
name|Override
specifier|public
name|CatalogTracker
name|getCatalogTracker
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
name|ServerName
name|getServerName
parameter_list|()
block|{
return|return
name|NAME
return|;
block|}
block|}
comment|/**    * Basic mock region server services.    */
specifier|static
class|class
name|MockRegionServerServices
implements|implements
name|RegionServerServices
block|{
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
name|boolean
name|stopping
init|=
literal|false
decl_stmt|;
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
block|{     }
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
empty_stmt|;
comment|/**    * Test the openregionhandler can deal with its znode being yanked out from    * under it.    * @see<a href="https://issues.apache.org/jira/browse/HBASE-3627">HBASE-3627</a>    * @throws IOException    * @throws NodeExistsException    * @throws KeeperException    */
annotation|@
name|Test
specifier|public
name|void
name|testOpenRegionHandlerYankingRegionFromUnderIt
parameter_list|()
throws|throws
name|IOException
throws|,
name|NodeExistsException
throws|,
name|KeeperException
block|{
specifier|final
name|Server
name|server
init|=
operator|new
name|MockServer
argument_list|()
decl_stmt|;
specifier|final
name|RegionServerServices
name|rss
init|=
operator|new
name|MockRegionServerServices
argument_list|()
decl_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"testOpenRegionHandlerYankingRegionFromUnderIt"
argument_list|)
decl_stmt|;
specifier|final
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|htd
operator|.
name|getName
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
decl_stmt|;
name|HRegion
name|region
init|=
name|HRegion
operator|.
name|createHRegion
argument_list|(
name|hri
argument_list|,
name|HBaseTestingUtility
operator|.
name|getTestDir
argument_list|()
argument_list|,
name|HTU
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|htd
argument_list|)
decl_stmt|;
name|OpenRegionHandler
name|handler
init|=
operator|new
name|OpenRegionHandler
argument_list|(
name|server
argument_list|,
name|rss
argument_list|,
name|hri
argument_list|)
block|{
name|HRegion
name|openRegion
parameter_list|()
block|{
comment|// Open region first, then remove znode as though it'd been hijacked.
comment|//HRegion region = super.openRegion();
name|HRegion
name|region
init|=
name|super
operator|.
name|openRegion
argument_list|(
name|HBaseTestingUtility
operator|.
name|getTestDir
argument_list|()
argument_list|)
decl_stmt|;
comment|// Don't actually open region BUT remove the znode as though it'd
comment|// been hijacked on us.
name|ZooKeeperWatcher
name|zkw
init|=
name|this
operator|.
name|server
operator|.
name|getZooKeeper
argument_list|()
decl_stmt|;
name|String
name|node
init|=
name|ZKAssign
operator|.
name|getNodeName
argument_list|(
name|zkw
argument_list|,
name|hri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
try|try
block|{
name|ZKUtil
operator|.
name|deleteNodeFailSilent
argument_list|(
name|zkw
argument_list|,
name|node
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|KeeperException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Ugh failed delete of "
operator|+
name|node
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
name|region
return|;
block|}
block|}
decl_stmt|;
comment|// Call process without first creating OFFLINE region in zk, see if
comment|// exception or just quiet return (expected).
name|handler
operator|.
name|process
argument_list|()
expr_stmt|;
name|ZKAssign
operator|.
name|createNodeOffline
argument_list|(
name|server
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
name|hri
argument_list|,
name|server
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
comment|// Call process again but this time yank the zk znode out from under it
comment|// post OPENING; again will expect it to come back w/o NPE or exception.
name|handler
operator|.
name|process
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

