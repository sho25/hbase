begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements. See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership. The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License. You may obtain a copy of the License at  *  *   http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing,  * software distributed under the License is distributed on an  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY  * KIND, either express or implied. See the License for the  * specific language governing permissions and limitations  * under the License.  */
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
name|thrift
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Locale
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
name|conf
operator|.
name|Configuration
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|server
operator|.
name|TThreadedSelectorServer
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|thrift
operator|.
name|transport
operator|.
name|TNonblockingServerTransport
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
comment|/**  * A TThreadedSelectorServer.Args that reads hadoop configuration  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|HThreadedSelectorServerArgs
extends|extends
name|TThreadedSelectorServer
operator|.
name|Args
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
name|TThreadedSelectorServer
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Number of selector threads for reading and writing socket    */
specifier|public
specifier|static
specifier|final
name|String
name|SELECTOR_THREADS_CONF_KEY
init|=
literal|"hbase.thrift.selector.threads"
decl_stmt|;
comment|/**    * Number fo threads for processing the thrift calls    */
specifier|public
specifier|static
specifier|final
name|String
name|WORKER_THREADS_CONF_KEY
init|=
literal|"hbase.thrift.worker.threads"
decl_stmt|;
comment|/**    * Time to wait for server to stop gracefully    */
specifier|public
specifier|static
specifier|final
name|String
name|STOP_TIMEOUT_CONF_KEY
init|=
literal|"hbase.thrift.stop.timeout.seconds"
decl_stmt|;
comment|/**    * Maximum number of accepted elements per selector    */
specifier|public
specifier|static
specifier|final
name|String
name|ACCEPT_QUEUE_SIZE_PER_THREAD_CONF_KEY
init|=
literal|"hbase.thrift.accept.queue.size.per.selector"
decl_stmt|;
comment|/**    * The strategy for handling new accepted connections.    */
specifier|public
specifier|static
specifier|final
name|String
name|ACCEPT_POLICY_CONF_KEY
init|=
literal|"hbase.thrift.accept.policy"
decl_stmt|;
specifier|public
name|HThreadedSelectorServerArgs
parameter_list|(
name|TNonblockingServerTransport
name|transport
parameter_list|,
name|Configuration
name|conf
parameter_list|)
block|{
name|super
argument_list|(
name|transport
argument_list|)
expr_stmt|;
name|readConf
argument_list|(
name|conf
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|readConf
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|int
name|selectorThreads
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|SELECTOR_THREADS_CONF_KEY
argument_list|,
name|getSelectorThreads
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|workerThreads
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|WORKER_THREADS_CONF_KEY
argument_list|,
name|getWorkerThreads
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|stopTimeoutVal
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|STOP_TIMEOUT_CONF_KEY
argument_list|,
name|getStopTimeoutVal
argument_list|()
argument_list|)
decl_stmt|;
name|int
name|acceptQueueSizePerThread
init|=
name|conf
operator|.
name|getInt
argument_list|(
name|ACCEPT_QUEUE_SIZE_PER_THREAD_CONF_KEY
argument_list|,
name|getAcceptQueueSizePerThread
argument_list|()
argument_list|)
decl_stmt|;
name|AcceptPolicy
name|acceptPolicy
init|=
name|AcceptPolicy
operator|.
name|valueOf
argument_list|(
name|conf
operator|.
name|get
argument_list|(
name|ACCEPT_POLICY_CONF_KEY
argument_list|,
name|getAcceptPolicy
argument_list|()
operator|.
name|toString
argument_list|()
argument_list|)
operator|.
name|toUpperCase
argument_list|(
name|Locale
operator|.
name|ROOT
argument_list|)
argument_list|)
decl_stmt|;
name|super
operator|.
name|selectorThreads
argument_list|(
name|selectorThreads
argument_list|)
operator|.
name|workerThreads
argument_list|(
name|workerThreads
argument_list|)
operator|.
name|stopTimeoutVal
argument_list|(
name|stopTimeoutVal
argument_list|)
operator|.
name|acceptQueueSizePerThread
argument_list|(
name|acceptQueueSizePerThread
argument_list|)
operator|.
name|acceptPolicy
argument_list|(
name|acceptPolicy
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Read configuration selectorThreads:"
operator|+
name|selectorThreads
operator|+
literal|" workerThreads:"
operator|+
name|workerThreads
operator|+
literal|" stopTimeoutVal:"
operator|+
name|stopTimeoutVal
operator|+
literal|"sec"
operator|+
literal|" acceptQueueSizePerThread:"
operator|+
name|acceptQueueSizePerThread
operator|+
literal|" acceptPolicy:"
operator|+
name|acceptPolicy
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

