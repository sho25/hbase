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
name|metrics2
operator|.
name|impl
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|ScheduledFuture
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
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicBoolean
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
name|atomic
operator|.
name|AtomicReference
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
name|hadoop
operator|.
name|metrics2
operator|.
name|MetricsExecutor
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
name|metrics2
operator|.
name|lib
operator|.
name|DefaultMetricsSystem
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
name|metrics2
operator|.
name|lib
operator|.
name|MetricsExecutorImpl
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
name|util
operator|.
name|StringUtils
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

begin_comment
comment|/**  * JMX caches the beans that have been exported; even after the values are removed from hadoop's  * metrics system the keys and old values will still remain.  This class stops and restarts the  * Hadoop metrics system, forcing JMX to clear the cache of exported metrics.  *  * This class need to be in the o.a.h.metrics2.impl namespace as many of the variables/calls used  * are package private.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|JmxCacheBuster
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
name|JmxCacheBuster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|AtomicReference
argument_list|<
name|ScheduledFuture
argument_list|>
name|fut
init|=
operator|new
name|AtomicReference
argument_list|<>
argument_list|(
literal|null
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|MetricsExecutor
name|executor
init|=
operator|new
name|MetricsExecutorImpl
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|AtomicBoolean
name|stopped
init|=
operator|new
name|AtomicBoolean
argument_list|(
literal|false
argument_list|)
decl_stmt|;
specifier|private
name|JmxCacheBuster
parameter_list|()
block|{
comment|// Static only cache.
block|}
comment|/**    * For JMX to forget about all previously exported metrics.    */
specifier|public
specifier|static
name|void
name|clearJmxCache
parameter_list|()
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"clearing JMX Cache"
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
operator|new
name|Exception
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
block|}
comment|//If there are more then 100 ms before the executor will run then everything should be merged.
name|ScheduledFuture
name|future
init|=
name|fut
operator|.
name|get
argument_list|()
decl_stmt|;
if|if
condition|(
operator|(
name|future
operator|!=
literal|null
operator|&&
operator|(
operator|!
name|future
operator|.
name|isDone
argument_list|()
operator|&&
name|future
operator|.
name|getDelay
argument_list|(
name|TimeUnit
operator|.
name|MILLISECONDS
argument_list|)
operator|>
literal|100
operator|)
operator|)
condition|)
block|{
comment|// BAIL OUT
return|return;
block|}
if|if
condition|(
name|stopped
operator|.
name|get
argument_list|()
condition|)
block|{
return|return;
block|}
name|future
operator|=
name|executor
operator|.
name|getExecutor
argument_list|()
operator|.
name|schedule
argument_list|(
operator|new
name|JmxCacheBusterRunnable
argument_list|()
argument_list|,
literal|5
argument_list|,
name|TimeUnit
operator|.
name|SECONDS
argument_list|)
expr_stmt|;
name|fut
operator|.
name|set
argument_list|(
name|future
argument_list|)
expr_stmt|;
block|}
comment|/**    * Stops the clearing of JMX metrics and restarting the Hadoop metrics system. This is needed for    * some test environments where we manually inject sources or sinks dynamically.    */
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|void
name|stop
parameter_list|()
block|{
name|stopped
operator|.
name|set
argument_list|(
literal|true
argument_list|)
expr_stmt|;
name|ScheduledFuture
name|future
init|=
name|fut
operator|.
name|get
argument_list|()
decl_stmt|;
name|future
operator|.
name|cancel
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
comment|/**    * Restarts the stopped service.    * @see #stop()    */
annotation|@
name|VisibleForTesting
specifier|public
specifier|static
name|void
name|restart
parameter_list|()
block|{
name|stopped
operator|.
name|set
argument_list|(
literal|false
argument_list|)
expr_stmt|;
block|}
specifier|final
specifier|static
class|class
name|JmxCacheBusterRunnable
implements|implements
name|Runnable
block|{
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
if|if
condition|(
name|LOG
operator|.
name|isTraceEnabled
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|trace
argument_list|(
literal|"Clearing JMX mbean cache."
argument_list|)
expr_stmt|;
block|}
comment|// This is pretty extreme but it's the best way that
comment|// I could find to get metrics to be removed.
try|try
block|{
if|if
condition|(
name|DefaultMetricsSystem
operator|.
name|instance
argument_list|()
operator|!=
literal|null
condition|)
block|{
name|DefaultMetricsSystem
operator|.
name|instance
argument_list|()
operator|.
name|stop
argument_list|()
expr_stmt|;
comment|// Sleep some time so that the rest of the hadoop metrics
comment|// system knows that things are done
name|Thread
operator|.
name|sleep
argument_list|(
literal|500
argument_list|)
expr_stmt|;
name|DefaultMetricsSystem
operator|.
name|instance
argument_list|()
operator|.
name|start
argument_list|()
expr_stmt|;
block|}
block|}
catch|catch
parameter_list|(
name|Exception
name|exception
parameter_list|)
block|{
name|LOG
operator|.
name|debug
argument_list|(
literal|"error clearing the jmx it appears the metrics system hasn't been started"
argument_list|,
name|exception
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

