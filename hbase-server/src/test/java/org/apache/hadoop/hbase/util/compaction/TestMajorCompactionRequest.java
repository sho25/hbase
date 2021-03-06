begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|compaction
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
import|import static
name|org
operator|.
name|mockito
operator|.
name|ArgumentMatchers
operator|.
name|any
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|ArgumentMatchers
operator|.
name|eq
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|ArgumentMatchers
operator|.
name|isA
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Matchers
operator|.
name|anyString
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|doReturn
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|spy
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|when
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
name|java
operator|.
name|util
operator|.
name|Optional
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
name|commons
operator|.
name|lang3
operator|.
name|RandomStringUtils
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
name|regionserver
operator|.
name|HRegion
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
name|HRegionFileSystem
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
name|StoreFileInfo
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
name|ClassRule
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
name|Lists
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
name|Sets
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestMajorCompactionRequest
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
name|TestMajorCompactionRequest
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|HBaseTestingUtility
name|UTILITY
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|protected
specifier|static
specifier|final
name|String
name|FAMILY
init|=
literal|"a"
decl_stmt|;
specifier|protected
name|Path
name|rootRegionDir
decl_stmt|;
specifier|protected
name|Path
name|regionStoreDir
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
name|rootRegionDir
operator|=
name|UTILITY
operator|.
name|getDataTestDirOnTestFS
argument_list|(
literal|"TestMajorCompactionRequest"
argument_list|)
expr_stmt|;
name|regionStoreDir
operator|=
operator|new
name|Path
argument_list|(
name|rootRegionDir
argument_list|,
name|FAMILY
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStoresNeedingCompaction
parameter_list|()
throws|throws
name|Exception
block|{
comment|// store files older than timestamp
name|List
argument_list|<
name|StoreFileInfo
argument_list|>
name|storeFiles
init|=
name|mockStoreFiles
argument_list|(
name|regionStoreDir
argument_list|,
literal|5
argument_list|,
literal|10
argument_list|)
decl_stmt|;
name|MajorCompactionRequest
name|request
init|=
name|makeMockRequest
argument_list|(
name|storeFiles
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|Optional
argument_list|<
name|MajorCompactionRequest
argument_list|>
name|result
init|=
name|request
operator|.
name|createRequest
argument_list|(
name|mock
argument_list|(
name|Configuration
operator|.
name|class
argument_list|)
argument_list|,
name|Sets
operator|.
name|newHashSet
argument_list|(
name|FAMILY
argument_list|)
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|isPresent
argument_list|()
argument_list|)
expr_stmt|;
comment|// store files newer than timestamp
name|storeFiles
operator|=
name|mockStoreFiles
argument_list|(
name|regionStoreDir
argument_list|,
literal|5
argument_list|,
literal|101
argument_list|)
expr_stmt|;
name|request
operator|=
name|makeMockRequest
argument_list|(
name|storeFiles
argument_list|,
literal|false
argument_list|)
expr_stmt|;
name|result
operator|=
name|request
operator|.
name|createRequest
argument_list|(
name|mock
argument_list|(
name|Configuration
operator|.
name|class
argument_list|)
argument_list|,
name|Sets
operator|.
name|newHashSet
argument_list|(
name|FAMILY
argument_list|)
argument_list|,
literal|100
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|result
operator|.
name|isPresent
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testIfWeHaveNewReferenceFilesButOldStoreFiles
parameter_list|()
throws|throws
name|Exception
block|{
comment|// this tests that reference files that are new, but have older timestamps for the files
comment|// they reference still will get compacted.
name|TableName
name|table
init|=
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TestMajorCompactor"
argument_list|)
decl_stmt|;
name|TableDescriptor
name|htd
init|=
name|UTILITY
operator|.
name|createTableDescriptor
argument_list|(
name|table
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
decl_stmt|;
name|RegionInfo
name|hri
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|htd
operator|.
name|getTableName
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HRegion
name|region
init|=
name|HBaseTestingUtility
operator|.
name|createRegionAndWAL
argument_list|(
name|hri
argument_list|,
name|rootRegionDir
argument_list|,
name|UTILITY
operator|.
name|getConfiguration
argument_list|()
argument_list|,
name|htd
argument_list|)
decl_stmt|;
name|Configuration
name|configuration
init|=
name|mock
argument_list|(
name|Configuration
operator|.
name|class
argument_list|)
decl_stmt|;
comment|// the reference file timestamp is newer
name|List
argument_list|<
name|StoreFileInfo
argument_list|>
name|storeFiles
init|=
name|mockStoreFiles
argument_list|(
name|regionStoreDir
argument_list|,
literal|4
argument_list|,
literal|101
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Path
argument_list|>
name|paths
init|=
name|storeFiles
operator|.
name|stream
argument_list|()
operator|.
name|map
argument_list|(
name|StoreFileInfo
operator|::
name|getPath
argument_list|)
operator|.
name|collect
argument_list|(
name|Collectors
operator|.
name|toList
argument_list|()
argument_list|)
decl_stmt|;
comment|// the files that are referenced are older, thus we still compact.
name|HRegionFileSystem
name|fileSystem
init|=
name|mockFileSystem
argument_list|(
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
literal|true
argument_list|,
name|storeFiles
argument_list|,
literal|50
argument_list|)
decl_stmt|;
name|MajorCompactionRequest
name|majorCompactionRequest
init|=
name|spy
argument_list|(
operator|new
name|MajorCompactionRequest
argument_list|(
name|configuration
argument_list|,
name|region
operator|.
name|getRegionInfo
argument_list|()
argument_list|,
name|Sets
operator|.
name|newHashSet
argument_list|(
name|FAMILY
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
name|doReturn
argument_list|(
name|mock
argument_list|(
name|Connection
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|majorCompactionRequest
argument_list|)
operator|.
name|getConnection
argument_list|(
name|eq
argument_list|(
name|configuration
argument_list|)
argument_list|)
expr_stmt|;
name|doReturn
argument_list|(
name|paths
argument_list|)
operator|.
name|when
argument_list|(
name|majorCompactionRequest
argument_list|)
operator|.
name|getReferenceFilePaths
argument_list|(
name|any
argument_list|(
name|FileSystem
operator|.
name|class
argument_list|)
argument_list|,
name|any
argument_list|(
name|Path
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|doReturn
argument_list|(
name|fileSystem
argument_list|)
operator|.
name|when
argument_list|(
name|majorCompactionRequest
argument_list|)
operator|.
name|getFileSystem
argument_list|(
name|any
argument_list|(
name|Connection
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|Set
argument_list|<
name|String
argument_list|>
name|result
init|=
name|majorCompactionRequest
operator|.
name|getStoresRequiringCompaction
argument_list|(
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"a"
argument_list|)
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|FAMILY
argument_list|,
name|Iterables
operator|.
name|getOnlyElement
argument_list|(
name|result
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|protected
name|HRegionFileSystem
name|mockFileSystem
parameter_list|(
name|RegionInfo
name|info
parameter_list|,
name|boolean
name|hasReferenceFiles
parameter_list|,
name|List
argument_list|<
name|StoreFileInfo
argument_list|>
name|storeFiles
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|timestamp
init|=
name|storeFiles
operator|.
name|stream
argument_list|()
operator|.
name|findFirst
argument_list|()
operator|.
name|get
argument_list|()
operator|.
name|getModificationTime
argument_list|()
decl_stmt|;
return|return
name|mockFileSystem
argument_list|(
name|info
argument_list|,
name|hasReferenceFiles
argument_list|,
name|storeFiles
argument_list|,
name|timestamp
argument_list|)
return|;
block|}
specifier|private
name|HRegionFileSystem
name|mockFileSystem
parameter_list|(
name|RegionInfo
name|info
parameter_list|,
name|boolean
name|hasReferenceFiles
parameter_list|,
name|List
argument_list|<
name|StoreFileInfo
argument_list|>
name|storeFiles
parameter_list|,
name|long
name|referenceFileTimestamp
parameter_list|)
throws|throws
name|IOException
block|{
name|FileSystem
name|fileSystem
init|=
name|mock
argument_list|(
name|FileSystem
operator|.
name|class
argument_list|)
decl_stmt|;
if|if
condition|(
name|hasReferenceFiles
condition|)
block|{
name|FileStatus
name|fileStatus
init|=
name|mock
argument_list|(
name|FileStatus
operator|.
name|class
argument_list|)
decl_stmt|;
name|doReturn
argument_list|(
name|referenceFileTimestamp
argument_list|)
operator|.
name|when
argument_list|(
name|fileStatus
argument_list|)
operator|.
name|getModificationTime
argument_list|()
expr_stmt|;
name|doReturn
argument_list|(
name|fileStatus
argument_list|)
operator|.
name|when
argument_list|(
name|fileSystem
argument_list|)
operator|.
name|getFileLinkStatus
argument_list|(
name|isA
argument_list|(
name|Path
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|HRegionFileSystem
name|mockSystem
init|=
name|mock
argument_list|(
name|HRegionFileSystem
operator|.
name|class
argument_list|)
decl_stmt|;
name|doReturn
argument_list|(
name|info
argument_list|)
operator|.
name|when
argument_list|(
name|mockSystem
argument_list|)
operator|.
name|getRegionInfo
argument_list|()
expr_stmt|;
name|doReturn
argument_list|(
name|regionStoreDir
argument_list|)
operator|.
name|when
argument_list|(
name|mockSystem
argument_list|)
operator|.
name|getStoreDir
argument_list|(
name|FAMILY
argument_list|)
expr_stmt|;
name|doReturn
argument_list|(
name|hasReferenceFiles
argument_list|)
operator|.
name|when
argument_list|(
name|mockSystem
argument_list|)
operator|.
name|hasReferences
argument_list|(
name|anyString
argument_list|()
argument_list|)
expr_stmt|;
name|doReturn
argument_list|(
name|storeFiles
argument_list|)
operator|.
name|when
argument_list|(
name|mockSystem
argument_list|)
operator|.
name|getStoreFiles
argument_list|(
name|anyString
argument_list|()
argument_list|)
expr_stmt|;
name|doReturn
argument_list|(
name|fileSystem
argument_list|)
operator|.
name|when
argument_list|(
name|mockSystem
argument_list|)
operator|.
name|getFileSystem
argument_list|()
expr_stmt|;
return|return
name|mockSystem
return|;
block|}
specifier|protected
name|List
argument_list|<
name|StoreFileInfo
argument_list|>
name|mockStoreFiles
parameter_list|(
name|Path
name|regionStoreDir
parameter_list|,
name|int
name|howMany
parameter_list|,
name|long
name|timestamp
parameter_list|)
throws|throws
name|IOException
block|{
name|List
argument_list|<
name|StoreFileInfo
argument_list|>
name|infos
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|int
name|i
init|=
literal|0
decl_stmt|;
while|while
condition|(
name|i
operator|<
name|howMany
condition|)
block|{
name|StoreFileInfo
name|storeFileInfo
init|=
name|mock
argument_list|(
name|StoreFileInfo
operator|.
name|class
argument_list|)
decl_stmt|;
name|doReturn
argument_list|(
name|timestamp
argument_list|)
operator|.
name|doReturn
argument_list|(
name|timestamp
argument_list|)
operator|.
name|when
argument_list|(
name|storeFileInfo
argument_list|)
operator|.
name|getModificationTime
argument_list|()
expr_stmt|;
name|doReturn
argument_list|(
operator|new
name|Path
argument_list|(
name|regionStoreDir
argument_list|,
name|RandomStringUtils
operator|.
name|randomAlphabetic
argument_list|(
literal|10
argument_list|)
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|storeFileInfo
argument_list|)
operator|.
name|getPath
argument_list|()
expr_stmt|;
name|infos
operator|.
name|add
argument_list|(
name|storeFileInfo
argument_list|)
expr_stmt|;
name|i
operator|++
expr_stmt|;
block|}
return|return
name|infos
return|;
block|}
specifier|private
name|MajorCompactionRequest
name|makeMockRequest
parameter_list|(
name|List
argument_list|<
name|StoreFileInfo
argument_list|>
name|storeFiles
parameter_list|,
name|boolean
name|references
parameter_list|)
throws|throws
name|IOException
block|{
name|Configuration
name|configuration
init|=
name|mock
argument_list|(
name|Configuration
operator|.
name|class
argument_list|)
decl_stmt|;
name|RegionInfo
name|regionInfo
init|=
name|mock
argument_list|(
name|RegionInfo
operator|.
name|class
argument_list|)
decl_stmt|;
name|when
argument_list|(
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
literal|"HBase"
argument_list|)
expr_stmt|;
name|when
argument_list|(
name|regionInfo
operator|.
name|getTable
argument_list|()
argument_list|)
operator|.
name|thenReturn
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"foo"
argument_list|)
argument_list|)
expr_stmt|;
name|MajorCompactionRequest
name|request
init|=
operator|new
name|MajorCompactionRequest
argument_list|(
name|configuration
argument_list|,
name|regionInfo
argument_list|,
name|Sets
operator|.
name|newHashSet
argument_list|(
literal|"a"
argument_list|)
argument_list|)
decl_stmt|;
name|MajorCompactionRequest
name|spy
init|=
name|spy
argument_list|(
name|request
argument_list|)
decl_stmt|;
name|HRegionFileSystem
name|fileSystem
init|=
name|mockFileSystem
argument_list|(
name|regionInfo
argument_list|,
name|references
argument_list|,
name|storeFiles
argument_list|)
decl_stmt|;
name|doReturn
argument_list|(
name|fileSystem
argument_list|)
operator|.
name|when
argument_list|(
name|spy
argument_list|)
operator|.
name|getFileSystem
argument_list|(
name|isA
argument_list|(
name|Connection
operator|.
name|class
argument_list|)
argument_list|)
expr_stmt|;
name|doReturn
argument_list|(
name|mock
argument_list|(
name|Connection
operator|.
name|class
argument_list|)
argument_list|)
operator|.
name|when
argument_list|(
name|spy
argument_list|)
operator|.
name|getConnection
argument_list|(
name|eq
argument_list|(
name|configuration
argument_list|)
argument_list|)
expr_stmt|;
return|return
name|spy
return|;
block|}
block|}
end_class

end_unit

