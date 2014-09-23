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
name|security
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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Security related generic utility methods.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
class|class
name|SecurityUtil
block|{
comment|/**    * Get the user name from a principal    */
specifier|public
specifier|static
name|String
name|getUserFromPrincipal
parameter_list|(
specifier|final
name|String
name|principal
parameter_list|)
block|{
name|int
name|i
init|=
name|principal
operator|.
name|indexOf
argument_list|(
literal|"/"
argument_list|)
decl_stmt|;
if|if
condition|(
name|i
operator|==
operator|-
literal|1
condition|)
block|{
name|i
operator|=
name|principal
operator|.
name|indexOf
argument_list|(
literal|"@"
argument_list|)
expr_stmt|;
block|}
return|return
operator|(
name|i
operator|>
operator|-
literal|1
operator|)
condition|?
name|principal
operator|.
name|substring
argument_list|(
literal|0
argument_list|,
name|i
argument_list|)
else|:
name|principal
return|;
block|}
block|}
end_class

end_unit

