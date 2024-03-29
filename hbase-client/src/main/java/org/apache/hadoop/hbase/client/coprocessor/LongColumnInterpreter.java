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
name|client
operator|.
name|coprocessor
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
name|HBaseInterfaceAudience
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
name|PrivateCellUtil
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
name|yetus
operator|.
name|audience
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
name|coprocessor
operator|.
name|ColumnInterpreter
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
name|EmptyMsg
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
name|LongMsg
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

begin_comment
comment|/**  * a concrete column interpreter implementation. The cell value is a Long value  * and its promoted data type is also a Long value. For computing aggregation  * function, this class is used to find the datatype of the cell value. Client  * is supposed to instantiate it and passed along as a parameter. See  * TestAggregateProtocol methods for its sample usage.  * Its methods handle null arguments gracefully.   */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|LongColumnInterpreter
extends|extends
name|ColumnInterpreter
argument_list|<
name|Long
argument_list|,
name|Long
argument_list|,
name|EmptyMsg
argument_list|,
name|LongMsg
argument_list|,
name|LongMsg
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|Long
name|getValue
parameter_list|(
name|byte
index|[]
name|colFamily
parameter_list|,
name|byte
index|[]
name|colQualifier
parameter_list|,
name|Cell
name|kv
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|kv
operator|==
literal|null
operator|||
name|kv
operator|.
name|getValueLength
argument_list|()
operator|!=
name|Bytes
operator|.
name|SIZEOF_LONG
condition|)
return|return
literal|null
return|;
return|return
name|PrivateCellUtil
operator|.
name|getValueAsLong
argument_list|(
name|kv
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|add
parameter_list|(
name|Long
name|l1
parameter_list|,
name|Long
name|l2
parameter_list|)
block|{
if|if
condition|(
name|l1
operator|==
literal|null
operator|^
name|l2
operator|==
literal|null
condition|)
block|{
return|return
operator|(
name|l1
operator|==
literal|null
operator|)
condition|?
name|l2
else|:
name|l1
return|;
comment|// either of one is null.
block|}
elseif|else
if|if
condition|(
name|l1
operator|==
literal|null
condition|)
comment|// both are null
return|return
literal|null
return|;
return|return
name|l1
operator|+
name|l2
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
specifier|final
name|Long
name|l1
parameter_list|,
specifier|final
name|Long
name|l2
parameter_list|)
block|{
if|if
condition|(
name|l1
operator|==
literal|null
operator|^
name|l2
operator|==
literal|null
condition|)
block|{
return|return
name|l1
operator|==
literal|null
condition|?
operator|-
literal|1
else|:
literal|1
return|;
comment|// either of one is null.
block|}
elseif|else
if|if
condition|(
name|l1
operator|==
literal|null
condition|)
return|return
literal|0
return|;
comment|// both are null
return|return
name|l1
operator|.
name|compareTo
argument_list|(
name|l2
argument_list|)
return|;
comment|// natural ordering.
block|}
annotation|@
name|Override
specifier|public
name|Long
name|getMaxValue
parameter_list|()
block|{
return|return
name|Long
operator|.
name|MAX_VALUE
return|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|increment
parameter_list|(
name|Long
name|o
parameter_list|)
block|{
return|return
name|o
operator|==
literal|null
condition|?
literal|null
else|:
operator|(
name|o
operator|+
literal|1l
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|multiply
parameter_list|(
name|Long
name|l1
parameter_list|,
name|Long
name|l2
parameter_list|)
block|{
return|return
operator|(
name|l1
operator|==
literal|null
operator|||
name|l2
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|l1
operator|*
name|l2
return|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|getMinValue
parameter_list|()
block|{
return|return
name|Long
operator|.
name|MIN_VALUE
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|divideForAvg
parameter_list|(
name|Long
name|l1
parameter_list|,
name|Long
name|l2
parameter_list|)
block|{
return|return
operator|(
name|l2
operator|==
literal|null
operator|||
name|l1
operator|==
literal|null
operator|)
condition|?
name|Double
operator|.
name|NaN
else|:
operator|(
name|l1
operator|.
name|doubleValue
argument_list|()
operator|/
name|l2
operator|.
name|doubleValue
argument_list|()
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|castToReturnType
parameter_list|(
name|Long
name|o
parameter_list|)
block|{
return|return
name|o
return|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|castToCellType
parameter_list|(
name|Long
name|l
parameter_list|)
block|{
return|return
name|l
return|;
block|}
annotation|@
name|Override
specifier|public
name|EmptyMsg
name|getRequestData
parameter_list|()
block|{
return|return
name|EmptyMsg
operator|.
name|getDefaultInstance
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|EmptyMsg
name|msg
parameter_list|)
block|{
comment|//nothing
block|}
annotation|@
name|Override
specifier|public
name|LongMsg
name|getProtoForCellType
parameter_list|(
name|Long
name|t
parameter_list|)
block|{
name|LongMsg
operator|.
name|Builder
name|builder
init|=
name|LongMsg
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
return|return
name|builder
operator|.
name|setLongMsg
argument_list|(
name|t
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|LongMsg
name|getProtoForPromotedType
parameter_list|(
name|Long
name|s
parameter_list|)
block|{
name|LongMsg
operator|.
name|Builder
name|builder
init|=
name|LongMsg
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
return|return
name|builder
operator|.
name|setLongMsg
argument_list|(
name|s
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|getPromotedValueFromProto
parameter_list|(
name|LongMsg
name|r
parameter_list|)
block|{
return|return
name|r
operator|.
name|getLongMsg
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Long
name|getCellValueFromProto
parameter_list|(
name|LongMsg
name|q
parameter_list|)
block|{
return|return
name|q
operator|.
name|getLongMsg
argument_list|()
return|;
block|}
block|}
end_class

end_unit

