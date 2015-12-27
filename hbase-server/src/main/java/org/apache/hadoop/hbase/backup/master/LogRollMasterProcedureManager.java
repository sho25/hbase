begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements. See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License. You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|backup
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
name|java
operator|.
name|util
operator|.
name|ArrayList
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ThreadPoolExecutor
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
name|logging
operator|.
name|Log
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
name|logging
operator|.
name|LogFactory
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
name|CoordinatedStateManagerFactory
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
name|coordination
operator|.
name|BaseCoordinatedStateManager
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
name|errorhandling
operator|.
name|ForeignException
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
name|errorhandling
operator|.
name|ForeignExceptionDispatcher
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
name|MasterServices
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
name|MetricsMaster
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
name|procedure
operator|.
name|MasterProcedureManager
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
name|procedure
operator|.
name|Procedure
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
name|procedure
operator|.
name|ProcedureCoordinator
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
name|procedure
operator|.
name|ProcedureCoordinatorRpcs
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
name|ProcedureDescription
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_class
specifier|public
class|class
name|LogRollMasterProcedureManager
extends|extends
name|MasterProcedureManager
block|{
specifier|public
specifier|static
specifier|final
name|String
name|ROLLLOG_PROCEDURE_SIGNATURE
init|=
literal|"rolllog-proc"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|ROLLLOG_PROCEDURE_NAME
init|=
literal|"rolllog"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|LogRollMasterProcedureManager
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|MasterServices
name|master
decl_stmt|;
specifier|private
name|ProcedureCoordinator
name|coordinator
decl_stmt|;
specifier|private
name|boolean
name|done
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"stop: "
operator|+
name|why
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|initialize
parameter_list|(
name|MasterServices
name|master
parameter_list|,
name|MetricsMaster
name|metricsMaster
parameter_list|)
throws|throws
name|KeeperException
throws|,
name|IOException
throws|,
name|UnsupportedOperationException
block|{
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
name|this
operator|.
name|done
operator|=
literal|false
expr_stmt|;
comment|// setup the default procedure coordinator
name|String
name|name
init|=
name|master
operator|.
name|getServerName
argument_list|()
operator|.
name|toString
argument_list|()
decl_stmt|;
name|ThreadPoolExecutor
name|tpool
init|=
name|ProcedureCoordinator
operator|.
name|defaultPool
argument_list|(
name|name
argument_list|,
literal|1
argument_list|)
decl_stmt|;
name|BaseCoordinatedStateManager
name|coordManager
init|=
operator|(
name|BaseCoordinatedStateManager
operator|)
name|CoordinatedStateManagerFactory
operator|.
name|getCoordinatedStateManager
argument_list|(
name|master
operator|.
name|getConfiguration
argument_list|()
argument_list|)
decl_stmt|;
name|coordManager
operator|.
name|initialize
argument_list|(
name|master
argument_list|)
expr_stmt|;
name|ProcedureCoordinatorRpcs
name|comms
init|=
name|coordManager
operator|.
name|getProcedureCoordinatorRpcs
argument_list|(
name|getProcedureSignature
argument_list|()
argument_list|,
name|name
argument_list|)
decl_stmt|;
name|this
operator|.
name|coordinator
operator|=
operator|new
name|ProcedureCoordinator
argument_list|(
name|comms
argument_list|,
name|tpool
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getProcedureSignature
parameter_list|()
block|{
return|return
name|ROLLLOG_PROCEDURE_SIGNATURE
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|execProcedure
parameter_list|(
name|ProcedureDescription
name|desc
parameter_list|)
throws|throws
name|IOException
block|{
name|this
operator|.
name|done
operator|=
literal|false
expr_stmt|;
comment|// start the process on the RS
name|ForeignExceptionDispatcher
name|monitor
init|=
operator|new
name|ForeignExceptionDispatcher
argument_list|(
name|desc
operator|.
name|getInstance
argument_list|()
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|ServerName
argument_list|>
name|serverNames
init|=
name|master
operator|.
name|getServerManager
argument_list|()
operator|.
name|getOnlineServersList
argument_list|()
decl_stmt|;
name|List
argument_list|<
name|String
argument_list|>
name|servers
init|=
operator|new
name|ArrayList
argument_list|<
name|String
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|ServerName
name|sn
range|:
name|serverNames
control|)
block|{
name|servers
operator|.
name|add
argument_list|(
name|sn
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
block|}
name|Procedure
name|proc
init|=
name|coordinator
operator|.
name|startProcedure
argument_list|(
name|monitor
argument_list|,
name|desc
operator|.
name|getInstance
argument_list|()
argument_list|,
operator|new
name|byte
index|[
literal|0
index|]
argument_list|,
name|servers
argument_list|)
decl_stmt|;
if|if
condition|(
name|proc
operator|==
literal|null
condition|)
block|{
name|String
name|msg
init|=
literal|"Failed to submit distributed procedure for '"
operator|+
name|desc
operator|.
name|getInstance
argument_list|()
operator|+
literal|"'"
decl_stmt|;
name|LOG
operator|.
name|error
argument_list|(
name|msg
argument_list|)
expr_stmt|;
throw|throw
operator|new
name|IOException
argument_list|(
name|msg
argument_list|)
throw|;
block|}
try|try
block|{
comment|// wait for the procedure to complete. A timer thread is kicked off that should cancel this
comment|// if it takes too long.
name|proc
operator|.
name|waitForCompleted
argument_list|()
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Done waiting - exec procedure for "
operator|+
name|desc
operator|.
name|getInstance
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Distributed roll log procedure is successful!"
argument_list|)
expr_stmt|;
name|this
operator|.
name|done
operator|=
literal|true
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|ForeignException
name|ee
init|=
operator|new
name|ForeignException
argument_list|(
literal|"Interrupted while waiting for roll log procdure to finish"
argument_list|,
name|e
argument_list|)
decl_stmt|;
name|monitor
operator|.
name|receive
argument_list|(
name|ee
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|ForeignException
name|e
parameter_list|)
block|{
name|ForeignException
name|ee
init|=
operator|new
name|ForeignException
argument_list|(
literal|"Exception while waiting for roll log procdure to finish"
argument_list|,
name|e
argument_list|)
decl_stmt|;
name|monitor
operator|.
name|receive
argument_list|(
name|ee
argument_list|)
expr_stmt|;
block|}
name|monitor
operator|.
name|rethrowException
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isProcedureDone
parameter_list|(
name|ProcedureDescription
name|desc
parameter_list|)
throws|throws
name|IOException
block|{
return|return
name|done
return|;
block|}
block|}
end_class

end_unit

