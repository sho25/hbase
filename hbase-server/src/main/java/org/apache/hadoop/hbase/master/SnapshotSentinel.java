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
name|master
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
name|hbase
operator|.
name|classification
operator|.
name|InterfaceStability
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
name|errorhandling
operator|.
name|ForeignException
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
name|protobuf
operator|.
name|generated
operator|.
name|HBaseProtos
operator|.
name|SnapshotDescription
import|;
end_import

begin_comment
comment|/**  * Watch the current snapshot under process  */
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
name|SnapshotSentinel
block|{
comment|/**    * Check to see if the snapshot is finished, where finished may be success or failure.    * @return<tt>false</tt> if the snapshot is still in progress,<tt>true</tt> if the snapshot has    *         finished    */
name|boolean
name|isFinished
parameter_list|()
function_decl|;
comment|/**    * @return -1 if the snapshot is in progress, otherwise the completion timestamp.    */
name|long
name|getCompletionTimestamp
parameter_list|()
function_decl|;
comment|/**    * Actively cancel a running snapshot.    * @param why Reason for cancellation.    */
name|void
name|cancel
parameter_list|(
name|String
name|why
parameter_list|)
function_decl|;
comment|/**    * @return the description of the snapshot being run    */
name|SnapshotDescription
name|getSnapshot
parameter_list|()
function_decl|;
comment|/**    * Get the exception that caused the snapshot to fail, if the snapshot has failed.    * @return {@link ForeignException} that caused the snapshot to fail, or<tt>null</tt> if the    *  snapshot is still in progress or has succeeded    */
name|ForeignException
name|getExceptionIfFailed
parameter_list|()
function_decl|;
comment|/**    * Rethrow the exception returned by {@link SnapshotSentinel#getExceptionIfFailed}.    * If there is no exception this is a no-op.    *    * @throws ForeignException all exceptions from remote sources are procedure exceptions    */
name|void
name|rethrowExceptionIfFailed
parameter_list|()
throws|throws
name|ForeignException
function_decl|;
block|}
end_interface

end_unit

