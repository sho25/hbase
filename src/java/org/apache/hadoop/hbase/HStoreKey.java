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
comment|/**  * A Key for a stored row  */
end_comment

begin_class
specifier|public
class|class
name|HStoreKey
implements|implements
name|WritableComparable
block|{
comment|// TODO: Move these utility methods elsewhere (To a Column class?).
comment|/**    * Extracts the column family name from a column    * For example, returns 'info' if the specified column was 'info:server'    * @param col name of column    * @return column family name    * @throws InvalidColumnNameException     */
specifier|public
specifier|static
name|Text
name|extractFamily
parameter_list|(
specifier|final
name|Text
name|col
parameter_list|)
throws|throws
name|InvalidColumnNameException
block|{
return|return
name|extractFamily
argument_list|(
name|col
argument_list|,
literal|false
argument_list|)
return|;
block|}
comment|/**    * Extracts the column family name from a column    * For example, returns 'info' if the specified column was 'info:server'    * @param col name of column    * @param withColon if returned family name should include the ':' suffix.    * @return column family name    * @throws InvalidColumnNameException     */
specifier|public
specifier|static
name|Text
name|extractFamily
parameter_list|(
specifier|final
name|Text
name|col
parameter_list|,
specifier|final
name|boolean
name|withColon
parameter_list|)
throws|throws
name|InvalidColumnNameException
block|{
name|int
name|offset
init|=
name|getColonOffset
argument_list|(
name|col
argument_list|)
decl_stmt|;
comment|// Include ':' in copy?
name|offset
operator|+=
operator|(
name|withColon
operator|)
condition|?
literal|1
else|:
literal|0
expr_stmt|;
if|if
condition|(
name|offset
operator|==
name|col
operator|.
name|getLength
argument_list|()
condition|)
block|{
return|return
name|col
return|;
block|}
name|byte
index|[]
name|buffer
init|=
operator|new
name|byte
index|[
name|offset
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|col
operator|.
name|getBytes
argument_list|()
argument_list|,
literal|0
argument_list|,
name|buffer
argument_list|,
literal|0
argument_list|,
name|offset
argument_list|)
expr_stmt|;
return|return
operator|new
name|Text
argument_list|(
name|buffer
argument_list|)
return|;
block|}
comment|/**    * Extracts the column qualifier, the portion that follows the colon (':')    * family/qualifier separator.    * For example, returns 'server' if the specified column was 'info:server'    * @param col name of column    * @return column qualifier or null if there is no qualifier.    * @throws InvalidColumnNameException     */
specifier|public
specifier|static
name|Text
name|extractQualifier
parameter_list|(
specifier|final
name|Text
name|col
parameter_list|)
throws|throws
name|InvalidColumnNameException
block|{
name|int
name|offset
init|=
name|getColonOffset
argument_list|(
name|col
argument_list|)
decl_stmt|;
if|if
condition|(
name|offset
operator|+
literal|1
operator|==
name|col
operator|.
name|getLength
argument_list|()
condition|)
block|{
return|return
literal|null
return|;
block|}
name|int
name|bufferLength
init|=
name|col
operator|.
name|getLength
argument_list|()
operator|-
operator|(
name|offset
operator|+
literal|1
operator|)
decl_stmt|;
name|byte
index|[]
name|buffer
init|=
operator|new
name|byte
index|[
name|bufferLength
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|col
operator|.
name|getBytes
argument_list|()
argument_list|,
name|offset
operator|+
literal|1
argument_list|,
name|buffer
argument_list|,
literal|0
argument_list|,
name|bufferLength
argument_list|)
expr_stmt|;
return|return
operator|new
name|Text
argument_list|(
name|buffer
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|int
name|getColonOffset
parameter_list|(
specifier|final
name|Text
name|col
parameter_list|)
throws|throws
name|InvalidColumnNameException
block|{
name|int
name|offset
init|=
name|col
operator|.
name|find
argument_list|(
literal|":"
argument_list|)
decl_stmt|;
if|if
condition|(
name|offset
operator|<
literal|0
condition|)
block|{
throw|throw
operator|new
name|InvalidColumnNameException
argument_list|(
name|col
operator|+
literal|" is missing the colon "
operator|+
literal|"family/qualifier separator"
argument_list|)
throw|;
block|}
return|return
name|offset
return|;
block|}
comment|/**    * Returns row and column bytes out of an HStoreKey.    * @param hsk Store key.    * @return byte array encoding of HStoreKey    * @throws UnsupportedEncodingException    */
specifier|public
specifier|static
name|byte
index|[]
name|getBytes
parameter_list|(
specifier|final
name|HStoreKey
name|hsk
parameter_list|)
throws|throws
name|UnsupportedEncodingException
block|{
name|StringBuilder
name|s
init|=
operator|new
name|StringBuilder
argument_list|(
name|hsk
operator|.
name|getRow
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
decl_stmt|;
name|s
operator|.
name|append
argument_list|(
name|hsk
operator|.
name|getColumn
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|s
operator|.
name|toString
argument_list|()
operator|.
name|getBytes
argument_list|(
name|HConstants
operator|.
name|UTF8_ENCODING
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
comment|/** Default constructor used in conjunction with Writable interface */
specifier|public
name|HStoreKey
parameter_list|()
block|{
name|this
argument_list|(
operator|new
name|Text
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an HStoreKey specifying only the row    * The column defaults to the empty string and the time stamp defaults to    * Long.MAX_VALUE    *     * @param row - row key    */
specifier|public
name|HStoreKey
parameter_list|(
name|Text
name|row
parameter_list|)
block|{
name|this
argument_list|(
name|row
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an HStoreKey specifying the row and timestamp    * The column name defaults to the empty string    *     * @param row row key    * @param timestamp timestamp value    */
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
argument_list|(
name|row
argument_list|,
operator|new
name|Text
argument_list|()
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an HStoreKey specifying the row and column names    * The timestamp defaults to Long.MAX_VALUE    *     * @param row row key    * @param column column key    */
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
argument_list|(
name|row
argument_list|,
name|column
argument_list|,
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an HStoreKey specifying all the fields    *     * @param row row key    * @param column column key    * @param timestamp timestamp value    */
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
comment|/** @return Approximate size in bytes of this key. */
specifier|public
name|long
name|getSize
parameter_list|()
block|{
return|return
name|this
operator|.
name|row
operator|.
name|getLength
argument_list|()
operator|+
name|this
operator|.
name|column
operator|.
name|getLength
argument_list|()
operator|+
literal|8
comment|/* There is no sizeof in java. Presume long is 8 (64bit machine)*/
return|;
block|}
comment|/**    * Constructs a new HStoreKey from another    *     * @param other the source key    */
specifier|public
name|HStoreKey
parameter_list|(
name|HStoreKey
name|other
parameter_list|)
block|{
name|this
argument_list|(
name|other
operator|.
name|row
argument_list|,
name|other
operator|.
name|column
argument_list|,
name|other
operator|.
name|timestamp
argument_list|)
expr_stmt|;
block|}
comment|/**    * Change the value of the row key    *     * @param newrow new row key value    */
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
comment|/**    * Change the value of the column key    *     * @param newcol new column key value    */
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
comment|/**    * Change the value of the timestamp field    *     * @param timestamp new timestamp value    */
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
comment|/**    * Set the value of this HStoreKey from the supplied key    *     * @param k key value to copy    */
specifier|public
name|void
name|set
parameter_list|(
name|HStoreKey
name|k
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|k
operator|.
name|getRow
argument_list|()
expr_stmt|;
name|this
operator|.
name|column
operator|=
name|k
operator|.
name|getColumn
argument_list|()
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|k
operator|.
name|getTimestamp
argument_list|()
expr_stmt|;
block|}
comment|/** @return value of row key */
specifier|public
name|Text
name|getRow
parameter_list|()
block|{
return|return
name|row
return|;
block|}
comment|/** @return value of column key */
specifier|public
name|Text
name|getColumn
parameter_list|()
block|{
return|return
name|column
return|;
block|}
comment|/** @return value of timestamp */
specifier|public
name|long
name|getTimestamp
parameter_list|()
block|{
return|return
name|timestamp
return|;
block|}
comment|/**    * Compares the row and column of two keys    * @param other Key to compare against. Compares row and column.    * @return True if same row and column.    * @see #matchesWithoutColumn(HStoreKey)    * @see #matchesRowFamily(HStoreKey)    */
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
comment|/**    * Compares the row and timestamp of two keys    *     * @param other Key to copmare against. Compares row and timestamp.    *     * @return True if same row and timestamp is greater than<code>other</code>    * @see #matchesRowCol(HStoreKey)    * @see #matchesRowFamily(HStoreKey)    */
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
comment|/**    * Compares the row and column family of two keys    *     * @param that Key to compare against. Compares row and column family    *     * @return true if same row and column family    * @throws InvalidColumnNameException     * @see #matchesRowCol(HStoreKey)    * @see #matchesWithoutColumn(HStoreKey)    */
specifier|public
name|boolean
name|matchesRowFamily
parameter_list|(
name|HStoreKey
name|that
parameter_list|)
throws|throws
name|InvalidColumnNameException
block|{
return|return
name|this
operator|.
name|row
operator|.
name|compareTo
argument_list|(
name|that
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
name|that
operator|.
name|getColumn
argument_list|()
argument_list|)
argument_list|)
operator|==
literal|0
return|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
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
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
name|Object
name|obj
parameter_list|)
block|{
return|return
name|compareTo
argument_list|(
name|obj
argument_list|)
operator|==
literal|0
return|;
block|}
comment|/** {@inheritDoc} */
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
name|int
name|result
init|=
name|this
operator|.
name|row
operator|.
name|hashCode
argument_list|()
decl_stmt|;
name|result
operator|^=
name|this
operator|.
name|column
operator|.
name|hashCode
argument_list|()
expr_stmt|;
name|result
operator|^=
name|Long
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|timestamp
argument_list|)
operator|.
name|hashCode
argument_list|()
expr_stmt|;
return|return
name|result
return|;
block|}
comment|// Comparable
comment|/** {@inheritDoc} */
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
comment|// The below older timestamps sorting ahead of newer timestamps looks
comment|// wrong but it is intentional.  This way, newer timestamps are first
comment|// found when we iterate over a memcache and newer versions are the
comment|// first we trip over when reading from a store file.
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

