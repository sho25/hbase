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
comment|/**  * Carries the execution state for a given invocation of an Observer coprocessor  * ({@link RegionObserver}, {@link MasterObserver}, or {@link WALObserver})  * method. The same ObserverContext instance is passed sequentially to all loaded  * coprocessors for a given Observer method trigger, with the  *<code>CoprocessorEnvironment</code> reference set appropriately for each Coprocessor type:  * e.g. the RegionCoprocessorEnvironment is passed to RegionCoprocessors, and so on.  * @param<E> The {@link CoprocessorEnvironment} subclass applicable to the  *     revelant Observer interface.  */
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
comment|/**    * Call to indicate that the current coprocessor's return value (or parameter -- depends on the    * call-type) should be used in place of the value that would be obtained via normal processing;    * i.e. bypass the core call and return the Coprocessor's result instead. DOES NOT work for all    * Coprocessor invocations, only on a small subset of methods, mostly preXXX calls in    * RegionObserver. Check javadoc on the pertinent Coprocessor Observer to see if    *<code>bypass</code> is supported.    *<p>This behavior of honoring only a subset of methods is new since hbase-2.0.0.    *<p>Where bypass is supported what is being bypassed is all of the core code    * implementing the remainder of the operation. In order to understand what    * calling bypass() will skip, a coprocessor implementer should read and    * understand all of the remaining code and its nuances. Although this    * is good practice for coprocessor developers in general, it demands a lot.    * What is skipped is extremely version dependent. The core code will vary, perhaps significantly,    * even between point releases. We do not provide the promise of consistent behavior even between    * point releases for the bypass semantic. To achieve    * that we could not change any code between hook points. Therefore the    * coprocessor implementer becomes an HBase core developer in practice as soon    * as they rely on bypass(). Every release of HBase may break the assumption    * that the replacement for the bypassed code takes care of all necessary    * skipped concerns. Because those concerns can change at any point, such an    * assumption is never safe.</p>    */
name|void
name|bypass
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

