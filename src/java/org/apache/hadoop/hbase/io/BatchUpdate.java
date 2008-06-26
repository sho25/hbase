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
name|Iterator
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
name|HConstants
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
name|Text
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
comment|/**  * A Writable object that contains a series of BatchOperations  *   * There is one BatchUpdate object per server, so a series of batch operations  * can result in multiple BatchUpdate objects if the batch contains rows that  * are served by multiple region servers.  */
end_comment

begin_class
specifier|public
class|class
name|BatchUpdate
implements|implements
name|Writable
implements|,
name|Iterable
argument_list|<
name|BatchOperation
argument_list|>
block|{
comment|// the row being updated
specifier|private
name|byte
index|[]
name|row
init|=
literal|null
decl_stmt|;
comment|// the batched operations
specifier|private
name|ArrayList
argument_list|<
name|BatchOperation
argument_list|>
name|operations
init|=
operator|new
name|ArrayList
argument_list|<
name|BatchOperation
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|long
name|timestamp
init|=
name|HConstants
operator|.
name|LATEST_TIMESTAMP
decl_stmt|;
comment|/**    * Default constructor used serializing.    */
specifier|public
name|BatchUpdate
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
comment|/**    * Initialize a BatchUpdate operation on a row. Timestamp is assumed to be    * now.    *     * @param row    */
specifier|public
name|BatchUpdate
parameter_list|(
specifier|final
name|Text
name|row
parameter_list|)
block|{
name|this
argument_list|(
name|row
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
block|}
comment|/**    * Initialize a BatchUpdate operation on a row. Timestamp is assumed to be    * now.    *     * @param row    */
specifier|public
name|BatchUpdate
parameter_list|(
specifier|final
name|String
name|row
parameter_list|)
block|{
name|this
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
block|}
comment|/**    * Initialize a BatchUpdate operation on a row. Timestamp is assumed to be    * now.    *     * @param row    */
specifier|public
name|BatchUpdate
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|)
block|{
name|this
argument_list|(
name|row
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
block|}
comment|/**    * Initialize a BatchUpdate operation on a row with a specific timestamp.    *     * @param row    */
specifier|public
name|BatchUpdate
parameter_list|(
specifier|final
name|String
name|row
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
name|this
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|row
argument_list|)
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
block|}
comment|/**    * Initialize a BatchUpdate operation on a row with a specific timestamp.    *     * @param row    */
specifier|public
name|BatchUpdate
parameter_list|(
specifier|final
name|Text
name|row
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
name|this
argument_list|(
name|row
operator|.
name|getBytes
argument_list|()
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
block|}
comment|/**    * Initialize a BatchUpdate operation on a row with a specific timestamp.    *     * @param row    */
specifier|public
name|BatchUpdate
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
name|this
operator|.
name|operations
operator|=
operator|new
name|ArrayList
argument_list|<
name|BatchOperation
argument_list|>
argument_list|()
expr_stmt|;
block|}
comment|/** @return the row */
specifier|public
name|byte
index|[]
name|getRow
parameter_list|()
block|{
return|return
name|row
return|;
block|}
comment|/**    * Return the timestamp this BatchUpdate will be committed with.    */
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestamp
return|;
block|}
comment|/**    * Set this BatchUpdate's timestamp.    */
specifier|public
name|void
name|setTimestamp
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
block|}
comment|/**     * Change a value for the specified column    *    * @param column column whose value is being set    * @param val new value for column.  Cannot be null (can be empty).    */
specifier|public
specifier|synchronized
name|void
name|put
parameter_list|(
specifier|final
name|Text
name|column
parameter_list|,
specifier|final
name|byte
name|val
index|[]
parameter_list|)
block|{
name|put
argument_list|(
name|column
operator|.
name|getBytes
argument_list|()
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
comment|/**     * Change a value for the specified column    *    * @param column column whose value is being set    * @param val new value for column.  Cannot be null (can be empty).    */
specifier|public
specifier|synchronized
name|void
name|put
parameter_list|(
specifier|final
name|String
name|column
parameter_list|,
specifier|final
name|byte
name|val
index|[]
parameter_list|)
block|{
name|put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|column
argument_list|)
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
comment|/**     * Change a value for the specified column    *    * @param column column whose value is being set    * @param val new value for column.  Cannot be null (can be empty).    */
specifier|public
specifier|synchronized
name|void
name|put
parameter_list|(
specifier|final
name|byte
index|[]
name|column
parameter_list|,
specifier|final
name|byte
name|val
index|[]
parameter_list|)
block|{
if|if
condition|(
name|val
operator|==
literal|null
condition|)
block|{
comment|// If null, the PUT becomes a DELETE operation.
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Passed value cannot be null"
argument_list|)
throw|;
block|}
name|operations
operator|.
name|add
argument_list|(
operator|new
name|BatchOperation
argument_list|(
name|column
argument_list|,
name|val
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**     * Delete the value for a column    * Deletes the cell whose row/column/commit-timestamp match those of the    * delete.    * @param column name of column whose value is to be deleted    */
specifier|public
name|void
name|delete
parameter_list|(
specifier|final
name|Text
name|column
parameter_list|)
block|{
name|delete
argument_list|(
name|column
operator|.
name|getBytes
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**     * Delete the value for a column    * Deletes the cell whose row/column/commit-timestamp match those of the    * delete.    * @param column name of column whose value is to be deleted    */
specifier|public
name|void
name|delete
parameter_list|(
specifier|final
name|String
name|column
parameter_list|)
block|{
name|delete
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|column
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**     * Delete the value for a column    * Deletes the cell whose row/column/commit-timestamp match those of the    * delete.    * @param column name of column whose value is to be deleted    */
specifier|public
specifier|synchronized
name|void
name|delete
parameter_list|(
specifier|final
name|byte
index|[]
name|column
parameter_list|)
block|{
name|operations
operator|.
name|add
argument_list|(
operator|new
name|BatchOperation
argument_list|(
name|column
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//
comment|// Iterable
comment|//
comment|/**    * @return Iterator<BatchOperation>    */
specifier|public
name|Iterator
argument_list|<
name|BatchOperation
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
name|operations
operator|.
name|iterator
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|()
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|"row => "
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|row
operator|==
literal|null
condition|?
literal|""
else|:
name|Bytes
operator|.
name|toString
argument_list|(
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", {"
argument_list|)
expr_stmt|;
name|boolean
name|morethanone
init|=
literal|false
decl_stmt|;
for|for
control|(
name|BatchOperation
name|bo
range|:
name|this
operator|.
name|operations
control|)
block|{
if|if
condition|(
name|morethanone
condition|)
block|{
name|sb
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|morethanone
operator|=
literal|true
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|bo
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"}"
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
comment|//
comment|// Writable
comment|//
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
comment|// Clear any existing operations; may be hangovers from previous use of
comment|// this instance.
if|if
condition|(
name|this
operator|.
name|operations
operator|.
name|size
argument_list|()
operator|!=
literal|0
condition|)
block|{
name|this
operator|.
name|operations
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
name|this
operator|.
name|row
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|timestamp
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|int
name|nOps
init|=
name|in
operator|.
name|readInt
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
name|nOps
condition|;
name|i
operator|++
control|)
block|{
name|BatchOperation
name|op
init|=
operator|new
name|BatchOperation
argument_list|()
decl_stmt|;
name|op
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|this
operator|.
name|operations
operator|.
name|add
argument_list|(
name|op
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
name|row
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|timestamp
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeInt
argument_list|(
name|operations
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|BatchOperation
name|op
range|:
name|operations
control|)
block|{
name|op
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

