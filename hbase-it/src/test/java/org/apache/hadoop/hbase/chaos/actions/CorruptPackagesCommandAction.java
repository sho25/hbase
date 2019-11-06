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
name|chaos
operator|.
name|monkies
operator|.
name|PolicyBasedChaosMonkey
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
comment|/**  *  * Corrupt network packages on a random regionserver.  */
end_comment

begin_class
specifier|public
class|class
name|CorruptPackagesCommandAction
extends|extends
name|TCCommandAction
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
name|CorruptPackagesCommandAction
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|float
name|ratio
decl_stmt|;
specifier|private
name|long
name|duration
decl_stmt|;
comment|/**    * Corrupt network packages on a random regionserver.    *    * @param ratio the ratio of packages corrupted    * @param duration the time this issue persists in milliseconds    * @param timeout the timeout for executing required commands on the region server in milliseconds    * @param network network interface the regionserver uses for communication    */
specifier|public
name|CorruptPackagesCommandAction
parameter_list|(
name|float
name|ratio
parameter_list|,
name|long
name|duration
parameter_list|,
name|long
name|timeout
parameter_list|,
name|String
name|network
parameter_list|)
block|{
name|super
argument_list|(
name|timeout
argument_list|,
name|network
argument_list|)
expr_stmt|;
name|this
operator|.
name|ratio
operator|=
name|ratio
expr_stmt|;
name|this
operator|.
name|duration
operator|=
name|duration
expr_stmt|;
block|}
specifier|protected
name|void
name|localPerform
parameter_list|()
throws|throws
name|IOException
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Starting to execute CorruptPackagesCommandAction"
argument_list|)
expr_stmt|;
name|ServerName
name|server
init|=
name|PolicyBasedChaosMonkey
operator|.
name|selectRandomItem
argument_list|(
name|getCurrentServers
argument_list|()
argument_list|)
decl_stmt|;
name|String
name|hostname
init|=
name|server
operator|.
name|getHostname
argument_list|()
decl_stmt|;
try|try
block|{
name|clusterManager
operator|.
name|execSudoWithRetries
argument_list|(
name|hostname
argument_list|,
name|timeout
argument_list|,
name|getCommand
argument_list|(
name|ADD
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
operator|.
name|sleep
argument_list|(
name|duration
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Failed to run the command for the full duration"
argument_list|,
name|e
argument_list|)
expr_stmt|;
block|}
finally|finally
block|{
name|clusterManager
operator|.
name|execSudoWithRetries
argument_list|(
name|hostname
argument_list|,
name|timeout
argument_list|,
name|getCommand
argument_list|(
name|DELETE
argument_list|)
argument_list|)
expr_stmt|;
block|}
name|LOG
operator|.
name|info
argument_list|(
literal|"Finished to execute CorruptPackagesCommandAction"
argument_list|)
expr_stmt|;
block|}
specifier|private
name|String
name|getCommand
parameter_list|(
name|String
name|operation
parameter_list|)
block|{
return|return
name|String
operator|.
name|format
argument_list|(
literal|"tc qdisc %s dev %s root netem corrupt %s%%"
argument_list|,
name|operation
argument_list|,
name|network
argument_list|,
name|ratio
operator|*
literal|100
argument_list|)
return|;
block|}
block|}
end_class

end_unit

