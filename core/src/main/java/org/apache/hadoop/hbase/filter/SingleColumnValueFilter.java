begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|filter
package|;
end_package

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
name|client
operator|.
name|Scan
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
name|filter
operator|.
name|CompareFilter
operator|.
name|CompareOp
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
name|HbaseObjectWritable
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
name|Arrays
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_comment
comment|/**  * This filter is used to filter cells based on value. It takes a {@link CompareFilter.CompareOp}  * operator (equal, greater, not equal, etc), and either a byte [] value or  * a WritableByteArrayComparable.  *<p>  * If we have a byte [] value then we just do a lexicographic compare. For  * example, if passed value is 'b' and cell has 'a' and the compare operator  * is LESS, then we will filter out this cell (return true).  If this is not  * sufficient (eg you want to deserialize a long and then compare it to a fixed  * long value), then you can pass in your own comparator instead.  *<p>  * You must also specify a family and qualifier.  Only the value of this column  * will be tested. When using this filter on a {@link Scan} with specified  * inputs, the column to be tested should also be added as input (otherwise  * the filter will regard the column as missing).  *<p>  * To prevent the entire row from being emitted if the column is not found  * on a row, use {@link #setFilterIfMissing}.  * Otherwise, if the column is found, the entire row will be emitted only if  * the value passes.  If the value fails, the row will be filtered out.  *<p>  * In order to test values of previous versions (timestamps), set  * {@link #setLatestVersionOnly} to false. The default is true, meaning that  * only the latest version's value is tested and all previous versions are ignored.  *<p>  * To filter based on the value of all scanned columns, use {@link ValueFilter}.  */
end_comment

begin_class
specifier|public
class|class
name|SingleColumnValueFilter
extends|extends
name|FilterBase
block|{
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|SingleColumnValueFilter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
name|byte
index|[]
name|columnFamily
decl_stmt|;
specifier|protected
name|byte
index|[]
name|columnQualifier
decl_stmt|;
specifier|private
name|CompareOp
name|compareOp
decl_stmt|;
specifier|private
name|WritableByteArrayComparable
name|comparator
decl_stmt|;
specifier|private
name|boolean
name|foundColumn
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|matchedColumn
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|filterIfMissing
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|latestVersionOnly
init|=
literal|true
decl_stmt|;
comment|/**    * Writable constructor, do not use.    */
specifier|public
name|SingleColumnValueFilter
parameter_list|()
block|{   }
comment|/**    * Constructor for binary compare of the value of a single column.  If the    * column is found and the condition passes, all columns of the row will be    * emitted.  If the column is not found or the condition fails, the row will    * not be emitted.    *    * @param family name of column family    * @param qualifier name of column qualifier    * @param compareOp operator    * @param value value to compare column values against    */
specifier|public
name|SingleColumnValueFilter
parameter_list|(
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|CompareOp
name|compareOp
parameter_list|,
specifier|final
name|byte
index|[]
name|value
parameter_list|)
block|{
name|this
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|compareOp
argument_list|,
operator|new
name|BinaryComparator
argument_list|(
name|value
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor for binary compare of the value of a single column.  If the    * column is found and the condition passes, all columns of the row will be    * emitted.  If the condition fails, the row will not be emitted.    *<p>    * Use the filterIfColumnMissing flag to set whether the rest of the columns    * in a row will be emitted if the specified column to check is not found in    * the row.    *    * @param family name of column family    * @param qualifier name of column qualifier    * @param compareOp operator    * @param comparator Comparator to use.    */
specifier|public
name|SingleColumnValueFilter
parameter_list|(
specifier|final
name|byte
index|[]
name|family
parameter_list|,
specifier|final
name|byte
index|[]
name|qualifier
parameter_list|,
specifier|final
name|CompareOp
name|compareOp
parameter_list|,
specifier|final
name|WritableByteArrayComparable
name|comparator
parameter_list|)
block|{
name|this
operator|.
name|columnFamily
operator|=
name|family
expr_stmt|;
name|this
operator|.
name|columnQualifier
operator|=
name|qualifier
expr_stmt|;
name|this
operator|.
name|compareOp
operator|=
name|compareOp
expr_stmt|;
name|this
operator|.
name|comparator
operator|=
name|comparator
expr_stmt|;
block|}
comment|/**    * @return operator    */
specifier|public
name|CompareOp
name|getOperator
parameter_list|()
block|{
return|return
name|compareOp
return|;
block|}
comment|/**    * @return the comparator    */
specifier|public
name|WritableByteArrayComparable
name|getComparator
parameter_list|()
block|{
return|return
name|comparator
return|;
block|}
comment|/**    * @return the family    */
specifier|public
name|byte
index|[]
name|getFamily
parameter_list|()
block|{
return|return
name|columnFamily
return|;
block|}
comment|/**    * @return the qualifier    */
specifier|public
name|byte
index|[]
name|getQualifier
parameter_list|()
block|{
return|return
name|columnQualifier
return|;
block|}
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
name|KeyValue
name|keyValue
parameter_list|)
block|{
comment|// System.out.println("REMOVE KEY=" + keyValue.toString() + ", value=" + Bytes.toString(keyValue.getValue()));
if|if
condition|(
name|this
operator|.
name|matchedColumn
condition|)
block|{
comment|// We already found and matched the single column, all keys now pass
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
elseif|else
if|if
condition|(
name|this
operator|.
name|latestVersionOnly
operator|&&
name|this
operator|.
name|foundColumn
condition|)
block|{
comment|// We found but did not match the single column, skip to next row
return|return
name|ReturnCode
operator|.
name|NEXT_ROW
return|;
block|}
if|if
condition|(
operator|!
name|keyValue
operator|.
name|matchingColumn
argument_list|(
name|this
operator|.
name|columnFamily
argument_list|,
name|this
operator|.
name|columnQualifier
argument_list|)
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
name|foundColumn
operator|=
literal|true
expr_stmt|;
if|if
condition|(
name|filterColumnValue
argument_list|(
name|keyValue
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|keyValue
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|keyValue
operator|.
name|getValueLength
argument_list|()
argument_list|)
condition|)
block|{
return|return
name|this
operator|.
name|latestVersionOnly
condition|?
name|ReturnCode
operator|.
name|NEXT_ROW
else|:
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
name|this
operator|.
name|matchedColumn
operator|=
literal|true
expr_stmt|;
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
specifier|private
name|boolean
name|filterColumnValue
parameter_list|(
specifier|final
name|byte
index|[]
name|data
parameter_list|,
specifier|final
name|int
name|offset
parameter_list|,
specifier|final
name|int
name|length
parameter_list|)
block|{
comment|// TODO: Can this filter take a rawcomparator so don't have to make this
comment|// byte array copy?
name|int
name|compareResult
init|=
name|this
operator|.
name|comparator
operator|.
name|compareTo
argument_list|(
name|Arrays
operator|.
name|copyOfRange
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|offset
operator|+
name|length
argument_list|)
argument_list|)
decl_stmt|;
switch|switch
condition|(
name|this
operator|.
name|compareOp
condition|)
block|{
case|case
name|LESS
case|:
return|return
name|compareResult
operator|<=
literal|0
return|;
case|case
name|LESS_OR_EQUAL
case|:
return|return
name|compareResult
operator|<
literal|0
return|;
case|case
name|EQUAL
case|:
return|return
name|compareResult
operator|!=
literal|0
return|;
case|case
name|NOT_EQUAL
case|:
return|return
name|compareResult
operator|==
literal|0
return|;
case|case
name|GREATER_OR_EQUAL
case|:
return|return
name|compareResult
operator|>
literal|0
return|;
case|case
name|GREATER
case|:
return|return
name|compareResult
operator|>=
literal|0
return|;
default|default:
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Unknown Compare op "
operator|+
name|compareOp
operator|.
name|name
argument_list|()
argument_list|)
throw|;
block|}
block|}
specifier|public
name|boolean
name|filterRow
parameter_list|()
block|{
comment|// If column was found, return false if it was matched, true if it was not
comment|// If column not found, return true if we filter if missing, false if not
return|return
name|this
operator|.
name|foundColumn
condition|?
operator|!
name|this
operator|.
name|matchedColumn
else|:
name|this
operator|.
name|filterIfMissing
return|;
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|foundColumn
operator|=
literal|false
expr_stmt|;
name|matchedColumn
operator|=
literal|false
expr_stmt|;
block|}
comment|/**    * Get whether entire row should be filtered if column is not found.    * @return true if row should be skipped if column not found, false if row    * should be let through anyways    */
specifier|public
name|boolean
name|getFilterIfMissing
parameter_list|()
block|{
return|return
name|filterIfMissing
return|;
block|}
comment|/**    * Set whether entire row should be filtered if column is not found.    *<p>    * If true, the entire row will be skipped if the column is not found.    *<p>    * If false, the row will pass if the column is not found.  This is default.    * @param filterIfMissing flag    */
specifier|public
name|void
name|setFilterIfMissing
parameter_list|(
name|boolean
name|filterIfMissing
parameter_list|)
block|{
name|this
operator|.
name|filterIfMissing
operator|=
name|filterIfMissing
expr_stmt|;
block|}
comment|/**    * Get whether only the latest version of the column value should be compared.    * If true, the row will be returned if only the latest version of the column    * value matches. If false, the row will be returned if any version of the    * column value matches. The default is true.    * @return return value    */
specifier|public
name|boolean
name|getLatestVersionOnly
parameter_list|()
block|{
return|return
name|latestVersionOnly
return|;
block|}
comment|/**    * Set whether only the latest version of the column value should be compared.    * If true, the row will be returned if only the latest version of the column    * value matches. If false, the row will be returned if any version of the    * column value matches. The default is true.    * @param latestVersionOnly flag    */
specifier|public
name|void
name|setLatestVersionOnly
parameter_list|(
name|boolean
name|latestVersionOnly
parameter_list|)
block|{
name|this
operator|.
name|latestVersionOnly
operator|=
name|latestVersionOnly
expr_stmt|;
block|}
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
name|columnFamily
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|columnFamily
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|this
operator|.
name|columnFamily
operator|=
literal|null
expr_stmt|;
block|}
name|this
operator|.
name|columnQualifier
operator|=
name|Bytes
operator|.
name|readByteArray
argument_list|(
name|in
argument_list|)
expr_stmt|;
if|if
condition|(
name|this
operator|.
name|columnQualifier
operator|.
name|length
operator|==
literal|0
condition|)
block|{
name|this
operator|.
name|columnQualifier
operator|=
literal|null
expr_stmt|;
block|}
name|this
operator|.
name|compareOp
operator|=
name|CompareOp
operator|.
name|valueOf
argument_list|(
name|in
operator|.
name|readUTF
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|comparator
operator|=
operator|(
name|WritableByteArrayComparable
operator|)
name|HbaseObjectWritable
operator|.
name|readObject
argument_list|(
name|in
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|this
operator|.
name|foundColumn
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|this
operator|.
name|matchedColumn
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|this
operator|.
name|filterIfMissing
operator|=
name|in
operator|.
name|readBoolean
argument_list|()
expr_stmt|;
name|this
operator|.
name|latestVersionOnly
operator|=
name|in
operator|.
name|readBoolean
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
name|columnFamily
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
name|columnQualifier
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeUTF
argument_list|(
name|compareOp
operator|.
name|name
argument_list|()
argument_list|)
expr_stmt|;
name|HbaseObjectWritable
operator|.
name|writeObject
argument_list|(
name|out
argument_list|,
name|comparator
argument_list|,
name|WritableByteArrayComparable
operator|.
name|class
argument_list|,
literal|null
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|foundColumn
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|matchedColumn
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|filterIfMissing
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|latestVersionOnly
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

