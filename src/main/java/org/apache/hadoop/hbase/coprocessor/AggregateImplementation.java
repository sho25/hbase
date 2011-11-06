begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|ArrayList
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
name|FirstKeyOnlyFilter
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
name|ipc
operator|.
name|ProtocolSignature
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
name|regionserver
operator|.
name|InternalScanner
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
name|Pair
import|;
end_import

begin_comment
comment|/**  * A concrete AggregateProtocol implementation. Its system level coprocessor  * that computes the aggregate function at a region level.  */
end_comment

begin_class
specifier|public
class|class
name|AggregateImplementation
extends|extends
name|BaseEndpointCoprocessor
implements|implements
name|AggregateProtocol
block|{
specifier|protected
specifier|static
name|Log
name|log
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|AggregateImplementation
operator|.
name|class
argument_list|)
decl_stmt|;
annotation|@
name|Override
specifier|public
name|ProtocolSignature
name|getProtocolSignature
parameter_list|(
name|String
name|protocol
parameter_list|,
name|long
name|version
parameter_list|,
name|int
name|clientMethodsHashCode
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|AggregateProtocol
operator|.
name|class
operator|.
name|getName
argument_list|()
operator|.
name|equals
argument_list|(
name|protocol
argument_list|)
condition|)
block|{
return|return
operator|new
name|ProtocolSignature
argument_list|(
name|AggregateProtocol
operator|.
name|VERSION
argument_list|,
literal|null
argument_list|)
return|;
block|}
throw|throw
operator|new
name|IOException
argument_list|(
literal|"Unknown protocol: "
operator|+
name|protocol
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|,
name|S
parameter_list|>
name|T
name|getMax
parameter_list|(
name|ColumnInterpreter
argument_list|<
name|T
argument_list|,
name|S
argument_list|>
name|ci
parameter_list|,
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
name|T
name|temp
decl_stmt|;
name|T
name|max
init|=
literal|null
decl_stmt|;
name|InternalScanner
name|scanner
init|=
operator|(
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|getEnvironment
argument_list|()
operator|)
operator|.
name|getRegion
argument_list|()
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|byte
index|[]
name|colFamily
init|=
name|scan
operator|.
name|getFamilies
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|scan
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|get
argument_list|(
name|colFamily
argument_list|)
operator|.
name|pollFirst
argument_list|()
decl_stmt|;
comment|// qualifier can be null.
try|try
block|{
name|boolean
name|hasMoreRows
init|=
literal|false
decl_stmt|;
do|do
block|{
name|hasMoreRows
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|results
control|)
block|{
name|temp
operator|=
name|ci
operator|.
name|getValue
argument_list|(
name|colFamily
argument_list|,
name|qualifier
argument_list|,
name|kv
argument_list|)
expr_stmt|;
name|max
operator|=
operator|(
name|max
operator|==
literal|null
operator|||
name|ci
operator|.
name|compare
argument_list|(
name|temp
argument_list|,
name|max
argument_list|)
operator|>
literal|0
operator|)
condition|?
name|temp
else|:
name|max
expr_stmt|;
block|}
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|hasMoreRows
condition|)
do|;
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|log
operator|.
name|info
argument_list|(
literal|"Maximum from this region is "
operator|+
operator|(
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|getEnvironment
argument_list|()
operator|)
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|": "
operator|+
name|max
argument_list|)
expr_stmt|;
return|return
name|max
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|,
name|S
parameter_list|>
name|T
name|getMin
parameter_list|(
name|ColumnInterpreter
argument_list|<
name|T
argument_list|,
name|S
argument_list|>
name|ci
parameter_list|,
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
name|T
name|min
init|=
literal|null
decl_stmt|;
name|T
name|temp
decl_stmt|;
name|InternalScanner
name|scanner
init|=
operator|(
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|getEnvironment
argument_list|()
operator|)
operator|.
name|getRegion
argument_list|()
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|byte
index|[]
name|colFamily
init|=
name|scan
operator|.
name|getFamilies
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|scan
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|get
argument_list|(
name|colFamily
argument_list|)
operator|.
name|pollFirst
argument_list|()
decl_stmt|;
try|try
block|{
name|boolean
name|hasMoreRows
init|=
literal|false
decl_stmt|;
do|do
block|{
name|hasMoreRows
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|results
control|)
block|{
name|temp
operator|=
name|ci
operator|.
name|getValue
argument_list|(
name|colFamily
argument_list|,
name|qualifier
argument_list|,
name|kv
argument_list|)
expr_stmt|;
name|min
operator|=
operator|(
name|min
operator|==
literal|null
operator|||
name|ci
operator|.
name|compare
argument_list|(
name|temp
argument_list|,
name|min
argument_list|)
operator|<
literal|0
operator|)
condition|?
name|temp
else|:
name|min
expr_stmt|;
block|}
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|hasMoreRows
condition|)
do|;
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|log
operator|.
name|info
argument_list|(
literal|"Minimum from this region is "
operator|+
operator|(
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|getEnvironment
argument_list|()
operator|)
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|": "
operator|+
name|min
argument_list|)
expr_stmt|;
return|return
name|min
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|,
name|S
parameter_list|>
name|S
name|getSum
parameter_list|(
name|ColumnInterpreter
argument_list|<
name|T
argument_list|,
name|S
argument_list|>
name|ci
parameter_list|,
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|sum
init|=
literal|0l
decl_stmt|;
name|S
name|sumVal
init|=
literal|null
decl_stmt|;
name|T
name|temp
decl_stmt|;
name|InternalScanner
name|scanner
init|=
operator|(
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|getEnvironment
argument_list|()
operator|)
operator|.
name|getRegion
argument_list|()
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|byte
index|[]
name|colFamily
init|=
name|scan
operator|.
name|getFamilies
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|scan
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|get
argument_list|(
name|colFamily
argument_list|)
operator|.
name|pollFirst
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
try|try
block|{
name|boolean
name|hasMoreRows
init|=
literal|false
decl_stmt|;
do|do
block|{
name|hasMoreRows
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|results
control|)
block|{
name|temp
operator|=
name|ci
operator|.
name|getValue
argument_list|(
name|colFamily
argument_list|,
name|qualifier
argument_list|,
name|kv
argument_list|)
expr_stmt|;
if|if
condition|(
name|temp
operator|!=
literal|null
condition|)
name|sumVal
operator|=
name|ci
operator|.
name|add
argument_list|(
name|sumVal
argument_list|,
name|ci
operator|.
name|castToReturnType
argument_list|(
name|temp
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|hasMoreRows
condition|)
do|;
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|log
operator|.
name|debug
argument_list|(
literal|"Sum from this region is "
operator|+
operator|(
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|getEnvironment
argument_list|()
operator|)
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|": "
operator|+
name|sum
argument_list|)
expr_stmt|;
return|return
name|sumVal
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|,
name|S
parameter_list|>
name|long
name|getRowNum
parameter_list|(
name|ColumnInterpreter
argument_list|<
name|T
argument_list|,
name|S
argument_list|>
name|ci
parameter_list|,
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
name|long
name|counter
init|=
literal|0l
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|byte
index|[]
name|colFamily
init|=
name|scan
operator|.
name|getFamilies
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|scan
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|get
argument_list|(
name|colFamily
argument_list|)
operator|.
name|pollFirst
argument_list|()
decl_stmt|;
if|if
condition|(
name|scan
operator|.
name|getFilter
argument_list|()
operator|==
literal|null
operator|&&
name|qualifier
operator|==
literal|null
condition|)
name|scan
operator|.
name|setFilter
argument_list|(
operator|new
name|FirstKeyOnlyFilter
argument_list|()
argument_list|)
expr_stmt|;
name|InternalScanner
name|scanner
init|=
operator|(
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|getEnvironment
argument_list|()
operator|)
operator|.
name|getRegion
argument_list|()
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
try|try
block|{
name|boolean
name|hasMoreRows
init|=
literal|false
decl_stmt|;
do|do
block|{
name|hasMoreRows
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
expr_stmt|;
if|if
condition|(
name|results
operator|.
name|size
argument_list|()
operator|>
literal|0
condition|)
block|{
name|counter
operator|++
expr_stmt|;
block|}
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|hasMoreRows
condition|)
do|;
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|log
operator|.
name|info
argument_list|(
literal|"Row counter from this region is "
operator|+
operator|(
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|getEnvironment
argument_list|()
operator|)
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|": "
operator|+
name|counter
argument_list|)
expr_stmt|;
return|return
name|counter
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|,
name|S
parameter_list|>
name|Pair
argument_list|<
name|S
argument_list|,
name|Long
argument_list|>
name|getAvg
parameter_list|(
name|ColumnInterpreter
argument_list|<
name|T
argument_list|,
name|S
argument_list|>
name|ci
parameter_list|,
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
name|S
name|sumVal
init|=
literal|null
decl_stmt|;
name|Long
name|rowCountVal
init|=
literal|0l
decl_stmt|;
name|InternalScanner
name|scanner
init|=
operator|(
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|getEnvironment
argument_list|()
operator|)
operator|.
name|getRegion
argument_list|()
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|byte
index|[]
name|colFamily
init|=
name|scan
operator|.
name|getFamilies
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|scan
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|get
argument_list|(
name|colFamily
argument_list|)
operator|.
name|pollFirst
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|hasMoreRows
init|=
literal|false
decl_stmt|;
try|try
block|{
do|do
block|{
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
name|hasMoreRows
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|results
control|)
block|{
name|sumVal
operator|=
name|ci
operator|.
name|add
argument_list|(
name|sumVal
argument_list|,
name|ci
operator|.
name|castToReturnType
argument_list|(
name|ci
operator|.
name|getValue
argument_list|(
name|colFamily
argument_list|,
name|qualifier
argument_list|,
name|kv
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|rowCountVal
operator|++
expr_stmt|;
block|}
do|while
condition|(
name|hasMoreRows
condition|)
do|;
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|Pair
argument_list|<
name|S
argument_list|,
name|Long
argument_list|>
name|pair
init|=
operator|new
name|Pair
argument_list|<
name|S
argument_list|,
name|Long
argument_list|>
argument_list|(
name|sumVal
argument_list|,
name|rowCountVal
argument_list|)
decl_stmt|;
return|return
name|pair
return|;
block|}
annotation|@
name|Override
specifier|public
parameter_list|<
name|T
parameter_list|,
name|S
parameter_list|>
name|Pair
argument_list|<
name|List
argument_list|<
name|S
argument_list|>
argument_list|,
name|Long
argument_list|>
name|getStd
parameter_list|(
name|ColumnInterpreter
argument_list|<
name|T
argument_list|,
name|S
argument_list|>
name|ci
parameter_list|,
name|Scan
name|scan
parameter_list|)
throws|throws
name|IOException
block|{
name|S
name|sumVal
init|=
literal|null
decl_stmt|,
name|sumSqVal
init|=
literal|null
decl_stmt|,
name|tempVal
init|=
literal|null
decl_stmt|;
name|long
name|rowCountVal
init|=
literal|0l
decl_stmt|;
name|InternalScanner
name|scanner
init|=
operator|(
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|getEnvironment
argument_list|()
operator|)
operator|.
name|getRegion
argument_list|()
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
decl_stmt|;
name|byte
index|[]
name|colFamily
init|=
name|scan
operator|.
name|getFamilies
argument_list|()
index|[
literal|0
index|]
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|scan
operator|.
name|getFamilyMap
argument_list|()
operator|.
name|get
argument_list|(
name|colFamily
argument_list|)
operator|.
name|pollFirst
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|KeyValue
argument_list|>
name|results
init|=
operator|new
name|ArrayList
argument_list|<
name|KeyValue
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|hasMoreRows
init|=
literal|false
decl_stmt|;
try|try
block|{
do|do
block|{
name|tempVal
operator|=
literal|null
expr_stmt|;
name|hasMoreRows
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|results
argument_list|)
expr_stmt|;
for|for
control|(
name|KeyValue
name|kv
range|:
name|results
control|)
block|{
name|tempVal
operator|=
name|ci
operator|.
name|add
argument_list|(
name|tempVal
argument_list|,
name|ci
operator|.
name|castToReturnType
argument_list|(
name|ci
operator|.
name|getValue
argument_list|(
name|colFamily
argument_list|,
name|qualifier
argument_list|,
name|kv
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|results
operator|.
name|clear
argument_list|()
expr_stmt|;
name|sumVal
operator|=
name|ci
operator|.
name|add
argument_list|(
name|sumVal
argument_list|,
name|tempVal
argument_list|)
expr_stmt|;
name|sumSqVal
operator|=
name|ci
operator|.
name|add
argument_list|(
name|sumSqVal
argument_list|,
name|ci
operator|.
name|multiply
argument_list|(
name|tempVal
argument_list|,
name|tempVal
argument_list|)
argument_list|)
expr_stmt|;
name|rowCountVal
operator|++
expr_stmt|;
block|}
do|while
condition|(
name|hasMoreRows
condition|)
do|;
block|}
finally|finally
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
name|List
argument_list|<
name|S
argument_list|>
name|l
init|=
operator|new
name|ArrayList
argument_list|<
name|S
argument_list|>
argument_list|()
decl_stmt|;
name|l
operator|.
name|add
argument_list|(
name|sumVal
argument_list|)
expr_stmt|;
name|l
operator|.
name|add
argument_list|(
name|sumSqVal
argument_list|)
expr_stmt|;
name|Pair
argument_list|<
name|List
argument_list|<
name|S
argument_list|>
argument_list|,
name|Long
argument_list|>
name|p
init|=
operator|new
name|Pair
argument_list|<
name|List
argument_list|<
name|S
argument_list|>
argument_list|,
name|Long
argument_list|>
argument_list|(
name|l
argument_list|,
name|rowCountVal
argument_list|)
decl_stmt|;
return|return
name|p
return|;
block|}
block|}
end_class

end_unit

