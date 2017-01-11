begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
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
name|assertNull
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
name|Collection
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
name|FSDataInputStream
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
name|FSDataOutputStream
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
name|fs
operator|.
name|permission
operator|.
name|FsPermission
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
name|PerformanceEvaluation
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
name|HTable
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
name|Put
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
name|fs
operator|.
name|HFileSystem
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
name|RegionServerTests
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
name|apache
operator|.
name|hadoop
operator|.
name|util
operator|.
name|Progressable
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
name|RegionServerTests
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
name|TestHRegionFileSystem
block|{
specifier|private
specifier|static
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
name|TestHRegionFileSystem
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
index|[]
name|FAMILIES
init|=
block|{
name|Bytes
operator|.
name|add
argument_list|(
name|PerformanceEvaluation
operator|.
name|FAMILY_NAME
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"-A"
argument_list|)
argument_list|)
block|,
name|Bytes
operator|.
name|add
argument_list|(
name|PerformanceEvaluation
operator|.
name|FAMILY_NAME
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"-B"
argument_list|)
argument_list|)
block|}
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
literal|"TestTable"
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testBlockStoragePolicy
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|=
operator|new
name|HBaseTestingUtility
argument_list|()
expr_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|HTable
name|table
init|=
operator|(
name|HTable
operator|)
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILIES
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|"Should start with empty table"
argument_list|,
literal|0
argument_list|,
name|TEST_UTIL
operator|.
name|countRows
argument_list|(
name|table
argument_list|)
argument_list|)
expr_stmt|;
name|HRegionFileSystem
name|regionFs
init|=
name|getHRegionFS
argument_list|(
name|table
argument_list|,
name|conf
argument_list|)
decl_stmt|;
comment|// the original block storage policy would be HOT
name|String
name|spA
init|=
name|regionFs
operator|.
name|getStoragePolicyName
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|String
name|spB
init|=
name|regionFs
operator|.
name|getStoragePolicyName
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILIES
index|[
literal|1
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Storage policy of cf 0: ["
operator|+
name|spA
operator|+
literal|"]."
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Storage policy of cf 1: ["
operator|+
name|spB
operator|+
literal|"]."
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"HOT"
argument_list|,
name|spA
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"HOT"
argument_list|,
name|spB
argument_list|)
expr_stmt|;
comment|// Recreate table and make sure storage policy could be set through configuration
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
name|HStore
operator|.
name|BLOCK_STORAGE_POLICY_KEY
argument_list|,
literal|"WARM"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|table
operator|=
operator|(
name|HTable
operator|)
name|TEST_UTIL
operator|.
name|createTable
argument_list|(
name|TABLE_NAME
argument_list|,
name|FAMILIES
argument_list|)
expr_stmt|;
name|regionFs
operator|=
name|getHRegionFS
argument_list|(
name|table
argument_list|,
name|conf
argument_list|)
expr_stmt|;
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
name|spA
operator|=
name|regionFs
operator|.
name|getStoragePolicyName
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|spB
operator|=
name|regionFs
operator|.
name|getStoragePolicyName
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILIES
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Storage policy of cf 0: ["
operator|+
name|spA
operator|+
literal|"]."
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Storage policy of cf 1: ["
operator|+
name|spB
operator|+
literal|"]."
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"WARM"
argument_list|,
name|spA
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"WARM"
argument_list|,
name|spB
argument_list|)
expr_stmt|;
comment|// alter table cf schema to change storage policies
comment|// and make sure it could override settings in conf
name|HColumnDescriptor
name|hcdA
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|)
argument_list|)
decl_stmt|;
comment|// alter through setting HStore#BLOCK_STORAGE_POLICY_KEY in HColumnDescriptor
name|hcdA
operator|.
name|setValue
argument_list|(
name|HStore
operator|.
name|BLOCK_STORAGE_POLICY_KEY
argument_list|,
literal|"ONE_SSD"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyColumnFamily
argument_list|(
name|TABLE_NAME
argument_list|,
name|hcdA
argument_list|)
expr_stmt|;
while|while
condition|(
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
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
name|isRegionsInTransition
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|200
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting on table to finish schema altering"
argument_list|)
expr_stmt|;
block|}
comment|// alter through HColumnDescriptor#setStoragePolicy
name|HColumnDescriptor
name|hcdB
init|=
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILIES
index|[
literal|1
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|hcdB
operator|.
name|setStoragePolicy
argument_list|(
literal|"ALL_SSD"
argument_list|)
expr_stmt|;
name|admin
operator|.
name|modifyColumnFamily
argument_list|(
name|TABLE_NAME
argument_list|,
name|hcdB
argument_list|)
expr_stmt|;
while|while
condition|(
name|TEST_UTIL
operator|.
name|getMiniHBaseCluster
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
name|isRegionsInTransition
argument_list|()
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|200
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Waiting on table to finish schema altering"
argument_list|)
expr_stmt|;
block|}
name|spA
operator|=
name|regionFs
operator|.
name|getStoragePolicyName
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|spB
operator|=
name|regionFs
operator|.
name|getStoragePolicyName
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILIES
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Storage policy of cf 0: ["
operator|+
name|spA
operator|+
literal|"]."
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Storage policy of cf 1: ["
operator|+
name|spB
operator|+
literal|"]."
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|spA
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ONE_SSD"
argument_list|,
name|spA
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|spB
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ALL_SSD"
argument_list|,
name|spB
argument_list|)
expr_stmt|;
comment|// flush memstore snapshot into 3 files
for|for
control|(
name|long
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|3
condition|;
name|i
operator|++
control|)
block|{
name|Put
name|put
init|=
operator|new
name|Put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|put
operator|.
name|addColumn
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
expr_stmt|;
name|table
operator|.
name|put
argument_list|(
name|put
argument_list|)
expr_stmt|;
name|admin
operator|.
name|flush
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
block|}
comment|// there should be 3 files in store dir
name|FileSystem
name|fs
init|=
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|Path
name|storePath
init|=
name|regionFs
operator|.
name|getStoreDir
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|FileStatus
index|[]
name|storeFiles
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|storePath
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|storeFiles
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|3
argument_list|,
name|storeFiles
operator|.
name|length
argument_list|)
expr_stmt|;
comment|// store temp dir still exists but empty
name|Path
name|storeTempDir
init|=
operator|new
name|Path
argument_list|(
name|regionFs
operator|.
name|getTempDir
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|storeTempDir
argument_list|)
argument_list|)
expr_stmt|;
name|FileStatus
index|[]
name|tempFiles
init|=
name|FSUtils
operator|.
name|listStatus
argument_list|(
name|fs
argument_list|,
name|storeTempDir
argument_list|)
decl_stmt|;
name|assertNull
argument_list|(
name|tempFiles
argument_list|)
expr_stmt|;
comment|// storage policy of cf temp dir and 3 store files should be ONE_SSD
name|assertEquals
argument_list|(
literal|"ONE_SSD"
argument_list|,
operator|(
operator|(
name|HFileSystem
operator|)
name|regionFs
operator|.
name|getFileSystem
argument_list|()
operator|)
operator|.
name|getStoragePolicyName
argument_list|(
name|storeTempDir
argument_list|)
argument_list|)
expr_stmt|;
for|for
control|(
name|FileStatus
name|status
range|:
name|storeFiles
control|)
block|{
name|assertEquals
argument_list|(
literal|"ONE_SSD"
argument_list|,
operator|(
operator|(
name|HFileSystem
operator|)
name|regionFs
operator|.
name|getFileSystem
argument_list|()
operator|)
operator|.
name|getStoragePolicyName
argument_list|(
name|status
operator|.
name|getPath
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|// change storage policies by calling raw api directly
name|regionFs
operator|.
name|setStoragePolicy
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|)
argument_list|,
literal|"ALL_SSD"
argument_list|)
expr_stmt|;
name|regionFs
operator|.
name|setStoragePolicy
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILIES
index|[
literal|1
index|]
argument_list|)
argument_list|,
literal|"ONE_SSD"
argument_list|)
expr_stmt|;
name|spA
operator|=
name|regionFs
operator|.
name|getStoragePolicyName
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILIES
index|[
literal|0
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|spB
operator|=
name|regionFs
operator|.
name|getStoragePolicyName
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|FAMILIES
index|[
literal|1
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Storage policy of cf 0: ["
operator|+
name|spA
operator|+
literal|"]."
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Storage policy of cf 1: ["
operator|+
name|spB
operator|+
literal|"]."
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|spA
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ALL_SSD"
argument_list|,
name|spA
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|spB
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"ONE_SSD"
argument_list|,
name|spB
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|table
operator|.
name|close
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|deleteTable
argument_list|(
name|TABLE_NAME
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
block|}
specifier|private
name|HRegionFileSystem
name|getHRegionFS
parameter_list|(
name|HTable
name|table
parameter_list|,
name|Configuration
name|conf
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fs
init|=
name|TEST_UTIL
operator|.
name|getDFSCluster
argument_list|()
operator|.
name|getFileSystem
argument_list|()
decl_stmt|;
name|Path
name|tableDir
init|=
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|TEST_UTIL
operator|.
name|getDefaultRootDirPath
argument_list|()
argument_list|,
name|table
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|regionDirs
init|=
name|FSUtils
operator|.
name|getRegionDirs
argument_list|(
name|fs
argument_list|,
name|tableDir
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|regionDirs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|familyDirs
init|=
name|FSUtils
operator|.
name|getFamilyDirs
argument_list|(
name|fs
argument_list|,
name|regionDirs
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|familyDirs
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|HRegionInfo
name|hri
init|=
name|table
operator|.
name|getRegionLocator
argument_list|()
operator|.
name|getAllRegionLocations
argument_list|()
operator|.
name|get
argument_list|(
literal|0
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
decl_stmt|;
name|HRegionFileSystem
name|regionFs
init|=
operator|new
name|HRegionFileSystem
argument_list|(
name|conf
argument_list|,
operator|new
name|HFileSystem
argument_list|(
name|fs
argument_list|)
argument_list|,
name|tableDir
argument_list|,
name|hri
argument_list|)
decl_stmt|;
return|return
name|regionFs
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOnDiskRegionCreation
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|rootDir
init|=
name|TEST_UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|(
literal|"testOnDiskRegionCreation"
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// Create a Region
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestTable"
argument_list|)
argument_list|)
decl_stmt|;
name|HRegionFileSystem
name|regionFs
init|=
name|HRegionFileSystem
operator|.
name|createRegionOnFileSystem
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|rootDir
argument_list|,
name|hri
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|,
name|hri
argument_list|)
decl_stmt|;
comment|// Verify if the region is on disk
name|Path
name|regionDir
init|=
name|regionFs
operator|.
name|getRegionDir
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
literal|"The region folder should be created"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|regionDir
argument_list|)
argument_list|)
expr_stmt|;
comment|// Verify the .regioninfo
name|HRegionInfo
name|hriVerify
init|=
name|HRegionFileSystem
operator|.
name|loadRegionInfoFileContent
argument_list|(
name|fs
argument_list|,
name|regionDir
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|hri
argument_list|,
name|hriVerify
argument_list|)
expr_stmt|;
comment|// Open the region
name|regionFs
operator|=
name|HRegionFileSystem
operator|.
name|openRegionFromFileSystem
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|rootDir
argument_list|,
name|hri
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|,
name|hri
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|regionDir
argument_list|,
name|regionFs
operator|.
name|getRegionDir
argument_list|()
argument_list|)
expr_stmt|;
comment|// Delete the region
name|HRegionFileSystem
operator|.
name|deleteRegionFromFileSystem
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|FSUtils
operator|.
name|getTableDir
argument_list|(
name|rootDir
argument_list|,
name|hri
operator|.
name|getTable
argument_list|()
argument_list|)
argument_list|,
name|hri
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
literal|"The region folder should be removed"
argument_list|,
name|fs
operator|.
name|exists
argument_list|(
name|regionDir
argument_list|)
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|rootDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNonIdempotentOpsWithRetries
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|rootDir
init|=
name|TEST_UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|(
literal|"testOnDiskRegionCreation"
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// Create a Region
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestTable"
argument_list|)
argument_list|)
decl_stmt|;
name|HRegionFileSystem
name|regionFs
init|=
name|HRegionFileSystem
operator|.
name|createRegionOnFileSystem
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|rootDir
argument_list|,
name|hri
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|regionFs
operator|.
name|getRegionDir
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|regionFs
operator|=
operator|new
name|HRegionFileSystem
argument_list|(
name|conf
argument_list|,
operator|new
name|MockFileSystemForCreate
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
comment|// HRegionFileSystem.createRegionOnFileSystem(conf, new MockFileSystemForCreate(), rootDir,
comment|// hri);
name|boolean
name|result
init|=
name|regionFs
operator|.
name|createDir
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/foo/bar"
argument_list|)
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
literal|"Couldn't create the directory"
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|regionFs
operator|=
operator|new
name|HRegionFileSystem
argument_list|(
name|conf
argument_list|,
operator|new
name|MockFileSystem
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|result
operator|=
name|regionFs
operator|.
name|rename
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/foo/bar"
argument_list|)
argument_list|,
operator|new
name|Path
argument_list|(
literal|"/foo/bar2"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Couldn't rename the directory"
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|regionFs
operator|=
operator|new
name|HRegionFileSystem
argument_list|(
name|conf
argument_list|,
operator|new
name|MockFileSystem
argument_list|()
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|result
operator|=
name|regionFs
operator|.
name|deleteDir
argument_list|(
operator|new
name|Path
argument_list|(
literal|"/foo/bar"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
literal|"Couldn't delete the directory"
argument_list|,
name|result
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|rootDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|static
class|class
name|MockFileSystemForCreate
extends|extends
name|MockFileSystem
block|{
annotation|@
name|Override
specifier|public
name|boolean
name|exists
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
return|return
literal|false
return|;
block|}
block|}
comment|/**    * a mock fs which throws exception for first 3 times, and then process the call (returns the    * excepted result).    */
specifier|static
class|class
name|MockFileSystem
extends|extends
name|FileSystem
block|{
name|int
name|retryCount
decl_stmt|;
specifier|final
specifier|static
name|int
name|successRetryCount
init|=
literal|3
decl_stmt|;
specifier|public
name|MockFileSystem
parameter_list|()
block|{
name|retryCount
operator|=
literal|0
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|FSDataOutputStream
name|append
parameter_list|(
name|Path
name|arg0
parameter_list|,
name|int
name|arg1
parameter_list|,
name|Progressable
name|arg2
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|""
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|FSDataOutputStream
name|create
parameter_list|(
name|Path
name|arg0
parameter_list|,
name|FsPermission
name|arg1
parameter_list|,
name|boolean
name|arg2
parameter_list|,
name|int
name|arg3
parameter_list|,
name|short
name|arg4
parameter_list|,
name|long
name|arg5
parameter_list|,
name|Progressable
name|arg6
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Create, "
operator|+
name|retryCount
argument_list|)
expr_stmt|;
if|if
condition|(
name|retryCount
operator|++
operator|<
name|successRetryCount
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Something bad happen"
argument_list|)
throw|;
return|return
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|delete
parameter_list|(
name|Path
name|arg0
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|retryCount
operator|++
operator|<
name|successRetryCount
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Something bad happen"
argument_list|)
throw|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|delete
parameter_list|(
name|Path
name|arg0
parameter_list|,
name|boolean
name|arg1
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|retryCount
operator|++
operator|<
name|successRetryCount
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Something bad happen"
argument_list|)
throw|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|FileStatus
name|getFileStatus
parameter_list|(
name|Path
name|arg0
parameter_list|)
throws|throws
name|IOException
block|{
name|FileStatus
name|fs
init|=
operator|new
name|FileStatus
argument_list|()
decl_stmt|;
return|return
name|fs
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|exists
parameter_list|(
name|Path
name|path
parameter_list|)
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|URI
name|getUri
parameter_list|()
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Something bad happen"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|Path
name|getWorkingDirectory
parameter_list|()
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Something bad happen"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|FileStatus
index|[]
name|listStatus
parameter_list|(
name|Path
name|arg0
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Something bad happen"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|mkdirs
parameter_list|(
name|Path
name|arg0
parameter_list|,
name|FsPermission
name|arg1
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"mkdirs, "
operator|+
name|retryCount
argument_list|)
expr_stmt|;
if|if
condition|(
name|retryCount
operator|++
operator|<
name|successRetryCount
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Something bad happen"
argument_list|)
throw|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|FSDataInputStream
name|open
parameter_list|(
name|Path
name|arg0
parameter_list|,
name|int
name|arg1
parameter_list|)
throws|throws
name|IOException
block|{
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Something bad happen"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|rename
parameter_list|(
name|Path
name|arg0
parameter_list|,
name|Path
name|arg1
parameter_list|)
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"rename, "
operator|+
name|retryCount
argument_list|)
expr_stmt|;
if|if
condition|(
name|retryCount
operator|++
operator|<
name|successRetryCount
condition|)
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Something bad happen"
argument_list|)
throw|;
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setWorkingDirectory
parameter_list|(
name|Path
name|arg0
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Something bad happen"
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testTempAndCommit
parameter_list|()
throws|throws
name|IOException
block|{
name|Path
name|rootDir
init|=
name|TEST_UTIL
operator|.
name|getDataTestDirOnTestFS
argument_list|(
literal|"testTempAndCommit"
argument_list|)
decl_stmt|;
name|FileSystem
name|fs
init|=
name|TEST_UTIL
operator|.
name|getTestFileSystem
argument_list|()
decl_stmt|;
name|Configuration
name|conf
init|=
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
decl_stmt|;
comment|// Create a Region
name|String
name|familyName
init|=
literal|"cf"
decl_stmt|;
name|HRegionInfo
name|hri
init|=
operator|new
name|HRegionInfo
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestTable"
argument_list|)
argument_list|)
decl_stmt|;
name|HRegionFileSystem
name|regionFs
init|=
name|HRegionFileSystem
operator|.
name|createRegionOnFileSystem
argument_list|(
name|conf
argument_list|,
name|fs
argument_list|,
name|rootDir
argument_list|,
name|hri
argument_list|)
decl_stmt|;
comment|// New region, no store files
name|Collection
argument_list|<
name|StoreFileInfo
argument_list|>
name|storeFiles
init|=
name|regionFs
operator|.
name|getStoreFiles
argument_list|(
name|familyName
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|storeFiles
operator|!=
literal|null
condition|?
name|storeFiles
operator|.
name|size
argument_list|()
else|:
literal|0
argument_list|)
expr_stmt|;
comment|// Create a new file in temp (no files in the family)
name|Path
name|buildPath
init|=
name|regionFs
operator|.
name|createTempName
argument_list|()
decl_stmt|;
name|fs
operator|.
name|createNewFile
argument_list|(
name|buildPath
argument_list|)
expr_stmt|;
name|storeFiles
operator|=
name|regionFs
operator|.
name|getStoreFiles
argument_list|(
name|familyName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|storeFiles
operator|!=
literal|null
condition|?
name|storeFiles
operator|.
name|size
argument_list|()
else|:
literal|0
argument_list|)
expr_stmt|;
comment|// commit the file
name|Path
name|dstPath
init|=
name|regionFs
operator|.
name|commitStoreFile
argument_list|(
name|familyName
argument_list|,
name|buildPath
argument_list|)
decl_stmt|;
name|storeFiles
operator|=
name|regionFs
operator|.
name|getStoreFiles
argument_list|(
name|familyName
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|storeFiles
operator|!=
literal|null
condition|?
name|storeFiles
operator|.
name|size
argument_list|()
else|:
literal|0
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|fs
operator|.
name|exists
argument_list|(
name|buildPath
argument_list|)
argument_list|)
expr_stmt|;
name|fs
operator|.
name|delete
argument_list|(
name|rootDir
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

