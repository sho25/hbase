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
name|chaos
operator|.
name|policies
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Arrays
import|;
end_import

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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|hbase
operator|.
name|chaos
operator|.
name|actions
operator|.
name|Action
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
name|chaos
operator|.
name|monkies
operator|.
name|PolicyBasedChaosMonkey
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
name|util
operator|.
name|Pair
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
name|util
operator|.
name|StringUtils
import|;
end_import

begin_comment
comment|/**  * A policy, which picks a random action according to the given weights,  * and performs it every configurable period.  */
end_comment

begin_class
specifier|public
class|class
name|PeriodicRandomActionPolicy
extends|extends
name|PeriodicPolicy
block|{
specifier|private
name|List
argument_list|<
name|Pair
argument_list|<
name|Action
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|actions
decl_stmt|;
specifier|public
name|PeriodicRandomActionPolicy
parameter_list|(
name|long
name|periodMs
parameter_list|,
name|List
argument_list|<
name|Pair
argument_list|<
name|Action
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|actions
parameter_list|)
block|{
name|super
argument_list|(
name|periodMs
argument_list|)
expr_stmt|;
name|this
operator|.
name|actions
operator|=
name|actions
expr_stmt|;
block|}
specifier|public
name|PeriodicRandomActionPolicy
parameter_list|(
name|long
name|periodMs
parameter_list|,
name|Pair
argument_list|<
name|Action
argument_list|,
name|Integer
argument_list|>
modifier|...
name|actions
parameter_list|)
block|{
comment|// We don't expect it to be modified.
name|this
argument_list|(
name|periodMs
argument_list|,
name|Arrays
operator|.
name|asList
argument_list|(
name|actions
argument_list|)
argument_list|)
expr_stmt|;
block|}
specifier|public
name|PeriodicRandomActionPolicy
parameter_list|(
name|long
name|periodMs
parameter_list|,
name|Action
modifier|...
name|actions
parameter_list|)
block|{
name|super
argument_list|(
name|periodMs
argument_list|)
expr_stmt|;
name|this
operator|.
name|actions
operator|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|actions
operator|.
name|length
argument_list|)
expr_stmt|;
for|for
control|(
name|Action
name|action
range|:
name|actions
control|)
block|{
name|this
operator|.
name|actions
operator|.
name|add
argument_list|(
operator|new
name|Pair
argument_list|<>
argument_list|(
name|action
argument_list|,
literal|1
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|protected
name|void
name|runOneIteration
parameter_list|()
block|{
name|Action
name|action
init|=
name|PolicyBasedChaosMonkey
operator|.
name|selectWeightedRandomItem
argument_list|(
name|actions
argument_list|)
decl_stmt|;
try|try
block|{
name|action
operator|.
name|perform
argument_list|()
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|Exception
name|ex
parameter_list|)
block|{
name|LOG
operator|.
name|warn
argument_list|(
literal|"Exception performing action: "
operator|+
name|StringUtils
operator|.
name|stringifyException
argument_list|(
name|ex
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|init
parameter_list|(
name|PolicyContext
name|context
parameter_list|)
throws|throws
name|Exception
block|{
name|super
operator|.
name|init
argument_list|(
name|context
argument_list|)
expr_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|Action
argument_list|,
name|Integer
argument_list|>
name|action
range|:
name|actions
control|)
block|{
name|action
operator|.
name|getFirst
argument_list|()
operator|.
name|init
argument_list|(
name|this
operator|.
name|context
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

