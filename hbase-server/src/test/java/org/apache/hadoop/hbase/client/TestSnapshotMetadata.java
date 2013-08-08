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
name|io
operator|.
name|encoding
operator|.
name|DataBlockEncoding
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
name|regionserver
operator|.
name|BloomType
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
name|ConstantSizeRegionSplitPolicy
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
name|SnapshotTestingUtils
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
name|After
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Assert
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

begin_comment
comment|/**  * Test class to verify that metadata is consistent before and after a snapshot attempt.  */
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
name|TestSnapshotMetadata
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
specifier|private
specifier|static
specifier|final
name|int
name|NUM_RS
init|=
literal|2
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|STRING_TABLE_NAME
init|=
literal|"testtable"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MAX_VERSIONS_FAM_STR
init|=
literal|"fam_max_columns"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|MAX_VERSIONS_FAM
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|MAX_VERSIONS_FAM_STR
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|COMPRESSED_FAM_STR
init|=
literal|"fam_compressed"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|COMPRESSED_FAM
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|COMPRESSED_FAM_STR
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|BLOCKSIZE_FAM_STR
init|=
literal|"fam_blocksize"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|BLOCKSIZE_FAM
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|BLOCKSIZE_FAM_STR
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|BLOOMFILTER_FAM_STR
init|=
literal|"fam_bloomfilter"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|BLOOMFILTER_FAM
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|BLOOMFILTER_FAM_STR
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEST_CONF_CUSTOM_VALUE
init|=
literal|"TestCustomConf"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|TEST_CUSTOM_VALUE
init|=
literal|"TestCustomValue"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|families
init|=
block|{
name|MAX_VERSIONS_FAM
block|,
name|BLOOMFILTER_FAM
block|,
name|COMPRESSED_FAM
block|,
name|BLOCKSIZE_FAM
block|}
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|DataBlockEncoding
name|DATA_BLOCK_ENCODING_TYPE
init|=
name|DataBlockEncoding
operator|.
name|FAST_DIFF
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|BloomType
name|BLOOM_TYPE
init|=
name|BloomType
operator|.
name|ROW
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|BLOCK_SIZE
init|=
literal|98
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_VERSIONS
init|=
literal|8
decl_stmt|;
specifier|private
name|HBaseAdmin
name|admin
decl_stmt|;
specifier|private
name|String
name|originalTableDescription
decl_stmt|;
specifier|private
name|HTableDescriptor
name|originalTableDescriptor
decl_stmt|;
name|TableName
name|originalTableName
decl_stmt|;
specifier|private
specifier|static
name|FileSystem
name|fs
decl_stmt|;
specifier|private
specifier|static
name|Path
name|rootDir
decl_stmt|;
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
name|NUM_RS
argument_list|)
expr_stmt|;
name|fs
operator|=
name|UTIL
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
name|getFileSystem
argument_list|()
expr_stmt|;
name|rootDir
operator|=
name|UTIL
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
expr_stmt|;
block|}
specifier|private
specifier|static
name|void
name|setupConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
comment|// enable snapshot support
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
comment|// disable the ui
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.regionsever.info.port"
argument_list|,
operator|-
literal|1
argument_list|)
expr_stmt|;
comment|// change the flush size to a small amount, regulating number of store files
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hregion.memstore.flush.size"
argument_list|,
literal|25000
argument_list|)
expr_stmt|;
comment|// so make sure we get a compaction when doing a load, but keep around
comment|// some files in the store
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compaction.min"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.compactionThreshold"
argument_list|,
literal|10
argument_list|)
expr_stmt|;
comment|// block writes if we get to 12 store files
name|conf
operator|.
name|setInt
argument_list|(
literal|"hbase.hstore.blockingStoreFiles"
argument_list|,
literal|12
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
name|setBoolean
argument_list|(
literal|"hbase.master.enabletable.roundrobin"
argument_list|,
literal|true
argument_list|)
expr_stmt|;
comment|// Avoid potentially aggressive splitting which would cause snapshot to fail
name|conf
operator|.
name|set
argument_list|(
name|HConstants
operator|.
name|HBASE_REGION_SPLIT_POLICY_KEY
argument_list|,
name|ConstantSizeRegionSplitPolicy
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
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
name|createTableWithNonDefaultProperties
argument_list|()
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
name|admin
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/*    *  Create a table that has non-default properties so we can see if they hold    */
specifier|private
name|void
name|createTableWithNonDefaultProperties
parameter_list|()
throws|throws
name|Exception
block|{
comment|// create a table
name|admin
operator|=
operator|new
name|HBaseAdmin
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|long
name|startTime
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|final
name|String
name|sourceTableNameAsString
init|=
name|STRING_TABLE_NAME
operator|+
name|startTime
decl_stmt|;
name|originalTableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|sourceTableNameAsString
argument_list|)
expr_stmt|;
comment|// enable replication on a column family
name|HColumnDescriptor
name|maxVersionsColumn
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|MAX_VERSIONS_FAM
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|bloomFilterColumn
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|BLOOMFILTER_FAM
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|dataBlockColumn
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|COMPRESSED_FAM
argument_list|)
decl_stmt|;
name|HColumnDescriptor
name|blockSizeColumn
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|BLOCKSIZE_FAM
argument_list|)
decl_stmt|;
name|maxVersionsColumn
operator|.
name|setMaxVersions
argument_list|(
name|MAX_VERSIONS
argument_list|)
expr_stmt|;
name|bloomFilterColumn
operator|.
name|setBloomFilterType
argument_list|(
name|BLOOM_TYPE
argument_list|)
expr_stmt|;
name|dataBlockColumn
operator|.
name|setDataBlockEncoding
argument_list|(
name|DATA_BLOCK_ENCODING_TYPE
argument_list|)
expr_stmt|;
name|blockSizeColumn
operator|.
name|setBlocksize
argument_list|(
name|BLOCK_SIZE
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|sourceTableNameAsString
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|maxVersionsColumn
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|bloomFilterColumn
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|dataBlockColumn
argument_list|)
expr_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
name|blockSizeColumn
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setValue
argument_list|(
name|TEST_CUSTOM_VALUE
argument_list|,
name|TEST_CUSTOM_VALUE
argument_list|)
expr_stmt|;
name|htd
operator|.
name|setConfiguration
argument_list|(
name|TEST_CONF_CUSTOM_VALUE
argument_list|,
name|TEST_CONF_CUSTOM_VALUE
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|htd
operator|.
name|getConfiguration
argument_list|()
operator|.
name|size
argument_list|()
operator|>
literal|0
argument_list|)
expr_stmt|;
name|admin
operator|.
name|createTable
argument_list|(
name|htd
argument_list|)
expr_stmt|;
name|HTable
name|original
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|originalTableName
argument_list|)
decl_stmt|;
name|originalTableName
operator|=
name|TableName
operator|.
name|valueOf
argument_list|(
name|sourceTableNameAsString
argument_list|)
expr_stmt|;
name|originalTableDescriptor
operator|=
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|originalTableName
argument_list|)
expr_stmt|;
name|originalTableDescription
operator|=
name|originalTableDescriptor
operator|.
name|toStringCustomizedValues
argument_list|()
expr_stmt|;
name|original
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Verify that the describe for a cloned table matches the describe from the original.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testDescribeMatchesAfterClone
parameter_list|()
throws|throws
name|Exception
block|{
comment|// Clone the original table
specifier|final
name|String
name|clonedTableNameAsString
init|=
literal|"clone"
operator|+
name|originalTableName
decl_stmt|;
specifier|final
name|byte
index|[]
name|clonedTableName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|clonedTableNameAsString
argument_list|)
decl_stmt|;
specifier|final
name|String
name|snapshotNameAsString
init|=
literal|"snapshot"
operator|+
name|originalTableName
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|final
name|byte
index|[]
name|snapshotName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|snapshotNameAsString
argument_list|)
decl_stmt|;
comment|// restore the snapshot into a cloned table and examine the output
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|familiesList
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
name|familiesList
operator|.
name|add
argument_list|(
name|family
argument_list|)
expr_stmt|;
block|}
comment|// Create a snapshot in which all families are empty
name|SnapshotTestingUtils
operator|.
name|createSnapshotAndValidate
argument_list|(
name|admin
argument_list|,
name|originalTableName
argument_list|,
literal|null
argument_list|,
name|familiesList
argument_list|,
name|snapshotNameAsString
argument_list|,
name|rootDir
argument_list|,
name|fs
argument_list|)
expr_stmt|;
name|admin
operator|.
name|cloneSnapshot
argument_list|(
name|snapshotName
argument_list|,
name|clonedTableName
argument_list|)
expr_stmt|;
name|HTable
name|clonedTable
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|clonedTableName
argument_list|)
decl_stmt|;
name|HTableDescriptor
name|cloneHtd
init|=
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|clonedTableName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|originalTableDescription
operator|.
name|replace
argument_list|(
name|originalTableName
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|clonedTableNameAsString
argument_list|)
argument_list|,
name|cloneHtd
operator|.
name|toStringCustomizedValues
argument_list|()
argument_list|)
expr_stmt|;
comment|// Verify the custom fields
name|assertEquals
argument_list|(
name|originalTableDescriptor
operator|.
name|getValues
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|cloneHtd
operator|.
name|getValues
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|originalTableDescriptor
operator|.
name|getConfiguration
argument_list|()
operator|.
name|size
argument_list|()
argument_list|,
name|cloneHtd
operator|.
name|getConfiguration
argument_list|()
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|cloneHtd
operator|.
name|getValue
argument_list|(
name|TEST_CUSTOM_VALUE
argument_list|)
argument_list|,
name|TEST_CUSTOM_VALUE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|cloneHtd
operator|.
name|getConfigurationValue
argument_list|(
name|TEST_CONF_CUSTOM_VALUE
argument_list|)
argument_list|,
name|TEST_CONF_CUSTOM_VALUE
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|originalTableDescriptor
operator|.
name|getValues
argument_list|()
argument_list|,
name|cloneHtd
operator|.
name|getValues
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|originalTableDescriptor
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|cloneHtd
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|originalTableName
argument_list|)
expr_stmt|;
name|clonedTable
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
comment|/**    * Verify that the describe for a restored table matches the describe for one the original.    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testDescribeMatchesAfterRestore
parameter_list|()
throws|throws
name|Exception
block|{
name|runRestoreWithAdditionalMetadata
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify that if metadata changed after a snapshot was taken, that the old metadata replaces the    * new metadata during a restore    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testDescribeMatchesAfterMetadataChangeAndRestore
parameter_list|()
throws|throws
name|Exception
block|{
name|runRestoreWithAdditionalMetadata
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Verify that when the table is empty, making metadata changes after the restore does not affect    * the restored table's original metadata    * @throws Exception    */
annotation|@
name|Test
argument_list|(
name|timeout
operator|=
literal|300000
argument_list|)
specifier|public
name|void
name|testDescribeOnEmptyTableMatchesAfterMetadataChangeAndRestore
parameter_list|()
throws|throws
name|Exception
block|{
name|runRestoreWithAdditionalMetadata
argument_list|(
literal|true
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runRestoreWithAdditionalMetadata
parameter_list|(
name|boolean
name|changeMetadata
parameter_list|)
throws|throws
name|Exception
block|{
name|runRestoreWithAdditionalMetadata
argument_list|(
name|changeMetadata
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runRestoreWithAdditionalMetadata
parameter_list|(
name|boolean
name|changeMetadata
parameter_list|,
name|boolean
name|addData
parameter_list|)
throws|throws
name|Exception
block|{
if|if
condition|(
name|admin
operator|.
name|isTableDisabled
argument_list|(
name|originalTableName
argument_list|)
condition|)
block|{
name|admin
operator|.
name|enableTable
argument_list|(
name|originalTableName
argument_list|)
expr_stmt|;
block|}
comment|// populate it with data
specifier|final
name|byte
index|[]
name|familyForUpdate
init|=
name|BLOCKSIZE_FAM
decl_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|familiesWithDataList
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|byte
index|[]
argument_list|>
name|emptyFamiliesList
init|=
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|()
decl_stmt|;
if|if
condition|(
name|addData
condition|)
block|{
name|HTable
name|original
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|originalTableName
argument_list|)
decl_stmt|;
name|UTIL
operator|.
name|loadTable
argument_list|(
name|original
argument_list|,
name|familyForUpdate
argument_list|)
expr_stmt|;
comment|// family arbitrarily chosen
name|original
operator|.
name|close
argument_list|()
expr_stmt|;
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
if|if
condition|(
name|family
operator|!=
name|familyForUpdate
condition|)
block|{
name|emptyFamiliesList
operator|.
name|add
argument_list|(
name|family
argument_list|)
expr_stmt|;
block|}
block|}
name|familiesWithDataList
operator|.
name|add
argument_list|(
name|familyForUpdate
argument_list|)
expr_stmt|;
block|}
else|else
block|{
for|for
control|(
name|byte
index|[]
name|family
range|:
name|families
control|)
block|{
name|emptyFamiliesList
operator|.
name|add
argument_list|(
name|family
argument_list|)
expr_stmt|;
block|}
block|}
comment|// take a snapshot
specifier|final
name|String
name|snapshotNameAsString
init|=
literal|"snapshot"
operator|+
name|originalTableName
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|final
name|byte
index|[]
name|snapshotName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|snapshotNameAsString
argument_list|)
decl_stmt|;
name|SnapshotTestingUtils
operator|.
name|createSnapshotAndValidate
argument_list|(
name|admin
argument_list|,
name|originalTableName
argument_list|,
name|familiesWithDataList
argument_list|,
name|emptyFamiliesList
argument_list|,
name|snapshotNameAsString
argument_list|,
name|rootDir
argument_list|,
name|fs
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|originalTableName
argument_list|)
expr_stmt|;
if|if
condition|(
name|changeMetadata
condition|)
block|{
specifier|final
name|String
name|newFamilyNameAsString
init|=
literal|"newFamily"
operator|+
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
specifier|final
name|byte
index|[]
name|newFamilyName
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|newFamilyNameAsString
argument_list|)
decl_stmt|;
name|admin
operator|.
name|disableTable
argument_list|(
name|originalTableName
argument_list|)
expr_stmt|;
name|HColumnDescriptor
name|hcd
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|newFamilyName
argument_list|)
decl_stmt|;
name|admin
operator|.
name|addColumn
argument_list|(
name|originalTableName
argument_list|,
name|hcd
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"New column family was not added."
argument_list|,
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|originalTableName
argument_list|)
operator|.
name|toString
argument_list|()
operator|.
name|contains
argument_list|(
name|newFamilyNameAsString
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// restore it
if|if
condition|(
operator|!
name|admin
operator|.
name|isTableDisabled
argument_list|(
name|originalTableName
argument_list|)
condition|)
block|{
name|admin
operator|.
name|disableTable
argument_list|(
name|originalTableName
argument_list|)
expr_stmt|;
block|}
name|admin
operator|.
name|restoreSnapshot
argument_list|(
name|snapshotName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|enableTable
argument_list|(
name|originalTableName
argument_list|)
expr_stmt|;
comment|// verify that the descrption is reverted
name|HTable
name|original
init|=
operator|new
name|HTable
argument_list|(
name|UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|originalTableName
argument_list|)
decl_stmt|;
try|try
block|{
name|assertTrue
argument_list|(
name|originalTableDescriptor
operator|.
name|equals
argument_list|(
name|admin
operator|.
name|getTableDescriptor
argument_list|(
name|originalTableName
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|originalTableDescriptor
operator|.
name|equals
argument_list|(
name|original
operator|.
name|getTableDescriptor
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|original
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

