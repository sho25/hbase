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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|Thread
operator|.
name|UncaughtExceptionHandler
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
comment|/**  * Abstract class which contains a Thread and delegates the common Thread  * methods to that instance.  *   * The purpose of this class is to workaround Sun JVM bug #6915621, in which  * something internal to the JDK uses Thread.currentThread() as a monitor  * lock. This can produce deadlocks like HBASE-4367, HBASE-4101, etc.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|HasThread
implements|implements
name|Runnable
block|{
specifier|private
specifier|final
name|Thread
name|thread
decl_stmt|;
specifier|public
name|HasThread
parameter_list|()
block|{
name|this
operator|.
name|thread
operator|=
operator|new
name|Thread
argument_list|(
name|this
argument_list|)
expr_stmt|;
block|}
specifier|public
name|HasThread
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|this
operator|.
name|thread
operator|=
operator|new
name|Thread
argument_list|(
name|this
argument_list|,
name|name
argument_list|)
expr_stmt|;
block|}
specifier|public
name|Thread
name|getThread
parameter_list|()
block|{
return|return
name|thread
return|;
block|}
annotation|@
name|Override
specifier|public
specifier|abstract
name|void
name|run
parameter_list|()
function_decl|;
comment|//// Begin delegation to Thread
specifier|public
specifier|final
name|String
name|getName
parameter_list|()
block|{
return|return
name|thread
operator|.
name|getName
argument_list|()
return|;
block|}
specifier|public
name|void
name|interrupt
parameter_list|()
block|{
name|thread
operator|.
name|interrupt
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|final
name|boolean
name|isAlive
parameter_list|()
block|{
return|return
name|thread
operator|.
name|isAlive
argument_list|()
return|;
block|}
specifier|public
name|boolean
name|isInterrupted
parameter_list|()
block|{
return|return
name|thread
operator|.
name|isInterrupted
argument_list|()
return|;
block|}
specifier|public
specifier|final
name|void
name|setDaemon
parameter_list|(
name|boolean
name|on
parameter_list|)
block|{
name|thread
operator|.
name|setDaemon
argument_list|(
name|on
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|final
name|void
name|setName
parameter_list|(
name|String
name|name
parameter_list|)
block|{
name|thread
operator|.
name|setName
argument_list|(
name|name
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|final
name|void
name|setPriority
parameter_list|(
name|int
name|newPriority
parameter_list|)
block|{
name|thread
operator|.
name|setPriority
argument_list|(
name|newPriority
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|setUncaughtExceptionHandler
parameter_list|(
name|UncaughtExceptionHandler
name|eh
parameter_list|)
block|{
name|thread
operator|.
name|setUncaughtExceptionHandler
argument_list|(
name|eh
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|start
parameter_list|()
block|{
name|thread
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|final
name|void
name|join
parameter_list|()
throws|throws
name|InterruptedException
block|{
name|thread
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
specifier|public
specifier|final
name|void
name|join
parameter_list|(
name|long
name|millis
parameter_list|,
name|int
name|nanos
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|thread
operator|.
name|join
argument_list|(
name|millis
argument_list|,
name|nanos
argument_list|)
expr_stmt|;
block|}
specifier|public
specifier|final
name|void
name|join
parameter_list|(
name|long
name|millis
parameter_list|)
throws|throws
name|InterruptedException
block|{
name|thread
operator|.
name|join
argument_list|(
name|millis
argument_list|)
expr_stmt|;
block|}
comment|//// End delegation to Thread
block|}
end_class

end_unit

