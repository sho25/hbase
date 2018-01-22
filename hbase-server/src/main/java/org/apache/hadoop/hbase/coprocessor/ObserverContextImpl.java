begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|util
operator|.
name|Optional
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
name|ipc
operator|.
name|RpcServer
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
name|security
operator|.
name|User
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
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_comment
comment|/**  * This is the only implementation of {@link ObserverContext}, which serves as the interface for  * third-party Coprocessor developers.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|ObserverContextImpl
parameter_list|<
name|E
extends|extends
name|CoprocessorEnvironment
parameter_list|>
implements|implements
name|ObserverContext
argument_list|<
name|E
argument_list|>
block|{
specifier|private
name|E
name|env
decl_stmt|;
specifier|private
name|boolean
name|bypass
decl_stmt|;
comment|/**    * Is this operation bypassable?    */
specifier|private
specifier|final
name|boolean
name|bypassable
decl_stmt|;
specifier|private
specifier|final
name|User
name|caller
decl_stmt|;
specifier|public
name|ObserverContextImpl
parameter_list|(
name|User
name|caller
parameter_list|)
block|{
name|this
argument_list|(
name|caller
argument_list|,
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|public
name|ObserverContextImpl
parameter_list|(
name|User
name|caller
parameter_list|,
name|boolean
name|bypassable
parameter_list|)
block|{
name|this
operator|.
name|caller
operator|=
name|caller
expr_stmt|;
name|this
operator|.
name|bypassable
operator|=
name|bypassable
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|E
name|getEnvironment
parameter_list|()
block|{
return|return
name|env
return|;
block|}
specifier|public
name|void
name|prepare
parameter_list|(
name|E
name|env
parameter_list|)
block|{
name|this
operator|.
name|env
operator|=
name|env
expr_stmt|;
block|}
specifier|public
name|boolean
name|isBypassable
parameter_list|()
block|{
return|return
name|this
operator|.
name|bypassable
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|bypass
parameter_list|()
block|{
if|if
condition|(
operator|!
name|this
operator|.
name|bypassable
condition|)
block|{
throw|throw
operator|new
name|UnsupportedOperationException
argument_list|(
literal|"This method does not support 'bypass'."
argument_list|)
throw|;
block|}
name|bypass
operator|=
literal|true
expr_stmt|;
block|}
comment|/**    * @return {@code true}, if {@link ObserverContext#bypass()} was called by one of the loaded    * coprocessors, {@code false} otherwise.    */
specifier|public
name|boolean
name|shouldBypass
parameter_list|()
block|{
if|if
condition|(
operator|!
name|isBypassable
argument_list|()
condition|)
block|{
return|return
literal|false
return|;
block|}
if|if
condition|(
name|bypass
condition|)
block|{
name|bypass
operator|=
literal|false
expr_stmt|;
return|return
literal|true
return|;
block|}
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|Optional
argument_list|<
name|User
argument_list|>
name|getCaller
parameter_list|()
block|{
return|return
name|Optional
operator|.
name|ofNullable
argument_list|(
name|caller
argument_list|)
return|;
block|}
comment|/**    * Instantiates a new ObserverContext instance if the passed reference is<code>null</code> and    * sets the environment in the new or existing instance. This allows deferring the instantiation    * of a ObserverContext until it is actually needed.    * @param<E> The environment type for the context    * @param env The coprocessor environment to set    * @return An instance of<code>ObserverContext</code> with the environment set    */
annotation|@
name|Deprecated
annotation|@
name|VisibleForTesting
comment|// TODO: Remove this method, ObserverContext should not depend on RpcServer
specifier|public
specifier|static
parameter_list|<
name|E
extends|extends
name|CoprocessorEnvironment
parameter_list|>
name|ObserverContext
argument_list|<
name|E
argument_list|>
name|createAndPrepare
parameter_list|(
name|E
name|env
parameter_list|)
block|{
name|ObserverContextImpl
argument_list|<
name|E
argument_list|>
name|ctx
init|=
operator|new
name|ObserverContextImpl
argument_list|<>
argument_list|(
name|RpcServer
operator|.
name|getRequestUser
argument_list|()
operator|.
name|orElse
argument_list|(
literal|null
argument_list|)
argument_list|)
decl_stmt|;
name|ctx
operator|.
name|prepare
argument_list|(
name|env
argument_list|)
expr_stmt|;
return|return
name|ctx
return|;
block|}
block|}
end_class

end_unit

