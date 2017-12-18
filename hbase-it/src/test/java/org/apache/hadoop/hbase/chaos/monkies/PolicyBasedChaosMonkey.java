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
name|monkies
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
name|Collection
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
name|commons
operator|.
name|lang3
operator|.
name|RandomUtils
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
name|IntegrationTestingUtility
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
name|policies
operator|.
name|Policy
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Chaos monkey that given multiple policies will run actions against the cluster.  */
end_comment

begin_class
specifier|public
class|class
name|PolicyBasedChaosMonkey
extends|extends
name|ChaosMonkey
block|{
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|PolicyBasedChaosMonkey
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|ONE_SEC
init|=
literal|1000
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|FIVE_SEC
init|=
literal|5
operator|*
name|ONE_SEC
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|long
name|ONE_MIN
init|=
literal|60
operator|*
name|ONE_SEC
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|long
name|TIMEOUT
init|=
name|ONE_MIN
decl_stmt|;
specifier|final
name|IntegrationTestingUtility
name|util
decl_stmt|;
comment|/**    * Construct a new ChaosMonkey    * @param util the HBaseIntegrationTestingUtility already configured    * @param policies custom policies to use    */
specifier|public
name|PolicyBasedChaosMonkey
parameter_list|(
name|IntegrationTestingUtility
name|util
parameter_list|,
name|Policy
modifier|...
name|policies
parameter_list|)
block|{
name|this
operator|.
name|util
operator|=
name|util
expr_stmt|;
name|this
operator|.
name|policies
operator|=
name|policies
expr_stmt|;
block|}
specifier|public
name|PolicyBasedChaosMonkey
parameter_list|(
name|IntegrationTestingUtility
name|util
parameter_list|,
name|Collection
argument_list|<
name|Policy
argument_list|>
name|policies
parameter_list|)
block|{
name|this
operator|.
name|util
operator|=
name|util
expr_stmt|;
name|this
operator|.
name|policies
operator|=
name|policies
operator|.
name|toArray
argument_list|(
operator|new
name|Policy
index|[
name|policies
operator|.
name|size
argument_list|()
index|]
argument_list|)
expr_stmt|;
block|}
comment|/** Selects a random item from the given items */
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|selectRandomItem
parameter_list|(
name|T
index|[]
name|items
parameter_list|)
block|{
return|return
name|items
index|[
name|RandomUtils
operator|.
name|nextInt
argument_list|(
literal|0
argument_list|,
name|items
operator|.
name|length
argument_list|)
index|]
return|;
block|}
comment|/** Selects a random item from the given items with weights*/
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|T
name|selectWeightedRandomItem
parameter_list|(
name|List
argument_list|<
name|Pair
argument_list|<
name|T
argument_list|,
name|Integer
argument_list|>
argument_list|>
name|items
parameter_list|)
block|{
name|int
name|totalWeight
init|=
literal|0
decl_stmt|;
for|for
control|(
name|Pair
argument_list|<
name|T
argument_list|,
name|Integer
argument_list|>
name|pair
range|:
name|items
control|)
block|{
name|totalWeight
operator|+=
name|pair
operator|.
name|getSecond
argument_list|()
expr_stmt|;
block|}
name|int
name|cutoff
init|=
name|RandomUtils
operator|.
name|nextInt
argument_list|(
literal|0
argument_list|,
name|totalWeight
argument_list|)
decl_stmt|;
name|int
name|cummulative
init|=
literal|0
decl_stmt|;
name|T
name|item
init|=
literal|null
decl_stmt|;
comment|//warn: O(n)
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|items
operator|.
name|size
argument_list|()
condition|;
name|i
operator|++
control|)
block|{
name|int
name|curWeight
init|=
name|items
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getSecond
argument_list|()
decl_stmt|;
if|if
condition|(
name|cutoff
operator|<
name|cummulative
operator|+
name|curWeight
condition|)
block|{
name|item
operator|=
name|items
operator|.
name|get
argument_list|(
name|i
argument_list|)
operator|.
name|getFirst
argument_list|()
expr_stmt|;
break|break;
block|}
name|cummulative
operator|+=
name|curWeight
expr_stmt|;
block|}
return|return
name|item
return|;
block|}
comment|/** Selects and returns ceil(ratio * items.length) random items from the given array */
specifier|public
specifier|static
parameter_list|<
name|T
parameter_list|>
name|List
argument_list|<
name|T
argument_list|>
name|selectRandomItems
parameter_list|(
name|T
index|[]
name|items
parameter_list|,
name|float
name|ratio
parameter_list|)
block|{
name|int
name|remaining
init|=
operator|(
name|int
operator|)
name|Math
operator|.
name|ceil
argument_list|(
name|items
operator|.
name|length
operator|*
name|ratio
argument_list|)
decl_stmt|;
name|List
argument_list|<
name|T
argument_list|>
name|selectedItems
init|=
operator|new
name|ArrayList
argument_list|<>
argument_list|(
name|remaining
argument_list|)
decl_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
argument_list|<
name|items
operator|.
name|length
operator|&&
name|remaining
argument_list|>
literal|0
condition|;
name|i
operator|++
control|)
block|{
if|if
condition|(
name|RandomUtils
operator|.
name|nextFloat
argument_list|()
operator|<
operator|(
operator|(
name|float
operator|)
name|remaining
operator|/
operator|(
name|items
operator|.
name|length
operator|-
name|i
operator|)
operator|)
condition|)
block|{
name|selectedItems
operator|.
name|add
argument_list|(
name|items
index|[
name|i
index|]
argument_list|)
expr_stmt|;
name|remaining
operator|--
expr_stmt|;
block|}
block|}
return|return
name|selectedItems
return|;
block|}
specifier|private
name|Policy
index|[]
name|policies
decl_stmt|;
specifier|private
name|Thread
index|[]
name|monkeyThreads
decl_stmt|;
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|Exception
block|{
name|monkeyThreads
operator|=
operator|new
name|Thread
index|[
name|policies
operator|.
name|length
index|]
expr_stmt|;
for|for
control|(
name|int
name|i
init|=
literal|0
init|;
name|i
operator|<
name|policies
operator|.
name|length
condition|;
name|i
operator|++
control|)
block|{
name|policies
index|[
name|i
index|]
operator|.
name|init
argument_list|(
operator|new
name|Policy
operator|.
name|PolicyContext
argument_list|(
name|this
operator|.
name|util
argument_list|)
argument_list|)
expr_stmt|;
name|Thread
name|monkeyThread
init|=
operator|new
name|Thread
argument_list|(
name|policies
index|[
name|i
index|]
argument_list|,
literal|"ChaosMonkeyThread"
argument_list|)
decl_stmt|;
name|monkeyThread
operator|.
name|start
argument_list|()
expr_stmt|;
name|monkeyThreads
index|[
name|i
index|]
operator|=
name|monkeyThread
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{
if|if
condition|(
name|policies
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
name|Policy
name|policy
range|:
name|policies
control|)
block|{
name|policy
operator|.
name|stop
argument_list|(
name|why
argument_list|)
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
name|policies
index|[
literal|0
index|]
operator|.
name|isStopped
argument_list|()
return|;
block|}
comment|/**    * Wait for ChaosMonkey to stop.    * @throws InterruptedException    */
annotation|@
name|Override
specifier|public
name|void
name|waitForStop
parameter_list|()
throws|throws
name|InterruptedException
block|{
if|if
condition|(
name|monkeyThreads
operator|==
literal|null
condition|)
block|{
return|return;
block|}
for|for
control|(
name|Thread
name|monkeyThread
range|:
name|monkeyThreads
control|)
block|{
comment|// TODO: bound the wait time per policy
name|monkeyThread
operator|.
name|join
argument_list|()
expr_stmt|;
block|}
block|}
annotation|@
name|Override
specifier|public
name|boolean
name|isDestructive
parameter_list|()
block|{
comment|// TODO: we can look at the actions, and decide to do the restore cluster or not based on them.
return|return
literal|true
return|;
block|}
block|}
end_class

end_unit

