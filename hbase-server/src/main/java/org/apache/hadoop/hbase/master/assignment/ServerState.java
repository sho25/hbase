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
operator|.
name|assignment
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
comment|/**  * Server State.  */
end_comment

begin_enum
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
enum|enum
name|ServerState
block|{
comment|/**    * Initial state. Available.    */
name|ONLINE
block|,
comment|/**    * Indicate that the server has crashed, i.e., we have already scheduled a SCP for it.    */
name|CRASHED
block|,
comment|/**    * Only server which carries meta can have this state. We will split wal for meta and then    * assign meta first before splitting other wals.    */
name|SPLITTING_META
block|,
comment|/**    * Indicate that the meta splitting is done. We need this state so that the UnassignProcedure    * for meta can safely quit. See the comments in UnassignProcedure.remoteCallFailed for more    * details.    */
name|SPLITTING_META_DONE
block|,
comment|/**    * Server expired/crashed. Currently undergoing WAL splitting.    */
name|SPLITTING
block|,
comment|/**    * WAL splitting done. This state will be used to tell the UnassignProcedure that it can safely    * quit. See the comments in UnassignProcedure.remoteCallFailed for more details.    */
name|OFFLINE
block|}
end_enum

end_unit

