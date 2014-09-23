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
name|util
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
name|io
operator|.
name|OutputStream
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
name|Collection
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
name|classification
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
name|classification
operator|.
name|InterfaceStability
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

begin_comment
comment|/**  * Utility methods for working with {@link ByteRange}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|ByteRangeUtils
block|{
specifier|public
specifier|static
name|int
name|numEqualPrefixBytes
parameter_list|(
name|ByteRange
name|left
parameter_list|,
name|ByteRange
name|right
parameter_list|,
name|int
name|rightInnerOffset
parameter_list|)
block|{
name|int
name|maxCompares
init|=
name|Math
operator|.
name|min
argument_list|(
name|left
operator|.
name|getLength
argument_list|()
argument_list|,
name|right
operator|.
name|getLength
argument_list|()
operator|-
name|rightInnerOffset
argument_list|)
decl_stmt|;
specifier|final
name|byte
index|[]
name|lbytes
init|=
name|left
operator|.
name|getBytes
argument_list|()
decl_stmt|,
name|rbytes
init|=
name|right
operator|.
name|getBytes
argument_list|()
decl_stmt|;
specifier|final
name|int
name|loffset
init|=
name|left
operator|.
name|getOffset
argument_list|()
decl_stmt|,
name|roffset
init|=
name|right
operator|.
name|getOffset
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
name|maxCompares
condition|;
operator|++
name|i
control|)
block|{
if|if
condition|(
name|lbytes
index|[
name|loffset
operator|+
name|i
index|]
operator|!=
name|rbytes
index|[
name|roffset
operator|+
name|rightInnerOffset
operator|+
name|i
index|]
condition|)
block|{
return|return
name|i
return|;
block|}
block|}
return|return
name|maxCompares
return|;
block|}
specifier|public
specifier|static
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|copyToNewArrays
parameter_list|(
name|Collection
argument_list|<
name|ByteRange
argument_list|>
name|ranges
parameter_list|)
block|{
if|if
condition|(
name|ranges
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
literal|0
argument_list|)
return|;
block|}
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|arrays
init|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
name|ranges
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|ByteRange
name|range
range|:
name|ranges
control|)
block|{
name|arrays
operator|.
name|add
argument_list|(
name|range
operator|.
name|deepCopyToNewArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
name|arrays
return|;
block|}
specifier|public
specifier|static
name|ArrayList
argument_list|<
name|ByteRange
argument_list|>
name|fromArrays
parameter_list|(
name|Collection
argument_list|<
name|byte
index|[]
argument_list|>
name|arrays
parameter_list|)
block|{
if|if
condition|(
name|arrays
operator|==
literal|null
condition|)
block|{
return|return
operator|new
name|ArrayList
argument_list|<
name|ByteRange
argument_list|>
argument_list|(
literal|0
argument_list|)
return|;
block|}
name|ArrayList
argument_list|<
name|ByteRange
argument_list|>
name|ranges
init|=
name|Lists
operator|.
name|newArrayListWithCapacity
argument_list|(
name|arrays
operator|.
name|size
argument_list|()
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|array
range|:
name|arrays
control|)
block|{
name|ranges
operator|.
name|add
argument_list|(
operator|new
name|SimpleMutableByteRange
argument_list|(
name|array
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|ranges
return|;
block|}
specifier|public
specifier|static
name|void
name|write
parameter_list|(
name|OutputStream
name|os
parameter_list|,
name|ByteRange
name|byteRange
parameter_list|)
throws|throws
name|IOException
block|{
name|os
operator|.
name|write
argument_list|(
name|byteRange
operator|.
name|getBytes
argument_list|()
argument_list|,
name|byteRange
operator|.
name|getOffset
argument_list|()
argument_list|,
name|byteRange
operator|.
name|getLength
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|static
name|void
name|write
parameter_list|(
name|OutputStream
name|os
parameter_list|,
name|ByteRange
name|byteRange
parameter_list|,
name|int
name|byteRangeInnerOffset
parameter_list|)
throws|throws
name|IOException
block|{
name|os
operator|.
name|write
argument_list|(
name|byteRange
operator|.
name|getBytes
argument_list|()
argument_list|,
name|byteRange
operator|.
name|getOffset
argument_list|()
operator|+
name|byteRangeInnerOffset
argument_list|,
name|byteRange
operator|.
name|getLength
argument_list|()
operator|-
name|byteRangeInnerOffset
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

