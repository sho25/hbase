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
operator|.
name|balancer
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
import|;
end_import

begin_import
import|import
name|com
operator|.
name|google
operator|.
name|common
operator|.
name|collect
operator|.
name|Lists
import|;
end_import

begin_comment
comment|/**  * Used for FavoredNode unit tests  */
end_comment

begin_class
specifier|public
class|class
name|LoadOnlyFavoredStochasticBalancer
extends|extends
name|FavoredStochasticBalancer
block|{
annotation|@
name|Override
specifier|protected
name|void
name|configureGenerators
parameter_list|()
block|{
name|List
argument_list|<
name|CandidateGenerator
argument_list|>
name|fnPickers
init|=
name|Lists
operator|.
name|newArrayList
argument_list|()
decl_stmt|;
name|fnPickers
operator|.
name|add
argument_list|(
operator|new
name|FavoredNodeLoadPicker
argument_list|()
argument_list|)
expr_stmt|;
name|setCandidateGenerators
argument_list|(
name|fnPickers
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

