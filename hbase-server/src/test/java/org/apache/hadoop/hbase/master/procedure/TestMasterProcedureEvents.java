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
name|procedure
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
name|ColumnFamilyDescriptorBuilder
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
name|client
operator|.
name|RegionInfoBuilder
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
name|TableDescriptor
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
name|TableDescriptorBuilder
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
name|ProcedureEvent
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
name|procedure2
operator|.
name|store
operator|.
name|wal
operator|.
name|WALProcedureStore
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
name|CommonFSUtils
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
name|TestMasterProcedureEvents
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
name|TestMasterProcedureEvents
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
name|TestCreateTableProcedure
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTIL
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
specifier|private
specifier|static
name|void
name|setupConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|conf
operator|.
name|setInt
argument_list|(
name|MasterProcedureConstants
operator|.
name|MASTER_PROCEDURE_THREADS
argument_list|,
literal|1
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|WALProcedureStore
operator|.
name|USE_HSYNC_CONF_KEY
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|BeforeClass
specifier|public
specifier|static
name|void
name|setupCluster
parameter_list|()
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
name|UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|setupConf
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniDFSCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|CommonFSUtils
operator|.
name|setWALRootDir
argument_list|(
name|conf
argument_list|,
operator|new
name|Path
argument_list|(
name|conf
operator|.
name|get
argument_list|(
literal|"fs.defaultFS"
argument_list|)
argument_list|,
literal|"/tmp/wal"
argument_list|)
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|waitUntilNoRegionsInTransition
argument_list|()
expr_stmt|;
block|}
annotation|@
name|AfterClass
specifier|public
specifier|static
name|void
name|cleanupTest
parameter_list|()
throws|throws
name|Exception
block|{
try|try
block|{
name|UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
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
literal|"failure shutting down cluster"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
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
for|for
control|(
name|TableDescriptor
name|htd
range|:
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|listTableDescriptors
argument_list|()
control|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Tear down, remove table="
operator|+
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|deleteTable
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testMasterInitializedEvent
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
name|HMaster
name|master
init|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
decl_stmt|;
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
init|=
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
decl_stmt|;
name|RegionInfo
name|hri
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|TableDescriptor
name|htd
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tableName
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
literal|"f"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
while|while
condition|(
operator|!
name|master
operator|.
name|isInitialized
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|250
argument_list|)
expr_stmt|;
block|}
name|master
operator|.
name|setInitialized
argument_list|(
literal|false
argument_list|)
expr_stmt|;
comment|// fake it, set back later
comment|// check event wait/wake
name|testProcedureEventWaitWake
argument_list|(
name|master
argument_list|,
name|master
operator|.
name|getInitializedEvent
argument_list|()
argument_list|,
operator|new
name|CreateTableProcedure
argument_list|(
name|procExec
operator|.
name|getEnvironment
argument_list|()
argument_list|,
name|htd
argument_list|,
operator|new
name|RegionInfo
index|[]
block|{
name|hri
block|}
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testProcedureEventWaitWake
parameter_list|(
specifier|final
name|HMaster
name|master
parameter_list|,
specifier|final
name|ProcedureEvent
argument_list|<
name|?
argument_list|>
name|event
parameter_list|,
specifier|final
name|Procedure
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|proc
parameter_list|)
throws|throws
name|Exception
block|{
specifier|final
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|procExec
init|=
name|master
operator|.
name|getMasterProcedureExecutor
argument_list|()
decl_stmt|;
specifier|final
name|MasterProcedureScheduler
name|procSched
init|=
name|procExec
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getProcedureScheduler
argument_list|()
decl_stmt|;
specifier|final
name|long
name|startPollCalls
init|=
name|procSched
operator|.
name|getPollCalls
argument_list|()
decl_stmt|;
specifier|final
name|long
name|startNullPollCalls
init|=
name|procSched
operator|.
name|getNullPollCalls
argument_list|()
decl_stmt|;
comment|// check that nothing is in the event queue
name|LOG
operator|.
name|debug
argument_list|(
literal|"checking "
operator|+
name|event
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|event
operator|.
name|isReady
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|event
operator|.
name|getSuspendedProcedures
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// submit the procedure
name|LOG
operator|.
name|debug
argument_list|(
literal|"submit "
operator|+
name|proc
argument_list|)
expr_stmt|;
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
comment|// wait until the event is in the queue (proc executed and got into suspended state)
name|LOG
operator|.
name|debug
argument_list|(
literal|"wait procedure suspended on "
operator|+
name|event
argument_list|)
expr_stmt|;
while|while
condition|(
name|event
operator|.
name|getSuspendedProcedures
argument_list|()
operator|.
name|size
argument_list|()
operator|<
literal|1
condition|)
name|Thread
operator|.
name|sleep
argument_list|(
literal|25
argument_list|)
expr_stmt|;
comment|// check that the proc is in the event queue
name|LOG
operator|.
name|debug
argument_list|(
literal|"checking "
operator|+
name|event
operator|+
literal|" size="
operator|+
name|event
operator|.
name|getSuspendedProcedures
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|false
argument_list|,
name|event
operator|.
name|isReady
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|event
operator|.
name|getSuspendedProcedures
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
comment|// wake the event
name|LOG
operator|.
name|debug
argument_list|(
literal|"wake "
operator|+
name|event
argument_list|)
expr_stmt|;
name|event
operator|.
name|wake
argument_list|(
name|procSched
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|event
operator|.
name|isReady
argument_list|()
argument_list|)
expr_stmt|;
comment|// wait until proc completes
name|LOG
operator|.
name|debug
argument_list|(
literal|"waiting "
operator|+
name|proc
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|procExec
argument_list|,
name|procId
argument_list|)
expr_stmt|;
comment|// check that nothing is in the event queue and the event is not suspended
name|assertEquals
argument_list|(
literal|true
argument_list|,
name|event
operator|.
name|isReady
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|event
operator|.
name|getSuspendedProcedures
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"completed execution of "
operator|+
name|proc
operator|+
literal|" pollCalls="
operator|+
operator|(
name|procSched
operator|.
name|getPollCalls
argument_list|()
operator|-
name|startPollCalls
operator|)
operator|+
literal|" nullPollCalls="
operator|+
operator|(
name|procSched
operator|.
name|getNullPollCalls
argument_list|()
operator|-
name|startNullPollCalls
operator|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

