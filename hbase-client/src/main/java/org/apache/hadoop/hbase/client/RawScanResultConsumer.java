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
name|classification
operator|.
name|InterfaceStability
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
name|Result
import|;
end_import

begin_comment
comment|/**  * Receives {@link Result} for an asynchronous scan.  *<p>  * Notice that, the {@link #onNext(Result[])} method will be called in the thread which we send  * request to HBase service. So if you want the asynchronous scanner fetch data from HBase in  * background while you process the returned data, you need to move the processing work to another  * thread to make the {@code onNext} call return immediately. And please do NOT do any time  * consuming tasks in all methods below unless you know what you are doing.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Unstable
specifier|public
interface|interface
name|RawScanResultConsumer
block|{
comment|/**    * @param results the data fetched from HBase service.    * @return {@code false} if you want to terminate the scan process. Otherwise {@code true}    */
name|boolean
name|onNext
parameter_list|(
name|Result
index|[]
name|results
parameter_list|)
function_decl|;
comment|/**    * Indicate that there is an heartbeat message but we have not cumulated enough cells to call    * onNext.    *<p>    * This method give you a chance to terminate a slow scan operation.    * @return {@code false} if you want to terminate the scan process. Otherwise {@code true}    */
specifier|default
name|boolean
name|onHeartbeat
parameter_list|()
block|{
return|return
literal|true
return|;
block|}
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
block|}
end_interface

end_unit

