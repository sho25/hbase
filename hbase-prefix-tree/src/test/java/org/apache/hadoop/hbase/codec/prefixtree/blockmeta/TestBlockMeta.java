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
name|codec
operator|.
name|prefixtree
operator|.
name|blockmeta
package|;
end_package

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
name|IOException
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
name|codec
operator|.
name|prefixtree
operator|.
name|PrefixTreeBlockMeta
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

begin_class
specifier|public
class|class
name|TestBlockMeta
block|{
specifier|static
name|int
name|BLOCK_START
init|=
literal|123
decl_stmt|;
specifier|private
specifier|static
name|PrefixTreeBlockMeta
name|createSample
parameter_list|()
block|{
name|PrefixTreeBlockMeta
name|m
init|=
operator|new
name|PrefixTreeBlockMeta
argument_list|()
decl_stmt|;
name|m
operator|.
name|setNumMetaBytes
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|m
operator|.
name|setNumKeyValueBytes
argument_list|(
literal|3195
argument_list|)
expr_stmt|;
name|m
operator|.
name|setNumRowBytes
argument_list|(
literal|0
argument_list|)
expr_stmt|;
name|m
operator|.
name|setNumFamilyBytes
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|m
operator|.
name|setNumQualifierBytes
argument_list|(
literal|12345
argument_list|)
expr_stmt|;
name|m
operator|.
name|setNumTagsBytes
argument_list|(
literal|50
argument_list|)
expr_stmt|;
name|m
operator|.
name|setNumTimestampBytes
argument_list|(
literal|23456
argument_list|)
expr_stmt|;
name|m
operator|.
name|setNumMvccVersionBytes
argument_list|(
literal|5
argument_list|)
expr_stmt|;
name|m
operator|.
name|setNumValueBytes
argument_list|(
literal|34567
argument_list|)
expr_stmt|;
name|m
operator|.
name|setNextNodeOffsetWidth
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|m
operator|.
name|setFamilyOffsetWidth
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|m
operator|.
name|setQualifierOffsetWidth
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|m
operator|.
name|setTagsOffsetWidth
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|m
operator|.
name|setTimestampIndexWidth
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|m
operator|.
name|setMvccVersionIndexWidth
argument_list|(
literal|2
argument_list|)
expr_stmt|;
name|m
operator|.
name|setValueOffsetWidth
argument_list|(
literal|8
argument_list|)
expr_stmt|;
name|m
operator|.
name|setValueLengthWidth
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|m
operator|.
name|setRowTreeDepth
argument_list|(
literal|11
argument_list|)
expr_stmt|;
name|m
operator|.
name|setMaxRowLength
argument_list|(
literal|200
argument_list|)
expr_stmt|;
name|m
operator|.
name|setMaxQualifierLength
argument_list|(
literal|50
argument_list|)
expr_stmt|;
name|m
operator|.
name|setMaxTagsLength
argument_list|(
literal|40
argument_list|)
expr_stmt|;
name|m
operator|.
name|setMinTimestamp
argument_list|(
literal|1318966363481L
argument_list|)
expr_stmt|;
name|m
operator|.
name|setTimestampDeltaWidth
argument_list|(
literal|3
argument_list|)
expr_stmt|;
name|m
operator|.
name|setMinMvccVersion
argument_list|(
literal|100L
argument_list|)
expr_stmt|;
name|m
operator|.
name|setMvccVersionDeltaWidth
argument_list|(
literal|4
argument_list|)
expr_stmt|;
name|m
operator|.
name|setAllSameType
argument_list|(
literal|false
argument_list|)
expr_stmt|;
name|m
operator|.
name|setAllTypes
argument_list|(
name|KeyValue
operator|.
name|Type
operator|.
name|Delete
operator|.
name|getCode
argument_list|()
argument_list|)
expr_stmt|;
name|m
operator|.
name|setNumUniqueRows
argument_list|(
literal|88
argument_list|)
expr_stmt|;
name|m
operator|.
name|setNumUniqueFamilies
argument_list|(
literal|1
argument_list|)
expr_stmt|;
name|m
operator|.
name|setNumUniqueQualifiers
argument_list|(
literal|56
argument_list|)
expr_stmt|;
name|m
operator|.
name|setNumUniqueTags
argument_list|(
literal|5
argument_list|)
expr_stmt|;
return|return
name|m
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testStreamSerialization
parameter_list|()
throws|throws
name|IOException
block|{
name|PrefixTreeBlockMeta
name|original
init|=
name|createSample
argument_list|()
decl_stmt|;
name|ByteArrayOutputStream
name|os
init|=
operator|new
name|ByteArrayOutputStream
argument_list|(
literal|10000
argument_list|)
decl_stmt|;
name|original
operator|.
name|writeVariableBytesToOutputStream
argument_list|(
name|os
argument_list|)
expr_stmt|;
name|ByteBuffer
name|buffer
init|=
name|ByteBuffer
operator|.
name|wrap
argument_list|(
name|os
operator|.
name|toByteArray
argument_list|()
argument_list|)
decl_stmt|;
name|PrefixTreeBlockMeta
name|roundTripped
init|=
operator|new
name|PrefixTreeBlockMeta
argument_list|(
name|buffer
argument_list|)
decl_stmt|;
name|Assert
operator|.
name|assertTrue
argument_list|(
name|original
operator|.
name|equals
argument_list|(
name|roundTripped
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

