begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one  * or more contributor license agreements.  See the NOTICE file  * distributed with this work for additional information  * regarding copyright ownership.  The ASF licenses this file  * to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance  * with the License.  You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS,  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  * See the License for the specific language governing permissions and  * limitations under the License.  */
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
name|io
operator|.
name|InputStream
import|;
end_import

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
name|hbase
operator|.
name|classification
operator|.
name|InterfaceAudience
import|;
end_import

begin_comment
comment|/**  * Decryptors apply a cipher to an InputStream to recover plaintext.  */
end_comment

begin_interface
annotation|@
name|InterfaceAudience
operator|.
name|Public
specifier|public
interface|interface
name|Decryptor
block|{
comment|/**    * Set the secret key    * @param key    */
specifier|public
name|void
name|setKey
parameter_list|(
name|Key
name|key
parameter_list|)
function_decl|;
comment|/**    * Get the expected length for the initialization vector    * @return the expected length for the initialization vector    */
specifier|public
name|int
name|getIvLength
parameter_list|()
function_decl|;
comment|/**    * Get the cipher's internal block size    * @return the cipher's internal block size    */
specifier|public
name|int
name|getBlockSize
parameter_list|()
function_decl|;
comment|/**    * Set the initialization vector    * @param iv    */
specifier|public
name|void
name|setIv
parameter_list|(
name|byte
index|[]
name|iv
parameter_list|)
function_decl|;
comment|/**    * Create a stream for decryption    * @param in    */
specifier|public
name|InputStream
name|createDecryptionStream
parameter_list|(
name|InputStream
name|in
parameter_list|)
function_decl|;
comment|/**    * Reset state, reinitialize with the key and iv    */
name|void
name|reset
parameter_list|()
function_decl|;
block|}
end_interface

end_unit

