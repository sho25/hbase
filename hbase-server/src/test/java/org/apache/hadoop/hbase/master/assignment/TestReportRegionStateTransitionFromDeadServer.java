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
name|master
operator|.
name|assignment
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
name|assertNotEquals
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ExecutionException
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
name|Future
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
name|TimeUnit
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
name|TimeoutException
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
name|HBaseIOException
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
name|PleaseHoldException
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
name|client
operator|.
name|RegionInfo
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
name|HMaster
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
name|MasterServices
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
name|RegionPlan
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
name|ServerManager
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
name|HRegionServer
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
name|MasterTests
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
name|util
operator|.
name|Bytes
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|RegionServerStatusProtos
operator|.
name|ReportRegionStateTransitionRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|RegionServerStatusProtos
operator|.
name|ReportRegionStateTransitionResponse
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MasterTests
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
name|TestReportRegionStateTransitionFromDeadServer
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
name|TestReportRegionStateTransitionFromDeadServer
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|ServerName
argument_list|>
name|EXCLUDE_SERVERS
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|CountDownLatch
name|ARRIVE_GET_REGIONS
decl_stmt|;
specifier|private
specifier|static
name|CountDownLatch
name|RESUME_GET_REGIONS
decl_stmt|;
specifier|private
specifier|static
name|CountDownLatch
name|ARRIVE_REPORT
decl_stmt|;
specifier|private
specifier|static
name|CountDownLatch
name|RESUME_REPORT
decl_stmt|;
specifier|private
specifier|static
specifier|final
class|class
name|ServerManagerForTest
extends|extends
name|ServerManager
block|{
specifier|public
name|ServerManagerForTest
parameter_list|(
name|MasterServices
name|master
parameter_list|)
block|{
name|super
argument_list|(
name|master
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|ServerName
argument_list|>
name|createDestinationServersList
parameter_list|()
block|{
return|return
name|super
operator|.
name|createDestinationServersList
argument_list|(
name|EXCLUDE_SERVERS
argument_list|)
return|;
block|}
block|}
specifier|private
specifier|static
specifier|final
class|class
name|AssignmentManagerForTest
extends|extends
name|AssignmentManager
block|{
specifier|public
name|AssignmentManagerForTest
parameter_list|(
name|MasterServices
name|master
parameter_list|)
block|{
name|super
argument_list|(
name|master
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|getRegionsOnServer
parameter_list|(
name|ServerName
name|serverName
parameter_list|)
block|{
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
init|=
name|super
operator|.
name|getRegionsOnServer
argument_list|(
name|serverName
argument_list|)
decl_stmt|;
if|if
condition|(
name|ARRIVE_GET_REGIONS
operator|!=
literal|null
condition|)
block|{
name|ARRIVE_GET_REGIONS
operator|.
name|countDown
argument_list|()
expr_stmt|;
try|try
block|{
name|RESUME_GET_REGIONS
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
return|return
name|regions
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReportRegionStateTransitionResponse
name|reportRegionStateTransition
parameter_list|(
name|ReportRegionStateTransitionRequest
name|req
parameter_list|)
throws|throws
name|PleaseHoldException
block|{
if|if
condition|(
name|ARRIVE_REPORT
operator|!=
literal|null
operator|&&
name|req
operator|.
name|getTransitionList
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|allMatch
argument_list|(
name|t
lambda|->
operator|!
name|ProtobufUtil
operator|.
name|toRegionInfo
argument_list|(
name|t
operator|.
name|getRegionInfo
argument_list|(
literal|0
argument_list|)
argument_list|)
operator|.
name|isMetaRegion
argument_list|()
argument_list|)
condition|)
block|{
name|ARRIVE_REPORT
operator|.
name|countDown
argument_list|()
block|;
try|try
block|{
name|RESUME_REPORT
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
return|return
name|super
operator|.
name|reportRegionStateTransition
argument_list|(
name|req
argument_list|)
return|;
block|}
block|}
end_class

begin_class
specifier|public
specifier|static
specifier|final
class|class
name|HMasterForTest
extends|extends
name|HMaster
block|{
specifier|public
name|HMasterForTest
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
name|AssignmentManager
name|createAssignmentManager
parameter_list|(
name|MasterServices
name|master
parameter_list|)
block|{
return|return
operator|new
name|AssignmentManagerForTest
argument_list|(
name|master
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|protected
name|ServerManager
name|createServerManager
parameter_list|(
name|MasterServices
name|master
parameter_list|)
throws|throws
name|IOException
block|{
name|setupClusterConnection
argument_list|()
expr_stmt|;
return|return
operator|new
name|ServerManagerForTest
argument_list|(
name|master
argument_list|)
return|;
block|}
block|}
end_class

begin_decl_stmt
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
end_decl_stmt

begin_decl_stmt
specifier|private
specifier|static
name|TableName
name|NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"Report"
argument_list|)
decl_stmt|;
end_decl_stmt

begin_decl_stmt
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
literal|"cf"
argument_list|)
decl_stmt|;
end_decl_stmt

begin_function
annotation|@
name|BeforeClass
specifier|public
specifier|static
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
name|MASTER_IMPL
argument_list|,
name|HMasterForTest
operator|.
name|class
argument_list|,
name|HMaster
operator|.
name|class
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.msginterval"
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|balancerSwitch
argument_list|(
literal|false
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|NAME
argument_list|,
name|CF
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitTableAvailable
argument_list|(
name|NAME
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|AfterClass
specifier|public
specifier|static
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
end_function

begin_function
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|HBaseIOException
throws|,
name|InterruptedException
throws|,
name|ExecutionException
block|{
name|RegionInfo
name|region
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegions
argument_list|(
name|NAME
argument_list|)
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
name|AssignmentManager
name|am
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
decl_stmt|;
name|RegionStateNode
name|rsn
init|=
name|am
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionStateNode
argument_list|(
name|region
argument_list|)
decl_stmt|;
comment|// move from rs0 to rs1, and then kill rs0. Later add rs1 to exclude servers, and at last verify
comment|// that the region should not be on rs1 and rs2 both.
name|HRegionServer
name|rs0
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServer
argument_list|(
name|rsn
operator|.
name|getRegionLocation
argument_list|()
argument_list|)
decl_stmt|;
name|HRegionServer
name|rs1
init|=
name|UTIL
operator|.
name|getOtherRegionServer
argument_list|(
name|rs0
argument_list|)
decl_stmt|;
name|HRegionServer
name|rs2
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getRegionServerThreads
argument_list|()
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|t
lambda|->
name|t
operator|.
name|getRegionServer
argument_list|()
argument_list|)
operator|.
name|filter
argument_list|(
name|rs
lambda|->
name|rs
operator|!=
name|rs0
operator|&&
name|rs
operator|!=
name|rs1
argument_list|)
operator|.
name|findAny
argument_list|()
operator|.
name|get
argument_list|()
decl_stmt|;
name|RESUME_REPORT
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|ARRIVE_REPORT
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|Future
argument_list|<
name|?
argument_list|>
name|future
init|=
name|am
operator|.
name|moveAsync
argument_list|(
operator|new
name|RegionPlan
argument_list|(
name|region
argument_list|,
name|rs0
operator|.
name|getServerName
argument_list|()
argument_list|,
name|rs1
operator|.
name|getServerName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|ARRIVE_REPORT
operator|.
name|await
argument_list|()
expr_stmt|;
name|RESUME_GET_REGIONS
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|ARRIVE_GET_REGIONS
operator|=
operator|new
name|CountDownLatch
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|rs0
operator|.
name|abort
argument_list|(
literal|"For testing!"
argument_list|)
expr_stmt|;
name|ARRIVE_GET_REGIONS
operator|.
name|await
argument_list|()
expr_stmt|;
name|RESUME_REPORT
operator|.
name|countDown
argument_list|()
expr_stmt|;
try|try
block|{
name|future
operator|.
name|get
argument_list|(
literal|15
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|TimeoutException
name|e
parameter_list|)
block|{
comment|// after the fix in HBASE-21508 we will get this exception as the TRSP can not be finished any
comment|// more before SCP interrupts it. It's OK.
block|}
name|EXCLUDE_SERVERS
operator|.
name|add
argument_list|(
name|rs1
operator|.
name|getServerName
argument_list|()
argument_list|)
expr_stmt|;
name|RESUME_GET_REGIONS
operator|.
name|countDown
argument_list|()
expr_stmt|;
comment|// wait until there are no running procedures, no SCP and no TRSP
name|UTIL
operator|.
name|waitFor
argument_list|(
literal|30000
argument_list|,
parameter_list|()
lambda|->
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getActiveProcIds
argument_list|()
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|boolean
name|onRS1
init|=
operator|!
name|rs1
operator|.
name|getRegions
argument_list|(
name|NAME
argument_list|)
operator|.
name|isEmpty
argument_list|()
decl_stmt|;
name|boolean
name|onRS2
init|=
operator|!
name|rs2
operator|.
name|getRegions
argument_list|(
name|NAME
argument_list|)
operator|.
name|isEmpty
argument_list|()
decl_stmt|;
name|assertNotEquals
argument_list|(
literal|"should either be on rs1 or rs2, but onRS1 is "
operator|+
name|onRS1
operator|+
literal|" and on RS2 is "
operator|+
name|onRS2
argument_list|,
name|onRS1
argument_list|,
name|onRS2
argument_list|)
expr_stmt|;
block|}
end_function

unit|}
end_unit

