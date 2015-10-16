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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|HBaseTestingUtility
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
name|client
operator|.
name|Admin
import|;
end_import

begin_comment
comment|/** * Action that tries to flush a table. */
end_comment

begin_class
specifier|public
class|class
name|FlushTableAction
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
specifier|public
name|FlushTableAction
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
name|tableName
argument_list|)
expr_stmt|;
block|}
specifier|public
name|FlushTableAction
parameter_list|(
name|int
name|sleepTime
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
name|HBaseTestingUtility
name|util
init|=
name|context
operator|.
name|getHBaseIntegrationTestingUtility
argument_list|()
decl_stmt|;
name|Admin
name|admin
init|=
name|util
operator|.
name|getHBaseAdmin
argument_list|()
decl_stmt|;
comment|// Don't try the flush if we're stopping
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Performing action: Flush table "
operator|+
name|tableName
argument_list|)
expr_stmt|;
try|try
block|{
name|admin
operator|.
name|flush
argument_list|(
name|tableName
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
literal|"Flush failed, might be caused by other chaos: "
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
block|}
block|}
end_class

end_unit

