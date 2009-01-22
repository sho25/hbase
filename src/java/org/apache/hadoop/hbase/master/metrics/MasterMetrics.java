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
name|master
operator|.
name|metrics
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
name|metrics
operator|.
name|MetricsContext
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
name|metrics
operator|.
name|MetricsRecord
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
name|metrics
operator|.
name|MetricsUtil
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
name|metrics
operator|.
name|Updater
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
name|metrics
operator|.
name|jvm
operator|.
name|JvmMetrics
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
name|metrics
operator|.
name|util
operator|.
name|MetricsIntValue
import|;
end_import

begin_comment
comment|/**   * This class is for maintaining the various master statistics  * and publishing them through the metrics interfaces.  *<p>  * This class has a number of metrics variables that are publicly accessible;  * these variables (objects) have methods to update their values.  */
end_comment

begin_class
specifier|public
class|class
name|MasterMetrics
implements|implements
name|Updater
block|{
specifier|private
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|this
operator|.
name|getClass
argument_list|()
argument_list|)
decl_stmt|;
specifier|private
specifier|final
name|MetricsRecord
name|metricsRecord
decl_stmt|;
comment|/*    * Count of requests to the cluster since last call to metrics update    */
specifier|private
specifier|final
name|MetricsIntValue
name|cluster_requests
init|=
operator|new
name|MetricsIntValue
argument_list|(
literal|"cluster_requests"
argument_list|)
decl_stmt|;
specifier|public
name|MasterMetrics
parameter_list|()
block|{
name|MetricsContext
name|context
init|=
name|MetricsUtil
operator|.
name|getContext
argument_list|(
literal|"hbase"
argument_list|)
decl_stmt|;
name|metricsRecord
operator|=
name|MetricsUtil
operator|.
name|createRecord
argument_list|(
name|context
argument_list|,
literal|"master"
argument_list|)
expr_stmt|;
name|String
name|name
init|=
name|Thread
operator|.
name|currentThread
argument_list|()
operator|.
name|getName
argument_list|()
decl_stmt|;
name|metricsRecord
operator|.
name|setTag
argument_list|(
literal|"Master"
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|context
operator|.
name|registerUpdater
argument_list|(
name|this
argument_list|)
expr_stmt|;
name|JvmMetrics
operator|.
name|init
argument_list|(
literal|"Master"
argument_list|,
name|name
argument_list|)
expr_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Initialized"
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|shutdown
parameter_list|()
block|{
comment|// nought to do.
block|}
comment|/**    * Since this object is a registered updater, this method will be called    * periodically, e.g. every 5 seconds.    */
specifier|public
name|void
name|doUpdates
parameter_list|(
name|MetricsContext
name|unused
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|cluster_requests
init|)
block|{
name|this
operator|.
name|cluster_requests
operator|.
name|pushMetric
argument_list|(
name|metricsRecord
argument_list|)
expr_stmt|;
comment|// Set requests down to zero again.
name|this
operator|.
name|cluster_requests
operator|.
name|set
argument_list|(
literal|0
argument_list|)
expr_stmt|;
block|}
block|}
name|this
operator|.
name|metricsRecord
operator|.
name|update
argument_list|()
expr_stmt|;
block|}
specifier|public
name|void
name|resetAllMinMax
parameter_list|()
block|{
comment|// Nothing to do
block|}
comment|/**    * @return Count of requests.    */
specifier|public
name|int
name|getRequests
parameter_list|()
block|{
return|return
name|this
operator|.
name|cluster_requests
operator|.
name|get
argument_list|()
return|;
block|}
comment|/**    * @param inc How much to add to requests.    */
specifier|public
name|void
name|incrementRequests
parameter_list|(
specifier|final
name|int
name|inc
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
operator|.
name|cluster_requests
init|)
block|{
name|this
operator|.
name|cluster_requests
operator|.
name|inc
argument_list|(
name|inc
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

