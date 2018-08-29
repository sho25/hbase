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
comment|/**  * Interface of class that will wrap a MetricsTableSource and export numbers so they can be  * used in MetricsTableSource  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsTableWrapperAggregate
block|{
comment|/**    * Get the number of read requests that have been issued against this table    */
name|long
name|getReadRequestCount
parameter_list|(
name|String
name|table
parameter_list|)
function_decl|;
comment|/**    * Get the number of CoprocessorService requests that have been issued against this table    */
name|long
name|getCpRequestsCount
parameter_list|(
name|String
name|table
parameter_list|)
function_decl|;
comment|/**    * Get the total number of filtered read requests that have been issued against this table    */
name|long
name|getFilteredReadRequestCount
parameter_list|(
name|String
name|table
parameter_list|)
function_decl|;
comment|/**    * Get the number of write requests that have been issued for this table    */
name|long
name|getWriteRequestCount
parameter_list|(
name|String
name|table
parameter_list|)
function_decl|;
comment|/**    * Get the total number of requests that have been issued for this table    */
name|long
name|getTotalRequestsCount
parameter_list|(
name|String
name|table
parameter_list|)
function_decl|;
comment|/**    * Get the memory store size against this table    */
name|long
name|getMemStoreSize
parameter_list|(
name|String
name|table
parameter_list|)
function_decl|;
comment|/**    * Get the store file size against this table    */
name|long
name|getStoreFileSize
parameter_list|(
name|String
name|table
parameter_list|)
function_decl|;
comment|/**    * Get the table region size against this table    */
name|long
name|getTableSize
parameter_list|(
name|String
name|table
parameter_list|)
function_decl|;
comment|/**    * Get the average region size for this table    */
name|long
name|getAvgRegionSize
parameter_list|(
name|String
name|table
parameter_list|)
function_decl|;
comment|/**    * Get the number of regions hosted on for this table    */
name|long
name|getNumRegions
parameter_list|(
name|String
name|table
parameter_list|)
function_decl|;
comment|/**    * Get the number of stores hosted on for this table    */
name|long
name|getNumStores
parameter_list|(
name|String
name|table
parameter_list|)
function_decl|;
comment|/**    * Get the number of store files hosted for this table    */
name|long
name|getNumStoreFiles
parameter_list|(
name|String
name|table
parameter_list|)
function_decl|;
comment|/**    * @return Max age of store files for this table    */
name|long
name|getMaxStoreFileAge
parameter_list|(
name|String
name|table
parameter_list|)
function_decl|;
comment|/**    * @return Min age of store files for this table    */
name|long
name|getMinStoreFileAge
parameter_list|(
name|String
name|table
parameter_list|)
function_decl|;
comment|/**    *  @return Average age of store files for this table    */
name|long
name|getAvgStoreFileAge
parameter_list|(
name|String
name|table
parameter_list|)
function_decl|;
comment|/**    *  @return Number of reference files for this table    */
name|long
name|getNumReferenceFiles
parameter_list|(
name|String
name|table
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

