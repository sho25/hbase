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
name|WritableComparable
import|;
end_import

begin_comment
comment|/**  * A Key for a stored row.  */
end_comment

begin_class
specifier|public
class|class
name|HStoreKey
implements|implements
name|WritableComparable
block|{
comment|/**    * Colon character in UTF-8    */
specifier|public
specifier|static
specifier|final
name|char
name|COLUMN_FAMILY_DELIMITER
init|=
literal|':'
decl_stmt|;
specifier|private
name|byte
index|[]
name|row
init|=
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
decl_stmt|;
specifier|private
name|byte
index|[]
name|column
init|=
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
decl_stmt|;
specifier|private
name|long
name|timestamp
init|=
name|Long
operator|.
name|MAX_VALUE
decl_stmt|;
comment|/*    * regionInfo is only used as a hack to compare HSKs.    * It is not serialized.  See https://issues.apache.org/jira/browse/HBASE-832    */
specifier|private
name|HRegionInfo
name|regionInfo
init|=
literal|null
decl_stmt|;
comment|/** Default constructor used in conjunction with Writable interface */
specifier|public
name|HStoreKey
parameter_list|()
block|{
name|super
argument_list|()
expr_stmt|;
block|}
comment|/**    * Create an HStoreKey specifying only the row    * The column defaults to the empty string, the time stamp defaults to    * Long.MAX_VALUE and the table defaults to empty string    *     * @param row - row key    */
specifier|public
name|HStoreKey
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
name|Long
operator|.
name|MAX_VALUE
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an HStoreKey specifying only the row    * The column defaults to the empty string, the time stamp defaults to    * Long.MAX_VALUE and the table defaults to empty string    *     * @param row - row key    */
specifier|public
name|HStoreKey
parameter_list|(
specifier|final
name|String
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
comment|/**    * Create an HStoreKey specifying the row and timestamp    * The column and table names default to the empty string    *     * @param row row key    * @param timestamp timestamp value    */
specifier|public
name|HStoreKey
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
argument_list|(
name|row
argument_list|,
name|HConstants
operator|.
name|EMPTY_BYTE_ARRAY
argument_list|,
name|timestamp
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an HStoreKey specifying the row and timestamp    * The column and table names default to the empty string    *     * @param row row key    * @param timestamp timestamp value    */
specifier|public
name|HStoreKey
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
name|row
argument_list|,
literal|""
argument_list|,
name|timestamp
argument_list|,
operator|new
name|HRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an HStoreKey specifying the row and column names    * The timestamp defaults to LATEST_TIMESTAMP    * and table name defaults to the empty string    *     * @param row row key    * @param column column key    */
specifier|public
name|HStoreKey
parameter_list|(
specifier|final
name|String
name|row
parameter_list|,
specifier|final
name|String
name|column
parameter_list|)
block|{
name|this
argument_list|(
name|row
argument_list|,
name|column
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
operator|new
name|HRegionInfo
argument_list|()
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an HStoreKey specifying the row and column names    * The timestamp defaults to LATEST_TIMESTAMP    * and table name defaults to the empty string    *     * @param row row key    * @param column column key    */
specifier|public
name|HStoreKey
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|column
parameter_list|)
block|{
name|this
argument_list|(
name|row
argument_list|,
name|column
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an HStoreKey specifying the row, column names and table name    * The timestamp defaults to LATEST_TIMESTAMP    *     * @param row row key    * @param column column key    * @param regionInfo region info    */
specifier|public
name|HStoreKey
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|column
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|)
block|{
name|this
argument_list|(
name|row
argument_list|,
name|column
argument_list|,
name|HConstants
operator|.
name|LATEST_TIMESTAMP
argument_list|,
name|regionInfo
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an HStoreKey specifying all the fields    * Does not make copies of the passed byte arrays. Presumes the passed     * arrays immutable.    * @param row row key    * @param column column key    * @param timestamp timestamp value    * @param regionInfo region info    */
specifier|public
name|HStoreKey
parameter_list|(
specifier|final
name|String
name|row
parameter_list|,
specifier|final
name|String
name|column
parameter_list|,
name|long
name|timestamp
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
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
name|Bytes
operator|.
name|toBytes
argument_list|(
name|column
argument_list|)
argument_list|,
name|timestamp
argument_list|,
name|regionInfo
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an HStoreKey specifying all the fields with unspecified table    * Does not make copies of the passed byte arrays. Presumes the passed     * arrays immutable.    * @param row row key    * @param column column key    * @param timestamp timestamp value    */
specifier|public
name|HStoreKey
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|column
parameter_list|,
name|long
name|timestamp
parameter_list|)
block|{
name|this
argument_list|(
name|row
argument_list|,
name|column
argument_list|,
name|timestamp
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Create an HStoreKey specifying all the fields with specified table    * Does not make copies of the passed byte arrays. Presumes the passed     * arrays immutable.    * @param row row key    * @param column column key    * @param timestamp timestamp value    * @param regionInfo region info    */
specifier|public
name|HStoreKey
parameter_list|(
specifier|final
name|byte
index|[]
name|row
parameter_list|,
specifier|final
name|byte
index|[]
name|column
parameter_list|,
name|long
name|timestamp
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|)
block|{
comment|// Make copies
name|this
operator|.
name|row
operator|=
name|row
expr_stmt|;
name|this
operator|.
name|column
operator|=
name|column
expr_stmt|;
name|this
operator|.
name|timestamp
operator|=
name|timestamp
expr_stmt|;
name|this
operator|.
name|regionInfo
operator|=
name|regionInfo
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
name|length
operator|+
name|this
operator|.
name|column
operator|.
name|length
operator|+
name|Bytes
operator|.
name|SIZEOF_LONG
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
name|byte
index|[]
name|newrow
parameter_list|)
block|{
name|this
operator|.
name|row
operator|=
name|newrow
expr_stmt|;
block|}
comment|/**    * Change the value of the column in this key    *     * @param c new column family value    */
specifier|public
name|void
name|setColumn
parameter_list|(
name|byte
index|[]
name|c
parameter_list|)
block|{
name|this
operator|.
name|column
operator|=
name|c
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
name|byte
index|[]
name|getRow
parameter_list|()
block|{
return|return
name|row
return|;
block|}
comment|/** @return value of column */
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
comment|/** @return value of timestamp */
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
comment|/** @return value of regioninfo */
specifier|public
name|HRegionInfo
name|getHRegionInfo
parameter_list|()
block|{
return|return
name|this
operator|.
name|regionInfo
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
name|Bytes
operator|.
name|equals
argument_list|(
name|this
operator|.
name|row
argument_list|,
name|other
operator|.
name|row
argument_list|)
operator|&&
name|Bytes
operator|.
name|equals
argument_list|(
name|column
argument_list|,
name|other
operator|.
name|column
argument_list|)
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
name|Bytes
operator|.
name|equals
argument_list|(
name|this
operator|.
name|row
argument_list|,
name|other
operator|.
name|row
argument_list|)
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
comment|/**    * Compares the row and column family of two keys    *     * @param that Key to compare against. Compares row and column family    *     * @return true if same row and column family    * @see #matchesRowCol(HStoreKey)    * @see #matchesWithoutColumn(HStoreKey)    */
specifier|public
name|boolean
name|matchesRowFamily
parameter_list|(
name|HStoreKey
name|that
parameter_list|)
block|{
name|int
name|delimiterIndex
init|=
name|getFamilyDelimiterIndex
argument_list|(
name|this
operator|.
name|column
argument_list|)
decl_stmt|;
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|this
operator|.
name|row
argument_list|,
name|that
operator|.
name|row
argument_list|)
operator|&&
name|Bytes
operator|.
name|compareTo
argument_list|(
name|this
operator|.
name|column
argument_list|,
literal|0
argument_list|,
name|delimiterIndex
argument_list|,
name|that
operator|.
name|column
argument_list|,
literal|0
argument_list|,
name|delimiterIndex
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
name|Bytes
operator|.
name|toString
argument_list|(
name|this
operator|.
name|row
argument_list|)
operator|+
literal|"/"
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
name|Bytes
operator|.
name|hashCode
argument_list|(
name|this
operator|.
name|row
argument_list|)
decl_stmt|;
name|result
operator|^=
name|Bytes
operator|.
name|hashCode
argument_list|(
name|this
operator|.
name|column
argument_list|)
expr_stmt|;
name|result
operator|^=
name|this
operator|.
name|timestamp
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
name|compareTwoRowKeys
argument_list|(
name|this
operator|.
name|regionInfo
argument_list|,
name|this
operator|.
name|row
argument_list|,
name|other
operator|.
name|row
argument_list|)
decl_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
block|{
return|return
name|result
return|;
block|}
name|result
operator|=
name|this
operator|.
name|column
operator|==
literal|null
operator|&&
name|other
operator|.
name|column
operator|==
literal|null
condition|?
literal|0
else|:
name|this
operator|.
name|column
operator|==
literal|null
operator|&&
name|other
operator|.
name|column
operator|!=
literal|null
condition|?
operator|-
literal|1
else|:
name|this
operator|.
name|column
operator|!=
literal|null
operator|&&
name|other
operator|.
name|column
operator|==
literal|null
condition|?
literal|1
else|:
name|Bytes
operator|.
name|compareTo
argument_list|(
name|this
operator|.
name|column
argument_list|,
name|other
operator|.
name|column
argument_list|)
expr_stmt|;
if|if
condition|(
name|result
operator|!=
literal|0
condition|)
block|{
return|return
name|result
return|;
block|}
comment|// The below older timestamps sorting ahead of newer timestamps looks
comment|// wrong but it is intentional. This way, newer timestamps are first
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
return|return
name|result
return|;
block|}
comment|/**    * @param column    * @return New byte array that holds<code>column</code> family prefix only    * (Does not include the colon DELIMITER).    * @throws ColumnNameParseException     * @see #parseColumn(byte[])    */
specifier|public
specifier|static
name|byte
index|[]
name|getFamily
parameter_list|(
specifier|final
name|byte
index|[]
name|column
parameter_list|)
throws|throws
name|ColumnNameParseException
block|{
name|int
name|index
init|=
name|getFamilyDelimiterIndex
argument_list|(
name|column
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|ColumnNameParseException
argument_list|(
literal|"No ':' delimiter between "
operator|+
literal|"column family and qualifier in the passed column name<"
operator|+
name|Bytes
operator|.
name|toString
argument_list|(
name|column
argument_list|)
operator|+
literal|">"
argument_list|)
throw|;
block|}
name|byte
index|[]
name|result
init|=
operator|new
name|byte
index|[
name|index
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|column
argument_list|,
literal|0
argument_list|,
name|result
argument_list|,
literal|0
argument_list|,
name|index
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * @param column    * @return Return hash of family portion of passed column.    */
specifier|public
specifier|static
name|Integer
name|getFamilyMapKey
parameter_list|(
specifier|final
name|byte
index|[]
name|column
parameter_list|)
block|{
name|int
name|index
init|=
name|getFamilyDelimiterIndex
argument_list|(
name|column
argument_list|)
decl_stmt|;
comment|// If index< -1, presume passed column is a family name absent colon
comment|// delimiter
return|return
name|Bytes
operator|.
name|mapKey
argument_list|(
name|column
argument_list|,
name|index
operator|>
literal|0
condition|?
name|index
else|:
name|column
operator|.
name|length
argument_list|)
return|;
block|}
comment|/**    * @param family    * @param column    * @return True if<code>column</code> has a family of<code>family</code>.    */
specifier|public
specifier|static
name|boolean
name|matchingFamily
parameter_list|(
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|column
parameter_list|)
block|{
comment|// Make sure index of the ':' is at same offset.
name|int
name|index
init|=
name|getFamilyDelimiterIndex
argument_list|(
name|column
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|!=
name|family
operator|.
name|length
condition|)
block|{
return|return
literal|false
return|;
block|}
return|return
name|Bytes
operator|.
name|compareTo
argument_list|(
name|family
argument_list|,
literal|0
argument_list|,
name|index
argument_list|,
name|column
argument_list|,
literal|0
argument_list|,
name|index
argument_list|)
operator|==
literal|0
return|;
block|}
comment|/**    * @param family    * @return Return<code>family</code> plus the family delimiter.    */
specifier|public
specifier|static
name|byte
index|[]
name|addDelimiter
parameter_list|(
specifier|final
name|byte
index|[]
name|family
parameter_list|)
block|{
comment|// Manufacture key by adding delimiter to the passed in colFamily.
name|byte
index|[]
name|familyPlusDelimiter
init|=
operator|new
name|byte
index|[
name|family
operator|.
name|length
operator|+
literal|1
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|family
argument_list|,
literal|0
argument_list|,
name|familyPlusDelimiter
argument_list|,
literal|0
argument_list|,
name|family
operator|.
name|length
argument_list|)
expr_stmt|;
name|familyPlusDelimiter
index|[
name|family
operator|.
name|length
index|]
operator|=
name|HStoreKey
operator|.
name|COLUMN_FAMILY_DELIMITER
expr_stmt|;
return|return
name|familyPlusDelimiter
return|;
block|}
comment|/**    * @param column    * @return New byte array that holds<code>column</code> qualifier suffix.    * @see #parseColumn(byte[])    */
specifier|public
specifier|static
name|byte
index|[]
name|getQualifier
parameter_list|(
specifier|final
name|byte
index|[]
name|column
parameter_list|)
block|{
name|int
name|index
init|=
name|getFamilyDelimiterIndex
argument_list|(
name|column
argument_list|)
decl_stmt|;
name|int
name|len
init|=
name|column
operator|.
name|length
operator|-
operator|(
name|index
operator|+
literal|1
operator|)
decl_stmt|;
name|byte
index|[]
name|result
init|=
operator|new
name|byte
index|[
name|len
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|column
argument_list|,
name|index
operator|+
literal|1
argument_list|,
name|result
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * @param c Column name    * @return Return array of size two whose first element has the family    * prefix of passed column<code>c</code> and whose second element is the    * column qualifier.    * @throws ColumnNameParseException     */
specifier|public
specifier|static
name|byte
index|[]
index|[]
name|parseColumn
parameter_list|(
specifier|final
name|byte
index|[]
name|c
parameter_list|)
throws|throws
name|ColumnNameParseException
block|{
name|byte
index|[]
index|[]
name|result
init|=
operator|new
name|byte
index|[
literal|2
index|]
index|[]
decl_stmt|;
name|int
name|index
init|=
name|getFamilyDelimiterIndex
argument_list|(
name|c
argument_list|)
decl_stmt|;
if|if
condition|(
name|index
operator|==
operator|-
literal|1
condition|)
block|{
throw|throw
operator|new
name|ColumnNameParseException
argument_list|(
literal|"Impossible column name: "
operator|+
name|c
argument_list|)
throw|;
block|}
name|result
index|[
literal|0
index|]
operator|=
operator|new
name|byte
index|[
name|index
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|c
argument_list|,
literal|0
argument_list|,
name|result
index|[
literal|0
index|]
argument_list|,
literal|0
argument_list|,
name|index
argument_list|)
expr_stmt|;
name|int
name|len
init|=
name|c
operator|.
name|length
operator|-
operator|(
name|index
operator|+
literal|1
operator|)
decl_stmt|;
name|result
index|[
literal|1
index|]
operator|=
operator|new
name|byte
index|[
name|len
index|]
expr_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|c
argument_list|,
name|index
operator|+
literal|1
comment|/*Skip delimiter*/
argument_list|,
name|result
index|[
literal|1
index|]
argument_list|,
literal|0
argument_list|,
name|len
argument_list|)
expr_stmt|;
return|return
name|result
return|;
block|}
comment|/**    * @param b    * @return Index of the family-qualifier colon delimiter character in passed    * buffer.    */
specifier|public
specifier|static
name|int
name|getFamilyDelimiterIndex
parameter_list|(
specifier|final
name|byte
index|[]
name|b
parameter_list|)
block|{
if|if
condition|(
name|b
operator|==
literal|null
condition|)
block|{
throw|throw
operator|new
name|NullPointerException
argument_list|()
throw|;
block|}
name|int
name|result
init|=
operator|-
literal|1
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
name|b
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|b
index|[
name|i
index|]
operator|==
name|COLUMN_FAMILY_DELIMITER
condition|)
block|{
name|result
operator|=
name|i
expr_stmt|;
break|break;
block|}
block|}
return|return
name|result
return|;
block|}
comment|/**    * Returns row and column bytes out of an HStoreKey.    * @param hsk Store key.    * @return byte array encoding of HStoreKey    */
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
block|{
return|return
name|Bytes
operator|.
name|add
argument_list|(
name|hsk
operator|.
name|getRow
argument_list|()
argument_list|,
name|hsk
operator|.
name|getColumn
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * Utility method to compare two row keys.    * This is required because of the meta delimiters.    * This is a hack.    * @param regionInfo    * @param rowA    * @param rowB    * @return value of the comparison    */
specifier|public
specifier|static
name|int
name|compareTwoRowKeys
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|byte
index|[]
name|rowA
parameter_list|,
name|byte
index|[]
name|rowB
parameter_list|)
block|{
if|if
condition|(
name|regionInfo
operator|!=
literal|null
operator|&&
operator|(
name|regionInfo
operator|.
name|isMetaRegion
argument_list|()
operator|||
name|regionInfo
operator|.
name|isRootRegion
argument_list|()
operator|)
condition|)
block|{
name|byte
index|[]
index|[]
name|keysA
init|=
name|stripStartKeyMeta
argument_list|(
name|rowA
argument_list|)
decl_stmt|;
name|byte
index|[]
index|[]
name|KeysB
init|=
name|stripStartKeyMeta
argument_list|(
name|rowB
argument_list|)
decl_stmt|;
name|int
name|rowCompare
init|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|keysA
index|[
literal|0
index|]
argument_list|,
name|KeysB
index|[
literal|0
index|]
argument_list|)
decl_stmt|;
if|if
condition|(
name|rowCompare
operator|==
literal|0
condition|)
name|rowCompare
operator|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|keysA
index|[
literal|1
index|]
argument_list|,
name|KeysB
index|[
literal|1
index|]
argument_list|)
expr_stmt|;
return|return
name|rowCompare
return|;
block|}
else|else
block|{
return|return
name|Bytes
operator|.
name|compareTo
argument_list|(
name|rowA
argument_list|,
name|rowB
argument_list|)
return|;
block|}
block|}
comment|/**    * Utility method to check if two row keys are equal.    * This is required because of the meta delimiters    * This is a hack    * @param regionInfo    * @param rowA    * @param rowB    * @return if it's equal    */
specifier|public
specifier|static
name|boolean
name|equalsTwoRowKeys
parameter_list|(
name|HRegionInfo
name|regionInfo
parameter_list|,
name|byte
index|[]
name|rowA
parameter_list|,
name|byte
index|[]
name|rowB
parameter_list|)
block|{
return|return
name|rowA
operator|==
literal|null
operator|&&
name|rowB
operator|==
literal|null
condition|?
literal|true
else|:
name|rowA
operator|==
literal|null
operator|&&
name|rowB
operator|!=
literal|null
condition|?
literal|false
else|:
name|rowA
operator|!=
literal|null
operator|&&
name|rowB
operator|==
literal|null
condition|?
literal|false
else|:
name|rowA
operator|.
name|length
operator|!=
name|rowB
operator|.
name|length
condition|?
literal|false
else|:
name|compareTwoRowKeys
argument_list|(
name|regionInfo
argument_list|,
name|rowA
argument_list|,
name|rowB
argument_list|)
operator|==
literal|0
return|;
block|}
specifier|private
specifier|static
name|byte
index|[]
index|[]
name|stripStartKeyMeta
parameter_list|(
name|byte
index|[]
name|rowKey
parameter_list|)
block|{
name|int
name|offset
init|=
operator|-
literal|1
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
name|rowKey
operator|.
name|length
operator|-
literal|1
init|;
name|i
operator|>
literal|0
condition|;
name|i
operator|--
control|)
block|{
if|if
condition|(
name|rowKey
index|[
name|i
index|]
operator|==
name|HConstants
operator|.
name|META_ROW_DELIMITER
condition|)
block|{
name|offset
operator|=
name|i
expr_stmt|;
break|break;
block|}
block|}
name|byte
index|[]
name|row
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
name|rowKey
argument_list|,
literal|0
argument_list|,
name|row
argument_list|,
literal|0
argument_list|,
name|offset
argument_list|)
expr_stmt|;
name|byte
index|[]
name|timestamp
init|=
operator|new
name|byte
index|[
name|rowKey
operator|.
name|length
operator|-
name|offset
operator|-
literal|1
index|]
decl_stmt|;
name|System
operator|.
name|arraycopy
argument_list|(
name|rowKey
argument_list|,
name|offset
operator|+
literal|1
argument_list|,
name|timestamp
argument_list|,
literal|0
argument_list|,
name|rowKey
operator|.
name|length
operator|-
name|offset
operator|-
literal|1
argument_list|)
expr_stmt|;
name|byte
index|[]
index|[]
name|elements
init|=
operator|new
name|byte
index|[
literal|2
index|]
index|[]
decl_stmt|;
name|elements
index|[
literal|0
index|]
operator|=
name|row
expr_stmt|;
name|elements
index|[
literal|1
index|]
operator|=
name|timestamp
expr_stmt|;
return|return
name|elements
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
name|row
argument_list|)
expr_stmt|;
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

