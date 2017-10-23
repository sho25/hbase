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
name|java
operator|.
name|io
operator|.
name|IOException
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
name|HRegion
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
name|HStore
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
name|security
operator|.
name|User
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
name|edu
operator|.
name|umd
operator|.
name|cs
operator|.
name|findbugs
operator|.
name|annotations
operator|.
name|Nullable
import|;
end_import

begin_comment
comment|/**  * Request a compaction.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
interface|interface
name|CompactionRequester
block|{
comment|/**    * Request compaction on all the stores of the given region.    */
name|void
name|requestCompaction
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|String
name|why
parameter_list|,
name|int
name|priority
parameter_list|,
name|CompactionLifeCycleTracker
name|tracker
parameter_list|,
annotation|@
name|Nullable
name|User
name|user
parameter_list|)
throws|throws
name|IOException
function_decl|;
comment|/**    * Request compaction on the given store.    */
name|void
name|requestCompaction
parameter_list|(
name|HRegion
name|region
parameter_list|,
name|HStore
name|store
parameter_list|,
name|String
name|why
parameter_list|,
name|int
name|priority
parameter_list|,
name|CompactionLifeCycleTracker
name|tracker
parameter_list|,
annotation|@
name|Nullable
name|User
name|user
parameter_list|)
throws|throws
name|IOException
function_decl|;
block|}
end_interface

end_unit
