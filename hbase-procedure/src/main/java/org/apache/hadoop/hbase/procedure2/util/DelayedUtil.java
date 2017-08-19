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
name|procedure2
operator|.
name|util
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|DelayQueue
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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceStability
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
name|EnvironmentEdgeManager
import|;
end_import

begin_comment
comment|// FIX namings. TODO.
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
specifier|final
class|class
name|DelayedUtil
block|{
specifier|private
name|DelayedUtil
parameter_list|()
block|{ }
comment|/**    * Add a timeout to a Delay    */
specifier|public
interface|interface
name|DelayedWithTimeout
extends|extends
name|Delayed
block|{
name|long
name|getTimeout
parameter_list|()
function_decl|;
block|}
comment|/**    * POISON implementation; used to mark special state: e.g. shutdown.    */
specifier|public
specifier|static
specifier|final
name|DelayedWithTimeout
name|DELAYED_POISON
init|=
operator|new
name|DelayedWithTimeout
argument_list|()
block|{
annotation|@
name|Override
specifier|public
name|long
name|getTimeout
parameter_list|()
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getDelay
parameter_list|(
specifier|final
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
specifier|final
name|Delayed
name|o
parameter_list|)
block|{
return|return
name|Long
operator|.
name|compare
argument_list|(
literal|0
argument_list|,
name|DelayedUtil
operator|.
name|getTimeout
argument_list|(
name|o
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
specifier|final
name|Object
name|other
parameter_list|)
block|{
return|return
name|this
operator|==
name|other
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
name|getClass
argument_list|()
operator|.
name|getSimpleName
argument_list|()
operator|+
literal|"(POISON)"
return|;
block|}
block|}
decl_stmt|;
comment|/**    * @return null (if an interrupt) or an instance of E; resets interrupt on calling thread.    */
specifier|public
specifier|static
parameter_list|<
name|E
extends|extends
name|Delayed
parameter_list|>
name|E
name|takeWithoutInterrupt
parameter_list|(
specifier|final
name|DelayQueue
argument_list|<
name|E
argument_list|>
name|queue
parameter_list|)
block|{
try|try
block|{
return|return
name|queue
operator|.
name|take
argument_list|()
return|;
block|}
catch|catch
parameter_list|(
name|InterruptedException
name|e
parameter_list|)
block|{
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|interrupt
argument_list|()
expr_stmt|;
return|return
literal|null
return|;
block|}
block|}
comment|/**    * @return Time remaining as milliseconds.    */
specifier|public
specifier|static
name|long
name|getRemainingTime
parameter_list|(
specifier|final
name|TimeUnit
name|resultUnit
parameter_list|,
specifier|final
name|long
name|timeout
parameter_list|)
block|{
specifier|final
name|long
name|currentTime
init|=
name|EnvironmentEdgeManager
operator|.
name|currentTime
argument_list|()
decl_stmt|;
if|if
condition|(
name|currentTime
operator|>=
name|timeout
condition|)
block|{
return|return
literal|0
return|;
block|}
return|return
name|resultUnit
operator|.
name|convert
argument_list|(
name|timeout
operator|-
name|currentTime
argument_list|,
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
specifier|public
specifier|static
name|int
name|compareDelayed
parameter_list|(
specifier|final
name|Delayed
name|o1
parameter_list|,
specifier|final
name|Delayed
name|o2
parameter_list|)
block|{
return|return
name|Long
operator|.
name|compare
argument_list|(
name|getTimeout
argument_list|(
name|o1
argument_list|)
argument_list|,
name|getTimeout
argument_list|(
name|o2
argument_list|)
argument_list|)
return|;
block|}
specifier|private
specifier|static
name|long
name|getTimeout
parameter_list|(
specifier|final
name|Delayed
name|o
parameter_list|)
block|{
assert|assert
name|o
operator|instanceof
name|DelayedWithTimeout
operator|:
literal|"expected DelayedWithTimeout instance, got "
operator|+
name|o
assert|;
return|return
operator|(
operator|(
name|DelayedWithTimeout
operator|)
name|o
operator|)
operator|.
name|getTimeout
argument_list|()
return|;
block|}
specifier|public
specifier|static
specifier|abstract
class|class
name|DelayedObject
implements|implements
name|DelayedWithTimeout
block|{
annotation|@
name|Override
specifier|public
name|long
name|getDelay
parameter_list|(
specifier|final
name|TimeUnit
name|unit
parameter_list|)
block|{
return|return
name|DelayedUtil
operator|.
name|getRemainingTime
argument_list|(
name|unit
argument_list|,
name|getTimeout
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|compareTo
parameter_list|(
specifier|final
name|Delayed
name|other
parameter_list|)
block|{
return|return
name|DelayedUtil
operator|.
name|compareDelayed
argument_list|(
name|this
argument_list|,
name|other
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
name|long
name|timeout
init|=
name|getTimeout
argument_list|()
decl_stmt|;
return|return
literal|"timeout="
operator|+
name|timeout
operator|+
literal|", delay="
operator|+
name|getDelay
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
return|;
block|}
block|}
specifier|public
specifier|static
specifier|abstract
class|class
name|DelayedContainer
parameter_list|<
name|T
parameter_list|>
extends|extends
name|DelayedObject
block|{
specifier|private
specifier|final
name|T
name|object
decl_stmt|;
specifier|public
name|DelayedContainer
parameter_list|(
specifier|final
name|T
name|object
parameter_list|)
block|{
name|this
operator|.
name|object
operator|=
name|object
expr_stmt|;
block|}
specifier|public
name|T
name|getObject
parameter_list|()
block|{
return|return
name|this
operator|.
name|object
return|;
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|equals
parameter_list|(
specifier|final
name|Object
name|other
parameter_list|)
block|{
if|if
condition|(
name|other
operator|==
name|this
condition|)
return|return
literal|true
return|;
if|if
condition|(
operator|!
operator|(
name|other
operator|instanceof
name|DelayedContainer
operator|)
condition|)
return|return
literal|false
return|;
return|return
name|Objects
operator|.
name|equals
argument_list|(
name|getObject
argument_list|()
argument_list|,
operator|(
operator|(
name|DelayedContainer
operator|)
name|other
operator|)
operator|.
name|getObject
argument_list|()
argument_list|)
return|;
block|}
annotation|@
name|Override
specifier|public
name|int
name|hashCode
parameter_list|()
block|{
return|return
name|object
operator|!=
literal|null
condition|?
name|object
operator|.
name|hashCode
argument_list|()
else|:
literal|0
return|;
block|}
annotation|@
name|Override
specifier|public
name|String
name|toString
parameter_list|()
block|{
return|return
literal|"containedObject="
operator|+
name|getObject
argument_list|()
operator|+
literal|", "
operator|+
name|super
operator|.
name|toString
argument_list|()
return|;
block|}
block|}
comment|/**    * Has a timeout.    */
specifier|public
specifier|static
class|class
name|DelayedContainerWithTimestamp
parameter_list|<
name|T
parameter_list|>
extends|extends
name|DelayedContainer
argument_list|<
name|T
argument_list|>
block|{
specifier|private
name|long
name|timeout
decl_stmt|;
specifier|public
name|DelayedContainerWithTimestamp
parameter_list|(
specifier|final
name|T
name|object
parameter_list|,
specifier|final
name|long
name|timeout
parameter_list|)
block|{
name|super
argument_list|(
name|object
argument_list|)
expr_stmt|;
name|setTimeout
argument_list|(
name|timeout
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getTimeout
parameter_list|()
block|{
return|return
name|timeout
return|;
block|}
specifier|public
name|void
name|setTimeout
parameter_list|(
specifier|final
name|long
name|timeout
parameter_list|)
block|{
name|this
operator|.
name|timeout
operator|=
name|timeout
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

