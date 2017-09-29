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
operator|.
name|compactions
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
name|regionserver
operator|.
name|StoreFile
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
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_comment
comment|/**  * Coprocessors use this interface to get details about compaction.  */
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
specifier|public
interface|interface
name|CompactionRequest
block|{
comment|/**    * @return unmodifiable collection of StoreFiles in compaction    */
name|Collection
argument_list|<
name|?
extends|extends
name|StoreFile
argument_list|>
name|getFiles
parameter_list|()
function_decl|;
comment|/**    * @return total size of all StoreFiles in compaction    */
name|long
name|getSize
parameter_list|()
function_decl|;
comment|/**    * @return<code>true</code> if major compaction or all files are compacted    */
name|boolean
name|isAllFiles
parameter_list|()
function_decl|;
comment|/**    * @return<code>true</code> if major compaction    */
name|boolean
name|isMajor
parameter_list|()
function_decl|;
comment|/**    * @return priority of compaction request    */
name|int
name|getPriority
parameter_list|()
function_decl|;
comment|/**    * @return<code>true</code> if compaction is Off-peak    */
name|boolean
name|isOffPeak
parameter_list|()
function_decl|;
comment|/**    * @return compaction request creation time in milliseconds    */
name|long
name|getSelectionTime
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

