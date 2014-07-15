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
name|regionserver
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
name|fs
operator|.
name|Path
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
name|monitoring
operator|.
name|MonitoredTask
import|;
end_import

begin_comment
comment|/**  * A package protected interface for a store flushing.  * A store flush context carries the state required to prepare/flush/commit the store's cache.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
interface|interface
name|StoreFlushContext
block|{
comment|/**    * Prepare for a store flush (create snapshot)    *    * Requires pausing writes.    *    * A very short operation.    */
name|void
name|prepare
parameter_list|()
function_decl|;
comment|/**    * Flush the cache (create the new store file)    *    * A length operation which doesn't require locking out any function    * of the store.    *    * @throws IOException in case the flush fails    */
name|void
name|flushCache
parameter_list|(
name|MonitoredTask
name|status
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Commit the flush - add the store file to the store and clear the    * memstore snapshot.    *    * Requires pausing scans.    *    * A very short operation    *    * @return    * @throws IOException    */
name|boolean
name|commit
parameter_list|(
name|MonitoredTask
name|status
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Returns the newly committed files from the flush. Called only if commit returns true    * @return a list of Paths for new files    */
name|List
argument_list|<
name|Path
argument_list|>
name|getCommittedFiles
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

