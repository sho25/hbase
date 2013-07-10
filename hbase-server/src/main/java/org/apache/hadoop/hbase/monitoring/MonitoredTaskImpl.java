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
name|monitoring
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_import
import|import
name|org
operator|.
name|codehaus
operator|.
name|jackson
operator|.
name|map
operator|.
name|ObjectMapper
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
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
name|statusTime
decl_stmt|;
specifier|private
name|long
name|stateTime
decl_stmt|;
specifier|private
specifier|volatile
name|String
name|status
decl_stmt|;
specifier|private
specifier|volatile
name|String
name|description
decl_stmt|;
specifier|protected
specifier|volatile
name|State
name|state
init|=
name|State
operator|.
name|RUNNING
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|ObjectMapper
name|MAPPER
init|=
operator|new
name|ObjectMapper
argument_list|()
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
name|statusTime
operator|=
name|startTime
expr_stmt|;
name|stateTime
operator|=
name|startTime
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
specifier|synchronized
name|MonitoredTaskImpl
name|clone
parameter_list|()
block|{
try|try
block|{
return|return
operator|(
name|MonitoredTaskImpl
operator|)
name|super
operator|.
name|clone
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|CloneNotSupportedException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|AssertionError
argument_list|()
throw|;
comment|// Won't happen
block|}
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
name|long
name|getStatusTime
parameter_list|()
block|{
return|return
name|statusTime
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
name|getStateTime
parameter_list|()
block|{
return|return
name|stateTime
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getCompletionTimestamp
parameter_list|()
block|{
if|if
condition|(
name|state
operator|==
name|State
operator|.
name|COMPLETE
operator|||
name|state
operator|==
name|State
operator|.
name|ABORTED
condition|)
block|{
return|return
name|stateTime
return|;
block|}
return|return
operator|-
literal|1
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
name|setState
argument_list|(
name|State
operator|.
name|COMPLETE
argument_list|)
expr_stmt|;
name|setStatus
argument_list|(
name|status
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|pause
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|setState
argument_list|(
name|State
operator|.
name|WAITING
argument_list|)
expr_stmt|;
name|setStatus
argument_list|(
name|msg
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|resume
parameter_list|(
name|String
name|msg
parameter_list|)
block|{
name|setState
argument_list|(
name|State
operator|.
name|RUNNING
argument_list|)
expr_stmt|;
name|setStatus
argument_list|(
name|msg
argument_list|)
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
name|setState
argument_list|(
name|State
operator|.
name|ABORTED
argument_list|)
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
name|statusTime
operator|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
expr_stmt|;
block|}
specifier|protected
name|void
name|setState
parameter_list|(
name|State
name|state
parameter_list|)
block|{
name|this
operator|.
name|state
operator|=
name|state
expr_stmt|;
name|stateTime
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
name|setState
argument_list|(
name|State
operator|.
name|ABORTED
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * Force the completion timestamp backwards so that    * it expires now.    */
specifier|public
name|void
name|expireNow
parameter_list|()
block|{
name|stateTime
operator|-=
literal|180
operator|*
literal|1000
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|toMap
parameter_list|()
block|{
name|Map
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
name|map
init|=
operator|new
name|HashMap
argument_list|<
name|String
argument_list|,
name|Object
argument_list|>
argument_list|()
decl_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"description"
argument_list|,
name|getDescription
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"status"
argument_list|,
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"state"
argument_list|,
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"starttimems"
argument_list|,
name|getStartTime
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"statustimems"
argument_list|,
name|getCompletionTimestamp
argument_list|()
argument_list|)
expr_stmt|;
name|map
operator|.
name|put
argument_list|(
literal|"statetimems"
argument_list|,
name|getCompletionTimestamp
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|map
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toJSON
parameter_list|()
throws|throws
name|IOException
block|{
return|return
name|MAPPER
operator|.
name|writeValueAsString
argument_list|(
name|toMap
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
name|StringBuilder
name|sb
init|=
operator|new
name|StringBuilder
argument_list|(
literal|512
argument_list|)
decl_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|getDescription
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|": status="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|getStatus
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", state="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|getState
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", startTime="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|getStartTime
argument_list|()
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
literal|", completionTime="
argument_list|)
expr_stmt|;
name|sb
operator|.
name|append
argument_list|(
name|getCompletionTimestamp
argument_list|()
argument_list|)
expr_stmt|;
return|return
name|sb
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
end_class

end_unit

