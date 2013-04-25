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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * A more restricted interface for HStore. Only gives the caller access to information  * about store configuration/settings that cannot easily be obtained from XML config object.  * Example user would be CompactionPolicy that doesn't need entire (H)Store, only this.  * Add things here as needed.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Unstable
specifier|public
interface|interface
name|StoreConfigInformation
block|{
comment|/**    * Gets the Memstore flush size for the region that this store works with.    * TODO: remove after HBASE-7236 is fixed.    */
specifier|public
name|long
name|getMemstoreFlushSize
parameter_list|()
function_decl|;
comment|/**    * Gets the cf-specific time-to-live for store files.    */
specifier|public
name|long
name|getStoreFileTtl
parameter_list|()
function_decl|;
comment|/**    * The number of files required before flushes for this store will be blocked.    */
specifier|public
name|long
name|getBlockingFileCount
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

