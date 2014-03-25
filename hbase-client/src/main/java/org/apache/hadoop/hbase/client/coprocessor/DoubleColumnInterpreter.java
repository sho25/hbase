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
name|DoubleMsg
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
comment|/**  * a concrete column interpreter implementation. The cell value is a Double value  * and its promoted data type is also a Double value. For computing aggregation  * function, this class is used to find the datatype of the cell value. Client  * is supposed to instantiate it and passed along as a parameter. See  * TestDoubleColumnInterpreter methods for its sample usage.  * Its methods handle null arguments gracefully.   */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|DoubleColumnInterpreter
extends|extends
name|ColumnInterpreter
argument_list|<
name|Double
argument_list|,
name|Double
argument_list|,
name|EmptyMsg
argument_list|,
name|DoubleMsg
argument_list|,
name|DoubleMsg
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|Double
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
name|c
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|c
operator|==
literal|null
operator|||
name|c
operator|.
name|getValueLength
argument_list|()
operator|!=
name|Bytes
operator|.
name|SIZEOF_DOUBLE
condition|)
return|return
literal|null
return|;
return|return
name|Bytes
operator|.
name|toDouble
argument_list|(
name|c
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|c
operator|.
name|getValueOffset
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Double
name|add
parameter_list|(
name|Double
name|d1
parameter_list|,
name|Double
name|d2
parameter_list|)
block|{
if|if
condition|(
name|d1
operator|==
literal|null
operator|||
name|d2
operator|==
literal|null
condition|)
block|{
return|return
operator|(
name|d1
operator|==
literal|null
operator|)
condition|?
name|d2
else|:
name|d1
return|;
block|}
return|return
name|d1
operator|+
name|d2
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
specifier|final
name|Double
name|d1
parameter_list|,
specifier|final
name|Double
name|d2
parameter_list|)
block|{
if|if
condition|(
name|d1
operator|==
literal|null
operator|^
name|d2
operator|==
literal|null
condition|)
block|{
return|return
name|d1
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
name|d1
operator|==
literal|null
condition|)
return|return
literal|0
return|;
comment|// both are null
return|return
name|d1
operator|.
name|compareTo
argument_list|(
name|d2
argument_list|)
return|;
comment|// natural ordering.
block|}
annotation|@
name|Override
specifier|public
name|Double
name|getMaxValue
parameter_list|()
block|{
return|return
name|Double
operator|.
name|MAX_VALUE
return|;
block|}
annotation|@
name|Override
specifier|public
name|Double
name|increment
parameter_list|(
name|Double
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
literal|1.00d
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|Double
name|multiply
parameter_list|(
name|Double
name|d1
parameter_list|,
name|Double
name|d2
parameter_list|)
block|{
return|return
operator|(
name|d1
operator|==
literal|null
operator|||
name|d2
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|d1
operator|*
name|d2
return|;
block|}
annotation|@
name|Override
specifier|public
name|Double
name|getMinValue
parameter_list|()
block|{
return|return
name|Double
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
name|Double
name|d1
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
name|d1
operator|==
literal|null
operator|)
condition|?
name|Double
operator|.
name|NaN
else|:
operator|(
name|d1
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
name|Double
name|castToReturnType
parameter_list|(
name|Double
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
name|Double
name|castToCellType
parameter_list|(
name|Double
name|d
parameter_list|)
block|{
return|return
name|d
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
name|DoubleMsg
name|getProtoForCellType
parameter_list|(
name|Double
name|t
parameter_list|)
block|{
name|DoubleMsg
operator|.
name|Builder
name|builder
init|=
name|DoubleMsg
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
return|return
name|builder
operator|.
name|setDoubleMsg
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
name|DoubleMsg
name|getProtoForPromotedType
parameter_list|(
name|Double
name|s
parameter_list|)
block|{
name|DoubleMsg
operator|.
name|Builder
name|builder
init|=
name|DoubleMsg
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
return|return
name|builder
operator|.
name|setDoubleMsg
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
name|Double
name|getPromotedValueFromProto
parameter_list|(
name|DoubleMsg
name|r
parameter_list|)
block|{
return|return
name|r
operator|.
name|getDoubleMsg
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|Double
name|getCellValueFromProto
parameter_list|(
name|DoubleMsg
name|q
parameter_list|)
block|{
return|return
name|q
operator|.
name|getDoubleMsg
argument_list|()
return|;
block|}
block|}
end_class

end_unit

