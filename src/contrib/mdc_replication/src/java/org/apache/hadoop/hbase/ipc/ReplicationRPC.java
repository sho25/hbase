begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  * Copyright 2010 The Apache Software Foundation  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|ipc
package|;
end_package

begin_comment
comment|/**  * Helper class to add RPC-related configs for replication  */
end_comment

begin_class
specifier|public
class|class
name|ReplicationRPC
block|{
specifier|private
specifier|static
specifier|final
name|byte
name|RPC_CODE
init|=
literal|110
decl_stmt|;
specifier|private
specifier|static
name|boolean
name|initialized
init|=
literal|false
decl_stmt|;
specifier|public
specifier|synchronized
specifier|static
name|void
name|initialize
parameter_list|()
block|{
if|if
condition|(
name|initialized
condition|)
block|{
return|return;
block|}
name|HBaseRPC
operator|.
name|addToMap
argument_list|(
name|ReplicationRegionInterface
operator|.
name|class
argument_list|,
name|RPC_CODE
argument_list|)
expr_stmt|;
name|initialized
operator|=
literal|true
expr_stmt|;
block|}
specifier|private
name|ReplicationRPC
parameter_list|()
block|{
comment|// Static helper class;
block|}
block|}
end_class

end_unit

