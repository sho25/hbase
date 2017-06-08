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
name|client
package|;
end_package

begin_import
import|import
name|java
operator|.
name|io
operator|.
name|IOException
import|;
end_import

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
name|List
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|function
operator|.
name|Consumer
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
name|HColumnDescriptor
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
name|HTableDescriptor
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
name|TableName
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
name|testclassification
operator|.
name|ClientTests
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
name|testclassification
operator|.
name|SmallTests
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
name|BuilderStyleTest
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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|fail
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Rule
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Test
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
name|rules
operator|.
name|TestName
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|ClientTests
operator|.
name|class
block|,
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestImmutableHTableDescriptor
block|{
annotation|@
name|Rule
specifier|public
name|TestName
name|name
init|=
operator|new
name|TestName
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|List
argument_list|<
name|Consumer
argument_list|<
name|ImmutableHTableDescriptor
argument_list|>
argument_list|>
name|TEST_FUNCTION
init|=
name|Arrays
operator|.
name|asList
argument_list|(
name|htd
lambda|->
name|htd
operator|.
name|setValue
argument_list|(
literal|"a"
argument_list|,
literal|"a"
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|setValue
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|,
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|setValue
argument_list|(
operator|new
name|Bytes
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|)
argument_list|,
operator|new
name|Bytes
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"a"
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|setCompactionEnabled
argument_list|(
literal|false
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|setConfiguration
argument_list|(
literal|"aaa"
argument_list|,
literal|"ccc"
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|setDurability
argument_list|(
name|Durability
operator|.
name|USE_DEFAULT
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|setFlushPolicyClassName
argument_list|(
literal|"class"
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|setMaxFileSize
argument_list|(
literal|123
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|setMemStoreFlushSize
argument_list|(
literal|123123123
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|setNormalizationEnabled
argument_list|(
literal|false
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|setPriority
argument_list|(
literal|123
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|setReadOnly
argument_list|(
literal|true
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|setRegionMemstoreReplication
argument_list|(
literal|true
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|setRegionReplication
argument_list|(
literal|123
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|setRegionSplitPolicyClassName
argument_list|(
literal|"class"
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fm"
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|remove
argument_list|(
operator|new
name|Bytes
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
argument_list|)
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|remove
argument_list|(
literal|"aaa"
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|remove
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"aaa"
argument_list|)
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|removeConfiguration
argument_list|(
literal|"xxx"
argument_list|)
argument_list|,
name|htd
lambda|->
name|htd
operator|.
name|removeFamily
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"fm"
argument_list|)
argument_list|)
argument_list|,
name|htd
lambda|->
block|{
lambda|try
block|{
name|htd
operator|.
name|addCoprocessor
argument_list|(
literal|"xxx"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|IOException
name|e
parameter_list|)
block|{
throw|throw
operator|new
name|RuntimeException
argument_list|(
name|e
argument_list|)
throw|;
block|}
block|}
end_class

begin_empty_stmt
unit|)
empty_stmt|;
end_empty_stmt

begin_function
annotation|@
name|Test
specifier|public
name|void
name|testImmutable
parameter_list|()
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|ImmutableHTableDescriptor
name|immutableHtd
init|=
operator|new
name|ImmutableHTableDescriptor
argument_list|(
name|htd
argument_list|)
decl_stmt|;
name|TEST_FUNCTION
operator|.
name|forEach
argument_list|(
name|f
lambda|->
block|{
try|try
block|{
name|f
operator|.
name|accept
argument_list|(
name|immutableHtd
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"ImmutableHTableDescriptor can't be modified!!!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{       }
block|}
argument_list|)
expr_stmt|;
block|}
end_function

begin_function
annotation|@
name|Test
specifier|public
name|void
name|testImmutableHColumnDescriptor
parameter_list|()
block|{
name|HTableDescriptor
name|htd
init|=
operator|new
name|HTableDescriptor
argument_list|(
name|TableName
operator|.
name|valueOf
argument_list|(
name|name
operator|.
name|getMethodName
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|htd
operator|.
name|addFamily
argument_list|(
operator|new
name|HColumnDescriptor
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"family"
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|ImmutableHTableDescriptor
name|immutableHtd
init|=
operator|new
name|ImmutableHTableDescriptor
argument_list|(
name|htd
argument_list|)
decl_stmt|;
for|for
control|(
name|HColumnDescriptor
name|hcd
range|:
name|immutableHtd
operator|.
name|getColumnFamilies
argument_list|()
control|)
block|{
name|assertReadOnly
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
block|}
for|for
control|(
name|HColumnDescriptor
name|hcd
range|:
name|immutableHtd
operator|.
name|getFamilies
argument_list|()
control|)
block|{
name|assertReadOnly
argument_list|(
name|hcd
argument_list|)
expr_stmt|;
block|}
block|}
end_function

begin_function
specifier|private
name|void
name|assertReadOnly
parameter_list|(
name|HColumnDescriptor
name|hcd
parameter_list|)
block|{
try|try
block|{
name|hcd
operator|.
name|setBlocksize
argument_list|(
literal|10
argument_list|)
expr_stmt|;
name|fail
argument_list|(
literal|"ImmutableHColumnDescriptor can't be modified!!!"
argument_list|)
expr_stmt|;
block|}
catch|catch
parameter_list|(
name|UnsupportedOperationException
name|e
parameter_list|)
block|{     }
block|}
end_function

begin_function
annotation|@
name|Test
specifier|public
name|void
name|testClassMethodsAreBuilderStyle
parameter_list|()
block|{
comment|/* ImmutableHTableDescriptor should have a builder style setup where setXXX/addXXX methods    * can be chainable together:    * . For example:    * ImmutableHTableDescriptor d    *   = new ImmutableHTableDescriptor()    *     .setFoo(foo)    *     .setBar(bar)    *     .setBuz(buz)    *    * This test ensures that all methods starting with "set" returns the declaring object    */
name|BuilderStyleTest
operator|.
name|assertClassesAreBuilderStyle
argument_list|(
name|ImmutableHTableDescriptor
operator|.
name|class
argument_list|)
expr_stmt|;
block|}
end_function

unit|}
end_unit

