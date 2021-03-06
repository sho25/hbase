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
name|BaseSource
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
comment|/**  * This interface will be implemented by a MetricsSource that will export metrics from  * multiple regions of a table into the hadoop metrics system.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsTableAggregateSource
extends|extends
name|BaseSource
block|{
comment|/**    * The name of the metrics    */
name|String
name|METRICS_NAME
init|=
literal|"Tables"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under.    */
name|String
name|METRICS_CONTEXT
init|=
literal|"regionserver"
decl_stmt|;
comment|/**    * Description    */
name|String
name|METRICS_DESCRIPTION
init|=
literal|"Metrics about HBase RegionServer tables"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under in jmx    */
name|String
name|METRICS_JMX_CONTEXT
init|=
literal|"RegionServer,sub="
operator|+
name|METRICS_NAME
decl_stmt|;
name|String
name|NUM_TABLES
init|=
literal|"numTables"
decl_stmt|;
name|String
name|NUMBER_OF_TABLES_DESC
init|=
literal|"Number of tables in the metrics system"
decl_stmt|;
comment|/**    * Returns MetricsTableSource registered for the table. Creates one if not defined.    * @param table The table name    */
name|MetricsTableSource
name|getOrCreateTableSource
parameter_list|(
name|String
name|table
parameter_list|,
name|MetricsTableWrapperAggregate
name|wrapper
parameter_list|)
function_decl|;
comment|/**    * Remove a table's source. This is called when regions of a table are closed.    *    * @param table The table name    */
name|void
name|deleteTableSource
parameter_list|(
name|String
name|table
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

