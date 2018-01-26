begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUTKey WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|locking
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
name|assertFalse
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
name|util
operator|.
name|List
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
name|NamespaceDescriptor
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
name|procedure
operator|.
name|MasterProcedureConstants
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
name|procedure2
operator|.
name|LockType
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
name|SmallTests
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
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestLockManager
block|{
annotation|@
name|Rule
specifier|public
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
comment|// crank this up if this test turns out to be flaky.
specifier|private
specifier|static
specifier|final
name|int
name|LOCAL_LOCKS_TIMEOUT
init|=
literal|1000
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
name|TestLockManager
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
specifier|private
specifier|static
name|MasterServices
name|masterServices
decl_stmt|;
specifier|private
specifier|static
name|String
name|namespace
init|=
literal|"namespace"
decl_stmt|;
specifier|private
specifier|static
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|namespace
argument_list|,
literal|"table"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|HRegionInfo
index|[]
name|tableRegions
decl_stmt|;
specifier|private
specifier|static
name|void
name|setupConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
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
literal|"hbase.procedure.check.owner.set"
argument_list|,
literal|false
argument_list|)
expr_stmt|;
comment|// since rpc user will be null
name|conf
operator|.
name|setInt
argument_list|(
name|LockProcedure
operator|.
name|LOCAL_MASTER_LOCKS_TIMEOUT_MS_CONF
argument_list|,
name|LOCAL_LOCKS_TIMEOUT
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
name|startMiniCluster
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|masterServices
operator|=
name|UTIL
operator|.
name|getMiniHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
expr_stmt|;
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|createNamespace
argument_list|(
name|NamespaceDescriptor
operator|.
name|create
argument_list|(
name|namespace
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|UTIL
operator|.
name|createTable
argument_list|(
name|tableName
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
literal|"fam"
operator|.
name|getBytes
argument_list|()
block|}
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
literal|"1"
operator|.
name|getBytes
argument_list|()
block|}
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|UTIL
operator|.
name|getAdmin
argument_list|()
operator|.
name|getTableRegions
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
assert|assert
name|regions
operator|.
name|size
argument_list|()
operator|>
literal|0
assert|;
name|tableRegions
operator|=
operator|new
name|HRegionInfo
index|[
name|regions
operator|.
name|size
argument_list|()
index|]
expr_stmt|;
name|regions
operator|.
name|toArray
argument_list|(
name|tableRegions
argument_list|)
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
name|Procedure
argument_list|<
name|?
argument_list|>
name|proc
range|:
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getProcedures
argument_list|()
control|)
block|{
if|if
condition|(
name|proc
operator|instanceof
name|LockProcedure
condition|)
block|{
operator|(
operator|(
name|LockProcedure
operator|)
name|proc
operator|)
operator|.
name|unlock
argument_list|(
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getEnvironment
argument_list|()
argument_list|)
expr_stmt|;
name|ProcedureTestingUtility
operator|.
name|waitProcedure
argument_list|(
name|getMasterProcedureExecutor
argument_list|()
argument_list|,
name|proc
argument_list|)
expr_stmt|;
block|}
block|}
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|getMasterProcedureExecutor
argument_list|()
operator|.
name|getEnvironment
argument_list|()
operator|.
name|getProcedureScheduler
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ProcedureExecutor
argument_list|<
name|MasterProcedureEnv
argument_list|>
name|getMasterProcedureExecutor
parameter_list|()
block|{
return|return
name|UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterProcedureExecutor
argument_list|()
return|;
block|}
comment|/**    * Tests that basic lock functionality works.    */
annotation|@
name|Test
specifier|public
name|void
name|testMasterLockAcquire
parameter_list|()
throws|throws
name|Exception
block|{
name|LockManager
operator|.
name|MasterLock
name|lock
init|=
name|masterServices
operator|.
name|getLockManager
argument_list|()
operator|.
name|createMasterLock
argument_list|(
name|namespace
argument_list|,
name|LockType
operator|.
name|EXCLUSIVE
argument_list|,
literal|"desc"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|lock
operator|.
name|tryAcquire
argument_list|(
literal|2000
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|lock
operator|.
name|getProc
argument_list|()
operator|.
name|isLocked
argument_list|()
argument_list|)
expr_stmt|;
name|lock
operator|.
name|release
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|lock
operator|.
name|getProc
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Two locks try to acquire lock on same table, assert that later one times out.    */
annotation|@
name|Test
specifier|public
name|void
name|testMasterLockAcquireTimeout
parameter_list|()
throws|throws
name|Exception
block|{
name|LockManager
operator|.
name|MasterLock
name|lock
init|=
name|masterServices
operator|.
name|getLockManager
argument_list|()
operator|.
name|createMasterLock
argument_list|(
name|tableName
argument_list|,
name|LockType
operator|.
name|EXCLUSIVE
argument_list|,
literal|"desc"
argument_list|)
decl_stmt|;
name|LockManager
operator|.
name|MasterLock
name|lock2
init|=
name|masterServices
operator|.
name|getLockManager
argument_list|()
operator|.
name|createMasterLock
argument_list|(
name|tableName
argument_list|,
name|LockType
operator|.
name|EXCLUSIVE
argument_list|,
literal|"desc"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|lock
operator|.
name|tryAcquire
argument_list|(
literal|2000
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|lock2
operator|.
name|tryAcquire
argument_list|(
name|LOCAL_LOCKS_TIMEOUT
operator|/
literal|2
argument_list|)
argument_list|)
expr_stmt|;
comment|// wait less than other lock's timeout
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|lock2
operator|.
name|getProc
argument_list|()
argument_list|)
expr_stmt|;
name|lock
operator|.
name|release
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|lock2
operator|.
name|tryAcquire
argument_list|(
literal|2000
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|lock2
operator|.
name|getProc
argument_list|()
operator|.
name|isLocked
argument_list|()
argument_list|)
expr_stmt|;
name|lock2
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
comment|/**    * Take region lock, they try table exclusive lock, later one should time out.    */
annotation|@
name|Test
specifier|public
name|void
name|testMasterLockAcquireTimeoutRegionVsTableExclusive
parameter_list|()
throws|throws
name|Exception
block|{
name|LockManager
operator|.
name|MasterLock
name|lock
init|=
name|masterServices
operator|.
name|getLockManager
argument_list|()
operator|.
name|createMasterLock
argument_list|(
name|tableRegions
argument_list|,
literal|"desc"
argument_list|)
decl_stmt|;
name|LockManager
operator|.
name|MasterLock
name|lock2
init|=
name|masterServices
operator|.
name|getLockManager
argument_list|()
operator|.
name|createMasterLock
argument_list|(
name|tableName
argument_list|,
name|LockType
operator|.
name|EXCLUSIVE
argument_list|,
literal|"desc"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|lock
operator|.
name|tryAcquire
argument_list|(
literal|2000
argument_list|)
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|lock2
operator|.
name|tryAcquire
argument_list|(
name|LOCAL_LOCKS_TIMEOUT
operator|/
literal|2
argument_list|)
argument_list|)
expr_stmt|;
comment|// wait less than other lock's timeout
name|assertEquals
argument_list|(
literal|null
argument_list|,
name|lock2
operator|.
name|getProc
argument_list|()
argument_list|)
expr_stmt|;
name|lock
operator|.
name|release
argument_list|()
expr_stmt|;
name|assertTrue
argument_list|(
name|lock2
operator|.
name|tryAcquire
argument_list|(
literal|2000
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|lock2
operator|.
name|getProc
argument_list|()
operator|.
name|isLocked
argument_list|()
argument_list|)
expr_stmt|;
name|lock2
operator|.
name|release
argument_list|()
expr_stmt|;
block|}
block|}
end_class

end_unit

