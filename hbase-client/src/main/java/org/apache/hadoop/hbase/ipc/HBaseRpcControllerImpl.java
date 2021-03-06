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
name|hbase
operator|.
name|thirdparty
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
name|hadoop
operator|.
name|hbase
operator|.
name|CellScannable
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
comment|/**  * Optionally carries Cells across the proxy/service interface down into ipc. On its way out it  * optionally carries a set of result Cell data. We stick the Cells here when we want to avoid  * having to protobuf them (for performance reasons). This class is used ferrying data across the  * proxy/protobuf service chasm. Also does call timeout. Used by client and server ipc'ing.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HBaseRpcControllerImpl
implements|implements
name|HBaseRpcController
block|{
comment|/**    * The time, in ms before the call should expire.    */
specifier|private
name|Integer
name|callTimeout
decl_stmt|;
specifier|private
name|boolean
name|done
init|=
literal|false
decl_stmt|;
specifier|private
name|boolean
name|cancelled
init|=
literal|false
decl_stmt|;
specifier|private
specifier|final
name|List
argument_list|<
name|RpcCallback
argument_list|<
name|Object
argument_list|>
argument_list|>
name|cancellationCbs
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|()
decl_stmt|;
specifier|private
name|IOException
name|exception
decl_stmt|;
comment|/**    * Priority to set on this request. Set it here in controller so available composing the request.    * This is the ordained way of setting priorities going forward. We will be undoing the old    * annotation-based mechanism.    */
specifier|private
name|int
name|priority
init|=
name|HConstants
operator|.
name|PRIORITY_UNSET
decl_stmt|;
comment|/**    * They are optionally set on construction, cleared after we make the call, and then optionally    * set on response with the result. We use this lowest common denominator access to Cells because    * sometimes the scanner is backed by a List of Cells and other times, it is backed by an encoded    * block that implements CellScanner.    */
specifier|private
name|CellScanner
name|cellScanner
decl_stmt|;
specifier|public
name|HBaseRpcControllerImpl
parameter_list|()
block|{
name|this
argument_list|(
operator|(
name|CellScanner
operator|)
literal|null
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HBaseRpcControllerImpl
parameter_list|(
specifier|final
name|CellScanner
name|cellScanner
parameter_list|)
block|{
name|this
operator|.
name|cellScanner
operator|=
name|cellScanner
expr_stmt|;
block|}
specifier|public
name|HBaseRpcControllerImpl
parameter_list|(
specifier|final
name|List
argument_list|<
name|CellScannable
argument_list|>
name|cellIterables
parameter_list|)
block|{
name|this
operator|.
name|cellScanner
operator|=
name|cellIterables
operator|==
literal|null
condition|?
literal|null
else|:
name|CellUtil
operator|.
name|createCellScanner
argument_list|(
name|cellIterables
argument_list|)
expr_stmt|;
block|}
comment|/**    * @return One-shot cell scanner (you cannot back it up and restart)    */
annotation|@
name|Override
specifier|public
name|CellScanner
name|cellScanner
parameter_list|()
block|{
return|return
name|cellScanner
return|;
block|}
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
literal|"The only possible race method is startCancel"
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|setCellScanner
parameter_list|(
specifier|final
name|CellScanner
name|cellScanner
parameter_list|)
block|{
name|this
operator|.
name|cellScanner
operator|=
name|cellScanner
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setPriority
parameter_list|(
name|int
name|priority
parameter_list|)
block|{
name|this
operator|.
name|priority
operator|=
name|Math
operator|.
name|max
argument_list|(
name|this
operator|.
name|priority
argument_list|,
name|priority
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setPriority
parameter_list|(
specifier|final
name|TableName
name|tn
parameter_list|)
block|{
name|setPriority
argument_list|(
name|tn
operator|!=
literal|null
operator|&&
name|tn
operator|.
name|isSystemTable
argument_list|()
condition|?
name|HConstants
operator|.
name|SYSTEMTABLE_QOS
else|:
name|HConstants
operator|.
name|NORMAL_QOS
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getPriority
parameter_list|()
block|{
return|return
name|priority
operator|<
literal|0
condition|?
name|HConstants
operator|.
name|NORMAL_QOS
else|:
name|priority
return|;
block|}
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
literal|"The only possible race method is startCancel"
argument_list|)
annotation|@
name|Override
specifier|public
name|void
name|reset
parameter_list|()
block|{
name|priority
operator|=
literal|0
expr_stmt|;
name|cellScanner
operator|=
literal|null
expr_stmt|;
name|exception
operator|=
literal|null
expr_stmt|;
name|callTimeout
operator|=
literal|null
expr_stmt|;
comment|// In the implementations of some callable with replicas, rpc calls are executed in a executor
comment|// and we could cancel the operation from outside which means there could be a race between
comment|// reset and startCancel. Although I think the race should be handled by the callable since the
comment|// reset may clear the cancel state...
synchronized|synchronized
init|(
name|this
init|)
block|{
name|done
operator|=
literal|false
expr_stmt|;
name|cancelled
operator|=
literal|false
expr_stmt|;
name|cancellationCbs
operator|.
name|clear
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|int
name|getCallTimeout
parameter_list|()
block|{
if|if
condition|(
name|callTimeout
operator|!=
literal|null
condition|)
block|{
return|return
name|callTimeout
operator|.
name|intValue
argument_list|()
return|;
block|}
else|else
block|{
return|return
literal|0
return|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|setCallTimeout
parameter_list|(
name|int
name|callTimeout
parameter_list|)
block|{
name|this
operator|.
name|callTimeout
operator|=
name|callTimeout
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|hasCallTimeout
parameter_list|()
block|{
return|return
name|callTimeout
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|String
name|errorText
parameter_list|()
block|{
if|if
condition|(
operator|!
name|done
operator|||
name|exception
operator|==
literal|null
condition|)
block|{
return|return
literal|null
return|;
block|}
return|return
name|exception
operator|.
name|getMessage
argument_list|()
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|boolean
name|failed
parameter_list|()
block|{
return|return
name|done
operator|&&
name|this
operator|.
name|exception
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|boolean
name|isCanceled
parameter_list|()
block|{
return|return
name|cancelled
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|notifyOnCancel
parameter_list|(
name|RpcCallback
argument_list|<
name|Object
argument_list|>
name|callback
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
if|if
condition|(
operator|!
name|cancelled
condition|)
block|{
name|cancellationCbs
operator|.
name|add
argument_list|(
name|callback
argument_list|)
expr_stmt|;
return|return;
block|}
block|}
comment|// run it directly as we have already been cancelled.
name|callback
operator|.
name|run
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|setFailed
parameter_list|(
name|String
name|reason
parameter_list|)
block|{
if|if
condition|(
name|done
condition|)
block|{
return|return;
block|}
name|done
operator|=
literal|true
expr_stmt|;
name|exception
operator|=
operator|new
name|IOException
argument_list|(
name|reason
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|setFailed
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
if|if
condition|(
name|done
condition|)
block|{
return|return;
block|}
name|done
operator|=
literal|true
expr_stmt|;
name|exception
operator|=
name|e
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|IOException
name|getFailed
parameter_list|()
block|{
return|return
name|done
condition|?
name|exception
else|:
literal|null
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|setDone
parameter_list|(
name|CellScanner
name|cellScanner
parameter_list|)
block|{
if|if
condition|(
name|done
condition|)
block|{
return|return;
block|}
name|done
operator|=
literal|true
expr_stmt|;
name|this
operator|.
name|cellScanner
operator|=
name|cellScanner
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|startCancel
parameter_list|()
block|{
comment|// As said above in the comment of reset, the cancellationCbs maybe cleared by reset, so we need
comment|// to copy it.
name|List
argument_list|<
name|RpcCallback
argument_list|<
name|Object
argument_list|>
argument_list|>
name|cbs
decl_stmt|;
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
name|done
operator|=
literal|true
expr_stmt|;
name|cancelled
operator|=
literal|true
expr_stmt|;
name|cbs
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|cancellationCbs
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|RpcCallback
argument_list|<
name|?
argument_list|>
name|cb
range|:
name|cbs
control|)
block|{
name|cb
operator|.
name|run
argument_list|(
literal|null
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|notifyOnCancel
parameter_list|(
name|RpcCallback
argument_list|<
name|Object
argument_list|>
name|callback
parameter_list|,
name|CancellationCallback
name|action
parameter_list|)
throws|throws
name|IOException
block|{
if|if
condition|(
name|cancelled
condition|)
block|{
name|action
operator|.
name|run
argument_list|(
literal|true
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cancellationCbs
operator|.
name|add
argument_list|(
name|callback
argument_list|)
expr_stmt|;
name|action
operator|.
name|run
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

