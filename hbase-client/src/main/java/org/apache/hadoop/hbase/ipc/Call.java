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
name|ipc
package|;
end_package

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
name|io
operator|.
name|netty
operator|.
name|util
operator|.
name|Timeout
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
name|Descriptors
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
name|Message
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
name|RpcCallback
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
name|ProtobufUtil
import|;
end_import

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
name|CellScanner
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
name|client
operator|.
name|MetricsConnection
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
name|EnvironmentEdgeManager
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|htrace
operator|.
name|Span
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|htrace
operator|.
name|Trace
import|;
end_import

begin_comment
comment|/** A call waiting for a value. */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|Call
block|{
specifier|final
name|int
name|id
decl_stmt|;
comment|// call id
specifier|final
name|Message
name|param
decl_stmt|;
comment|// rpc request method param object
comment|/**    * Optionally has cells when making call. Optionally has cells set on response. Used passing cells    * to the rpc and receiving the response.    */
name|CellScanner
name|cells
decl_stmt|;
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"IS2_INCONSISTENT_SYNC"
argument_list|,
name|justification
operator|=
literal|"Direct access is only allowed after done"
argument_list|)
name|Message
name|response
decl_stmt|;
comment|// value, null if error
comment|// The return type. Used to create shell into which we deserialize the response if any.
name|Message
name|responseDefaultType
decl_stmt|;
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"IS2_INCONSISTENT_SYNC"
argument_list|,
name|justification
operator|=
literal|"Direct access is only allowed after done"
argument_list|)
name|IOException
name|error
decl_stmt|;
comment|// exception, null if value
specifier|private
name|boolean
name|done
decl_stmt|;
comment|// true when call is done
specifier|final
name|Descriptors
operator|.
name|MethodDescriptor
name|md
decl_stmt|;
specifier|final
name|int
name|timeout
decl_stmt|;
comment|// timeout in millisecond for this call; 0 means infinite.
specifier|final
name|int
name|priority
decl_stmt|;
specifier|final
name|MetricsConnection
operator|.
name|CallStats
name|callStats
decl_stmt|;
specifier|final
name|RpcCallback
argument_list|<
name|Call
argument_list|>
name|callback
decl_stmt|;
specifier|final
name|Span
name|span
decl_stmt|;
name|Timeout
name|timeoutTask
decl_stmt|;
specifier|protected
name|Call
parameter_list|(
name|int
name|id
parameter_list|,
specifier|final
name|Descriptors
operator|.
name|MethodDescriptor
name|md
parameter_list|,
name|Message
name|param
parameter_list|,
specifier|final
name|CellScanner
name|cells
parameter_list|,
specifier|final
name|Message
name|responseDefaultType
parameter_list|,
name|int
name|timeout
parameter_list|,
name|int
name|priority
parameter_list|,
name|RpcCallback
argument_list|<
name|Call
argument_list|>
name|callback
parameter_list|,
name|MetricsConnection
operator|.
name|CallStats
name|callStats
parameter_list|)
block|{
name|this
operator|.
name|param
operator|=
name|param
expr_stmt|;
name|this
operator|.
name|md
operator|=
name|md
expr_stmt|;
name|this
operator|.
name|cells
operator|=
name|cells
expr_stmt|;
name|this
operator|.
name|callStats
operator|=
name|callStats
expr_stmt|;
name|this
operator|.
name|callStats
operator|.
name|setStartTime
argument_list|(
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
argument_list|)
expr_stmt|;
name|this
operator|.
name|responseDefaultType
operator|=
name|responseDefaultType
expr_stmt|;
name|this
operator|.
name|id
operator|=
name|id
expr_stmt|;
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
name|this
operator|.
name|priority
operator|=
name|priority
expr_stmt|;
name|this
operator|.
name|callback
operator|=
name|callback
expr_stmt|;
name|this
operator|.
name|span
operator|=
name|Trace
operator|.
name|currentSpan
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"callId: "
operator|+
name|this
operator|.
name|id
operator|+
literal|" methodName: "
operator|+
name|this
operator|.
name|md
operator|.
name|getName
argument_list|()
operator|+
literal|" param {"
operator|+
operator|(
name|this
operator|.
name|param
operator|!=
literal|null
condition|?
name|ProtobufUtil
operator|.
name|getShortTextFormat
argument_list|(
name|this
operator|.
name|param
argument_list|)
else|:
literal|""
operator|)
operator|+
literal|"}"
return|;
block|}
comment|/**    * called from timeoutTask, prevent self cancel    */
specifier|public
name|void
name|setTimeout
parameter_list|(
name|IOException
name|error
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|done
condition|)
block|{
return|return;
block|}
name|this
operator|.
name|done
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|error
operator|=
name|error
expr_stmt|;
block|}
name|callback
operator|.
name|run
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|callComplete
parameter_list|()
block|{
if|if
condition|(
name|timeoutTask
operator|!=
literal|null
condition|)
block|{
name|timeoutTask
operator|.
name|cancel
argument_list|()
expr_stmt|;
block|}
name|callback
operator|.
name|run
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the exception when there is an error. Notify the caller the call is done.    * @param error exception thrown by the call; either local or remote    */
specifier|public
name|void
name|setException
parameter_list|(
name|IOException
name|error
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|done
condition|)
block|{
return|return;
block|}
name|this
operator|.
name|done
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|error
operator|=
name|error
expr_stmt|;
block|}
name|callComplete
argument_list|()
expr_stmt|;
block|}
comment|/**    * Set the return value when there is no error. Notify the caller the call is done.    * @param response return value of the call.    * @param cells Can be null    */
specifier|public
name|void
name|setResponse
parameter_list|(
name|Message
name|response
parameter_list|,
specifier|final
name|CellScanner
name|cells
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
if|if
condition|(
name|done
condition|)
block|{
return|return;
block|}
name|this
operator|.
name|done
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|response
operator|=
name|response
expr_stmt|;
name|this
operator|.
name|cells
operator|=
name|cells
expr_stmt|;
block|}
name|callComplete
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|synchronized
name|boolean
name|isDone
parameter_list|()
block|{
return|return
name|done
return|;
block|}
specifier|public
name|long
name|getStartTime
parameter_list|()
block|{
return|return
name|this
operator|.
name|callStats
operator|.
name|getStartTime
argument_list|()
return|;
block|}
block|}
end_class

end_unit

