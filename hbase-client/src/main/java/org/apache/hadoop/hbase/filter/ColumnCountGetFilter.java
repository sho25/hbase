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
name|hbase
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
name|InvalidProtocolBufferException
import|;
end_import

begin_comment
comment|/**  * Simple filter that returns first N columns on row only.  * This filter was written to test filters in Get and as soon as it gets  * its quota of columns, {@link #filterAllRemaining()} returns true.  This  * makes this filter unsuitable as a Scan filter.  */
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
name|ColumnCountGetFilter
extends|extends
name|FilterBase
block|{
specifier|private
name|int
name|limit
init|=
literal|0
decl_stmt|;
specifier|private
name|int
name|count
init|=
literal|0
decl_stmt|;
specifier|public
name|ColumnCountGetFilter
parameter_list|(
specifier|final
name|int
name|n
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkArgument
argument_list|(
name|n
operator|>=
literal|0
argument_list|,
literal|"limit be positive %s"
argument_list|,
name|n
argument_list|)
expr_stmt|;
name|this
operator|.
name|limit
operator|=
name|n
expr_stmt|;
block|}
specifier|public
name|int
name|getLimit
parameter_list|()
block|{
return|return
name|limit
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
name|this
operator|.
name|count
operator|>
name|this
operator|.
name|limit
return|;
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
name|this
operator|.
name|count
operator|++
expr_stmt|;
return|return
name|filterAllRemaining
argument_list|()
condition|?
name|ReturnCode
operator|.
name|NEXT_COL
else|:
name|ReturnCode
operator|.
name|INCLUDE_AND_NEXT_COL
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|this
operator|.
name|count
operator|=
literal|0
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
name|int
name|limit
init|=
name|ParseFilter
operator|.
name|convertByteArrayToInt
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
name|ColumnCountGetFilter
argument_list|(
name|limit
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
name|ColumnCountGetFilter
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|ColumnCountGetFilter
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setLimit
argument_list|(
name|this
operator|.
name|limit
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
comment|/**    * @param pbBytes A pb serialized {@link ColumnCountGetFilter} instance    * @return An instance of {@link ColumnCountGetFilter} made from<code>bytes</code>    * @throws org.apache.hadoop.hbase.exceptions.DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|ColumnCountGetFilter
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
name|ColumnCountGetFilter
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|FilterProtos
operator|.
name|ColumnCountGetFilter
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
name|ColumnCountGetFilter
argument_list|(
name|proto
operator|.
name|getLimit
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
name|ColumnCountGetFilter
operator|)
condition|)
return|return
literal|false
return|;
name|ColumnCountGetFilter
name|other
init|=
operator|(
name|ColumnCountGetFilter
operator|)
name|o
decl_stmt|;
return|return
name|this
operator|.
name|getLimit
argument_list|()
operator|==
name|other
operator|.
name|getLimit
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
name|this
operator|.
name|limit
return|;
block|}
block|}
end_class

end_unit

