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
name|HashSet
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
name|List
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
name|ArrayList
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
name|util
operator|.
name|Bytes
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
comment|/**  * A filter for adding inter-column timestamp matching  * Only cells with a correspondingly timestamped entry in  * the target column will be retained  * Not compatible with Scan.setBatch as operations need   * full rows for correct filtering   */
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
name|DependentColumnFilter
extends|extends
name|CompareFilter
block|{
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
name|boolean
name|dropDependentColumn
decl_stmt|;
specifier|protected
name|Set
argument_list|<
name|Long
argument_list|>
name|stampSet
init|=
operator|new
name|HashSet
argument_list|<
name|Long
argument_list|>
argument_list|()
decl_stmt|;
comment|/**    * Build a dependent column filter with value checking    * dependent column varies will be compared using the supplied    * compareOp and comparator, for usage of which    * refer to {@link CompareFilter}    *     * @param family dependent column family    * @param qualifier dependent column qualifier    * @param dropDependentColumn whether the column should be discarded after    * @param valueCompareOp comparison op     * @param valueComparator comparator    */
specifier|public
name|DependentColumnFilter
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
name|boolean
name|dropDependentColumn
parameter_list|,
specifier|final
name|CompareOp
name|valueCompareOp
parameter_list|,
specifier|final
name|WritableByteArrayComparable
name|valueComparator
parameter_list|)
block|{
comment|// set up the comparator
name|super
argument_list|(
name|valueCompareOp
argument_list|,
name|valueComparator
argument_list|)
expr_stmt|;
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
name|dropDependentColumn
operator|=
name|dropDependentColumn
expr_stmt|;
block|}
comment|/**    * Constructor for DependentColumn filter.    * Keyvalues where a keyvalue from target column     * with the same timestamp do not exist will be dropped.     *     * @param family name of target column family    * @param qualifier name of column qualifier    */
specifier|public
name|DependentColumnFilter
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
parameter_list|)
block|{
name|this
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Constructor for DependentColumn filter.    * Keyvalues where a keyvalue from target column     * with the same timestamp do not exist will be dropped.     *     * @param family name of dependent column family    * @param qualifier name of dependent qualifier    * @param dropDependentColumn whether the dependent columns keyvalues should be discarded    */
specifier|public
name|DependentColumnFilter
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
name|boolean
name|dropDependentColumn
parameter_list|)
block|{
name|this
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|dropDependentColumn
argument_list|,
name|CompareOp
operator|.
name|NO_OP
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return the column family    */
specifier|public
name|byte
index|[]
name|getFamily
parameter_list|()
block|{
return|return
name|this
operator|.
name|columnFamily
return|;
block|}
comment|/**    * @return the column qualifier    */
specifier|public
name|byte
index|[]
name|getQualifier
parameter_list|()
block|{
return|return
name|this
operator|.
name|columnQualifier
return|;
block|}
comment|/**    * @return true if we should drop the dependent column, false otherwise    */
specifier|public
name|boolean
name|dropDependentColumn
parameter_list|()
block|{
return|return
name|this
operator|.
name|dropDependentColumn
return|;
block|}
specifier|public
name|boolean
name|getDropDependentColumn
parameter_list|()
block|{
return|return
name|this
operator|.
name|dropDependentColumn
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
name|KeyValue
name|v
parameter_list|)
block|{
comment|// Check if the column and qualifier match
if|if
condition|(
operator|!
name|v
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
comment|// include non-matches for the time being, they'll be discarded afterwards
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
comment|// If it doesn't pass the op, skip it
if|if
condition|(
name|comparator
operator|!=
literal|null
operator|&&
name|doCompare
argument_list|(
name|compareOp
argument_list|,
name|comparator
argument_list|,
name|v
operator|.
name|getBuffer
argument_list|()
argument_list|,
name|v
operator|.
name|getValueOffset
argument_list|()
argument_list|,
name|v
operator|.
name|getValueLength
argument_list|()
argument_list|)
condition|)
return|return
name|ReturnCode
operator|.
name|SKIP
return|;
name|stampSet
operator|.
name|add
argument_list|(
name|v
operator|.
name|getTimestamp
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|dropDependentColumn
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|SKIP
return|;
block|}
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|filterRow
parameter_list|(
name|List
argument_list|<
name|KeyValue
argument_list|>
name|kvs
parameter_list|)
block|{
name|Iterator
argument_list|<
name|KeyValue
argument_list|>
name|it
init|=
name|kvs
operator|.
name|iterator
argument_list|()
decl_stmt|;
name|KeyValue
name|kv
decl_stmt|;
while|while
condition|(
name|it
operator|.
name|hasNext
argument_list|()
condition|)
block|{
name|kv
operator|=
name|it
operator|.
name|next
argument_list|()
expr_stmt|;
if|if
condition|(
operator|!
name|stampSet
operator|.
name|contains
argument_list|(
name|kv
operator|.
name|getTimestamp
argument_list|()
argument_list|)
condition|)
block|{
name|it
operator|.
name|remove
argument_list|()
expr_stmt|;
block|}
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasFilterRow
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterRow
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
name|byte
index|[]
name|buffer
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
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|stampSet
operator|.
name|clear
argument_list|()
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
literal|2
operator|||
name|filterArguments
operator|.
name|size
argument_list|()
operator|==
literal|3
operator|||
name|filterArguments
operator|.
name|size
argument_list|()
operator|==
literal|5
argument_list|,
literal|"Expected 2, 3 or 5 but got: %s"
argument_list|,
name|filterArguments
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
if|if
condition|(
name|filterArguments
operator|.
name|size
argument_list|()
operator|==
literal|2
condition|)
block|{
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
return|return
operator|new
name|DependentColumnFilter
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|filterArguments
operator|.
name|size
argument_list|()
operator|==
literal|3
condition|)
block|{
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
name|boolean
name|dropDependentColumn
init|=
name|ParseFilter
operator|.
name|convertByteArrayToBoolean
argument_list|(
name|filterArguments
operator|.
name|get
argument_list|(
literal|2
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|DependentColumnFilter
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|dropDependentColumn
argument_list|)
return|;
block|}
elseif|else
if|if
condition|(
name|filterArguments
operator|.
name|size
argument_list|()
operator|==
literal|5
condition|)
block|{
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
name|boolean
name|dropDependentColumn
init|=
name|ParseFilter
operator|.
name|convertByteArrayToBoolean
argument_list|(
name|filterArguments
operator|.
name|get
argument_list|(
literal|2
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
literal|3
argument_list|)
argument_list|)
decl_stmt|;
name|WritableByteArrayComparable
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
literal|4
argument_list|)
argument_list|)
argument_list|)
decl_stmt|;
return|return
operator|new
name|DependentColumnFilter
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|,
name|dropDependentColumn
argument_list|,
name|compareOp
argument_list|,
name|comparator
argument_list|)
return|;
block|}
else|else
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Expected 2, 3 or 5 but got: "
operator|+
name|filterArguments
operator|.
name|size
argument_list|()
argument_list|)
throw|;
block|}
block|}
comment|/**    * @return The filter serialized using pb    */
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
block|{
name|FilterProtos
operator|.
name|DependentColumnFilter
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|DependentColumnFilter
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setCompareFilter
argument_list|(
name|super
operator|.
name|convert
argument_list|()
argument_list|)
expr_stmt|;
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
name|builder
operator|.
name|setDropDependentColumn
argument_list|(
name|this
operator|.
name|dropDependentColumn
argument_list|)
expr_stmt|;
return|return
name|builder
operator|.
name|build
argument_list|()
operator|.
name|toByteArray
argument_list|()
return|;
block|}
comment|/**    * @param pbBytes A pb serialized {@link DependentColumnFilter} instance    * @return An instance of {@link DependentColumnFilter} made from<code>bytes</code>    * @throws DeserializationException    * @see {@link #toByteArray()}    */
specifier|public
specifier|static
name|DependentColumnFilter
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
name|DependentColumnFilter
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|FilterProtos
operator|.
name|DependentColumnFilter
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
name|valueCompareOp
init|=
name|CompareOp
operator|.
name|valueOf
argument_list|(
name|proto
operator|.
name|getCompareFilter
argument_list|()
operator|.
name|getCompareOp
argument_list|()
operator|.
name|name
argument_list|()
argument_list|)
decl_stmt|;
name|WritableByteArrayComparable
name|valueComparator
init|=
literal|null
decl_stmt|;
try|try
block|{
if|if
condition|(
name|proto
operator|.
name|getCompareFilter
argument_list|()
operator|.
name|hasComparator
argument_list|()
condition|)
block|{
name|valueComparator
operator|=
name|ProtobufUtil
operator|.
name|toComparator
argument_list|(
name|proto
operator|.
name|getCompareFilter
argument_list|()
operator|.
name|getComparator
argument_list|()
argument_list|)
expr_stmt|;
block|}
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
name|DependentColumnFilter
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
name|proto
operator|.
name|getDropDependentColumn
argument_list|()
argument_list|,
name|valueCompareOp
argument_list|,
name|valueComparator
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
name|DependentColumnFilter
operator|)
condition|)
return|return
literal|false
return|;
name|DependentColumnFilter
name|other
init|=
operator|(
name|DependentColumnFilter
operator|)
name|o
decl_stmt|;
return|return
name|other
operator|!=
literal|null
operator|&&
name|super
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|other
argument_list|)
operator|&&
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
name|dropDependentColumn
argument_list|()
operator|==
name|other
operator|.
name|dropDependentColumn
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
return|return
name|String
operator|.
name|format
argument_list|(
literal|"%s (%s, %s, %s, %s, %s)"
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
name|dropDependentColumn
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

