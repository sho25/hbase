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
name|procedure
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
name|Hashtable
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
name|conf
operator|.
name|Configuration
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
name|zookeeper
operator|.
name|KeeperException
import|;
end_import

begin_comment
comment|/**  * Provides the globally barriered procedure framework and environment for  * master oriented operations. {@link org.apache.hadoop.hbase.master.HMaster}   * interacts with the loaded procedure manager through this class.  */
end_comment

begin_class
specifier|public
class|class
name|MasterProcedureManagerHost
extends|extends
name|ProcedureManagerHost
argument_list|<
name|MasterProcedureManager
argument_list|>
block|{
specifier|private
name|Hashtable
argument_list|<
name|String
argument_list|,
name|MasterProcedureManager
argument_list|>
name|procedureMgrMap
init|=
operator|new
name|Hashtable
argument_list|<>
argument_list|()
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|loadProcedures
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|loadUserProcedures
argument_list|(
name|conf
argument_list|,
name|MASTER_PROCEDURE_CONF_KEY
argument_list|)
expr_stmt|;
for|for
control|(
name|MasterProcedureManager
name|mpm
range|:
name|getProcedureManagers
argument_list|()
control|)
block|{
name|procedureMgrMap
operator|.
name|put
argument_list|(
name|mpm
operator|.
name|getProcedureSignature
argument_list|()
argument_list|,
name|mpm
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|initialize
parameter_list|(
name|MasterServices
name|master
parameter_list|,
specifier|final
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
for|for
control|(
name|MasterProcedureManager
name|mpm
range|:
name|getProcedureManagers
argument_list|()
control|)
block|{
name|mpm
operator|.
name|initialize
argument_list|(
name|master
argument_list|,
name|metricsMaster
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
for|for
control|(
name|MasterProcedureManager
name|mpm
range|:
name|getProcedureManagers
argument_list|()
control|)
block|{
name|mpm
operator|.
name|stop
argument_list|(
name|why
argument_list|)
expr_stmt|;
block|}
block|}
specifier|public
name|MasterProcedureManager
name|getProcedureManager
parameter_list|(
name|String
name|signature
parameter_list|)
block|{
return|return
name|procedureMgrMap
operator|.
name|get
argument_list|(
name|signature
argument_list|)
return|;
block|}
block|}
end_class

end_unit

