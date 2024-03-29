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
name|Matchers
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
name|Matchers
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
name|TestMajorCompactionTTLRequest
extends|extends
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
name|TestMajorCompactionTTLRequest
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Before
annotation|@
name|Override
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
literal|"TestMajorCompactionTTLRequest"
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
comment|// store files older than timestamp 10
name|List
argument_list|<
name|StoreFileInfo
argument_list|>
name|storeFiles1
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
comment|// store files older than timestamp 100
name|List
argument_list|<
name|StoreFileInfo
argument_list|>
name|storeFiles2
init|=
name|mockStoreFiles
argument_list|(
name|regionStoreDir
argument_list|,
literal|5
argument_list|,
literal|100
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|StoreFileInfo
argument_list|>
name|storeFiles
init|=
name|Lists
operator|.
name|newArrayList
argument_list|(
name|storeFiles1
argument_list|)
decl_stmt|;
name|storeFiles
operator|.
name|addAll
argument_list|(
name|storeFiles2
argument_list|)
expr_stmt|;
name|MajorCompactionTTLRequest
name|request
init|=
name|makeMockRequest
argument_list|(
name|storeFiles
argument_list|)
decl_stmt|;
comment|// All files are<= 100, so region should not be compacted.
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
literal|10
argument_list|)
decl_stmt|;
name|assertFalse
argument_list|(
name|result
operator|.
name|isPresent
argument_list|()
argument_list|)
expr_stmt|;
comment|// All files are<= 100, so region should not be compacted yet.
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
comment|// All files are<= 100, so they should be considered for compaction
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
literal|101
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|result
operator|.
name|isPresent
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|MajorCompactionTTLRequest
name|makeMockRequest
parameter_list|(
name|List
argument_list|<
name|StoreFileInfo
argument_list|>
name|storeFiles
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
name|MajorCompactionTTLRequest
name|request
init|=
operator|new
name|MajorCompactionTTLRequest
argument_list|(
name|configuration
argument_list|,
name|regionInfo
argument_list|)
decl_stmt|;
name|MajorCompactionTTLRequest
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
literal|false
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

