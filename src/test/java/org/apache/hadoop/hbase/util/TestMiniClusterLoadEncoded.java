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
package|;
end_package

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|ArrayList
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Collection
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|List
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
name|LargeTests
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
name|io
operator|.
name|encoding
operator|.
name|DataBlockEncoding
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|experimental
operator|.
name|categories
operator|.
name|Category
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|runners
operator|.
name|Parameterized
operator|.
name|Parameters
import|;
end_import

begin_comment
comment|/**  * Runs a load test on a mini HBase cluster with data block encoding turned on.  * Compared to other load-test-style unit tests, this one writes a smaller  * amount of data, but goes through all available data block encoding  * algorithms.  */
end_comment

begin_class
annotation|@
name|Category
argument_list|(
name|LargeTests
operator|.
name|class
argument_list|)
specifier|public
class|class
name|TestMiniClusterLoadEncoded
extends|extends
name|TestMiniClusterLoadParallel
block|{
comment|/** We do not alternate the multi-put flag in this test. */
specifier|private
specifier|static
specifier|final
name|boolean
name|USE_MULTI_PUT
init|=
literal|true
decl_stmt|;
annotation|@
name|Parameters
specifier|public
specifier|static
name|Collection
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
parameter_list|()
block|{
name|List
argument_list|<
name|Object
index|[]
argument_list|>
name|parameters
init|=
operator|new
name|ArrayList
argument_list|<
name|Object
index|[]
argument_list|>
argument_list|()
decl_stmt|;
for|for
control|(
name|DataBlockEncoding
name|dataBlockEncoding
range|:
name|DataBlockEncoding
operator|.
name|values
argument_list|()
control|)
block|{
name|parameters
operator|.
name|add
argument_list|(
operator|new
name|Object
index|[]
block|{
name|dataBlockEncoding
block|}
argument_list|)
expr_stmt|;
block|}
return|return
name|parameters
return|;
block|}
specifier|public
name|TestMiniClusterLoadEncoded
parameter_list|(
name|DataBlockEncoding
name|encoding
parameter_list|)
block|{
name|super
argument_list|(
name|USE_MULTI_PUT
argument_list|,
name|encoding
argument_list|)
expr_stmt|;
block|}
comment|/**    * Use a smaller number of keys in in this test.    */
annotation|@
name|Override
specifier|protected
name|int
name|numKeys
parameter_list|()
block|{
return|return
literal|3000
return|;
block|}
block|}
end_class

end_unit

