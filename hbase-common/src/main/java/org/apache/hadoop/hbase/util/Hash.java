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
name|org
operator|.
name|apache
operator|.
name|hadoop
operator|.
name|conf
operator|.
name|Configuration
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
comment|/**  * This class represents a common API for hashing functions.  */
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
specifier|abstract
class|class
name|Hash
block|{
comment|/** Constant to denote invalid hash type. */
specifier|public
specifier|static
specifier|final
name|int
name|INVALID_HASH
init|=
operator|-
literal|1
decl_stmt|;
comment|/** Constant to denote {@link JenkinsHash}. */
specifier|public
specifier|static
specifier|final
name|int
name|JENKINS_HASH
init|=
literal|0
decl_stmt|;
comment|/** Constant to denote {@link MurmurHash}. */
specifier|public
specifier|static
specifier|final
name|int
name|MURMUR_HASH
init|=
literal|1
decl_stmt|;
comment|/** Constant to denote {@link MurmurHash3}. */
specifier|public
specifier|static
specifier|final
name|int
name|MURMUR_HASH3
init|=
literal|2
decl_stmt|;
comment|/**    * This utility method converts String representation of hash function name    * to a symbolic constant. Currently three function types are supported,    * "jenkins", "murmur" and "murmur3".    * @param name hash function name    * @return one of the predefined constants    */
specifier|public
specifier|static
name|int
name|parseHashType
parameter_list|(
name|String
name|name
parameter_list|)
block|{
if|if
condition|(
literal|"jenkins"
operator|.
name|equalsIgnoreCase
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|JENKINS_HASH
return|;
block|}
elseif|else
if|if
condition|(
literal|"murmur"
operator|.
name|equalsIgnoreCase
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|MURMUR_HASH
return|;
block|}
elseif|else
if|if
condition|(
literal|"murmur3"
operator|.
name|equalsIgnoreCase
argument_list|(
name|name
argument_list|)
condition|)
block|{
return|return
name|MURMUR_HASH3
return|;
block|}
else|else
block|{
return|return
name|INVALID_HASH
return|;
block|}
block|}
comment|/**    * This utility method converts the name of the configured    * hash type to a symbolic constant.    * @param conf configuration    * @return one of the predefined constants    */
specifier|public
specifier|static
name|int
name|getHashType
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|String
name|name
init|=
name|conf
operator|.
name|get
argument_list|(
literal|"hbase.hash.type"
argument_list|,
literal|"murmur"
argument_list|)
decl_stmt|;
return|return
name|parseHashType
argument_list|(
name|name
argument_list|)
return|;
block|}
comment|/**    * Get a singleton instance of hash function of a given type.    * @param type predefined hash type    * @return hash function instance, or null if type is invalid    */
specifier|public
specifier|static
name|Hash
name|getInstance
parameter_list|(
name|int
name|type
parameter_list|)
block|{
switch|switch
condition|(
name|type
condition|)
block|{
case|case
name|JENKINS_HASH
case|:
return|return
name|JenkinsHash
operator|.
name|getInstance
argument_list|()
return|;
case|case
name|MURMUR_HASH
case|:
return|return
name|MurmurHash
operator|.
name|getInstance
argument_list|()
return|;
case|case
name|MURMUR_HASH3
case|:
return|return
name|MurmurHash3
operator|.
name|getInstance
argument_list|()
return|;
default|default:
return|return
literal|null
return|;
block|}
block|}
comment|/**    * Get a singleton instance of hash function of a type    * defined in the configuration.    * @param conf current configuration    * @return defined hash type, or null if type is invalid    */
specifier|public
specifier|static
name|Hash
name|getInstance
parameter_list|(
name|Configuration
name|conf
parameter_list|)
block|{
name|int
name|type
init|=
name|getHashType
argument_list|(
name|conf
argument_list|)
decl_stmt|;
return|return
name|getInstance
argument_list|(
name|type
argument_list|)
return|;
block|}
comment|/**    * Calculate a hash using all bytes from the input argument, and    * a seed of -1.    * @param bytes input bytes    * @return hash value    */
specifier|public
name|int
name|hash
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|)
block|{
return|return
name|hash
argument_list|(
name|bytes
argument_list|,
name|bytes
operator|.
name|length
argument_list|,
operator|-
literal|1
argument_list|)
return|;
block|}
comment|/**    * Calculate a hash using all bytes from the input argument,    * and a provided seed value.    * @param bytes input bytes    * @param initval seed value    * @return hash value    */
specifier|public
name|int
name|hash
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|initval
parameter_list|)
block|{
return|return
name|hash
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|bytes
operator|.
name|length
argument_list|,
name|initval
argument_list|)
return|;
block|}
comment|/**    * Calculate a hash using bytes from 0 to<code>length</code>, and    * the provided seed value    * @param bytes input bytes    * @param length length of the valid bytes after offset to consider    * @param initval seed value    * @return hash value    */
specifier|public
name|int
name|hash
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|length
parameter_list|,
name|int
name|initval
parameter_list|)
block|{
return|return
name|hash
argument_list|(
name|bytes
argument_list|,
literal|0
argument_list|,
name|length
argument_list|,
name|initval
argument_list|)
return|;
block|}
comment|/**    * Calculate a hash using bytes from<code>offset</code> to<code>offset +     * length</code>, and the provided seed value.    * @param bytes input bytes    * @param offset the offset into the array to start consideration    * @param length length of the valid bytes after offset to consider    * @param initval seed value    * @return hash value    */
specifier|public
specifier|abstract
name|int
name|hash
parameter_list|(
name|byte
index|[]
name|bytes
parameter_list|,
name|int
name|offset
parameter_list|,
name|int
name|length
parameter_list|,
name|int
name|initval
parameter_list|)
function_decl|;
block|}
end_class

end_unit

