begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2006 The Apache Software Foundation  *  * Licensed under the Apache License, Version 2.0 (the "License");  * you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_comment
comment|/*******************************************************************************  * A Key for a stored row  ******************************************************************************/
end_comment

begin_class
specifier|public
class|class
name|HStoreKey
implements|implements
name|WritableComparable
block|{
specifier|public
specifier|static
name|Text
name|extractFamily
parameter_list|(
name|Text
name|col
parameter_list|)
throws|throws
name|IOException
block|{
name|String
name|column
init|=
name|col
operator|.
name|toString
argument_list|()
decl_stmt|;
name|int
name|colpos
init|=
name|column
operator|.
name|indexOf
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
if|if
condition|(
name|colpos
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Illegal column name has no family indicator: "
operator|+
name|column
argument_list|)
throw|;
block|}
return|return
operator|new
name|Text
argument_list|(
name|column
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|colpos
argument_list|)
argument_list|)
return|;
block|}
name|Text
name|row
decl_stmt|;
name|Text
name|column
decl_stmt|;
name|long
name|timestamp
decl_stmt|;
specifier|public
name|HStoreKey
parameter_list|()
block|{
name|this
operator|.
name|row
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
name|this
operator|.
name|column
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
specifier|public
name|HStoreKey
parameter_list|(
name|Text
name|row
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
operator|new
name|Text
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|this
operator|.
name|column
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
specifier|public
name|HStoreKey
parameter_list|(
name|Text
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
operator|new
name|Text
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|this
operator|.
name|column
operator|=
operator|new
name|Text
argument_list|()
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
block|}
specifier|public
name|HStoreKey
parameter_list|(
name|Text
name|row
parameter_list|,
name|Text
name|column
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
operator|new
name|Text
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|this
operator|.
name|column
operator|=
operator|new
name|Text
argument_list|(
name|column
argument_list|)
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|Long
operator|.
name|MAX_VALUE
expr_stmt|;
block|}
specifier|public
name|HStoreKey
parameter_list|(
name|Text
name|row
parameter_list|,
name|Text
name|column
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
operator|new
name|Text
argument_list|(
name|row
argument_list|)
expr_stmt|;
name|this
operator|.
name|column
operator|=
operator|new
name|Text
argument_list|(
name|column
argument_list|)
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
block|}
specifier|public
name|void
name|setRow
parameter_list|(
name|Text
name|newrow
parameter_list|)
block|{
name|this
operator|.
name|row
operator|.
name|set
argument_list|(
name|newrow
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setColumn
parameter_list|(
name|Text
name|newcol
parameter_list|)
block|{
name|this
operator|.
name|column
operator|.
name|set
argument_list|(
name|newcol
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setVersion
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
specifier|public
name|Text
name|getRow
parameter_list|()
block|{
return|return
name|row
return|;
block|}
specifier|public
name|Text
name|getColumn
parameter_list|()
block|{
return|return
name|column
return|;
block|}
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestamp
return|;
block|}
comment|/**    * @param other Key to compare against. Compares row and column.    * @return True if same row and column.    * @see {@link #matchesWithoutColumn(HStoreKey)}    * @see {@link #matchesRowFamily(HStoreKey)}    */
specifier|public
name|boolean
name|matchesRowCol
parameter_list|(
name|HStoreKey
name|other
parameter_list|)
block|{
return|return
name|this
operator|.
name|row
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|row
argument_list|)
operator|==
literal|0
operator|&&
name|this
operator|.
name|column
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|column
argument_list|)
operator|==
literal|0
return|;
block|}
comment|/**    * @param other Key to copmare against. Compares row and timestamp.    *     * @return True if same row and timestamp is greater than<code>other</code>    * @see {@link #matchesRowCol(HStoreKey)}    * @see {@link #matchesRowFamily(HStoreKey)}    */
specifier|public
name|boolean
name|matchesWithoutColumn
parameter_list|(
name|HStoreKey
name|other
parameter_list|)
block|{
return|return
name|this
operator|.
name|row
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|row
argument_list|)
operator|==
literal|0
operator|&&
name|this
operator|.
name|timestamp
operator|>=
name|other
operator|.
name|getTimestamp
argument_list|()
return|;
block|}
comment|/**    * @param other Key to compare against. Compares row and column family    *     * @return true if same row and column family    * @see {@link #matchesRowCol(HStoreKey)}    * @see {@link #matchesWithoutColumn(HStoreKey)}    */
specifier|public
name|boolean
name|matchesRowFamily
parameter_list|(
name|HStoreKey
name|other
parameter_list|)
block|{
name|boolean
name|status
init|=
literal|false
decl_stmt|;
try|try
block|{
name|status
operator|=
name|this
operator|.
name|row
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|row
argument_list|)
operator|==
literal|0
operator|&&
name|extractFamily
argument_list|(
name|this
operator|.
name|column
argument_list|)
operator|.
name|compareTo
argument_list|(
name|extractFamily
argument_list|(
name|other
operator|.
name|getColumn
argument_list|()
argument_list|)
argument_list|)
operator|==
literal|0
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{     }
return|return
name|status
return|;
block|}
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|row
operator|.
name|toString
argument_list|()
operator|+
literal|"/"
operator|+
name|column
operator|.
name|toString
argument_list|()
operator|+
literal|"/"
operator|+
name|timestamp
return|;
block|}
comment|//////////////////////////////////////////////////////////////////////////////
comment|// Comparable
comment|//////////////////////////////////////////////////////////////////////////////
specifier|public
name|int
name|compareTo
parameter_list|(
name|Object
name|o
parameter_list|)
block|{
name|HStoreKey
name|other
init|=
operator|(
name|HStoreKey
operator|)
name|o
decl_stmt|;
name|int
name|result
init|=
name|this
operator|.
name|row
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|row
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|==
literal|0
condition|)
block|{
name|result
operator|=
name|this
operator|.
name|column
operator|.
name|compareTo
argument_list|(
name|other
operator|.
name|column
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
operator|==
literal|0
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|timestamp
operator|<
name|other
operator|.
name|timestamp
condition|)
block|{
name|result
operator|=
literal|1
expr_stmt|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|timestamp
operator|>
name|other
operator|.
name|timestamp
condition|)
block|{
name|result
operator|=
operator|-
literal|1
expr_stmt|;
block|}
block|}
block|}
return|return
name|result
return|;
block|}
comment|//////////////////////////////////////////////////////////////////////////////
comment|// Writable
comment|//////////////////////////////////////////////////////////////////////////////
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
name|row
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|column
operator|.
name|write
argument_list|(
name|out
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeLong
argument_list|(
name|timestamp
argument_list|)
expr_stmt|;
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
name|row
operator|.
name|readFields
argument_list|(
name|in
argument_list|)
expr_stmt|;
name|column
operator|.
name|readFields
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
block|}
block|}
end_class

end_unit

