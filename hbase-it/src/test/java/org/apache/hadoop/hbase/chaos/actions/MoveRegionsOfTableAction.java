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
name|chaos
operator|.
name|actions
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collections
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
name|commons
operator|.
name|lang
operator|.
name|math
operator|.
name|RandomUtils
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
name|TableName
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
name|chaos
operator|.
name|factories
operator|.
name|MonkeyConstants
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
name|Admin
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
name|util
operator|.
name|Bytes
import|;
end_import

begin_comment
comment|/** * Action that tries to move every region of a table. */
end_comment

begin_class
specifier|public
class|class
name|MoveRegionsOfTableAction
extends|extends
name|Action
block|{
specifier|private
specifier|final
name|long
name|sleepTime
decl_stmt|;
specifier|private
specifier|final
name|TableName
name|tableName
decl_stmt|;
specifier|private
specifier|final
name|long
name|maxTime
decl_stmt|;
specifier|public
name|MoveRegionsOfTableAction
parameter_list|(
name|TableName
name|tableName
parameter_list|)
block|{
name|this
argument_list|(
operator|-
literal|1
argument_list|,
name|MonkeyConstants
operator|.
name|DEFAULT_MOVE_REGIONS_MAX_TIME
argument_list|,
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MoveRegionsOfTableAction
parameter_list|(
name|long
name|sleepTime
parameter_list|,
name|long
name|maxSleepTime
parameter_list|,
name|TableName
name|tableName
parameter_list|)
block|{
name|this
operator|.
name|sleepTime
operator|=
name|sleepTime
expr_stmt|;
name|this
operator|.
name|tableName
operator|=
name|tableName
expr_stmt|;
name|this
operator|.
name|maxTime
operator|=
name|maxSleepTime
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|perform
parameter_list|()
throws|throws
name|Exception
block|{
if|if
condition|(
name|sleepTime
operator|>
literal|0
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
name|Admin
name|admin
init|=
name|this
operator|.
name|context
operator|.
name|getHBaseIntegrationTestingUtility
argument_list|()
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
name|Collection
argument_list|<
name|ServerName
argument_list|>
name|serversList
init|=
name|admin
operator|.
name|getClusterStatus
argument_list|()
operator|.
name|getServers
argument_list|()
decl_stmt|;
name|ServerName
index|[]
name|servers
init|=
name|serversList
operator|.
name|toArray
argument_list|(
operator|new
name|ServerName
index|[
name|serversList
operator|.
name|size
argument_list|()
index|]
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Performing action: Move regions of table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
name|List
argument_list|<
name|HRegionInfo
argument_list|>
name|regions
init|=
name|admin
operator|.
name|getTableRegions
argument_list|(
name|tableName
argument_list|)
decl_stmt|;
if|if
condition|(
name|regions
operator|==
literal|null
operator|||
name|regions
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Table "
operator|+
name|tableName
operator|+
literal|" doesn't have regions to move"
argument_list|)
expr_stmt|;
return|return;
block|}
name|Collections
operator|.
name|shuffle
argument_list|(
name|regions
argument_list|)
expr_stmt|;
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
for|for
control|(
name|HRegionInfo
name|regionInfo
range|:
name|regions
control|)
block|{
comment|// Don't try the move if we're stopping
if|if
condition|(
name|context
operator|.
name|isStopping
argument_list|()
condition|)
block|{
return|return;
block|}
try|try
block|{
name|String
name|destServerName
init|=
name|servers
index|[
name|RandomUtils
operator|.
name|nextInt
argument_list|(
name|servers
operator|.
name|length
argument_list|)
index|]
operator|.
name|getServerName
argument_list|()
decl_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Moving "
operator|+
name|regionInfo
operator|.
name|getRegionNameAsString
argument_list|()
operator|+
literal|" to "
operator|+
name|destServerName
argument_list|)
expr_stmt|;
name|admin
operator|.
name|move
argument_list|(
name|regionInfo
operator|.
name|getEncodedNameAsBytes
argument_list|()
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
name|destServerName
argument_list|)
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Move failed, might be caused by other chaos: "
operator|+
name|ex
operator|.
name|getMessage
argument_list|()
argument_list|)
expr_stmt|;
block|}
if|if
condition|(
name|sleepTime
operator|>
literal|0
condition|)
block|{
name|Thread
operator|.
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
comment|// put a limit on max num regions. Otherwise, this won't finish
comment|// with a sleep time of 10sec, 100 regions will finish in 16min
if|if
condition|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|>
name|maxTime
condition|)
block|{
break|break;
block|}
block|}
block|}
block|}
end_class

end_unit

