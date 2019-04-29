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
name|client
package|;
end_package

begin_import
import|import static
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
name|ConnectionUtils
operator|.
name|retries2Attempts
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|TimeUnit
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
name|TableName
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

begin_comment
comment|/**  * Base class for all asynchronous table builders.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|abstract
class|class
name|AsyncTableBuilderBase
parameter_list|<
name|C
extends|extends
name|ScanResultConsumerBase
parameter_list|>
implements|implements
name|AsyncTableBuilder
argument_list|<
name|C
argument_list|>
block|{
specifier|protected
name|TableName
name|tableName
decl_stmt|;
specifier|protected
name|long
name|operationTimeoutNs
decl_stmt|;
specifier|protected
name|long
name|scanTimeoutNs
decl_stmt|;
specifier|protected
name|long
name|rpcTimeoutNs
decl_stmt|;
specifier|protected
name|long
name|readRpcTimeoutNs
decl_stmt|;
specifier|protected
name|long
name|writeRpcTimeoutNs
decl_stmt|;
specifier|protected
name|long
name|pauseNs
decl_stmt|;
specifier|protected
name|long
name|pauseForCQTBENs
decl_stmt|;
specifier|protected
name|int
name|maxAttempts
decl_stmt|;
specifier|protected
name|int
name|startLogErrorsCnt
decl_stmt|;
name|AsyncTableBuilderBase
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|AsyncConnectionConfiguration
name|connConf
parameter_list|)
block|{
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|operationTimeoutNs
operator|=
name|tableName
operator|.
name|isSystemTable
argument_list|()
condition|?
name|connConf
operator|.
name|getMetaOperationTimeoutNs
argument_list|()
else|:
name|connConf
operator|.
name|getOperationTimeoutNs
argument_list|()
expr_stmt|;
name|this
operator|.
name|scanTimeoutNs
operator|=
name|connConf
operator|.
name|getScanTimeoutNs
argument_list|()
expr_stmt|;
name|this
operator|.
name|rpcTimeoutNs
operator|=
name|connConf
operator|.
name|getRpcTimeoutNs
argument_list|()
expr_stmt|;
name|this
operator|.
name|readRpcTimeoutNs
operator|=
name|connConf
operator|.
name|getReadRpcTimeoutNs
argument_list|()
expr_stmt|;
name|this
operator|.
name|writeRpcTimeoutNs
operator|=
name|connConf
operator|.
name|getWriteRpcTimeoutNs
argument_list|()
expr_stmt|;
name|this
operator|.
name|pauseNs
operator|=
name|connConf
operator|.
name|getPauseNs
argument_list|()
expr_stmt|;
name|this
operator|.
name|pauseForCQTBENs
operator|=
name|connConf
operator|.
name|getPauseForCQTBENs
argument_list|()
expr_stmt|;
name|this
operator|.
name|maxAttempts
operator|=
name|retries2Attempts
argument_list|(
name|connConf
operator|.
name|getMaxRetries
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|startLogErrorsCnt
operator|=
name|connConf
operator|.
name|getStartLogErrorsCnt
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncTableBuilderBase
argument_list|<
name|C
argument_list|>
name|setOperationTimeout
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|operationTimeoutNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncTableBuilderBase
argument_list|<
name|C
argument_list|>
name|setScanTimeout
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|scanTimeoutNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncTableBuilderBase
argument_list|<
name|C
argument_list|>
name|setRpcTimeout
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|rpcTimeoutNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncTableBuilderBase
argument_list|<
name|C
argument_list|>
name|setReadRpcTimeout
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|readRpcTimeoutNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncTableBuilderBase
argument_list|<
name|C
argument_list|>
name|setWriteRpcTimeout
parameter_list|(
name|long
name|timeout
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|writeRpcTimeoutNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncTableBuilderBase
argument_list|<
name|C
argument_list|>
name|setRetryPause
parameter_list|(
name|long
name|pause
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|pauseNs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|pause
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncTableBuilderBase
argument_list|<
name|C
argument_list|>
name|setRetryPauseForCQTBE
parameter_list|(
name|long
name|pause
parameter_list|,
name|TimeUnit
name|unit
parameter_list|)
block|{
name|this
operator|.
name|pauseForCQTBENs
operator|=
name|unit
operator|.
name|toNanos
argument_list|(
name|pause
argument_list|)
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncTableBuilderBase
argument_list|<
name|C
argument_list|>
name|setMaxAttempts
parameter_list|(
name|int
name|maxAttempts
parameter_list|)
block|{
name|this
operator|.
name|maxAttempts
operator|=
name|maxAttempts
expr_stmt|;
return|return
name|this
return|;
block|}
annotation|@
name|Override
specifier|public
name|AsyncTableBuilderBase
argument_list|<
name|C
argument_list|>
name|setStartLogErrorsCnt
parameter_list|(
name|int
name|startLogErrorsCnt
parameter_list|)
block|{
name|this
operator|.
name|startLogErrorsCnt
operator|=
name|startLogErrorsCnt
expr_stmt|;
return|return
name|this
return|;
block|}
block|}
end_class

end_unit

