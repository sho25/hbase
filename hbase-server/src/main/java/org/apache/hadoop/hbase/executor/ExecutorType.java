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
name|executor
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
comment|/**  * The following is a list of all executor types, both those that run in the  * master and those that run in the regionserver.  */
end_comment

begin_enum
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
enum|enum
name|ExecutorType
block|{
comment|// Master executor services
name|MASTER_CLOSE_REGION
argument_list|(
literal|1
argument_list|)
block|,
name|MASTER_OPEN_REGION
argument_list|(
literal|2
argument_list|)
block|,
name|MASTER_SERVER_OPERATIONS
argument_list|(
literal|3
argument_list|)
block|,
name|MASTER_TABLE_OPERATIONS
argument_list|(
literal|4
argument_list|)
block|,
name|MASTER_RS_SHUTDOWN
argument_list|(
literal|5
argument_list|)
block|,
name|MASTER_META_SERVER_OPERATIONS
argument_list|(
literal|6
argument_list|)
block|,
name|M_LOG_REPLAY_OPS
argument_list|(
literal|7
argument_list|)
block|,
comment|// RegionServer executor services
name|RS_OPEN_REGION
argument_list|(
literal|20
argument_list|)
block|,
name|RS_OPEN_ROOT
argument_list|(
literal|21
argument_list|)
block|,
name|RS_OPEN_META
argument_list|(
literal|22
argument_list|)
block|,
name|RS_CLOSE_REGION
argument_list|(
literal|23
argument_list|)
block|,
name|RS_CLOSE_ROOT
argument_list|(
literal|24
argument_list|)
block|,
name|RS_CLOSE_META
argument_list|(
literal|25
argument_list|)
block|,
name|RS_PARALLEL_SEEK
argument_list|(
literal|26
argument_list|)
block|,
name|RS_LOG_REPLAY_OPS
argument_list|(
literal|27
argument_list|)
block|,
name|RS_REGION_REPLICA_FLUSH_OPS
argument_list|(
literal|28
argument_list|)
block|,
name|RS_COMPACTED_FILES_DISCHARGER
argument_list|(
literal|29
argument_list|)
block|,
name|RS_OPEN_PRIORITY_REGION
argument_list|(
literal|30
argument_list|)
block|,
name|RS_REFRESH_PEER
argument_list|(
literal|31
argument_list|)
block|,
name|RS_REPLAY_SYNC_REPLICATION_WAL
argument_list|(
literal|32
argument_list|)
block|;
name|ExecutorType
parameter_list|(
name|int
name|value
parameter_list|)
block|{   }
comment|/**    * @return Conflation of the executor type and the passed {@code serverName}.    */
name|String
name|getExecutorName
parameter_list|(
name|String
name|serverName
parameter_list|)
block|{
return|return
name|this
operator|.
name|toString
argument_list|()
operator|+
literal|"-"
operator|+
name|serverName
operator|.
name|replace
argument_list|(
literal|"%"
argument_list|,
literal|"%%"
argument_list|)
return|;
block|}
block|}
end_enum

end_unit

