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
name|ipc
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
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Utility for managing the flag byte passed in response to a  * {@link RpcServer.Call}  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
class|class
name|ResponseFlag
block|{
specifier|private
specifier|static
specifier|final
name|byte
name|ERROR_BIT
init|=
literal|0x1
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|byte
name|LENGTH_BIT
init|=
literal|0x2
decl_stmt|;
specifier|private
name|ResponseFlag
parameter_list|()
block|{
comment|// Make it so this class cannot be constructed.
block|}
specifier|static
name|boolean
name|isError
parameter_list|(
specifier|final
name|byte
name|flag
parameter_list|)
block|{
return|return
operator|(
name|flag
operator|&
name|ERROR_BIT
operator|)
operator|!=
literal|0
return|;
block|}
specifier|static
name|boolean
name|isLength
parameter_list|(
specifier|final
name|byte
name|flag
parameter_list|)
block|{
return|return
operator|(
name|flag
operator|&
name|LENGTH_BIT
operator|)
operator|!=
literal|0
return|;
block|}
specifier|static
name|byte
name|getLengthSetOnly
parameter_list|()
block|{
return|return
name|LENGTH_BIT
return|;
block|}
specifier|static
name|byte
name|getErrorAndLengthSet
parameter_list|()
block|{
return|return
name|LENGTH_BIT
operator||
name|ERROR_BIT
return|;
block|}
block|}
end_class

end_unit

