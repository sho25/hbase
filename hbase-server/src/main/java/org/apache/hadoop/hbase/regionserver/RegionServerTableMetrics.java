begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements.  See the NOTICE file distributed with  * this work for additional information regarding copyright ownership.  * The ASF licenses this file to you under the Apache License, Version 2.0  * (the "License"); you may not use this file except in compliance with  * the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|CompatibilitySingletonFactory
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
name|TableName
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
name|metrics
operator|.
name|MetricRegistries
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
comment|/**  * Captures operation metrics by table. Separates metrics collection for table metrics away from  * {@link MetricsRegionServer} for encapsulation and ease of testing.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|RegionServerTableMetrics
block|{
specifier|private
specifier|final
name|MetricsTableLatencies
name|latencies
decl_stmt|;
specifier|private
specifier|final
name|MetricsTableQueryMeter
name|queryMeter
decl_stmt|;
specifier|public
name|RegionServerTableMetrics
parameter_list|()
block|{
name|latencies
operator|=
name|CompatibilitySingletonFactory
operator|.
name|getInstance
argument_list|(
name|MetricsTableLatencies
operator|.
name|class
argument_list|)
expr_stmt|;
name|queryMeter
operator|=
operator|new
name|MetricsTableQueryMeterImpl
argument_list|(
name|MetricRegistries
operator|.
name|global
argument_list|()
operator|.
name|get
argument_list|(
operator|(
operator|(
name|MetricsTableLatenciesImpl
operator|)
name|latencies
operator|)
operator|.
name|getMetricRegistryInfo
argument_list|()
argument_list|)
operator|.
name|get
argument_list|()
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updatePut
parameter_list|(
name|TableName
name|table
parameter_list|,
name|long
name|time
parameter_list|)
block|{
name|latencies
operator|.
name|updatePut
argument_list|(
name|table
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|time
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updatePutBatch
parameter_list|(
name|TableName
name|table
parameter_list|,
name|long
name|time
parameter_list|)
block|{
name|latencies
operator|.
name|updatePutBatch
argument_list|(
name|table
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|time
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateGet
parameter_list|(
name|TableName
name|table
parameter_list|,
name|long
name|time
parameter_list|)
block|{
name|latencies
operator|.
name|updateGet
argument_list|(
name|table
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|time
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateIncrement
parameter_list|(
name|TableName
name|table
parameter_list|,
name|long
name|time
parameter_list|)
block|{
name|latencies
operator|.
name|updateIncrement
argument_list|(
name|table
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|time
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateAppend
parameter_list|(
name|TableName
name|table
parameter_list|,
name|long
name|time
parameter_list|)
block|{
name|latencies
operator|.
name|updateAppend
argument_list|(
name|table
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|time
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateDelete
parameter_list|(
name|TableName
name|table
parameter_list|,
name|long
name|time
parameter_list|)
block|{
name|latencies
operator|.
name|updateDelete
argument_list|(
name|table
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|time
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateDeleteBatch
parameter_list|(
name|TableName
name|table
parameter_list|,
name|long
name|time
parameter_list|)
block|{
name|latencies
operator|.
name|updateDeleteBatch
argument_list|(
name|table
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|time
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateScanTime
parameter_list|(
name|TableName
name|table
parameter_list|,
name|long
name|time
parameter_list|)
block|{
name|latencies
operator|.
name|updateScanTime
argument_list|(
name|table
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|time
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateScanSize
parameter_list|(
name|TableName
name|table
parameter_list|,
name|long
name|size
parameter_list|)
block|{
name|latencies
operator|.
name|updateScanSize
argument_list|(
name|table
operator|.
name|getNameAsString
argument_list|()
argument_list|,
name|size
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateTableReadQueryMeter
parameter_list|(
name|TableName
name|table
parameter_list|,
name|long
name|count
parameter_list|)
block|{
name|queryMeter
operator|.
name|updateTableReadQueryMeter
argument_list|(
name|table
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateTableReadQueryMeter
parameter_list|(
name|TableName
name|table
parameter_list|)
block|{
name|queryMeter
operator|.
name|updateTableReadQueryMeter
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateTableWriteQueryMeter
parameter_list|(
name|TableName
name|table
parameter_list|,
name|long
name|count
parameter_list|)
block|{
name|queryMeter
operator|.
name|updateTableWriteQueryMeter
argument_list|(
name|table
argument_list|,
name|count
argument_list|)
expr_stmt|;
block|}
specifier|public
name|void
name|updateTableWriteQueryMeter
parameter_list|(
name|TableName
name|table
parameter_list|)
block|{
name|queryMeter
operator|.
name|updateTableWriteQueryMeter
argument_list|(
name|table
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

