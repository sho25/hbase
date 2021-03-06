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
name|util
operator|.
name|Threads
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/** * Base class for restarting HBaseServer's */
end_comment

begin_class
specifier|public
class|class
name|RestartActionBaseAction
extends|extends
name|Action
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|RestartActionBaseAction
operator|.
name|class
argument_list|)
decl_stmt|;
name|long
name|sleepTime
decl_stmt|;
comment|// how long should we sleep
specifier|public
name|RestartActionBaseAction
parameter_list|(
name|long
name|sleepTime
parameter_list|)
block|{
name|this
operator|.
name|sleepTime
operator|=
name|sleepTime
expr_stmt|;
block|}
name|void
name|sleep
parameter_list|(
name|long
name|sleepTime
parameter_list|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Sleeping for:"
operator|+
name|sleepTime
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
name|void
name|restartMaster
parameter_list|(
name|ServerName
name|server
parameter_list|,
name|long
name|sleepTime
parameter_list|)
throws|throws
name|IOException
block|{
name|sleepTime
operator|=
name|Math
operator|.
name|max
argument_list|(
name|sleepTime
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
comment|// Don't try the kill if we're stopping
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
literal|"Killing master: {}"
argument_list|,
name|server
argument_list|)
expr_stmt|;
name|killMaster
argument_list|(
name|server
argument_list|)
expr_stmt|;
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting master: {}"
argument_list|,
name|server
argument_list|)
expr_stmt|;
name|startMaster
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
comment|/**    * Stop and then restart the region server instead of killing it.    * @param server hostname to restart the regionserver on    * @param sleepTime number of milliseconds between stop and restart    * @throws IOException if something goes wrong    */
name|void
name|gracefulRestartRs
parameter_list|(
name|ServerName
name|server
parameter_list|,
name|long
name|sleepTime
parameter_list|)
throws|throws
name|IOException
block|{
name|sleepTime
operator|=
name|Math
operator|.
name|max
argument_list|(
name|sleepTime
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
comment|// Don't try the stop if we're stopping already
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
literal|"Stopping region server: {}"
argument_list|,
name|server
argument_list|)
expr_stmt|;
name|stopRs
argument_list|(
name|server
argument_list|)
expr_stmt|;
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting region server: {}"
argument_list|,
name|server
argument_list|)
expr_stmt|;
name|startRs
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
name|void
name|restartRs
parameter_list|(
name|ServerName
name|server
parameter_list|,
name|long
name|sleepTime
parameter_list|)
throws|throws
name|IOException
block|{
name|sleepTime
operator|=
name|Math
operator|.
name|max
argument_list|(
name|sleepTime
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
comment|// Don't try the kill if we're stopping
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
literal|"Killing region server: {}"
argument_list|,
name|server
argument_list|)
expr_stmt|;
name|killRs
argument_list|(
name|server
argument_list|)
expr_stmt|;
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting region server: {}"
argument_list|,
name|server
argument_list|)
expr_stmt|;
name|startRs
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
name|void
name|restartZKNode
parameter_list|(
name|ServerName
name|server
parameter_list|,
name|long
name|sleepTime
parameter_list|)
throws|throws
name|IOException
block|{
name|sleepTime
operator|=
name|Math
operator|.
name|max
argument_list|(
name|sleepTime
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
comment|// Don't try the kill if we're stopping
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
literal|"Killing zookeeper node: {}"
argument_list|,
name|server
argument_list|)
expr_stmt|;
name|killZKNode
argument_list|(
name|server
argument_list|)
expr_stmt|;
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting zookeeper node: {}"
argument_list|,
name|server
argument_list|)
expr_stmt|;
name|startZKNode
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
name|void
name|restartDataNode
parameter_list|(
name|ServerName
name|server
parameter_list|,
name|long
name|sleepTime
parameter_list|)
throws|throws
name|IOException
block|{
name|sleepTime
operator|=
name|Math
operator|.
name|max
argument_list|(
name|sleepTime
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
comment|// Don't try the kill if we're stopping
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
literal|"Killing data node: {}"
argument_list|,
name|server
argument_list|)
expr_stmt|;
name|killDataNode
argument_list|(
name|server
argument_list|)
expr_stmt|;
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting data node: {}"
argument_list|,
name|server
argument_list|)
expr_stmt|;
name|startDataNode
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
name|void
name|restartNameNode
parameter_list|(
name|ServerName
name|server
parameter_list|,
name|long
name|sleepTime
parameter_list|)
throws|throws
name|IOException
block|{
name|sleepTime
operator|=
name|Math
operator|.
name|max
argument_list|(
name|sleepTime
argument_list|,
literal|1000
argument_list|)
expr_stmt|;
comment|// Don't try the kill if we're stopping
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
literal|"Killing name node: {}"
argument_list|,
name|server
argument_list|)
expr_stmt|;
name|killNameNode
argument_list|(
name|server
argument_list|)
expr_stmt|;
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting name node: {}"
argument_list|,
name|server
argument_list|)
expr_stmt|;
name|startNameNode
argument_list|(
name|server
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

