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
name|hbtop
operator|.
name|screen
operator|.
name|top
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Objects
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

begin_comment
comment|/**  * Represents the summary of the metrics.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|Summary
block|{
specifier|private
specifier|final
name|String
name|currentTime
decl_stmt|;
specifier|private
specifier|final
name|String
name|version
decl_stmt|;
specifier|private
specifier|final
name|String
name|clusterId
decl_stmt|;
specifier|private
specifier|final
name|int
name|servers
decl_stmt|;
specifier|private
specifier|final
name|int
name|liveServers
decl_stmt|;
specifier|private
specifier|final
name|int
name|deadServers
decl_stmt|;
specifier|private
specifier|final
name|int
name|regionCount
decl_stmt|;
specifier|private
specifier|final
name|int
name|ritCount
decl_stmt|;
specifier|private
specifier|final
name|double
name|averageLoad
decl_stmt|;
specifier|private
specifier|final
name|long
name|aggregateRequestPerSecond
decl_stmt|;
specifier|public
name|Summary
parameter_list|(
name|String
name|currentTime
parameter_list|,
name|String
name|version
parameter_list|,
name|String
name|clusterId
parameter_list|,
name|int
name|servers
parameter_list|,
name|int
name|liveServers
parameter_list|,
name|int
name|deadServers
parameter_list|,
name|int
name|regionCount
parameter_list|,
name|int
name|ritCount
parameter_list|,
name|double
name|averageLoad
parameter_list|,
name|long
name|aggregateRequestPerSecond
parameter_list|)
block|{
name|this
operator|.
name|currentTime
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|currentTime
argument_list|)
expr_stmt|;
name|this
operator|.
name|version
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|version
argument_list|)
expr_stmt|;
name|this
operator|.
name|clusterId
operator|=
name|Objects
operator|.
name|requireNonNull
argument_list|(
name|clusterId
argument_list|)
expr_stmt|;
name|this
operator|.
name|servers
operator|=
name|servers
expr_stmt|;
name|this
operator|.
name|liveServers
operator|=
name|liveServers
expr_stmt|;
name|this
operator|.
name|deadServers
operator|=
name|deadServers
expr_stmt|;
name|this
operator|.
name|regionCount
operator|=
name|regionCount
expr_stmt|;
name|this
operator|.
name|ritCount
operator|=
name|ritCount
expr_stmt|;
name|this
operator|.
name|averageLoad
operator|=
name|averageLoad
expr_stmt|;
name|this
operator|.
name|aggregateRequestPerSecond
operator|=
name|aggregateRequestPerSecond
expr_stmt|;
block|}
specifier|public
name|String
name|getCurrentTime
parameter_list|()
block|{
return|return
name|currentTime
return|;
block|}
specifier|public
name|String
name|getVersion
parameter_list|()
block|{
return|return
name|version
return|;
block|}
specifier|public
name|String
name|getClusterId
parameter_list|()
block|{
return|return
name|clusterId
return|;
block|}
specifier|public
name|int
name|getServers
parameter_list|()
block|{
return|return
name|servers
return|;
block|}
specifier|public
name|int
name|getLiveServers
parameter_list|()
block|{
return|return
name|liveServers
return|;
block|}
specifier|public
name|int
name|getDeadServers
parameter_list|()
block|{
return|return
name|deadServers
return|;
block|}
specifier|public
name|int
name|getRegionCount
parameter_list|()
block|{
return|return
name|regionCount
return|;
block|}
specifier|public
name|int
name|getRitCount
parameter_list|()
block|{
return|return
name|ritCount
return|;
block|}
specifier|public
name|double
name|getAverageLoad
parameter_list|()
block|{
return|return
name|averageLoad
return|;
block|}
specifier|public
name|long
name|getAggregateRequestPerSecond
parameter_list|()
block|{
return|return
name|aggregateRequestPerSecond
return|;
block|}
block|}
end_class

end_unit

