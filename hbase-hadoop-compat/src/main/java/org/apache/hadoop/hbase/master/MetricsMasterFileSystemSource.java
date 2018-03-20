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
name|master
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
name|MetricsMasterFileSystemSource
extends|extends
name|BaseSource
block|{
comment|/**    * The name of the metrics    */
name|String
name|METRICS_NAME
init|=
literal|"FileSystem"
decl_stmt|;
comment|/**    * The context metrics will be under.    */
name|String
name|METRICS_CONTEXT
init|=
literal|"master"
decl_stmt|;
comment|/**    * The name of the metrics context that metrics will be under in jmx    */
name|String
name|METRICS_JMX_CONTEXT
init|=
literal|"Master,sub="
operator|+
name|METRICS_NAME
decl_stmt|;
comment|/**    * Description    */
name|String
name|METRICS_DESCRIPTION
init|=
literal|"Metrics about HBase master file system."
decl_stmt|;
name|String
name|META_SPLIT_TIME_NAME
init|=
literal|"metaHlogSplitTime"
decl_stmt|;
name|String
name|META_SPLIT_SIZE_NAME
init|=
literal|"metaHlogSplitSize"
decl_stmt|;
name|String
name|SPLIT_TIME_NAME
init|=
literal|"hlogSplitTime"
decl_stmt|;
name|String
name|SPLIT_SIZE_NAME
init|=
literal|"hlogSplitSize"
decl_stmt|;
name|String
name|META_SPLIT_TIME_DESC
init|=
literal|"Time it takes to finish splitMetaLog()"
decl_stmt|;
name|String
name|META_SPLIT_SIZE_DESC
init|=
literal|"Size of hbase:meta WAL files being split"
decl_stmt|;
name|String
name|SPLIT_TIME_DESC
init|=
literal|"Time it takes to finish WAL.splitLog()"
decl_stmt|;
name|String
name|SPLIT_SIZE_DESC
init|=
literal|"Size of WAL files being split"
decl_stmt|;
name|void
name|updateMetaWALSplitTime
parameter_list|(
name|long
name|time
parameter_list|)
function_decl|;
name|void
name|updateMetaWALSplitSize
parameter_list|(
name|long
name|size
parameter_list|)
function_decl|;
name|void
name|updateSplitTime
parameter_list|(
name|long
name|time
parameter_list|)
function_decl|;
name|void
name|updateSplitSize
parameter_list|(
name|long
name|size
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

