begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|quotas
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
name|assertNotNull
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
name|ArrayList
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
name|Collections
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
name|Map
operator|.
name|Entry
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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
name|Cell
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
name|CellScanner
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
name|Connection
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
name|Get
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
name|Result
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
name|ResultScanner
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
name|Scan
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
name|client
operator|.
name|SnapshotType
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
name|quotas
operator|.
name|FileArchiverNotifierImpl
operator|.
name|SnapshotWithSize
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
name|hadoop
operator|.
name|hbase
operator|.
name|snapshot
operator|.
name|SnapshotManifest
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
name|FSUtils
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
name|collect
operator|.
name|ImmutableSet
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
name|collect
operator|.
name|Iterables
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
name|collect
operator|.
name|Maps
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
operator|.
name|FamilyFiles
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
operator|.
name|StoreFile
import|;
end_import

begin_comment
comment|/**  * Test class for {@link FileArchiverNotifierImpl}.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|MediumTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestFileArchiverNotifierImpl
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
name|TestFileArchiverNotifierImpl
operator|.
name|class
argument_list|)
decl_stmt|;
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
name|AtomicLong
name|COUNTER
init|=
operator|new
name|AtomicLong
argument_list|()
decl_stmt|;
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
specifier|private
name|Connection
name|conn
decl_stmt|;
specifier|private
name|Admin
name|admin
decl_stmt|;
specifier|private
name|SpaceQuotaHelperForTests
name|helper
decl_stmt|;
specifier|private
name|FileSystem
name|fs
decl_stmt|;
specifier|private
name|Configuration
name|conf
decl_stmt|;
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
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|SpaceQuotaHelperForTests
operator|.
name|updateConfigForQuotas
argument_list|(
name|conf
argument_list|)
expr_stmt|;
comment|// Clean up the compacted files faster than normal (15s instead of 2mins)
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hfile.compaction.discharger.interval"
argument_list|,
literal|15
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// Prevent the SnapshotQuotaObserverChore from running
name|conf
operator|.
name|setInt
argument_list|(
name|SnapshotQuotaObserverChore
operator|.
name|SNAPSHOT_QUOTA_CHORE_DELAY_KEY
argument_list|,
literal|60
operator|*
literal|60
operator|*
literal|1000
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
name|SnapshotQuotaObserverChore
operator|.
name|SNAPSHOT_QUOTA_CHORE_PERIOD_KEY
argument_list|,
literal|60
operator|*
literal|60
operator|*
literal|1000
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
name|AfterClass
specifier|public
specifier|static
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
name|Before
specifier|public
name|void
name|setup
parameter_list|()
throws|throws
name|Exception
block|{
name|conn
operator|=
name|TEST_UTIL
operator|.
name|getConnection
argument_list|()
expr_stmt|;
name|admin
operator|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
expr_stmt|;
name|helper
operator|=
operator|new
name|SpaceQuotaHelperForTests
argument_list|(
name|TEST_UTIL
argument_list|,
name|testName
argument_list|,
name|COUNTER
argument_list|)
expr_stmt|;
name|helper
operator|.
name|removeAllQuotas
argument_list|(
name|conn
argument_list|)
expr_stmt|;
name|fs
operator|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
expr_stmt|;
name|conf
operator|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testSnapshotSizePersistence
parameter_list|()
throws|throws
name|IOException
block|{
specifier|final
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
specifier|final
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|tn
argument_list|)
condition|)
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|tn
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|tn
argument_list|)
expr_stmt|;
block|}
name|TableDescriptor
name|desc
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|tn
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|QuotaTableUtil
operator|.
name|QUOTA_FAMILY_USAGE
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
name|FileArchiverNotifierImpl
name|notifier
init|=
operator|new
name|FileArchiverNotifierImpl
argument_list|(
name|conn
argument_list|,
name|conf
argument_list|,
name|fs
argument_list|,
name|tn
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|SnapshotWithSize
argument_list|>
name|snapshotsWithSizes
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
try|try
init|(
name|Table
name|table
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|tn
argument_list|)
init|)
block|{
comment|// Writing no values will result in no records written.
name|verify
argument_list|(
name|table
argument_list|,
parameter_list|()
lambda|->
block|{
name|notifier
operator|.
name|persistSnapshotSizes
argument_list|(
name|table
argument_list|,
name|snapshotsWithSizes
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|count
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
name|verify
argument_list|(
name|table
argument_list|,
parameter_list|()
lambda|->
block|{
name|snapshotsWithSizes
operator|.
name|add
argument_list|(
operator|new
name|SnapshotWithSize
argument_list|(
literal|"ss1"
argument_list|,
literal|1024L
argument_list|)
argument_list|)
expr_stmt|;
name|snapshotsWithSizes
operator|.
name|add
argument_list|(
operator|new
name|SnapshotWithSize
argument_list|(
literal|"ss2"
argument_list|,
literal|4096L
argument_list|)
argument_list|)
expr_stmt|;
name|notifier
operator|.
name|persistSnapshotSizes
argument_list|(
name|table
argument_list|,
name|snapshotsWithSizes
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|count
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1024L
argument_list|,
name|extractSnapshotSize
argument_list|(
name|table
argument_list|,
name|tn
argument_list|,
literal|"ss1"
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|4096L
argument_list|,
name|extractSnapshotSize
argument_list|(
name|table
argument_list|,
name|tn
argument_list|,
literal|"ss2"
argument_list|)
argument_list|)
expr_stmt|;
block|}
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIncrementalFileArchiving
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
specifier|final
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|tn
argument_list|)
condition|)
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|tn
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|tn
argument_list|)
expr_stmt|;
block|}
specifier|final
name|Table
name|quotaTable
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|QuotaUtil
operator|.
name|QUOTA_TABLE_NAME
argument_list|)
decl_stmt|;
specifier|final
name|TableName
name|tn1
init|=
name|helper
operator|.
name|createTableWithRegions
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|admin
operator|.
name|setQuota
argument_list|(
name|QuotaSettingsFactory
operator|.
name|limitTableSpace
argument_list|(
name|tn1
argument_list|,
name|SpaceQuotaHelperForTests
operator|.
name|ONE_GIGABYTE
argument_list|,
name|SpaceViolationPolicy
operator|.
name|NO_INSERTS
argument_list|)
argument_list|)
expr_stmt|;
comment|// Write some data and flush it
name|helper
operator|.
name|writeData
argument_list|(
name|tn1
argument_list|,
literal|256L
operator|*
name|SpaceQuotaHelperForTests
operator|.
name|ONE_KILOBYTE
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|tn1
argument_list|)
expr_stmt|;
comment|// Create a snapshot on the table
specifier|final
name|String
name|snapshotName1
init|=
name|tn1
operator|+
literal|"snapshot1"
decl_stmt|;
name|admin
operator|.
name|snapshot
argument_list|(
operator|new
name|SnapshotDescription
argument_list|(
name|snapshotName1
argument_list|,
name|tn1
argument_list|,
name|SnapshotType
operator|.
name|SKIPFLUSH
argument_list|)
argument_list|)
expr_stmt|;
name|FileArchiverNotifierImpl
name|notifier
init|=
operator|new
name|FileArchiverNotifierImpl
argument_list|(
name|conn
argument_list|,
name|conf
argument_list|,
name|fs
argument_list|,
name|tn
argument_list|)
decl_stmt|;
name|long
name|t1
init|=
name|notifier
operator|.
name|getLastFullCompute
argument_list|()
decl_stmt|;
name|long
name|snapshotSize
init|=
name|notifier
operator|.
name|computeAndStoreSnapshotSizes
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|snapshotName1
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"The size of the snapshots should be zero"
argument_list|,
literal|0
argument_list|,
name|snapshotSize
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Last compute time was not less than current compute time"
argument_list|,
name|t1
operator|<
name|notifier
operator|.
name|getLastFullCompute
argument_list|()
argument_list|)
expr_stmt|;
comment|// No recently archived files and the snapshot should have no size
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|extractSnapshotSize
argument_list|(
name|quotaTable
argument_list|,
name|tn
argument_list|,
name|snapshotName1
argument_list|)
argument_list|)
expr_stmt|;
comment|// Invoke the addArchivedFiles method with no files
name|notifier
operator|.
name|addArchivedFiles
argument_list|(
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|)
expr_stmt|;
comment|// The size should not have changed
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|extractSnapshotSize
argument_list|(
name|quotaTable
argument_list|,
name|tn
argument_list|,
name|snapshotName1
argument_list|)
argument_list|)
expr_stmt|;
name|notifier
operator|.
name|addArchivedFiles
argument_list|(
name|ImmutableSet
operator|.
name|of
argument_list|(
name|entry
argument_list|(
literal|"a"
argument_list|,
literal|1024L
argument_list|)
argument_list|,
name|entry
argument_list|(
literal|"b"
argument_list|,
literal|1024L
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// The size should not have changed
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|extractSnapshotSize
argument_list|(
name|quotaTable
argument_list|,
name|tn
argument_list|,
name|snapshotName1
argument_list|)
argument_list|)
expr_stmt|;
comment|// Pull one file referenced by the snapshot out of the manifest
name|Set
argument_list|<
name|String
argument_list|>
name|referencedFiles
init|=
name|getFilesReferencedBySnapshot
argument_list|(
name|snapshotName1
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Found snapshot referenced files: "
operator|+
name|referencedFiles
argument_list|,
name|referencedFiles
operator|.
name|size
argument_list|()
operator|>=
literal|1
argument_list|)
expr_stmt|;
name|String
name|referencedFile
init|=
name|Iterables
operator|.
name|getFirst
argument_list|(
name|referencedFiles
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|referencedFile
argument_list|)
expr_stmt|;
comment|// Report that a file this snapshot referenced was moved to the archive. This is a sign
comment|// that the snapshot should now "own" the size of this file
specifier|final
name|long
name|fakeFileSize
init|=
literal|2048L
decl_stmt|;
name|notifier
operator|.
name|addArchivedFiles
argument_list|(
name|ImmutableSet
operator|.
name|of
argument_list|(
name|entry
argument_list|(
name|referencedFile
argument_list|,
name|fakeFileSize
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
comment|// Verify that the snapshot owns this file.
name|assertEquals
argument_list|(
name|fakeFileSize
argument_list|,
name|extractSnapshotSize
argument_list|(
name|quotaTable
argument_list|,
name|tn
argument_list|,
name|snapshotName1
argument_list|)
argument_list|)
expr_stmt|;
comment|// In reality, we did not actually move the file, so a "full" computation should re-set the
comment|// size of the snapshot back to 0.
name|long
name|t2
init|=
name|notifier
operator|.
name|getLastFullCompute
argument_list|()
decl_stmt|;
name|snapshotSize
operator|=
name|notifier
operator|.
name|computeAndStoreSnapshotSizes
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
name|snapshotName1
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|snapshotSize
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|extractSnapshotSize
argument_list|(
name|quotaTable
argument_list|,
name|tn
argument_list|,
name|snapshotName1
argument_list|)
argument_list|)
expr_stmt|;
comment|// We should also have no recently archived files after a re-computation
name|assertTrue
argument_list|(
literal|"Last compute time was not less than current compute time"
argument_list|,
name|t2
operator|<
name|notifier
operator|.
name|getLastFullCompute
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testParseOldNamespaceSnapshotSize
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|Admin
name|admin
init|=
name|TEST_UTIL
operator|.
name|getAdmin
argument_list|()
decl_stmt|;
specifier|final
name|TableName
name|fakeQuotaTableName
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|TableName
name|tn
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|testName
operator|.
name|getMethodName
argument_list|()
operator|+
literal|"1"
argument_list|)
decl_stmt|;
if|if
condition|(
name|admin
operator|.
name|tableExists
argument_list|(
name|fakeQuotaTableName
argument_list|)
condition|)
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|fakeQuotaTableName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|deleteTable
argument_list|(
name|fakeQuotaTableName
argument_list|)
expr_stmt|;
block|}
name|TableDescriptor
name|desc
init|=
name|TableDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|fakeQuotaTableName
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|QuotaTableUtil
operator|.
name|QUOTA_FAMILY_USAGE
argument_list|)
argument_list|)
operator|.
name|setColumnFamily
argument_list|(
name|ColumnFamilyDescriptorBuilder
operator|.
name|of
argument_list|(
name|QuotaUtil
operator|.
name|QUOTA_FAMILY_INFO
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|desc
argument_list|)
expr_stmt|;
specifier|final
name|String
name|ns
init|=
literal|""
decl_stmt|;
try|try
init|(
name|Table
name|fakeQuotaTable
init|=
name|conn
operator|.
name|getTable
argument_list|(
name|fakeQuotaTableName
argument_list|)
init|)
block|{
name|FileArchiverNotifierImpl
name|notifier
init|=
operator|new
name|FileArchiverNotifierImpl
argument_list|(
name|conn
argument_list|,
name|conf
argument_list|,
name|fs
argument_list|,
name|tn
argument_list|)
decl_stmt|;
comment|// Verify no record is treated as zero
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|notifier
operator|.
name|getPreviousNamespaceSnapshotSize
argument_list|(
name|fakeQuotaTable
argument_list|,
name|ns
argument_list|)
argument_list|)
expr_stmt|;
comment|// Set an explicit value of zero
name|fakeQuotaTable
operator|.
name|put
argument_list|(
name|QuotaTableUtil
operator|.
name|createPutForNamespaceSnapshotSize
argument_list|(
name|ns
argument_list|,
literal|0L
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|notifier
operator|.
name|getPreviousNamespaceSnapshotSize
argument_list|(
name|fakeQuotaTable
argument_list|,
name|ns
argument_list|)
argument_list|)
expr_stmt|;
comment|// Set a non-zero value
name|fakeQuotaTable
operator|.
name|put
argument_list|(
name|QuotaTableUtil
operator|.
name|createPutForNamespaceSnapshotSize
argument_list|(
name|ns
argument_list|,
literal|1024L
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1024L
argument_list|,
name|notifier
operator|.
name|getPreviousNamespaceSnapshotSize
argument_list|(
name|fakeQuotaTable
argument_list|,
name|ns
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
specifier|private
name|long
name|count
parameter_list|(
name|Table
name|t
parameter_list|)
throws|throws
name|IOException
block|{
try|try
init|(
name|ResultScanner
name|rs
init|=
name|t
operator|.
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
argument_list|)
init|)
block|{
name|long
name|sum
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Result
name|r
range|:
name|rs
control|)
block|{
while|while
condition|(
name|r
operator|.
name|advance
argument_list|()
condition|)
block|{
name|sum
operator|++
expr_stmt|;
block|}
block|}
return|return
name|sum
return|;
block|}
block|}
specifier|private
name|long
name|extractSnapshotSize
parameter_list|(
name|Table
name|quotaTable
parameter_list|,
name|TableName
name|tn
parameter_list|,
name|String
name|snapshot
parameter_list|)
throws|throws
name|IOException
block|{
name|Get
name|g
init|=
name|QuotaTableUtil
operator|.
name|makeGetForSnapshotSize
argument_list|(
name|tn
argument_list|,
name|snapshot
argument_list|)
decl_stmt|;
name|Result
name|r
init|=
name|quotaTable
operator|.
name|get
argument_list|(
name|g
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|r
argument_list|)
expr_stmt|;
name|CellScanner
name|cs
init|=
name|r
operator|.
name|cellScanner
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|cs
operator|.
name|advance
argument_list|()
argument_list|)
expr_stmt|;
name|Cell
name|c
init|=
name|cs
operator|.
name|current
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|c
argument_list|)
expr_stmt|;
return|return
name|QuotaTableUtil
operator|.
name|extractSnapshotSize
argument_list|(
name|c
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|c
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|c
operator|.
name|getValueLength
argument_list|()
argument_list|)
return|;
block|}
specifier|private
name|void
name|verify
parameter_list|(
name|Table
name|t
parameter_list|,
name|IOThrowingRunnable
name|test
parameter_list|)
throws|throws
name|IOException
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|truncateTable
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|test
operator|.
name|run
argument_list|()
expr_stmt|;
block|}
annotation|@
name|FunctionalInterface
specifier|private
interface|interface
name|IOThrowingRunnable
block|{
name|void
name|run
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
specifier|private
name|Set
argument_list|<
name|String
argument_list|>
name|getFilesReferencedBySnapshot
parameter_list|(
name|String
name|snapshotName
parameter_list|)
throws|throws
name|IOException
block|{
name|HashSet
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
name|Path
name|snapshotDir
init|=
name|SnapshotDescriptionUtils
operator|.
name|getCompletedSnapshotDir
argument_list|(
name|snapshotName
argument_list|,
name|FSUtils
operator|.
name|getRootDir
argument_list|(
name|conf
argument_list|)
argument_list|)
decl_stmt|;
name|SnapshotProtos
operator|.
name|SnapshotDescription
name|sd
init|=
name|SnapshotDescriptionUtils
operator|.
name|readSnapshotInfo
argument_list|(
name|fs
argument_list|,
name|snapshotDir
argument_list|)
decl_stmt|;
name|SnapshotManifest
name|manifest
init|=
name|SnapshotManifest
operator|.
name|open
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|snapshotDir
argument_list|,
name|sd
argument_list|)
decl_stmt|;
comment|// For each region referenced by the snapshot
for|for
control|(
name|SnapshotRegionManifest
name|rm
range|:
name|manifest
operator|.
name|getRegionManifests
argument_list|()
control|)
block|{
comment|// For each column family in this region
for|for
control|(
name|FamilyFiles
name|ff
range|:
name|rm
operator|.
name|getFamilyFilesList
argument_list|()
control|)
block|{
comment|// And each store file in that family
for|for
control|(
name|StoreFile
name|sf
range|:
name|ff
operator|.
name|getStoreFilesList
argument_list|()
control|)
block|{
name|files
operator|.
name|add
argument_list|(
name|sf
operator|.
name|getName
argument_list|()
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
parameter_list|<
name|K
parameter_list|,
name|V
parameter_list|>
name|Entry
argument_list|<
name|K
argument_list|,
name|V
argument_list|>
name|entry
parameter_list|(
name|K
name|k
parameter_list|,
name|V
name|v
parameter_list|)
block|{
return|return
name|Maps
operator|.
name|immutableEntry
argument_list|(
name|k
argument_list|,
name|v
argument_list|)
return|;
block|}
block|}
end_class

end_unit

