begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|replication
operator|.
name|regionserver
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
name|hbase
operator|.
name|metrics
operator|.
name|MetricsRate
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
name|MetricsLongValue
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
name|MetricsRegistry
import|;
end_import

begin_comment
comment|/**  * This class is for maintaining the various replication statistics  * for a sink and publishing them through the metrics interfaces.  */
end_comment

begin_class
specifier|public
class|class
name|ReplicationSinkMetrics
implements|implements
name|Updater
block|{
specifier|private
specifier|final
name|MetricsRecord
name|metricsRecord
decl_stmt|;
specifier|private
name|MetricsRegistry
name|registry
init|=
operator|new
name|MetricsRegistry
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|ReplicationSinkMetrics
name|instance
decl_stmt|;
comment|/** Rate of operations applied by the sink */
specifier|public
specifier|final
name|MetricsRate
name|appliedOpsRate
init|=
operator|new
name|MetricsRate
argument_list|(
literal|"appliedOpsRate"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/** Rate of batches (of operations) applied by the sink */
specifier|public
specifier|final
name|MetricsRate
name|appliedBatchesRate
init|=
operator|new
name|MetricsRate
argument_list|(
literal|"appliedBatchesRate"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/** Age of the last operation that was applied by the sink */
specifier|private
specifier|final
name|MetricsLongValue
name|ageOfLastAppliedOp
init|=
operator|new
name|MetricsLongValue
argument_list|(
literal|"ageOfLastAppliedOp"
argument_list|,
name|registry
argument_list|)
decl_stmt|;
comment|/**    * Constructor used to register the metrics    */
specifier|public
name|ReplicationSinkMetrics
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
operator|=
name|MetricsUtil
operator|.
name|createRecord
argument_list|(
name|context
argument_list|,
literal|"replication"
argument_list|)
expr_stmt|;
name|metricsRecord
operator|.
name|setTag
argument_list|(
literal|"RegionServer"
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
comment|// Add jvmmetrics.
name|JvmMetrics
operator|.
name|init
argument_list|(
literal|"RegionServer"
argument_list|,
name|name
argument_list|)
expr_stmt|;
comment|// export for JMX
operator|new
name|ReplicationStatistics
argument_list|(
name|this
operator|.
name|registry
argument_list|,
literal|"ReplicationSink"
argument_list|)
expr_stmt|;
block|}
comment|/**    * Set the age of the last edit that was applied    * @param timestamp write time of the edit    */
specifier|public
name|void
name|setAgeOfLastAppliedOp
parameter_list|(
name|long
name|timestamp
parameter_list|)
block|{
name|ageOfLastAppliedOp
operator|.
name|set
argument_list|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|timestamp
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|doUpdates
parameter_list|(
name|MetricsContext
name|metricsContext
parameter_list|)
block|{
synchronized|synchronized
init|(
name|this
init|)
block|{
name|this
operator|.
name|appliedOpsRate
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|appliedBatchesRate
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
name|this
operator|.
name|ageOfLastAppliedOp
operator|.
name|pushMetric
argument_list|(
name|this
operator|.
name|metricsRecord
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

