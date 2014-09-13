begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License  */
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
operator|.
name|wal
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|ByteArrayInputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataInputStream
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
name|List
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
name|KeyValue
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
name|Tag
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
name|util
operator|.
name|LRUDictionary
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
name|io
operator|.
name|DataOutputBuffer
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|*
import|;
end_import

begin_import
import|import
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
name|TestKeyValueCompression
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|VALUE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fake value"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|BUF_SIZE
init|=
literal|256
operator|*
literal|1024
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testCountingKVs
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|400
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
name|i
argument_list|)
decl_stmt|;
name|byte
index|[]
name|fam
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
operator|+
name|i
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qual
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
operator|+
name|i
argument_list|)
decl_stmt|;
name|kvs
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|fam
argument_list|,
name|qual
argument_list|,
literal|12345L
argument_list|,
name|VALUE
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|runTestCycle
argument_list|(
name|kvs
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testRepeatingKVs
parameter_list|()
throws|throws
name|Exception
block|{
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
literal|400
condition|;
name|i
operator|++
control|)
block|{
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"row"
operator|+
operator|(
name|i
operator|%
literal|10
operator|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|fam
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fam"
operator|+
operator|(
name|i
operator|%
literal|127
operator|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qual
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"qual"
operator|+
operator|(
name|i
operator|%
literal|128
operator|)
argument_list|)
decl_stmt|;
name|kvs
operator|.
name|add
argument_list|(
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|fam
argument_list|,
name|qual
argument_list|,
literal|12345L
argument_list|,
name|VALUE
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|runTestCycle
argument_list|(
name|kvs
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|runTestCycle
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
parameter_list|)
throws|throws
name|Exception
block|{
name|CompressionContext
name|ctx
init|=
operator|new
name|CompressionContext
argument_list|(
name|LRUDictionary
operator|.
name|class
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|DataOutputBuffer
name|buf
init|=
operator|new
name|DataOutputBuffer
argument_list|(
name|BUF_SIZE
argument_list|)
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
name|KeyValueCompression
operator|.
name|writeKV
argument_list|(
name|buf
argument_list|,
name|kv
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
block|}
name|ctx
operator|.
name|clear
argument_list|()
expr_stmt|;
name|DataInputStream
name|in
init|=
operator|new
name|DataInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|buf
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|buf
operator|.
name|getLength
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|kvs
control|)
block|{
name|KeyValue
name|readBack
init|=
name|KeyValueCompression
operator|.
name|readKV
argument_list|(
name|in
argument_list|,
name|ctx
argument_list|)
decl_stmt|;
name|assertEquals
argument_list|(
name|kv
argument_list|,
name|readBack
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Test
specifier|public
name|void
name|testKVWithTags
parameter_list|()
throws|throws
name|Exception
block|{
name|CompressionContext
name|ctx
init|=
operator|new
name|CompressionContext
argument_list|(
name|LRUDictionary
operator|.
name|class
argument_list|,
literal|false
argument_list|,
literal|false
argument_list|)
decl_stmt|;
name|DataOutputBuffer
name|buf
init|=
operator|new
name|DataOutputBuffer
argument_list|(
name|BUF_SIZE
argument_list|)
decl_stmt|;
name|KeyValueCompression
operator|.
name|writeKV
argument_list|(
name|buf
argument_list|,
name|createKV
argument_list|(
literal|1
argument_list|)
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
name|KeyValueCompression
operator|.
name|writeKV
argument_list|(
name|buf
argument_list|,
name|createKV
argument_list|(
literal|0
argument_list|)
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
name|KeyValueCompression
operator|.
name|writeKV
argument_list|(
name|buf
argument_list|,
name|createKV
argument_list|(
literal|2
argument_list|)
argument_list|,
name|ctx
argument_list|)
expr_stmt|;
name|ctx
operator|.
name|clear
argument_list|()
expr_stmt|;
name|DataInputStream
name|in
init|=
operator|new
name|DataInputStream
argument_list|(
operator|new
name|ByteArrayInputStream
argument_list|(
name|buf
operator|.
name|getData
argument_list|()
argument_list|,
literal|0
argument_list|,
name|buf
operator|.
name|getLength
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|KeyValue
name|readBack
init|=
name|KeyValueCompression
operator|.
name|readKV
argument_list|(
name|in
argument_list|,
name|ctx
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
init|=
name|readBack
operator|.
name|getTags
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|1
argument_list|,
name|tags
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|private
name|KeyValue
name|createKV
parameter_list|(
name|int
name|noOfTags
parameter_list|)
block|{
name|byte
index|[]
name|row
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"myRow"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|cf
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"myCF"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|q
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"myQualifier"
argument_list|)
decl_stmt|;
name|byte
index|[]
name|value
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"myValue"
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
init|=
operator|new
name|ArrayList
argument_list|<
name|Tag
argument_list|>
argument_list|(
name|noOfTags
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|1
init|;
name|i
operator|<=
name|noOfTags
condition|;
name|i
operator|++
control|)
block|{
name|tags
operator|.
name|add
argument_list|(
operator|new
name|Tag
argument_list|(
operator|(
name|byte
operator|)
name|i
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"tagValue"
operator|+
name|i
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|cf
argument_list|,
name|q
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|value
argument_list|,
name|tags
argument_list|)
return|;
block|}
block|}
end_class

end_unit

