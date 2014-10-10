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
name|classification
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

begin_comment
comment|/**  * JMX caches the beans that have been exported; even after the values are removed from hadoop's  * metrics system the keys and old values will still remain.  This class stops and restarts the  * Hadoop metrics system, forcing JMX to clear the cache of exported metrics.  *  * This class need to be in the o.a.h.metrics2.impl namespace as many of the variables/calls used  * are package private.  */
end_comment

begin_class
annotation|@
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|SuppressWarnings
argument_list|(
name|value
operator|=
literal|"LI_LAZY_INIT_STATIC"
argument_list|,
name|justification
operator|=
literal|"Yeah, its weird but its what we want"
argument_list|)
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
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|JmxCacheBuster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
name|Object
name|lock
init|=
operator|new
name|Object
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|ScheduledFuture
name|fut
init|=
literal|null
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
comment|/**    * For JMX to forget about all previously exported metrics.    */
specifier|public
specifier|static
name|void
name|clearJmxCache
parameter_list|()
block|{
comment|//If there are more then 100 ms before the executor will run then everything should be merged.
synchronized|synchronized
init|(
name|lock
init|)
block|{
if|if
condition|(
name|fut
operator|==
literal|null
operator|||
operator|(
operator|!
name|fut
operator|.
name|isDone
argument_list|()
operator|&&
name|fut
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
condition|)
return|return;
name|fut
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
block|}
block|}
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
name|LOG
operator|.
name|trace
argument_list|(
literal|"Clearing JMX mbean cache."
argument_list|)
expr_stmt|;
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

