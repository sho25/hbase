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
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertTrue
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
name|concurrent
operator|.
name|CountDownLatch
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
name|HBaseClassTestRule
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
name|Waiter
operator|.
name|ExplainingPredicate
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
name|YouAreDeadException
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
name|Table
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
name|testclassification
operator|.
name|MediumTests
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
name|testclassification
operator|.
name|RegionServerTests
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
name|JVMClusterUtil
operator|.
name|RegionServerThread
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
name|WALFactory
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
name|SessionExpiredException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
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

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runner
operator|.
name|RunWith
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameter
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * See HBASE-19929 for more details.  */
end_comment

begin_class
annotation|@
name|RunWith
argument_list|(
name|Parameterized
operator|.
name|class
argument_list|)
annotation|@
name|Category
argument_list|(
block|{
name|RegionServerTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestShutdownWhileWALBroken
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestShutdownWhileWALBroken
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestShutdownWhileWALBroken
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestShutdownWhileWALBroken"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|byte
index|[]
name|CF
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"CF"
argument_list|)
decl_stmt|;
annotation|@
name|Parameter
specifier|public
name|String
name|walType
decl_stmt|;
annotation|@
name|Parameters
argument_list|(
name|name
operator|=
literal|"{index}: WAL={0}"
argument_list|)
specifier|public
specifier|static
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|params
parameter_list|()
block|{
return|return
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|Object
index|[]
block|{
literal|"asyncfs"
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
literal|"filesystem"
block|}
argument_list|)
return|;
block|}
specifier|public
specifier|static
specifier|final
class|class
name|MyRegionServer
extends|extends
name|HRegionServer
block|{
specifier|private
specifier|final
name|CountDownLatch
name|latch
init|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
decl_stmt|;
specifier|public
name|MyRegionServer
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|void
name|tryRegionServerReport
parameter_list|(
name|long
name|reportStartTime
parameter_list|,
name|long
name|reportEndTime
parameter_list|)
throws|throws
name|IOException
block|{
try|try
block|{
name|super
operator|.
name|tryRegionServerReport
argument_list|(
name|reportStartTime
argument_list|,
name|reportEndTime
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|YouAreDeadException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Caught YouAreDeadException, ignore"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|reason
parameter_list|,
name|Throwable
name|cause
parameter_list|)
block|{
if|if
condition|(
name|cause
operator|instanceof
name|SessionExpiredException
condition|)
block|{
comment|// called from ZKWatcher, let's wait a bit to make sure that we call stop before calling
comment|// abort.
try|try
block|{
name|latch
operator|.
name|await
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{         }
block|}
else|else
block|{
comment|// abort from other classes, usually LogRoller, now we can make progress on abort.
name|latch
operator|.
name|countDown
argument_list|()
expr_stmt|;
block|}
name|super
operator|.
name|abort
argument_list|(
name|reason
argument_list|,
name|cause
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setClass
argument_list|(
name|HConstants
operator|.
name|REGION_SERVER_IMPL
argument_list|,
name|MyRegionServer
operator|.
name|class
argument_list|,
name|HRegionServer
operator|.
name|class
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|WALFactory
operator|.
name|WAL_PROVIDER
argument_list|,
name|walType
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|WALFactory
operator|.
name|META_WAL_PROVIDER
argument_list|,
name|walType
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
block|}
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|Exception
block|{
name|UTIL
operator|.
name|createMultiRegionTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|CF
argument_list|)
expr_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
init|)
block|{
name|UTIL
operator|.
name|loadTable
argument_list|(
name|table
argument_list|,
name|CF
argument_list|)
expr_stmt|;
block|}
name|int
name|numRegions
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|size
argument_list|()
decl_stmt|;
name|RegionServerThread
name|rst0
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
decl_stmt|;
name|RegionServerThread
name|rst1
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|get
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|HRegionServer
name|liveRS
decl_stmt|;
name|RegionServerThread
name|toKillRSThread
decl_stmt|;
if|if
condition|(
name|rst1
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getRegions
argument_list|(
name|TableName
operator|.
name|META_TABLE_NAME
argument_list|)
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|liveRS
operator|=
name|rst0
operator|.
name|getRegionServer
argument_list|()
expr_stmt|;
name|toKillRSThread
operator|=
name|rst1
expr_stmt|;
block|}
else|else
block|{
name|liveRS
operator|=
name|rst1
operator|.
name|getRegionServer
argument_list|()
expr_stmt|;
name|toKillRSThread
operator|=
name|rst0
expr_stmt|;
block|}
name|assertTrue
argument_list|(
name|liveRS
operator|.
name|getRegions
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|size
argument_list|()
operator|<
name|numRegions
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|expireSession
argument_list|(
name|toKillRSThread
operator|.
name|getRegionServer
argument_list|()
operator|.
name|getZooKeeper
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
operator|new
name|ExplainingPredicate
argument_list|<
name|Exception
argument_list|>
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|evaluate
parameter_list|()
throws|throws
name|Exception
block|{
return|return
name|liveRS
operator|.
name|getRegions
argument_list|(
name|TABLE_NAME
argument_list|)
operator|.
name|size
argument_list|()
operator|==
name|numRegions
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|explainFailure
parameter_list|()
throws|throws
name|Exception
block|{
return|return
literal|"Failover is not finished yet"
return|;
block|}
block|}
argument_list|)
expr_stmt|;
name|toKillRSThread
operator|.
name|getRegionServer
argument_list|()
operator|.
name|stop
argument_list|(
literal|"Stop for test"
argument_list|)
expr_stmt|;
comment|// make sure that we can successfully quit
name|toKillRSThread
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit
