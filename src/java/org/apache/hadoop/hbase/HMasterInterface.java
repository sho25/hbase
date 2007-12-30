begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2007 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|Text
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
name|ipc
operator|.
name|VersionedProtocol
import|;
end_import

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

begin_comment
comment|/**  * Clients interact with the HMasterInterface to gain access to meta-level  * HBase functionality, like finding an HRegionServer and creating/destroying  * tables.  */
end_comment

begin_interface
specifier|public
interface|interface
name|HMasterInterface
extends|extends
name|VersionedProtocol
block|{
comment|/**    * Interface version.    * Version was incremented to 2 when we brought the hadoop RPC local to hbase.    */
specifier|public
specifier|static
specifier|final
name|long
name|versionID
init|=
literal|2L
decl_stmt|;
comment|/** @return true if master is available */
specifier|public
name|boolean
name|isMasterRunning
parameter_list|()
function_decl|;
comment|// Admin tools would use these cmds
comment|/**    * Creates a new table    * @param desc table descriptor    * @throws IOException    */
specifier|public
name|void
name|createTable
parameter_list|(
name|HTableDescriptor
name|desc
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Deletes a table    * @param tableName    * @throws IOException    */
specifier|public
name|void
name|deleteTable
parameter_list|(
name|Text
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Adds a column to the specified table    * @param tableName    * @param column column descriptor    * @throws IOException    */
specifier|public
name|void
name|addColumn
parameter_list|(
name|Text
name|tableName
parameter_list|,
name|HColumnDescriptor
name|column
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Modifies an existing column on the specified table    * @param tableName    * @param columnName name of the column to edit    * @param descriptor new column descriptor    * @throws IOException    */
specifier|public
name|void
name|modifyColumn
parameter_list|(
name|Text
name|tableName
parameter_list|,
name|Text
name|columnName
parameter_list|,
name|HColumnDescriptor
name|descriptor
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Deletes a column from the specified table    * @param tableName    * @param columnName    * @throws IOException    */
specifier|public
name|void
name|deleteColumn
parameter_list|(
name|Text
name|tableName
parameter_list|,
name|Text
name|columnName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Puts the table on-line (only needed if table has been previously taken offline)    * @param tableName    * @throws IOException    */
specifier|public
name|void
name|enableTable
parameter_list|(
name|Text
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Take table offline    *     * @param tableName    * @throws IOException    */
specifier|public
name|void
name|disableTable
parameter_list|(
name|Text
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Shutdown an HBase cluster.    * @throws IOException    */
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Get the location of the root region    * @return address of server that serves the root region    */
specifier|public
name|HServerAddress
name|findRootRegion
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

