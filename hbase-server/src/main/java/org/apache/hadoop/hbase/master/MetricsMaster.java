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
name|hbase
operator|.
name|classification
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
name|CompatibilitySingletonFactory
import|;
end_import

begin_comment
comment|/**  * This class is for maintaining the various master statistics  * and publishing them through the metrics interfaces.  *<p>  * This class has a number of metrics variables that are publicly accessible;  * these variables (objects) have methods to update their values.  */
end_comment

begin_class
annotation|@
name|InterfaceStability
operator|.
name|Evolving
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|MetricsMaster
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
name|MetricsMaster
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|MetricsMasterSource
name|masterSource
decl_stmt|;
specifier|private
name|MetricsMasterProcSource
name|masterProcSource
decl_stmt|;
specifier|public
name|MetricsMaster
parameter_list|(
name|MetricsMasterWrapper
name|masterWrapper
parameter_list|)
block|{
name|masterSource
operator|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsMasterSourceFactory
operator|.
name|class
argument_list|)
operator|.
name|create
argument_list|(
name|masterWrapper
argument_list|)
expr_stmt|;
name|masterProcSource
operator|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsMasterProcSourceFactory
operator|.
name|class
argument_list|)
operator|.
name|create
argument_list|(
name|masterWrapper
argument_list|)
expr_stmt|;
block|}
comment|// for unit-test usage
specifier|public
name|MetricsMasterSource
name|getMetricsSource
parameter_list|()
block|{
return|return
name|masterSource
return|;
block|}
specifier|public
name|MetricsMasterProcSource
name|getMetricsProcSource
parameter_list|()
block|{
return|return
name|masterProcSource
return|;
block|}
comment|/**    * @param inc How much to add to requests.    */
specifier|public
name|void
name|incrementRequests
parameter_list|(
specifier|final
name|long
name|inc
parameter_list|)
block|{
name|masterSource
operator|.
name|incRequests
argument_list|(
name|inc
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

