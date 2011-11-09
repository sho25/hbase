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
import|import
name|junit
operator|.
name|framework
operator|.
name|TestCase
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
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_comment
comment|/**  * Test comparing HBase objects.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|SmallTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestCompare
extends|extends
name|TestCase
block|{
comment|/**    * Sort of HRegionInfo.    */
specifier|public
name|void
name|testHRegionInfo
parameter_list|()
block|{
name|HRegionInfo
name|a
init|=
operator|new
name|HRegionInfo
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|HRegionInfo
name|b
init|=
operator|new
name|HRegionInfo
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"b"
argument_list|)
argument_list|,
literal|null
argument_list|,
literal|null
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|a
operator|.
name|compareTo
argument_list|(
name|b
argument_list|)
operator|!=
literal|0
argument_list|)
expr_stmt|;
name|HTableDescriptor
name|t
init|=
operator|new
name|HTableDescriptor
argument_list|(
literal|"t"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|midway
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"midway"
argument_list|)
decl_stmt|;
name|a
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|,
literal|null
argument_list|,
name|midway
argument_list|)
expr_stmt|;
name|b
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|,
name|midway
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|a
operator|.
name|compareTo
argument_list|(
name|b
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|b
operator|.
name|compareTo
argument_list|(
name|a
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
name|a
argument_list|,
name|a
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|a
operator|.
name|compareTo
argument_list|(
name|a
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|a
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"d"
argument_list|)
argument_list|)
expr_stmt|;
name|b
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"e"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"g"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|a
operator|.
name|compareTo
argument_list|(
name|b
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|a
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaaa"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"dddd"
argument_list|)
argument_list|)
expr_stmt|;
name|b
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"e"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"g"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|a
operator|.
name|compareTo
argument_list|(
name|b
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
name|a
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaaa"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"dddd"
argument_list|)
argument_list|)
expr_stmt|;
name|b
operator|=
operator|new
name|HRegionInfo
argument_list|(
name|t
operator|.
name|getName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaaa"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"eeee"
argument_list|)
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|a
operator|.
name|compareTo
argument_list|(
name|b
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

