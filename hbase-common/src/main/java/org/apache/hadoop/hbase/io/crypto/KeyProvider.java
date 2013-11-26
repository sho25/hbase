begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations under  * the License.  */
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
name|io
operator|.
name|crypto
package|;
end_package

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|Key
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
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * KeyProvider is a interface to abstract the different methods of retrieving  * key material from key storage such as Java key store.  *  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Evolving
specifier|public
interface|interface
name|KeyProvider
block|{
specifier|public
specifier|static
specifier|final
name|String
name|PASSWORD
init|=
literal|"password"
decl_stmt|;
specifier|public
specifier|static
specifier|final
name|String
name|PASSWORDFILE
init|=
literal|"passwordfile"
decl_stmt|;
comment|/**    * Initialize the key provider    * @param params    */
specifier|public
name|void
name|init
parameter_list|(
name|String
name|params
parameter_list|)
function_decl|;
comment|/**    * Retrieve the key for a given key aliase    * @param alias    * @return the keys corresponding to the supplied alias, or null if a key is    * not found    */
specifier|public
name|Key
name|getKey
parameter_list|(
name|String
name|alias
parameter_list|)
function_decl|;
comment|/**    * Retrieve keys for a given set of key aliases    * @param aliases an array of aliases    * @return an array of keys corresponding to the supplied aliases, an    * entry will be null if a key is not found    */
specifier|public
name|Key
index|[]
name|getKeys
parameter_list|(
name|String
index|[]
name|aliases
parameter_list|)
function_decl|;
block|}
end_interface

end_unit

