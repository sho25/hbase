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
name|io
operator|.
name|UnsupportedEncodingException
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
comment|/**  * A log value.  *  * These aren't sortable; you need to sort by the matching HLogKey.  * TODO: Remove.  Just output KVs.  */
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
comment|/** Value written to HLog on a complete cache flush.  TODO: Remove.  Not used.    */
specifier|static
name|byte
index|[]
name|COMPLETE_CACHE_FLUSH
decl_stmt|;
static|static
block|{
try|try
block|{
name|COMPLETE_CACHE_FLUSH
operator|=
literal|"HBASE::CACHEFLUSH"
operator|.
name|getBytes
argument_list|(
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
assert|assert
operator|(
literal|false
operator|)
assert|;
block|}
block|}
comment|/** If transactional log entry, these are the op codes */
specifier|public
enum|enum
name|TransactionalOperation
block|{
comment|/** start transaction */
name|START
block|,
comment|/** Equivalent to append in non-transactional environment */
name|WRITE
block|,
comment|/** Transaction commit entry */
name|COMMIT
block|,
comment|/** Abort transaction entry */
name|ABORT
block|}
specifier|private
name|KeyValue
name|kv
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
name|this
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Construct a fully initialized HLogEdit    * @param kv    */
specifier|public
name|HLogEdit
parameter_list|(
specifier|final
name|KeyValue
name|kv
parameter_list|)
block|{
name|this
operator|.
name|kv
operator|=
name|kv
expr_stmt|;
name|this
operator|.
name|isTransactionEntry
operator|=
literal|false
expr_stmt|;
block|}
comment|/**     * Construct a WRITE transaction.     * @param transactionId    * @param op    * @param timestamp    */
specifier|public
name|HLogEdit
parameter_list|(
name|long
name|transactionId
parameter_list|,
specifier|final
name|byte
index|[]
name|row
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
operator|new
name|KeyValue
argument_list|(
name|row
argument_list|,
name|op
operator|.
name|getColumn
argument_list|()
argument_list|,
name|timestamp
argument_list|,
name|op
operator|.
name|isPut
argument_list|()
condition|?
name|KeyValue
operator|.
name|Type
operator|.
name|Put
else|:
name|KeyValue
operator|.
name|Type
operator|.
name|Delete
argument_list|,
name|op
operator|.
name|getValue
argument_list|()
argument_list|)
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
name|kv
operator|=
name|KeyValue
operator|.
name|LOWESTKEY
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
comment|/** @return the KeyValue */
specifier|public
name|KeyValue
name|getKeyValue
parameter_list|()
block|{
return|return
name|this
operator|.
name|kv
return|;
block|}
comment|/** @return true if entry is a transactional entry */
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
name|kv
operator|.
name|getValueLength
argument_list|()
operator|>
name|MAX_VALUE_LEN
operator|)
condition|?
operator|new
name|String
argument_list|(
name|this
operator|.
name|kv
operator|.
name|getValue
argument_list|()
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
name|this
operator|.
name|kv
operator|.
name|getValue
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
name|this
operator|.
name|kv
operator|.
name|toString
argument_list|()
operator|+
operator|(
name|isTransactionEntry
condition|?
literal|"/tran="
operator|+
name|transactionId
operator|+
literal|"/op="
operator|+
name|operation
operator|.
name|toString
argument_list|()
else|:
literal|""
operator|)
operator|+
literal|"/value="
operator|+
name|value
return|;
block|}
comment|// Writable
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
name|kv
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|kv
operator|.
name|getOffset
argument_list|()
argument_list|,
name|kv
operator|.
name|getLength
argument_list|()
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
name|byte
index|[]
name|kvbytes
init|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
decl_stmt|;
name|this
operator|.
name|kv
operator|=
operator|new
name|KeyValue
argument_list|(
name|kvbytes
argument_list|,
literal|0
argument_list|,
name|kvbytes
operator|.
name|length
argument_list|)
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

