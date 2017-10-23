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
name|http
operator|.
name|lib
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
import|import static
name|org
operator|.
name|mockito
operator|.
name|Mockito
operator|.
name|mock
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|FilterChain
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|FilterConfig
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|ServletResponse
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletRequest
import|;
end_import

begin_import
import|import
name|javax
operator|.
name|servlet
operator|.
name|http
operator|.
name|HttpServletRequestWrapper
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
name|fs
operator|.
name|CommonConfigurationKeys
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
name|MiscTests
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
name|http
operator|.
name|ServerConfigurationKeys
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
name|http
operator|.
name|lib
operator|.
name|StaticUserWebFilter
operator|.
name|StaticUserFilter
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
name|mockito
operator|.
name|ArgumentCaptor
import|;
end_import

begin_import
import|import
name|org
operator|.
name|mockito
operator|.
name|Mockito
import|;
end_import

begin_class
annotation|@
name|Category
argument_list|(
block|{
name|MiscTests
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
name|TestStaticUserWebFilter
block|{
specifier|private
name|FilterConfig
name|mockConfig
parameter_list|(
name|String
name|username
parameter_list|)
block|{
name|FilterConfig
name|mock
init|=
name|Mockito
operator|.
name|mock
argument_list|(
name|FilterConfig
operator|.
name|class
argument_list|)
decl_stmt|;
name|Mockito
operator|.
name|doReturn
argument_list|(
name|username
argument_list|)
operator|.
name|when
argument_list|(
name|mock
argument_list|)
operator|.
name|getInitParameter
argument_list|(
name|ServerConfigurationKeys
operator|.
name|HBASE_HTTP_STATIC_USER
argument_list|)
expr_stmt|;
return|return
name|mock
return|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testFilter
parameter_list|()
throws|throws
name|Exception
block|{
name|FilterConfig
name|config
init|=
name|mockConfig
argument_list|(
literal|"myuser"
argument_list|)
decl_stmt|;
name|StaticUserFilter
name|suf
init|=
operator|new
name|StaticUserFilter
argument_list|()
decl_stmt|;
name|suf
operator|.
name|init
argument_list|(
name|config
argument_list|)
expr_stmt|;
name|ArgumentCaptor
argument_list|<
name|HttpServletRequestWrapper
argument_list|>
name|wrapperArg
init|=
name|ArgumentCaptor
operator|.
name|forClass
argument_list|(
name|HttpServletRequestWrapper
operator|.
name|class
argument_list|)
decl_stmt|;
name|FilterChain
name|chain
init|=
name|mock
argument_list|(
name|FilterChain
operator|.
name|class
argument_list|)
decl_stmt|;
name|suf
operator|.
name|doFilter
argument_list|(
name|mock
argument_list|(
name|HttpServletRequest
operator|.
name|class
argument_list|)
argument_list|,
name|mock
argument_list|(
name|ServletResponse
operator|.
name|class
argument_list|)
argument_list|,
name|chain
argument_list|)
expr_stmt|;
name|Mockito
operator|.
name|verify
argument_list|(
name|chain
argument_list|)
operator|.
name|doFilter
argument_list|(
name|wrapperArg
operator|.
name|capture
argument_list|()
argument_list|,
name|Mockito
operator|.
expr|<
name|ServletResponse
operator|>
name|anyObject
argument_list|()
argument_list|)
expr_stmt|;
name|HttpServletRequestWrapper
name|wrapper
init|=
name|wrapperArg
operator|.
name|getValue
argument_list|()
decl_stmt|;
name|assertEquals
argument_list|(
literal|"myuser"
argument_list|,
name|wrapper
operator|.
name|getUserPrincipal
argument_list|()
operator|.
name|getName
argument_list|()
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"myuser"
argument_list|,
name|wrapper
operator|.
name|getRemoteUser
argument_list|()
argument_list|)
expr_stmt|;
name|suf
operator|.
name|destroy
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testOldStyleConfiguration
parameter_list|()
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
literal|"dfs.web.ugi"
argument_list|,
literal|"joe,group1,group2"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"joe"
argument_list|,
name|StaticUserWebFilter
operator|.
name|getUsernameFromConf
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testConfiguration
parameter_list|()
block|{
name|Configuration
name|conf
init|=
operator|new
name|Configuration
argument_list|()
decl_stmt|;
name|conf
operator|.
name|set
argument_list|(
name|CommonConfigurationKeys
operator|.
name|HADOOP_HTTP_STATIC_USER
argument_list|,
literal|"dr.stack"
argument_list|)
expr_stmt|;
name|assertEquals
argument_list|(
literal|"dr.stack"
argument_list|,
name|StaticUserWebFilter
operator|.
name|getUsernameFromConf
argument_list|(
name|conf
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit
