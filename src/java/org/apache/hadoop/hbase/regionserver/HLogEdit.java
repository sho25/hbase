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
name|regionserver
package|;
end_package

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
name|BatchOperation
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
name|ImmutableBytesWritable
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
name|*
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|*
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

begin_comment
comment|/**  * A log value.  *  * These aren't sortable; you need to sort by the matching HLogKey.  * The table and row are already identified in HLogKey.  * This just indicates the column and value.  */
end_comment

begin_class
specifier|public
class|class
name|HLogEdit
implements|implements
name|Writable
implements|,
name|HConstants
block|{
comment|/** Value stored for a deleted item */
specifier|public
specifier|static
name|ImmutableBytesWritable
name|deleteBytes
init|=
literal|null
decl_stmt|;
comment|/** Value written to HLog on a complete cache flush */
specifier|public
specifier|static
name|ImmutableBytesWritable
name|completeCacheFlush
init|=
literal|null
decl_stmt|;
static|static
block|{
try|try
block|{
name|deleteBytes
operator|=
operator|new
name|ImmutableBytesWritable
argument_list|(
literal|"HBASE::DELETEVAL"
operator|.
name|getBytes
argument_list|(
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
name|completeCacheFlush
operator|=
operator|new
name|ImmutableBytesWritable
argument_list|(
literal|"HBASE::CACHEFLUSH"
operator|.
name|getBytes
argument_list|(
name|UTF8_ENCODING
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
assert|assert
operator|(
literal|false
operator|)
assert|;
block|}
block|}
comment|/**    * @param value    * @return True if an entry and its content is {@link #deleteBytes}.    */
specifier|public
specifier|static
name|boolean
name|isDeleted
parameter_list|(
specifier|final
name|byte
index|[]
name|value
parameter_list|)
block|{
return|return
operator|(
name|value
operator|==
literal|null
operator|)
condition|?
literal|false
else|:
name|deleteBytes
operator|.
name|compareTo
argument_list|(
name|value
argument_list|)
operator|==
literal|0
return|;
block|}
specifier|public
enum|enum
name|TransactionalOperation
block|{
name|START
block|,
name|WRITE
block|,
name|COMMIT
block|,
name|ABORT
block|}
specifier|private
name|byte
index|[]
name|column
decl_stmt|;
specifier|private
name|byte
index|[]
name|val
decl_stmt|;
specifier|private
name|long
name|timestamp
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|int
name|MAX_VALUE_LEN
init|=
literal|128
decl_stmt|;
specifier|private
name|boolean
name|isTransactionEntry
decl_stmt|;
specifier|private
name|Long
name|transactionId
init|=
literal|null
decl_stmt|;
specifier|private
name|TransactionalOperation
name|operation
decl_stmt|;
comment|/**    * Default constructor used by Writable    */
specifier|public
name|HLogEdit
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Construct a fully initialized HLogEdit    * @param c column name    * @param bval value    * @param timestamp timestamp for modification    */
specifier|public
name|HLogEdit
parameter_list|(
name|byte
index|[]
name|c
parameter_list|,
name|byte
index|[]
name|bval
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|column
operator|=
name|c
expr_stmt|;
name|this
operator|.
name|val
operator|=
name|bval
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
name|this
operator|.
name|isTransactionEntry
operator|=
literal|false
expr_stmt|;
block|}
comment|/** Construct a WRITE transaction.     *     * @param transactionId    * @param op    * @param timestamp    */
specifier|public
name|HLogEdit
parameter_list|(
name|long
name|transactionId
parameter_list|,
name|BatchOperation
name|op
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
name|this
argument_list|(
name|op
operator|.
name|getColumn
argument_list|()
argument_list|,
name|op
operator|.
name|getValue
argument_list|()
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
comment|// This covers delete ops too...
name|this
operator|.
name|transactionId
operator|=
name|transactionId
expr_stmt|;
name|this
operator|.
name|operation
operator|=
name|TransactionalOperation
operator|.
name|WRITE
expr_stmt|;
name|this
operator|.
name|isTransactionEntry
operator|=
literal|true
expr_stmt|;
block|}
comment|/** Construct a transactional operation (BEGIN, ABORT, or COMMIT).     *     * @param transactionId    * @param op    */
specifier|public
name|HLogEdit
parameter_list|(
name|long
name|transactionId
parameter_list|,
name|TransactionalOperation
name|op
parameter_list|)
block|{
name|this
operator|.
name|column
operator|=
operator|new
name|byte
index|[
literal|0
index|]
expr_stmt|;
name|this
operator|.
name|val
operator|=
operator|new
name|byte
index|[
literal|0
index|]
expr_stmt|;
name|this
operator|.
name|transactionId
operator|=
name|transactionId
expr_stmt|;
name|this
operator|.
name|operation
operator|=
name|op
expr_stmt|;
name|this
operator|.
name|isTransactionEntry
operator|=
literal|true
expr_stmt|;
block|}
comment|/** @return the column */
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
comment|/** @return the value */
specifier|public
name|byte
index|[]
name|getVal
parameter_list|()
block|{
return|return
name|this
operator|.
name|val
return|;
block|}
comment|/** @return the timestamp */
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|this
operator|.
name|timestamp
return|;
block|}
specifier|public
name|boolean
name|isTransactionEntry
parameter_list|()
block|{
return|return
name|isTransactionEntry
return|;
block|}
comment|/**    * Get the transactionId, or null if this is not a transactional edit.    *     * @return Return the transactionId.    */
specifier|public
name|Long
name|getTransactionId
parameter_list|()
block|{
return|return
name|transactionId
return|;
block|}
comment|/**    * Get the operation.    *     * @return Return the operation.    */
specifier|public
name|TransactionalOperation
name|getOperation
parameter_list|()
block|{
return|return
name|operation
return|;
block|}
comment|/**    * @return First column name, timestamp, and first 128 bytes of the value    * bytes as a String.    */
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|String
name|value
init|=
literal|""
decl_stmt|;
try|try
block|{
name|value
operator|=
operator|(
name|this
operator|.
name|val
operator|.
name|length
operator|>
name|MAX_VALUE_LEN
operator|)
condition|?
operator|new
name|String
argument_list|(
name|this
operator|.
name|val
argument_list|,
literal|0
argument_list|,
name|MAX_VALUE_LEN
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
operator|+
literal|"..."
else|:
operator|new
name|String
argument_list|(
name|getVal
argument_list|()
argument_list|,
name|HConstants
operator|.
name|UTF8_ENCODING
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedEncodingException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"UTF8 encoding not present?"
argument_list|,
name|e
argument_list|)
throw|;
block|}
return|return
literal|"("
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|getColumn
argument_list|()
argument_list|)
operator|+
literal|"/"
operator|+
name|getTimestamp
argument_list|()
operator|+
literal|"/"
operator|+
operator|(
name|isTransactionEntry
condition|?
literal|"tran: "
operator|+
name|transactionId
operator|+
literal|" op "
operator|+
name|operation
operator|.
name|toString
argument_list|()
operator|+
literal|"/"
else|:
literal|""
operator|)
operator|+
name|value
operator|+
literal|")"
return|;
block|}
comment|// Writable
comment|/** {@inheritDoc} */
specifier|public
name|void
name|write
parameter_list|(
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
if|if
condition|(
name|this
operator|.
name|val
operator|==
literal|null
condition|)
block|{
name|out
operator|.
name|writeInt
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|out
operator|.
name|writeInt
argument_list|(
name|this
operator|.
name|val
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|this
operator|.
name|val
argument_list|)
expr_stmt|;
block|}
name|out
operator|.
name|writeLong
argument_list|(
name|timestamp
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|isTransactionEntry
argument_list|)
expr_stmt|;
if|if
condition|(
name|isTransactionEntry
condition|)
block|{
name|out
operator|.
name|writeLong
argument_list|(
name|transactionId
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|operation
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/** {@inheritDoc} */
specifier|public
name|void
name|readFields
parameter_list|(
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
name|this
operator|.
name|val
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
name|val
argument_list|)
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|isTransactionEntry
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
if|if
condition|(
name|isTransactionEntry
condition|)
block|{
name|transactionId
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
name|operation
operator|=
name|TransactionalOperation
operator|.
name|valueOf
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

