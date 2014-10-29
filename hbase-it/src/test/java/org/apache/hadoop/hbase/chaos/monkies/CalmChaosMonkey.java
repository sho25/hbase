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

begin_comment
comment|/**  * Chaos Monkey that does nothing.  */
end_comment

begin_class
specifier|public
class|class
name|CalmChaosMonkey
extends|extends
name|ChaosMonkey
block|{
annotation|@
name|Override
specifier|public
name|void
name|start
parameter_list|()
throws|throws
name|Exception
block|{    }
annotation|@
name|Override
specifier|public
name|void
name|stop
parameter_list|(
name|String
name|why
parameter_list|)
block|{    }
annotation|@
name|Override
specifier|public
name|boolean
name|isStopped
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
annotation|@
name|Override
specifier|public
name|void
name|waitForStop
parameter_list|()
throws|throws
name|InterruptedException
block|{    }
annotation|@
name|Override
specifier|public
name|boolean
name|isDestructive
parameter_list|()
block|{
return|return
literal|false
return|;
block|}
block|}
end_class

end_unit

