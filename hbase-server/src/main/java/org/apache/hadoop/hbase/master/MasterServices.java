begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HTableDescriptor
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
name|Server
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
name|TableDescriptors
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
name|executor
operator|.
name|ExecutorService
import|;
end_import

begin_comment
comment|/**  * Services Master supplies  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MasterServices
extends|extends
name|Server
block|{
comment|/**    * @return Master's instance of the {@link AssignmentManager}    */
specifier|public
name|AssignmentManager
name|getAssignmentManager
parameter_list|()
function_decl|;
comment|/**    * @return Master's filesystem {@link MasterFileSystem} utility class.    */
specifier|public
name|MasterFileSystem
name|getMasterFileSystem
parameter_list|()
function_decl|;
comment|/**    * @return Master's {@link ServerManager} instance.    */
specifier|public
name|ServerManager
name|getServerManager
parameter_list|()
function_decl|;
comment|/**    * @return Master's instance of {@link ExecutorService}    */
specifier|public
name|ExecutorService
name|getExecutorService
parameter_list|()
function_decl|;
comment|/**    * Check table is modifiable; i.e. exists and is offline.    * @param tableName Name of table to check.    * @throws TableNotDisabledException    * @throws TableNotFoundException     */
specifier|public
name|void
name|checkTableModifiable
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Create a table using the given table definition.    * @param desc The table definition    * @param splitKeys Starting row keys for the initial table regions.  If null    *     a single region is created.    */
specifier|public
name|void
name|createTable
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|,
name|byte
index|[]
index|[]
name|splitKeys
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * @return Return table descriptors implementation.    */
specifier|public
name|TableDescriptors
name|getTableDescriptors
parameter_list|()
function_decl|;
comment|/**    * @return true if master enables ServerShutdownHandler;    */
specifier|public
name|boolean
name|isServerShutdownHandlerEnabled
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

