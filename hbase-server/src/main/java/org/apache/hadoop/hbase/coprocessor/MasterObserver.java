begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|coprocessor
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
name|java
operator|.
name|util
operator|.
name|List
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
name|Coprocessor
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
name|HRegionInfo
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
name|ServerName
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
name|master
operator|.
name|RegionPlan
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
comment|/**  * Defines coprocessor hooks for interacting with operations on the  * {@link org.apache.hadoop.hbase.master.HMaster} process.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|MasterObserver
extends|extends
name|Coprocessor
block|{
comment|/**    * Called before a new table is created by    * {@link org.apache.hadoop.hbase.master.HMaster}.  Called as part of create    * table RPC call.    * It can't bypass the default action, e.g., ctx.bypass() won't have effect.    * @param ctx the environment to interact with the framework and master    * @param desc the HTableDescriptor for the table    * @param regions the initial regions created for the table    * @throws IOException    */
name|void
name|preCreateTable
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|HTableDescriptor
name|desc
parameter_list|,
name|HRegionInfo
index|[]
name|regions
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the createTable operation has been requested.  Called as part    * of create table RPC call.    * @param ctx the environment to interact with the framework and master    * @param desc the HTableDescriptor for the table    * @param regions the initial regions created for the table    * @throws IOException    */
name|void
name|postCreateTable
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|HTableDescriptor
name|desc
parameter_list|,
name|HRegionInfo
index|[]
name|regions
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called before a new table is created by    * {@link org.apache.hadoop.hbase.master.HMaster}.  Called as part of create    * table handler and it is async to the create RPC call.    * It can't bypass the default action, e.g., ctx.bypass() won't have effect.    * @param ctx the environment to interact with the framework and master    * @param desc the HTableDescriptor for the table    * @param regions the initial regions created for the table    * @throws IOException    */
name|void
name|preCreateTableHandler
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|HTableDescriptor
name|desc
parameter_list|,
name|HRegionInfo
index|[]
name|regions
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the createTable operation has been requested.  Called as part    * of create table RPC call.  Called as part of create table handler and    * it is async to the create RPC call.    * @param ctx the environment to interact with the framework and master    * @param desc the HTableDescriptor for the table    * @param regions the initial regions created for the table    * @throws IOException    */
name|void
name|postCreateTableHandler
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|HTableDescriptor
name|desc
parameter_list|,
name|HRegionInfo
index|[]
name|regions
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called before {@link org.apache.hadoop.hbase.master.HMaster} deletes a    * table.  Called as part of delete table RPC call.    * It can't bypass the default action, e.g., ctx.bypass() won't have effect.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    */
name|void
name|preDeleteTable
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the deleteTable operation has been requested.  Called as part    * of delete table RPC call.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    */
name|void
name|postDeleteTable
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called before {@link org.apache.hadoop.hbase.master.HMaster} deletes a    * table.  Called as part of delete table handler and    * it is async to the delete RPC call.    * It can't bypass the default action, e.g., ctx.bypass() won't have effect.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    */
name|void
name|preDeleteTableHandler
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after {@link org.apache.hadoop.hbase.master.HMaster} deletes a    * table.  Called as part of delete table handler and it is async to the    * delete RPC call.    * It can't bypass the default action, e.g., ctx.bypass() won't have effect.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    */
name|void
name|postDeleteTableHandler
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called prior to modifying a table's properties.  Called as part of modify    * table RPC call.    * It can't bypass the default action, e.g., ctx.bypass() won't have effect.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    * @param htd the HTableDescriptor    */
name|void
name|preModifyTable
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
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
comment|/**    * Called after the modifyTable operation has been requested.  Called as part    * of modify table RPC call.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    * @param htd the HTableDescriptor    */
name|void
name|postModifyTable
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
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
comment|/**    * Called prior to modifying a table's properties.  Called as part of modify    * table handler and it is async to the modify table RPC call.    * It can't bypass the default action, e.g., ctx.bypass() won't have effect.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    * @param htd the HTableDescriptor    */
name|void
name|preModifyTableHandler
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
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
comment|/**    * Called after to modifying a table's properties.  Called as part of modify    * table handler and it is async to the modify table RPC call.    * It can't bypass the default action, e.g., ctx.bypass() won't have effect.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    * @param htd the HTableDescriptor    */
name|void
name|postModifyTableHandler
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
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
comment|/**    * Called prior to adding a new column family to the table.  Called as part of    * add column RPC call.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    * @param column the HColumnDescriptor    */
name|void
name|preAddColumn
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
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
comment|/**    * Called after the new column family has been created.  Called as part of    * add column RPC call.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    * @param column the HColumnDescriptor    */
name|void
name|postAddColumn
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
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
comment|/**    * Called prior to adding a new column family to the table.  Called as part of    * add column handler.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    * @param column the HColumnDescriptor    */
name|void
name|preAddColumnHandler
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
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
comment|/**    * Called after the new column family has been created.  Called as part of    * add column handler.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    * @param column the HColumnDescriptor    */
name|void
name|postAddColumnHandler
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
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
comment|/**    * Called prior to modifying a column family's attributes.  Called as part of    * modify column RPC call.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    * @param descriptor the HColumnDescriptor    */
name|void
name|preModifyColumn
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
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
comment|/**    * Called after the column family has been updated.  Called as part of modify    * column RPC call.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    * @param descriptor the HColumnDescriptor    */
name|void
name|postModifyColumn
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
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
comment|/**    * Called prior to modifying a column family's attributes.  Called as part of    * modify column handler.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    * @param descriptor the HColumnDescriptor    */
name|void
name|preModifyColumnHandler
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
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
comment|/**    * Called after the column family has been updated.  Called as part of modify    * column handler.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    * @param descriptor the HColumnDescriptor    */
name|void
name|postModifyColumnHandler
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
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
comment|/**    * Called prior to deleting the entire column family.  Called as part of    * delete column RPC call.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    * @param c the column    */
name|void
name|preDeleteColumn
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|c
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the column family has been deleted.  Called as part of delete    * column RPC call.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    * @param c the column    */
name|void
name|postDeleteColumn
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|c
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called prior to deleting the entire column family.  Called as part of    * delete column handler.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    * @param c the column    */
name|void
name|preDeleteColumnHandler
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|c
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the column family has been deleted.  Called as part of    * delete column handler.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    * @param c the column    */
name|void
name|postDeleteColumnHandler
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|,
specifier|final
name|byte
index|[]
name|c
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called prior to enabling a table.  Called as part of enable table RPC call.    * It can't bypass the default action, e.g., ctx.bypass() won't have effect.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    */
name|void
name|preEnableTable
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the enableTable operation has been requested.  Called as part    * of enable table RPC call.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    */
name|void
name|postEnableTable
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called prior to enabling a table.  Called as part of enable table handler    * and it is async to the enable table RPC call.    * It can't bypass the default action, e.g., ctx.bypass() won't have effect.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    */
name|void
name|preEnableTableHandler
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the enableTable operation has been requested.  Called as part    * of enable table handler and it is async to the enable table RPC call.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    */
name|void
name|postEnableTableHandler
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called prior to disabling a table.  Called as part of disable table RPC    * call.    * It can't bypass the default action, e.g., ctx.bypass() won't have effect.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    */
name|void
name|preDisableTable
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the disableTable operation has been requested.  Called as part    * of disable table RPC call.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    */
name|void
name|postDisableTable
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called prior to disabling a table.  Called as part of disable table handler    * and it is asyn to the disable table RPC call.    * It can't bypass the default action, e.g., ctx.bypass() won't have effect.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    */
name|void
name|preDisableTableHandler
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the disableTable operation has been requested.  Called as part    * of disable table handler and it is asyn to the disable table RPC call.    * @param ctx the environment to interact with the framework and master    * @param tableName the name of the table    */
name|void
name|postDisableTableHandler
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|byte
index|[]
name|tableName
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called prior to moving a given region from one region server to another.    * @param ctx the environment to interact with the framework and master    * @param region the HRegionInfo    * @param srcServer the source ServerName    * @param destServer the destination ServerName    */
name|void
name|preMove
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|HRegionInfo
name|region
parameter_list|,
specifier|final
name|ServerName
name|srcServer
parameter_list|,
specifier|final
name|ServerName
name|destServer
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the region move has been requested.    * @param ctx the environment to interact with the framework and master    * @param region the HRegionInfo    * @param srcServer the source ServerName    * @param destServer the destination ServerName    */
name|void
name|postMove
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|HRegionInfo
name|region
parameter_list|,
specifier|final
name|ServerName
name|srcServer
parameter_list|,
specifier|final
name|ServerName
name|destServer
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called prior to assigning a specific region.    * @param ctx the environment to interact with the framework and master    * @param regionInfo the regionInfo of the region    */
name|void
name|preAssign
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the region assignment has been requested.    * @param ctx the environment to interact with the framework and master    * @param regionInfo the regionInfo of the region    */
name|void
name|postAssign
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called prior to unassigning a given region.    * @param ctx the environment to interact with the framework and master    * @param regionInfo    * @param force whether to force unassignment or not    */
name|void
name|preUnassign
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|boolean
name|force
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the region unassignment has been requested.    * @param ctx the environment to interact with the framework and master    * @param regionInfo    * @param force whether to force unassignment or not    */
name|void
name|postUnassign
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|,
specifier|final
name|boolean
name|force
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called prior to marking a given region as offline.<code>ctx.bypass()</code> will not have any    * impact on this hook.    * @param ctx the environment to interact with the framework and master    * @param regionInfo    */
name|void
name|preRegionOffline
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the region has been marked offline.    * @param ctx the environment to interact with the framework and master    * @param regionInfo    */
name|void
name|postRegionOffline
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|HRegionInfo
name|regionInfo
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called prior to requesting rebalancing of the cluster regions, though after    * the initial checks for regions in transition and the balance switch flag.    * @param ctx the environment to interact with the framework and master    */
name|void
name|preBalance
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the balancing plan has been submitted.    * @param ctx the environment to interact with the framework and master    * @param plans the RegionPlans which master has executed. RegionPlan serves as hint    * as for the final destination for the underlying region but may not represent the    * final state of assignment    */
name|void
name|postBalance
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
name|List
argument_list|<
name|RegionPlan
argument_list|>
name|plans
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called prior to modifying the flag used to enable/disable region balancing.    * @param ctx the coprocessor instance's environment    * @param newValue the new flag value submitted in the call    */
name|boolean
name|preBalanceSwitch
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|boolean
name|newValue
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the flag to enable/disable balancing has changed.    * @param ctx the coprocessor instance's environment    * @param oldValue the previously set balanceSwitch value    * @param newValue the newly set balanceSwitch value    */
name|void
name|postBalanceSwitch
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|boolean
name|oldValue
parameter_list|,
specifier|final
name|boolean
name|newValue
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called prior to shutting down the full HBase cluster, including this    * {@link org.apache.hadoop.hbase.master.HMaster} process.    */
name|void
name|preShutdown
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called immediately prior to stopping this    * {@link org.apache.hadoop.hbase.master.HMaster} process.    */
name|void
name|preStopMaster
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called immediately after an active master instance has completed    * initialization.  Will not be called on standby master instances unless    * they take over the active role.    */
name|void
name|postStartMaster
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called before a new snapshot is taken.    * Called as part of snapshot RPC call.    * It can't bypass the default action, e.g., ctx.bypass() won't have effect.    * @param ctx the environment to interact with the framework and master    * @param snapshot the SnapshotDescriptor for the snapshot    * @param hTableDescriptor the hTableDescriptor of the table to snapshot    * @throws IOException    */
name|void
name|preSnapshot
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|SnapshotDescription
name|snapshot
parameter_list|,
specifier|final
name|HTableDescriptor
name|hTableDescriptor
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the snapshot operation has been requested.    * Called as part of snapshot RPC call.    * @param ctx the environment to interact with the framework and master    * @param snapshot the SnapshotDescriptor for the snapshot    * @param hTableDescriptor the hTableDescriptor of the table to snapshot    * @throws IOException    */
name|void
name|postSnapshot
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|SnapshotDescription
name|snapshot
parameter_list|,
specifier|final
name|HTableDescriptor
name|hTableDescriptor
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called before a snapshot is cloned.    * Called as part of restoreSnapshot RPC call.    * It can't bypass the default action, e.g., ctx.bypass() won't have effect.    * @param ctx the environment to interact with the framework and master    * @param snapshot the SnapshotDescriptor for the snapshot    * @param hTableDescriptor the hTableDescriptor of the table to create    * @throws IOException    */
name|void
name|preCloneSnapshot
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|SnapshotDescription
name|snapshot
parameter_list|,
specifier|final
name|HTableDescriptor
name|hTableDescriptor
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after a snapshot clone operation has been requested.    * Called as part of restoreSnapshot RPC call.    * @param ctx the environment to interact with the framework and master    * @param snapshot the SnapshotDescriptor for the snapshot    * @param hTableDescriptor the hTableDescriptor of the table to create    * @throws IOException    */
name|void
name|postCloneSnapshot
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|SnapshotDescription
name|snapshot
parameter_list|,
specifier|final
name|HTableDescriptor
name|hTableDescriptor
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called before a snapshot is restored.    * Called as part of restoreSnapshot RPC call.    * It can't bypass the default action, e.g., ctx.bypass() won't have effect.    * @param ctx the environment to interact with the framework and master    * @param snapshot the SnapshotDescriptor for the snapshot    * @param hTableDescriptor the hTableDescriptor of the table to restore    * @throws IOException    */
name|void
name|preRestoreSnapshot
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|SnapshotDescription
name|snapshot
parameter_list|,
specifier|final
name|HTableDescriptor
name|hTableDescriptor
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after a snapshot restore operation has been requested.    * Called as part of restoreSnapshot RPC call.    * @param ctx the environment to interact with the framework and master    * @param snapshot the SnapshotDescriptor for the snapshot    * @param hTableDescriptor the hTableDescriptor of the table to restore    * @throws IOException    */
name|void
name|postRestoreSnapshot
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|SnapshotDescription
name|snapshot
parameter_list|,
specifier|final
name|HTableDescriptor
name|hTableDescriptor
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called before a snapshot is deleted.    * Called as part of deleteSnapshot RPC call.    * It can't bypass the default action, e.g., ctx.bypass() won't have effect.    * @param ctx the environment to interact with the framework and master    * @param snapshot the SnapshotDescriptor of the snapshot to delete    * @throws IOException    */
name|void
name|preDeleteSnapshot
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|SnapshotDescription
name|snapshot
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Called after the delete snapshot operation has been requested.    * Called as part of deleteSnapshot RPC call.    * @param ctx the environment to interact with the framework and master    * @param snapshot the SnapshotDescriptor of the snapshot to delete    * @throws IOException    */
name|void
name|postDeleteSnapshot
parameter_list|(
specifier|final
name|ObserverContext
argument_list|<
name|MasterCoprocessorEnvironment
argument_list|>
name|ctx
parameter_list|,
specifier|final
name|SnapshotDescription
name|snapshot
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit

