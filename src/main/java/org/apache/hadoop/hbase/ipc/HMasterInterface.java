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

begin_comment
comment|/**  * Clients interact with the HMasterInterface to gain access to meta-level  * HBase functionality, like finding an HRegionServer and creating/destroying  * tables.  *  *<p>NOTE: if you change the interface, you must change the RPC version  * number in HBaseRPCProtocolVersion  *  */
end_comment

begin_interface
specifier|public
interface|interface
name|HMasterInterface
extends|extends
name|VersionedProtocol
block|{
comment|/**    * This Interfaces' version. Version changes when the Interface changes.    */
comment|// All HBase Interfaces used derive from HBaseRPCProtocolVersion.  It
comment|// maintained a single global version number on all HBase Interfaces.  This
comment|// meant all HBase RPC was broke though only one of the three RPC Interfaces
comment|// had changed.  This has since been undone.
specifier|public
specifier|static
specifier|final
name|long
name|VERSION
init|=
literal|28L
decl_stmt|;
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
comment|/**    * Move the region<code>r</code> to<code>dest</code>.    * @param encodedRegionName The encoded region name; i.e. the hash that makes    * up the region name suffix: e.g. if regionname is    *<code>TestTable,0094429456,1289497600452.527db22f95c8a9e0116f0cc13c680396.</code>,    * then the encoded region name is:<code>527db22f95c8a9e0116f0cc13c680396</code>.    * @param destServerName The servername of the destination regionserver.  If    * passed the empty byte array we'll assign to a random server.  A server name    * is made of host, port and startcode.  Here is an example:    *<code> host187.example.com,60020,1289493121758</code>.    * @throws UnknownRegionException Thrown if we can't find a region named    *<code>encodedRegionName</code>    */
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
comment|/**    * Assign a region to a server chosen at random.    * @param regionName Region to assign.  Will use existing RegionPlan if one    * found.    * @param force If true, will force the assignment.    * @throws IOException    */
specifier|public
name|void
name|assign
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|boolean
name|force
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Unassign a region from current hosting regionserver.  Region will then be    * assigned to a regionserver chosen at random.  Region could be reassigned    * back to the same server.  Use {@link #move(byte[], byte[])} if you want    * to control the region movement.    * @param regionName Region to unassign. Will clear any existing RegionPlan    * if one found.    * @param force If true, force unassign (Will remove region from    * regions-in-transition too if present).    * @throws IOException    */
specifier|public
name|void
name|unassign
parameter_list|(
specifier|final
name|byte
index|[]
name|regionName
parameter_list|,
specifier|final
name|boolean
name|force
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Run the balancer.  Will run the balancer and if regions to move, it will    * go ahead and do the reassignments.  Can NOT run for various reasons.  Check    * logs.    * @return True if balancer ran, false otherwise.    */
specifier|public
name|boolean
name|balance
parameter_list|()
function_decl|;
comment|/**    * Turn the load balancer on or off.    * @param b If true, enable balancer. If false, disable balancer.    * @return Previous balancer value    */
specifier|public
name|boolean
name|balanceSwitch
parameter_list|(
specifier|final
name|boolean
name|b
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

