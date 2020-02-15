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
name|snapshot
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|ToolRunner
operator|.
name|run
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
name|io
operator|.
name|IOException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|net
operator|.
name|URI
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
name|HashSet
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
name|Objects
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
name|fs
operator|.
name|FileStatus
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
name|client
operator|.
name|Admin
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
name|snapshot
operator|.
name|SnapshotManager
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
name|testclassification
operator|.
name|VerySlowMapReduceTests
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
name|SnapshotProtos
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
name|shaded
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|SnapshotProtos
operator|.
name|SnapshotRegionManifest
import|;
end_import

begin_comment
comment|/**  * Test Export Snapshot Tool  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|VerySlowMapReduceTests
operator|.
name|class
block|,
name|LargeTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestExportSnapshot
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
name|TestExportSnapshot
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
name|TestExportSnapshot
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
specifier|final
specifier|static
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"cf"
argument_list|)
decl_stmt|;
annotation|@
name|Rule
specifier|public
specifier|final
name|TestName
name|testName
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|protected
name|TableName
name|tableName
decl_stmt|;
specifier|private
name|String
name|emptySnapshotName
decl_stmt|;
specifier|private
name|String
name|snapshotName
decl_stmt|;
specifier|private
name|int
name|tableNumFiles
decl_stmt|;
specifier|private
name|Admin
name|admin
decl_stmt|;
specifier|public
specifier|static
name|void
name|setUpBaseConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
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
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionserver.msginterval"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.client.pause"
argument_list|,
literal|250
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|HConstants
operator|.
name|HBASE_CLIENT_RETRIES_NUMBER
argument_list|,
literal|6
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
literal|"hbase.master.enabletable.roundrobin"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"mapreduce.map.maxattempts"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
comment|// If a single node has enough failures (default 3), resource manager will blacklist it.
comment|// With only 2 nodes and tests injecting faults, we don't want that.
name|conf
operator|.
name|setInt
argument_list|(
literal|"mapreduce.job.maxtaskfailures.per.tracker"
argument_list|,
literal|100
argument_list|)
expr_stmt|;
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
name|setUpBaseConf
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniMapReduceCluster
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
name|TEST_UTIL
operator|.
name|shutdownMiniMapReduceCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
comment|/**    * Create a table and take a snapshot of the table used by the export test.    */
annotation|@
name|Before
specifier|public
name|void
name|setUp
parameter_list|()
throws|throws
name|Exception
block|{
name|this
operator|.
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|tableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"testtb-"
operator|+
name|testName
operator|.
name|getMethodName
argument_list|()
argument_list|)
expr_stmt|;
name|snapshotName
operator|=
literal|"snaptb0-"
operator|+
name|testName
operator|.
name|getMethodName
argument_list|()
expr_stmt|;
name|emptySnapshotName
operator|=
literal|"emptySnaptb0-"
operator|+
name|testName
operator|.
name|getMethodName
argument_list|()
expr_stmt|;
comment|// create Table
name|createTable
argument_list|()
expr_stmt|;
comment|// Take an empty snapshot
name|admin
operator|.
name|snapshot
argument_list|(
name|emptySnapshotName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
comment|// Add some rows
name|SnapshotTestingUtils
operator|.
name|loadData
argument_list|(
name|TEST_UTIL
argument_list|,
name|tableName
argument_list|,
literal|50
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
name|tableNumFiles
operator|=
name|admin
operator|.
name|getRegions
argument_list|(
name|tableName
argument_list|)
operator|.
name|size
argument_list|()
expr_stmt|;
comment|// take a snapshot
name|admin
operator|.
name|snapshot
argument_list|(
name|snapshotName
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|createTable
parameter_list|()
throws|throws
name|Exception
block|{
name|SnapshotTestingUtils
operator|.
name|createPreSplitTable
argument_list|(
name|TEST_UTIL
argument_list|,
name|tableName
argument_list|,
literal|2
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
block|}
specifier|protected
interface|interface
name|RegionPredicate
block|{
name|boolean
name|evaluate
parameter_list|(
specifier|final
name|RegionInfo
name|regionInfo
parameter_list|)
function_decl|;
block|}
specifier|protected
name|RegionPredicate
name|getBypassRegionPredicate
parameter_list|()
block|{
return|return
literal|null
return|;
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
name|deleteTable
argument_list|(
name|tableName
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|deleteAllSnapshots
argument_list|(
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
argument_list|)
expr_stmt|;
name|SnapshotTestingUtils
operator|.
name|deleteArchiveDirectory
argument_list|(
name|TEST_UTIL
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify if exported snapshot and copied files matches the original one.    */
annotation|@
name|Test
specifier|public
name|void
name|testExportFileSystemState
parameter_list|()
throws|throws
name|Exception
block|{
name|testExportFileSystemState
argument_list|(
name|tableName
argument_list|,
name|snapshotName
argument_list|,
name|snapshotName
argument_list|,
name|tableNumFiles
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExportFileSystemStateWithSkipTmp
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|ExportSnapshot
operator|.
name|CONF_SKIP_TMP
argument_list|,
literal|true
argument_list|)
expr_stmt|;
try|try
block|{
name|testExportFileSystemState
argument_list|(
name|tableName
argument_list|,
name|snapshotName
argument_list|,
name|snapshotName
argument_list|,
name|tableNumFiles
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|setBoolean
argument_list|(
name|ExportSnapshot
operator|.
name|CONF_SKIP_TMP
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEmptyExportFileSystemState
parameter_list|()
throws|throws
name|Exception
block|{
name|testExportFileSystemState
argument_list|(
name|tableName
argument_list|,
name|emptySnapshotName
argument_list|,
name|emptySnapshotName
argument_list|,
literal|0
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConsecutiveExports
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|copyDir
init|=
name|getLocalDestinationDir
argument_list|()
decl_stmt|;
name|testExportFileSystemState
argument_list|(
name|tableName
argument_list|,
name|snapshotName
argument_list|,
name|snapshotName
argument_list|,
name|tableNumFiles
argument_list|,
name|copyDir
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|testExportFileSystemState
argument_list|(
name|tableName
argument_list|,
name|snapshotName
argument_list|,
name|snapshotName
argument_list|,
name|tableNumFiles
argument_list|,
name|copyDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|removeExportDir
argument_list|(
name|copyDir
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExportWithTargetName
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|String
name|targetName
init|=
literal|"testExportWithTargetName"
decl_stmt|;
name|testExportFileSystemState
argument_list|(
name|tableName
argument_list|,
name|snapshotName
argument_list|,
name|targetName
argument_list|,
name|tableNumFiles
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|testExportFileSystemState
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|String
name|snapshotName
parameter_list|,
specifier|final
name|String
name|targetName
parameter_list|,
name|int
name|filesExpected
parameter_list|)
throws|throws
name|Exception
block|{
name|testExportFileSystemState
argument_list|(
name|tableName
argument_list|,
name|snapshotName
argument_list|,
name|targetName
argument_list|,
name|filesExpected
argument_list|,
name|getHdfsDestinationDir
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|void
name|testExportFileSystemState
parameter_list|(
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|String
name|snapshotName
parameter_list|,
specifier|final
name|String
name|targetName
parameter_list|,
name|int
name|filesExpected
parameter_list|,
name|Path
name|copyDir
parameter_list|,
name|boolean
name|overwrite
parameter_list|)
throws|throws
name|Exception
block|{
name|testExportFileSystemState
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|tableName
argument_list|,
name|snapshotName
argument_list|,
name|targetName
argument_list|,
name|filesExpected
argument_list|,
name|TEST_UTIL
operator|.
name|getDefaultRootDirPath
argument_list|()
argument_list|,
name|copyDir
argument_list|,
name|overwrite
argument_list|,
name|getBypassRegionPredicate
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates destination directory, runs ExportSnapshot() tool, and runs some verifications.    */
specifier|protected
specifier|static
name|void
name|testExportFileSystemState
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|String
name|snapshotName
parameter_list|,
specifier|final
name|String
name|targetName
parameter_list|,
specifier|final
name|int
name|filesExpected
parameter_list|,
specifier|final
name|Path
name|sourceDir
parameter_list|,
name|Path
name|copyDir
parameter_list|,
specifier|final
name|boolean
name|overwrite
parameter_list|,
specifier|final
name|RegionPredicate
name|bypassregionPredicate
parameter_list|,
name|boolean
name|success
parameter_list|)
throws|throws
name|Exception
block|{
name|URI
name|hdfsUri
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|conf
argument_list|)
operator|.
name|getUri
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|copyDir
operator|.
name|toUri
argument_list|()
argument_list|,
name|conf
argument_list|)
decl_stmt|;
name|copyDir
operator|=
name|copyDir
operator|.
name|makeQualified
argument_list|(
name|fs
operator|.
name|getUri
argument_list|()
argument_list|,
name|fs
operator|.
name|getWorkingDirectory
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|opts
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
name|opts
operator|.
name|add
argument_list|(
literal|"--snapshot"
argument_list|)
expr_stmt|;
name|opts
operator|.
name|add
argument_list|(
name|snapshotName
argument_list|)
expr_stmt|;
name|opts
operator|.
name|add
argument_list|(
literal|"--copy-to"
argument_list|)
expr_stmt|;
name|opts
operator|.
name|add
argument_list|(
name|copyDir
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|targetName
operator|.
name|equals
argument_list|(
name|snapshotName
argument_list|)
condition|)
block|{
name|opts
operator|.
name|add
argument_list|(
literal|"--target"
argument_list|)
expr_stmt|;
name|opts
operator|.
name|add
argument_list|(
name|targetName
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|overwrite
condition|)
name|opts
operator|.
name|add
argument_list|(
literal|"--overwrite"
argument_list|)
expr_stmt|;
comment|// Export Snapshot
name|int
name|res
init|=
name|run
argument_list|(
name|conf
argument_list|,
operator|new
name|ExportSnapshot
argument_list|()
argument_list|,
name|opts
operator|.
name|toArray
argument_list|(
operator|new
name|String
index|[
name|opts
operator|.
name|size
argument_list|()
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"success "
operator|+
name|success
operator|+
literal|", res="
operator|+
name|res
argument_list|,
name|success
condition|?
literal|0
else|:
literal|1
argument_list|,
name|res
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|success
condition|)
block|{
specifier|final
name|Path
name|targetDir
init|=
operator|new
name|Path
argument_list|(
name|HConstants
operator|.
name|SNAPSHOT_DIR_NAME
argument_list|,
name|targetName
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|copyDir
operator|.
name|toString
argument_list|()
operator|+
literal|" "
operator|+
name|targetDir
operator|.
name|toString
argument_list|()
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
operator|new
name|Path
argument_list|(
name|copyDir
argument_list|,
name|targetDir
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
return|return;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Exported snapshot"
argument_list|)
expr_stmt|;
comment|// Verify File-System state
name|FileStatus
index|[]
name|rootFiles
init|=
name|fs
operator|.
name|listStatus
argument_list|(
name|copyDir
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|filesExpected
operator|>
literal|0
condition|?
literal|2
else|:
literal|1
argument_list|,
name|rootFiles
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|FileStatus
name|fileStatus
range|:
name|rootFiles
control|)
block|{
name|String
name|name
init|=
name|fileStatus
operator|.
name|getPath
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|fileStatus
operator|.
name|toString
argument_list|()
argument_list|,
name|fileStatus
operator|.
name|isDirectory
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|name
operator|.
name|toString
argument_list|()
argument_list|,
name|name
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|SNAPSHOT_DIR_NAME
argument_list|)
operator|||
name|name
operator|.
name|equals
argument_list|(
name|HConstants
operator|.
name|HFILE_ARCHIVE_DIRECTORY
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Verified filesystem state"
argument_list|)
expr_stmt|;
comment|// Compare the snapshot metadata and verify the hfiles
specifier|final
name|FileSystem
name|hdfs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|hdfsUri
argument_list|,
name|conf
argument_list|)
decl_stmt|;
specifier|final
name|Path
name|snapshotDir
init|=
operator|new
name|Path
argument_list|(
name|HConstants
operator|.
name|SNAPSHOT_DIR_NAME
argument_list|,
name|snapshotName
argument_list|)
decl_stmt|;
specifier|final
name|Path
name|targetDir
init|=
operator|new
name|Path
argument_list|(
name|HConstants
operator|.
name|SNAPSHOT_DIR_NAME
argument_list|,
name|targetName
argument_list|)
decl_stmt|;
name|verifySnapshotDir
argument_list|(
name|hdfs
argument_list|,
operator|new
name|Path
argument_list|(
name|sourceDir
argument_list|,
name|snapshotDir
argument_list|)
argument_list|,
name|fs
argument_list|,
operator|new
name|Path
argument_list|(
name|copyDir
argument_list|,
name|targetDir
argument_list|)
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|snapshotFiles
init|=
name|verifySnapshot
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|copyDir
argument_list|,
name|tableName
argument_list|,
name|targetName
argument_list|,
name|bypassregionPredicate
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|filesExpected
argument_list|,
name|snapshotFiles
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check that ExportSnapshot will succeed if something fails but the retry succeed.    */
annotation|@
name|Test
specifier|public
name|void
name|testExportRetry
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|copyDir
init|=
name|getLocalDestinationDir
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|copyDir
operator|.
name|toUri
argument_list|()
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|)
decl_stmt|;
name|copyDir
operator|=
name|copyDir
operator|.
name|makeQualified
argument_list|(
name|fs
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|ExportSnapshot
operator|.
name|Testing
operator|.
name|CONF_TEST_FAILURE
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|ExportSnapshot
operator|.
name|Testing
operator|.
name|CONF_TEST_FAILURE_COUNT
argument_list|,
literal|2
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"mapreduce.map.maxattempts"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|testExportFileSystemState
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|,
name|snapshotName
argument_list|,
name|snapshotName
argument_list|,
name|tableNumFiles
argument_list|,
name|TEST_UTIL
operator|.
name|getDefaultRootDirPath
argument_list|()
argument_list|,
name|copyDir
argument_list|,
literal|true
argument_list|,
name|getBypassRegionPredicate
argument_list|()
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Check that ExportSnapshot will fail if we inject failure more times than MR will retry.    */
annotation|@
name|Test
specifier|public
name|void
name|testExportFailure
parameter_list|()
throws|throws
name|Exception
block|{
name|Path
name|copyDir
init|=
name|getLocalDestinationDir
argument_list|()
decl_stmt|;
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|copyDir
operator|.
name|toUri
argument_list|()
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|)
decl_stmt|;
name|copyDir
operator|=
name|copyDir
operator|.
name|makeQualified
argument_list|(
name|fs
argument_list|)
expr_stmt|;
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|ExportSnapshot
operator|.
name|Testing
operator|.
name|CONF_TEST_FAILURE
argument_list|,
literal|true
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|ExportSnapshot
operator|.
name|Testing
operator|.
name|CONF_TEST_FAILURE_COUNT
argument_list|,
literal|4
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"mapreduce.map.maxattempts"
argument_list|,
literal|3
argument_list|)
expr_stmt|;
name|testExportFileSystemState
argument_list|(
name|conf
argument_list|,
name|tableName
argument_list|,
name|snapshotName
argument_list|,
name|snapshotName
argument_list|,
name|tableNumFiles
argument_list|,
name|TEST_UTIL
operator|.
name|getDefaultRootDirPath
argument_list|()
argument_list|,
name|copyDir
argument_list|,
literal|true
argument_list|,
name|getBypassRegionPredicate
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/*    * verify if the snapshot folder on file-system 1 match the one on file-system 2    */
specifier|protected
specifier|static
name|void
name|verifySnapshotDir
parameter_list|(
specifier|final
name|FileSystem
name|fs1
parameter_list|,
specifier|final
name|Path
name|root1
parameter_list|,
specifier|final
name|FileSystem
name|fs2
parameter_list|,
specifier|final
name|Path
name|root2
parameter_list|)
throws|throws
name|IOException
block|{
name|assertEquals
argument_list|(
name|listFiles
argument_list|(
name|fs1
argument_list|,
name|root1
argument_list|,
name|root1
argument_list|)
argument_list|,
name|listFiles
argument_list|(
name|fs2
argument_list|,
name|root2
argument_list|,
name|root2
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|Set
argument_list|<
name|String
argument_list|>
name|verifySnapshot
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|rootDir
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|String
name|snapshotName
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|verifySnapshot
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|fs
argument_list|,
name|rootDir
argument_list|,
name|tableName
argument_list|,
name|snapshotName
argument_list|,
name|getBypassRegionPredicate
argument_list|()
argument_list|)
return|;
block|}
comment|/*    * Verify if the files exists    */
specifier|protected
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|verifySnapshot
parameter_list|(
specifier|final
name|Configuration
name|conf
parameter_list|,
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|rootDir
parameter_list|,
specifier|final
name|TableName
name|tableName
parameter_list|,
specifier|final
name|String
name|snapshotName
parameter_list|,
specifier|final
name|RegionPredicate
name|bypassregionPredicate
parameter_list|)
throws|throws
name|IOException
block|{
specifier|final
name|Path
name|exportedSnapshot
init|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
operator|new
name|Path
argument_list|(
name|HConstants
operator|.
name|SNAPSHOT_DIR_NAME
argument_list|,
name|snapshotName
argument_list|)
argument_list|)
decl_stmt|;
specifier|final
name|Set
argument_list|<
name|String
argument_list|>
name|snapshotFiles
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
specifier|final
name|Path
name|exportedArchive
init|=
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
name|HConstants
operator|.
name|HFILE_ARCHIVE_DIRECTORY
argument_list|)
decl_stmt|;
name|SnapshotReferenceUtil
operator|.
name|visitReferencedFiles
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|exportedSnapshot
argument_list|,
operator|new
name|SnapshotReferenceUtil
operator|.
name|SnapshotVisitor
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|void
name|storeFile
parameter_list|(
specifier|final
name|RegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|String
name|family
parameter_list|,
specifier|final
name|SnapshotRegionManifest
operator|.
name|StoreFile
name|storeFile
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|bypassregionPredicate
operator|!=
literal|null
operator|&&
name|bypassregionPredicate
operator|.
name|evaluate
argument_list|(
name|regionInfo
argument_list|)
condition|)
return|return;
name|String
name|hfile
init|=
name|storeFile
operator|.
name|getName
argument_list|()
decl_stmt|;
name|snapshotFiles
operator|.
name|add
argument_list|(
name|hfile
argument_list|)
expr_stmt|;
if|if
condition|(
name|storeFile
operator|.
name|hasReference
argument_list|()
condition|)
block|{
comment|// Nothing to do here, we have already the reference embedded
block|}
else|else
block|{
name|verifyNonEmptyFile
argument_list|(
operator|new
name|Path
argument_list|(
name|exportedArchive
argument_list|,
operator|new
name|Path
argument_list|(
name|FSUtils
operator|.
name|getTableDir
argument_list|(
operator|new
name|Path
argument_list|(
literal|"./"
argument_list|)
argument_list|,
name|tableName
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|,
operator|new
name|Path
argument_list|(
name|family
argument_list|,
name|hfile
argument_list|)
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|void
name|verifyNonEmptyFile
parameter_list|(
specifier|final
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|assertTrue
argument_list|(
name|path
operator|+
literal|" should exists"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|path
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|path
operator|+
literal|" should not be empty"
argument_list|,
name|fs
operator|.
name|getFileStatus
argument_list|(
name|path
argument_list|)
operator|.
name|getLen
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
comment|// Verify Snapshot description
name|SnapshotDescription
name|desc
init|=
name|SnapshotDescriptionUtils
operator|.
name|readSnapshotInfo
argument_list|(
name|fs
argument_list|,
name|exportedSnapshot
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|desc
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|snapshotName
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|desc
operator|.
name|getTable
argument_list|()
operator|.
name|equals
argument_list|(
name|tableName
operator|.
name|getNameAsString
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|snapshotFiles
return|;
block|}
specifier|private
specifier|static
name|Set
argument_list|<
name|String
argument_list|>
name|listFiles
parameter_list|(
specifier|final
name|FileSystem
name|fs
parameter_list|,
specifier|final
name|Path
name|root
parameter_list|,
specifier|final
name|Path
name|dir
parameter_list|)
throws|throws
name|IOException
block|{
name|Set
argument_list|<
name|String
argument_list|>
name|files
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"List files in {} in root {} at {}"
argument_list|,
name|fs
argument_list|,
name|root
argument_list|,
name|dir
argument_list|)
expr_stmt|;
name|int
name|rootPrefix
init|=
name|root
operator|.
name|makeQualified
argument_list|(
name|fs
operator|.
name|getUri
argument_list|()
argument_list|,
name|root
argument_list|)
operator|.
name|toString
argument_list|()
operator|.
name|length
argument_list|()
decl_stmt|;
name|FileStatus
index|[]
name|list
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|dir
argument_list|)
decl_stmt|;
if|if
condition|(
name|list
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|FileStatus
name|fstat
range|:
name|list
control|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
name|Objects
operator|.
name|toString
argument_list|(
name|fstat
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
if|if
condition|(
name|fstat
operator|.
name|isDirectory
argument_list|()
condition|)
block|{
name|files
operator|.
name|addAll
argument_list|(
name|listFiles
argument_list|(
name|fs
argument_list|,
name|root
argument_list|,
name|fstat
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|files
operator|.
name|add
argument_list|(
name|fstat
operator|.
name|getPath
argument_list|()
operator|.
name|makeQualified
argument_list|(
name|fs
argument_list|)
operator|.
name|toString
argument_list|()
operator|.
name|substring
argument_list|(
name|rootPrefix
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
return|return
name|files
return|;
block|}
specifier|private
name|Path
name|getHdfsDestinationDir
parameter_list|()
block|{
name|Path
name|rootDir
init|=
name|TEST_UTIL
operator|.
name|getHBaseCluster
argument_list|()
operator|.
name|getMaster
argument_list|()
operator|.
name|getMasterFileSystem
argument_list|()
operator|.
name|getRootDir
argument_list|()
decl_stmt|;
name|Path
name|path
init|=
operator|new
name|Path
argument_list|(
operator|new
name|Path
argument_list|(
name|rootDir
argument_list|,
literal|"export-test"
argument_list|)
argument_list|,
literal|"export-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"HDFS export destination path: "
operator|+
name|path
argument_list|)
expr_stmt|;
return|return
name|path
return|;
block|}
specifier|private
name|Path
name|getLocalDestinationDir
parameter_list|()
block|{
name|Path
name|path
init|=
name|TEST_UTIL
operator|.
name|getDataTestDir
argument_list|(
literal|"local-export-"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Local export destination path: "
operator|+
name|path
argument_list|)
expr_stmt|;
return|return
name|path
return|;
block|}
specifier|private
specifier|static
name|void
name|removeExportDir
parameter_list|(
specifier|final
name|Path
name|path
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|FileSystem
operator|.
name|get
argument_list|(
name|path
operator|.
name|toUri
argument_list|()
argument_list|,
operator|new
name|Configuration
argument_list|()
argument_list|)
decl_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|path
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

