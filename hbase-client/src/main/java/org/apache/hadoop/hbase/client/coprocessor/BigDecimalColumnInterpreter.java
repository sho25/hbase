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
name|java
operator|.
name|math
operator|.
name|BigDecimal
import|;
end_import

begin_import
import|import
name|java
operator|.
name|math
operator|.
name|RoundingMode
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
name|CellUtil
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
name|BigDecimalMsg
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
name|util
operator|.
name|ByteStringer
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
comment|/**  * ColumnInterpreter for doing Aggregation's with BigDecimal columns. This class  * is required at the RegionServer also.  *  */
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
name|BigDecimalColumnInterpreter
extends|extends
name|ColumnInterpreter
argument_list|<
name|BigDecimal
argument_list|,
name|BigDecimal
argument_list|,
name|EmptyMsg
argument_list|,
name|BigDecimalMsg
argument_list|,
name|BigDecimalMsg
argument_list|>
block|{
annotation|@
name|Override
specifier|public
name|BigDecimal
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
name|CellUtil
operator|.
name|cloneValue
argument_list|(
name|kv
argument_list|)
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|CellUtil
operator|.
name|getValueAsBigDecimal
argument_list|(
name|kv
argument_list|)
operator|.
name|setScale
argument_list|(
literal|2
argument_list|,
name|RoundingMode
operator|.
name|HALF_EVEN
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|BigDecimal
name|add
parameter_list|(
name|BigDecimal
name|bd1
parameter_list|,
name|BigDecimal
name|bd2
parameter_list|)
block|{
if|if
condition|(
name|bd1
operator|==
literal|null
operator|^
name|bd2
operator|==
literal|null
condition|)
block|{
return|return
operator|(
name|bd1
operator|==
literal|null
operator|)
condition|?
name|bd2
else|:
name|bd1
return|;
comment|// either of one is null.
block|}
if|if
condition|(
name|bd1
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|bd1
operator|.
name|add
argument_list|(
name|bd2
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compare
parameter_list|(
specifier|final
name|BigDecimal
name|bd1
parameter_list|,
specifier|final
name|BigDecimal
name|bd2
parameter_list|)
block|{
if|if
condition|(
name|bd1
operator|==
literal|null
operator|^
name|bd2
operator|==
literal|null
condition|)
block|{
return|return
name|bd1
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
if|if
condition|(
name|bd1
operator|==
literal|null
condition|)
block|{
return|return
literal|0
return|;
comment|// both are null
block|}
return|return
name|bd1
operator|.
name|compareTo
argument_list|(
name|bd2
argument_list|)
return|;
comment|// natural ordering.
block|}
annotation|@
name|Override
specifier|public
name|BigDecimal
name|getMaxValue
parameter_list|()
block|{
return|return
name|BigDecimal
operator|.
name|valueOf
argument_list|(
name|Double
operator|.
name|MAX_VALUE
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|BigDecimal
name|increment
parameter_list|(
name|BigDecimal
name|bd
parameter_list|)
block|{
return|return
name|bd
operator|==
literal|null
condition|?
literal|null
else|:
operator|(
name|bd
operator|.
name|add
argument_list|(
name|BigDecimal
operator|.
name|ONE
argument_list|)
operator|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|BigDecimal
name|multiply
parameter_list|(
name|BigDecimal
name|bd1
parameter_list|,
name|BigDecimal
name|bd2
parameter_list|)
block|{
return|return
operator|(
name|bd1
operator|==
literal|null
operator|||
name|bd2
operator|==
literal|null
operator|)
condition|?
literal|null
else|:
name|bd1
operator|.
name|multiply
argument_list|(
name|bd2
argument_list|)
operator|.
name|setScale
argument_list|(
literal|2
argument_list|,
name|RoundingMode
operator|.
name|HALF_EVEN
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|BigDecimal
name|getMinValue
parameter_list|()
block|{
return|return
name|BigDecimal
operator|.
name|valueOf
argument_list|(
name|Double
operator|.
name|MIN_VALUE
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|double
name|divideForAvg
parameter_list|(
name|BigDecimal
name|bd1
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
name|bd1
operator|==
literal|null
operator|)
condition|?
name|Double
operator|.
name|NaN
else|:
operator|(
name|bd1
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
name|BigDecimal
name|castToReturnType
parameter_list|(
name|BigDecimal
name|bd
parameter_list|)
block|{
return|return
name|bd
return|;
block|}
annotation|@
name|Override
specifier|public
name|BigDecimal
name|castToCellType
parameter_list|(
name|BigDecimal
name|bd
parameter_list|)
block|{
return|return
name|bd
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
specifier|private
name|BigDecimalMsg
name|getProtoForType
parameter_list|(
name|BigDecimal
name|t
parameter_list|)
block|{
name|BigDecimalMsg
operator|.
name|Builder
name|builder
init|=
name|BigDecimalMsg
operator|.
name|newBuilder
argument_list|()
decl_stmt|;
return|return
name|builder
operator|.
name|setBigdecimalMsg
argument_list|(
name|ByteStringer
operator|.
name|wrap
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|t
argument_list|)
argument_list|)
argument_list|)
operator|.
name|build
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
name|BigDecimalMsg
name|getProtoForCellType
parameter_list|(
name|BigDecimal
name|t
parameter_list|)
block|{
return|return
name|getProtoForType
argument_list|(
name|t
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|BigDecimalMsg
name|getProtoForPromotedType
parameter_list|(
name|BigDecimal
name|s
parameter_list|)
block|{
return|return
name|getProtoForType
argument_list|(
name|s
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|BigDecimal
name|getPromotedValueFromProto
parameter_list|(
name|BigDecimalMsg
name|r
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toBigDecimal
argument_list|(
name|r
operator|.
name|getBigdecimalMsg
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|BigDecimal
name|getCellValueFromProto
parameter_list|(
name|BigDecimalMsg
name|q
parameter_list|)
block|{
return|return
name|Bytes
operator|.
name|toBigDecimal
argument_list|(
name|q
operator|.
name|getBigdecimalMsg
argument_list|()
operator|.
name|toByteArray
argument_list|()
argument_list|)
return|;
block|}
block|}
end_class

end_unit

