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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Optional
import|;
end_import

begin_comment
comment|/**  * Carries the execution state for a given invocation of an Observer coprocessor  * ({@link RegionObserver}, {@link MasterObserver}, or {@link WALObserver})  * method.  The same ObserverContext instance is passed sequentially to all loaded  * coprocessors for a given Observer method trigger, with the  *<code>CoprocessorEnvironment</code> reference swapped out for each  * coprocessor.  * @param<E> The {@link CoprocessorEnvironment} subclass applicable to the  *     revelant Observer interface.  */
end_comment

begin_interface
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
interface|interface
name|ObserverContext
parameter_list|<
name|E
extends|extends
name|CoprocessorEnvironment
parameter_list|>
block|{
name|E
name|getEnvironment
parameter_list|()
function_decl|;
comment|/**    * Call to indicate that the current coprocessor's return value should be    * used in place of the normal HBase obtained value.    */
name|void
name|bypass
parameter_list|()
function_decl|;
comment|/**    * Call to indicate that additional coprocessors further down the execution    * chain do not need to be invoked.  Implies that this coprocessor's response    * is definitive.    */
name|void
name|complete
parameter_list|()
function_decl|;
comment|/**    * Returns the active user for the coprocessor call. If an explicit {@code User} instance was    * provided to the constructor, that will be returned, otherwise if we are in the context of an    * RPC call, the remote user is used. May not be present if the execution is outside of an RPC    * context.    */
name|Optional
argument_list|<
name|User
argument_list|>
name|getCaller
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

