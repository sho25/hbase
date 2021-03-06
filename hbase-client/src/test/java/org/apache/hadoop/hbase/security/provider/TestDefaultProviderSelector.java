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
import|import static
name|org
operator|.
name|junit
operator|.
name|Assert
operator|.
name|assertNotNull
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
name|Collections
import|;
end_import

begin_import
import|import
name|java
operator|.
name|util
operator|.
name|HashSet
import|;
end_import

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
name|SmallTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|Before
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

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|SmallTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestDefaultProviderSelector
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
name|TestDefaultProviderSelector
operator|.
name|class
argument_list|)
decl_stmt|;
name|BuiltInProviderSelector
name|selector
decl_stmt|;
annotation|@
name|Before
specifier|public
name|void
name|setup
parameter_list|()
block|{
name|selector
operator|=
operator|new
name|BuiltInProviderSelector
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalStateException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testExceptionOnMissingProviders
parameter_list|()
block|{
name|selector
operator|.
name|configure
argument_list|(
operator|new
name|Configuration
argument_list|(
literal|false
argument_list|)
argument_list|,
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|NullPointerException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testNullConfiguration
parameter_list|()
block|{
name|selector
operator|.
name|configure
argument_list|(
literal|null
argument_list|,
name|Collections
operator|.
name|emptySet
argument_list|()
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|NullPointerException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testNullProviderMap
parameter_list|()
block|{
name|selector
operator|.
name|configure
argument_list|(
operator|new
name|Configuration
argument_list|(
literal|false
argument_list|)
argument_list|,
literal|null
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
argument_list|(
name|expected
operator|=
name|IllegalStateException
operator|.
name|class
argument_list|)
specifier|public
name|void
name|testDuplicateProviders
parameter_list|()
block|{
name|Set
argument_list|<
name|SaslClientAuthenticationProvider
argument_list|>
name|providers
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|()
decl_stmt|;
name|providers
operator|.
name|add
argument_list|(
operator|new
name|SimpleSaslClientAuthenticationProvider
argument_list|()
argument_list|)
expr_stmt|;
name|providers
operator|.
name|add
argument_list|(
operator|new
name|SimpleSaslClientAuthenticationProvider
argument_list|()
argument_list|)
expr_stmt|;
name|selector
operator|.
name|configure
argument_list|(
operator|new
name|Configuration
argument_list|(
literal|false
argument_list|)
argument_list|,
name|providers
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testExpectedProviders
parameter_list|()
block|{
name|HashSet
argument_list|<
name|SaslClientAuthenticationProvider
argument_list|>
name|providers
init|=
operator|new
name|HashSet
argument_list|<>
argument_list|(
name|Arrays
operator|.
name|asList
argument_list|(
operator|new
name|SimpleSaslClientAuthenticationProvider
argument_list|()
argument_list|,
operator|new
name|GssSaslClientAuthenticationProvider
argument_list|()
argument_list|,
operator|new
name|DigestSaslClientAuthenticationProvider
argument_list|()
argument_list|)
argument_list|)
decl_stmt|;
name|selector
operator|.
name|configure
argument_list|(
operator|new
name|Configuration
argument_list|(
literal|false
argument_list|)
argument_list|,
name|providers
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"Simple provider was null"
argument_list|,
name|selector
operator|.
name|simpleAuth
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"Kerberos provider was null"
argument_list|,
name|selector
operator|.
name|krbAuth
argument_list|)
expr_stmt|;
name|assertNotNull
argument_list|(
literal|"Digest provider was null"
argument_list|,
name|selector
operator|.
name|digestAuth
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

