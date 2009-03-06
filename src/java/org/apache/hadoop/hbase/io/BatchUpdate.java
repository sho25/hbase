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
name|lang
operator|.
name|management
operator|.
name|ManagementFactory
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|management
operator|.
name|RuntimeMXBean
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
name|Arrays
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
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|io
operator|.
name|RowResult
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
name|WritableComparable
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
name|WritableComparable
argument_list|<
name|BatchUpdate
argument_list|>
implements|,
name|Iterable
argument_list|<
name|BatchOperation
argument_list|>
implements|,
name|HeapSize
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|BatchUpdate
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Estimated 'shallow size' of this object not counting payload.    */
comment|// Shallow size is 56.  Add 32 for the arraylist below.
specifier|public
specifier|static
specifier|final
name|int
name|ESTIMATED_HEAP_TAX
init|=
literal|56
operator|+
literal|32
decl_stmt|;
comment|// the row being updated
specifier|private
name|byte
index|[]
name|row
init|=
literal|null
decl_stmt|;
specifier|private
name|long
name|size
init|=
literal|0
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
specifier|private
name|long
name|rowLock
init|=
operator|-
literal|1l
decl_stmt|;
comment|/**    * Default constructor used serializing.  Do not use directly.    */
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
comment|/**    * Initialize a BatchUpdate operation on a row with a specific timestamp.    *     * @param row    * @param timestamp    */
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
comment|/**    * Recopy constructor    * @param buToCopy BatchUpdate to copy    */
specifier|public
name|BatchUpdate
parameter_list|(
name|BatchUpdate
name|buToCopy
parameter_list|)
block|{
name|this
argument_list|(
name|buToCopy
operator|.
name|getRow
argument_list|()
argument_list|,
name|buToCopy
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|BatchOperation
name|bo
range|:
name|buToCopy
control|)
block|{
name|byte
index|[]
name|val
init|=
name|bo
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|val
operator|==
literal|null
condition|)
block|{
comment|// Presume a delete is intended.
name|this
operator|.
name|delete
argument_list|(
name|bo
operator|.
name|getColumn
argument_list|()
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|put
argument_list|(
name|bo
operator|.
name|getColumn
argument_list|()
argument_list|,
name|val
argument_list|)
expr_stmt|;
block|}
block|}
block|}
comment|/**    * Initialize a BatchUpdate operation on a row with a specific timestamp.    *     * @param row    * @param timestamp    */
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
name|this
operator|.
name|size
operator|=
operator|(
name|row
operator|==
literal|null
operator|)
condition|?
literal|0
else|:
name|row
operator|.
name|length
expr_stmt|;
block|}
comment|/**    * Create a batch operation.    * @param rr the RowResult    */
specifier|public
name|BatchUpdate
parameter_list|(
specifier|final
name|RowResult
name|rr
parameter_list|)
block|{
name|this
argument_list|(
name|rr
operator|.
name|getRow
argument_list|()
argument_list|)
expr_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
name|entry
range|:
name|rr
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|this
operator|.
name|put
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
operator|.
name|getValue
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Get the row lock associated with this update    * @return the row lock    */
specifier|public
name|long
name|getRowLock
parameter_list|()
block|{
return|return
name|rowLock
return|;
block|}
comment|/**    * Set the lock to be used for this update    * @param rowLock the row lock    */
specifier|public
name|void
name|setRowLock
parameter_list|(
name|long
name|rowLock
parameter_list|)
block|{
name|this
operator|.
name|rowLock
operator|=
name|rowLock
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
comment|/**    * @return the timestamp this BatchUpdate will be committed with.    */
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestamp
return|;
block|}
comment|/**    * Set this BatchUpdate's timestamp.    *     * @param timestamp    */
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
comment|/**    * Get the current value of the specified column    *     * @param column column name    * @return byte[] the cell value, returns null if the column does not exist.    */
specifier|public
specifier|synchronized
name|byte
index|[]
name|get
parameter_list|(
specifier|final
name|String
name|column
parameter_list|)
block|{
return|return
name|get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|column
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Get the current value of the specified column     *     * @param column column name    * @return byte[] the cell value, returns null if the column does not exist.    */
specifier|public
specifier|synchronized
name|byte
index|[]
name|get
parameter_list|(
specifier|final
name|byte
index|[]
name|column
parameter_list|)
block|{
for|for
control|(
name|BatchOperation
name|operation
range|:
name|operations
control|)
block|{
if|if
condition|(
name|Arrays
operator|.
name|equals
argument_list|(
name|column
argument_list|,
name|operation
operator|.
name|getColumn
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|operation
operator|.
name|getValue
argument_list|()
return|;
block|}
block|}
return|return
literal|null
return|;
block|}
comment|/**    * Get the current columns    *     * @return byte[][] an array of byte[] columns    */
specifier|public
specifier|synchronized
name|byte
index|[]
index|[]
name|getColumns
parameter_list|()
block|{
name|byte
index|[]
index|[]
name|columns
init|=
operator|new
name|byte
index|[
name|operations
operator|.
name|size
argument_list|()
index|]
index|[]
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
name|operations
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|columns
index|[
name|i
index|]
operator|=
name|operations
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getColumn
argument_list|()
expr_stmt|;
block|}
return|return
name|columns
return|;
block|}
comment|/**    * Check if the specified column is currently assigned a value    *     * @param column column to check for    * @return boolean true if the given column exists    */
specifier|public
specifier|synchronized
name|boolean
name|hasColumn
parameter_list|(
name|String
name|column
parameter_list|)
block|{
return|return
name|hasColumn
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|column
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Check if the specified column is currently assigned a value    *     * @param column column to check for    * @return boolean true if the given column exists    */
specifier|public
specifier|synchronized
name|boolean
name|hasColumn
parameter_list|(
name|byte
index|[]
name|column
parameter_list|)
block|{
name|byte
index|[]
name|getColumn
init|=
name|get
argument_list|(
name|column
argument_list|)
decl_stmt|;
if|if
condition|(
name|getColumn
operator|==
literal|null
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
literal|true
return|;
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
name|BatchOperation
name|bo
init|=
operator|new
name|BatchOperation
argument_list|(
name|column
argument_list|,
name|val
argument_list|)
decl_stmt|;
name|this
operator|.
name|size
operator|+=
name|bo
operator|.
name|heapSize
argument_list|()
expr_stmt|;
name|operations
operator|.
name|add
argument_list|(
name|bo
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
name|this
operator|.
name|size
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
name|this
operator|.
name|rowLock
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
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
name|writeLong
argument_list|(
name|this
operator|.
name|size
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
name|out
operator|.
name|writeLong
argument_list|(
name|this
operator|.
name|rowLock
argument_list|)
expr_stmt|;
block|}
specifier|public
name|int
name|compareTo
parameter_list|(
name|BatchUpdate
name|o
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|compareTo
argument_list|(
name|this
operator|.
name|row
argument_list|,
name|o
operator|.
name|getRow
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|long
name|heapSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|row
operator|.
name|length
operator|+
name|Bytes
operator|.
name|ESTIMATED_HEAP_TAX
operator|+
name|this
operator|.
name|size
operator|+
name|ESTIMATED_HEAP_TAX
return|;
block|}
comment|/**    * Code to test sizes of BatchUpdate arrays.    * @param args    * @throws InterruptedException    */
specifier|public
specifier|static
name|void
name|main
parameter_list|(
name|String
index|[]
name|args
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|RuntimeMXBean
name|runtime
init|=
name|ManagementFactory
operator|.
name|getRuntimeMXBean
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"vmName="
operator|+
name|runtime
operator|.
name|getVmName
argument_list|()
operator|+
literal|", vmVendor="
operator|+
name|runtime
operator|.
name|getVmVendor
argument_list|()
operator|+
literal|", vmVersion="
operator|+
name|runtime
operator|.
name|getVmVersion
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"vmInputArguments="
operator|+
name|runtime
operator|.
name|getInputArguments
argument_list|()
argument_list|)
expr_stmt|;
specifier|final
name|int
name|count
init|=
literal|10000
decl_stmt|;
name|BatchUpdate
index|[]
name|batch1
init|=
operator|new
name|BatchUpdate
index|[
name|count
index|]
decl_stmt|;
comment|// TODO: x32 vs x64
name|long
name|size
init|=
literal|0
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|BatchUpdate
name|bu
init|=
operator|new
name|BatchUpdate
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
decl_stmt|;
name|bu
operator|.
name|put
argument_list|(
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|)
expr_stmt|;
name|batch1
index|[
name|i
index|]
operator|=
name|bu
expr_stmt|;
name|size
operator|+=
name|bu
operator|.
name|heapSize
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"batch1 estimated size="
operator|+
name|size
argument_list|)
expr_stmt|;
comment|// Make a variably sized memcache.
name|size
operator|=
literal|0
expr_stmt|;
name|BatchUpdate
index|[]
name|batch2
init|=
operator|new
name|BatchUpdate
index|[
name|count
index|]
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
name|count
condition|;
name|i
operator|++
control|)
block|{
name|BatchUpdate
name|bu
init|=
operator|new
name|BatchUpdate
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|)
decl_stmt|;
name|bu
operator|.
name|put
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|i
argument_list|)
argument_list|,
operator|new
name|byte
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|batch2
index|[
name|i
index|]
operator|=
name|bu
expr_stmt|;
name|size
operator|+=
name|bu
operator|.
name|heapSize
argument_list|()
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"batch2 estimated size="
operator|+
name|size
argument_list|)
expr_stmt|;
specifier|final
name|int
name|seconds
init|=
literal|30
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Waiting "
operator|+
name|seconds
operator|+
literal|" seconds while heap dump is taken"
argument_list|)
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|seconds
condition|;
name|i
operator|++
control|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
literal|1000
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Exiting."
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

