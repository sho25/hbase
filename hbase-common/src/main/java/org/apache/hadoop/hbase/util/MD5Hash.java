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
name|util
package|;
end_package

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|MessageDigest
import|;
end_import

begin_import
import|import
name|java
operator|.
name|security
operator|.
name|NoSuchAlgorithmException
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|codec
operator|.
name|binary
operator|.
name|Hex
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|Log
import|;
end_import

begin_import
import|import
name|org
operator|.
name|apache
operator|.
name|commons
operator|.
name|logging
operator|.
name|LogFactory
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
name|hbase
operator|.
name|classification
operator|.
name|InterfaceStability
import|;
end_import

begin_comment
comment|/**  * Utility class for MD5  * MD5 hash produces a 128-bit digest.  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Public
annotation|@
name|InterfaceStability
operator|.
name|Stable
specifier|public
class|class
name|MD5Hash
block|{
specifier|private
specifier|static
specifier|final
name|Log
name|LOG
init|=
name|LogFactory
operator|.
name|getLog
argument_list|(
name|MD5Hash
operator|.
name|class
argument_list|)
decl_stmt|;
comment|/**    * Given a byte array, returns in MD5 hash as a hex string.    * @param key    * @return SHA1 hash as a 32 character hex string.    */
specifier|public
specifier|static
name|String
name|getMD5AsHex
parameter_list|(
name|byte
index|[]
name|key
parameter_list|)
block|{
return|return
name|getMD5AsHex
argument_list|(
name|key
argument_list|,
literal|0
argument_list|,
name|key
operator|.
name|length
argument_list|)
return|;
block|}
comment|/**    * Given a byte array, returns its MD5 hash as a hex string.    * Only "length" number of bytes starting at "offset" within the    * byte array are used.    *    * @param key the key to hash (variable length byte array)    * @param offset    * @param length     * @return MD5 hash as a 32 character hex string.    */
specifier|public
specifier|static
name|String
name|getMD5AsHex
parameter_list|(
name|byte
index|[]
name|key
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|)
block|{
try|try
block|{
name|MessageDigest
name|md
init|=
name|MessageDigest
operator|.
name|getInstance
argument_list|(
literal|"MD5"
argument_list|)
decl_stmt|;
name|md
operator|.
name|update
argument_list|(
name|key
argument_list|,
name|offset
argument_list|,
name|length
argument_list|)
expr_stmt|;
name|byte
index|[]
name|digest
init|=
name|md
operator|.
name|digest
argument_list|()
decl_stmt|;
return|return
operator|new
name|String
argument_list|(
name|Hex
operator|.
name|encodeHex
argument_list|(
name|digest
argument_list|)
argument_list|)
return|;
block|}
catch|catch
parameter_list|(
name|NoSuchAlgorithmException
name|e
parameter_list|)
block|{
comment|// this should never happen unless the JDK is messed up.
throw|throw
operator|new
name|RuntimeException
argument_list|(
literal|"Error computing MD5 hash"
argument_list|,
name|e
argument_list|)
throw|;
block|}
block|}
block|}
end_class

end_unit

