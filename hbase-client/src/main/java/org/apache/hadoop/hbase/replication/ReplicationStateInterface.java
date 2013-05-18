begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|replication
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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|Closeable
import|;
end_import

begin_comment
comment|/**  * This provides an interface for getting and setting the replication state of a  * cluster. This state is used to indicate whether replication is enabled or  * disabled on a cluster.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|ReplicationStateInterface
extends|extends
name|Closeable
block|{
comment|/**    * Initialize the replication state interface.    */
specifier|public
name|void
name|init
parameter_list|()
throws|throws
name|KeeperException
function_decl|;
comment|/**    * Get the current state of replication (i.e. ENABLED or DISABLED).    * @return true if replication is enabled, false otherwise    * @throws KeeperException    */
specifier|public
name|boolean
name|getState
parameter_list|()
throws|throws
name|KeeperException
function_decl|;
comment|/**    * Set the state of replication.    * @param newState    * @throws KeeperException    */
specifier|public
name|void
name|setState
parameter_list|(
name|boolean
name|newState
parameter_list|)
throws|throws
name|KeeperException
function_decl|;
block|}
end_interface

end_unit

