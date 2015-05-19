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
name|errorhandling
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * The dispatcher acts as the state holding entity for foreign error handling.  The first  * exception received by the dispatcher get passed directly to the listeners.  Subsequent  * exceptions are dropped.  *<p>  * If there are multiple dispatchers that are all in the same foreign exception monitoring group,  * ideally all these monitors are "peers" -- any error on one dispatcher should get propagated to  * all others (via rpc, or some other mechanism).  Due to racing error conditions the exact reason  * for failure may be different on different peers, but the fact that they are in error state  * should eventually hold on all.  *<p>  * This is thread-safe and must be because this is expected to be used to propagate exceptions  * from foreign threads.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ForeignExceptionDispatcher
implements|implements
name|ForeignExceptionListener
implements|,
name|ForeignExceptionSnare
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|ForeignExceptionDispatcher
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|protected
specifier|final
name|String
name|name
decl_stmt|;
specifier|protected
specifier|final
name|List
argument_list|<
name|ForeignExceptionListener
argument_list|>
name|listeners
init|=
operator|new
name|ArrayList
argument_list|<
name|ForeignExceptionListener
argument_list|>
argument_list|()
decl_stmt|;
specifier|private
name|ForeignException
name|exception
decl_stmt|;
specifier|public
name|ForeignExceptionDispatcher
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|name
operator|=
name|name
expr_stmt|;
block|}
specifier|public
name|ForeignExceptionDispatcher
parameter_list|()
block|{
name|this
argument_list|(
literal|""
argument_list|)
expr_stmt|;
block|}
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|receive
parameter_list|(
name|ForeignException
name|e
parameter_list|)
block|{
comment|// if we already have an exception, then ignore it
if|if
condition|(
name|exception
operator|!=
literal|null
condition|)
return|return;
name|LOG
operator|.
name|debug
argument_list|(
name|name
operator|+
literal|" accepting received exception"
argument_list|,
name|e
argument_list|)
expr_stmt|;
comment|// mark that we got the error
if|if
condition|(
name|e
operator|!=
literal|null
condition|)
block|{
name|exception
operator|=
name|e
expr_stmt|;
block|}
else|else
block|{
name|exception
operator|=
operator|new
name|ForeignException
argument_list|(
name|name
argument_list|,
literal|""
argument_list|)
expr_stmt|;
block|}
comment|// notify all the listeners
name|dispatch
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|void
name|rethrowException
parameter_list|()
throws|throws
name|ForeignException
block|{
if|if
condition|(
name|exception
operator|!=
literal|null
condition|)
block|{
comment|// This gets the stack where this is caused, (instead of where it was deserialized).
comment|// This is much more useful for debugging
throw|throw
operator|new
name|ForeignException
argument_list|(
name|exception
operator|.
name|getSource
argument_list|()
argument_list|,
name|exception
operator|.
name|getCause
argument_list|()
argument_list|)
throw|;
block|}
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|boolean
name|hasException
parameter_list|()
block|{
return|return
name|exception
operator|!=
literal|null
return|;
block|}
annotation|@
name|Override
specifier|synchronized
specifier|public
name|ForeignException
name|getException
parameter_list|()
block|{
return|return
name|exception
return|;
block|}
comment|/**    * Sends an exception to all listeners.    * @param message human readable message passed to the listener    * @param e {@link ForeignException} containing the cause.  Can be null.    */
specifier|private
name|void
name|dispatch
parameter_list|(
name|ForeignException
name|e
parameter_list|)
block|{
comment|// update all the listeners with the passed error
for|for
control|(
name|ForeignExceptionListener
name|l
range|:
name|listeners
control|)
block|{
name|l
operator|.
name|receive
argument_list|(
name|e
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Listen for failures to a given process.  This method should only be used during    * initialization and not added to after exceptions are accepted.    * @param errorable listener for the errors.  may be null.    */
specifier|public
specifier|synchronized
name|void
name|addListener
parameter_list|(
name|ForeignExceptionListener
name|errorable
parameter_list|)
block|{
name|this
operator|.
name|listeners
operator|.
name|add
argument_list|(
name|errorable
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

