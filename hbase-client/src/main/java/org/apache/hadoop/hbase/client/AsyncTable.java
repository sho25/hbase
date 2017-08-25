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

begin_comment
comment|/**  * The asynchronous table for normal users.  *<p>  * The implementation is required to be thread safe.  *<p>  * The implementation should make sure that user can do everything they want to the returned  * {@code CompletableFuture} without breaking anything. Usually the implementation will require user  * to provide a {@code ExecutorService}.  * @since 2.0.0  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|AsyncTable
extends|extends
name|AsyncTableBase
block|{
comment|/**    * Gets a scanner on the current table for the given family.    * @param family The column family to scan.    * @return A scanner.    */
specifier|default
name|ResultScanner
name|getScanner
parameter_list|(
name|byte
index|[]
name|family
parameter_list|)
block|{
return|return
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
operator|.
name|addFamily
argument_list|(
name|family
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Gets a scanner on the current table for the given family and qualifier.    * @param family The column family to scan.    * @param qualifier The column qualifier to scan.    * @return A scanner.    */
specifier|default
name|ResultScanner
name|getScanner
parameter_list|(
name|byte
index|[]
name|family
parameter_list|,
name|byte
index|[]
name|qualifier
parameter_list|)
block|{
return|return
name|getScanner
argument_list|(
operator|new
name|Scan
argument_list|()
operator|.
name|addColumn
argument_list|(
name|family
argument_list|,
name|qualifier
argument_list|)
argument_list|)
return|;
block|}
comment|/**    * Returns a scanner on the current table as specified by the {@link Scan} object.    * @param scan A configured {@link Scan} object.    * @return A scanner.    */
name|ResultScanner
name|getScanner
parameter_list|(
name|Scan
name|scan
parameter_list|)
function_decl|;
comment|/**    * The scan API uses the observer pattern. All results that match the given scan object will be    * passed to the given {@code consumer} by calling {@link ScanResultConsumer#onNext(Result)}.    * {@link ScanResultConsumer#onComplete()} means the scan is finished, and    * {@link ScanResultConsumer#onError(Throwable)} means we hit an unrecoverable error and the scan    * is terminated.    * @param scan A configured {@link Scan} object.    * @param consumer the consumer used to receive results.    */
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

