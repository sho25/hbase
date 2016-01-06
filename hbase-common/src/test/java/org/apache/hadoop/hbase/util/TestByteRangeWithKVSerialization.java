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
package|;
end_package

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
name|Assert
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
name|TestByteRangeWithKVSerialization
block|{
specifier|static
name|void
name|writeCell
parameter_list|(
name|PositionedByteRange
name|pbr
parameter_list|,
name|KeyValue
name|kv
parameter_list|)
throws|throws
name|Exception
block|{
name|pbr
operator|.
name|putInt
argument_list|(
name|kv
operator|.
name|getKeyLength
argument_list|()
argument_list|)
expr_stmt|;
name|pbr
operator|.
name|putInt
argument_list|(
name|kv
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
name|pbr
operator|.
name|put
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getKeyOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getKeyLength
argument_list|()
argument_list|)
expr_stmt|;
name|pbr
operator|.
name|put
argument_list|(
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueLength
argument_list|()
argument_list|)
expr_stmt|;
name|int
name|tagsLen
init|=
name|kv
operator|.
name|getTagsLength
argument_list|()
decl_stmt|;
name|pbr
operator|.
name|put
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|tagsLen
operator|>>
literal|8
operator|&
literal|0xff
argument_list|)
argument_list|)
expr_stmt|;
name|pbr
operator|.
name|put
argument_list|(
call|(
name|byte
call|)
argument_list|(
name|tagsLen
operator|&
literal|0xff
argument_list|)
argument_list|)
expr_stmt|;
name|pbr
operator|.
name|put
argument_list|(
name|kv
operator|.
name|getTagsArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|tagsLen
argument_list|)
expr_stmt|;
name|pbr
operator|.
name|putVLong
argument_list|(
name|kv
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|static
name|KeyValue
name|readCell
parameter_list|(
name|PositionedByteRange
name|pbr
parameter_list|)
throws|throws
name|Exception
block|{
name|int
name|kvStartPos
init|=
name|pbr
operator|.
name|getPosition
argument_list|()
decl_stmt|;
name|int
name|keyLen
init|=
name|pbr
operator|.
name|getInt
argument_list|()
decl_stmt|;
name|int
name|valLen
init|=
name|pbr
operator|.
name|getInt
argument_list|()
decl_stmt|;
name|pbr
operator|.
name|setPosition
argument_list|(
name|pbr
operator|.
name|getPosition
argument_list|()
operator|+
name|keyLen
operator|+
name|valLen
argument_list|)
expr_stmt|;
comment|// Skip the key and value section
name|int
name|tagsLen
init|=
operator|(
operator|(
name|pbr
operator|.
name|get
argument_list|()
operator|&
literal|0xff
operator|)
operator|<<
literal|8
operator|)
operator|^
operator|(
name|pbr
operator|.
name|get
argument_list|()
operator|&
literal|0xff
operator|)
decl_stmt|;
name|pbr
operator|.
name|setPosition
argument_list|(
name|pbr
operator|.
name|getPosition
argument_list|()
operator|+
name|tagsLen
argument_list|)
expr_stmt|;
comment|// Skip the tags section
name|long
name|mvcc
init|=
name|pbr
operator|.
name|getVLong
argument_list|()
decl_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|pbr
operator|.
name|getBytes
argument_list|()
argument_list|,
name|kvStartPos
argument_list|,
operator|(
name|int
operator|)
name|KeyValue
operator|.
name|getKeyValueDataStructureSize
argument_list|(
name|keyLen
argument_list|,
name|valLen
argument_list|,
name|tagsLen
argument_list|)
argument_list|)
decl_stmt|;
name|kv
operator|.
name|setSequenceId
argument_list|(
name|mvcc
argument_list|)
expr_stmt|;
return|return
name|kv
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testWritingAndReadingCells
parameter_list|()
throws|throws
name|Exception
block|{
specifier|final
name|byte
index|[]
name|FAMILY
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"f1"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|QUALIFIER
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"q1"
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|VALUE
init|=
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"v"
argument_list|)
decl_stmt|;
name|int
name|kvCount
init|=
literal|1000000
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|(
name|kvCount
argument_list|)
decl_stmt|;
name|int
name|totalSize
init|=
literal|0
decl_stmt|;
name|Tag
index|[]
name|tags
init|=
operator|new
name|Tag
index|[]
block|{
operator|new
name|ArrayBackedTag
argument_list|(
operator|(
name|byte
operator|)
literal|1
argument_list|,
literal|"tag1"
argument_list|)
block|}
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
name|kvCount
condition|;
name|i
operator|++
control|)
block|{
name|KeyValue
name|kv
init|=
operator|new
name|KeyValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|,
name|FAMILY
argument_list|,
name|QUALIFIER
argument_list|,
name|i
argument_list|,
name|VALUE
argument_list|,
name|tags
argument_list|)
decl_stmt|;
name|kv
operator|.
name|setSequenceId
argument_list|(
name|i
argument_list|)
expr_stmt|;
name|kvs
operator|.
name|add
argument_list|(
name|kv
argument_list|)
expr_stmt|;
name|totalSize
operator|+=
name|kv
operator|.
name|getLength
argument_list|()
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
expr_stmt|;
block|}
name|PositionedByteRange
name|pbr
init|=
operator|new
name|SimplePositionedMutableByteRange
argument_list|(
name|totalSize
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
name|writeCell
argument_list|(
name|pbr
argument_list|,
name|kv
argument_list|)
expr_stmt|;
block|}
name|PositionedByteRange
name|pbr1
init|=
operator|new
name|SimplePositionedMutableByteRange
argument_list|(
name|pbr
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|pbr
operator|.
name|getPosition
argument_list|()
argument_list|)
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
name|kvCount
condition|;
name|i
operator|++
control|)
block|{
name|KeyValue
name|kv
init|=
name|readCell
argument_list|(
name|pbr1
argument_list|)
decl_stmt|;
name|KeyValue
name|kv1
init|=
name|kvs
operator|.
name|get
argument_list|(
name|i
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|kv
operator|.
name|equals
argument_list|(
name|kv1
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueLength
argument_list|()
argument_list|,
name|kv1
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|kv1
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|kv1
operator|.
name|getValueLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|Bytes
operator|.
name|equals
argument_list|(
name|kv
operator|.
name|getTagsArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getTagsOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getTagsLength
argument_list|()
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
name|kv1
operator|.
name|getTagsLength
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|Assert
operator|.
name|assertEquals
argument_list|(
name|kv1
operator|.
name|getSequenceId
argument_list|()
argument_list|,
name|kv
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

