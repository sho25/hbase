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
name|metrics
package|;
end_package

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

begin_comment
comment|/**  * Interface for sources that will export JvmPauseMonitor metrics  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|JvmPauseMonitorSource
block|{
name|String
name|INFO_THRESHOLD_COUNT_KEY
init|=
literal|"pauseInfoThresholdExceeded"
decl_stmt|;
name|String
name|INFO_THRESHOLD_COUNT_DESC
init|=
literal|"Count of INFO level pause threshold alerts"
decl_stmt|;
name|String
name|WARN_THRESHOLD_COUNT_KEY
init|=
literal|"pauseWarnThresholdExceeded"
decl_stmt|;
name|String
name|WARN_THRESHOLD_COUNT_DESC
init|=
literal|"Count of WARN level pause threshold alerts"
decl_stmt|;
name|String
name|PAUSE_TIME_WITH_GC_KEY
init|=
literal|"pauseTimeWithGc"
decl_stmt|;
name|String
name|PAUSE_TIME_WITH_GC_DESC
init|=
literal|"Histogram for excessive pause times with GC activity detected"
decl_stmt|;
name|String
name|PAUSE_TIME_WITHOUT_GC_KEY
init|=
literal|"pauseTimeWithoutGc"
decl_stmt|;
name|String
name|PAUSE_TIME_WITHOUT_GC_DESC
init|=
literal|"Histogram for excessive pause times without GC activity detected"
decl_stmt|;
comment|/**    * Increment the INFO level threshold exceeded count    * @param count the count    */
name|void
name|incInfoThresholdExceeded
parameter_list|(
name|int
name|count
parameter_list|)
function_decl|;
comment|/**    * Increment the WARN level threshold exceeded count    * @param count the count    */
name|void
name|incWarnThresholdExceeded
parameter_list|(
name|int
name|count
parameter_list|)
function_decl|;
comment|/**    * Update the pause time histogram where GC activity was detected.    *    * @param t time it took    */
name|void
name|updatePauseTimeWithGc
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
comment|/**    * Update the pause time histogram where GC activity was not detected.    *    * @param t time it took    */
name|void
name|updatePauseTimeWithoutGc
parameter_list|(
name|long
name|t
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

