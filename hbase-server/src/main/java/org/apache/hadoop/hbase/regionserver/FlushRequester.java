begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
package|;
end_package

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
comment|/**  * Request a flush.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|FlushRequester
block|{
comment|/**    * Tell the listener the cache needs to be flushed.    *    * @param region the Region requesting the cache flush    * @param forceFlushAllStores whether we want to flush all stores. e.g., when request from log    *          rolling.    * @return true if our region is added into the queue, false otherwise    */
name|boolean
name|requestFlush
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|boolean
name|forceFlushAllStores
parameter_list|,
name|FlushLifeCycleTracker
name|tracker
parameter_list|)
function_decl|;
comment|/**    * Tell the listener the cache needs to be flushed after a delay    *    * @param region the Region requesting the cache flush    * @param delay after how much time should the flush happen    * @param forceFlushAllStores whether we want to flush all stores. e.g., when request from log    *          rolling.    * @return true if our region is added into the queue, false otherwise    */
name|boolean
name|requestDelayedFlush
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|long
name|delay
parameter_list|,
name|boolean
name|forceFlushAllStores
parameter_list|)
function_decl|;
comment|/**    * Register a FlushRequestListener    *    * @param listener    */
name|void
name|registerFlushRequestListener
parameter_list|(
specifier|final
name|FlushRequestListener
name|listener
parameter_list|)
function_decl|;
comment|/**    * Unregister the given FlushRequestListener    *    * @param listener    * @return true when passed listener is unregistered successfully.    */
specifier|public
name|boolean
name|unregisterFlushRequestListener
parameter_list|(
specifier|final
name|FlushRequestListener
name|listener
parameter_list|)
function_decl|;
comment|/**    * Sets the global memstore limit to a new size.    *    * @param globalMemStoreSize    */
specifier|public
name|void
name|setGlobalMemStoreLimit
parameter_list|(
name|long
name|globalMemStoreSize
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

