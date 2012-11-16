begin_unit|revision:0.9.5;language:Java;cregit-version:0.0.1
begin_comment
comment|/*  * Licensed to the Apache Software Foundation (ASF) under one or more  * contributor license agreements. See the NOTICE file distributed with this  * work for additional information regarding copyright ownership. The ASF  * licenses this file to you under the Apache License, Version 2.0 (the  * "License"); you may not use this file except in compliance with the License.  * You may obtain a copy of the License at  *  * http://www.apache.org/licenses/LICENSE-2.0  *  * Unless required by applicable law or agreed to in writing, software  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the  * License for the specific language governing permissions and limitations  * under the License.  */
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
operator|.
name|test
package|;
end_package

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
name|util
operator|.
name|Bytes
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
name|util
operator|.
name|MD5Hash
import|;
end_import

begin_comment
comment|/**  * A generator of random keys and values for load testing. Keys are generated  * by converting numeric indexes to strings and prefixing them with an MD5  * hash. Values are generated by selecting value size in the configured range  * and generating a pseudo-random sequence of bytes seeded by key, column  * qualifier, and value size.  *<p>  * Not thread-safe, so a separate instance is needed for every writer thread/  */
end_comment

begin_class
specifier|public
class|class
name|LoadTestKVGenerator
block|{
comment|/** A random number generator for determining value size */
specifier|private
name|Random
name|randomForValueSize
init|=
operator|new
name|Random
argument_list|()
decl_stmt|;
specifier|private
specifier|final
name|int
name|minValueSize
decl_stmt|;
specifier|private
specifier|final
name|int
name|maxValueSize
decl_stmt|;
specifier|public
name|LoadTestKVGenerator
parameter_list|(
name|int
name|minValueSize
parameter_list|,
name|int
name|maxValueSize
parameter_list|)
block|{
if|if
condition|(
name|minValueSize
operator|<=
literal|0
operator|||
name|maxValueSize
operator|<=
literal|0
condition|)
block|{
throw|throw
operator|new
name|IllegalArgumentException
argument_list|(
literal|"Invalid min/max value sizes: "
operator|+
name|minValueSize
operator|+
literal|", "
operator|+
name|maxValueSize
argument_list|)
throw|;
block|}
name|this
operator|.
name|minValueSize
operator|=
name|minValueSize
expr_stmt|;
name|this
operator|.
name|maxValueSize
operator|=
name|maxValueSize
expr_stmt|;
block|}
comment|/**    * Verifies that the given byte array is the same as what would be generated    * for the given row key and qualifier. We are assuming that the value size    * is correct, and only verify the actual bytes. However, if the min/max    * value sizes are set sufficiently high, an accidental match should be    * extremely improbable.    */
specifier|public
specifier|static
name|boolean
name|verify
parameter_list|(
name|String
name|rowKey
parameter_list|,
name|String
name|qual
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
block|{
name|byte
index|[]
name|expectedData
init|=
name|getValueForRowColumn
argument_list|(
name|rowKey
argument_list|,
name|qual
argument_list|,
name|value
operator|.
name|length
argument_list|)
decl_stmt|;
return|return
name|Bytes
operator|.
name|equals
argument_list|(
name|expectedData
argument_list|,
name|value
argument_list|)
return|;
block|}
comment|/**    * Converts the given key to string, and prefixes it with the MD5 hash of    * the index's string representation.    */
specifier|public
specifier|static
name|String
name|md5PrefixedKey
parameter_list|(
name|long
name|key
parameter_list|)
block|{
name|String
name|stringKey
init|=
name|Long
operator|.
name|toString
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|String
name|md5hash
init|=
name|MD5Hash
operator|.
name|getMD5AsHex
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|stringKey
argument_list|)
argument_list|)
decl_stmt|;
comment|// flip the key to randomize
return|return
name|md5hash
operator|+
literal|"-"
operator|+
name|stringKey
return|;
block|}
comment|/**    * Generates a value for the given key index and column qualifier. Size is    * selected randomly in the configured range. The generated value depends    * only on the combination of the key, qualifier, and the selected value    * size. This allows to verify the actual value bytes when reading, as done    * in {@link #verify(String, String, byte[])}.    */
specifier|public
name|byte
index|[]
name|generateRandomSizeValue
parameter_list|(
name|long
name|key
parameter_list|,
name|String
name|qual
parameter_list|)
block|{
name|String
name|rowKey
init|=
name|md5PrefixedKey
argument_list|(
name|key
argument_list|)
decl_stmt|;
name|int
name|dataSize
init|=
name|minValueSize
decl_stmt|;
if|if
condition|(
name|minValueSize
operator|!=
name|maxValueSize
condition|)
block|{
name|dataSize
operator|=
name|minValueSize
operator|+
name|randomForValueSize
operator|.
name|nextInt
argument_list|(
name|Math
operator|.
name|abs
argument_list|(
name|maxValueSize
operator|-
name|minValueSize
argument_list|)
argument_list|)
expr_stmt|;
block|}
return|return
name|getValueForRowColumn
argument_list|(
name|rowKey
argument_list|,
name|qual
argument_list|,
name|dataSize
argument_list|)
return|;
block|}
comment|/**    * Generates random bytes of the given size for the given row and column    * qualifier. The random seed is fully determined by these parameters.    */
specifier|private
specifier|static
name|byte
index|[]
name|getValueForRowColumn
parameter_list|(
name|String
name|rowKey
parameter_list|,
name|String
name|qual
parameter_list|,
name|int
name|dataSize
parameter_list|)
block|{
name|Random
name|seededRandom
init|=
operator|new
name|Random
argument_list|(
name|rowKey
operator|.
name|hashCode
argument_list|()
operator|+
name|qual
operator|.
name|hashCode
argument_list|()
operator|+
name|dataSize
argument_list|)
decl_stmt|;
name|byte
index|[]
name|randomBytes
init|=
operator|new
name|byte
index|[
name|dataSize
index|]
decl_stmt|;
name|seededRandom
operator|.
name|nextBytes
argument_list|(
name|randomBytes
argument_list|)
expr_stmt|;
return|return
name|randomBytes
return|;
block|}
block|}
end_class

end_unit

