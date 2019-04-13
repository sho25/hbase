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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|security
operator|.
name|User
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
name|security
operator|.
name|access
operator|.
name|AccessChecker
import|;
end_import

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
name|Stoppable
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
name|shaded
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

begin_comment
comment|/** * A life-cycle management interface for globally barriered procedures on master. * See the following doc on details of globally barriered procedure: * https://issues.apache.org/jira/secure/attachment/12555103/121127-global-barrier-proc.pdf * * To implement a custom globally barriered procedure, user needs to extend two classes: * {@link MasterProcedureManager} and {@link RegionServerProcedureManager}. Implementation of * {@link MasterProcedureManager} is loaded into {@link org.apache.hadoop.hbase.master.HMaster} * process via configuration parameter 'hbase.procedure.master.classes', while implementation of * {@link RegionServerProcedureManager} is loaded into * {@link org.apache.hadoop.hbase.regionserver.HRegionServer} process via * configuration parameter 'hbase.procedure.regionserver.classes'. * * An example of globally barriered procedure implementation is * {@link org.apache.hadoop.hbase.master.snapshot.SnapshotManager} and * {@link org.apache.hadoop.hbase.regionserver.snapshot.RegionServerSnapshotManager}. * * A globally barriered procedure is identified by its signature (usually it is the name of the * procedure znode). During the initialization phase, the initialize methods are called by both * {@link org.apache.hadoop.hbase.master.HMaster} * and {@link org.apache.hadoop.hbase.regionserver.HRegionServer} which create the procedure znode * and register the listeners. A procedure can be triggered by its signature and an instant name * (encapsulated in a {@link ProcedureDescription} object). When the servers are shutdown, * the stop methods on both classes are called to clean up the data associated with the procedure. */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|MasterProcedureManager
extends|extends
name|ProcedureManager
implements|implements
name|Stoppable
block|{
comment|/**    * Initialize a globally barriered procedure for master.    *    * @param master Master service interface    * @throws KeeperException    * @throws IOException    * @throws UnsupportedOperationException    */
specifier|public
specifier|abstract
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
function_decl|;
comment|/**    * Execute a distributed procedure on cluster    *    * @param desc Procedure description    * @throws IOException    */
specifier|public
name|void
name|execProcedure
parameter_list|(
name|ProcedureDescription
name|desc
parameter_list|)
throws|throws
name|IOException
block|{}
comment|/**    * Execute a distributed procedure on cluster with return data.    *    * @param desc Procedure description    * @return data returned from the procedure execution, null if no data    * @throws IOException    */
specifier|public
name|byte
index|[]
name|execProcedureWithRet
parameter_list|(
name|ProcedureDescription
name|desc
parameter_list|)
throws|throws
name|IOException
block|{
return|return
literal|null
return|;
block|}
comment|/**    * Check for required permissions before executing the procedure.    * @throws IOException if permissions requirements are not met.    */
specifier|public
specifier|abstract
name|void
name|checkPermissions
parameter_list|(
name|ProcedureDescription
name|desc
parameter_list|,
name|AccessChecker
name|accessChecker
parameter_list|,
name|User
name|user
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Check if the procedure is finished successfully    *    * @param desc Procedure description    * @return true if the specified procedure is finished successfully    * @throws IOException    */
specifier|public
specifier|abstract
name|boolean
name|isProcedureDone
parameter_list|(
name|ProcedureDescription
name|desc
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

