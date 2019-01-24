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
name|coordination
operator|.
name|SplitLogManagerCoordination
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
name|coordination
operator|.
name|SplitLogWorkerCoordination
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
comment|/**  * Implementations of this interface will keep and return to clients  * implementations of classes providing API to execute  * coordinated operations. This interface is client-side, so it does NOT  * include methods to retrieve the particular interface implementations.  *  * For each coarse-grained area of operations there will be a separate  * interface with implementation, providing API for relevant operations  * requiring coordination.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|CoordinatedStateManager
block|{
comment|/**    * Method to retrieve coordination for split log worker    */
name|SplitLogWorkerCoordination
name|getSplitLogWorkerCoordination
parameter_list|()
function_decl|;
comment|/**    * Method to retrieve coordination for split log manager    */
name|SplitLogManagerCoordination
name|getSplitLogManagerCoordination
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

