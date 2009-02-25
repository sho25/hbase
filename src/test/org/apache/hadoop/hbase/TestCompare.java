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
name|junit
operator|.
name|framework
operator|.
name|TestCase
import|;
end_import

begin_comment
comment|/**  * Test comparing HBase objects.  */
end_comment

begin_class
specifier|public
class|class
name|TestCompare
extends|extends
name|TestCase
block|{
comment|/**    * HStoreKey sorts as you would expect in the row and column portions but    * for the timestamps, it sorts in reverse with the newest sorting before    * the oldest (This is intentional so we trip over the latest first when    * iterating or looking in store files).    */
specifier|public
name|void
name|testHStoreKey
parameter_list|()
block|{
name|long
name|timestamp
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|byte
index|[]
name|a
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
decl_stmt|;
name|HStoreKey
name|past
init|=
operator|new
name|HStoreKey
argument_list|(
name|a
argument_list|,
name|a
argument_list|,
name|timestamp
operator|-
literal|10
argument_list|)
decl_stmt|;
name|HStoreKey
name|now
init|=
operator|new
name|HStoreKey
argument_list|(
name|a
argument_list|,
name|a
argument_list|,
name|timestamp
argument_list|)
decl_stmt|;
name|HStoreKey
name|future
init|=
operator|new
name|HStoreKey
argument_list|(
name|a
argument_list|,
name|a
argument_list|,
name|timestamp
operator|+
literal|10
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|past
operator|.
name|compareTo
argument_list|(
name|now
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|now
operator|.
name|compareTo
argument_list|(
name|now
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|future
operator|.
name|compareTo
argument_list|(
name|now
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
comment|// Check that empty column comes before one with a column
name|HStoreKey
name|nocolumn
init|=
operator|new
name|HStoreKey
argument_list|(
name|a
argument_list|,
name|timestamp
argument_list|)
decl_stmt|;
name|HStoreKey
name|withcolumn
init|=
operator|new
name|HStoreKey
argument_list|(
name|a
argument_list|,
name|a
argument_list|,
name|timestamp
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|nocolumn
operator|.
name|compareTo
argument_list|(
name|withcolumn
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
comment|// Check that empty column comes and LATEST comes before one with a column
comment|// and old timestamp.
name|nocolumn
operator|=
operator|new
name|HStoreKey
argument_list|(
name|a
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
name|withcolumn
operator|=
operator|new
name|HStoreKey
argument_list|(
name|a
argument_list|,
name|a
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|nocolumn
operator|.
name|compareTo
argument_list|(
name|withcolumn
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
comment|// Test null keys.
name|HStoreKey
name|normal
init|=
operator|new
name|HStoreKey
argument_list|(
literal|"a"
argument_list|,
literal|"b"
argument_list|)
decl_stmt|;
name|assertTrue
argument_list|(
name|normal
operator|.
name|compareTo
argument_list|(
literal|null
argument_list|)
operator|>
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|HStoreKey
operator|.
name|compareTo
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
operator|==
literal|0
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|HStoreKey
operator|.
name|compareTo
argument_list|(
literal|null
argument_list|,
name|normal
argument_list|)
operator|<
literal|0
argument_list|)
expr_stmt|;
block|}
comment|/**    * Tests cases where rows keys have characters below the ','.    * See HBASE-832    */
specifier|public
name|void
name|testHStoreKeyBorderCases
parameter_list|()
block|{
comment|/** TODO!!!!     HRegionInfo info = new HRegionInfo(new HTableDescriptor("testtable"),         HConstants.EMPTY_BYTE_ARRAY, HConstants.EMPTY_BYTE_ARRAY);     HStoreKey rowA = new HStoreKey("testtable,www.hbase.org/,1234",         "", Long.MAX_VALUE, info);     HStoreKey rowB = new HStoreKey("testtable,www.hbase.org/%20,99999",         "", Long.MAX_VALUE, info);      assertTrue(rowA.compareTo(rowB)> 0);      rowA = new HStoreKey("testtable,www.hbase.org/,1234",         "", Long.MAX_VALUE, HRegionInfo.FIRST_META_REGIONINFO);     rowB = new HStoreKey("testtable,www.hbase.org/%20,99999",         "", Long.MAX_VALUE, HRegionInfo.FIRST_META_REGIONINFO);      assertTrue(rowA.compareTo(rowB)< 0);      rowA = new HStoreKey("testtable,,1234",         "", Long.MAX_VALUE, HRegionInfo.FIRST_META_REGIONINFO);     rowB = new HStoreKey("testtable,$www.hbase.org/,99999",         "", Long.MAX_VALUE, HRegionInfo.FIRST_META_REGIONINFO);      assertTrue(rowA.compareTo(rowB)< 0);      rowA = new HStoreKey(".META.,testtable,www.hbase.org/,1234,4321",         "", Long.MAX_VALUE, HRegionInfo.ROOT_REGIONINFO);     rowB = new HStoreKey(".META.,testtable,www.hbase.org/%20,99999,99999",         "", Long.MAX_VALUE, HRegionInfo.ROOT_REGIONINFO);      assertTrue(rowA.compareTo(rowB)> 0);     */
block|}
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
operator|new
name|HTableDescriptor
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
operator|new
name|HTableDescriptor
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

