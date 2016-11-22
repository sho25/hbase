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

begin_comment
comment|/**  * A low level asynchronous table.  *<p>  * The returned {@code CompletableFuture} will be finished directly in the rpc framework's callback  * thread, so typically you should not do any time consuming work inside these methods, otherwise  * you will be likely to block at least one connection to RS(even more if the rpc framework uses  * NIO).  *<p>  * So, only experts that want to build high performance service should use this interface directly,  * especially for the {@link #scan(Scan, ScanResultConsumer)} below.  *<p>  * TODO: For now the only difference between this interface and {@link AsyncTable} is the scan  * method. The {@link ScanResultConsumer} exposes the implementation details of a scan(heartbeat) so  * it is not suitable for a normal user. If it is still the only difference after we implement most  * features of AsyncTable, we can think about merge these two interfaces.  */
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
name|RawAsyncTable
extends|extends
name|AsyncTableBase
block|{
comment|/**    * The basic scan API uses the observer pattern. All results that match the given scan object will    * be passed to the given {@code consumer} by calling {@link ScanResultConsumer#onNext(Result[])}.    * {@link ScanResultConsumer#onComplete()} means the scan is finished, and    * {@link ScanResultConsumer#onError(Throwable)} means we hit an unrecoverable error and the scan    * is terminated. {@link ScanResultConsumer#onHeartbeat()} means the RS is still working but we    * can not get a valid result to call {@link ScanResultConsumer#onNext(Result[])}. This is usually    * because the matched results are too sparse, for example, a filter which almost filters out    * everything is specified.    *<p>    * Notice that, the methods of the given {@code consumer} will be called directly in the rpc    * framework's callback thread, so typically you should not do any time consuming work inside    * these methods, otherwise you will be likely to block at least one connection to RS(even more if    * the rpc framework uses NIO).    * @param scan A configured {@link Scan} object.    * @param consumer the consumer used to receive results.    */
name|void
name|scan
parameter_list|(
name|Scan
name|scan
parameter_list|,
name|ScanResultConsumer
name|consumer
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

