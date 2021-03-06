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
name|util
operator|.
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
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
name|TestRegionSizeStoreImpl
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
name|TestRegionSizeStoreImpl
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|RegionInfo
name|INFOA
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TEST"
argument_list|)
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|RegionInfo
name|INFOB
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
literal|"TEST"
argument_list|)
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"c"
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testSizeUpdates
parameter_list|()
block|{
name|RegionSizeStore
name|store
init|=
operator|new
name|RegionSizeStoreImpl
argument_list|()
decl_stmt|;
name|assertTrue
argument_list|(
name|store
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|store
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|store
operator|.
name|put
argument_list|(
name|INFOA
argument_list|,
literal|1024L
argument_list|)
expr_stmt|;
name|assertFalse
argument_list|(
name|store
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|store
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1024L
argument_list|,
name|store
operator|.
name|getRegionSize
argument_list|(
name|INFOA
argument_list|)
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
name|store
operator|.
name|put
argument_list|(
name|INFOA
argument_list|,
literal|2048L
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|store
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2048L
argument_list|,
name|store
operator|.
name|getRegionSize
argument_list|(
name|INFOA
argument_list|)
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
name|store
operator|.
name|incrementRegionSize
argument_list|(
name|INFOA
argument_list|,
literal|512L
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|store
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2048L
operator|+
literal|512L
argument_list|,
name|store
operator|.
name|getRegionSize
argument_list|(
name|INFOA
argument_list|)
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
name|store
operator|.
name|remove
argument_list|(
name|INFOA
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|store
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|store
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|store
operator|.
name|put
argument_list|(
name|INFOA
argument_list|,
literal|64L
argument_list|)
expr_stmt|;
name|store
operator|.
name|put
argument_list|(
name|INFOB
argument_list|,
literal|128L
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|store
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|Map
argument_list|<
name|RegionInfo
argument_list|,
name|RegionSize
argument_list|>
name|records
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
for|for
control|(
name|Entry
argument_list|<
name|RegionInfo
argument_list|,
name|RegionSize
argument_list|>
name|entry
range|:
name|store
control|)
block|{
name|records
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|assertEquals
argument_list|(
literal|64L
argument_list|,
name|records
operator|.
name|remove
argument_list|(
name|INFOA
argument_list|)
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|128L
argument_list|,
name|records
operator|.
name|remove
argument_list|(
name|INFOB
argument_list|)
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|records
operator|.
name|isEmpty
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testNegativeDeltaForMissingRegion
parameter_list|()
block|{
name|RegionSizeStore
name|store
init|=
operator|new
name|RegionSizeStoreImpl
argument_list|()
decl_stmt|;
name|assertNull
argument_list|(
name|store
operator|.
name|getRegionSize
argument_list|(
name|INFOA
argument_list|)
argument_list|)
expr_stmt|;
comment|// We shouldn't allow a negative size to enter the RegionSizeStore. Getting a negative size
comment|// like this shouldn't be possible, but we can prevent the bad state from propagating and
comment|// getting worse.
name|store
operator|.
name|incrementRegionSize
argument_list|(
name|INFOA
argument_list|,
operator|-
literal|5
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
name|store
operator|.
name|getRegionSize
argument_list|(
name|INFOA
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|store
operator|.
name|getRegionSize
argument_list|(
name|INFOA
argument_list|)
operator|.
name|getSize
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

