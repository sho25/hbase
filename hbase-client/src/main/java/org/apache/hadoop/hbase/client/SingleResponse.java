begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|client
package|;
end_package

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

begin_comment
comment|/**  * Class for single action response  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
class|class
name|SingleResponse
extends|extends
name|AbstractResponse
block|{
specifier|private
name|Entry
name|entry
init|=
literal|null
decl_stmt|;
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|static
class|class
name|Entry
block|{
specifier|private
name|Result
name|result
init|=
literal|null
decl_stmt|;
specifier|private
name|boolean
name|processed
init|=
literal|false
decl_stmt|;
specifier|public
name|Result
name|getResult
parameter_list|()
block|{
return|return
name|result
return|;
block|}
specifier|public
name|void
name|setResult
parameter_list|(
name|Result
name|result
parameter_list|)
block|{
name|this
operator|.
name|result
operator|=
name|result
expr_stmt|;
block|}
specifier|public
name|boolean
name|isProcessed
parameter_list|()
block|{
return|return
name|processed
return|;
block|}
specifier|public
name|void
name|setProcessed
parameter_list|(
name|boolean
name|processed
parameter_list|)
block|{
name|this
operator|.
name|processed
operator|=
name|processed
expr_stmt|;
block|}
block|}
specifier|public
name|Entry
name|getEntry
parameter_list|()
block|{
return|return
name|entry
return|;
block|}
specifier|public
name|void
name|setEntry
parameter_list|(
name|Entry
name|entry
parameter_list|)
block|{
name|this
operator|.
name|entry
operator|=
name|entry
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|ResponseType
name|type
parameter_list|()
block|{
return|return
name|ResponseType
operator|.
name|SINGLE
return|;
block|}
block|}
end_class

end_unit

