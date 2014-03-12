begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Coprocessor
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
name|CoprocessorEnvironment
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
name|HConstants
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
name|coprocessor
operator|.
name|protobuf
operator|.
name|generated
operator|.
name|ColumnAggregationWithNullResponseProtos
operator|.
name|ColumnAggregationServiceNullResponse
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
name|protobuf
operator|.
name|generated
operator|.
name|ColumnAggregationWithNullResponseProtos
operator|.
name|SumRequest
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
name|protobuf
operator|.
name|generated
operator|.
name|ColumnAggregationWithNullResponseProtos
operator|.
name|SumResponse
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
name|ResponseConverter
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
name|HRegion
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
name|Bytes
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
name|RpcCallback
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
name|RpcController
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
name|Service
import|;
end_import

begin_comment
comment|/**  * Test coprocessor endpoint that always returns {@code null} for requests to the last region  * in the table.  This allows tests to provide assurance of correct {@code null} handling for  * response values.  */
end_comment

begin_class
specifier|public
class|class
name|ColumnAggregationEndpointNullResponse
extends|extends
name|ColumnAggregationServiceNullResponse
implements|implements
name|Coprocessor
implements|,
name|CoprocessorService
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
name|ColumnAggregationEndpointNullResponse
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|RegionCoprocessorEnvironment
name|env
init|=
literal|null
decl_stmt|;
annotation|@
name|Override
specifier|public
name|Service
name|getService
parameter_list|()
block|{
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|env
operator|instanceof
name|RegionCoprocessorEnvironment
condition|)
block|{
name|this
operator|.
name|env
operator|=
operator|(
name|RegionCoprocessorEnvironment
operator|)
name|env
expr_stmt|;
return|return;
block|}
throw|throw
operator|new
name|CoprocessorException
argument_list|(
literal|"Must be loaded on a table region!"
argument_list|)
throw|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|CoprocessorEnvironment
name|env
parameter_list|)
throws|throws
name|IOException
block|{
comment|// Nothing to do.
block|}
annotation|@
name|Override
specifier|public
name|void
name|sum
parameter_list|(
name|RpcController
name|controller
parameter_list|,
name|SumRequest
name|request
parameter_list|,
name|RpcCallback
argument_list|<
name|SumResponse
argument_list|>
name|done
parameter_list|)
block|{
comment|// aggregate at each region
name|Scan
name|scan
init|=
operator|new
name|Scan
argument_list|()
decl_stmt|;
comment|// Family is required in pb. Qualifier is not.
name|byte
index|[]
name|family
init|=
name|request
operator|.
name|getFamily
argument_list|()
operator|.
name|toByteArray
argument_list|()
decl_stmt|;
name|byte
index|[]
name|qualifier
init|=
name|request
operator|.
name|hasQualifier
argument_list|()
condition|?
name|request
operator|.
name|getQualifier
argument_list|()
operator|.
name|toByteArray
argument_list|()
else|:
literal|null
decl_stmt|;
if|if
condition|(
name|request
operator|.
name|hasQualifier
argument_list|()
condition|)
block|{
name|scan
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|scan
operator|.
name|addFamily
argument_list|(
name|family
argument_list|)
expr_stmt|;
block|}
name|int
name|sumResult
init|=
literal|0
decl_stmt|;
name|InternalScanner
name|scanner
init|=
literal|null
decl_stmt|;
try|try
block|{
name|HRegion
name|region
init|=
name|this
operator|.
name|env
operator|.
name|getRegion
argument_list|()
decl_stmt|;
comment|// for the last region in the table, return null to test null handling
if|if
condition|(
name|Bytes
operator|.
name|equals
argument_list|(
name|region
operator|.
name|getEndKey
argument_list|()
argument_list|,
name|HConstants
operator|.
name|EMPTY_END_ROW
argument_list|)
condition|)
block|{
name|done
operator|.
name|run
argument_list|(
literal|null
argument_list|)
expr_stmt|;
return|return;
block|}
name|scanner
operator|=
name|region
operator|.
name|getScanner
argument_list|(
name|scan
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|Cell
argument_list|>
name|curVals
init|=
operator|new
name|ArrayList
argument_list|<
name|Cell
argument_list|>
argument_list|()
decl_stmt|;
name|boolean
name|hasMore
init|=
literal|false
decl_stmt|;
do|do
block|{
name|curVals
operator|.
name|clear
argument_list|()
expr_stmt|;
name|hasMore
operator|=
name|scanner
operator|.
name|next
argument_list|(
name|curVals
argument_list|)
expr_stmt|;
for|for
control|(
name|Cell
name|kv
range|:
name|curVals
control|)
block|{
if|if
condition|(
name|CellUtil
operator|.
name|matchingQualifier
argument_list|(
name|kv
argument_list|,
name|qualifier
argument_list|)
condition|)
block|{
name|sumResult
operator|+=
name|Bytes
operator|.
name|toInt
argument_list|(
name|kv
operator|.
name|getValueArray
argument_list|()
argument_list|,
name|kv
operator|.
name|getValueOffset
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
block|}
do|while
condition|(
name|hasMore
condition|)
do|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|ResponseConverter
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// Set result to -1 to indicate error.
name|sumResult
operator|=
operator|-
literal|1
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting sum result to -1 to indicate error"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
if|if
condition|(
name|scanner
operator|!=
literal|null
condition|)
block|{
try|try
block|{
name|scanner
operator|.
name|close
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
name|ResponseConverter
operator|.
name|setControllerException
argument_list|(
name|controller
argument_list|,
name|e
argument_list|)
expr_stmt|;
name|sumResult
operator|=
operator|-
literal|1
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Setting sum result to -1 to indicate error"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
block|}
block|}
name|done
operator|.
name|run
argument_list|(
name|SumResponse
operator|.
name|newBuilder
argument_list|()
operator|.
name|setSum
argument_list|(
name|sumResult
argument_list|)
operator|.
name|build
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Returning sum "
operator|+
name|sumResult
operator|+
literal|" for region "
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|env
operator|.
name|getRegion
argument_list|()
operator|.
name|getRegionName
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

