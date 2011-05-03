begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2011 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|monitoring
package|;
end_package

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|annotations
operator|.
name|VisibleForTesting
import|;
end_import

begin_class
class|class
name|MonitoredTaskImpl
implements|implements
name|MonitoredTask
block|{
specifier|private
name|long
name|startTime
decl_stmt|;
specifier|private
name|long
name|completionTimestamp
init|=
operator|-
literal|1
decl_stmt|;
specifier|private
name|String
name|status
decl_stmt|;
specifier|private
name|String
name|description
decl_stmt|;
specifier|private
name|State
name|state
init|=
name|State
operator|.
name|RUNNING
decl_stmt|;
specifier|public
name|MonitoredTaskImpl
parameter_list|()
block|{
name|startTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getStartTime
parameter_list|()
block|{
return|return
name|startTime
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getDescription
parameter_list|()
block|{
return|return
name|description
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|getStatus
parameter_list|()
block|{
return|return
name|status
return|;
block|}
annotation|@
name|Override
specifier|public
name|State
name|getState
parameter_list|()
block|{
return|return
name|state
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCompletionTimestamp
parameter_list|()
block|{
return|return
name|completionTimestamp
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|markComplete
parameter_list|(
name|String
name|status
parameter_list|)
block|{
name|state
operator|=
name|State
operator|.
name|COMPLETE
expr_stmt|;
name|setStatus
argument_list|(
name|status
argument_list|)
expr_stmt|;
name|completionTimestamp
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|abort
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|setStatus
argument_list|(
name|msg
argument_list|)
expr_stmt|;
name|state
operator|=
name|State
operator|.
name|ABORTED
expr_stmt|;
name|completionTimestamp
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setStatus
parameter_list|(
name|String
name|status
parameter_list|)
block|{
name|this
operator|.
name|status
operator|=
name|status
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|setDescription
parameter_list|(
name|String
name|description
parameter_list|)
block|{
name|this
operator|.
name|description
operator|=
name|description
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|cleanup
parameter_list|()
block|{
if|if
condition|(
name|state
operator|==
name|State
operator|.
name|RUNNING
condition|)
block|{
name|state
operator|=
name|State
operator|.
name|ABORTED
expr_stmt|;
name|completionTimestamp
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
block|}
comment|/**    * Force the completion timestamp backwards so that    * it expires now.    */
annotation|@
name|VisibleForTesting
name|void
name|expireNow
parameter_list|()
block|{
name|completionTimestamp
operator|-=
literal|180
operator|*
literal|1000
expr_stmt|;
block|}
block|}
end_class

end_unit

