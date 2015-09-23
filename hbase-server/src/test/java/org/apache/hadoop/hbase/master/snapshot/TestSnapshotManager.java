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
name|snapshot
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
name|testclassification
operator|.
name|SmallTests
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
name|executor
operator|.
name|ExecutorService
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
name|MasterFileSystem
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
name|MetricsMaster
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
name|cleaner
operator|.
name|HFileCleaner
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
name|cleaner
operator|.
name|HFileLinkCleaner
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
name|procedure
operator|.
name|ProcedureCoordinator
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
name|SnapshotDescriptionUtils
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
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_comment
comment|/**  * Test basic snapshot manager functionality  */
end_comment

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
name|TestSnapshotManager
block|{
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
name|MasterServices
name|services
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|MasterServices
operator|.
name|class
argument_list|)
decl_stmt|;
name|MetricsMaster
name|metrics
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|MetricsMaster
operator|.
name|class
argument_list|)
decl_stmt|;
name|ProcedureCoordinator
name|coordinator
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ProcedureCoordinator
operator|.
name|class
argument_list|)
decl_stmt|;
name|ExecutorService
name|pool
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|ExecutorService
operator|.
name|class
argument_list|)
decl_stmt|;
name|MasterFileSystem
name|mfs
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|MasterFileSystem
operator|.
name|class
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
decl_stmt|;
block|{
try|try
block|{
name|fs
operator|=
name|UTIL
operator|.
name|getTestFileSystem
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Couldn't get test filesystem"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
specifier|private
name|SnapshotManager
name|getNewManager
parameter_list|()
throws|throws
name|IOException
throws|,
name|KeeperException
block|{
return|return
name|getNewManager
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|SnapshotManager
name|getNewManager
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
throws|,
name|KeeperException
block|{
name|Mockito
operator|.
name|reset
argument_list|(
name|services
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|services
operator|.
name|getConfiguration
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|services
operator|.
name|getMasterFileSystem
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|mfs
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mfs
operator|.
name|getFileSystem
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|fs
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|mfs
operator|.
name|getRootDir
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
argument_list|)
expr_stmt|;
return|return
operator|new
name|SnapshotManager
argument_list|(
name|services
argument_list|,
name|metrics
argument_list|,
name|coordinator
argument_list|,
name|pool
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testInProcess
parameter_list|()
throws|throws
name|KeeperException
throws|,
name|IOException
block|{
name|TableName
name|tableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testTable"
argument_list|)
decl_stmt|;
name|SnapshotManager
name|manager
init|=
name|getNewManager
argument_list|()
decl_stmt|;
name|TakeSnapshotHandler
name|handler
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|TakeSnapshotHandler
operator|.
name|class
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"Manager is in process when there is no current handler"
argument_list|,
name|manager
operator|.
name|isTakingSnapshot
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|manager
operator|.
name|setSnapshotHandlerForTesting
argument_list|(
name|tableName
argument_list|,
name|handler
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|handler
operator|.
name|isFinished
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Manager isn't in process when handler is running"
argument_list|,
name|manager
operator|.
name|isTakingSnapshot
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|when
argument_list|(
name|handler
operator|.
name|isFinished
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Manager is process when handler isn't running"
argument_list|,
name|manager
operator|.
name|isTakingSnapshot
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify the snapshot support based on the configuration.    */
annotation|@
name|Test
specifier|public
name|void
name|testSnapshotSupportConfiguration
parameter_list|()
throws|throws
name|Exception
block|{
comment|// No configuration (no cleaners, not enabled): snapshot feature disabled
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|SnapshotManager
name|manager
init|=
name|getNewManager
argument_list|(
name|conf
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
literal|"Snapshot should be disabled with no configuration"
argument_list|,
name|isSnapshotSupported
argument_list|(
name|manager
argument_list|)
argument_list|)
expr_stmt|;
comment|// force snapshot feature to be enabled
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|SnapshotManager
operator|.
name|HBASE_SNAPSHOT_ENABLED
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|manager
operator|=
name|getNewManager
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Snapshot should be enabled"
argument_list|,
name|isSnapshotSupported
argument_list|(
name|manager
argument_list|)
argument_list|)
expr_stmt|;
comment|// force snapshot feature to be disabled
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|SnapshotManager
operator|.
name|HBASE_SNAPSHOT_ENABLED
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|manager
operator|=
name|getNewManager
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Snapshot should be disabled"
argument_list|,
name|isSnapshotSupported
argument_list|(
name|manager
argument_list|)
argument_list|)
expr_stmt|;
comment|// force snapshot feature to be disabled, even if cleaners are present
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|HFileCleaner
operator|.
name|MASTER_HFILE_CLEANER_PLUGINS
argument_list|,
name|SnapshotHFileCleaner
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|HFileLinkCleaner
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|SnapshotManager
operator|.
name|HBASE_SNAPSHOT_ENABLED
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|manager
operator|=
name|getNewManager
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"Snapshot should be disabled"
argument_list|,
name|isSnapshotSupported
argument_list|(
name|manager
argument_list|)
argument_list|)
expr_stmt|;
comment|// cleaners are present, but missing snapshot enabled property
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setStrings
argument_list|(
name|HFileCleaner
operator|.
name|MASTER_HFILE_CLEANER_PLUGINS
argument_list|,
name|SnapshotHFileCleaner
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|,
name|HFileLinkCleaner
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|manager
operator|=
name|getNewManager
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Snapshot should be enabled, because cleaners are present"
argument_list|,
name|isSnapshotSupported
argument_list|(
name|manager
argument_list|)
argument_list|)
expr_stmt|;
comment|// Create a "test snapshot"
name|Path
name|rootDir
init|=
name|UTIL
operator|.
name|getDataTestDir
argument_list|()
decl_stmt|;
name|Path
name|testSnapshotDir
init|=
name|SnapshotDescriptionUtils
operator|.
name|getCompletedSnapshotDir
argument_list|(
literal|"testSnapshotSupportConfiguration"
argument_list|,
name|rootDir
argument_list|)
decl_stmt|;
name|fs
operator|.
name|mkdirs
argument_list|(
name|testSnapshotDir
argument_list|)
expr_stmt|;
try|try
block|{
comment|// force snapshot feature to be disabled, but snapshots are present
name|conf
operator|=
operator|new
name|Configuration
argument_list|()
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|SnapshotManager
operator|.
name|HBASE_SNAPSHOT_ENABLED
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|manager
operator|=
name|getNewManager
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"Master should not start when snapshot is disabled, but snapshots are present"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
comment|// expected
block|}
finally|finally
block|{
name|fs
operator|.
name|delete
argument_list|(
name|testSnapshotDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|boolean
name|isSnapshotSupported
parameter_list|(
specifier|final
name|SnapshotManager
name|manager
parameter_list|)
block|{
try|try
block|{
name|manager
operator|.
name|checkSnapshotSupport
argument_list|()
expr_stmt|;
return|return
literal|true
return|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
block|}
end_class

end_unit

