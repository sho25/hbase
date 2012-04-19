begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Copyright The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
operator|.
name|metrics
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Set
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
name|hadoop
operator|.
name|hbase
operator|.
name|HRegionInfo
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
name|client
operator|.
name|Append
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
name|client
operator|.
name|Delete
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
name|client
operator|.
name|Get
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
name|client
operator|.
name|HTable
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
name|client
operator|.
name|Increment
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
name|client
operator|.
name|Put
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
name|regionserver
operator|.
name|HRegion
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
name|Bytes
import|;
end_import

begin_comment
comment|/**  * This class provides a simplified interface to expose time varying metrics  * about GET/DELETE/PUT/ICV operations on a region and on Column Families. All  * metrics are stored in {@link RegionMetricsStorage} and exposed to hadoop  * metrics through {@link RegionServerDynamicMetrics}.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|OperationMetrics
block|{
specifier|private
specifier|static
specifier|final
name|String
name|DELETE_KEY
init|=
literal|"delete_"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|PUT_KEY
init|=
literal|"put_"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|GET_KEY
init|=
literal|"get_"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|ICV_KEY
init|=
literal|"incrementColumnValue_"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|INCREMENT_KEY
init|=
literal|"increment_"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|MULTIPUT_KEY
init|=
literal|"multiput_"
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|String
name|APPEND_KEY
init|=
literal|"append_"
decl_stmt|;
comment|/** Conf key controlling whether we should expose metrics.*/
specifier|private
specifier|static
specifier|final
name|String
name|CONF_KEY
init|=
literal|"hbase.metrics.exposeOperationTimes"
decl_stmt|;
specifier|private
name|String
name|tableName
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|regionName
init|=
literal|null
decl_stmt|;
specifier|private
name|String
name|regionMetrixPrefix
init|=
literal|null
decl_stmt|;
specifier|private
name|Configuration
name|conf
init|=
literal|null
decl_stmt|;
comment|/**    * Create a new OperationMetrics    * @param conf The Configuration of the HRegion reporting operations coming in.    * @param regionInfo The region info    */
specifier|public
name|OperationMetrics
parameter_list|(
name|Configuration
name|conf
parameter_list|,
name|HRegionInfo
name|regionInfo
parameter_list|)
block|{
comment|// Configure SchemaMetrics before trying to create a RegionOperationMetrics instance as
comment|// RegionOperationMetrics relies on SchemaMetrics to do naming.
if|if
condition|(
name|conf
operator|!=
literal|null
condition|)
block|{
name|SchemaMetrics
operator|.
name|configureGlobally
argument_list|(
name|conf
argument_list|)
expr_stmt|;
name|this
operator|.
name|conf
operator|=
name|conf
expr_stmt|;
if|if
condition|(
name|regionInfo
operator|!=
literal|null
condition|)
block|{
name|this
operator|.
name|tableName
operator|=
name|regionInfo
operator|.
name|getTableNameAsString
argument_list|()
expr_stmt|;
name|this
operator|.
name|regionName
operator|=
name|regionInfo
operator|.
name|getEncodedName
argument_list|()
expr_stmt|;
block|}
else|else
block|{
name|this
operator|.
name|tableName
operator|=
name|SchemaMetrics
operator|.
name|UNKNOWN
expr_stmt|;
name|this
operator|.
name|regionName
operator|=
name|SchemaMetrics
operator|.
name|UNKNOWN
expr_stmt|;
block|}
name|this
operator|.
name|regionMetrixPrefix
operator|=
name|SchemaMetrics
operator|.
name|generateRegionMetricsPrefix
argument_list|(
name|this
operator|.
name|tableName
argument_list|,
name|this
operator|.
name|regionName
argument_list|)
expr_stmt|;
block|}
block|}
comment|/**    * This is used in creating a testing HRegion where the regionInfo is unknown    * @param conf    */
specifier|public
name|OperationMetrics
parameter_list|()
block|{
name|this
argument_list|(
literal|null
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
comment|/**    * Update the stats associated with {@link HTable#put(java.util.List)}.    *     * @param columnFamilies Set of CF's this multiput is associated with    * @param value the time    */
specifier|public
name|void
name|updateMultiPutMetrics
parameter_list|(
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|columnFamilies
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|doUpdateTimeVarying
argument_list|(
name|columnFamilies
argument_list|,
name|MULTIPUT_KEY
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|/**    * Update the metrics associated with a {@link Get}    *     * @param columnFamilies    *          Set of Column Families in this get.    * @param value    *          the time    */
specifier|public
name|void
name|updateGetMetrics
parameter_list|(
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|columnFamilies
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|doUpdateTimeVarying
argument_list|(
name|columnFamilies
argument_list|,
name|GET_KEY
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|/**    * Update metrics associated with an {@link Increment}    * @param columnFamilies    * @param value    */
specifier|public
name|void
name|updateIncrementMetrics
parameter_list|(
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|columnFamilies
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|doUpdateTimeVarying
argument_list|(
name|columnFamilies
argument_list|,
name|INCREMENT_KEY
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|/**    * Update the metrics associated with an {@link Append}    * @param columnFamilies    * @param value    */
specifier|public
name|void
name|updateAppendMetrics
parameter_list|(
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|columnFamilies
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|doUpdateTimeVarying
argument_list|(
name|columnFamilies
argument_list|,
name|APPEND_KEY
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|/**    * Update the metrics associated with    * {@link HTable#incrementColumnValue(byte[], byte[], byte[], long)}    *     * @param columnFamily    *          The single column family associated with an ICV    * @param value    *          the time    */
specifier|public
name|void
name|updateIncrementColumnValueMetrics
parameter_list|(
name|byte
index|[]
name|columnFamily
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|String
name|cfMetricPrefix
init|=
name|SchemaMetrics
operator|.
name|generateSchemaMetricsPrefix
argument_list|(
name|this
operator|.
name|tableName
argument_list|,
name|Bytes
operator|.
name|toString
argument_list|(
name|columnFamily
argument_list|)
argument_list|)
decl_stmt|;
name|doSafeIncTimeVarying
argument_list|(
name|cfMetricPrefix
argument_list|,
name|ICV_KEY
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|doSafeIncTimeVarying
argument_list|(
name|this
operator|.
name|regionMetrixPrefix
argument_list|,
name|ICV_KEY
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|/**    * update metrics associated with a {@link Put}    *     * @param columnFamilies    *          Set of column families involved.    * @param value    *          the time.    */
specifier|public
name|void
name|updatePutMetrics
parameter_list|(
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|columnFamilies
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|doUpdateTimeVarying
argument_list|(
name|columnFamilies
argument_list|,
name|PUT_KEY
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|/**    * update metrics associated with a {@link Delete}    *     * @param columnFamilies    * @param value    *          the time.    */
specifier|public
name|void
name|updateDeleteMetrics
parameter_list|(
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|columnFamilies
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|doUpdateTimeVarying
argument_list|(
name|columnFamilies
argument_list|,
name|DELETE_KEY
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
comment|/**    * Method to send updates for cf and region metrics. This is the normal method    * used if the naming of stats and CF's are in line with put/delete/multiput.    *     * @param columnFamilies    *          the set of column families involved.    * @param key    *          the metric name.    * @param value    *          the time.    */
specifier|private
name|void
name|doUpdateTimeVarying
parameter_list|(
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|columnFamilies
parameter_list|,
name|String
name|key
parameter_list|,
name|long
name|value
parameter_list|)
block|{
name|String
name|cfPrefix
init|=
literal|null
decl_stmt|;
if|if
condition|(
name|columnFamilies
operator|!=
literal|null
condition|)
block|{
name|cfPrefix
operator|=
name|SchemaMetrics
operator|.
name|generateSchemaMetricsPrefix
argument_list|(
name|tableName
argument_list|,
name|columnFamilies
argument_list|)
expr_stmt|;
block|}
else|else
block|{
name|cfPrefix
operator|=
name|SchemaMetrics
operator|.
name|generateSchemaMetricsPrefix
argument_list|(
name|tableName
argument_list|,
name|SchemaMetrics
operator|.
name|UNKNOWN
argument_list|)
expr_stmt|;
block|}
name|doSafeIncTimeVarying
argument_list|(
name|cfPrefix
argument_list|,
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
name|doSafeIncTimeVarying
argument_list|(
name|this
operator|.
name|regionMetrixPrefix
argument_list|,
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
specifier|private
name|void
name|doSafeIncTimeVarying
parameter_list|(
name|String
name|prefix
parameter_list|,
name|String
name|key
parameter_list|,
name|long
name|value
parameter_list|)
block|{
if|if
condition|(
name|conf
operator|.
name|getBoolean
argument_list|(
name|CONF_KEY
argument_list|,
literal|true
argument_list|)
condition|)
block|{
if|if
condition|(
name|prefix
operator|!=
literal|null
operator|&&
operator|!
name|prefix
operator|.
name|isEmpty
argument_list|()
operator|&&
name|key
operator|!=
literal|null
operator|&&
operator|!
name|key
operator|.
name|isEmpty
argument_list|()
condition|)
block|{
name|RegionMetricsStorage
operator|.
name|incrTimeVaryingMetric
argument_list|(
name|prefix
operator|+
name|key
argument_list|,
name|value
argument_list|)
expr_stmt|;
block|}
block|}
block|}
block|}
end_class

end_unit

