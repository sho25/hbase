begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
name|hfile
operator|.
name|bucket
package|;
end_package

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
name|io
operator|.
name|hfile
operator|.
name|Cacheable
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
name|hfile
operator|.
name|Cacheable
operator|.
name|MemoryType
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
name|ByteBuff
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
comment|/**  * IO engine that stores data to a file on the local block device using memory mapping mechanism  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ExclusiveMemoryMmapIOEngine
extends|extends
name|FileMmapIOEngine
block|{
specifier|public
name|ExclusiveMemoryMmapIOEngine
parameter_list|(
name|String
name|filePath
parameter_list|,
name|long
name|capacity
parameter_list|)
throws|throws
name|IOException
block|{
name|super
argument_list|(
name|filePath
argument_list|,
name|capacity
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Cacheable
name|read
parameter_list|(
name|BucketEntry
name|be
parameter_list|)
throws|throws
name|IOException
block|{
name|ByteBuff
name|dst
init|=
name|ByteBuff
operator|.
name|wrap
argument_list|(
name|ByteBuffer
operator|.
name|allocate
argument_list|(
name|be
operator|.
name|getLength
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|bufferArray
operator|.
name|read
argument_list|(
name|be
operator|.
name|offset
argument_list|()
argument_list|,
name|dst
argument_list|)
expr_stmt|;
name|dst
operator|.
name|position
argument_list|(
literal|0
argument_list|)
operator|.
name|limit
argument_list|(
name|be
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|be
operator|.
name|wrapAsCacheable
argument_list|(
name|dst
operator|.
name|nioByteBuffers
argument_list|()
argument_list|,
name|MemoryType
operator|.
name|EXCLUSIVE
argument_list|)
return|;
block|}
block|}
end_class

end_unit

