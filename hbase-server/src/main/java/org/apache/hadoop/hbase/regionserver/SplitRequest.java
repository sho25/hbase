begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|regionserver
package|;
end_package

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|PrivilegedAction
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
name|client
operator|.
name|RegionInfo
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
name|RegionInfoBuilder
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
name|RegionServerServices
operator|.
name|RegionStateTransitionContext
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
name|util
operator|.
name|Bytes
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
name|common
operator|.
name|base
operator|.
name|Preconditions
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
operator|.
name|RegionStateTransition
operator|.
name|TransitionCode
import|;
end_import

begin_comment
comment|/**  * Handles processing region splits. Put in a queue, owned by HRegionServer.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|SplitRequest
implements|implements
name|Runnable
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
name|SplitRequest
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|RegionInfo
name|parent
decl_stmt|;
specifier|private
specifier|final
name|byte
index|[]
name|midKey
decl_stmt|;
specifier|private
specifier|final
name|HRegionServer
name|server
decl_stmt|;
specifier|private
specifier|final
name|User
name|user
decl_stmt|;
name|SplitRequest
parameter_list|(
name|Region
name|region
parameter_list|,
name|byte
index|[]
name|midKey
parameter_list|,
name|HRegionServer
name|hrs
parameter_list|,
name|User
name|user
parameter_list|)
block|{
name|Preconditions
operator|.
name|checkNotNull
argument_list|(
name|hrs
argument_list|)
expr_stmt|;
name|this
operator|.
name|parent
operator|=
name|region
operator|.
name|getRegionInfo
argument_list|()
expr_stmt|;
name|this
operator|.
name|midKey
operator|=
name|midKey
expr_stmt|;
name|this
operator|.
name|server
operator|=
name|hrs
expr_stmt|;
name|this
operator|.
name|user
operator|=
name|user
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"regionName="
operator|+
name|parent
operator|+
literal|", midKey="
operator|+
name|Bytes
operator|.
name|toStringBinary
argument_list|(
name|midKey
argument_list|)
return|;
block|}
specifier|private
name|void
name|doSplitting
parameter_list|()
block|{
name|server
operator|.
name|getMetrics
argument_list|()
operator|.
name|incrSplitRequest
argument_list|()
expr_stmt|;
if|if
condition|(
name|user
operator|!=
literal|null
operator|&&
name|user
operator|.
name|getUGI
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|user
operator|.
name|getUGI
argument_list|()
operator|.
name|doAs
argument_list|(
call|(
name|PrivilegedAction
argument_list|<
name|Void
argument_list|>
call|)
argument_list|()
operator|->
block|{
name|requestRegionSplit
argument_list|()
block|;
return|return
literal|null
return|;
block|}
block|)
function|;
block|}
end_class

begin_else
else|else
block|{
name|requestRegionSplit
argument_list|()
expr_stmt|;
block|}
end_else

begin_function
unit|}    private
name|void
name|requestRegionSplit
parameter_list|()
block|{
specifier|final
name|TableName
name|table
init|=
name|parent
operator|.
name|getTable
argument_list|()
decl_stmt|;
specifier|final
name|RegionInfo
name|hri_a
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|table
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|parent
operator|.
name|getStartKey
argument_list|()
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|midKey
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
specifier|final
name|RegionInfo
name|hri_b
init|=
name|RegionInfoBuilder
operator|.
name|newBuilder
argument_list|(
name|table
argument_list|)
operator|.
name|setStartKey
argument_list|(
name|midKey
argument_list|)
operator|.
name|setEndKey
argument_list|(
name|parent
operator|.
name|getEndKey
argument_list|()
argument_list|)
operator|.
name|build
argument_list|()
decl_stmt|;
comment|// Send the split request to the master. the master will do the validation on the split-key.
comment|// The parent region will be unassigned and the two new regions will be assigned.
comment|// hri_a and hri_b objects may not reflect the regions that will be created, those objects
comment|// are created just to pass the information to the reportRegionStateTransition().
if|if
condition|(
operator|!
name|server
operator|.
name|reportRegionStateTransition
argument_list|(
operator|new
name|RegionStateTransitionContext
argument_list|(
name|TransitionCode
operator|.
name|READY_TO_SPLIT
argument_list|,
name|HConstants
operator|.
name|NO_SEQNUM
argument_list|,
operator|-
literal|1
argument_list|,
name|parent
argument_list|,
name|hri_a
argument_list|,
name|hri_b
argument_list|)
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|error
argument_list|(
literal|"Unable to ask master to split "
operator|+
name|parent
operator|.
name|getRegionNameAsString
argument_list|()
argument_list|)
expr_stmt|;
block|}
block|}
end_function

begin_function
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
if|if
condition|(
name|this
operator|.
name|server
operator|.
name|isStopping
argument_list|()
operator|||
name|this
operator|.
name|server
operator|.
name|isStopped
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"Skipping split because server is stopping="
operator|+
name|this
operator|.
name|server
operator|.
name|isStopping
argument_list|()
operator|+
literal|" or stopped="
operator|+
name|this
operator|.
name|server
operator|.
name|isStopped
argument_list|()
argument_list|)
expr_stmt|;
return|return;
block|}
name|doSplitting
argument_list|()
expr_stmt|;
block|}
end_function

unit|}
end_unit

