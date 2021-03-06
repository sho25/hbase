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
name|regionserver
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
name|ByteBufferKeyValue
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
comment|/**  * ByteBuffer based cell which has the chunkid at the 0th offset  * @see MemStoreLAB  */
end_comment

begin_comment
comment|//TODO : When moving this cell to CellChunkMap we will have the following things
end_comment

begin_comment
comment|// to be serialized
end_comment

begin_comment
comment|// chunkId (Integer) + offset (Integer) + length (Integer) + seqId (Long) = 20 bytes
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ByteBufferChunkKeyValue
extends|extends
name|ByteBufferKeyValue
block|{
specifier|public
name|ByteBufferChunkKeyValue
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
name|ByteBufferChunkKeyValue
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
name|int
name|getChunkId
parameter_list|()
block|{
comment|// The chunkId is embedded at the 0th offset of the bytebuffer
return|return
name|ByteBufferUtils
operator|.
name|toInt
argument_list|(
name|buf
argument_list|,
literal|0
argument_list|)
return|;
block|}
block|}
end_class

end_unit

