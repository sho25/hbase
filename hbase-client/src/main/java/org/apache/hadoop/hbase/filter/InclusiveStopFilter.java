begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|CellComparator
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|yetus
operator|.
name|audience
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
name|shaded
operator|.
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|UnsafeByteOperations
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
name|shaded
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|shaded
operator|.
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

begin_comment
comment|/**  * A Filter that stops after the given row.  There is no "RowStopFilter" because  * the Scan spec allows you to specify a stop row.  *  * Use this filter to include the stop row, eg: [A,Z].  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|InclusiveStopFilter
extends|extends
name|FilterBase
block|{
specifier|private
name|byte
index|[]
name|stopRowKey
decl_stmt|;
specifier|private
name|boolean
name|done
init|=
literal|false
decl_stmt|;
specifier|public
name|InclusiveStopFilter
parameter_list|(
specifier|final
name|byte
index|[]
name|stopRowKey
parameter_list|)
block|{
name|this
operator|.
name|stopRowKey
operator|=
name|stopRowKey
expr_stmt|;
block|}
specifier|public
name|byte
index|[]
name|getStopRowKey
parameter_list|()
block|{
return|return
name|this
operator|.
name|stopRowKey
return|;
block|}
annotation|@
name|Deprecated
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterKeyValue
parameter_list|(
specifier|final
name|Cell
name|c
parameter_list|)
block|{
return|return
name|filterCell
argument_list|(
name|c
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|ReturnCode
name|filterCell
parameter_list|(
specifier|final
name|Cell
name|c
parameter_list|)
block|{
if|if
condition|(
name|done
condition|)
return|return
name|ReturnCode
operator|.
name|NEXT_ROW
return|;
return|return
name|ReturnCode
operator|.
name|INCLUDE
return|;
block|}
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
name|Cell
name|firstRowCell
parameter_list|)
block|{
comment|// if stopRowKey is<= buffer, then true, filter row.
if|if
condition|(
name|filterAllRemaining
argument_list|()
condition|)
return|return
literal|true
return|;
name|int
name|cmp
init|=
name|CellComparator
operator|.
name|getInstance
argument_list|()
operator|.
name|compareRows
argument_list|(
name|firstRowCell
argument_list|,
name|stopRowKey
argument_list|,
literal|0
argument_list|,
name|stopRowKey
operator|.
name|length
argument_list|)
decl_stmt|;
name|done
operator|=
name|reversed
condition|?
name|cmp
operator|<
literal|0
else|:
name|cmp
operator|>
literal|0
expr_stmt|;
return|return
name|done
return|;
block|}
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
block|{
return|return
name|done
return|;
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
literal|1
argument_list|,
literal|"Expected 1 but got: %s"
argument_list|,
name|filterArguments
operator|.
name|size
argument_list|()
argument_list|)
expr_stmt|;
name|byte
index|[]
name|stopRowKey
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
return|return
operator|new
name|InclusiveStopFilter
argument_list|(
name|stopRowKey
argument_list|)
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
name|InclusiveStopFilter
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|InclusiveStopFilter
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|stopRowKey
operator|!=
literal|null
condition|)
name|builder
operator|.
name|setStopRowKey
argument_list|(
name|UnsafeByteOperations
operator|.
name|unsafeWrap
argument_list|(
name|this
operator|.
name|stopRowKey
argument_list|)
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
comment|/**    * @param pbBytes A pb serialized {@link InclusiveStopFilter} instance    * @return An instance of {@link InclusiveStopFilter} made from<code>bytes</code>    * @throws DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|InclusiveStopFilter
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
name|InclusiveStopFilter
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|FilterProtos
operator|.
name|InclusiveStopFilter
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
return|return
operator|new
name|InclusiveStopFilter
argument_list|(
name|proto
operator|.
name|hasStopRowKey
argument_list|()
condition|?
name|proto
operator|.
name|getStopRowKey
argument_list|()
operator|.
name|toByteArray
argument_list|()
else|:
literal|null
argument_list|)
return|;
block|}
comment|/**    * @param o the other filter to compare with    * @return true if and only if the fields of the filter that are serialized    * are equal to the corresponding fields in other.  Used for testing.    */
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
name|InclusiveStopFilter
operator|)
condition|)
return|return
literal|false
return|;
name|InclusiveStopFilter
name|other
init|=
operator|(
name|InclusiveStopFilter
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
name|getStopRowKey
argument_list|()
argument_list|,
name|other
operator|.
name|getStopRowKey
argument_list|()
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
name|this
operator|.
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|" "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|this
operator|.
name|stopRowKey
argument_list|)
return|;
block|}
block|}
end_class

end_unit

