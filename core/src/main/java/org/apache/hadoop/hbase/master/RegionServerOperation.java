begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|HConstants
import|;
end_import

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
name|concurrent
operator|.
name|Delayed
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
name|TimeUnit
import|;
end_import

begin_class
specifier|abstract
class|class
name|RegionServerOperation
implements|implements
name|Delayed
implements|,
name|HConstants
block|{
specifier|protected
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|RegionServerOperation
operator|.
name|class
operator|.
name|getName
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
name|long
name|expire
decl_stmt|;
specifier|protected
specifier|final
name|HMaster
name|master
decl_stmt|;
specifier|final
name|int
name|delay
decl_stmt|;
specifier|protected
name|RegionServerOperation
parameter_list|(
name|HMaster
name|master
parameter_list|)
block|{
name|this
operator|.
name|master
operator|=
name|master
expr_stmt|;
name|this
operator|.
name|delay
operator|=
name|this
operator|.
name|master
operator|.
name|getConfiguration
argument_list|()
operator|.
name|getInt
argument_list|(
literal|"hbase.server.thread.wakefrequency"
argument_list|,
literal|10
operator|*
literal|1000
argument_list|)
expr_stmt|;
comment|// Set the future time at which we expect to be released from the
comment|// DelayQueue we're inserted in on lease expiration.
name|this
operator|.
name|expire
operator|=
name|whenToExpire
argument_list|()
expr_stmt|;
block|}
specifier|public
name|long
name|getDelay
parameter_list|(
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
name|unit
operator|.
name|convert
argument_list|(
name|this
operator|.
name|expire
operator|-
name|System
operator|.
name|currentTimeMillis
argument_list|()
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
specifier|public
name|int
name|compareTo
parameter_list|(
name|Delayed
name|o
parameter_list|)
block|{
return|return
name|Long
operator|.
name|valueOf
argument_list|(
name|getDelay
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|-
name|o
operator|.
name|getDelay
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
argument_list|)
operator|.
name|intValue
argument_list|()
return|;
block|}
specifier|protected
name|void
name|requeue
parameter_list|()
block|{
name|this
operator|.
name|expire
operator|=
name|whenToExpire
argument_list|()
expr_stmt|;
name|this
operator|.
name|master
operator|.
name|requeue
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
specifier|private
name|long
name|whenToExpire
parameter_list|()
block|{
return|return
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|+
name|this
operator|.
name|delay
return|;
block|}
specifier|protected
name|boolean
name|rootAvailable
parameter_list|()
block|{
name|boolean
name|available
init|=
literal|true
decl_stmt|;
if|if
condition|(
name|this
operator|.
name|master
operator|.
name|getRegionManager
argument_list|()
operator|.
name|getRootRegionLocation
argument_list|()
operator|==
literal|null
condition|)
block|{
name|available
operator|=
literal|false
expr_stmt|;
name|requeue
argument_list|()
expr_stmt|;
block|}
return|return
name|available
return|;
block|}
specifier|protected
name|boolean
name|metaTableAvailable
parameter_list|()
block|{
name|boolean
name|available
init|=
literal|true
decl_stmt|;
if|if
condition|(
operator|(
name|master
operator|.
name|getRegionManager
argument_list|()
operator|.
name|numMetaRegions
argument_list|()
operator|!=
name|master
operator|.
name|getRegionManager
argument_list|()
operator|.
name|numOnlineMetaRegions
argument_list|()
operator|)
operator|||
name|master
operator|.
name|getRegionManager
argument_list|()
operator|.
name|metaRegionsInTransition
argument_list|()
condition|)
block|{
comment|// We can't proceed because not all of the meta regions are online.
comment|// We can't block either because that would prevent the meta region
comment|// online message from being processed. In order to prevent spinning
comment|// in the run queue, put this request on the delay queue to give
comment|// other threads the opportunity to get the meta regions on-line.
if|if
condition|(
name|LOG
operator|.
name|isDebugEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"numberOfMetaRegions: "
operator|+
name|master
operator|.
name|getRegionManager
argument_list|()
operator|.
name|numMetaRegions
argument_list|()
operator|+
literal|", onlineMetaRegions.size(): "
operator|+
name|master
operator|.
name|getRegionManager
argument_list|()
operator|.
name|numOnlineMetaRegions
argument_list|()
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|debug
argument_list|(
literal|"Requeuing because not all meta regions are online"
argument_list|)
expr_stmt|;
block|}
name|available
operator|=
literal|false
expr_stmt|;
name|requeue
argument_list|()
expr_stmt|;
block|}
return|return
name|available
return|;
block|}
specifier|public
name|int
name|compareTo
parameter_list|(
name|RegionServerOperation
name|other
parameter_list|)
block|{
return|return
name|getPriority
argument_list|()
operator|-
name|other
operator|.
name|getPriority
argument_list|()
return|;
block|}
comment|// the Priority of this operation, 0 is lowest priority
specifier|protected
name|int
name|getPriority
parameter_list|()
block|{
return|return
name|Integer
operator|.
name|MAX_VALUE
return|;
block|}
specifier|protected
specifier|abstract
name|boolean
name|process
parameter_list|()
throws|throws
name|IOException
function_decl|;
block|}
end_class

end_unit

