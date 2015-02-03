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
name|regionserver
operator|.
name|compactions
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
name|HBaseInterfaceAudience
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
name|Stoppable
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
name|regionserver
operator|.
name|RegionServerServices
import|;
end_import

begin_comment
comment|/**  * A utility that constrains the total throughput of one or more simultaneous flows (compactions) by  * sleeping when necessary.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|CONFIG
argument_list|)
specifier|public
interface|interface
name|CompactionThroughputController
extends|extends
name|Stoppable
block|{
comment|/**    * Setup controller for the given region server.    */
name|void
name|setup
parameter_list|(
name|RegionServerServices
name|server
parameter_list|)
function_decl|;
comment|/**    * Start a compaction.    */
name|void
name|start
parameter_list|(
name|String
name|compactionName
parameter_list|)
function_decl|;
comment|/**    * Control the compaction throughput. Will sleep if too fast.    * @return the actual sleep time.    */
name|long
name|control
parameter_list|(
name|String
name|compactionName
parameter_list|,
name|long
name|size
parameter_list|)
throws|throws
name|InterruptedException
function_decl|;
comment|/**    * Finish a compaction. Should call this method in a finally block.    */
name|void
name|finish
parameter_list|(
name|String
name|compactionName
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

