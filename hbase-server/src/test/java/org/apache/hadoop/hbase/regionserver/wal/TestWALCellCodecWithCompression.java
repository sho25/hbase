begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ByteArrayOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|InputStream
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
name|TagUtil
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
name|ArrayBackedTag
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
name|codec
operator|.
name|Codec
operator|.
name|Decoder
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
name|codec
operator|.
name|Codec
operator|.
name|Encoder
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
name|TestWALCellCodecWithCompression
block|{
annotation|@
name|Test
specifier|public
name|void
name|testEncodeDecodeKVsWithTags
parameter_list|()
throws|throws
name|Exception
block|{
name|doTest
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testEncodeDecodeKVsWithTagsWithTagsCompression
parameter_list|()
throws|throws
name|Exception
block|{
name|doTest
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|doTest
parameter_list|(
name|boolean
name|compressTags
parameter_list|)
throws|throws
name|Exception
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|(
literal|false
argument_list|)
decl_stmt|;
name|conf
operator|.
name|setBoolean
argument_list|(
name|CompressionContext
operator|.
name|ENABLE_WAL_TAGS_COMPRESSION
argument_list|,
name|compressTags
argument_list|)
expr_stmt|;
name|WALCellCodec
name|codec
init|=
operator|new
name|WALCellCodec
argument_list|(
name|conf
argument_list|,
operator|new
name|CompressionContext
argument_list|(
name|LRUDictionary
operator|.
name|class
argument_list|,
literal|false
argument_list|,
name|compressTags
argument_list|)
argument_list|)
decl_stmt|;
name|ByteArrayOutputStream
name|bos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|(
literal|1024
argument_list|)
decl_stmt|;
name|Encoder
name|encoder
init|=
name|codec
operator|.
name|getEncoder
argument_list|(
name|bos
argument_list|)
decl_stmt|;
name|encoder
operator|.
name|write
argument_list|(
name|createKV
argument_list|(
literal|1
argument_list|)
argument_list|)
expr_stmt|;
name|encoder
operator|.
name|write
argument_list|(
name|createKV
argument_list|(
literal|0
argument_list|)
argument_list|)
expr_stmt|;
name|encoder
operator|.
name|write
argument_list|(
name|createKV
argument_list|(
literal|2
argument_list|)
argument_list|)
expr_stmt|;
name|InputStream
name|is
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|bos
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|Decoder
name|decoder
init|=
name|codec
operator|.
name|getDecoder
argument_list|(
name|is
argument_list|)
decl_stmt|;
name|decoder
operator|.
name|advance
argument_list|()
expr_stmt|;
name|KeyValue
name|kv
init|=
operator|(
name|KeyValue
operator|)
name|decoder
operator|.
name|current
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
init|=
name|kv
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
name|assertEquals
argument_list|(
literal|"tagValue1"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|TagUtil
operator|.
name|cloneValue
argument_list|(
name|tags
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|decoder
operator|.
name|advance
argument_list|()
expr_stmt|;
name|kv
operator|=
operator|(
name|KeyValue
operator|)
name|decoder
operator|.
name|current
argument_list|()
expr_stmt|;
name|tags
operator|=
name|kv
operator|.
name|getTags
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|0
argument_list|,
name|tags
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|decoder
operator|.
name|advance
argument_list|()
expr_stmt|;
name|kv
operator|=
operator|(
name|KeyValue
operator|)
name|decoder
operator|.
name|current
argument_list|()
expr_stmt|;
name|tags
operator|=
name|kv
operator|.
name|getTags
argument_list|()
expr_stmt|;
name|assertEquals
argument_list|(
literal|2
argument_list|,
name|tags
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"tagValue1"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|TagUtil
operator|.
name|cloneValue
argument_list|(
name|tags
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"tagValue2"
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|TagUtil
operator|.
name|cloneValue
argument_list|(
name|tags
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
argument_list|)
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
name|ArrayBackedTag
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

