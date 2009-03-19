begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
import|import
name|java
operator|.
name|io
operator|.
name|DataInput
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|DataOutput
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
name|apache
operator|.
name|hadoop
operator|.
name|io
operator|.
name|Writable
import|;
end_import

begin_comment
comment|/**  * Batch update operation.  *   * If value is null, its a DELETE operation.  If its non-null, its a PUT.  * This object is purposely bare-bones because many instances are created  * during bulk uploads.  We have one class for DELETEs and PUTs rather than  * a class per type because it makes the serialization easier.  * @see BatchUpdate   */
end_comment

begin_class
specifier|public
class|class
name|BatchOperation
implements|implements
name|Writable
implements|,
name|HeapSize
block|{
comment|/**    * Estimated size of this object.    */
comment|// JHat says this is 32 bytes.
specifier|public
specifier|final
name|int
name|ESTIMATED_HEAP_TAX
init|=
literal|36
decl_stmt|;
specifier|private
name|byte
index|[]
name|column
init|=
literal|null
decl_stmt|;
comment|// A null value defines DELETE operations.
specifier|private
name|byte
index|[]
name|value
init|=
literal|null
decl_stmt|;
comment|/**    * Default constructor    */
specifier|public
name|BatchOperation
parameter_list|()
block|{
name|this
argument_list|(
operator|(
name|byte
index|[]
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates a DELETE batch operation.    * @param column column name    */
specifier|public
name|BatchOperation
parameter_list|(
specifier|final
name|byte
index|[]
name|column
parameter_list|)
block|{
name|this
argument_list|(
name|column
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Creates a DELETE batch operation.    * @param column column name    */
specifier|public
name|BatchOperation
parameter_list|(
specifier|final
name|String
name|column
parameter_list|)
block|{
name|this
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|column
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a batch operation.    * @param column column name    * @param value column value.  If non-null, this is a PUT operation.    */
specifier|public
name|BatchOperation
parameter_list|(
specifier|final
name|String
name|column
parameter_list|,
name|String
name|value
parameter_list|)
block|{
name|this
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|column
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a batch operation.    * @param column column name    * @param value column value.  If non-null, this is a PUT operation.    */
specifier|public
name|BatchOperation
parameter_list|(
specifier|final
name|byte
index|[]
name|column
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
operator|.
name|column
operator|=
name|column
expr_stmt|;
name|this
operator|.
name|value
operator|=
name|value
expr_stmt|;
block|}
comment|/**    * @return the column    */
specifier|public
name|byte
index|[]
name|getColumn
parameter_list|()
block|{
return|return
name|this
operator|.
name|column
return|;
block|}
comment|/**    * @return the value    */
specifier|public
name|byte
index|[]
name|getValue
parameter_list|()
block|{
return|return
name|this
operator|.
name|value
return|;
block|}
comment|/**    * @return True if this is a PUT operation (this.value is not null).    */
specifier|public
name|boolean
name|isPut
parameter_list|()
block|{
return|return
name|this
operator|.
name|value
operator|!=
literal|null
return|;
block|}
comment|/**    * @see java.lang.Object#toString()    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"column => "
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|this
operator|.
name|column
argument_list|)
operator|+
literal|", value => '...'"
return|;
block|}
comment|// Writable methods
comment|// This is a hotspot when updating deserializing incoming client submissions.
comment|// In Performance Evaluation sequentialWrite, 70% of object allocations are
comment|// done in here.
specifier|public
name|void
name|readFields
parameter_list|(
specifier|final
name|DataInput
name|in
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|column
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
comment|// Is there a value to read?
if|if
condition|(
name|in
operator|.
name|readBoolean
argument_list|()
condition|)
block|{
name|this
operator|.
name|value
operator|=
operator|new
name|byte
index|[
name|in
operator|.
name|readInt
argument_list|()
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|this
operator|.
name|value
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|write
parameter_list|(
specifier|final
name|DataOutput
name|out
parameter_list|)
throws|throws
name|IOException
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|column
argument_list|)
expr_stmt|;
name|boolean
name|p
init|=
name|isPut
argument_list|()
decl_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|p
argument_list|)
expr_stmt|;
if|if
condition|(
name|p
condition|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|value
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
return|return
name|Bytes
operator|.
name|ESTIMATED_HEAP_TAX
operator|*
literal|2
operator|+
name|this
operator|.
name|column
operator|.
name|length
operator|+
name|this
operator|.
name|value
operator|.
name|length
operator|+
name|ESTIMATED_HEAP_TAX
return|;
block|}
block|}
end_class

end_unit

