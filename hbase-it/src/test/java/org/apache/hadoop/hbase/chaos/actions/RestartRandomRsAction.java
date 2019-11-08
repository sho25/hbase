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
comment|/**  * Action that restarts a random HRegionServer  */
end_comment

begin_class
specifier|public
class|class
name|RestartRandomRsAction
extends|extends
name|RestartActionBaseAction
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
name|RestartRandomRsAction
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|RestartRandomRsAction
parameter_list|(
name|long
name|sleepTime
parameter_list|)
block|{
name|super
argument_list|(
name|sleepTime
argument_list|)
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Performing action: Restart random region server"
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
name|restartRs
argument_list|(
name|server
argument_list|,
name|sleepTime
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

