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
name|io
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
name|assertTrue
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
name|DataOutputStream
import|;
end_import

begin_import
import|import
name|java
operator|.
name|nio
operator|.
name|ByteBuffer
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
name|ByteBufferExtendedCell
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
name|ByteBufferKeyValue
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
name|Cell
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
name|nio
operator|.
name|SingleByteBuff
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
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|util
operator|.
name|ByteBufferUtils
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
name|TestTagCompressionContext
block|{
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|ROW
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"r1"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|CF
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|Q
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q"
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
index|[]
name|V
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"v"
argument_list|)
decl_stmt|;
annotation|@
name|Test
specifier|public
name|void
name|testCompressUncompressTags1
parameter_list|()
throws|throws
name|Exception
block|{
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|TagCompressionContext
name|context
init|=
operator|new
name|TagCompressionContext
argument_list|(
name|LRUDictionary
operator|.
name|class
argument_list|,
name|Byte
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|KeyValue
name|kv1
init|=
name|createKVWithTags
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|int
name|tagsLength1
init|=
name|kv1
operator|.
name|getTagsLength
argument_list|()
decl_stmt|;
name|ByteBuffer
name|ib
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|kv1
operator|.
name|getTagsArray
argument_list|()
argument_list|)
decl_stmt|;
name|context
operator|.
name|compressTags
argument_list|(
name|baos
argument_list|,
name|ib
argument_list|,
name|kv1
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|tagsLength1
argument_list|)
expr_stmt|;
name|KeyValue
name|kv2
init|=
name|createKVWithTags
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|int
name|tagsLength2
init|=
name|kv2
operator|.
name|getTagsLength
argument_list|()
decl_stmt|;
name|ib
operator|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|kv2
operator|.
name|getTagsArray
argument_list|()
argument_list|)
expr_stmt|;
name|context
operator|.
name|compressTags
argument_list|(
name|baos
argument_list|,
name|ib
argument_list|,
name|kv2
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|tagsLength2
argument_list|)
expr_stmt|;
name|context
operator|.
name|clear
argument_list|()
expr_stmt|;
name|byte
index|[]
name|dest
init|=
operator|new
name|byte
index|[
name|tagsLength1
index|]
decl_stmt|;
name|ByteBuffer
name|ob
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|baos
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|context
operator|.
name|uncompressTags
argument_list|(
operator|new
name|SingleByteBuff
argument_list|(
name|ob
argument_list|)
argument_list|,
name|dest
argument_list|,
literal|0
argument_list|,
name|tagsLength1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|kv1
operator|.
name|getTagsArray
argument_list|()
argument_list|,
name|kv1
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|tagsLength1
argument_list|,
name|dest
argument_list|,
literal|0
argument_list|,
name|tagsLength1
argument_list|)
argument_list|)
expr_stmt|;
name|dest
operator|=
operator|new
name|byte
index|[
name|tagsLength2
index|]
expr_stmt|;
name|context
operator|.
name|uncompressTags
argument_list|(
operator|new
name|SingleByteBuff
argument_list|(
name|ob
argument_list|)
argument_list|,
name|dest
argument_list|,
literal|0
argument_list|,
name|tagsLength2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|kv2
operator|.
name|getTagsArray
argument_list|()
argument_list|,
name|kv2
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|tagsLength2
argument_list|,
name|dest
argument_list|,
literal|0
argument_list|,
name|tagsLength2
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompressUncompressTagsWithOffheapKeyValue1
parameter_list|()
throws|throws
name|Exception
block|{
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|daos
init|=
operator|new
name|ByteBufferWriterDataOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|TagCompressionContext
name|context
init|=
operator|new
name|TagCompressionContext
argument_list|(
name|LRUDictionary
operator|.
name|class
argument_list|,
name|Byte
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|ByteBufferExtendedCell
name|kv1
init|=
operator|(
name|ByteBufferExtendedCell
operator|)
name|createOffheapKVWithTags
argument_list|(
literal|2
argument_list|)
decl_stmt|;
name|int
name|tagsLength1
init|=
name|kv1
operator|.
name|getTagsLength
argument_list|()
decl_stmt|;
name|context
operator|.
name|compressTags
argument_list|(
name|daos
argument_list|,
name|kv1
operator|.
name|getTagsByteBuffer
argument_list|()
argument_list|,
name|kv1
operator|.
name|getTagsPosition
argument_list|()
argument_list|,
name|tagsLength1
argument_list|)
expr_stmt|;
name|ByteBufferExtendedCell
name|kv2
init|=
operator|(
name|ByteBufferExtendedCell
operator|)
name|createOffheapKVWithTags
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|int
name|tagsLength2
init|=
name|kv2
operator|.
name|getTagsLength
argument_list|()
decl_stmt|;
name|context
operator|.
name|compressTags
argument_list|(
name|daos
argument_list|,
name|kv2
operator|.
name|getTagsByteBuffer
argument_list|()
argument_list|,
name|kv2
operator|.
name|getTagsPosition
argument_list|()
argument_list|,
name|tagsLength2
argument_list|)
expr_stmt|;
name|context
operator|.
name|clear
argument_list|()
expr_stmt|;
name|byte
index|[]
name|dest
init|=
operator|new
name|byte
index|[
name|tagsLength1
index|]
decl_stmt|;
name|ByteBuffer
name|ob
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|baos
operator|.
name|getBuffer
argument_list|()
argument_list|)
decl_stmt|;
name|context
operator|.
name|uncompressTags
argument_list|(
operator|new
name|SingleByteBuff
argument_list|(
name|ob
argument_list|)
argument_list|,
name|dest
argument_list|,
literal|0
argument_list|,
name|tagsLength1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|kv1
operator|.
name|getTagsArray
argument_list|()
argument_list|,
name|kv1
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|tagsLength1
argument_list|,
name|dest
argument_list|,
literal|0
argument_list|,
name|tagsLength1
argument_list|)
argument_list|)
expr_stmt|;
name|dest
operator|=
operator|new
name|byte
index|[
name|tagsLength2
index|]
expr_stmt|;
name|context
operator|.
name|uncompressTags
argument_list|(
operator|new
name|SingleByteBuff
argument_list|(
name|ob
argument_list|)
argument_list|,
name|dest
argument_list|,
literal|0
argument_list|,
name|tagsLength2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|kv2
operator|.
name|getTagsArray
argument_list|()
argument_list|,
name|kv2
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|tagsLength2
argument_list|,
name|dest
argument_list|,
literal|0
argument_list|,
name|tagsLength2
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompressUncompressTags2
parameter_list|()
throws|throws
name|Exception
block|{
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|TagCompressionContext
name|context
init|=
operator|new
name|TagCompressionContext
argument_list|(
name|LRUDictionary
operator|.
name|class
argument_list|,
name|Byte
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|KeyValue
name|kv1
init|=
name|createKVWithTags
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|int
name|tagsLength1
init|=
name|kv1
operator|.
name|getTagsLength
argument_list|()
decl_stmt|;
name|context
operator|.
name|compressTags
argument_list|(
name|baos
argument_list|,
name|kv1
operator|.
name|getTagsArray
argument_list|()
argument_list|,
name|kv1
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|tagsLength1
argument_list|)
expr_stmt|;
name|KeyValue
name|kv2
init|=
name|createKVWithTags
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|int
name|tagsLength2
init|=
name|kv2
operator|.
name|getTagsLength
argument_list|()
decl_stmt|;
name|context
operator|.
name|compressTags
argument_list|(
name|baos
argument_list|,
name|kv2
operator|.
name|getTagsArray
argument_list|()
argument_list|,
name|kv2
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|tagsLength2
argument_list|)
expr_stmt|;
name|context
operator|.
name|clear
argument_list|()
expr_stmt|;
name|ByteArrayInputStream
name|bais
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|baos
operator|.
name|getBuffer
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|dest
init|=
operator|new
name|byte
index|[
name|tagsLength1
index|]
decl_stmt|;
name|context
operator|.
name|uncompressTags
argument_list|(
name|bais
argument_list|,
name|dest
argument_list|,
literal|0
argument_list|,
name|tagsLength1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|kv1
operator|.
name|getTagsArray
argument_list|()
argument_list|,
name|kv1
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|tagsLength1
argument_list|,
name|dest
argument_list|,
literal|0
argument_list|,
name|tagsLength1
argument_list|)
argument_list|)
expr_stmt|;
name|dest
operator|=
operator|new
name|byte
index|[
name|tagsLength2
index|]
expr_stmt|;
name|context
operator|.
name|uncompressTags
argument_list|(
name|bais
argument_list|,
name|dest
argument_list|,
literal|0
argument_list|,
name|tagsLength2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|kv2
operator|.
name|getTagsArray
argument_list|()
argument_list|,
name|kv2
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|tagsLength2
argument_list|,
name|dest
argument_list|,
literal|0
argument_list|,
name|tagsLength2
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testCompressUncompressTagsWithOffheapKeyValue2
parameter_list|()
throws|throws
name|Exception
block|{
name|ByteArrayOutputStream
name|baos
init|=
operator|new
name|ByteArrayOutputStream
argument_list|()
decl_stmt|;
name|DataOutputStream
name|daos
init|=
operator|new
name|ByteBufferWriterDataOutputStream
argument_list|(
name|baos
argument_list|)
decl_stmt|;
name|TagCompressionContext
name|context
init|=
operator|new
name|TagCompressionContext
argument_list|(
name|LRUDictionary
operator|.
name|class
argument_list|,
name|Byte
operator|.
name|MAX_VALUE
argument_list|)
decl_stmt|;
name|ByteBufferExtendedCell
name|kv1
init|=
operator|(
name|ByteBufferExtendedCell
operator|)
name|createOffheapKVWithTags
argument_list|(
literal|1
argument_list|)
decl_stmt|;
name|int
name|tagsLength1
init|=
name|kv1
operator|.
name|getTagsLength
argument_list|()
decl_stmt|;
name|context
operator|.
name|compressTags
argument_list|(
name|daos
argument_list|,
name|kv1
operator|.
name|getTagsByteBuffer
argument_list|()
argument_list|,
name|kv1
operator|.
name|getTagsPosition
argument_list|()
argument_list|,
name|tagsLength1
argument_list|)
expr_stmt|;
name|ByteBufferExtendedCell
name|kv2
init|=
operator|(
name|ByteBufferExtendedCell
operator|)
name|createOffheapKVWithTags
argument_list|(
literal|3
argument_list|)
decl_stmt|;
name|int
name|tagsLength2
init|=
name|kv2
operator|.
name|getTagsLength
argument_list|()
decl_stmt|;
name|context
operator|.
name|compressTags
argument_list|(
name|daos
argument_list|,
name|kv2
operator|.
name|getTagsByteBuffer
argument_list|()
argument_list|,
name|kv2
operator|.
name|getTagsPosition
argument_list|()
argument_list|,
name|tagsLength2
argument_list|)
expr_stmt|;
name|context
operator|.
name|clear
argument_list|()
expr_stmt|;
name|ByteArrayInputStream
name|bais
init|=
operator|new
name|ByteArrayInputStream
argument_list|(
name|baos
operator|.
name|getBuffer
argument_list|()
argument_list|)
decl_stmt|;
name|byte
index|[]
name|dest
init|=
operator|new
name|byte
index|[
name|tagsLength1
index|]
decl_stmt|;
name|context
operator|.
name|uncompressTags
argument_list|(
name|bais
argument_list|,
name|dest
argument_list|,
literal|0
argument_list|,
name|tagsLength1
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|kv1
operator|.
name|getTagsArray
argument_list|()
argument_list|,
name|kv1
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|tagsLength1
argument_list|,
name|dest
argument_list|,
literal|0
argument_list|,
name|tagsLength1
argument_list|)
argument_list|)
expr_stmt|;
name|dest
operator|=
operator|new
name|byte
index|[
name|tagsLength2
index|]
expr_stmt|;
name|context
operator|.
name|uncompressTags
argument_list|(
name|bais
argument_list|,
name|dest
argument_list|,
literal|0
argument_list|,
name|tagsLength2
argument_list|)
expr_stmt|;
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|kv2
operator|.
name|getTagsArray
argument_list|()
argument_list|,
name|kv2
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|tagsLength2
argument_list|,
name|dest
argument_list|,
literal|0
argument_list|,
name|tagsLength2
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|private
name|KeyValue
name|createKVWithTags
parameter_list|(
name|int
name|noOfTags
parameter_list|)
block|{
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
init|=
operator|new
name|ArrayList
argument_list|<>
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
literal|"tagValue"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|CF
argument_list|,
name|Q
argument_list|,
literal|1234L
argument_list|,
name|V
argument_list|,
name|tags
argument_list|)
decl_stmt|;
return|return
name|kv
return|;
block|}
specifier|private
name|Cell
name|createOffheapKVWithTags
parameter_list|(
name|int
name|noOfTags
parameter_list|)
block|{
name|List
argument_list|<
name|Tag
argument_list|>
name|tags
init|=
operator|new
name|ArrayList
argument_list|<>
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
literal|"tagValue"
operator|+
name|i
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|ROW
argument_list|,
name|CF
argument_list|,
name|Q
argument_list|,
literal|1234L
argument_list|,
name|V
argument_list|,
name|tags
argument_list|)
decl_stmt|;
name|ByteBuffer
name|dbb
init|=
name|ByteBuffer
operator|.
name|allocateDirect
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
operator|.
name|length
argument_list|)
decl_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromArrayToBuffer
argument_list|(
name|dbb
argument_list|,
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
literal|0
argument_list|,
name|kv
operator|.
name|getBuffer
argument_list|()
operator|.
name|length
argument_list|)
expr_stmt|;
name|ByteBufferKeyValue
name|offheapKV
init|=
operator|new
name|ByteBufferKeyValue
argument_list|(
name|dbb
argument_list|,
literal|0
argument_list|,
name|kv
operator|.
name|getBuffer
argument_list|()
operator|.
name|length
argument_list|,
literal|0
argument_list|)
decl_stmt|;
return|return
name|offheapKV
return|;
block|}
block|}
end_class

end_unit

