begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|List
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
comment|/**  * This is a Filter wrapper class which is used in the server side. Some filter  * related hooks can be defined in this wrapper. The only way to create a  * FilterWrapper instance is passing a client side Filter instance through  * {@link org.apache.hadoop.hbase.client.Scan#getFilter()}.  *   */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|final
specifier|public
class|class
name|FilterWrapper
extends|extends
name|Filter
block|{
name|Filter
name|filter
init|=
literal|null
decl_stmt|;
specifier|public
name|FilterWrapper
parameter_list|(
name|Filter
name|filter
parameter_list|)
block|{
if|if
condition|(
literal|null
operator|==
name|filter
condition|)
block|{
comment|// ensure the filter instance is not null
throw|throw
operator|new
name|NullPointerException
argument_list|(
literal|"Cannot create FilterWrapper with null Filter"
argument_list|)
throw|;
block|}
name|this
operator|.
name|filter
operator|=
name|filter
expr_stmt|;
block|}
comment|/**    * @return The filter serialized using pb    */
specifier|public
name|byte
index|[]
name|toByteArray
parameter_list|()
throws|throws
name|IOException
block|{
name|FilterProtos
operator|.
name|FilterWrapper
operator|.
name|Builder
name|builder
init|=
name|FilterProtos
operator|.
name|FilterWrapper
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
name|builder
operator|.
name|setFilter
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|this
operator|.
name|filter
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
comment|/**    * @param pbBytes A pb serialized {@link FilterWrapper} instance    * @return An instance of {@link FilterWrapper} made from<code>bytes</code>    * @throws org.apache.hadoop.hbase.exceptions.DeserializationException    * @see #toByteArray    */
specifier|public
specifier|static
name|FilterWrapper
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
name|FilterWrapper
name|proto
decl_stmt|;
try|try
block|{
name|proto
operator|=
name|FilterProtos
operator|.
name|FilterWrapper
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
try|try
block|{
return|return
operator|new
name|FilterWrapper
argument_list|(
name|ProtobufUtil
operator|.
name|toFilter
argument_list|(
name|proto
operator|.
name|getFilter
argument_list|()
argument_list|)
argument_list|)
return|;
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
block|}
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
throws|throws
name|IOException
block|{
name|this
operator|.
name|filter
operator|.
name|reset
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterAllRemaining
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|filter
operator|.
name|filterAllRemaining
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterRow
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|filter
operator|.
name|filterRow
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|getNextCellHint
parameter_list|(
name|Cell
name|currentCell
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|filter
operator|.
name|getNextCellHint
argument_list|(
name|currentCell
argument_list|)
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
throws|throws
name|IOException
block|{
comment|// No call to this.
return|return
name|this
operator|.
name|filter
operator|.
name|filterRowKey
argument_list|(
name|buffer
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|filterRowKey
parameter_list|(
name|Cell
name|cell
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|filter
operator|.
name|filterRowKey
argument_list|(
name|cell
argument_list|)
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
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|filter
operator|.
name|filterKeyValue
argument_list|(
name|v
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Cell
name|transformCell
parameter_list|(
name|Cell
name|v
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|this
operator|.
name|filter
operator|.
name|transformCell
argument_list|(
name|v
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasFilterRow
parameter_list|()
block|{
return|return
name|this
operator|.
name|filter
operator|.
name|hasFilterRow
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|filterRowCells
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|kvs
parameter_list|)
throws|throws
name|IOException
block|{
name|filterRowCellsWithRet
argument_list|(
name|kvs
argument_list|)
expr_stmt|;
block|}
specifier|public
enum|enum
name|FilterRowRetCode
block|{
name|NOT_CALLED
block|,
name|INCLUDE
block|,
comment|// corresponds to filter.filterRow() returning false
name|EXCLUDE
comment|// corresponds to filter.filterRow() returning true
block|}
specifier|public
name|FilterRowRetCode
name|filterRowCellsWithRet
parameter_list|(
name|List
argument_list|<
name|Cell
argument_list|>
name|kvs
parameter_list|)
throws|throws
name|IOException
block|{
comment|//To fix HBASE-6429,
comment|//Filter with filterRow() returning true is incompatible with scan with limit
comment|//1. hasFilterRow() returns true, if either filterRow() or filterRow(kvs) is implemented.
comment|//2. filterRow() is merged with filterRow(kvs),
comment|//so that to make all those row related filtering stuff in the same function.
name|this
operator|.
name|filter
operator|.
name|filterRowCells
argument_list|(
name|kvs
argument_list|)
expr_stmt|;
if|if
condition|(
operator|!
name|kvs
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
if|if
condition|(
name|this
operator|.
name|filter
operator|.
name|filterRow
argument_list|()
condition|)
block|{
name|kvs
operator|.
name|clear
argument_list|()
expr_stmt|;
return|return
name|FilterRowRetCode
operator|.
name|EXCLUDE
return|;
block|}
return|return
name|FilterRowRetCode
operator|.
name|INCLUDE
return|;
block|}
return|return
name|FilterRowRetCode
operator|.
name|NOT_CALLED
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isFamilyEssential
parameter_list|(
name|byte
index|[]
name|name
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|filter
operator|.
name|isFamilyEssential
argument_list|(
name|name
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
name|FilterWrapper
operator|)
condition|)
return|return
literal|false
return|;
name|FilterWrapper
name|other
init|=
operator|(
name|FilterWrapper
operator|)
name|o
decl_stmt|;
return|return
name|this
operator|.
name|filter
operator|.
name|areSerializedFieldsEqual
argument_list|(
name|other
operator|.
name|filter
argument_list|)
return|;
block|}
block|}
end_class

end_unit

