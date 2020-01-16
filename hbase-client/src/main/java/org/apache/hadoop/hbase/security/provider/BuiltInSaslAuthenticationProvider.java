begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  *     http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
operator|.
name|provider
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
comment|/**  * Base class for all Apache HBase, built-in {@link SaslAuthenticationProvider}'s to extend.  *  * HBase users should take care to note that this class (and its sub-classes) are marked with the  * {@code InterfaceAudience.Private} annotation. These implementations are available for users to  * read, copy, and modify, but should not be extended or re-used in binary form. There are no  * compatibility guarantees provided for implementations of this class.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|BuiltInSaslAuthenticationProvider
implements|implements
name|SaslAuthenticationProvider
block|{
specifier|public
specifier|static
specifier|final
name|String
name|AUTH_TOKEN_TYPE
init|=
literal|"HBASE_AUTH_TOKEN"
decl_stmt|;
annotation|@
name|Override
specifier|public
name|String
name|getTokenKind
parameter_list|()
block|{
return|return
name|AUTH_TOKEN_TYPE
return|;
block|}
block|}
end_class

end_unit

