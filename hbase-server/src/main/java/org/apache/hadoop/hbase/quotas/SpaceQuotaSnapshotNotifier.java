begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|quotas
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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|TableName
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
name|Connection
import|;
end_import

begin_comment
comment|/**  * An interface which abstract away the action taken to enable or disable  * a space quota violation policy across the HBase cluster. Implementations  * must have a no-args constructor.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|SpaceQuotaSnapshotNotifier
block|{
comment|/**    * Initializes the notifier.    */
name|void
name|initialize
parameter_list|(
name|Connection
name|conn
parameter_list|)
function_decl|;
comment|/**    * Informs the cluster of the current state of a space quota for a table.    *    * @param tableName The name of the table.    * @param snapshot The details of the space quota utilization.    */
name|void
name|transitionTable
parameter_list|(
name|TableName
name|tableName
parameter_list|,
name|SpaceQuotaSnapshot
name|snapshot
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

