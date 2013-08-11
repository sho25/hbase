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
name|devtools
operator|.
name|buildstats
package|;
end_package

begin_class
specifier|public
class|class
name|TestCaseResult
block|{
specifier|private
name|String
name|className
decl_stmt|;
specifier|private
name|int
name|failedSince
decl_stmt|;
specifier|private
name|String
name|name
decl_stmt|;
specifier|private
name|String
name|status
decl_stmt|;
specifier|public
name|String
name|getName
parameter_list|()
block|{
return|return
name|name
return|;
block|}
specifier|public
name|String
name|getClassName
parameter_list|()
block|{
return|return
name|className
return|;
block|}
specifier|public
name|int
name|failedSince
parameter_list|()
block|{
return|return
name|failedSince
return|;
block|}
specifier|public
name|String
name|getStatus
parameter_list|()
block|{
return|return
name|status
return|;
block|}
specifier|public
name|void
name|setName
parameter_list|(
name|String
name|s
parameter_list|)
block|{
name|name
operator|=
name|s
expr_stmt|;
block|}
specifier|public
name|void
name|setClassName
parameter_list|(
name|String
name|s
parameter_list|)
block|{
name|className
operator|=
name|s
expr_stmt|;
block|}
specifier|public
name|void
name|setFailedSince
parameter_list|(
name|int
name|s
parameter_list|)
block|{
name|failedSince
operator|=
name|s
expr_stmt|;
block|}
specifier|public
name|void
name|setStatus
parameter_list|(
name|String
name|s
parameter_list|)
block|{
name|status
operator|=
name|s
expr_stmt|;
block|}
specifier|public
name|String
name|getFullName
parameter_list|()
block|{
return|return
operator|(
name|this
operator|.
name|className
operator|+
literal|"."
operator|+
name|this
operator|.
name|name
operator|)
operator|.
name|toLowerCase
argument_list|()
return|;
block|}
block|}
end_class

end_unit

