begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/**  *  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|Random
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
name|HConstants
import|;
end_import

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
comment|/**  * NonceGenerator implementation that uses client ID hash + random int as nonce group, and random  * numbers as nonces.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|final
class|class
name|PerClientRandomNonceGenerator
implements|implements
name|NonceGenerator
block|{
specifier|private
specifier|static
specifier|final
name|PerClientRandomNonceGenerator
name|INST
init|=
operator|new
name|PerClientRandomNonceGenerator
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|Random
name|rdm
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|long
name|clientId
decl_stmt|;
specifier|private
name|PerClientRandomNonceGenerator
parameter_list|()
block|{
name|byte
index|[]
name|clientIdBase
init|=
name|ClientIdGenerator
operator|.
name|generateClientId
argument_list|()
decl_stmt|;
name|this
operator|.
name|clientId
operator|=
operator|(
operator|(
operator|(
name|long
operator|)
name|Arrays
operator|.
name|hashCode
argument_list|(
name|clientIdBase
argument_list|)
operator|)
operator|<<
literal|32
operator|)
operator|+
name|rdm
operator|.
name|nextInt
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|getNonceGroup
parameter_list|()
block|{
return|return
name|this
operator|.
name|clientId
return|;
block|}
annotation|@
name|Override
specifier|public
name|long
name|newNonce
parameter_list|()
block|{
name|long
name|result
init|=
name|HConstants
operator|.
name|NO_NONCE
decl_stmt|;
do|do
block|{
name|result
operator|=
name|rdm
operator|.
name|nextLong
argument_list|()
expr_stmt|;
block|}
do|while
condition|(
name|result
operator|==
name|HConstants
operator|.
name|NO_NONCE
condition|)
do|;
return|return
name|result
return|;
block|}
comment|/**    * Get the singleton nonce generator.    */
specifier|public
specifier|static
name|PerClientRandomNonceGenerator
name|get
parameter_list|()
block|{
return|return
name|INST
return|;
block|}
block|}
end_class

end_unit

