begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|ZeroCopyLiteralByteString
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
name|Cell
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
name|KeyValueUtil
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
name|exceptions
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

begin_comment
comment|/**  * The filter looks for the given columns in KeyValue. Once there is a match for  * any one of the columns, it returns ReturnCode.NEXT_ROW for remaining  * KeyValues in the row.  *<p>  * Note : It may emit KVs which do not have the given columns in them, if  * these KVs happen to occur before a KV which does have a match. Given this  * caveat, this filter is only useful for special cases  * like {@link org.apache.hadoop.hbase.mapreduce.RowCounter}.  *<p>  */
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
name|FirstKeyValueMatchingQualifiersFilter
extends|extends
name|FirstKeyOnlyFilter
block|{
specifier|private
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|qualifiers
decl_stmt|;
comment|/**    * Constructor which takes a set of columns. As soon as first KeyValue    * matching any of these columns is found, filter moves to next row.    *     * @param qualifiers the set of columns to me matched.    */
specifier|public
name|FirstKeyValueMatchingQualifiersFilter
parameter_list|(
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|qualifiers
parameter_list|)
block|{
name|this
operator|.
name|qualifiers
operator|=
name|qualifiers
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
name|Cell
name|v
parameter_list|)
block|{
if|if
condition|(
name|hasFoundKV
argument_list|()
condition|)
block|{
return|return
name|ReturnCode
operator|.
name|NEXT_ROW
return|;
block|}
elseif|else
if|if
condition|(
name|hasOneMatchingQualifier
argument_list|(
name|v
argument_list|)
condition|)
block|{
name|setFoundKV
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
specifier|private
name|boolean
name|hasOneMatchingQualifier
parameter_list|(
name|Cell
name|v
parameter_list|)
block|{
for|for
control|(
name|byte
index|[]
name|q
range|:
name|qualifiers
control|)
block|{
comment|// TODO get rid of this by adding matching qualifier to interface.
name|KeyValue
name|kv
init|=
name|KeyValueUtil
operator|.
name|ensureKeyValue
argument_list|(
name|v
argument_list|)
decl_stmt|;
if|if
condition|(
name|kv
operator|.
name|matchingQualifier
argument_list|(
name|q
argument_list|)
condition|)
block|{
return|return
literal|true
return|;
block|}
block|}
return|return
literal|false
return|;
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
name|FirstKeyValueMatchingQualifiersFilter
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|FirstKeyValueMatchingQualifiersFilter
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
for|for
control|(
name|byte
index|[]
name|qualifier
range|:
name|qualifiers
control|)
block|{
if|if
condition|(
name|qualifier
operator|!=
literal|null
condition|)
name|builder
operator|.
name|addQualifiers
argument_list|(
name|ZeroCopyLiteralByteString
operator|.
name|wrap
argument_list|(
name|qualifier
argument_list|)
argument_list|)
expr_stmt|;
block|}
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
comment|/**    * @param pbBytes A pb serialized {@link FirstKeyValueMatchingQualifiersFilter} instance    * @return An instance of {@link FirstKeyValueMatchingQualifiersFilter} made from<code>bytes</code>    * @throws DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|FirstKeyValueMatchingQualifiersFilter
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
name|FirstKeyValueMatchingQualifiersFilter
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|FilterProtos
operator|.
name|FirstKeyValueMatchingQualifiersFilter
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
name|TreeSet
argument_list|<
name|byte
index|[]
argument_list|>
name|qualifiers
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
name|ByteString
name|qualifier
range|:
name|proto
operator|.
name|getQualifiersList
argument_list|()
control|)
block|{
name|qualifiers
operator|.
name|add
argument_list|(
name|qualifier
operator|.
name|toByteArray
argument_list|()
argument_list|)
expr_stmt|;
block|}
return|return
operator|new
name|FirstKeyValueMatchingQualifiersFilter
argument_list|(
name|qualifiers
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
name|FirstKeyValueMatchingQualifiersFilter
operator|)
condition|)
return|return
literal|false
return|;
name|FirstKeyValueMatchingQualifiersFilter
name|other
init|=
operator|(
name|FirstKeyValueMatchingQualifiersFilter
operator|)
name|o
decl_stmt|;
return|return
name|this
operator|.
name|qualifiers
operator|.
name|equals
argument_list|(
name|other
operator|.
name|qualifiers
argument_list|)
return|;
block|}
block|}
end_class

end_unit

