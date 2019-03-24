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
name|client
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
name|assertEquals
import|;
end_import

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
name|master
operator|.
name|RegionState
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
name|procedure
operator|.
name|MasterProcedureEnv
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
name|procedure
operator|.
name|TableProcedureInterface
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
name|Procedure
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
name|ProcedureExecutor
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
name|ProcedureSuspendedException
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
name|ProcedureTestingUtility
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
name|ClientTests
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
name|LargeTests
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
name|Before
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
name|Rule
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
name|rules
operator|.
name|TestName
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

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|io
operator|.
name|Closeables
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

begin_comment
comment|/**  * Class to test HBaseHbck. Spins up the minicluster once at test start and then takes it down  * afterward. Add any testing of HBaseHbck functionality here.  */
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
name|LargeTests
operator|.
name|class
block|,
name|ClientTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestHbck
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
name|TestHbck
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
name|TestHbck
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
annotation|@
name|Parameter
specifier|public
name|boolean
name|async
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|TableName
name|TABLE_NAME
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|TestHbck
operator|.
name|class
operator|.
name|getSimpleName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
decl_stmt|;
specifier|private
specifier|static
name|AsyncConnection
name|ASYNC_CONN
decl_stmt|;
annotation|@
name|Parameters
argument_list|(
name|name
operator|=
literal|"{index}: async={0}"
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
literal|false
block|}
argument_list|,
operator|new
name|Object
index|[]
block|{
literal|true
block|}
argument_list|)
return|;
block|}
specifier|private
name|Hbck
name|getHbck
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|async
condition|)
block|{
return|return
name|ASYNC_CONN
operator|.
name|getHbck
argument_list|()
operator|.
name|get
argument_list|()
return|;
block|}
else|else
block|{
return|return
name|TEST_UTIL
operator|.
name|getHbck
argument_list|()
return|;
block|}
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setUpBeforeClass
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|createMultiRegionTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family1"
argument_list|)
argument_list|,
literal|5
argument_list|)
expr_stmt|;
name|procExec
operator|=
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
expr_stmt|;
name|ASYNC_CONN
operator|=
name|ConnectionFactory
operator|.
name|createAsyncConnection
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|tearDownAfterClass
parameter_list|()
throws|throws
name|Exception
block|{
name|Closeables
operator|.
name|close
argument_list|(
name|ASYNC_CONN
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|IOException
block|{
name|TEST_UTIL
operator|.
name|ensureSomeRegionServersAvailable
argument_list|(
literal|3
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|SuspendProcedure
extends|extends
name|ProcedureTestingUtility
operator|.
name|NoopProcedure
argument_list|<
name|MasterProcedureEnv
argument_list|>
implements|implements
name|TableProcedureInterface
block|{
specifier|public
name|SuspendProcedure
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
block|{
literal|"rawtypes"
block|,
literal|"unchecked"
block|}
argument_list|)
annotation|@
name|Override
specifier|protected
name|Procedure
index|[]
name|execute
parameter_list|(
specifier|final
name|MasterProcedureEnv
name|env
parameter_list|)
throws|throws
name|ProcedureSuspendedException
block|{
comment|// Always suspend the procedure
throw|throw
operator|new
name|ProcedureSuspendedException
argument_list|()
throw|;
block|}
annotation|@
name|Override
specifier|public
name|TableName
name|getTableName
parameter_list|()
block|{
return|return
name|TABLE_NAME
return|;
block|}
annotation|@
name|Override
specifier|public
name|TableOperationType
name|getTableOperationType
parameter_list|()
block|{
return|return
name|TableOperationType
operator|.
name|READ
return|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testBypassProcedure
parameter_list|()
throws|throws
name|Exception
block|{
comment|// SuspendProcedure
specifier|final
name|SuspendProcedure
name|proc
init|=
operator|new
name|SuspendProcedure
argument_list|()
decl_stmt|;
name|long
name|procId
init|=
name|procExec
operator|.
name|submitProcedure
argument_list|(
name|proc
argument_list|)
decl_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
literal|500
argument_list|)
expr_stmt|;
comment|// bypass the procedure
name|List
argument_list|<
name|Long
argument_list|>
name|pids
init|=
name|Arrays
operator|.
expr|<
name|Long
operator|>
name|asList
argument_list|(
name|procId
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Boolean
argument_list|>
name|results
init|=
name|getHbck
argument_list|()
operator|.
name|bypassProcedure
argument_list|(
name|pids
argument_list|,
literal|30000
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Failed to by pass procedure!"
argument_list|,
name|results
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|5000
argument_list|,
parameter_list|()
lambda|->
name|proc
operator|.
name|isSuccess
argument_list|()
operator|&&
name|proc
operator|.
name|isBypass
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"{} finished"
argument_list|,
name|proc
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSetTableStateInMeta
parameter_list|()
throws|throws
name|Exception
block|{
name|Hbck
name|hbck
init|=
name|getHbck
argument_list|()
decl_stmt|;
comment|// set table state to DISABLED
name|hbck
operator|.
name|setTableStateInMeta
argument_list|(
operator|new
name|TableState
argument_list|(
name|TABLE_NAME
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|DISABLED
argument_list|)
argument_list|)
expr_stmt|;
comment|// Method {@link Hbck#setTableStateInMeta()} returns previous state, which in this case
comment|// will be DISABLED
name|TableState
name|prevState
init|=
name|hbck
operator|.
name|setTableStateInMeta
argument_list|(
operator|new
name|TableState
argument_list|(
name|TABLE_NAME
argument_list|,
name|TableState
operator|.
name|State
operator|.
name|ENABLED
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Incorrect previous state! expeced=DISABLED, found="
operator|+
name|prevState
operator|.
name|getState
argument_list|()
argument_list|,
name|prevState
operator|.
name|isDisabled
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testAssigns
parameter_list|()
throws|throws
name|Exception
block|{
name|Hbck
name|hbck
init|=
name|getHbck
argument_list|()
decl_stmt|;
try|try
init|(
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getAdmin
argument_list|()
init|)
block|{
name|List
argument_list|<
name|RegionInfo
argument_list|>
name|regions
init|=
name|admin
operator|.
name|getRegions
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
for|for
control|(
name|RegionInfo
name|ri
range|:
name|regions
control|)
block|{
name|RegionState
name|rs
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionState
argument_list|(
name|ri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"RS: {}"
argument_list|,
name|rs
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|List
argument_list|<
name|Long
argument_list|>
name|pids
init|=
name|hbck
operator|.
name|unassigns
argument_list|(
name|regions
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|r
lambda|->
name|r
operator|.
name|getEncodedName
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|waitOnPids
argument_list|(
name|pids
argument_list|)
expr_stmt|;
for|for
control|(
name|RegionInfo
name|ri
range|:
name|regions
control|)
block|{
name|RegionState
name|rs
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionState
argument_list|(
name|ri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"RS: {}"
argument_list|,
name|rs
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|rs
operator|.
name|toString
argument_list|()
argument_list|,
name|rs
operator|.
name|isClosed
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|pids
operator|=
name|hbck
operator|.
name|assigns
argument_list|(
name|regions
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|r
lambda|->
name|r
operator|.
name|getEncodedName
argument_list|()
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|waitOnPids
argument_list|(
name|pids
argument_list|)
expr_stmt|;
for|for
control|(
name|RegionInfo
name|ri
range|:
name|regions
control|)
block|{
name|RegionState
name|rs
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
operator|.
name|getRegionState
argument_list|(
name|ri
operator|.
name|getEncodedName
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"RS: {}"
argument_list|,
name|rs
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|rs
operator|.
name|toString
argument_list|()
argument_list|,
name|rs
operator|.
name|isOpened
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|// What happens if crappy region list passed?
name|pids
operator|=
name|hbck
operator|.
name|assigns
argument_list|(
name|Arrays
operator|.
name|stream
argument_list|(
operator|new
name|String
index|[]
block|{
literal|"a"
block|,
literal|"some rubbish name"
block|}
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|long
name|pid
range|:
name|pids
control|)
block|{
name|assertEquals
argument_list|(
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
name|Procedure
operator|.
name|NO_PROC_ID
argument_list|,
name|pid
argument_list|)
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testScheduleSCP
parameter_list|()
throws|throws
name|Exception
block|{
name|HRegionServer
name|testRs
init|=
name|TEST_UTIL
operator|.
name|getRSForFirstRegionInTable
argument_list|(
name|TABLE_NAME
argument_list|)
decl_stmt|;
name|TEST_UTIL
operator|.
name|loadTable
argument_list|(
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
operator|.
name|getTable
argument_list|(
name|TABLE_NAME
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family1"
argument_list|)
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|ServerName
name|serverName
init|=
name|testRs
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|Hbck
name|hbck
init|=
name|getHbck
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Long
argument_list|>
name|pids
init|=
name|hbck
operator|.
name|scheduleServerCrashProcedure
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|serverName
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|pids
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"pid is {}"
argument_list|,
name|pids
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Long
argument_list|>
name|newPids
init|=
name|hbck
operator|.
name|scheduleServerCrashProcedure
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|ProtobufUtil
operator|.
name|toServerName
argument_list|(
name|serverName
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|newPids
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"pid is {}"
argument_list|,
name|newPids
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|waitOnPids
argument_list|(
name|pids
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|waitOnPids
parameter_list|(
name|List
argument_list|<
name|Long
argument_list|>
name|pids
parameter_list|)
block|{
name|TEST_UTIL
operator|.
name|waitFor
argument_list|(
literal|60000
argument_list|,
parameter_list|()
lambda|->
name|pids
operator|.
name|stream
argument_list|()
operator|.
name|allMatch
argument_list|(
name|procExec
operator|::
name|isFinished
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

