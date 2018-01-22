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
name|master
package|;
end_package

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|hbase
operator|.
name|thirdparty
operator|.
name|com
operator|.
name|google
operator|.
name|protobuf
operator|.
name|Message
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
name|HConstants
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
name|shaded
operator|.
name|protobuf
operator|.
name|ProtobufUtil
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
name|RPCProtos
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
name|RegionServerStatusProtos
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
name|regionserver
operator|.
name|AnnotationReadingPriorityFunction
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
name|regionserver
operator|.
name|RSRpcServices
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

begin_comment
comment|/**  * Priority function specifically for the master.  *  * This doesn't make the super users always priority since that would make everything  * to the master into high priority.  *  * Specifically when reporting that a region is in transition master will try and edit the meta  * table. That edit will block the thread until successful. However if at the same time meta is  * also moving then we need to ensure that the regular region that's moving isn't blocking  * processing of the request to online meta. To accomplish this this priority function makes sure  * that all requests to transition meta are handled in different threads from other report region  * in transition calls.  */
end_comment

begin_class
specifier|public
class|class
name|MasterAnnotationReadingPriorityFunction
extends|extends
name|AnnotationReadingPriorityFunction
block|{
specifier|public
name|MasterAnnotationReadingPriorityFunction
parameter_list|(
specifier|final
name|RSRpcServices
name|rpcServices
parameter_list|)
block|{
name|this
argument_list|(
name|rpcServices
argument_list|,
name|rpcServices
operator|.
name|getClass
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|MasterAnnotationReadingPriorityFunction
parameter_list|(
name|RSRpcServices
name|rpcServices
parameter_list|,
name|Class
argument_list|<
name|?
extends|extends
name|RSRpcServices
argument_list|>
name|clz
parameter_list|)
block|{
name|super
argument_list|(
name|rpcServices
argument_list|,
name|clz
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|getPriority
parameter_list|(
name|RPCProtos
operator|.
name|RequestHeader
name|header
parameter_list|,
name|Message
name|param
parameter_list|,
name|User
name|user
parameter_list|)
block|{
comment|// Yes this is copy pasted from the base class but it keeps from having to look in the
comment|// annotatedQos table twice something that could get costly since this is called for
comment|// every single RPC request.
name|int
name|priorityByAnnotation
init|=
name|getAnnotatedPriority
argument_list|(
name|header
argument_list|)
decl_stmt|;
if|if
condition|(
name|priorityByAnnotation
operator|>=
literal|0
condition|)
block|{
return|return
name|priorityByAnnotation
return|;
block|}
comment|// If meta is moving then all the other of reports of state transitions will be
comment|// un able to edit meta. Those blocked reports should not keep the report that opens meta from
comment|// running. Hence all reports of meta transitioning should always be in a different thread.
comment|// This keeps from deadlocking the cluster.
if|if
condition|(
name|param
operator|instanceof
name|RegionServerStatusProtos
operator|.
name|ReportRegionStateTransitionRequest
condition|)
block|{
comment|// Regions are moving. Lets see which ones.
name|RegionServerStatusProtos
operator|.
name|ReportRegionStateTransitionRequest
name|tRequest
init|=
operator|(
name|RegionServerStatusProtos
operator|.
name|ReportRegionStateTransitionRequest
operator|)
name|param
decl_stmt|;
for|for
control|(
name|RegionServerStatusProtos
operator|.
name|RegionStateTransition
name|rst
range|:
name|tRequest
operator|.
name|getTransitionList
argument_list|()
control|)
block|{
if|if
condition|(
name|rst
operator|.
name|getRegionInfoList
argument_list|()
operator|!=
literal|null
condition|)
block|{
for|for
control|(
name|HBaseProtos
operator|.
name|RegionInfo
name|info
range|:
name|rst
operator|.
name|getRegionInfoList
argument_list|()
control|)
block|{
name|TableName
name|tn
init|=
name|ProtobufUtil
operator|.
name|toTableName
argument_list|(
name|info
operator|.
name|getTableName
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|tn
operator|.
name|isSystemTable
argument_list|()
condition|)
block|{
return|return
name|HConstants
operator|.
name|SYSTEMTABLE_QOS
return|;
block|}
block|}
block|}
block|}
return|return
name|HConstants
operator|.
name|NORMAL_QOS
return|;
block|}
comment|// Handle the rest of the different reasons to change priority.
return|return
name|getBasePriority
argument_list|(
name|header
argument_list|,
name|param
argument_list|)
return|;
block|}
block|}
end_class

end_unit

