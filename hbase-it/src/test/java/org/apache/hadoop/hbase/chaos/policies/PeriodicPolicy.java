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
name|util
operator|.
name|Threads
import|;
end_import

begin_comment
comment|/** A policy which does stuff every time interval. */
end_comment

begin_class
specifier|public
specifier|abstract
class|class
name|PeriodicPolicy
extends|extends
name|Policy
block|{
specifier|private
name|long
name|periodMs
decl_stmt|;
specifier|public
name|PeriodicPolicy
parameter_list|(
name|long
name|periodMs
parameter_list|)
block|{
name|this
operator|.
name|periodMs
operator|=
name|periodMs
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|run
parameter_list|()
block|{
comment|// Add some jitter.
name|int
name|jitter
init|=
name|RandomUtils
operator|.
name|nextInt
argument_list|(
literal|0
argument_list|,
operator|(
name|int
operator|)
name|periodMs
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Sleeping for {} ms to add jitter"
argument_list|,
name|jitter
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
name|jitter
argument_list|)
expr_stmt|;
while|while
condition|(
operator|!
name|isStopped
argument_list|()
condition|)
block|{
name|long
name|start
init|=
name|System
operator|.
name|currentTimeMillis
argument_list|()
decl_stmt|;
name|runOneIteration
argument_list|()
expr_stmt|;
if|if
condition|(
name|isStopped
argument_list|()
condition|)
return|return;
name|long
name|sleepTime
init|=
name|periodMs
operator|-
operator|(
name|System
operator|.
name|currentTimeMillis
argument_list|()
operator|-
name|start
operator|)
decl_stmt|;
if|if
condition|(
name|sleepTime
operator|>
literal|0
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Sleeping for {} ms"
argument_list|,
name|sleepTime
argument_list|)
expr_stmt|;
name|Threads
operator|.
name|sleep
argument_list|(
name|sleepTime
argument_list|)
expr_stmt|;
block|}
block|}
block|}
specifier|protected
specifier|abstract
name|void
name|runOneIteration
parameter_list|()
function_decl|;
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
name|LOG
operator|.
name|info
argument_list|(
literal|"Using ChaosMonkey Policy {}, period={} ms"
argument_list|,
name|this
operator|.
name|getClass
argument_list|()
argument_list|,
name|periodMs
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

