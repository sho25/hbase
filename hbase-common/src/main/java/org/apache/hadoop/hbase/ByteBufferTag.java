begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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
comment|/**  * This is a {@link Tag} implementation in which value is backed by  * {@link java.nio.ByteBuffer}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|ByteBufferTag
implements|implements
name|Tag
block|{
specifier|private
name|ByteBuffer
name|buffer
decl_stmt|;
specifier|private
name|int
name|offset
decl_stmt|,
name|length
decl_stmt|;
specifier|private
name|byte
name|type
decl_stmt|;
specifier|public
name|ByteBufferTag
parameter_list|(
name|ByteBuffer
name|buffer
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
name|this
operator|.
name|buffer
operator|=
name|buffer
expr_stmt|;
name|this
operator|.
name|offset
operator|=
name|offset
expr_stmt|;
name|this
operator|.
name|length
operator|=
name|length
expr_stmt|;
name|this
operator|.
name|type
operator|=
name|ByteBufferUtils
operator|.
name|toByte
argument_list|(
name|buffer
argument_list|,
name|offset
operator|+
name|TAG_LENGTH_SIZE
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|byte
name|getType
parameter_list|()
block|{
return|return
name|this
operator|.
name|type
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getValueOffset
parameter_list|()
block|{
return|return
name|this
operator|.
name|offset
operator|+
name|INFRASTRUCTURE_SIZE
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getValueLength
parameter_list|()
block|{
return|return
name|this
operator|.
name|length
operator|-
name|INFRASTRUCTURE_SIZE
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasArray
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|byte
index|[]
name|getValueArray
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Tag is backed by an off heap buffer. Use getValueByteBuffer()"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|ByteBuffer
name|getValueByteBuffer
parameter_list|()
block|{
return|return
name|this
operator|.
name|buffer
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"[Tag type : "
operator|+
name|this
operator|.
name|type
operator|+
literal|", value : "
operator|+
name|ByteBufferUtils
operator|.
name|toStringBinary
argument_list|(
name|buffer
argument_list|,
name|getValueOffset
argument_list|()
argument_list|,
name|getValueLength
argument_list|()
argument_list|)
operator|+
literal|"]"
return|;
block|}
block|}
end_class

end_unit

