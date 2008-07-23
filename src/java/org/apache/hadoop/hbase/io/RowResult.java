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
name|java
operator|.
name|util
operator|.
name|Collections
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
name|java
operator|.
name|util
operator|.
name|Set
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|TreeSet
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
name|hbase
operator|.
name|util
operator|.
name|Writables
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
comment|/**  * Holds row name and then a map of columns to cells.  */
end_comment

begin_class
specifier|public
class|class
name|RowResult
implements|implements
name|Writable
implements|,
name|Map
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
block|{
specifier|private
name|byte
index|[]
name|row
init|=
literal|null
decl_stmt|;
specifier|private
specifier|final
name|HbaseMapWritable
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
name|cells
decl_stmt|;
specifier|public
name|RowResult
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|,
operator|new
name|HbaseMapWritable
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create a RowResult from a row and Cell map    */
specifier|public
name|RowResult
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|HbaseMapWritable
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
name|m
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
name|cells
operator|=
name|m
expr_stmt|;
block|}
comment|/**    * Get the row for this RowResult    */
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
comment|//
comment|// Map interface
comment|//
specifier|public
name|Cell
name|put
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|byte
index|[]
name|key
parameter_list|,
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|Cell
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"RowResult is read-only!"
argument_list|)
throw|;
block|}
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unchecked"
argument_list|)
specifier|public
name|void
name|putAll
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|Map
name|map
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"RowResult is read-only!"
argument_list|)
throw|;
block|}
specifier|public
name|Cell
name|get
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
return|return
operator|(
name|Cell
operator|)
name|this
operator|.
name|cells
operator|.
name|get
argument_list|(
name|key
argument_list|)
return|;
block|}
specifier|public
name|Cell
name|remove
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|Object
name|key
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"RowResult is read-only!"
argument_list|)
throw|;
block|}
specifier|public
name|boolean
name|containsKey
parameter_list|(
name|Object
name|key
parameter_list|)
block|{
return|return
name|cells
operator|.
name|containsKey
argument_list|(
name|key
argument_list|)
return|;
block|}
specifier|public
name|boolean
name|containsValue
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|Object
name|value
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"Don't support containsValue!"
argument_list|)
throw|;
block|}
specifier|public
name|boolean
name|isEmpty
parameter_list|()
block|{
return|return
name|cells
operator|.
name|isEmpty
argument_list|()
return|;
block|}
specifier|public
name|int
name|size
parameter_list|()
block|{
return|return
name|cells
operator|.
name|size
argument_list|()
return|;
block|}
specifier|public
name|void
name|clear
parameter_list|()
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"RowResult is read-only!"
argument_list|)
throw|;
block|}
specifier|public
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|keySet
parameter_list|()
block|{
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|result
init|=
operator|new
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
argument_list|(
name|Bytes
operator|.
name|BYTES_COMPARATOR
argument_list|)
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|w
range|:
name|cells
operator|.
name|keySet
argument_list|()
control|)
block|{
name|result
operator|.
name|add
argument_list|(
name|w
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
specifier|public
name|Set
argument_list|<
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
argument_list|>
name|entrySet
parameter_list|()
block|{
return|return
name|Collections
operator|.
name|unmodifiableSet
argument_list|(
name|this
operator|.
name|cells
operator|.
name|entrySet
argument_list|()
argument_list|)
return|;
block|}
specifier|public
name|Collection
argument_list|<
name|Cell
argument_list|>
name|values
parameter_list|()
block|{
name|ArrayList
argument_list|<
name|Cell
argument_list|>
name|result
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|Writable
name|w
range|:
name|cells
operator|.
name|values
argument_list|()
control|)
block|{
name|result
operator|.
name|add
argument_list|(
operator|(
name|Cell
operator|)
name|w
argument_list|)
expr_stmt|;
block|}
return|return
name|result
return|;
block|}
comment|/**    * Get the Cell that corresponds to column    */
specifier|public
name|Cell
name|get
parameter_list|(
name|byte
index|[]
name|column
parameter_list|)
block|{
return|return
name|this
operator|.
name|cells
operator|.
name|get
argument_list|(
name|column
argument_list|)
return|;
block|}
comment|/**    * Get the Cell that corresponds to column, using a String key    */
specifier|public
name|Cell
name|get
parameter_list|(
name|String
name|key
parameter_list|)
block|{
return|return
name|get
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|key
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Row entry.    */
specifier|public
class|class
name|Entry
implements|implements
name|Map
operator|.
name|Entry
argument_list|<
name|byte
index|[]
argument_list|,
name|Cell
argument_list|>
block|{
specifier|private
specifier|final
name|byte
index|[]
name|column
decl_stmt|;
specifier|private
specifier|final
name|Cell
name|cell
decl_stmt|;
name|Entry
parameter_list|(
name|byte
index|[]
name|row
parameter_list|,
name|Cell
name|cell
parameter_list|)
block|{
name|this
operator|.
name|column
operator|=
name|row
expr_stmt|;
name|this
operator|.
name|cell
operator|=
name|cell
expr_stmt|;
block|}
specifier|public
name|Cell
name|setValue
parameter_list|(
annotation|@
name|SuppressWarnings
argument_list|(
literal|"unused"
argument_list|)
name|Cell
name|c
parameter_list|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"RowResult is read-only!"
argument_list|)
throw|;
block|}
specifier|public
name|byte
index|[]
name|getKey
parameter_list|()
block|{
return|return
name|column
return|;
block|}
specifier|public
name|Cell
name|getValue
parameter_list|()
block|{
return|return
name|cell
return|;
block|}
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
literal|"row="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|this
operator|.
name|row
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", cells={"
argument_list|)
expr_stmt|;
name|boolean
name|moreThanOne
init|=
literal|false
decl_stmt|;
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
name|e
range|:
name|this
operator|.
name|cells
operator|.
name|entrySet
argument_list|()
control|)
block|{
if|if
condition|(
name|moreThanOne
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
else|else
block|{
name|moreThanOne
operator|=
literal|true
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|"(column="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|Bytes
operator|.
name|toString
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", timestamp="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|Long
operator|.
name|toString
argument_list|(
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getTimestamp
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", value="
argument_list|)
expr_stmt|;
name|byte
index|[]
name|v
init|=
name|e
operator|.
name|getValue
argument_list|()
operator|.
name|getValue
argument_list|()
decl_stmt|;
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|e
operator|.
name|getKey
argument_list|()
argument_list|,
name|HConstants
operator|.
name|COL_REGIONINFO
argument_list|)
condition|)
block|{
try|try
block|{
name|sb
operator|.
name|append
argument_list|(
name|Writables
operator|.
name|getHRegionInfo
argument_list|(
name|v
argument_list|)
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|ioe
parameter_list|)
block|{
name|sb
operator|.
name|append
argument_list|(
name|ioe
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
else|else
block|{
name|sb
operator|.
name|append
argument_list|(
name|v
argument_list|)
expr_stmt|;
block|}
name|sb
operator|.
name|append
argument_list|(
literal|")"
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
name|this
operator|.
name|cells
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
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
name|this
operator|.
name|cells
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

