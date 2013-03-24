begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|executor
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

begin_comment
comment|/**  * List of all HBase event handler types.  Event types are named by a  * convention: event type names specify the component from which the event  * originated and then where its destined -- e.g. RS2ZK_ prefix means the  * event came from a regionserver destined for zookeeper -- and then what  * the even is; e.g. REGION_OPENING.  *  *<p>We give the enums indices so we can add types later and keep them  * grouped together rather than have to add them always to the end as we  * would have to if we used raw enum ordinals.  */
end_comment

begin_enum
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
enum|enum
name|EventType
block|{
comment|// Messages originating from RS (NOTE: there is NO direct communication from
comment|// RS to Master). These are a result of RS updates into ZK.
comment|// RS_ZK_REGION_CLOSING    (1),   // It is replaced by M_ZK_REGION_CLOSING(HBASE-4739)
name|RS_ZK_REGION_CLOSED
argument_list|(
literal|2
argument_list|,
name|ExecutorType
operator|.
name|MASTER_CLOSE_REGION
argument_list|)
block|,
comment|// RS has finished closing a region
name|RS_ZK_REGION_OPENING
argument_list|(
literal|3
argument_list|,
literal|null
argument_list|)
block|,
comment|// RS is in process of opening a region
name|RS_ZK_REGION_OPENED
argument_list|(
literal|4
argument_list|,
name|ExecutorType
operator|.
name|MASTER_OPEN_REGION
argument_list|)
block|,
comment|// RS has finished opening a region
name|RS_ZK_REGION_SPLITTING
argument_list|(
literal|5
argument_list|,
literal|null
argument_list|)
block|,
comment|// RS has started a region split
name|RS_ZK_REGION_SPLIT
argument_list|(
literal|6
argument_list|,
name|ExecutorType
operator|.
name|MASTER_SERVER_OPERATIONS
argument_list|)
block|,
comment|// RS split has completed.
name|RS_ZK_REGION_FAILED_OPEN
argument_list|(
literal|7
argument_list|,
name|ExecutorType
operator|.
name|MASTER_CLOSE_REGION
argument_list|)
block|,
comment|// RS failed to open a region
name|RS_ZK_REGION_MERGING
argument_list|(
literal|8
argument_list|,
literal|null
argument_list|)
block|,
comment|// RS has started merging regions
name|RS_ZK_REGION_MERGE
argument_list|(
literal|9
argument_list|,
name|ExecutorType
operator|.
name|MASTER_SERVER_OPERATIONS
argument_list|)
block|,
comment|// RS region merge has completed.
comment|// Messages originating from Master to RS
name|M_RS_OPEN_REGION
argument_list|(
literal|20
argument_list|,
name|ExecutorType
operator|.
name|RS_OPEN_REGION
argument_list|)
block|,
comment|// Master asking RS to open a region
name|M_RS_OPEN_ROOT
argument_list|(
literal|21
argument_list|,
name|ExecutorType
operator|.
name|RS_OPEN_ROOT
argument_list|)
block|,
comment|// Master asking RS to open root
name|M_RS_OPEN_META
argument_list|(
literal|22
argument_list|,
name|ExecutorType
operator|.
name|RS_OPEN_META
argument_list|)
block|,
comment|// Master asking RS to open meta
name|M_RS_CLOSE_REGION
argument_list|(
literal|23
argument_list|,
name|ExecutorType
operator|.
name|RS_CLOSE_REGION
argument_list|)
block|,
comment|// Master asking RS to close a region
name|M_RS_CLOSE_ROOT
argument_list|(
literal|24
argument_list|,
name|ExecutorType
operator|.
name|RS_CLOSE_ROOT
argument_list|)
block|,
comment|// Master asking RS to close root
name|M_RS_CLOSE_META
argument_list|(
literal|25
argument_list|,
name|ExecutorType
operator|.
name|RS_CLOSE_META
argument_list|)
block|,
comment|// Master asking RS to close meta
comment|// Messages originating from Client to Master
name|C_M_MERGE_REGION
argument_list|(
literal|30
argument_list|,
name|ExecutorType
operator|.
name|MASTER_TABLE_OPERATIONS
argument_list|)
block|,
comment|// Client asking Master to merge regions
name|C_M_DELETE_TABLE
argument_list|(
literal|40
argument_list|,
name|ExecutorType
operator|.
name|MASTER_TABLE_OPERATIONS
argument_list|)
block|,
comment|// Client asking Master to delete a table
name|C_M_DISABLE_TABLE
argument_list|(
literal|41
argument_list|,
name|ExecutorType
operator|.
name|MASTER_TABLE_OPERATIONS
argument_list|)
block|,
comment|// Client asking Master to disable a table
name|C_M_ENABLE_TABLE
argument_list|(
literal|42
argument_list|,
name|ExecutorType
operator|.
name|MASTER_TABLE_OPERATIONS
argument_list|)
block|,
comment|// Client asking Master to enable a table
name|C_M_MODIFY_TABLE
argument_list|(
literal|43
argument_list|,
name|ExecutorType
operator|.
name|MASTER_TABLE_OPERATIONS
argument_list|)
block|,
comment|// Client asking Master to modify a table
name|C_M_ADD_FAMILY
argument_list|(
literal|44
argument_list|,
literal|null
argument_list|)
block|,
comment|// Client asking Master to add family to table
name|C_M_DELETE_FAMILY
argument_list|(
literal|45
argument_list|,
literal|null
argument_list|)
block|,
comment|// Client asking Master to delete family of table
name|C_M_MODIFY_FAMILY
argument_list|(
literal|46
argument_list|,
literal|null
argument_list|)
block|,
comment|// Client asking Master to modify family of table
name|C_M_CREATE_TABLE
argument_list|(
literal|47
argument_list|,
name|ExecutorType
operator|.
name|MASTER_TABLE_OPERATIONS
argument_list|)
block|,
comment|// Client asking Master to create a table
name|C_M_SNAPSHOT_TABLE
argument_list|(
literal|48
argument_list|,
name|ExecutorType
operator|.
name|MASTER_TABLE_OPERATIONS
argument_list|)
block|,
comment|// Client asking Master to snapshot an offline table
name|C_M_RESTORE_SNAPSHOT
argument_list|(
literal|49
argument_list|,
name|ExecutorType
operator|.
name|MASTER_TABLE_OPERATIONS
argument_list|)
block|,
comment|// Client asking Master to restore a snapshot
comment|// Updates from master to ZK. This is done by the master and there is
comment|// nothing to process by either Master or RS
name|M_ZK_REGION_OFFLINE
argument_list|(
literal|50
argument_list|,
literal|null
argument_list|)
block|,
comment|// Master adds this region as offline in ZK
name|M_ZK_REGION_CLOSING
argument_list|(
literal|51
argument_list|,
literal|null
argument_list|)
block|,
comment|// Master adds this region as closing in ZK
comment|// Master controlled events to be executed on the master
name|M_SERVER_SHUTDOWN
argument_list|(
literal|70
argument_list|,
name|ExecutorType
operator|.
name|MASTER_SERVER_OPERATIONS
argument_list|)
block|,
comment|// Master is processing shutdown of a RS
name|M_META_SERVER_SHUTDOWN
argument_list|(
literal|72
argument_list|,
name|ExecutorType
operator|.
name|MASTER_META_SERVER_OPERATIONS
argument_list|)
block|,
comment|// Master is processing shutdown of RS hosting a meta region (-ROOT- or .META.).
name|M_MASTER_RECOVERY
argument_list|(
literal|73
argument_list|,
name|ExecutorType
operator|.
name|MASTER_SERVER_OPERATIONS
argument_list|)
block|,
comment|// Master is processing recovery of regions found in ZK RIT
comment|// RS controlled events to be executed on the RS
name|RS_PARALLEL_SEEK
argument_list|(
literal|80
argument_list|,
name|ExecutorType
operator|.
name|RS_PARALLEL_SEEK
argument_list|)
block|;
specifier|private
specifier|final
name|int
name|code
decl_stmt|;
specifier|private
specifier|final
name|ExecutorType
name|executor
decl_stmt|;
comment|/**    * Constructor    */
name|EventType
parameter_list|(
specifier|final
name|int
name|code
parameter_list|,
specifier|final
name|ExecutorType
name|executor
parameter_list|)
block|{
name|this
operator|.
name|code
operator|=
name|code
expr_stmt|;
name|this
operator|.
name|executor
operator|=
name|executor
expr_stmt|;
block|}
specifier|public
name|int
name|getCode
parameter_list|()
block|{
return|return
name|this
operator|.
name|code
return|;
block|}
specifier|public
specifier|static
name|EventType
name|get
parameter_list|(
specifier|final
name|int
name|code
parameter_list|)
block|{
comment|// Is this going to be slow?  Its used rare but still...
for|for
control|(
name|EventType
name|et
range|:
name|EventType
operator|.
name|values
argument_list|()
control|)
block|{
if|if
condition|(
name|et
operator|.
name|getCode
argument_list|()
operator|==
name|code
condition|)
return|return
name|et
return|;
block|}
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Unknown code "
operator|+
name|code
argument_list|)
throw|;
block|}
specifier|public
name|boolean
name|isOnlineSchemaChangeSupported
parameter_list|()
block|{
return|return
operator|(
name|this
operator|.
name|equals
argument_list|(
name|EventType
operator|.
name|C_M_ADD_FAMILY
argument_list|)
operator|||
name|this
operator|.
name|equals
argument_list|(
name|EventType
operator|.
name|C_M_DELETE_FAMILY
argument_list|)
operator|||
name|this
operator|.
name|equals
argument_list|(
name|EventType
operator|.
name|C_M_MODIFY_FAMILY
argument_list|)
operator|||
name|this
operator|.
name|equals
argument_list|(
name|EventType
operator|.
name|C_M_MODIFY_TABLE
argument_list|)
operator|)
return|;
block|}
name|ExecutorType
name|getExecutorServiceType
parameter_list|()
block|{
return|return
name|this
operator|.
name|executor
return|;
block|}
block|}
end_enum

end_unit

