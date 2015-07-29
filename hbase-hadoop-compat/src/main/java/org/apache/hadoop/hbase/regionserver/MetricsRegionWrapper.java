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
name|java
operator|.
name|util
operator|.
name|Map
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
name|math
operator|.
name|stat
operator|.
name|descriptive
operator|.
name|DescriptiveStatistics
import|;
end_import

begin_comment
comment|/**  * Interface of class that will wrap an HRegion and export numbers so they can be  * used in MetricsRegionSource  */
end_comment

begin_interface
specifier|public
interface|interface
name|MetricsRegionWrapper
block|{
comment|/**    * Get the name of the table the region belongs to.    *    * @return The string version of the table name.    */
name|String
name|getTableName
parameter_list|()
function_decl|;
comment|/**    * Get the name of the namespace this table is in.    * @return String version of the namespace.  Can't be empty.    */
name|String
name|getNamespace
parameter_list|()
function_decl|;
comment|/**    * Get the name of the region.    *    * @return The encoded name of the region.    */
name|String
name|getRegionName
parameter_list|()
function_decl|;
comment|/**    * Get the number of stores hosted on this region server.    */
name|long
name|getNumStores
parameter_list|()
function_decl|;
comment|/**    * Get the number of store files hosted on this region server.    */
name|long
name|getNumStoreFiles
parameter_list|()
function_decl|;
comment|/**    * Get the size of the memstore on this region server.    */
name|long
name|getMemstoreSize
parameter_list|()
function_decl|;
comment|/**    * Get the total size of the store files this region server is serving from.    */
name|long
name|getStoreFileSize
parameter_list|()
function_decl|;
comment|/**    * Get the total number of read requests that have been issued against this region    */
name|long
name|getReadRequestCount
parameter_list|()
function_decl|;
comment|/**    * Get the total number of mutations that have been issued against this region.    */
name|long
name|getWriteRequestCount
parameter_list|()
function_decl|;
name|long
name|getNumFilesCompacted
parameter_list|()
function_decl|;
name|long
name|getNumBytesCompacted
parameter_list|()
function_decl|;
name|long
name|getNumCompactionsCompleted
parameter_list|()
function_decl|;
name|int
name|getRegionHashCode
parameter_list|()
function_decl|;
comment|/**    * Get the time spent by coprocessors in this region.    */
name|Map
argument_list|<
name|String
argument_list|,
name|DescriptiveStatistics
argument_list|>
name|getCoprocessorExecutionStatistics
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

