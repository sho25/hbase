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
name|master
operator|.
name|handler
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
name|List
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
name|HColumnDescriptor
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
name|MiniHBaseCluster
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
name|RegionState
operator|.
name|State
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
name|RegionStates
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|FSUtils
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
name|TestCreateTableHandler
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
name|TestCreateTableHandler
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|FAMILYNAME
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|throwException
init|=
literal|false
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
name|throwException
operator|=
literal|false
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testCreateTableCalledTwiceAndFirstOneInProgress
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
literal|"testCreateTableCalledTwiceAndFirstOneInProgress"
argument_list|)
decl_stmt|;
specifier|final
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
specifier|final
name|HMaster
name|m
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
specifier|final
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILYNAME
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|HRegionInfo
index|[]
name|hRegionInfos
init|=
operator|new
name|HRegionInfo
index|[]
block|{
operator|new
name|HRegionInfo
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|}
decl_stmt|;
name|CustomCreateTableHandler
name|handler
init|=
operator|new
name|CustomCreateTableHandler
argument_list|(
name|m
argument_list|,
name|m
operator|.
name|getMasterFileSystem
argument_list|()
argument_list|,
name|desc
argument_list|,
name|cluster
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|hRegionInfos
argument_list|,
name|m
argument_list|)
decl_stmt|;
name|handler
operator|.
name|prepare
argument_list|()
expr_stmt|;
name|throwException
operator|=
literal|true
expr_stmt|;
name|handler
operator|.
name|process
argument_list|()
expr_stmt|;
name|throwException
operator|=
literal|false
expr_stmt|;
name|CustomCreateTableHandler
name|handler1
init|=
operator|new
name|CustomCreateTableHandler
argument_list|(
name|m
argument_list|,
name|m
operator|.
name|getMasterFileSystem
argument_list|()
argument_list|,
name|desc
argument_list|,
name|cluster
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|hRegionInfos
argument_list|,
name|m
argument_list|)
decl_stmt|;
name|handler1
operator|.
name|prepare
argument_list|()
expr_stmt|;
name|handler1
operator|.
name|process
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|isTableAvailable
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|200
argument_list|)
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|isTableEnabled
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testCreateTableWithSplitRegion
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
literal|"testCreateTableWithSplitRegion"
argument_list|)
decl_stmt|;
specifier|final
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
specifier|final
name|HMaster
name|m
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
specifier|final
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILYNAME
argument_list|)
argument_list|)
expr_stmt|;
name|byte
index|[]
name|splitPoint
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"split-point"
argument_list|)
decl_stmt|;
name|long
name|ts
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|HRegionInfo
name|d1
init|=
operator|new
name|HRegionInfo
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
name|splitPoint
argument_list|,
literal|false
argument_list|,
name|ts
argument_list|)
decl_stmt|;
name|HRegionInfo
name|d2
init|=
operator|new
name|HRegionInfo
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|,
name|splitPoint
argument_list|,
literal|null
argument_list|,
literal|false
argument_list|,
name|ts
operator|+
literal|1
argument_list|)
decl_stmt|;
name|HRegionInfo
name|parent
init|=
operator|new
name|HRegionInfo
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|,
literal|true
argument_list|,
name|ts
operator|+
literal|2
argument_list|)
decl_stmt|;
name|parent
operator|.
name|setOffline
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|Path
name|tempdir
init|=
name|m
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getTempDir
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|m
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|Path
name|tempTableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|tempdir
argument_list|,
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|tempTableDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// Clean up temp table dir if exists
specifier|final
name|HRegionInfo
index|[]
name|hRegionInfos
init|=
operator|new
name|HRegionInfo
index|[]
block|{
name|d1
block|,
name|d2
block|,
name|parent
block|}
decl_stmt|;
name|CreateTableHandler
name|handler
init|=
operator|new
name|CreateTableHandler
argument_list|(
name|m
argument_list|,
name|m
operator|.
name|getMasterFileSystem
argument_list|()
argument_list|,
name|desc
argument_list|,
name|cluster
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|hRegionInfos
argument_list|,
name|m
argument_list|)
decl_stmt|;
name|handler
operator|.
name|prepare
argument_list|()
expr_stmt|;
name|handler
operator|.
name|process
argument_list|()
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|100
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
operator|!
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|isTableAvailable
argument_list|(
name|tableName
argument_list|)
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|300
argument_list|)
expr_stmt|;
block|}
block|}
name|assertTrue
argument_list|(
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|isTableEnabled
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|isTableAvailable
argument_list|(
name|tableName
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|TEST_UTIL
operator|.
name|getHBaseAdmin
argument_list|()
operator|.
name|isTableAvailable
argument_list|(
name|tableName
argument_list|,
operator|new
name|byte
index|[]
index|[]
block|{
name|splitPoint
block|}
argument_list|)
argument_list|)
expr_stmt|;
name|RegionStates
name|regionStates
init|=
name|m
operator|.
name|getAssignmentManager
argument_list|()
operator|.
name|getRegionStates
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Parent should be in SPLIT state"
argument_list|,
name|regionStates
operator|.
name|isRegionInState
argument_list|(
name|parent
argument_list|,
name|State
operator|.
name|SPLIT
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|60000
argument_list|)
specifier|public
name|void
name|testMasterRestartAfterEnablingNodeIsCreated
parameter_list|()
throws|throws
name|Exception
block|{
name|byte
index|[]
name|tableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"testMasterRestartAfterEnablingNodeIsCreated"
argument_list|)
decl_stmt|;
specifier|final
name|MiniHBaseCluster
name|cluster
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
decl_stmt|;
specifier|final
name|HMaster
name|m
init|=
name|cluster
operator|.
name|getMaster
argument_list|()
decl_stmt|;
specifier|final
name|HTableDescriptor
name|desc
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|tableName
argument_list|)
argument_list|)
decl_stmt|;
name|desc
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|FAMILYNAME
argument_list|)
argument_list|)
expr_stmt|;
specifier|final
name|HRegionInfo
index|[]
name|hRegionInfos
init|=
operator|new
name|HRegionInfo
index|[]
block|{
operator|new
name|HRegionInfo
argument_list|(
name|desc
operator|.
name|getTableName
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
block|}
decl_stmt|;
name|CustomCreateTableHandler
name|handler
init|=
operator|new
name|CustomCreateTableHandler
argument_list|(
name|m
argument_list|,
name|m
operator|.
name|getMasterFileSystem
argument_list|()
argument_list|,
name|desc
argument_list|,
name|cluster
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|hRegionInfos
argument_list|,
name|m
argument_list|)
decl_stmt|;
name|handler
operator|.
name|prepare
argument_list|()
expr_stmt|;
name|throwException
operator|=
literal|true
expr_stmt|;
name|handler
operator|.
name|process
argument_list|()
expr_stmt|;
name|abortAndStartNewMaster
argument_list|(
name|cluster
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|cluster
operator|.
name|getLiveMasterThreads
argument_list|()
operator|.
name|size
argument_list|()
operator|==
literal|1
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|abortAndStartNewMaster
parameter_list|(
specifier|final
name|MiniHBaseCluster
name|cluster
parameter_list|)
throws|throws
name|IOException
block|{
name|cluster
operator|.
name|abortMaster
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitOnMaster
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting new master"
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|startMaster
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting for master to become active."
argument_list|)
expr_stmt|;
name|cluster
operator|.
name|waitForActiveAndReadyMaster
argument_list|()
expr_stmt|;
block|}
specifier|private
specifier|static
class|class
name|CustomCreateTableHandler
extends|extends
name|CreateTableHandler
block|{
specifier|public
name|CustomCreateTableHandler
parameter_list|(
name|Server
name|server
parameter_list|,
name|MasterFileSystem
name|fileSystemManager
parameter_list|,
name|HTableDescriptor
name|hTableDescriptor
parameter_list|,
name|Configuration
name|conf
parameter_list|,
name|HRegionInfo
index|[]
name|newRegions
parameter_list|,
name|MasterServices
name|masterServices
parameter_list|)
block|{
name|super
argument_list|(
name|server
argument_list|,
name|fileSystemManager
argument_list|,
name|hTableDescriptor
argument_list|,
name|conf
argument_list|,
name|newRegions
argument_list|,
name|masterServices
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|protected
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|handleCreateHdfsRegions
parameter_list|(
name|Path
name|tableRootDir
parameter_list|,
name|TableName
name|tableName
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|throwException
condition|)
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Test throws exceptions."
argument_list|)
throw|;
block|}
return|return
name|super
operator|.
name|handleCreateHdfsRegions
argument_list|(
name|tableRootDir
argument_list|,
name|tableName
argument_list|)
return|;
block|}
block|}
block|}
end_class

end_unit

