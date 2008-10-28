begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2008 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
comment|/**  * Cell - Used to transport a cell value (byte[]) and the timestamp it was   * stored with together as a result for get and getRow methods. This promotes  * the timestamp of a cell to a first-class value, making it easy to take   * note of temporal data. Cell is used all the way from HStore up to HTable.  */
end_comment

begin_class
specifier|public
class|class
name|Cell
implements|implements
name|Writable
implements|,
name|Iterable
argument_list|<
name|Cell
argument_list|>
block|{
specifier|protected
name|byte
index|[]
index|[]
name|values
decl_stmt|;
specifier|protected
name|long
index|[]
name|timestamps
decl_stmt|;
comment|/** For Writable compatibility */
specifier|public
name|Cell
parameter_list|()
block|{
name|values
operator|=
literal|null
expr_stmt|;
name|timestamps
operator|=
literal|null
expr_stmt|;
block|}
comment|/**    * Create a new Cell with a given value and timestamp. Used by HStore.    * @param value    * @param timestamp    */
specifier|public
name|Cell
parameter_list|(
name|String
name|value
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
name|value
argument_list|)
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a new Cell with a given value and timestamp. Used by HStore.    * @param value    * @param timestamp    */
specifier|public
name|Cell
parameter_list|(
name|byte
index|[]
name|value
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|values
operator|=
operator|new
name|byte
index|[
literal|1
index|]
index|[]
expr_stmt|;
name|this
operator|.
name|values
index|[
literal|0
index|]
operator|=
name|value
expr_stmt|;
name|this
operator|.
name|timestamps
operator|=
operator|new
name|long
index|[
literal|1
index|]
expr_stmt|;
name|this
operator|.
name|timestamps
index|[
literal|0
index|]
operator|=
name|timestamp
expr_stmt|;
block|}
comment|/**    * @param vals array of values    * @param ts array of timestamps    */
specifier|public
name|Cell
parameter_list|(
name|String
index|[]
name|vals
parameter_list|,
name|long
index|[]
name|ts
parameter_list|)
block|{
if|if
condition|(
name|vals
operator|.
name|length
operator|!=
name|ts
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"number of values must be the same as the number of timestamps"
argument_list|)
throw|;
block|}
name|this
operator|.
name|values
operator|=
operator|new
name|byte
index|[
name|vals
operator|.
name|length
index|]
index|[]
expr_stmt|;
name|this
operator|.
name|timestamps
operator|=
operator|new
name|long
index|[
name|ts
operator|.
name|length
index|]
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
name|values
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|values
index|[
name|i
index|]
operator|=
name|Bytes
operator|.
name|toBytes
argument_list|(
name|vals
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|this
operator|.
name|timestamps
index|[
name|i
index|]
operator|=
name|ts
index|[
name|i
index|]
expr_stmt|;
block|}
block|}
comment|/**    * @param vals array of values    * @param ts array of timestamps    */
specifier|public
name|Cell
parameter_list|(
name|byte
index|[]
index|[]
name|vals
parameter_list|,
name|long
index|[]
name|ts
parameter_list|)
block|{
if|if
condition|(
name|vals
operator|.
name|length
operator|!=
name|ts
operator|.
name|length
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"number of values must be the same as the number of timestamps"
argument_list|)
throw|;
block|}
name|this
operator|.
name|values
operator|=
operator|new
name|byte
index|[
name|vals
operator|.
name|length
index|]
index|[]
expr_stmt|;
name|this
operator|.
name|timestamps
operator|=
operator|new
name|long
index|[
name|ts
operator|.
name|length
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|vals
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|values
argument_list|,
literal|0
argument_list|,
name|vals
operator|.
name|length
argument_list|)
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|ts
argument_list|,
literal|0
argument_list|,
name|this
operator|.
name|timestamps
argument_list|,
literal|0
argument_list|,
name|ts
operator|.
name|length
argument_list|)
expr_stmt|;
block|}
comment|/** @return the current cell's value */
specifier|public
name|byte
index|[]
name|getValue
parameter_list|()
block|{
return|return
name|values
index|[
literal|0
index|]
return|;
block|}
comment|/** @return the current cell's timestamp */
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestamps
index|[
literal|0
index|]
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|values
operator|.
name|length
operator|==
literal|1
condition|)
block|{
return|return
literal|"timestamp="
operator|+
name|this
operator|.
name|timestamps
index|[
literal|0
index|]
operator|+
literal|", value="
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|this
operator|.
name|values
index|[
literal|0
index|]
argument_list|)
return|;
block|}
name|StringBuilder
name|s
init|=
operator|new
name|StringBuilder
argument_list|(
literal|"{ "
argument_list|)
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
name|this
operator|.
name|values
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|i
operator|>
literal|0
condition|)
block|{
name|s
operator|.
name|append
argument_list|(
literal|", "
argument_list|)
expr_stmt|;
block|}
name|s
operator|.
name|append
argument_list|(
literal|"[timestamp="
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
name|timestamps
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
literal|", value="
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|values
index|[
name|i
index|]
argument_list|)
argument_list|)
expr_stmt|;
name|s
operator|.
name|append
argument_list|(
literal|"]"
argument_list|)
expr_stmt|;
block|}
name|s
operator|.
name|append
argument_list|(
literal|" }"
argument_list|)
expr_stmt|;
return|return
name|s
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
name|int
name|nvalues
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
name|this
operator|.
name|timestamps
operator|=
operator|new
name|long
index|[
name|nvalues
index|]
expr_stmt|;
name|this
operator|.
name|values
operator|=
operator|new
name|byte
index|[
name|nvalues
index|]
index|[]
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
name|nvalues
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|timestamps
index|[
name|i
index|]
operator|=
name|in
operator|.
name|readLong
argument_list|()
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|nvalues
condition|;
name|i
operator|++
control|)
block|{
name|this
operator|.
name|values
index|[
name|i
index|]
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
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
name|out
operator|.
name|writeInt
argument_list|(
name|this
operator|.
name|values
operator|.
name|length
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
name|this
operator|.
name|timestamps
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|out
operator|.
name|writeLong
argument_list|(
name|this
operator|.
name|timestamps
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|this
operator|.
name|values
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|Bytes
operator|.
name|writeByteArray
argument_list|(
name|out
argument_list|,
name|this
operator|.
name|values
index|[
name|i
index|]
argument_list|)
expr_stmt|;
block|}
block|}
comment|//
comment|// Iterable
comment|//
specifier|public
name|Iterator
argument_list|<
name|Cell
argument_list|>
name|iterator
parameter_list|()
block|{
return|return
operator|new
name|CellIterator
argument_list|()
return|;
block|}
specifier|private
class|class
name|CellIterator
implements|implements
name|Iterator
argument_list|<
name|Cell
argument_list|>
block|{
specifier|private
name|int
name|currentValue
init|=
literal|0
decl_stmt|;
name|CellIterator
parameter_list|()
block|{     }
specifier|public
name|boolean
name|hasNext
parameter_list|()
block|{
return|return
name|currentValue
operator|+
literal|1
operator|<
name|values
operator|.
name|length
return|;
block|}
specifier|public
name|Cell
name|next
parameter_list|()
block|{
name|Cell
name|c
init|=
operator|new
name|Cell
argument_list|(
name|values
index|[
name|currentValue
index|]
argument_list|,
name|timestamps
index|[
name|currentValue
index|]
argument_list|)
decl_stmt|;
name|currentValue
operator|++
expr_stmt|;
return|return
name|c
return|;
block|}
specifier|public
name|void
name|remove
parameter_list|()
throws|throws
name|UnsupportedOperationException
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"remove is not supported"
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

