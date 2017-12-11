begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
operator|.
name|encoding
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Stores the state of data block encoder at the beginning of new key.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|CompressionState
block|{
name|int
name|keyLength
decl_stmt|;
name|int
name|valueLength
decl_stmt|;
name|short
name|rowLength
decl_stmt|;
name|int
name|prevOffset
init|=
name|FIRST_KEY
decl_stmt|;
name|byte
name|familyLength
decl_stmt|;
name|int
name|qualifierLength
decl_stmt|;
name|byte
name|type
decl_stmt|;
specifier|private
specifier|final
specifier|static
name|int
name|FIRST_KEY
init|=
operator|-
literal|1
decl_stmt|;
name|boolean
name|isFirst
parameter_list|()
block|{
return|return
name|prevOffset
operator|==
name|FIRST_KEY
return|;
block|}
comment|/**    * Analyze the key and fill the state.    * Uses mark() and reset() in ByteBuffer.    * @param in Buffer at the position where key starts    * @param keyLength Length of key in bytes    * @param valueLength Length of values in bytes    */
name|void
name|readKey
parameter_list|(
name|ByteBuffer
name|in
parameter_list|,
name|int
name|keyLength
parameter_list|,
name|int
name|valueLength
parameter_list|)
block|{
name|readKey
argument_list|(
name|in
argument_list|,
name|keyLength
argument_list|,
name|valueLength
argument_list|,
literal|0
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**     * Analyze the key and fill the state assuming we know previous state.    * Uses mark() and reset() in ByteBuffer to avoid moving the position.    *<p>    * This method overrides all the fields of this instance, except    * {@link #prevOffset}, which is usually manipulated directly by encoders    * and decoders.    * @param in Buffer at the position where key starts    * @param keyLength Length of key in bytes    * @param valueLength Length of values in bytes    * @param commonPrefix how many first bytes are common with previous KeyValue    * @param previousState State from previous KeyValue    */
name|void
name|readKey
parameter_list|(
name|ByteBuffer
name|in
parameter_list|,
name|int
name|keyLength
parameter_list|,
name|int
name|valueLength
parameter_list|,
name|int
name|commonPrefix
parameter_list|,
name|CompressionState
name|previousState
parameter_list|)
block|{
name|this
operator|.
name|keyLength
operator|=
name|keyLength
expr_stmt|;
name|this
operator|.
name|valueLength
operator|=
name|valueLength
expr_stmt|;
comment|// fill the state
name|in
operator|.
name|mark
argument_list|()
expr_stmt|;
comment|// mark beginning of key
if|if
condition|(
name|commonPrefix
operator|<
name|KeyValue
operator|.
name|ROW_LENGTH_SIZE
condition|)
block|{
name|rowLength
operator|=
name|in
operator|.
name|getShort
argument_list|()
expr_stmt|;
name|ByteBufferUtils
operator|.
name|skip
argument_list|(
name|in
argument_list|,
name|rowLength
argument_list|)
expr_stmt|;
name|familyLength
operator|=
name|in
operator|.
name|get
argument_list|()
expr_stmt|;
name|qualifierLength
operator|=
name|keyLength
operator|-
name|rowLength
operator|-
name|familyLength
operator|-
name|KeyValue
operator|.
name|KEY_INFRASTRUCTURE_SIZE
expr_stmt|;
name|ByteBufferUtils
operator|.
name|skip
argument_list|(
name|in
argument_list|,
name|familyLength
operator|+
name|qualifierLength
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|rowLength
operator|=
name|previousState
operator|.
name|rowLength
expr_stmt|;
name|familyLength
operator|=
name|previousState
operator|.
name|familyLength
expr_stmt|;
name|qualifierLength
operator|=
name|previousState
operator|.
name|qualifierLength
operator|+
name|keyLength
operator|-
name|previousState
operator|.
name|keyLength
expr_stmt|;
name|ByteBufferUtils
operator|.
name|skip
argument_list|(
name|in
argument_list|,
operator|(
name|KeyValue
operator|.
name|ROW_LENGTH_SIZE
operator|+
name|KeyValue
operator|.
name|FAMILY_LENGTH_SIZE
operator|)
operator|+
name|rowLength
operator|+
name|familyLength
operator|+
name|qualifierLength
argument_list|)
expr_stmt|;
block|}
name|readTimestamp
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|type
operator|=
name|in
operator|.
name|get
argument_list|()
expr_stmt|;
name|in
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|readTimestamp
parameter_list|(
name|ByteBuffer
name|in
parameter_list|)
block|{
comment|// used in subclasses to add timestamp to state
name|ByteBufferUtils
operator|.
name|skip
argument_list|(
name|in
argument_list|,
name|KeyValue
operator|.
name|TIMESTAMP_SIZE
argument_list|)
expr_stmt|;
block|}
name|void
name|copyFrom
parameter_list|(
name|CompressionState
name|state
parameter_list|)
block|{
name|keyLength
operator|=
name|state
operator|.
name|keyLength
expr_stmt|;
name|valueLength
operator|=
name|state
operator|.
name|valueLength
expr_stmt|;
name|rowLength
operator|=
name|state
operator|.
name|rowLength
expr_stmt|;
name|prevOffset
operator|=
name|state
operator|.
name|prevOffset
expr_stmt|;
name|familyLength
operator|=
name|state
operator|.
name|familyLength
expr_stmt|;
name|qualifierLength
operator|=
name|state
operator|.
name|qualifierLength
expr_stmt|;
name|type
operator|=
name|state
operator|.
name|type
expr_stmt|;
block|}
block|}
end_class

end_unit

