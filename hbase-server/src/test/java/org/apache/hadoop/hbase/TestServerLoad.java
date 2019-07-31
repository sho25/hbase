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
name|MiscTests
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
name|protobuf
operator|.
name|ByteString
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
name|ClusterStatusProtos
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
name|HBaseProtos
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
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
name|TestServerLoad
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
name|TestServerLoad
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testRegionLoadAggregation
parameter_list|()
block|{
name|ServerLoad
name|sl
init|=
operator|new
name|ServerLoad
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost,1,1"
argument_list|)
argument_list|,
name|createServerLoadProto
argument_list|()
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
literal|13
argument_list|,
name|sl
operator|.
name|getStores
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|114
argument_list|,
name|sl
operator|.
name|getStorefiles
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|129
argument_list|,
name|sl
operator|.
name|getStoreUncompressedSizeMB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|504
argument_list|,
name|sl
operator|.
name|getRootIndexSizeKB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|820
argument_list|,
name|sl
operator|.
name|getStorefileSizeMB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|82
argument_list|,
name|sl
operator|.
name|getStorefileIndexSizeKB
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
operator|(
operator|(
name|long
operator|)
name|Integer
operator|.
name|MAX_VALUE
operator|)
operator|*
literal|2
argument_list|,
name|sl
operator|.
name|getReadRequestsCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|300
argument_list|,
name|sl
operator|.
name|getFilteredReadRequestsCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testToString
parameter_list|()
block|{
name|ServerLoad
name|sl
init|=
operator|new
name|ServerLoad
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost,1,1"
argument_list|)
argument_list|,
name|createServerLoadProto
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|slToString
init|=
name|sl
operator|.
name|toString
argument_list|()
decl_stmt|;
name|assertNotNull
argument_list|(
name|sl
operator|.
name|obtainServerLoadPB
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|slToString
operator|.
name|contains
argument_list|(
literal|"numberOfStores=13"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|slToString
operator|.
name|contains
argument_list|(
literal|"numberOfStorefiles=114"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|slToString
operator|.
name|contains
argument_list|(
literal|"storefileUncompressedSizeMB=129"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|slToString
operator|.
name|contains
argument_list|(
literal|"storefileSizeMB=820"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|slToString
operator|.
name|contains
argument_list|(
literal|"rootIndexSizeKB=504"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|slToString
operator|.
name|contains
argument_list|(
literal|"coprocessors=[]"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|slToString
operator|.
name|contains
argument_list|(
literal|"filteredReadRequestsCount=300"
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRegionLoadWrapAroundAggregation
parameter_list|()
block|{
name|ServerLoad
name|sl
init|=
operator|new
name|ServerLoad
argument_list|(
name|ServerName
operator|.
name|valueOf
argument_list|(
literal|"localhost,1,1"
argument_list|)
argument_list|,
name|createServerLoadProto
argument_list|()
argument_list|)
decl_stmt|;
name|assertNotNull
argument_list|(
name|sl
operator|.
name|obtainServerLoadPB
argument_list|()
argument_list|)
expr_stmt|;
name|long
name|totalCount
init|=
operator|(
operator|(
name|long
operator|)
name|Integer
operator|.
name|MAX_VALUE
operator|)
operator|*
literal|2
decl_stmt|;
name|assertEquals
argument_list|(
name|totalCount
argument_list|,
name|sl
operator|.
name|getReadRequestsCount
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|totalCount
argument_list|,
name|sl
operator|.
name|getWriteRequestsCount
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|ClusterStatusProtos
operator|.
name|ServerLoad
name|createServerLoadProto
parameter_list|()
block|{
name|HBaseProtos
operator|.
name|RegionSpecifier
name|rSpecOne
init|=
name|HBaseProtos
operator|.
name|RegionSpecifier
operator|.
name|newBuilder
argument_list|()
operator|.
name|setType
argument_list|(
name|HBaseProtos
operator|.
name|RegionSpecifier
operator|.
name|RegionSpecifierType
operator|.
name|ENCODED_REGION_NAME
argument_list|)
operator|.
name|setValue
argument_list|(
name|ByteString
operator|.
name|copyFromUtf8
argument_list|(
literal|"ASDFGQWERT"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|HBaseProtos
operator|.
name|RegionSpecifier
name|rSpecTwo
init|=
name|HBaseProtos
operator|.
name|RegionSpecifier
operator|.
name|newBuilder
argument_list|()
operator|.
name|setType
argument_list|(
name|HBaseProtos
operator|.
name|RegionSpecifier
operator|.
name|RegionSpecifierType
operator|.
name|ENCODED_REGION_NAME
argument_list|)
operator|.
name|setValue
argument_list|(
name|ByteString
operator|.
name|copyFromUtf8
argument_list|(
literal|"QWERTYUIOP"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ClusterStatusProtos
operator|.
name|RegionLoad
name|rlOne
init|=
name|ClusterStatusProtos
operator|.
name|RegionLoad
operator|.
name|newBuilder
argument_list|()
operator|.
name|setRegionSpecifier
argument_list|(
name|rSpecOne
argument_list|)
operator|.
name|setStores
argument_list|(
literal|10
argument_list|)
operator|.
name|setStorefiles
argument_list|(
literal|101
argument_list|)
operator|.
name|setStoreUncompressedSizeMB
argument_list|(
literal|106
argument_list|)
operator|.
name|setStorefileSizeMB
argument_list|(
literal|520
argument_list|)
operator|.
name|setFilteredReadRequestsCount
argument_list|(
literal|100
argument_list|)
operator|.
name|setStorefileIndexSizeKB
argument_list|(
literal|42
argument_list|)
operator|.
name|setRootIndexSizeKB
argument_list|(
literal|201
argument_list|)
operator|.
name|setReadRequestsCount
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|setWriteRequestsCount
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ClusterStatusProtos
operator|.
name|RegionLoad
name|rlTwo
init|=
name|ClusterStatusProtos
operator|.
name|RegionLoad
operator|.
name|newBuilder
argument_list|()
operator|.
name|setRegionSpecifier
argument_list|(
name|rSpecTwo
argument_list|)
operator|.
name|setStores
argument_list|(
literal|3
argument_list|)
operator|.
name|setStorefiles
argument_list|(
literal|13
argument_list|)
operator|.
name|setStoreUncompressedSizeMB
argument_list|(
literal|23
argument_list|)
operator|.
name|setStorefileSizeMB
argument_list|(
literal|300
argument_list|)
operator|.
name|setFilteredReadRequestsCount
argument_list|(
literal|200
argument_list|)
operator|.
name|setStorefileIndexSizeKB
argument_list|(
literal|40
argument_list|)
operator|.
name|setRootIndexSizeKB
argument_list|(
literal|303
argument_list|)
operator|.
name|setReadRequestsCount
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|setWriteRequestsCount
argument_list|(
name|Integer
operator|.
name|MAX_VALUE
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
name|ClusterStatusProtos
operator|.
name|ServerLoad
name|sl
init|=
name|ClusterStatusProtos
operator|.
name|ServerLoad
operator|.
name|newBuilder
argument_list|()
operator|.
name|addRegionLoads
argument_list|(
name|rlOne
argument_list|)
operator|.
name|addRegionLoads
argument_list|(
name|rlTwo
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
return|return
name|sl
return|;
block|}
block|}
end_class

end_unit

