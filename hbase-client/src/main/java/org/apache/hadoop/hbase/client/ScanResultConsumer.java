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
name|client
operator|.
name|metrics
operator|.
name|ScanMetrics
import|;
end_import

begin_comment
comment|/**  * Receives {@link Result} for an asynchronous scan.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|ScanResultConsumer
block|{
comment|/**    * @param result the data fetched from HBase service.    * @return {@code false} if you want to terminate the scan process. Otherwise {@code true}    */
name|boolean
name|onNext
parameter_list|(
name|Result
name|result
parameter_list|)
function_decl|;
comment|/**    * Indicate that we hit an unrecoverable error and the scan operation is terminated.    *<p>    * We will not call {@link #onComplete()} after calling {@link #onError(Throwable)}.    */
name|void
name|onError
parameter_list|(
name|Throwable
name|error
parameter_list|)
function_decl|;
comment|/**    * Indicate that the scan operation is completed normally.    */
name|void
name|onComplete
parameter_list|()
function_decl|;
comment|/**    * If {@code scan.isScanMetricsEnabled()} returns true, then this method will be called prior to    * all other methods in this interface to give you the {@link ScanMetrics} instance for this scan    * operation. The {@link ScanMetrics} instance will be updated on-the-fly during the scan, you can    * store it somewhere to get the metrics at any time if you want.    */
specifier|default
name|void
name|onScanMetricsCreated
parameter_list|(
name|ScanMetrics
name|scanMetrics
parameter_list|)
block|{   }
block|}
end_interface

end_unit

