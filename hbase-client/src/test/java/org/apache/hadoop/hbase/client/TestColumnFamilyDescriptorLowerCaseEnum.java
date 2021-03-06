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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertEquals
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|InvocationTargetException
import|;
end_import

begin_import
import|import
name|java
operator|.
name|lang
operator|.
name|reflect
operator|.
name|Method
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
name|HashMap
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|Map
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|NoSuchElementException
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
name|HBaseClassTestRule
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
name|Bytes
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|ClassRule
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
name|slf4j
operator|.
name|Logger
import|;
end_import

begin_import
import|import
name|org
operator|.
name|slf4j
operator|.
name|LoggerFactory
import|;
end_import

begin_comment
comment|/**  * Testcase for HBASE-21732. Make sure that all enum configurations can accept lower case value.  */
end_comment

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
name|TestColumnFamilyDescriptorLowerCaseEnum
block|{
annotation|@
name|ClassRule
specifier|public
specifier|static
specifier|final
name|HBaseClassTestRule
name|CLASS_RULE
init|=
name|HBaseClassTestRule
operator|.
name|forClass
argument_list|(
name|TestColumnFamilyDescriptorLowerCaseEnum
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|Logger
name|LOG
init|=
name|LoggerFactory
operator|.
name|getLogger
argument_list|(
name|TestColumnFamilyDescriptorLowerCaseEnum
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
name|Method
name|getSetMethod
parameter_list|(
name|Method
name|getMethod
parameter_list|,
name|Class
argument_list|<
name|?
argument_list|>
name|enumType
parameter_list|)
throws|throws
name|NoSuchMethodException
block|{
name|String
name|methodName
init|=
name|getMethod
operator|.
name|getName
argument_list|()
operator|.
name|replaceFirst
argument_list|(
literal|"get"
argument_list|,
literal|"set"
argument_list|)
decl_stmt|;
return|return
name|ColumnFamilyDescriptorBuilder
operator|.
name|class
operator|.
name|getMethod
argument_list|(
name|methodName
argument_list|,
name|enumType
argument_list|)
return|;
block|}
specifier|private
name|Enum
argument_list|<
name|?
argument_list|>
name|getEnumValue
parameter_list|(
name|Class
argument_list|<
name|?
argument_list|>
name|enumType
parameter_list|)
block|{
for|for
control|(
name|Enum
argument_list|<
name|?
argument_list|>
name|enumConst
range|:
name|enumType
operator|.
name|asSubclass
argument_list|(
name|Enum
operator|.
name|class
argument_list|)
operator|.
name|getEnumConstants
argument_list|()
control|)
block|{
if|if
condition|(
operator|!
name|enumConst
operator|.
name|name
argument_list|()
operator|.
name|equalsIgnoreCase
argument_list|(
literal|"NONE"
argument_list|)
operator|&&
operator|!
name|enumConst
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
literal|"DEFAULT"
argument_list|)
condition|)
block|{
return|return
name|enumConst
return|;
block|}
block|}
throw|throw
operator|new
name|NoSuchElementException
argument_list|(
name|enumType
operator|.
name|getName
argument_list|()
argument_list|)
throw|;
block|}
specifier|private
name|boolean
name|contains
parameter_list|(
name|Collection
argument_list|<
name|Enum
argument_list|<
name|?
argument_list|>
argument_list|>
name|enumConsts
parameter_list|,
name|String
name|value
parameter_list|)
block|{
return|return
name|enumConsts
operator|.
name|stream
argument_list|()
operator|.
name|anyMatch
argument_list|(
name|e
lambda|->
name|e
operator|.
name|name
argument_list|()
operator|.
name|equals
argument_list|(
name|value
argument_list|)
argument_list|)
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|test
parameter_list|()
throws|throws
name|IllegalAccessException
throws|,
name|InvocationTargetException
throws|,
name|NoSuchMethodException
block|{
name|Map
argument_list|<
name|Method
argument_list|,
name|Enum
argument_list|<
name|?
argument_list|>
argument_list|>
name|getMethod2Value
init|=
operator|new
name|HashMap
argument_list|<>
argument_list|()
decl_stmt|;
name|ColumnFamilyDescriptorBuilder
name|builder
init|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test"
argument_list|)
argument_list|)
decl_stmt|;
for|for
control|(
name|Method
name|method
range|:
name|ColumnFamilyDescriptor
operator|.
name|class
operator|.
name|getMethods
argument_list|()
control|)
block|{
if|if
condition|(
name|method
operator|.
name|getParameterCount
argument_list|()
operator|==
literal|0
operator|&&
name|method
operator|.
name|getReturnType
argument_list|()
operator|.
name|isEnum
argument_list|()
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Checking "
operator|+
name|method
argument_list|)
expr_stmt|;
name|Class
argument_list|<
name|?
argument_list|>
name|enumType
init|=
name|method
operator|.
name|getReturnType
argument_list|()
decl_stmt|;
name|Method
name|setMethod
init|=
name|getSetMethod
argument_list|(
name|method
argument_list|,
name|enumType
argument_list|)
decl_stmt|;
name|Enum
argument_list|<
name|?
argument_list|>
name|enumConst
init|=
name|getEnumValue
argument_list|(
name|enumType
argument_list|)
decl_stmt|;
name|LOG
operator|.
name|info
argument_list|(
literal|"Using "
operator|+
name|setMethod
operator|+
literal|" to set the value to "
operator|+
name|enumConst
argument_list|)
expr_stmt|;
name|setMethod
operator|.
name|invoke
argument_list|(
name|builder
argument_list|,
name|enumConst
argument_list|)
expr_stmt|;
name|getMethod2Value
operator|.
name|put
argument_list|(
name|method
argument_list|,
name|enumConst
argument_list|)
expr_stmt|;
block|}
block|}
name|ColumnFamilyDescriptor
name|desc
init|=
name|builder
operator|.
name|build
argument_list|()
decl_stmt|;
name|ColumnFamilyDescriptorBuilder
name|builder2
init|=
name|ColumnFamilyDescriptorBuilder
operator|.
name|newBuilder
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
literal|"test2"
argument_list|)
argument_list|)
decl_stmt|;
name|desc
operator|.
name|getValues
argument_list|()
operator|.
name|forEach
argument_list|(
parameter_list|(
name|k
parameter_list|,
name|v
parameter_list|)
lambda|->
block|{
name|LOG
operator|.
name|info
argument_list|(
name|k
operator|.
name|toString
argument_list|()
operator|+
literal|"=>"
operator|+
name|v
operator|.
name|toString
argument_list|()
argument_list|)
expr_stmt|;
name|String
name|str
init|=
name|Bytes
operator|.
name|toString
argument_list|(
name|v
operator|.
name|get
argument_list|()
argument_list|,
name|v
operator|.
name|getOffset
argument_list|()
argument_list|,
name|v
operator|.
name|getLength
argument_list|()
argument_list|)
decl_stmt|;
if|if
condition|(
name|contains
argument_list|(
name|getMethod2Value
operator|.
name|values
argument_list|()
argument_list|,
name|str
argument_list|)
condition|)
block|{
name|LOG
operator|.
name|info
argument_list|(
literal|"Set to lower case "
operator|+
name|str
operator|.
name|toLowerCase
argument_list|()
argument_list|)
expr_stmt|;
name|builder2
operator|.
name|setValue
argument_list|(
name|k
argument_list|,
operator|new
name|Bytes
argument_list|(
name|Bytes
operator|.
name|toBytes
argument_list|(
name|str
operator|.
name|toLowerCase
argument_list|()
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
argument_list|)
expr_stmt|;
name|ColumnFamilyDescriptor
name|desc2
init|=
name|builder2
operator|.
name|build
argument_list|()
decl_stmt|;
for|for
control|(
name|Map
operator|.
name|Entry
argument_list|<
name|Method
argument_list|,
name|Enum
argument_list|<
name|?
argument_list|>
argument_list|>
name|entry
range|:
name|getMethod2Value
operator|.
name|entrySet
argument_list|()
control|)
block|{
name|assertEquals
argument_list|(
name|entry
operator|.
name|getKey
argument_list|()
operator|+
literal|" should return "
operator|+
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|entry
operator|.
name|getValue
argument_list|()
argument_list|,
name|entry
operator|.
name|getKey
argument_list|()
operator|.
name|invoke
argument_list|(
name|desc2
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
block|}
end_class

end_unit

