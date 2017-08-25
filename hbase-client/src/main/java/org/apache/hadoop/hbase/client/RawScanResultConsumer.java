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
name|util
operator|.
name|Optional
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
comment|/**  * Receives {@link Result} for an asynchronous scan.  *<p>  * Notice that, the {@link #onNext(Result[], ScanController)} method will be called in the thread  * which we send request to HBase service. So if you want the asynchronous scanner fetch data from  * HBase in background while you process the returned data, you need to move the processing work to  * another thread to make the {@code onNext} call return immediately. And please do NOT do any time  * consuming tasks in all methods below unless you know what you are doing.  * @since 2.0.0  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|RawScanResultConsumer
block|{
comment|/**    * Used to resume a scan.    */
annotation|@
name|InterfaceAudience
operator|.
name|Public
interface|interface
name|ScanResumer
block|{
comment|/**      * Resume the scan. You are free to call it multiple time but only the first call will take      * effect.      */
name|void
name|resume
parameter_list|()
function_decl|;
block|}
comment|/**    * Used to suspend or stop a scan, or get a scan cursor if available.    *<p>    * Notice that, you should only call the {@link #suspend()} or {@link #terminate()} inside onNext    * or onHeartbeat method. A IllegalStateException will be thrown if you call them at other places.    *<p>    * You can only call one of the {@link #suspend()} and {@link #terminate()} methods(of course you    * are free to not call them both), and the methods are not reentrant. An IllegalStateException    * will be thrown if you have already called one of the methods.    */
annotation|@
name|InterfaceAudience
operator|.
name|Public
interface|interface
name|ScanController
block|{
comment|/**      * Suspend the scan.      *<p>      * This means we will stop fetching data in background, i.e., will not call onNext any more      * before you resume the scan.      * @return A resumer used to resume the scan later.      */
name|ScanResumer
name|suspend
parameter_list|()
function_decl|;
comment|/**      * Terminate the scan.      *<p>      * This is useful when you have got enough results and want to stop the scan in onNext method,      * or you want to stop the scan in onHeartbeat method because it has spent too many time.      */
name|void
name|terminate
parameter_list|()
function_decl|;
comment|/**      * Get the scan cursor if available.      * @return The scan cursor.      */
name|Optional
argument_list|<
name|Cursor
argument_list|>
name|cursor
parameter_list|()
function_decl|;
block|}
comment|/**    * Indicate that we have receive some data.    * @param results the data fetched from HBase service.    * @param controller used to suspend or terminate the scan. Notice that the {@code controller}    *          instance is only valid within scope of onNext method. You can only call its method in    *          onNext, do NOT store it and call it later outside onNext.    */
name|void
name|onNext
parameter_list|(
name|Result
index|[]
name|results
parameter_list|,
name|ScanController
name|controller
parameter_list|)
function_decl|;
comment|/**    * Indicate that there is a heartbeat message but we have not cumulated enough cells to call    * {@link #onNext(Result[], ScanController)}.    *<p>    * Note that this method will always be called when RS returns something to us but we do not have    * enough cells to call {@link #onNext(Result[], ScanController)}. Sometimes it may not be a    * 'heartbeat' message for RS, for example, we have a large row with many cells and size limit is    * exceeded before sending all the cells for this row. For RS it does send some data to us and the    * time limit has not been reached, but we can not return the data to client so here we call this    * method to tell client we have already received something.    *<p>    * This method give you a chance to terminate a slow scan operation.    * @param controller used to suspend or terminate the scan. Notice that the {@code controller}    *          instance is only valid within the scope of onHeartbeat method. You can only call its    *          method in onHeartbeat, do NOT store it and call it later outside onHeartbeat.    */
specifier|default
name|void
name|onHeartbeat
parameter_list|(
name|ScanController
name|controller
parameter_list|)
block|{   }
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

