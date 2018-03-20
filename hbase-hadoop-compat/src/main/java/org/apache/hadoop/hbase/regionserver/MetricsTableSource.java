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
name|yetus
operator|.
name|audience
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * This interface will be implemented to allow region server to push table metrics into  * MetricsRegionAggregateSource that will in turn push data to the Hadoop metrics system.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsTableSource
extends|extends
name|Comparable
argument_list|<
name|MetricsTableSource
argument_list|>
block|{
name|String
name|READ_REQUEST_COUNT
init|=
literal|"readRequestCount"
decl_stmt|;
name|String
name|READ_REQUEST_COUNT_DESC
init|=
literal|"Number of read requests"
decl_stmt|;
name|String
name|WRITE_REQUEST_COUNT
init|=
literal|"writeRequestCount"
decl_stmt|;
name|String
name|WRITE_REQUEST_COUNT_DESC
init|=
literal|"Number of write requests"
decl_stmt|;
name|String
name|TOTAL_REQUEST_COUNT
init|=
literal|"totalRequestCount"
decl_stmt|;
name|String
name|TOTAL_REQUEST_COUNT_DESC
init|=
literal|"Number of total requests"
decl_stmt|;
name|String
name|MEMSTORE_SIZE
init|=
literal|"memstoreSize"
decl_stmt|;
name|String
name|MEMSTORE_SIZE_DESC
init|=
literal|"The size of memory stores"
decl_stmt|;
name|String
name|STORE_FILE_SIZE
init|=
literal|"storeFileSize"
decl_stmt|;
name|String
name|STORE_FILE_SIZE_DESC
init|=
literal|"The size of store files size"
decl_stmt|;
name|String
name|TABLE_SIZE
init|=
literal|"tableSize"
decl_stmt|;
name|String
name|TABLE_SIZE_DESC
init|=
literal|"Total size of the table in the region server"
decl_stmt|;
name|String
name|getTableName
parameter_list|()
function_decl|;
comment|/**    * Close the table's metrics as all the region are closing.    */
name|void
name|close
parameter_list|()
function_decl|;
comment|/**    * Get the aggregate source to which this reports.    */
name|MetricsTableAggregateSource
name|getAggregateSource
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

