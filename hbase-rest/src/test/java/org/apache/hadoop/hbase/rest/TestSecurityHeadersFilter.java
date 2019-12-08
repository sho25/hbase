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
name|rest
package|;
end_package

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|CoreMatchers
operator|.
name|not
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|core
operator|.
name|Is
operator|.
name|is
import|;
end_import

begin_import
import|import static
name|org
operator|.
name|hamcrest
operator|.
name|core
operator|.
name|IsEqual
operator|.
name|equalTo
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
name|assertThat
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
name|HBaseTestingUtility
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
name|rest
operator|.
name|client
operator|.
name|Client
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
name|rest
operator|.
name|client
operator|.
name|Cluster
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
name|rest
operator|.
name|client
operator|.
name|Response
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
name|MediumTests
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
name|RestTests
import|;
end_import

begin_import
import|import
name|org
operator|.
name|junit
operator|.
name|After
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
name|RestTests
operator|.
name|class
block|,
name|MediumTests
operator|.
name|class
block|}
argument_list|)
specifier|public
class|class
name|TestSecurityHeadersFilter
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
name|TestSecurityHeadersFilter
operator|.
name|class
argument_list|)
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseTestingUtility
name|TEST_UTIL
init|=
operator|new
name|HBaseTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
specifier|final
name|HBaseRESTTestingUtility
name|REST_TEST_UTIL
init|=
operator|new
name|HBaseRESTTestingUtility
argument_list|()
decl_stmt|;
specifier|private
specifier|static
name|Client
name|client
decl_stmt|;
annotation|@
name|After
specifier|public
name|void
name|tearDown
parameter_list|()
throws|throws
name|Exception
block|{
name|REST_TEST_UTIL
operator|.
name|shutdownServletContainer
argument_list|()
expr_stmt|;
name|TEST_UTIL
operator|.
name|shutdownMiniCluster
argument_list|()
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testDefaultValues
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|REST_TEST_UTIL
operator|.
name|startServletContainer
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|=
operator|new
name|Client
argument_list|(
operator|new
name|Cluster
argument_list|()
operator|.
name|add
argument_list|(
literal|"localhost"
argument_list|,
name|REST_TEST_UTIL
operator|.
name|getServletPort
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|path
init|=
literal|"/version/cluster"
decl_stmt|;
name|Response
name|response
init|=
name|client
operator|.
name|get
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|200
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Header 'X-Content-Type-Options' is missing from Rest response"
argument_list|,
name|response
operator|.
name|getHeader
argument_list|(
literal|"X-Content-Type-Options"
argument_list|)
argument_list|,
name|is
argument_list|(
name|not
argument_list|(
operator|(
name|String
operator|)
literal|null
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Header 'X-Content-Type-Options' has invalid default value"
argument_list|,
name|response
operator|.
name|getHeader
argument_list|(
literal|"X-Content-Type-Options"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"nosniff"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Header 'X-XSS-Protection' is missing from Rest response"
argument_list|,
name|response
operator|.
name|getHeader
argument_list|(
literal|"X-XSS-Protection"
argument_list|)
argument_list|,
name|is
argument_list|(
name|not
argument_list|(
operator|(
name|String
operator|)
literal|null
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Header 'X-XSS-Protection' has invalid default value"
argument_list|,
name|response
operator|.
name|getHeader
argument_list|(
literal|"X-XSS-Protection"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"1; mode=block"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Header 'Strict-Transport-Security' should be missing from Rest response,"
operator|+
literal|"but it's present"
argument_list|,
name|response
operator|.
name|getHeader
argument_list|(
literal|"Strict-Transport-Security"
argument_list|)
argument_list|,
name|is
argument_list|(
operator|(
name|String
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Header 'Content-Security-Policy' should be missing from Rest response,"
operator|+
literal|"but it's present"
argument_list|,
name|response
operator|.
name|getHeader
argument_list|(
literal|"Content-Security-Policy"
argument_list|)
argument_list|,
name|is
argument_list|(
operator|(
name|String
operator|)
literal|null
argument_list|)
argument_list|)
expr_stmt|;
block|}
annotation|@
name|Test
specifier|public
name|void
name|testHstsAndCspSettings
parameter_list|()
throws|throws
name|Exception
block|{
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
literal|"hbase.http.filter.hsts.value"
argument_list|,
literal|"max-age=63072000;includeSubDomains;preload"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
operator|.
name|set
argument_list|(
literal|"hbase.http.filter.csp.value"
argument_list|,
literal|"default-src https: data: 'unsafe-inline' 'unsafe-eval'"
argument_list|)
expr_stmt|;
name|TEST_UTIL
operator|.
name|startMiniCluster
argument_list|()
expr_stmt|;
name|REST_TEST_UTIL
operator|.
name|startServletContainer
argument_list|(
name|TEST_UTIL
operator|.
name|getConfiguration
argument_list|()
argument_list|)
expr_stmt|;
name|client
operator|=
operator|new
name|Client
argument_list|(
operator|new
name|Cluster
argument_list|()
operator|.
name|add
argument_list|(
literal|"localhost"
argument_list|,
name|REST_TEST_UTIL
operator|.
name|getServletPort
argument_list|()
argument_list|)
argument_list|)
expr_stmt|;
name|String
name|path
init|=
literal|"/version/cluster"
decl_stmt|;
name|Response
name|response
init|=
name|client
operator|.
name|get
argument_list|(
name|path
argument_list|)
decl_stmt|;
name|assertThat
argument_list|(
name|response
operator|.
name|getCode
argument_list|()
argument_list|,
name|equalTo
argument_list|(
literal|200
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Header 'Strict-Transport-Security' is missing from Rest response"
argument_list|,
name|response
operator|.
name|getHeader
argument_list|(
literal|"Strict-Transport-Security"
argument_list|)
argument_list|,
name|is
argument_list|(
name|not
argument_list|(
operator|(
name|String
operator|)
literal|null
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Header 'Strict-Transport-Security' has invalid value"
argument_list|,
name|response
operator|.
name|getHeader
argument_list|(
literal|"Strict-Transport-Security"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"max-age=63072000;includeSubDomains;preload"
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Header 'Content-Security-Policy' is missing from Rest response"
argument_list|,
name|response
operator|.
name|getHeader
argument_list|(
literal|"Content-Security-Policy"
argument_list|)
argument_list|,
name|is
argument_list|(
name|not
argument_list|(
operator|(
name|String
operator|)
literal|null
argument_list|)
argument_list|)
argument_list|)
expr_stmt|;
name|assertThat
argument_list|(
literal|"Header 'Content-Security-Policy' has invalid value"
argument_list|,
name|response
operator|.
name|getHeader
argument_list|(
literal|"Content-Security-Policy"
argument_list|)
argument_list|,
name|equalTo
argument_list|(
literal|"default-src https: data: 'unsafe-inline' 'unsafe-eval'"
argument_list|)
argument_list|)
expr_stmt|;
block|}
block|}
end_class

end_unit

