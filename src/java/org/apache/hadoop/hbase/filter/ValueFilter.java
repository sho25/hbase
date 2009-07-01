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
name|filter
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
name|Arrays
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
name|HBaseConfiguration
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
name|ObjectWritable
import|;
end_import

begin_comment
comment|/**  * This filter is used to filter based on the value of a given column. It takes  * an operator (equal, greater, not equal, etc) and either a byte [] value or a  * byte [] comparator. If we have a byte [] value then we just do a  * lexicographic compare. For example, if passed value is 'b' and cell has 'a'  * and the compare operator is LESS, then we will filter out this cell (return  * true).  If this is not sufficient (eg you want to deserialize  * a long and then compare it to a fixed long value), then you can pass in your  * own comparator instead.  * */
end_comment

begin_class
specifier|public
class|class
name|ValueFilter
implements|implements
name|Filter
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
name|ValueFilter
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/** Comparison operators. */
specifier|public
enum|enum
name|CompareOp
block|{
comment|/** less than */
name|LESS
block|,
comment|/** less than or equal to */
name|LESS_OR_EQUAL
block|,
comment|/** equals */
name|EQUAL
block|,
comment|/** not equal */
name|NOT_EQUAL
block|,
comment|/** greater than or equal to */
name|GREATER_OR_EQUAL
block|,
comment|/** greater than */
name|GREATER
block|;   }
specifier|private
name|byte
index|[]
name|columnFamily
decl_stmt|;
specifier|private
name|byte
index|[]
name|columnQualifier
decl_stmt|;
specifier|private
name|CompareOp
name|compareOp
decl_stmt|;
specifier|private
name|byte
index|[]
name|value
decl_stmt|;
specifier|private
name|WritableByteArrayComparable
name|comparator
decl_stmt|;
specifier|private
name|boolean
name|filterIfColumnMissing
decl_stmt|;
specifier|private
name|boolean
name|filterThisRow
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|foundColValue
init|=
literal|false
decl_stmt|;
name|ValueFilter
parameter_list|()
block|{
comment|// for Writable
block|}
comment|/**    * Constructor.    *     * @param family name of column family    * @param qualifier name of column qualifier    * @param compareOp operator    * @param value value to compare column values against    */
specifier|public
name|ValueFilter
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
name|value
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor.    *     * @param family name of column family    * @param qualifier name of column qualifier    * @param compareOp operator    * @param value value to compare column values against    * @param filterIfColumnMissing if true then we will filter rows that don't    * have the column.    */
specifier|public
name|ValueFilter
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
parameter_list|,
name|boolean
name|filterIfColumnMissing
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
name|value
operator|=
name|value
expr_stmt|;
name|this
operator|.
name|filterIfColumnMissing
operator|=
name|filterIfColumnMissing
expr_stmt|;
block|}
comment|/**    * Constructor.    *     * @param family name of column family    * @param qualifier name of column qualifier    * @param compareOp operator    * @param comparator Comparator to use.    */
specifier|public
name|ValueFilter
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
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|compareOp
argument_list|,
name|comparator
argument_list|,
literal|true
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor.    *     * @param family name of column family    * @param qualifier name of column qualifier    * @param compareOp operator    * @param comparator Comparator to use.    * @param filterIfColumnMissing if true then we will filter rows that don't    * have the column.    */
specifier|public
name|ValueFilter
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
parameter_list|,
name|boolean
name|filterIfColumnMissing
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
name|this
operator|.
name|filterIfColumnMissing
operator|=
name|filterIfColumnMissing
expr_stmt|;
block|}
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
name|byte
index|[]
name|rowKey
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
return|return
literal|false
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
name|this
operator|.
name|foundColValue
operator|=
literal|true
expr_stmt|;
name|boolean
name|filtered
init|=
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
decl_stmt|;
if|if
condition|(
name|filtered
condition|)
block|{
name|this
operator|.
name|filterThisRow
operator|=
literal|true
expr_stmt|;
return|return
name|ReturnCode
operator|.
name|NEXT_ROW
return|;
block|}
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
decl_stmt|;
if|if
condition|(
name|comparator
operator|!=
literal|null
condition|)
block|{
name|compareResult
operator|=
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
expr_stmt|;
block|}
else|else
block|{
name|compareResult
operator|=
name|Bytes
operator|.
name|compareTo
argument_list|(
name|value
argument_list|,
literal|0
argument_list|,
name|value
operator|.
name|length
argument_list|,
name|data
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
block|}
switch|switch
condition|(
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
name|filterAllRemaining
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
specifier|public
name|boolean
name|filterRow
parameter_list|()
block|{
name|boolean
name|result
init|=
name|filterThisRow
operator|||
operator|(
name|filterIfColumnMissing
operator|&&
operator|!
name|foundColValue
operator|)
decl_stmt|;
name|filterThisRow
operator|=
literal|false
expr_stmt|;
name|foundColValue
operator|=
literal|false
expr_stmt|;
return|return
name|result
return|;
block|}
specifier|public
name|void
name|reset
parameter_list|()
block|{
comment|// Nothing.
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
name|int
name|valueLen
init|=
name|in
operator|.
name|readInt
argument_list|()
decl_stmt|;
if|if
condition|(
name|valueLen
operator|>
literal|0
condition|)
block|{
name|value
operator|=
operator|new
name|byte
index|[
name|valueLen
index|]
expr_stmt|;
name|in
operator|.
name|readFully
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
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
name|comparator
operator|=
operator|(
name|WritableByteArrayComparable
operator|)
name|ObjectWritable
operator|.
name|readObject
argument_list|(
name|in
argument_list|,
operator|new
name|HBaseConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|filterIfColumnMissing
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
if|if
condition|(
name|value
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
name|value
operator|.
name|length
argument_list|)
expr_stmt|;
name|out
operator|.
name|write
argument_list|(
name|value
argument_list|)
expr_stmt|;
block|}
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
name|ObjectWritable
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
operator|new
name|HBaseConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|out
operator|.
name|writeBoolean
argument_list|(
name|filterIfColumnMissing
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

