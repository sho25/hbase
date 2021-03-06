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
name|regionserver
operator|.
name|Store
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
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Used to track compaction execution.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|LimitedPrivate
argument_list|(
name|HBaseInterfaceAudience
operator|.
name|COPROC
argument_list|)
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|CompactionLifeCycleTracker
block|{
specifier|static
name|CompactionLifeCycleTracker
name|DUMMY
init|=
operator|new
name|CompactionLifeCycleTracker
argument_list|()
block|{   }
decl_stmt|;
comment|/**    * Called if the compaction request is failed for some reason.    */
specifier|default
name|void
name|notExecuted
parameter_list|(
name|Store
name|store
parameter_list|,
name|String
name|reason
parameter_list|)
block|{   }
comment|/**    * Called before compaction is executed by CompactSplitThread.    *<p>    * Requesting compaction on a region can lead to multiple compactions on different stores, so we    * will pass the {@link Store} in to tell you the store we operate on.    */
specifier|default
name|void
name|beforeExecution
parameter_list|(
name|Store
name|store
parameter_list|)
block|{   }
comment|/**    * Called after compaction is executed by CompactSplitThread.    *<p>    * Requesting compaction on a region can lead to multiple compactions on different stores, so we    * will pass the {@link Store} in to tell you the store we operate on.    */
specifier|default
name|void
name|afterExecution
parameter_list|(
name|Store
name|store
parameter_list|)
block|{   }
comment|/**    * Called after all the requested compactions are completed.    *<p>    * The compaction scheduling is per Store so if you request a compaction on a region it may lead    * to multiple compactions.    */
specifier|default
name|void
name|completed
parameter_list|()
block|{   }
block|}
end_interface

end_unit

