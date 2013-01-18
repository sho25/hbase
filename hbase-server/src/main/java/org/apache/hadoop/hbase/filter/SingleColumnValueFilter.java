begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|classification
operator|.
name|InterfaceAudience
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
name|classification
operator|.
name|InterfaceStability
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
name|DeserializationException
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
name|protobuf
operator|.
name|ProtobufUtil
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
name|protobuf
operator|.
name|generated
operator|.
name|FilterProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|CompareType
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
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|base
operator|.
name|Preconditions
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ByteString
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|InvalidProtocolBufferException
import|;
end_import

begin_comment
comment|/**  * This filter is used to filter cells based on value. It takes a {@link CompareFilter.CompareOp}  * operator (equal, greater, not equal, etc), and either a byte [] value or  * a ByteArrayComparable.  *<p>  * If we have a byte [] value then we just do a lexicographic compare. For  * example, if passed value is 'b' and cell has 'a' and the compare operator  * is LESS, then we will filter out this cell (return true).  If this is not  * sufficient (eg you want to deserialize a long and then compare it to a fixed  * long value), then you can pass in your own comparator instead.  *<p>  * You must also specify a family and qualifier.  Only the value of this column  * will be tested. When using this filter on a {@link Scan} with specified  * inputs, the column to be tested should also be added as input (otherwise  * the filter will regard the column as missing).  *<p>  * To prevent the entire row from being emitted if the column is not found  * on a row, use {@link #setFilterIfMissing}.  * Otherwise, if the column is found, the entire row will be emitted only if  * the value passes.  If the value fails, the row will be filtered out.  *<p>  * In order to test values of previous versions (timestamps), set  * {@link #setLatestVersionOnly} to false. The default is true, meaning that  * only the latest version's value is tested and all previous versions are ignored.  *<p>  * To filter based on the value of all scanned columns, use {@link ValueFilter}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
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
specifier|protected
name|CompareOp
name|compareOp
decl_stmt|;
specifier|protected
name|ByteArrayComparable
name|comparator
decl_stmt|;
specifier|protected
name|boolean
name|foundColumn
init|=
literal|false
decl_stmt|;
specifier|protected
name|boolean
name|matchedColumn
init|=
literal|false
decl_stmt|;
specifier|protected
name|boolean
name|filterIfMissing
init|=
literal|false
decl_stmt|;
specifier|protected
name|boolean
name|latestVersionOnly
init|=
literal|true
decl_stmt|;
comment|/**    * Constructor for binary compare of the value of a single column.  If the    * column is found and the condition passes, all columns of the row will be    * emitted.  If the condition fails, the row will not be emitted.    *<p>    * Use the filterIfColumnMissing flag to set whether the rest of the columns    * in a row will be emitted if the specified column to check is not found in    * the row.    *    * @param family name of column family    * @param qualifier name of column qualifier    * @param compareOp operator    * @param value value to compare column values against    */
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
name|ByteArrayComparable
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
comment|/**    * Constructor for protobuf deserialization only.    * @param family    * @param qualifier    * @param compareOp    * @param comparator    * @param filterIfMissing    * @param latestVersionOnly    */
specifier|protected
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
name|ByteArrayComparable
name|comparator
parameter_list|,
specifier|final
name|boolean
name|filterIfMissing
parameter_list|,
specifier|final
name|boolean
name|latestVersionOnly
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
name|comparator
argument_list|)
expr_stmt|;
name|this
operator|.
name|filterIfMissing
operator|=
name|filterIfMissing
expr_stmt|;
name|this
operator|.
name|latestVersionOnly
operator|=
name|latestVersionOnly
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
name|ByteArrayComparable
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
name|int
name|compareResult
init|=
name|this
operator|.
name|comparator
operator|.
name|compareTo
argument_list|(
name|data
argument_list|,
name|offset
argument_list|,
name|length
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
name|boolean
name|hasFilterRow
parameter_list|()
block|{
return|return
literal|true
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
specifier|static
name|Filter
name|createFilterFromArguments
parameter_list|(
name|ArrayList
argument_list|<
name|byte
index|[]
argument_list|>
name|filterArguments
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|filterArguments
operator|.
name|size
argument_list|()
operator|==
literal|4
operator|||
name|filterArguments
operator|.
name|size
argument_list|()
operator|==
literal|6
argument_list|,
literal|"Expected 4 or 6 but got: %s"
argument_list|,
name|filterArguments
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|family
init|=
name|ParseFilter
operator|.
name|removeQuotesFromByteArray
argument_list|(
name|filterArguments
operator|.
name|get
argument_list|(
literal|0
argument_list|)
argument_list|)
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|ParseFilter
operator|.
name|removeQuotesFromByteArray
argument_list|(
name|filterArguments
operator|.
name|get
argument_list|(
literal|1
argument_list|)
argument_list|)
decl_stmt|;
name|CompareOp
name|compareOp
init|=
name|ParseFilter
operator|.
name|createCompareOp
argument_list|(
name|filterArguments
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
decl_stmt|;
name|ByteArrayComparable
name|comparator
init|=
name|ParseFilter
operator|.
name|createComparator
argument_list|(
name|ParseFilter
operator|.
name|removeQuotesFromByteArray
argument_list|(
name|filterArguments
operator|.
name|get
argument_list|(
literal|3
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
if|if
condition|(
name|comparator
operator|instanceof
name|RegexStringComparator
operator|||
name|comparator
operator|instanceof
name|SubstringComparator
condition|)
block|{
if|if
condition|(
name|compareOp
operator|!=
name|CompareOp
operator|.
name|EQUAL
operator|&&
name|compareOp
operator|!=
name|CompareOp
operator|.
name|NOT_EQUAL
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"A regexstring comparator and substring comparator "
operator|+
literal|"can only be used with EQUAL and NOT_EQUAL"
argument_list|)
throw|;
block|}
block|}
name|SingleColumnValueFilter
name|filter
init|=
operator|new
name|SingleColumnValueFilter
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|compareOp
argument_list|,
name|comparator
argument_list|)
decl_stmt|;
if|if
condition|(
name|filterArguments
operator|.
name|size
argument_list|()
operator|==
literal|6
condition|)
block|{
name|boolean
name|filterIfMissing
init|=
name|ParseFilter
operator|.
name|convertByteArrayToBoolean
argument_list|(
name|filterArguments
operator|.
name|get
argument_list|(
literal|4
argument_list|)
argument_list|)
decl_stmt|;
name|boolean
name|latestVersionOnly
init|=
name|ParseFilter
operator|.
name|convertByteArrayToBoolean
argument_list|(
name|filterArguments
operator|.
name|get
argument_list|(
literal|5
argument_list|)
argument_list|)
decl_stmt|;
name|filter
operator|.
name|setFilterIfMissing
argument_list|(
name|filterIfMissing
argument_list|)
expr_stmt|;
name|filter
operator|.
name|setLatestVersionOnly
argument_list|(
name|latestVersionOnly
argument_list|)
expr_stmt|;
block|}
return|return
name|filter
return|;
block|}
name|FilterProtos
operator|.
name|SingleColumnValueFilter
name|convert
parameter_list|()
block|{
name|FilterProtos
operator|.
name|SingleColumnValueFilter
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|SingleColumnValueFilter
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|columnFamily
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setColumnFamily
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|this
operator|.
name|columnFamily
argument_list|)
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|this
operator|.
name|columnQualifier
operator|!=
literal|null
condition|)
block|{
name|builder
operator|.
name|setColumnQualifier
argument_list|(
name|ByteString
operator|.
name|copyFrom
argument_list|(
name|this
operator|.
name|columnQualifier
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|HBaseProtos
operator|.
name|CompareType
name|compareOp
init|=
name|CompareType
operator|.
name|valueOf
argument_list|(
name|this
operator|.
name|compareOp
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
name|builder
operator|.
name|setCompareOp
argument_list|(
name|compareOp
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setComparator
argument_list|(
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|this
operator|.
name|comparator
argument_list|)
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setFilterIfMissing
argument_list|(
name|this
operator|.
name|filterIfMissing
argument_list|)
expr_stmt|;
name|builder
operator|.
name|setLatestVersionOnly
argument_list|(
name|this
operator|.
name|latestVersionOnly
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
return|;
block|}
comment|/**    * @return The filter serialized using pb    */
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
block|{
return|return
name|convert
argument_list|()
operator|.
name|toByteArray
argument_list|()
return|;
block|}
comment|/**    * @param pbBytes A pb serialized {@link SingleColumnValueFilter} instance    * @return An instance of {@link SingleColumnValueFilter} made from<code>bytes</code>    * @throws DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|SingleColumnValueFilter
name|parseFrom
parameter_list|(
specifier|final
name|byte
index|[]
name|pbBytes
parameter_list|)
throws|throws
name|DeserializationException
block|{
name|FilterProtos
operator|.
name|SingleColumnValueFilter
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|FilterProtos
operator|.
name|SingleColumnValueFilter
operator|.
name|parseFrom
argument_list|(
name|pbBytes
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InvalidProtocolBufferException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|DeserializationException
argument_list|(
name|e
argument_list|)
throw|;
block|}
specifier|final
name|CompareOp
name|compareOp
init|=
name|CompareOp
operator|.
name|valueOf
argument_list|(
name|proto
operator|.
name|getCompareOp
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
specifier|final
name|ByteArrayComparable
name|comparator
decl_stmt|;
try|try
block|{
name|comparator
operator|=
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|proto
operator|.
name|getComparator
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
throw|throw
operator|new
name|DeserializationException
argument_list|(
name|ioe
argument_list|)
throw|;
block|}
return|return
operator|new
name|SingleColumnValueFilter
argument_list|(
name|proto
operator|.
name|hasColumnFamily
argument_list|()
condition|?
name|proto
operator|.
name|getColumnFamily
argument_list|()
operator|.
name|toByteArray
argument_list|()
else|:
literal|null
argument_list|,
name|proto
operator|.
name|hasColumnQualifier
argument_list|()
condition|?
name|proto
operator|.
name|getColumnQualifier
argument_list|()
operator|.
name|toByteArray
argument_list|()
else|:
literal|null
argument_list|,
name|compareOp
argument_list|,
name|comparator
argument_list|,
name|proto
operator|.
name|getFilterIfMissing
argument_list|()
argument_list|,
name|proto
operator|.
name|getLatestVersionOnly
argument_list|()
argument_list|)
return|;
block|}
comment|/**    * @param other    * @return true if and only if the fields of the filter that are serialized    * are equal to the corresponding fields in other.  Used for testing.    */
name|boolean
name|areSerializedFieldsEqual
parameter_list|(
name|Filter
name|o
parameter_list|)
block|{
if|if
condition|(
name|o
operator|==
name|this
condition|)
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|o
operator|instanceof
name|SingleColumnValueFilter
operator|)
condition|)
return|return
literal|false
return|;
name|SingleColumnValueFilter
name|other
init|=
operator|(
name|SingleColumnValueFilter
operator|)
name|o
decl_stmt|;
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|this
operator|.
name|getFamily
argument_list|()
argument_list|,
name|other
operator|.
name|getFamily
argument_list|()
argument_list|)
operator|&&
name|Bytes
operator|.
name|equals
argument_list|(
name|this
operator|.
name|getQualifier
argument_list|()
argument_list|,
name|other
operator|.
name|getQualifier
argument_list|()
argument_list|)
operator|&&
name|this
operator|.
name|compareOp
operator|.
name|equals
argument_list|(
name|other
operator|.
name|compareOp
argument_list|)
operator|&&
name|this
operator|.
name|getComparator
argument_list|()
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|other
operator|.
name|getComparator
argument_list|()
argument_list|)
operator|&&
name|this
operator|.
name|getFilterIfMissing
argument_list|()
operator|==
name|other
operator|.
name|getFilterIfMissing
argument_list|()
operator|&&
name|this
operator|.
name|getLatestVersionOnly
argument_list|()
operator|==
name|other
operator|.
name|getLatestVersionOnly
argument_list|()
return|;
block|}
comment|/**    * The only CF this filter needs is given column family. So, it's the only essential    * column in whole scan. If filterIfMissing == false, all families are essential,    * because of possibility of skipping the rows without any data in filtered CF.    */
specifier|public
name|boolean
name|isFamilyEssential
parameter_list|(
name|byte
index|[]
name|name
parameter_list|)
block|{
return|return
operator|!
name|this
operator|.
name|filterIfMissing
operator|||
name|Bytes
operator|.
name|equals
argument_list|(
name|name
argument_list|,
name|this
operator|.
name|columnFamily
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%s (%s, %s, %s, %s)"
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|columnFamily
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|columnQualifier
argument_list|)
argument_list|,
name|this
operator|.
name|compareOp
operator|.
name|name
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|comparator
operator|.
name|getValue
argument_list|()
argument_list|)
argument_list|)
return|;
block|}
block|}
end_class

end_unit

