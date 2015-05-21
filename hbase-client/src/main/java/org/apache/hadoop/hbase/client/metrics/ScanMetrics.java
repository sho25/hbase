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
operator|.
name|metrics
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|concurrent
operator|.
name|atomic
operator|.
name|AtomicLong
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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Provides metrics related to scan operations (both server side and client side metrics).  *<p>  * The data can be passed to mapreduce framework or other systems.  * We use atomic longs so that one thread can increment,  * while another atomically resets to zero after the values are reported  * to hadoop's counters.  *<p>  * Some of these metrics are general for any client operation such as put  * However, there is no need for this. So they are defined under scan operation  * for now.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|ScanMetrics
extends|extends
name|ServerSideScanMetrics
block|{
comment|// AtomicLongs to hold the metrics values. These are all updated through ClientScanner and
comment|// ScannerCallable. They are atomic longs so that atomic getAndSet can be used to reset the
comment|// values after progress is passed to hadoop's counters.
comment|/**    * number of RPC calls    */
specifier|public
specifier|final
name|AtomicLong
name|countOfRPCcalls
init|=
name|createCounter
argument_list|(
literal|"RPC_CALLS"
argument_list|)
decl_stmt|;
comment|/**    * number of remote RPC calls    */
specifier|public
specifier|final
name|AtomicLong
name|countOfRemoteRPCcalls
init|=
name|createCounter
argument_list|(
literal|"REMOTE_RPC_CALLS"
argument_list|)
decl_stmt|;
comment|/**    * sum of milliseconds between sequential next calls    */
specifier|public
specifier|final
name|AtomicLong
name|sumOfMillisSecBetweenNexts
init|=
name|createCounter
argument_list|(
literal|"MILLIS_BETWEEN_NEXTS"
argument_list|)
decl_stmt|;
comment|/**    * number of NotServingRegionException caught    */
specifier|public
specifier|final
name|AtomicLong
name|countOfNSRE
init|=
name|createCounter
argument_list|(
literal|"NOT_SERVING_REGION_EXCEPTION"
argument_list|)
decl_stmt|;
comment|/**    * number of bytes in Result objects from region servers    */
specifier|public
specifier|final
name|AtomicLong
name|countOfBytesInResults
init|=
name|createCounter
argument_list|(
literal|"BYTES_IN_RESULTS"
argument_list|)
decl_stmt|;
comment|/**    * number of bytes in Result objects from remote region servers    */
specifier|public
specifier|final
name|AtomicLong
name|countOfBytesInRemoteResults
init|=
name|createCounter
argument_list|(
literal|"BYTES_IN_REMOTE_RESULTS"
argument_list|)
decl_stmt|;
comment|/**    * number of regions    */
specifier|public
specifier|final
name|AtomicLong
name|countOfRegions
init|=
name|createCounter
argument_list|(
literal|"REGIONS_SCANNED"
argument_list|)
decl_stmt|;
comment|/**    * number of RPC retries    */
specifier|public
specifier|final
name|AtomicLong
name|countOfRPCRetries
init|=
name|createCounter
argument_list|(
literal|"RPC_RETRIES"
argument_list|)
decl_stmt|;
comment|/**    * number of remote RPC retries    */
specifier|public
specifier|final
name|AtomicLong
name|countOfRemoteRPCRetries
init|=
name|createCounter
argument_list|(
literal|"REMOTE_RPC_RETRIES"
argument_list|)
decl_stmt|;
comment|/**    * constructor    */
specifier|public
name|ScanMetrics
parameter_list|()
block|{   }
block|}
end_class

end_unit

