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
name|Set
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

begin_comment
comment|/**  * A generator of random data (keys/cfs/columns/values) for load testing.  * Contains LoadTestKVGenerator as a matter of convenience...  */
end_comment

begin_class
annotation|@
name|InterfaceAudience
operator|.
name|Private
specifier|public
specifier|abstract
class|class
name|LoadTestDataGenerator
block|{
specifier|protected
specifier|final
name|LoadTestKVGenerator
name|kvGenerator
decl_stmt|;
comment|// The mutate info column stores information
comment|// about update done to this column family this row.
specifier|public
specifier|final
specifier|static
name|byte
index|[]
name|MUTATE_INFO
init|=
literal|"mutate_info"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
comment|// The increment column always has a long value,
comment|// which can be incremented later on during updates.
specifier|public
specifier|final
specifier|static
name|byte
index|[]
name|INCREMENT
init|=
literal|"increment"
operator|.
name|getBytes
argument_list|()
decl_stmt|;
comment|/**    * Initializes the object.    * @param minValueSize minimum size of the value generated by    * {@link #generateValue(byte[], byte[], byte[])}.    * @param maxValueSize maximum size of the value generated by    * {@link #generateValue(byte[], byte[], byte[])}.    */
specifier|public
name|LoadTestDataGenerator
parameter_list|(
name|int
name|minValueSize
parameter_list|,
name|int
name|maxValueSize
parameter_list|)
block|{
name|this
operator|.
name|kvGenerator
operator|=
operator|new
name|LoadTestKVGenerator
argument_list|(
name|minValueSize
argument_list|,
name|maxValueSize
argument_list|)
expr_stmt|;
block|}
comment|/**    * Generates a deterministic, unique hashed row key from a number. That way, the user can    * keep track of numbers, without messing with byte array and ensuring key distribution.    * @param keyBase Base number for a key, such as a loop counter.    */
specifier|public
specifier|abstract
name|byte
index|[]
name|getDeterministicUniqueKey
parameter_list|(
name|long
name|keyBase
parameter_list|)
function_decl|;
comment|/**    * Gets column families for the load test table.    * @return The array of byte[]s representing column family names.    */
specifier|public
specifier|abstract
name|byte
index|[]
index|[]
name|getColumnFamilies
parameter_list|()
function_decl|;
comment|/**    * Generates an applicable set of columns to be used for a particular key and family.    * @param rowKey The row key to generate for.    * @param cf The column family name to generate for.    * @return The array of byte[]s representing column names.    */
specifier|public
specifier|abstract
name|byte
index|[]
index|[]
name|generateColumnsForCf
parameter_list|(
name|byte
index|[]
name|rowKey
parameter_list|,
name|byte
index|[]
name|cf
parameter_list|)
function_decl|;
comment|/**    * Generates a value to be used for a particular row/cf/column.    * @param rowKey The row key to generate for.    * @param cf The column family name to generate for.    * @param column The column name to generate for.    * @return The value to use.    */
specifier|public
specifier|abstract
name|byte
index|[]
name|generateValue
parameter_list|(
name|byte
index|[]
name|rowKey
parameter_list|,
name|byte
index|[]
name|cf
parameter_list|,
name|byte
index|[]
name|column
parameter_list|)
function_decl|;
comment|/**    * Checks that columns for a rowKey and cf are valid if generated via    * {@link #generateColumnsForCf(byte[], byte[])}    * @param rowKey The row key to verify for.    * @param cf The column family name to verify for.    * @param columnSet The column set (for example, encountered by read).    * @return True iff valid.    */
specifier|public
specifier|abstract
name|boolean
name|verify
parameter_list|(
name|byte
index|[]
name|rowKey
parameter_list|,
name|byte
index|[]
name|cf
parameter_list|,
name|Set
argument_list|<
name|byte
index|[]
argument_list|>
name|columnSet
parameter_list|)
function_decl|;
comment|/**    * Checks that value for a rowKey/cf/column is valid if generated via    * {@link #generateValue(byte[], byte[], byte[])}    * @param rowKey The row key to verify for.    * @param cf The column family name to verify for.    * @param column The column name to verify for.    * @param value The value (for example, encountered by read).    * @return True iff valid.    */
specifier|public
specifier|abstract
name|boolean
name|verify
parameter_list|(
name|byte
index|[]
name|rowKey
parameter_list|,
name|byte
index|[]
name|cf
parameter_list|,
name|byte
index|[]
name|column
parameter_list|,
name|byte
index|[]
name|value
parameter_list|)
function_decl|;
block|}
end_class

end_unit

