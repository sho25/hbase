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
name|replication
operator|.
name|regionserver
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
name|hadoop
operator|.
name|hbase
operator|.
name|metrics
operator|.
name|BaseMetricsSource
import|;
end_import

begin_comment
comment|/**  * Provides access to gauges and counters. Implementers will hide the details of hadoop1 or  * hadoop2's metrics2 classes and publishing.  */
end_comment

begin_interface
specifier|public
interface|interface
name|ReplicationMetricsSource
extends|extends
name|BaseMetricsSource
block|{
comment|/**    * The name of the metrics    */
specifier|public
specifier|static
specifier|final
name|String
name|METRICS_NAME
init|=
literal|"ReplicationMetrics"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under.    */
specifier|public
specifier|static
specifier|final
name|String
name|METRICS_CONTEXT
init|=
literal|"replicationmetrics"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under.    */
specifier|public
specifier|static
specifier|final
name|String
name|METRICS_JMX_CONTEXT
init|=
literal|"ReplicationMetrics"
decl_stmt|;
comment|/**    * A description.    */
specifier|public
specifier|static
specifier|final
name|String
name|METRICS_DESCRIPTION
init|=
literal|"Metrics about HBase replication"
decl_stmt|;
block|}
end_interface

end_unit

