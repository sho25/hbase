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
name|ipc
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
name|ClusterStatus
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
name|HColumnDescriptor
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
name|UnknownRegionException
import|;
end_import

begin_comment
comment|/**  * Clients interact with the HMasterInterface to gain access to meta-level  * HBase functionality, like finding an HRegionServer and creating/destroying  * tables.  *  *<p>NOTE: if you change the interface, you must change the RPC version  * number in HBaseRPCProtocolVersion  *  */
end_comment

begin_interface
specifier|public
interface|interface
name|HMasterInterface
extends|extends
name|HBaseRPCProtocolVersion
block|{
comment|/** @return true if master is available */
specifier|public
name|boolean
name|isMasterRunning
parameter_list|()
function_decl|;
comment|// Admin tools would use these cmds
comment|/**    * Creates a new table.  If splitKeys are specified, then the table will be    * created with an initial set of multiple regions.  If splitKeys is null,    * the table will be created with a single region.    * @param desc table descriptor    * @param splitKeys    * @throws IOException    */
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
comment|/**    * Deletes a table    * @param tableName table to delete    * @throws IOException e    */
specifier|public
name|void
name|deleteTable
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Adds a column to the specified table    * @param tableName table to modify    * @param column column descriptor    * @throws IOException e    */
specifier|public
name|void
name|addColumn
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
name|HColumnDescriptor
name|column
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Modifies an existing column on the specified table    * @param tableName table name    * @param descriptor new column descriptor    * @throws IOException e    */
specifier|public
name|void
name|modifyColumn
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
name|HColumnDescriptor
name|descriptor
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Deletes a column from the specified table. Table must be disabled.    * @param tableName table to alter    * @param columnName column family to remove    * @throws IOException e    */
specifier|public
name|void
name|deleteColumn
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|columnName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Puts the table on-line (only needed if table has been previously taken offline)    * @param tableName table to enable    * @throws IOException e    */
specifier|public
name|void
name|enableTable
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Take table offline    *    * @param tableName table to take offline    * @throws IOException e    */
specifier|public
name|void
name|disableTable
parameter_list|(
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Modify a table's metadata    *    * @param tableName table to modify    * @param htd new descriptor for table    * @throws IOException e    */
specifier|public
name|void
name|modifyTable
parameter_list|(
name|byte
index|[]
name|tableName
parameter_list|,
name|HTableDescriptor
name|htd
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Shutdown an HBase cluster.    * @throws IOException e    */
specifier|public
name|void
name|shutdown
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Stop HBase Master only.    * Does not shutdown the cluster.    * @throws IOException e    */
specifier|public
name|void
name|stopMaster
parameter_list|()
throws|throws
name|IOException
function_decl|;
comment|/**    * Return cluster status.    * @return status object    */
specifier|public
name|ClusterStatus
name|getClusterStatus
parameter_list|()
function_decl|;
comment|/**    * Move the region<code>r</code> to<code>dest</code>.    * @param encodedRegionName The encoded region name.    * @param destServerName The servername of the destination regionserver    * @throws UnknownRegionException Thrown if we can't find a region named    *<code>encodedRegionName</code>    */
specifier|public
name|void
name|move
parameter_list|(
specifier|final
name|byte
index|[]
name|encodedRegionName
parameter_list|,
specifier|final
name|byte
index|[]
name|destServerName
parameter_list|)
throws|throws
name|UnknownRegionException
function_decl|;
comment|/**    * @param b If true, enable balancer. If false, disable balancer.    * @return Previous balancer value    */
specifier|public
name|boolean
name|balance
parameter_list|(
specifier|final
name|boolean
name|b
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

