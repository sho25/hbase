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
comment|/**  * Thrown when the server is looking for a snapshot, but can't find the snapshot on the filesystem.  */
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
name|SnapshotDoesNotExistException
extends|extends
name|HBaseSnapshotException
block|{
comment|/**    * @param message the full description of the failure    */
specifier|public
name|SnapshotDoesNotExistException
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
comment|/**    * @param snapshotDescription expected snapshot to find    */
specifier|public
name|SnapshotDoesNotExistException
parameter_list|(
name|SnapshotDescription
name|snapshotDescription
parameter_list|)
block|{
name|super
argument_list|(
literal|"Snapshot '"
operator|+
name|snapshotDescription
operator|.
name|getName
argument_list|()
operator|+
literal|"' doesn't exist on the filesystem"
argument_list|,
name|snapshotDescription
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

