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
name|ClusterMetrics
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
comment|/** * Action that tries to restart the HRegionServer holding Meta. */
end_comment

begin_class
specifier|public
class|class
name|RestartRsHoldingMetaAction
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
name|RestartRsHoldingMetaAction
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|public
name|RestartRsHoldingMetaAction
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
literal|"Performing action: Restart region server holding META"
argument_list|)
expr_stmt|;
name|ServerName
name|server
init|=
name|cluster
operator|.
name|getServerHoldingMeta
argument_list|()
decl_stmt|;
if|if
condition|(
name|server
operator|==
literal|null
condition|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"No server is holding hbase:meta right now."
argument_list|)
expr_stmt|;
return|return;
block|}
name|ClusterMetrics
name|clusterStatus
init|=
name|cluster
operator|.
name|getClusterMetrics
argument_list|()
decl_stmt|;
if|if
condition|(
name|server
operator|.
name|equals
argument_list|(
name|clusterStatus
operator|.
name|getMasterName
argument_list|()
argument_list|)
condition|)
block|{
comment|// Master holds the meta, so restart the master.
name|restartMaster
argument_list|(
name|server
argument_list|,
name|sleepTime
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|restartRs
argument_list|(
name|server
argument_list|,
name|sleepTime
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

