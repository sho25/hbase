begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|snapshot
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
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicInteger
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
name|coprocessor
operator|.
name|BaseMasterObserver
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
name|coprocessor
operator|.
name|CoprocessorHost
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
name|coprocessor
operator|.
name|MasterCoprocessorEnvironment
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
name|coprocessor
operator|.
name|ObserverContext
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
name|HBaseProtos
operator|.
name|SnapshotDescription
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
name|snapshot
operator|.
name|SnapshotExistsException
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
name|snapshot
operator|.
name|SnapshotDoesNotExistException
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
name|TestTableName
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestSnapshotClientRetries
block|{
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
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
name|TestSnapshotClientRetries
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
name|TestTableName
name|TEST_TABLE
init|=
operator|new
name|TestTableName
argument_list|()
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|CoprocessorHost
operator|.
name|MASTER_COPROCESSOR_CONF_KEY
argument_list|,
name|MasterSyncObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|1
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
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|,
name|expected
operator|=
name|SnapshotExistsException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testSnapshotAlreadyExist
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|snapshotName
init|=
literal|"testSnapshotAlreadyExist"
decl_stmt|;
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|"f"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|snapshot
argument_list|(
name|snapshotName
argument_list|,
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
name|snapshotAndAssertOneRetry
argument_list|(
name|snapshotName
argument_list|,
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|,
name|expected
operator|=
name|SnapshotDoesNotExistException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testCloneNonExistentSnapshot
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|snapshotName
init|=
literal|"testCloneNonExistentSnapshot"
decl_stmt|;
name|cloneAndAssertOneRetry
argument_list|(
name|snapshotName
argument_list|,
name|TEST_TABLE
operator|.
name|getTableName
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
class|class
name|MasterSyncObserver
extends|extends
name|BaseMasterObserver
block|{
specifier|volatile
name|AtomicInteger
name|snapshotCount
init|=
literal|null
decl_stmt|;
specifier|volatile
name|AtomicInteger
name|cloneCount
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|preSnapshot
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|SnapshotDescription
name|snapshot
parameter_list|,
specifier|final
name|HTableDescriptor
name|hTableDescriptor
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|snapshotCount
operator|!=
literal|null
condition|)
block|{
name|snapshotCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|preCloneSnapshot
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|SnapshotDescription
name|snapshot
parameter_list|,
specifier|final
name|HTableDescriptor
name|hTableDescriptor
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|cloneCount
operator|!=
literal|null
condition|)
block|{
name|cloneCount
operator|.
name|incrementAndGet
argument_list|()
expr_stmt|;
block|}
block|}
block|}
specifier|public
name|void
name|snapshotAndAssertOneRetry
parameter_list|(
specifier|final
name|String
name|snapshotName
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|Exception
block|{
name|MasterSyncObserver
name|observer
init|=
name|getMasterSyncObserver
argument_list|()
decl_stmt|;
name|observer
operator|.
name|snapshotCount
operator|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|snapshot
argument_list|(
name|snapshotName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|observer
operator|.
name|snapshotCount
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|cloneAndAssertOneRetry
parameter_list|(
specifier|final
name|String
name|snapshotName
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|)
throws|throws
name|Exception
block|{
name|MasterSyncObserver
name|observer
init|=
name|getMasterSyncObserver
argument_list|()
decl_stmt|;
name|observer
operator|.
name|cloneCount
operator|=
operator|new
name|AtomicInteger
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|cloneSnapshot
argument_list|(
name|snapshotName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|observer
operator|.
name|cloneCount
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|MasterSyncObserver
name|getMasterSyncObserver
parameter_list|()
block|{
return|return
operator|(
name|MasterSyncObserver
operator|)
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterCoprocessorHost
argument_list|()
operator|.
name|findCoprocessor
argument_list|(
name|MasterSyncObserver
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

