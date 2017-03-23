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
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
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

begin_comment
comment|/**  * Used to separate the row constructing logic.  *<p>  * After we add heartbeat support for scan, RS may return partial result even if allowPartial is  * false and batch is 0. With this interface, the implementation now looks like:  *<ol>  *<li>Get results from ScanResponse proto.</li>  *<li>Pass them to ScanResultCache and get something back.</li>  *<li>If we actually get something back, then pass it to ScanConsumer.</li>  *</ol>  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
interface|interface
name|ScanResultCache
block|{
specifier|static
specifier|final
name|Result
index|[]
name|EMPTY_RESULT_ARRAY
init|=
operator|new
name|Result
index|[
literal|0
index|]
decl_stmt|;
comment|/**    * Add the given results to cache and get valid results back.    * @param results the results of a scan next. Must not be null.    * @param isHeartbeatMessage indicate whether the results is gotten from a heartbeat response.    * @return valid results, never null.    */
name|Result
index|[]
name|addAndGet
parameter_list|(
name|Result
index|[]
name|results
parameter_list|,
name|boolean
name|isHeartbeatMessage
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Clear the cached result if any. Called when scan error and we will start from a start of a row    * again.    */
name|void
name|clear
parameter_list|()
function_decl|;
comment|/**    * Return the number of complete rows. Used to implement limited scan.    */
name|int
name|numberOfCompleteRows
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

