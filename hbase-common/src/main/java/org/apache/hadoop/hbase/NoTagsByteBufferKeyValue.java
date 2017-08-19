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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
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

begin_comment
comment|/**  * An extension of the ByteBufferKeyValue where the tags length is always 0  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|NoTagsByteBufferKeyValue
extends|extends
name|ByteBufferKeyValue
block|{
specifier|public
name|NoTagsByteBufferKeyValue
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|super
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
specifier|public
name|NoTagsByteBufferKeyValue
parameter_list|(
name|ByteBuffer
name|buf
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|long
name|seqId
parameter_list|)
block|{
name|super
argument_list|(
name|buf
argument_list|,
name|offset
argument_list|,
name|length
argument_list|,
name|seqId
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getTagsArray
parameter_list|()
block|{
return|return
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getTagsLength
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getSerializedSize
parameter_list|(
name|boolean
name|withTags
parameter_list|)
block|{
return|return
name|this
operator|.
name|length
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|deepClone
parameter_list|()
block|{
name|byte
index|[]
name|copy
init|=
operator|new
name|byte
index|[
name|this
operator|.
name|length
index|]
decl_stmt|;
name|ByteBufferUtils
operator|.
name|copyFromBufferToArray
argument_list|(
name|copy
argument_list|,
name|this
operator|.
name|buf
argument_list|,
name|this
operator|.
name|offset
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|length
argument_list|)
expr_stmt|;
name|KeyValue
name|kv
init|=
operator|new
name|NoTagsKeyValue
argument_list|(
name|copy
argument_list|,
literal|0
argument_list|,
name|copy
operator|.
name|length
argument_list|)
decl_stmt|;
name|kv
operator|.
name|setSequenceId
argument_list|(
name|this
operator|.
name|getSequenceId
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|kv
return|;
block|}
block|}
end_class

end_unit

