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
name|io
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
name|metrics
operator|.
name|BaseSource
import|;
end_import

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

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|MetricsIOSource
extends|extends
name|BaseSource
block|{
comment|/**    * The name of the metrics    */
name|String
name|METRICS_NAME
init|=
literal|"IO"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under.    */
name|String
name|METRICS_CONTEXT
init|=
literal|"regionserver"
decl_stmt|;
comment|/**    * Description    */
name|String
name|METRICS_DESCRIPTION
init|=
literal|"Metrics about FileSystem IO"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under in jmx    */
name|String
name|METRICS_JMX_CONTEXT
init|=
literal|"RegionServer,sub="
operator|+
name|METRICS_NAME
decl_stmt|;
name|String
name|FS_READ_TIME_HISTO_KEY
init|=
literal|"fsReadTime"
decl_stmt|;
name|String
name|FS_PREAD_TIME_HISTO_KEY
init|=
literal|"fsPReadTime"
decl_stmt|;
name|String
name|FS_WRITE_HISTO_KEY
init|=
literal|"fsWriteTime"
decl_stmt|;
name|String
name|CHECKSUM_FAILURES_KEY
init|=
literal|"fsChecksumFailureCount"
decl_stmt|;
name|String
name|FS_READ_TIME_HISTO_DESC
init|=
literal|"Latency of HFile's sequential reads on this region server in milliseconds"
decl_stmt|;
name|String
name|FS_PREAD_TIME_HISTO_DESC
init|=
literal|"Latency of HFile's positional reads on this region server in milliseconds"
decl_stmt|;
name|String
name|FS_WRITE_TIME_HISTO_DESC
init|=
literal|"Latency of HFile's writes on this region server in milliseconds"
decl_stmt|;
name|String
name|CHECKSUM_FAILURES_DESC
init|=
literal|"Number of checksum failures for the HBase HFile checksums at the"
operator|+
literal|" HBase level (separate from HDFS checksums)"
decl_stmt|;
comment|/**    * Update the fs sequential read time histogram    * @param t time it took, in milliseconds    */
name|void
name|updateFsReadTime
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the fs positional read time histogram    * @param t time it took, in milliseconds    */
name|void
name|updateFsPReadTime
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the fs write time histogram    * @param t time it took, in milliseconds    */
name|void
name|updateFsWriteTime
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

