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
name|snapshot
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
name|client
operator|.
name|SnapshotDescription
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
comment|/**  * Thrown when a snapshot could not be created due to a server-side error when  * taking the snapshot.  */
end_comment

begin_class
annotation|@
name|SuppressWarnings
argument_list|(
literal|"serial"
argument_list|)
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
class|class
name|SnapshotCreationException
extends|extends
name|HBaseSnapshotException
block|{
comment|/**    * Used internally by the RPC engine to pass the exception back to the client.    *    * @param message error message to pass back    */
specifier|public
name|SnapshotCreationException
parameter_list|(
name|String
name|message
parameter_list|)
block|{
name|super
argument_list|(
name|message
argument_list|)
expr_stmt|;
block|}
comment|/**    * Failure to create the specified snapshot.    *    * @param message reason why the snapshot couldn't be completed    * @param snapshotDescription description of the snapshot attempted    */
specifier|public
name|SnapshotCreationException
parameter_list|(
name|String
name|message
parameter_list|,
name|SnapshotDescription
name|snapshotDescription
parameter_list|)
block|{
name|super
argument_list|(
name|message
argument_list|,
name|snapshotDescription
argument_list|)
expr_stmt|;
block|}
comment|/**    * Failure to create the specified snapshot due to an external cause.    *    * @param message reason why the snapshot couldn't be completed    * @param cause the root cause of the failure    * @param snapshotDescription description of the snapshot attempted    */
specifier|public
name|SnapshotCreationException
parameter_list|(
name|String
name|message
parameter_list|,
name|Throwable
name|cause
parameter_list|,
name|SnapshotDescription
name|snapshotDescription
parameter_list|)
block|{
name|super
argument_list|(
name|message
argument_list|,
name|cause
argument_list|,
name|snapshotDescription
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

